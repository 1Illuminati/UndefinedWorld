package org.red.minecraft.uw.core.skill.projectile;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.skill.controller.Controller;
import org.red.minecraft.uw.core.skill.target.LastTargetData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 틱 기반 가상 발사체 컨트롤러. (Controller 상속 — 구조 결정 2.5 T18)
 *
 * 역할: 발사체의 이동 및 엔티티 충돌 감지만 담당한다.
 * 시각 표현(파티클 등)과 적중 시 로직은 외부 콜백(onHit, onExpire)에서 처리한다.
 *
 * 동작 방식:
 *   - start() 이후 매 틱마다 tick()을 실행
 *   - ProjectileData.speed 만큼 방향으로 이동
 *   - targetRender 반경 내 엔티티를 targetFilter 로 필터링해 충돌 판정
 *   - NORMAL: 첫 번째 충돌 시 onHit 호출 후 종료
 *   - PIERCE: 모든 충돌 엔티티에 onHit 호출하며 계속 진행 (엔티티당 1회)
 *   - GUIDED: 매 틱 유도 타겟 방향으로 방향 벡터를 보정 후 이동
 */
public class ProjectileController extends Controller {

    private static final double GUIDED_TURN_RATE = 0.15;

    private final ProjectileData data;
    private final Predicate<Entity> targetFilter;
    private final Consumer<LastTargetData> onHit;

    @Nullable
    private A_Entity guidedTarget;

    /** 매 틱 이동 후 현재 위치로 호출되는 시각효과 콜백 (파티클 궤적 등, 선택) */
    @Nullable
    private Consumer<Location> moveVisual;

    private Location currentPos;
    private Vector currentDirection;
    private double distanceTraveled = 0;
    private final Set<UUID> hitSet = new HashSet<>();

    /**
     * @param data         발사체 설정 데이터
     * @param targetFilter 충돌 대상 필터 (faction 조건 등)
     * @param onHit        충돌 시 호출 콜백 — LastTargetData 에 적중 엔티티와 위치가 담긴다
     * @param onExpire     최대 사거리 도달 시 호출 콜백 (외부 stop()에서는 호출되지 않는다)
     */
    public ProjectileController(ProjectileData data,
                                Predicate<Entity> targetFilter,
                                Consumer<LastTargetData> onHit,
                                Runnable onExpire) {
        super(onExpire);
        this.data = data;
        this.targetFilter = targetFilter;
        this.onHit = onHit;
        this.currentPos = data.startLoc().clone();
        this.currentDirection = data.direction().clone().normalize();
    }

    /**
     * GUIDED 타입 전용. 유도할 타겟을 설정한다.
     * start() 전후 모두 호출 가능하다.
     */
    public void setGuidedTarget(@Nullable A_Entity target) {
        this.guidedTarget = target;
    }

    /** 이동 궤적 시각효과 콜백 설정 (선택). start() 전후 모두 호출 가능하다. */
    public void setMoveVisual(@Nullable Consumer<Location> moveVisual) {
        this.moveVisual = moveVisual;
    }

    // ──────────────────────────────────────────────────
    // 내부 틱 처리
    // ──────────────────────────────────────────────────

    @Override
    protected void tick() {
        if (currentPos.getWorld() == null) {
            stop();
            return;
        }

        if (data.type() == ProjectileType.GUIDED) {
            updateGuidedDirection();
        }

        Vector moveVec = currentDirection.clone().multiply(data.speed());
        currentPos = currentPos.clone().add(moveVec);
        distanceTraveled += data.speed();

        if (moveVisual != null) moveVisual.accept(currentPos.clone());

        if (distanceTraveled >= data.range()) {
            expire();
            return;
        }

        processHits();
    }

    /**
     * 유도 타겟 방향으로 currentDirection 을 GUIDED_TURN_RATE 만큼 보정한다.
     * 타겟이 무효하면 guidedTarget 을 null 로 설정하고 직선 진행한다.
     */
    private void updateGuidedDirection() {
        if (guidedTarget == null) return;

        Entity targetBukkit = Bukkit.getEntity(guidedTarget.getUniqueId());
        if (targetBukkit == null || !targetBukkit.isValid()) {
            guidedTarget = null;
            return;
        }

        Vector toTarget = targetBukkit.getLocation().toVector()
                .subtract(currentPos.toVector());

        if (toTarget.lengthSquared() < 0.001) return;

        toTarget.normalize();
        currentDirection = currentDirection.clone()
                .multiply(1.0 - GUIDED_TURN_RATE)
                .add(toTarget.multiply(GUIDED_TURN_RATE))
                .normalize();
    }

    /**
     * 현재 위치 주변 targetRender 반경 내 엔티티를 조회하고 충돌 처리한다.
     * NORMAL: 첫 적중 후 발사체 종료.
     * PIERCE / GUIDED: 엔티티당 1회 적중, 계속 진행.
     */
    private void processHits() {
        double renderSq = data.targetRender() * data.targetRender();

        Collection<Entity> nearby = currentPos.getWorld().getNearbyEntities(
                currentPos,
                data.targetRender(),
                data.targetRender(),
                data.targetRender()
        );

        for (Entity entity : nearby) {
            if (entity.getLocation().distanceSquared(currentPos) > renderSq) continue;
            if (!targetFilter.test(entity)) continue;
            if (hitSet.contains(entity.getUniqueId())) continue;

            hitSet.add(entity.getUniqueId());
            A_Entity aEntity = CommediaDellarte.getAEntity(entity);
            onHit.accept(LastTargetData.of(aEntity, currentPos.clone()));

            if (data.type() == ProjectileType.NORMAL) {
                stop();
                return;
            }
        }
    }
}
