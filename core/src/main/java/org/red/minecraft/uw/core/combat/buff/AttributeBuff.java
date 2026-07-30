package org.red.minecraft.uw.core.combat.buff;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.attribute.AttributeType;

public class AttributeBuff implements Buff {

    /** 올릴 스탯 (AttributeType). BuffContext.data 필수 키 */
    public static final String TYPE_KEY = "type";

    /** 더할 수치 (Number, 유한값). BuffContext.data 필수 키 */
    public static final String VALUE_KEY = "value";

    /**
     * 버프를 건 <b>출처</b> 식별자 (String). BuffContext.data 필수 키.
     *
     * 같은 스탯을 올리는 버프라도 출처가 다르면 서로 덮어쓰지 않고 동시에 적용된다 (사용자 확정).
     * 예: "POTION_STR", "SKILL_버서크" 처럼 부여 주체를 구분할 수 있는 값을 넘긴다.
     */
    public static final String SOURCE_KEY = "source";

    /**
     * 이름에서 스탯과 출처를 가르는 구분자.
     *
     * '_' 를 쓰면 안 된다. AttributeType 에는 서로 접두사가 되는 이름이 많아서
     * (BLOCK / BLOCK_DIVIDE, CRITICAL_DAMAGE / CRITICAL_DAMAGE_MULTIPLY 등)
     * 예를 들어 (BLOCK, "DIVIDE_포션") 과 (BLOCK_DIVIDE, "포션") 이 같은 이름이 되어
     * 서로 다른 버프가 같은 키로 묶이고 한쪽이 조용히 밀려난다.
     * AttributeType 이름에 절대 들어가지 않는 문자를 써서 경계를 확정한다.
     */
    private static final String NAME_SEPARATOR = "@";

    private final BuffContext ctx;
    private final AttributeType type;
    private final double value;
    private final String name;

    /**
     * type/value/source 는 BuffContext.data 로 넘어오는 확장 슬롯이라 호출자가 빠뜨릴 수 있다.
     * (BuffEffect 처럼 BuffType 만 지정하는 경로에서는 항상 비어있다)
     * 그대로 두면 언박싱 NPE / ClassCastException 이 원인 없이 터지므로 적용 시점에 명시적으로 실패시킨다.
     *
     * todo AttributeType 은 ConfigurationSerializable 이 아니라서 로그아웃 스냅샷(BuffManager.Suspended)이
     *      저장소를 거쳐 문자열 등으로 바뀌면 이 검사에서 걸러진다(= 서버 재시작 후 복원 불가).
     *      서버 재시작을 넘어 복원이 필요하다면 data 직렬화 규약(예: type을 name() 문자열로 저장)을 확정해야 한다.
     */
    public AttributeBuff(BuffContext ctx) {
        this.ctx = ctx;

        Object rawType = ctx.get(TYPE_KEY);
        if (!(rawType instanceof AttributeType attributeType))
            throw new IllegalArgumentException("ATTRIBUTE_BUFF 컨텍스트에 AttributeType \"" + TYPE_KEY + "\" 이 필요합니다 (현재: " + rawType + ")");

        Object rawValue = ctx.get(VALUE_KEY);
        if (!(rawValue instanceof Number number))
            throw new IllegalArgumentException("ATTRIBUTE_BUFF 컨텍스트에 숫자 \"" + VALUE_KEY + "\" 가 필요합니다 (현재: " + rawValue + ")");

        double parsed = number.doubleValue();
        // NaN/Infinity 가 BUFF 컨테이너에 들어가면 onRemove 로도 원복되지 않아 스탯이 영구히 깨진다
        if (!Double.isFinite(parsed))
            throw new IllegalArgumentException("ATTRIBUTE_BUFF \"" + VALUE_KEY + "\" 가 유한한 값이어야 합니다 (현재: " + parsed + ")");

        this.type = attributeType;
        this.value = parsed;
        this.name = resolveName(ctx, attributeType);
    }

    /**
     * 이름 결정 = <b>스탯 + 출처</b>. (사용자 확정: 출처가 다르면 같은 스탯이어도 동시 적용)
     *
     * 활성 버프 키가 (BuffType, name) 이므로 이름이 곧 "같은 버프인가" 의 판정 기준이다.
     *   같은 스탯 + 같은 출처 → 같은 이름 → 재적용 시 덮어쓰기 (같은 포션을 또 마시면 갱신, 무한 강화 아님)
     *   같은 스탯 + 다른 출처 → 다른 이름 → 동시 적용
     *   다른 스탯 + 같은 출처 → 다른 이름 → 동시 적용
     * 출처만으로 이름을 만들면 한 출처가 여러 스탯을 올릴 때 서로 덮어쓰므로 스탯도 함께 넣는다.
     * 스탯과 출처 사이는 NAME_SEPARATOR 로 가른다 (구분자 선택 이유는 해당 상수 주석 참조).
     *
     * <b>출처는 기본값을 두지 않고 필수로 받는다.</b>
     * 스탯만으로 파생하면 출처 구분이 불가능해 확정 요구사항을 못 지키고,
     * 매번 고유값(UUID 등)을 만들면 같은 출처의 재적용까지 별개 버프가 되어 무한 중첩 강화가 된다.
     * 둘 다 조용히 틀리는 기본값이라 아예 두지 않고, type/value 와 동일하게 누락 시 명시적으로 실패시킨다.
     */
    private static String resolveName(BuffContext ctx, AttributeType attributeType) {
        Object rawSource = ctx.get(SOURCE_KEY);
        if (!(rawSource instanceof String source) || source.isBlank())
            throw new IllegalArgumentException("ATTRIBUTE_BUFF 컨텍스트에 출처 문자열 \"" + SOURCE_KEY
                    + "\" 가 필요합니다. 같은 스탯이어도 출처가 다르면 별개 버프로 유지되어야 하므로 생략할 수 없습니다 (현재: " + rawSource + ")");

        return BuffType.ATTRIBUTE_BUFF.name() + NAME_SEPARATOR + attributeType.name() + NAME_SEPARATOR + source;
    }

    @Override
    public BuffContext context() {
        return this.ctx;
    }

    @Override
    public BuffType type() {
        return BuffType.ATTRIBUTE_BUFF;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public AttributeType getAttributeType() {
        return this.type;
    }

    public double getValue() {
        return this.value;
    }

    @Override public void onApply(A_Entity entity)  {
        AttributeManager manager = UndefinedWorldCore.getAttributeManager(entity);
        manager.addBaseAttributeValue(this.getAttributeType(), AttributeManager.ContainerType.BUFF, this.value);
    }
    @Override public void onRemove(A_Entity entity, BuffRemoveReason reason) {
        AttributeManager manager = UndefinedWorldCore.getAttributeManager(entity);
        manager.addBaseAttributeValue(this.getAttributeType(), AttributeManager.ContainerType.BUFF, -this.value);
    }

    @Override
    public int tickCount() { return 100; }
    @Override
    public void tick(A_Entity entity) {}
}
