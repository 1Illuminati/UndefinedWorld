package org.red.minecraft.uw.core.skill.effect.modifier;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.effect.Effect;
import org.red.minecraft.uw.core.skill.effect.EffectResult;
import org.red.minecraft.uw.core.skill.effect.EffectType;

import java.util.concurrent.CompletableFuture;

/**
 * 타겟 탐색 중심점({@link CTXType#SEARCH_CENTER})을 지정하는 수정자 기어.
 *
 * <p><b>값이 아니라 "모드"를 받는다.</b> 기어 YAML 에 월드 절대좌표를 박아 넣는 방식은 쓰지 않는다
 * (사용자 확정) — 스킬은 어디서든 쓰이므로 고정 좌표는 의미가 없다.
 *
 * <p><b>모드는 이 기어가 실행되는 시점에 좌표로 해석된다.</b> 수정자도 노드 체인의 일부라
 * 앞선 타겟 기어가 이미 실행된 뒤에 돌아간다. 즉 {@code LAST_TARGET} 은 "직전 기어가 잡은 대상"을
 * 정확히 가리킨다. (생성 시점 = 기어 로드 시점에 좌표로 굳히면 이 의미가 사라진다)
 *
 * <p>소비처: {@code TargetEffect}, {@code ThunderEffect.searchNearby}.
 * 두 소비처 모두 {@code hasCTX(SEARCH_CENTER)} 로 판정해, 이 기어가 없으면 시전자 위치를 쓴다.
 * ({@code SEARCH_CENTER} 에는 기본값이 없어야 이 판정이 성립한다 — 기본값을 넣지 말 것)
 */
public class SearchCenterEffect implements Effect {

    /**
     * 시전자가 바라보는 지점을 찾을 <b>기본</b> 최대 거리(블록). 실제 거리는 여기에 {@link CTXType#RANGE} 배율이 곱해진다.
     * <p>20.0 은 {@code projectile} 기어의 기본 사거리와 같은 값이다 — "바라보는 지점"과 발사체가 닿는 거리가
     * 같은 감각이 되도록 맞췄다. 밸런스 확정 시 이 상수만 교체하면 된다.
     */
    private static final double LOOK_BASE_DISTANCE = 20.0;

    /** 탐색 중심점을 무엇으로 잡을지. 실제 좌표 해석은 {@link #resolveCenter} 가 실행 시점에 한다. */
    public enum CenterMode {
        /** 시전자 위치 */
        CASTER,
        /** 직전 기어가 잡은 대상 중 첫 유효 대상의 위치 */
        LAST_TARGET,
        /**
         * 시전자가 바라보는 지점.
         * 최대 거리({@link #LOOK_BASE_DISTANCE} × {@link CTXType#RANGE}) 안의 첫 블록 표면,
         * 허공을 보고 있으면 최대 거리 지점. (사용자 확정)
         */
        LOOK,
    }

    private final CenterMode mode;

    public SearchCenterEffect(CenterMode mode) {
        if (mode == null)
            throw new IllegalArgumentException("SearchCenterEffect Error: mode is null");

        this.mode = mode;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        A_Entity caster = ctx.getCTX(CTXType.CASTER);

        Location center = resolveCenter(ctx, caster);
        if (center == null) {
            // 다른 이펙트가 LAST_TARGET_INFO 부재를 FAIL 로 처리하는 것과 같은 기준.
            // 조용히 시전자 위치로 폴백하면 기어를 넣은 것과 안 넣은 것이 구분되지 않아
            // "왜 엉뚱한 곳을 탐색하는지" 추적할 수 없다.
            UndefinedWorldCorePlugin.sendLog("SearchCenterEffect: 중심점을 해석할 수 없어 실패 (mode=" + mode + ")");
            return CompletableFuture.completedFuture(EffectResult.FAIL);
        }

        // Location 은 가변 객체이고 SkillCTX.copy() 는 얕은 복사다.
        // 원본을 그대로 넣으면 엔티티 위치 객체를 병렬 노드가 공유하게 되므로 복사본을 저장한다.
        ctx.setCTX(CTXType.SEARCH_CENTER, center.clone());
        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    /**
     * 모드를 실제 좌표로 해석한다. 해석 불가면 null (호출부가 FAIL 처리).
     */
    @Nullable
    private Location resolveCenter(SkillCTX ctx, A_Entity caster) {
        return switch (mode) {
            case CASTER -> caster.getLocation();
            case LAST_TARGET -> {
                if (!ctx.hasCTX(CTXType.LAST_TARGET_INFO)) yield null;

                A_Entity[] targets = ctx.getCTX(CTXType.LAST_TARGET_INFO);
                if (targets == null) yield null;

                // 죽었거나 null 인 대상이 섞여 있으면 중심점이 무의미해진다
                // (ProjectileEffect.firstValidTarget 과 동일한 기준)
                for (A_Entity target : targets) {
                    if (target != null && !target.isDead()) yield target.getLocation();
                }
                yield null;
            }
            case LOOK -> resolveLookCenter(ctx, caster);
        };
    }

    /**
     * 시전자가 바라보는 지점을 구한다. (사용자 확정)
     * <ul>
     *   <li>최대 거리 = {@link #LOOK_BASE_DISTANCE} × {@link CTXType#RANGE}</li>
     *   <li>그 안의 첫 블록에 맞으면 <b>충돌 지점</b></li>
     *   <li>허공을 보고 있으면 <b>최대 거리 지점</b> (실패로 처리하지 않는다)</li>
     * </ul>
     * 통과 가능 블록(공기·물·풀·횃불 등)은 무시한다 — {@code ProjectileController.isBlockedByTerrain} 의
     * {@code isPassable} 기준과 같게 맞춘 것이다.
     *
     * <p>⚠️ <b>최대 거리가 커지면 그만큼 블록을 훑는다.</b> RANGE 배율 수정자를 여러 개 겹치면
     * 레이캐스트 비용이 그대로 커진다({@link SimpleModifierEffect} 의 지수 폭주 주석 참고).
     * 확정 규칙에 따라 상한은 두지 않았고, 아래 검사는 상한이 아니라 <b>무효값 방어</b>다.
     *
     * @return 중심 좌표. 시전자 월드가 없으면 null (호출부가 FAIL 처리)
     */
    @Nullable
    private Location resolveLookCenter(SkillCTX ctx, A_Entity caster) {
        Location origin = eyeLocation(caster);
        if (origin == null || origin.getWorld() == null) return null;

        double distance = LOOK_BASE_DISTANCE * ctx.getCTX(CTXType.RANGE, 1.0);

        // 무효값 방어 — 0 이하/비유한 거리는 rayTraceBlocks 가 예외를 던지거나 무한 탐색이 된다.
        // 상한이 아니라 안전장치이므로 제거하지 말 것. 무효면 배율을 무시하고 기본 거리를 쓴다.
        if (!Double.isFinite(distance) || distance <= 0) {
            UndefinedWorldCorePlugin.sendLog("SearchCenterEffect: LOOK 거리가 유효하지 않아 기본 거리 사용 (distance=" + distance + ")");
            distance = LOOK_BASE_DISTANCE;
        }

        Vector direction = origin.getDirection();

        // 방향 벡터가 0이면 정규화가 NaN 을 만들어 레이캐스트가 망가진다 (엔티티 상태 이상 시 발생 가능)
        if (direction.lengthSquared() < 1.0E-6) {
            UndefinedWorldCorePlugin.sendLog("SearchCenterEffect: LOOK 방향 벡터가 0이라 시전자 위치로 대체");
            return origin;
        }

        RayTraceResult hit = origin.getWorld().rayTraceBlocks(
                origin, direction, distance, FluidCollisionMode.NEVER, true);

        if (hit != null && hit.getHitPosition() != null)
            return hit.getHitPosition().toLocation(origin.getWorld());

        // 허공 조준 — 최대 거리 지점 (확정: FAIL 아님)
        return origin.clone().add(direction.clone().normalize().multiply(distance));
    }

    /**
     * 레이캐스트 시작점. 리빙 엔티티면 눈높이, 아니면 엔티티 위치.
     * (발 위치에서 쏘면 시선이 조금만 아래를 향해도 바로 바닥에 맞는다)
     */
    @Nullable
    private Location eyeLocation(A_Entity caster) {
        A_LivingEntity living = caster.getALivingEntity();
        return living != null ? living.getEyeLocation() : caster.getLocation();
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.MODIFIER};
    }
}
