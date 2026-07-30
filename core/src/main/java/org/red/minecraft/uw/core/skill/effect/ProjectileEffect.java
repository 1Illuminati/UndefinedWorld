package org.red.minecraft.uw.core.skill.effect;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.combat.damage.DamageType;
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
    /**
     * 발사 높이 보정 (시전자 위치 기준).
     * <p>§2.10 확정: <b>발사 높이는 스킬마다 다르게 둔다 — 이펙트 간 통일하지 않는다.</b>
     * (SwordAuraEffect 는 1.2 로 다르며, 그것이 의도된 상태다)
     */
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

        if (directions.isEmpty()) {
            UndefinedWorldCorePlugin.sendLog("ProjectileEffect: 미구현 발사 형태라 발사 없음 shape=" + shape);
            return CompletableFuture.completedFuture(EffectResult.FAIL);
        }

        CompletableFuture<EffectResult> future = new CompletableFuture<>();
        List<A_Entity> hits = new ArrayList<>();
        AtomicInteger remaining = new AtomicInteger(directions.size());
        Predicate<Entity> filter = Faction.predicate(caster, faction);

        // 발사체 시각효과 (확정: DUST 파티클 + 속성별 색). 발사체 전체가 같은 옵션을 공유한다.
        // 이 이펙트는 데미지를 주지 않아(후속 기어 담당) 자체 DamageType 이 없다. 무속성일 때의 회색/파랑은
        // 스킬 전체의 데미지 유형인 CTX.DAMAGE_TYPE 으로 가른다. 그것도 없으면 물리(회색) 폴백 —
        // 'damage' 기어 팩토리의 damageType 기본값이 PHYSICAL 인 것과 맞춘 것이다.
        DamageType visualDamageType = ctx.getCTX(CTXType.DAMAGE_TYPE, DamageType.PHYSICAL);
        Particle.DustOptions dust = SkillParticle.dust(ctx.getCTX(CTXType.ELEMENTAL), visualDamageType != DamageType.MAGIC);
        // SIZE 수정자가 음수/NaN을 만들 수 있다. 파티클 확산값에 그대로 넘기면 매 틱 예외가 나
        // Controller.runTick이 발사체를 즉시 종료시킨다. 시각효과 때문에 발사체가 죽으면 안 되므로 여기서 정리한다.
        double visualSize = Double.isFinite(render) ? Math.abs(render) : 0.0;

        for (Vector direction : directions) {
            ProjectileData data = new ProjectileData(caster, start.clone(), direction, speed, range, render, type);

            ProjectileController controller = new ProjectileController(
                    data,
                    filter,
                    hitData -> {
                        // 메인스레드 틱에서 호출됨 — 동기화 불필요
                        // 완료 집계는 여기서 하지 않는다. ProjectileController는 모든 종료 경로(적중/사거리/진행불가)를
                        // onExpire로 모아 발사체 1기당 정확히 1회 호출하므로, 여기서 세면 이중 집계가 되어
                        // 여러 발일 때 나머지 발이 끝나기 전에 future가 완료된다.
                        for (A_Entity entity : hitData.entities()) {
                            if (entity == null) continue;
                            hits.add(entity);
                        }
                    },
                    () -> finishOne(ctx, future, hits, remaining)
            );

            if (type == ProjectileType.GUIDED && ctx.hasCTX(CTXType.LAST_TARGET_INFO)) {
                controller.setGuidedTarget(firstValidTarget(ctx.getCTX(CTXType.LAST_TARGET_INFO)));
            }

            controller.setMoveVisual(loc -> {
                if (loc.getWorld() == null) return;
                loc.getWorld().spawnParticle(Particle.DUST, loc, 8, visualSize * 0.3, visualSize * 0.3, visualSize * 0.3, 0, dust);
            });

            controller.start();
        }

        return future;
    }

    /**
     * 발사체 1기 종료 집계. 전부 종료되면 결과를 확정한다.
     * <p>이 콜백이 예외로 빠져나가면 future 가 영구 미완료로 남아 스킬 체인이 조용히 멈춘다.
     * 마지막 1기의 종료 콜백은 다시 오지 않으므로, 여기서 반드시 완료시킨다.
     */
    private void finishOne(SkillCTX ctx, CompletableFuture<EffectResult> future,
                           List<A_Entity> hits, AtomicInteger remaining) {
        if (remaining.decrementAndGet() > 0) return;

        try {
            if (hits.isEmpty()) {
                UndefinedWorldCorePlugin.sendLog("ProjectileEffect: 적중 대상 없음");
                future.complete(EffectResult.FAIL);
                return;
            }

            ctx.setCTX(CTXType.LAST_TARGET_INFO, hits.toArray(new A_Entity[0]));
            future.complete(EffectResult.SUCCESS);
        } catch (RuntimeException exception) {
            UndefinedWorldCorePlugin.sendLog("ProjectileEffect 종료 집계 실패 - " + exception);
            future.complete(EffectResult.ERROR);
        }
    }

    /**
     * GUIDED 유도 대상 선정 — 앞선 노드가 넘긴 대상 중 첫 유효 대상.
     * (배열에 null 이나 이미 죽은 대상이 섞여 있으면 유도가 무의미하게 꺼진다)
     * @return 없으면 null (직선 진행)
     */
    @Nullable
    private A_Entity firstValidTarget(@Nullable A_Entity[] targets) {
        if (targets == null) return null;

        for (A_Entity target : targets) {
            if (target != null && !target.isDead()) return target;
        }
        return null;
    }

    /**
     * 발사 형태별 방향 벡터 목록 생성 — 여기서 나온 개수만큼 발사체 컨트롤러가 생성된다.
     *
     * <p><b>발사 개수 상한은 두지 않는다 (사용자 확정).</b> COUNT가 커지는 것은 기어 파워/쿨타임/비용 등
     * 밸런스로 관리한다. 아래 {@code Math.max(1, count)} / {@code count <= 1} 처리는 상한이 아니라
     * <b>하한</b>이다(0 이하여도 최소 1발). 상한 클램프를 다시 넣지 말 것.
     */
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
                // §2.10 확정: CIRCLE 은 현행유지.
                // Y축 회전이라 시전자가 아래(또는 위)를 보면 원이 아니라 원뿔로 퍼진다.
                // 이는 알려진 동작이며 고치지 않는 것이 확정 사항이다 — 수평 원으로 바꾸지 말 것.
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
