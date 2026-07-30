package org.red.minecraft.uw.core.skill.projectile;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
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
 *
 * 종료 경로는 전부 expire()로 모인다 (사거리 도달 / NORMAL 적중 / 진행 불가).
 * 즉 onExpire는 발사체 1기당 정확히 1회 호출되므로 외부에서 완료 집계에 사용할 수 있다.
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
     * @param onExpire     발사체 수명 종료 시 1회 호출 콜백
     *                     (사거리 도달 / NORMAL 적중 종료 / 진행 불가. 외부 stop()에서는 호출되지 않는다)
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
        // 진행 불가 상태는 stop()이 아니라 expire()로 정리한다.
        // stop()은 onExpire를 부르지 않으므로, 완료 집계를 onExpire에 의존하는 이펙트의
        // CompletableFuture가 영구 미완료로 남아 스킬 체인이 멈춘다.
        if (currentPos.getWorld() == null) {
            UndefinedWorldCorePlugin.sendLog("Projectile 월드 소실로 종료: shooter=" + data.shooter().getUniqueIdStr());
            expire();
            return;
        }

        // speed/range가 0 이하이거나 유한하지 않으면 distanceTraveled가 사거리에 도달할 수 없어
        // 태스크가 영원히 돌게 된다 (SPEED/RANGE 수정자가 0을 만든 경우 포함).
        if (!isTravelable()) {
            UndefinedWorldCorePlugin.sendLog("Projectile 이동 불가 파라미터로 즉시 종료: speed=" + data.speed()
                    + " range=" + data.range() + " shooter=" + data.shooter().getUniqueIdStr());
            expire();
            return;
        }

        if (data.type() == ProjectileType.GUIDED) {
            updateGuidedDirection();
        }

        Vector moveVec = currentDirection.clone().multiply(data.speed());
        currentPos = currentPos.clone().add(moveVec);
        distanceTraveled += data.speed();

        if (moveVisual != null) moveVisual.accept(currentPos.clone());

        // 적중 판정을 사거리 판정보다 먼저 한다.
        // 반대 순서면 마지막 이동 구간(speed 만큼)의 적이 항상 무시되고,
        // speed >= range 인 발사체(SPEED 수정자가 큰 경우 포함)는 단 한 번도 적중 판정을 못 한다.
        processHits();

        // NORMAL 적중으로 processHits 안에서 이미 수명 종료된 경우 (expire()는 멱등이지만 흐름을 명시적으로 끊는다)
        if (!isRunning()) return;

        // 블록 충돌 (확정: PIERCE는 통과, NORMAL/GUIDED는 소멸)
        // 적중 판정 뒤에 둔다 — 벽에 붙어 있는 적을 그 틱에 맞히고 나서 소멸하는 편이 자연스럽다.
        if (isBlockedByTerrain()) {
            expire();
            return;
        }

        // 실효 사거리는 speed 단위로 올림된다(range=10, speed=3 이면 12에서 종료).
        // todo 정확한 사거리 컷이 필요하면 마지막 이동량을 range 잔여분으로 잘라야 한다 — 밸런스 확정 사항
        if (distanceTraveled >= data.range()) expire();
    }

    /**
     * 이동 가능한 파라미터인지 검사한다.
     * speed 가 0 이하이면 위치가 전진하지 않아 distanceTraveled 가 range 에 도달하지 못하고,
     * range 가 0 이하이거나 두 값이 유한하지 않으면 종료 조건 자체가 성립하지 않는다.
     * 두 경우 모두 태스크가 영구히 돌기 때문에 즉시 수명 종료로 처리해야 한다.
     */
    private boolean isTravelable() {
        return Double.isFinite(data.speed()) && data.speed() > 0
                && Double.isFinite(data.range()) && data.range() > 0;
    }

    /**
     * 지형(블록)에 막혔는지 검사한다. (확정 규칙)
     * <ul>
     *   <li>PIERCE — 블록을 통과한다. 항상 false.</li>
     *   <li>NORMAL / GUIDED — 통과 불가 블록에 닿으면 소멸한다.</li>
     * </ul>
     * 통과 가능 판정은 {@code Block.isPassable()} 을 쓴다. 충돌 상자가 없는 블록
     * (공기·물·풀·횃불 등)이 전부 통과로 잡히므로 "공기·물·풀까지 막지 않는다"는 요구와 일치한다.
     *
     * <p>청크가 로드되지 않은 위치에서 {@code getBlock()} 을 부르면 <b>동기 청크 로딩</b>이 일어난다.
     * 발사체가 미로드 지형으로 날아갈 때마다 서버가 멈추므로, 미로드면 지형에 막힌 것으로 처리해 종료한다.
     */
    private boolean isBlockedByTerrain() {
        if (data.type() == ProjectileType.PIERCE) return false;

        int blockX = currentPos.getBlockX();
        int blockZ = currentPos.getBlockZ();
        if (!currentPos.getWorld().isChunkLoaded(blockX >> 4, blockZ >> 4)) {
            UndefinedWorldCorePlugin.sendLog("Projectile 미로드 청크 진입으로 종료: shooter=" + data.shooter().getUniqueIdStr());
            return true;
        }

        return !currentPos.getBlock().isPassable();
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
        // SIZE 수정자가 0/음수/비유한 값을 만들 수 있다. 그대로 getNearbyEntities 에 넘기면
        // 의미 없는(또는 역전된) 탐색 박스가 되므로 충돌 판정만 건너뛴다. 발사체는 사거리까지 그대로 진행한다.
        if (!Double.isFinite(data.targetRender()) || data.targetRender() <= 0) return;

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

            // 클래스 계약대로 종료를 expire()로 모은다. stop()으로 끝내면 onExpire가 불리지 않아
            // 완료 집계를 onExpire에 의존하는 이펙트의 CompletableFuture가 영구 미완료로 남는다.
            if (data.type() == ProjectileType.NORMAL) {
                expire();
                return;
            }
        }
    }
}
