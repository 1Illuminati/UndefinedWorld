package org.red.minecraft.uw.mob.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import org.bukkit.entity.LivingEntity;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.combat.CombatManager;
import org.red.minecraft.uw.core.combat.damage.DamageType;

public class DamageMechanic implements ITargetedEntitySkill {
    private final double damage;
    private final DamageType type;

    public DamageMechanic(MythicLineConfig config) {
        this.damage = config.getDouble(new String[] {"damage", "d"}, 0);
        String typeStr = config.getString(new String[] {"type", "t"}, "PHYSICAL");

        DamageType resolvedType;
        try {
            resolvedType = DamageType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            UndefinedWorldCorePlugin.sendLog("데미지 타입 에러 발생");
            resolvedType = DamageType.PHYSICAL;
        }

        this.type = resolvedType;
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        A_LivingEntity defender = CommediaDellarte.getALivingEntity((LivingEntity) BukkitAdapter.adapt(target));
        A_Entity attacker = CommediaDellarte.getAEntity(BukkitAdapter.adapt(data.getCaster().getEntity()));

        CombatManager.damage(attacker, defender, this.type, this.damage, false);
        return SkillResult.SUCCESS;
    }
}
