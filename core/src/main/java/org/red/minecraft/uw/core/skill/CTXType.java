package org.red.minecraft.uw.core.skill;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.red.minecraft.dellarte.library.entity.A_Entity;
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
    TARGET_COUNT(int.class),
    SEARCH_AREA(BoundingBox[].class),
    SEARCH_TYPE(Target.SearchType.class),
    SEARCH_CENTER(Location.class),
    TARGET_FACTION(FactionType.class),

    ELEMENTAL(),

    //스킬의 체인 횟수 수정자
    PIERCE(int.class),
    //스킬의 반복횟수 수정자
    REPEAT(int.class),
    //스킬의 갯수 수정자
    COUNT(int.class),
    //스킬 크기 수정자
    SIZE(double.class),
    //스킬의 범위 수정자
    RANGE(double.class),
    //발사체 등의 속도 수정자
    SPEED(double.class),
    //스킬 데미지 수정자
    DAMAGE(double.class),
    //스킬 시전자
    CASTER(A_Entity.class);

    public final Class<?> clazz;
    CTXType(Class<?> clazz) {
        this.clazz = clazz;
    }
}
