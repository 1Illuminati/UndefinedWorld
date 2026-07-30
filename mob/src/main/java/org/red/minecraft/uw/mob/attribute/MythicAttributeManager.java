package org.red.minecraft.uw.mob.attribute;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.skills.stats.StatType;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.attribute.AttributeType;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MythicMob Stat 호환 Attribute Holder
 * ActiveMob으로 활성화된 몹한테 쓸때 사용한다
 *
 * BUFF 컨테이너는 오버라이드하지 않고 상위 AttributeManager 에 위임한다.
 * (상위는 엔티티 UUID 키의 공유 메모리 저장소를 쓴다. 예전처럼 인스턴스 필드 Map 으로 들고 있으면
 *  IMobModule.getAttributeHolder 가 호출마다 새 인스턴스를 만들기 때문에
 *  버프가 더한 값이 다음 조회에서 사라져 미스틱몹에게 걸린 속성 버프/디버프가 전부 무효였다.)
 */
public class MythicAttributeManager extends AttributeManager {

    /** 이미 "미등록 Stat" 경고를 남긴 이름. 같은 Stat 을 반복해서 경고하지 않기 위한 것뿐이다. */
    private static final Set<String> WARNED_MISSING_STATS = ConcurrentHashMap.newKeySet();

    private final ActiveMob activeMob;
    public MythicAttributeManager(ActiveMob mob) {
        super(CommediaDellarte.getAEntity(mob.getEntity().getBukkitEntity()));
        this.activeMob = mob;
    }

    @Override
    public double getBaseAttributeValue(AttributeType aType, ContainerType cType) {
        return switch (cType) {
            case EQUIPMENT -> throw new IllegalArgumentException("mythicMob cant use EquipmentContainer");
            case BUFF -> super.getBaseAttributeValue(aType, cType);
            case STAT -> {
                // 미등록 경로 두 가지 모두 statFallback 을 탄다.
                //   1. MythicMobs 에 StatType 자체가 없음   2. StatType 은 있으나 이 몹에 값이 설정되지 않음
                // (= hasBaseAttributeValue 가 false 인 모든 경우)
                Optional<StatType> statType = findStatType(aType);
                if (statType.isEmpty()) yield statFallback(aType);
                if (this.activeMob.getStatRegistry().getStatData(statType.get()).isEmpty()) yield statFallback(aType);

                yield this.activeMob.getStatRegistry().get(statType.get());
            }
        };
    }

    @Override
    public boolean hasBaseAttributeValue(AttributeType aType, ContainerType cType) {
        return switch (cType) {
            case EQUIPMENT -> throw new IllegalArgumentException("mythicMob cant use EquipmentContainer");
            case BUFF -> super.hasBaseAttributeValue(aType, cType);
            case STAT -> {
                Optional<StatType> statType = findStatType(aType);
                yield statType.isPresent() && this.activeMob.getStatRegistry().getStatData(statType.get()).isPresent();
            }
        };
    }

    @Override
    public void setBaseAttributeValue(AttributeType aType, ContainerType cType, double value) {
        switch (cType) {
            case EQUIPMENT, STAT -> throw new IllegalArgumentException("mythicMob cant set Equipment, Stat Container");
            case BUFF -> super.setBaseAttributeValue(aType, cType, value);
        };
    }

    /**
     * 컨테이너 비우기. EQUIPMENT/STAT 은 다른 오버라이드와 동일하게 거부하고 BUFF 만 상위에 위임한다.
     */
    @Override
    public void clearBaseAttributeValues(ContainerType cType) {
        switch (cType) {
            case EQUIPMENT, STAT -> throw new IllegalArgumentException("mythicMob cant clear Equipment, Stat Container");
            case BUFF -> super.clearBaseAttributeValues(cType);
        }
    }

    @Override
    public double getAttributeValue(AttributeType aType) {
        double result = 0;

        for (ContainerType cType : ContainerType.values()) {
            if (cType == ContainerType.EQUIPMENT) continue;
            result += getBaseAttributeValue(aType, cType);
        }

        return result;
    }

    /**
     * MythicMobs 에 등록된 StatType 조회.
     *
     * 미등록 Stat 은 <b>0 으로 취급</b>한다(확정 사항). 예전에는 IllegalStateException 을 던졌는데,
     * getAttributeValue 는 조회만 해도 이 경로를 타므로 Stat 하나만 등록돼 있지 않아도
     * 데미지 파이프라인 전체가 예외로 중단되어 해당 몹이 무적이 됐다.
     *
     * 경고는 Stat 이름당 최초 1회만 남긴다. (AttributeType 150개 × 매 데미지마다 찍으면 로그 폭탄)
     *
     * 미등록 시의 실제 반환값은 statFallback 이 정한다 (HEALTH_MAX 만 예외).
     */
    private Optional<StatType> findStatType(AttributeType type) {
        Optional<StatType> statType = MythicBukkit.inst().getStatManager().getStat(type.name());

        if (statType.isEmpty() && WARNED_MISSING_STATS.add(type.name()))
            UndefinedWorldCorePlugin.sendLog("[MythicAttribute] MythicMobs 에 등록되지 않은 Stat → 폴백값 사용: " + type.name());

        return statType;
    }

    /**
     * STAT 컨테이너에 값이 없을 때 쓰는 폴백값.
     *
     * <p>기본은 <b>0</b> 이다. {@code HEALTH_MAX} 하나만 예외로
     * <b>해당 미스틱몹의 현재 체력</b>({@code A_LivingEntity.getHealth()})을 돌려준다. (사용자 확정)
     *
     * <p><b>버그로 오해하지 말 것</b> — 현재 체력이므로 몹이 피를 흘릴수록 이 값이 줄어든다.
     * 최대 체력이 아니라 현재 체력을 쓰는 것이 사용자가 고른 확정 동작이다.
     *
     * <p>적용 범위가 좁다는 점이 중요하다.
     * <ul>
     *   <li><b>미등록일 때만</b> 적용된다. MythicMobs 에 HEALTH_MAX Stat 이 등록돼 있고 그 몹에 값이 설정돼 있으면
     *       (= hasBaseAttributeValue 가 true) 폴백을 타지 않고 <b>등록값을 그대로</b> 쓴다.</li>
     *   <li>나머지 {@code HEALTH_*} 7종(TRUE_DAMAGE / MULTIPLY / DIVIDE / REGEN / REGEN_REDUCE / USE_LESS / USE_MORE)은
     *       체력 값이 아니라 배율·계수이므로 0 폴백을 유지한다. 여기에 체력을 넣으면 계산이 망가진다.</li>
     * </ul>
     *
     * <p>이로 인해 HEALTH_MAX 는 {@code hasBaseAttributeValue == false} 인데
     * {@code getBaseAttributeValue != 0} 인 유일한 타입이 된다. 폴백이 있으면 불가피한 불일치이며,
     * 현재 hasBaseAttributeValue 를 호출하는 코드는 없다.
     */
    private double statFallback(AttributeType aType) {
        if (aType != AttributeType.HEALTH_MAX) return 0;

        A_LivingEntity living = getEntity().getALivingEntity();
        if (living == null) {
            UndefinedWorldCorePlugin.sendLog("[MythicAttribute] HEALTH_MAX 폴백 실패(리빙 엔티티 아님) → 0: "
                    + getEntity().getUniqueIdStr());
            return 0;
        }

        return living.getHealth();
    }
}
