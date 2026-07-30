package org.red.minecraft.uw.core.skill.summon;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeHolder;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.attribute.AttributeType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * 소환된 실체 하나. <b>스탯 계산만</b> 담당한다. (Process.md §2.10 T17 확정)
 *
 * <p>최종 Attribute = <b>소환수 자체(미스틱몹) Attribute + 시전자 Attribute × {@value #CASTER_ATTRIBUTE_RATIO}</b>
 *
 * <p><b>구현 범위</b> — 확정된 것은 위 계산 규칙과 클래스 배치뿐이다.
 * 소환 이펙트(EffectType.SUMMON 연계) / 수명 관리(Controller 상속 여부) / AI 는 <b>여전히 미확정이라 만들지 않는다.</b>
 * 엔티티를 스폰하거나 제거하는 책임도 이 클래스에 없다 — 이미 존재하는 엔티티를 받아 스탯만 계산한다.
 *
 * <p><b>아직 전투에 반영되지 않는다</b> — 데미지 파이프라인은 엔티티를 받아
 * {@code UndefinedWorldCore.getAttributeManager(entity)} 로 스탯을 읽으므로, 소환수의 실제 전투 스탯은
 * 여전히 미스틱몹 자체 Attribute 뿐이고 이 클래스가 더하는 시전자 기여분을 거치지 않는다.
 * 반영하려면 "이 엔티티가 소환수인지" 를 되짚는 경로(소환수 등록부 + 소멸 시 정리)가 필요한데,
 * 그건 <b>수명 관리 = 미확정 영역</b>이라 만들지 않았다. 수명 관리 확정 후 배선해야 한다.
 *
 * <p><b>책임 분리</b> — {@link SummonData} 는 소환 파라미터(시전자/지속틱/레벨) 전용이고,
 * AttributeHolder 는 여기(실체)가 구현한다. 소환수 자체 Attribute 를 읽으려면 소환된 엔티티가 필요한데
 * SummonData 는 그 참조를 갖지 않기 때문이다. (사용자 확정)
 */
public class SummonEntity implements AttributeHolder {

    /**
     * 시전자 Attribute 반영 비율. 사용자 원문: "소환수의 스텟은 미스틱몹 고유 스텟 + 플레이어 스텟의 10%".
     * (Process.md §2.10 "추가 개발 3. T17 소환수 스탯")
     */
    public static final double CASTER_ATTRIBUTE_RATIO = 0.1;

    /** 소환된 실체. 자체 Attribute(미스틱몹 Stat)를 읽고 버프를 쓰는 대상이다. */
    private final A_Entity entity;

    /** 소환 파라미터 (시전자/지속틱/레벨) */
    private final SummonData data;

    /**
     * 소환 시점에 계산해 둔 시전자 기여분 (= 시전자 Attribute × 비율). <b>스냅샷이다</b> (사용자 확정).
     *
     * <p>소환된 뒤에는 시전자가 버프를 받든 죽든 퇴장하든 이 값이 <b>변하지 않는다.</b>
     * 실시간 조회가 아닌 이유: 시전자가 사라지면 소환수 스탯이 도중에 급변하고,
     * 데미지 계산마다 시전자를 다시 조회하는 비용이 붙는다.
     *
     * <p>⚠️ <b>증폭 주의</b> — 소환수가 다시 소환하는 경로가 생기면 스냅샷이 스냅샷을 먹어
     * 세대가 늘수록 스탯이 누적된다(1세대 10% → 2세대는 그 10%를 포함한 값의 10%).
     * 현재는 소환 이펙트 자체가 없어 발생하지 않지만, 소환 이펙트를 만들 때 반드시 다시 검토해야 한다.
     *
     * <p>저장은 <b>메모리(인스턴스 필드)</b>다. 영속하지 않는 근거:
     * 소환 수명 관리가 미확정이라 "재시작 후에도 소환수가 남는가" 자체가 정해지지 않았고,
     * 영속시키려면 저장 키·정리 시점·소유자 소멸 처리까지 새 구조가 필요하다.
     * 버프 값(BUFF 컨테이너)도 같은 이유로 메모리 저장이 확정돼 있다(§2.6 버프 구조 개편 1).
     * 수명 관리가 확정되어 재시작 후 유지가 필요해지면 그때 다시 정한다.
     */
    private final Map<AttributeType, Double> casterBonus;

    /**
     * @param entity 소환된 실체 (이미 스폰되어 있는 엔티티). null 이면 즉시 실패시킨다 —
     *               나중에 스탯을 조회할 때 원인 없는 NPE 가 나는 것보다 소환 시점에 터지는 편이 추적이 쉽다.
     * @param data   소환 파라미터. {@code data.owner()} 가 스냅샷 대상 시전자다
     *               (owner 는 null 이어도 되고, 그러면 시전자 기여분 없이 자체 스탯만 쓴다).
     */
    public SummonEntity(A_Entity entity, SummonData data) {
        if (entity == null) throw new IllegalArgumentException("SummonEntity: 소환된 엔티티가 없다");
        if (data == null) throw new IllegalArgumentException("SummonEntity: 소환 파라미터(SummonData)가 없다");

        this.entity = entity;
        this.data = data;
        this.casterBonus = snapshotCasterBonus(data);
    }

    public A_Entity getEntity() { return entity; }
    public SummonData getData() { return data; }

    /**
     * 소환 시점 1회 실행. 시전자의 모든 Attribute 를 읽어 비율을 곱해 보관한다.
     *
     * <p>0 은 합산 결과를 바꾸지 않으므로 담지 않는다 (AttributeType 이 150개라 전부 담으면 소환마다 150엔트리).
     * 비유한값(NaN/Infinity)은 한 번 들어오면 이 소환수의 해당 스탯이 영구히 오염되므로 버리고 로그를 남긴다.
     */
    private static Map<AttributeType, Double> snapshotCasterBonus(SummonData data) {
        Map<AttributeType, Double> result = new EnumMap<>(AttributeType.class);

        A_Entity owner = data.owner();
        if (owner == null) {
            UndefinedWorldCorePlugin.sendLog("[Summon] 시전자가 없어 시전자 기여분 없이 소환 (자체 스탯만 사용)");
            return Collections.unmodifiableMap(result);
        }

        // mob 모듈 미등록 상태면 여기서 IllegalStateException 이 난다.
        // 조용히 0으로 만들면 "소환수가 왜 약한가"를 추적할 수 없으므로 그대로 전파한다
        // (UndefinedWorldCore.getAttributeManager 의 확정된 정책과 동일).
        AttributeManager manager = UndefinedWorldCore.getAttributeManager(owner);

        for (AttributeType type : AttributeType.values()) {
            double value = manager.getAttributeValue(type);
            if (value == 0) continue;

            double bonus = value * CASTER_ATTRIBUTE_RATIO;
            if (!Double.isFinite(bonus)) {
                UndefinedWorldCorePlugin.sendLog("[Summon] 시전자 스탯이 비정상값이라 제외: "
                        + type + " = " + value + " (시전자 " + owner.getUniqueIdStr() + ")");
                continue;
            }

            result.put(type, bonus);
        }

        UndefinedWorldCorePlugin.sendLog("[Summon] 시전자 기여분 스냅샷 " + result.size() + "종 (시전자 "
                + owner.getUniqueIdStr() + ", 비율 " + CASTER_ATTRIBUTE_RATIO + ")");
        return Collections.unmodifiableMap(result);
    }

    /**
     * 소환수 자체 Attribute + 소환 시점 시전자 기여분.
     *
     * <p>자체 Attribute 는 소환된 엔티티의 AttributeManager 가 돌려준다.
     * 미스틱몹이면 MythicAttributeManager 라 미스틱몹 Stat(+ 버프)이 그대로 들어오고,
     * 미스틱몹이 아니면 자체 스탯이 없다(§2.5 T17 "미스틱몹이 아닌 소환수는 스탯 없음").
     */
    @Override
    public double getAttributeValue(AttributeType type) {
        double own = manager().getAttributeValue(type);
        Double bonus = casterBonus.get(type);
        return (bonus == null) ? own : own + bonus;
    }

    /**
     * 소환수에게 값을 쓰면 <b>BUFF 컨테이너로 간다.</b> (사용자 확정)
     *
     * <p>다른 선택지가 성립하지 않는다 — 소환수 자체 스탯은 미스틱몹 쪽이라 읽기 전용이고
     * (MythicAttributeManager 는 STAT/EQUIPMENT 쓰기를 예외로 막는다),
     * 시전자 기여분은 소환 시점에 고정된 스냅샷이라 나중에 덮어쓸 대상이 아니다.
     * BUFF 로 보내야 소환수에게 버프/디버프(AttributeBuff)가 정상 적용된다.
     */
    @Override
    public void setAttributeValue(AttributeType type, double value) {
        manager().setBaseAttributeValue(type, AttributeManager.ContainerType.BUFF, value);
    }

    /**
     * 값이 설정돼 있는지. 시전자 기여분이 있거나, 소환수 자체 STAT/BUFF 에 값이 있으면 true.
     *
     * <p>EQUIPMENT 는 보지 않는다. 미스틱몹은 EQUIPMENT 컨테이너 조회 자체가 예외이고
     * ({@code MythicAttributeManager}), 합산({@code getAttributeValue})에서도 제외되므로
     * 여기서만 포함하면 두 메서드의 기준이 갈라진다.
     */
    @Override
    public boolean hasAttributeValue(AttributeType type) {
        if (casterBonus.containsKey(type)) return true;

        // manager() 는 호출마다 새 인스턴스를 만드므로 한 번만 받아 쓴다
        AttributeManager manager = manager();
        return manager.hasBaseAttributeValue(type, AttributeManager.ContainerType.STAT)
                || manager.hasBaseAttributeValue(type, AttributeManager.ContainerType.BUFF);
    }

    /**
     * 소환된 엔티티의 AttributeManager.
     * 매번 새로 조회한다 — mob 모듈이 호출마다 새 인스턴스를 만드는 구조라(IMobModule.getAttributeHolder)
     * 필드로 붙들고 있으면 몹 상태가 바뀌었을 때 옛 인스턴스를 보게 된다.
     */
    private AttributeManager manager() {
        return UndefinedWorldCore.getAttributeManager(entity);
    }
}
