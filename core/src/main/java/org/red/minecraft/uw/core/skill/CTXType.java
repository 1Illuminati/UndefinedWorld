package org.red.minecraft.uw.core.skill;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.combat.ElementalType;
import org.red.minecraft.uw.core.combat.damage.DamageType;
import org.red.minecraft.uw.core.skill.projectile.ProjectileType;
import org.red.minecraft.uw.core.skill.projectile.ProjectilesShape;
import org.red.minecraft.uw.core.skill.target.Target;
import org.red.minecraft.uw.core.skill.target.faction.FactionType;

public enum CTXType {
    /**
     * 발사체용 CTX
     */
    PROJECTILE_SHAPE(ProjectilesShape.class),
    PROJECTILE_TYPE(ProjectileType.class),

    /**
     * 타겟용 CTX
     */
    SEARCH_RANGE(double.class),
    /**
     * 타겟 수 <b>가산</b> 수정자. (§2.11 타입 규칙: int.class → 덧셈)
     *
     * <p>기본값이 <b>0</b> 인 이유는 덧셈의 항등원이 0 이기 때문이다.
     * 1 로 두면 수정자 기어가 없어도 항상 +1 이 되어 소비처마다 보정 코드가 필요해진다.
     * 0 이면 소비처가 {@code 기어 YAML 값 + CTX} 로 끝나고, 수정자가 없을 때 YAML 값이 그대로 살아난다.
     *
     * <p>⚠️ 소비처는 이 값을 <b>절대 개수로 쓰면 안 된다</b> — 기본값이 0 이라 대상이 0 이 된다.
     * 반드시 자기 기본값에 더해서 써야 한다.
     */
    TARGET_COUNT(int.class, 0),
    SEARCH_AREA(BoundingBox[].class),
    SEARCH_TYPE(Target.SearchType.class),
    SEARCH_CENTER(Location.class),
    TARGET_FACTION(FactionType.class),

    LAST_TARGET_INFO(A_Entity[].class),

    ELEMENTAL(ElementalType.class, ElementalType.NONE),

    /**
     * 이 스킬이 주는 데미지의 유형. (§2.6 확정)
     * 수정자 기어가 이 값을 세팅하면 <b>이후 기어들이 그 유형으로 데미지를 준다.</b>
     * <p>기본값을 두지 않는다 — 값이 없을 때 각 이펙트가 자기 기본값을 쓰는 것이 확정된 규약이라
     * ({@code damage} 기어의 {@code damageType} 등) 여기서 기본값을 채우면 그 폴백이 성립하지 않는다.
     * (TARGET_COUNT 와 같은 이유. SkillCTX 생성자가 기본값 있는 CTXType 을 전부 채운다)
     */
    DAMAGE_TYPE(DamageType.class),

    //스킬의 체인 횟수 수정자
    PIERCE(int.class, 1),
    //스킬의 반복횟수 수정자
    REPEAT(int.class, 1),
    //스킬의 갯수 수정자
    COUNT(int.class, 1),
    CHAIN(int.class, 1),
    LEVEL(int.class, 1),
    RENDER(double.class, 1.0),
    TIME(double.class, 1.0),
    //스킬 크기 수정자
    SIZE(double.class, 1.0),
    //스킬의 범위 수정자
    RANGE(double.class, 1.0),
    //발사체 등의 속도 수정자
    SPEED(double.class, 1.0),
    //스킬 데미지 수정자
    DAMAGE(double.class, 1.0),
    //스킬 시전자
    CASTER(A_Entity.class);

    /**
     * CTX 값의 타입. 원시 타입(double.class 등)으로 선언해도 <b>래퍼 클래스로 정규화</b>되어 저장된다.
     * <p>A_DataMap이 값 조회에 {@link Class#cast(Object)}를 사용하는데, 원시 Class 객체는
     * {@code isInstance}/{@code cast}가 항상 실패한다({@code double.class.cast(Double)} → ClassCastException).
     * 정규화 없이는 SIZE/RANGE/COUNT 등 모든 원시 타입 CTX 조회가 런타임에 터진다.
     */
    public final Class<?> clazz;
    public final Object defaultValue;
    <T> CTXType(Class<T> clazz) {
        this(clazz, null);
    }
    <T> CTXType(Class<T> clazz, T defaultValue) {
        this.clazz = toWrapper(clazz);
        this.defaultValue = defaultValue;
    }

    /** 원시 클래스를 대응 래퍼 클래스로 변환. 원시가 아니면 그대로 반환. */
    private static Class<?> toWrapper(Class<?> clazz) {
        if (!clazz.isPrimitive()) return clazz;

        if (clazz == boolean.class) return Boolean.class;
        if (clazz == byte.class)    return Byte.class;
        if (clazz == char.class)    return Character.class;
        if (clazz == double.class)  return Double.class;
        if (clazz == float.class)   return Float.class;
        if (clazz == int.class)     return Integer.class;
        if (clazz == long.class)    return Long.class;
        if (clazz == short.class)   return Short.class;
        return clazz; // void.class 등 — CTX 타입으로 쓰이지 않음
    }
}
