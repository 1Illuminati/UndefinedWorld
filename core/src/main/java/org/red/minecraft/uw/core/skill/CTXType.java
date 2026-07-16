package org.red.minecraft.uw.core.skill;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.combat.ElementalType;
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
    TARGET_COUNT(int.class, 1),
    SEARCH_AREA(BoundingBox[].class),
    SEARCH_TYPE(Target.SearchType.class),
    SEARCH_CENTER(Location.class),
    TARGET_FACTION(FactionType.class),

    LAST_TARGET_INFO(A_Entity[].class),

    ELEMENTAL(ElementalType.class, ElementalType.NONE),

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

    public final Class<?> clazz;
    public final Object defaultValue;
    <T> CTXType(Class<T> clazz) {
        this(clazz, null);
    }
    <T> CTXType(Class<T> clazz, T defaultValue) {
        this.clazz = clazz;
        this.defaultValue = defaultValue;
    }
}
