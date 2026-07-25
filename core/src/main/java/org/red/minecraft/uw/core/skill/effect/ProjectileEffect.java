package org.red.minecraft.uw.core.skill.effect;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.projectile.ProjectileController;
import org.red.minecraft.uw.core.skill.projectile.ProjectileData;
import org.red.minecraft.uw.core.skill.projectile.ProjectileType;
import org.red.minecraft.uw.core.skill.projectile.ProjectilesShape;
import org.red.minecraft.uw.core.skill.target.faction.Faction;
import org.red.minecraft.uw.core.skill.target.faction.FactionType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * 발사체 스킬 이펙트.
 *
 * 구조 결정 2.5-2: 적중 시 LAST_TARGET_INFO 에 적중 엔티티를 누적 저장하고,
 * 모든 발사체가 종료(적중/사거리만료)된 시점에 SUCCESS 를 완료해 다음 노드로 넘긴다.
 * 데미지는 이 이펙트가 아닌 후속 기어(ThunderEffect 등)가 담당한다.
 *
 * CTX 수정자 반영:
 *   SPEED/RANGE/SIZE — 기본값(base*)에 배율 적용
 *   COUNT            — SPREAD/CIRCLE 발사 수
 *   PROJECTILE_TYPE  — NORMAL/PIERCE/GUIDED (기본 NORMAL)
 *   PROJECTILE_SHAPE — SINGLE/SPREAD/CIRCLE (기본 SINGLE)
 *   TARGET_FACTION   — 충돌 대상 (기본 ENEMY)
 *   LAST_TARGET_INFO — GUIDED 유도 타겟 (이전 노드가 지정한 첫 대상)
 *
 * RAIN/SELF/POINT 형태는 위치 지정 방식 설계 확정 후 구현 (todo, 현재 FAIL 처리)
 */
public class ProjectileEffect implements Effect {

    /** SPREAD 부채꼴 전체 각도 — todo 밸런스 확정 필요 (임시 45도) */
    private static final double SPREAD_ANGLE_DEG = 45.0;
    /** 발사 높이 보정 (시전자 위치 기준) — todo 눈높이 처리 확정 필요 (임시 1.5) */
    private static final double LAUNCH_HEIGHT = 1.5;

    private final double baseSpeed;
    private final double baseRange;
    private final double baseRender;

    /**
     * @param baseSpeed  틱당 이동 거리 기본값
     * @param baseRange  최대 사거리 기본값
     * @param baseRender 충돌 감지 반경 기본값
     */
    public ProjectileEffect(double baseSpeed, double baseRange, double baseRender) {
        this.baseSpeed = baseSpeed;
        this.baseRange = baseRange;
        this.baseRender = baseRender;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        A_Entity caster = ctx.getCTX(CTXType.CASTER);

        ProjectileType type = ctx.getCTX(CTXType.PROJECTILE_TYPE, ProjectileType.NORMAL);
        ProjectilesShape shape = ctx.getCTX(CTXType.PROJECTILE_SHAPE, ProjectilesShape.SINGLE);
        FactionType faction = ctx.getCTX(CTXType.TARGET_FACTION, FactionType.ENEMY);

        double speed = baseSpeed * (double) ctx.getCTX(CTXType.SPEED);
        double range = baseRange * (double) ctx.getCTX(CTXType.RANGE);
        double render = baseRender * (double) ctx.getCTX(CTXType.SIZE);
        int count = ctx.getCTX(CTXType.COUNT);

        Location start = caster.getLocation().clone().add(0, LAUNCH_HEIGHT, 0);
        List<Vector> directions = resolveDirections(shape, caster.getLocation().getDirection(), count);

        if (directions.isEmpty()) return CompletableFuture.completedFuture(EffectResult.FAIL);

        CompletableFuture<EffectResult> future = new CompletableFuture<>();
        List<A_Entity> hits = new ArrayList<>();
        AtomicInteger remaining = new AtomicInteger(directions.size());
        Predicate<Entity> filter = Faction.predicate(caster, faction);

        for (Vector direction : directions) {
            ProjectileData data = new ProjectileData(caster, start.clone(), direction, speed, range, render, type);

            ProjectileController controller = new ProjectileController(
                    data,
                    filter,
                    hitData -> {
                        // 메인스레드 틱에서 호출됨 — 동기화 불필요
                        for (A_Entity entity : hitData.entities()) hits.add(entity);
                        // NORMAL은 첫 적중 시 컨트롤러가 스스로 종료(onExpire 미호출)하므로 여기서 완료 집계
                        if (type == ProjectileType.NORMAL) finishOne(ctx, future, hits, remaining);
                    },
                    () -> finishOne(ctx, future, hits, remaining)
            );

            if (type == ProjectileType.GUIDED && ctx.hasCTX(CTXType.LAST_TARGET_INFO)) {
                A_Entity[] lastTargets = ctx.getCTX(CTXType.LAST_TARGET_INFO);
                if (lastTargets != null && lastTargets.length > 0) controller.setGuidedTarget(lastTargets[0]);
            }

            controller.start();
        }

        return future;
    }

    /** 발사체 1기 종료 집계. 전부 종료되면 결과를 확정한다. */
    private void finishOne(SkillCTX ctx, CompletableFuture<EffectResult> future,
                           List<A_Entity> hits, AtomicInteger remaining) {
        if (remaining.decrementAndGet() > 0) return;

        if (hits.isEmpty()) {
            future.complete(EffectResult.FAIL);
            return;
        }

        ctx.setCTX(CTXType.LAST_TARGET_INFO, hits.toArray(new A_Entity[0]));
        future.complete(EffectResult.SUCCESS);
    }

    /** 발사 형태별 방향 벡터 목록 생성 */
    private List<Vector> resolveDirections(ProjectilesShape shape, Vector baseDirection, int count) {
        Vector base = baseDirection.clone().normalize();
        List<Vector> result = new ArrayList<>();

        switch (shape) {
            case SINGLE -> result.add(base);
            case SPREAD -> {
                if (count <= 1) {
                    result.add(base);
                    break;
                }
                double step = SPREAD_ANGLE_DEG / (count - 1);
                double startAngle = -SPREAD_ANGLE_DEG / 2;
                for (int i = 0; i < count; i++) {
                    result.add(rotateAroundY(base, Math.toRadians(startAngle + step * i)));
                }
            }
            case CIRCLE -> {
                int num = Math.max(1, count);
                double step = 360.0 / num;
                for (int i = 0; i < num; i++) {
                    result.add(rotateAroundY(base, Math.toRadians(step * i)));
                }
            }
            // todo RAIN/SELF/POINT: 위치 지정 방식(LocationTarget 연계) 설계 확정 필요
            case RAIN, SELF, POINT -> {}
        }

        return result;
    }

    private Vector rotateAroundY(Vector vector, double radians) {
        return vector.clone().rotateAroundY(radians);
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.ARROW, EffectType.TARGET};
    }
}
