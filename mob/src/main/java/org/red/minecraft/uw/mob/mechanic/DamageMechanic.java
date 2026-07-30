package org.red.minecraft.uw.mob.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.combat.CombatManager;
import org.red.minecraft.uw.core.combat.damage.DamageType;

import java.util.Locale;

public class DamageMechanic implements ITargetedEntitySkill {
    private final double damage;
    private final DamageType type;

    public DamageMechanic(MythicLineConfig config) {
        this.damage = config.getDouble(new String[] {"damage", "d"}, 0);
        String typeStr = config.getString(new String[] {"type", "t"}, "PHYSICAL");

        DamageType resolvedType;
        try {
            // YAML 은 소문자로 적히는 경우가 많다. 로케일 의존(터키어 i 문제)을 피해 ROOT 로 고정한다.
            resolvedType = DamageType.valueOf(typeStr.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            // 어떤 값이 잘못됐는지 남기지 않으면 YAML 어느 줄이 문제인지 추적할 수 없다
            UndefinedWorldCorePlugin.sendLog("U_Damage 데미지 타입 해석 실패 → PHYSICAL 로 대체: '" + typeStr + "'");
            resolvedType = DamageType.PHYSICAL;
        }

        this.type = resolvedType;
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        // 리빙이 아닌 대상(아이템 액자, 보트 등)에도 메커니즘이 걸릴 수 있다.
        // 무조건 캐스팅하면 ClassCastException 으로 스킬 전체가 중단된다.
        if (!(BukkitAdapter.adapt(target) instanceof LivingEntity targetEntity)) {
            UndefinedWorldCorePlugin.sendLog("U_Damage 대상이 리빙 엔티티가 아님 → 무시: " + target.getUniqueId());
            return SkillResult.INVALID_TARGET;
        }

        if (data.getCaster() == null || data.getCaster().getEntity() == null) {
            UndefinedWorldCorePlugin.sendLog("U_Damage 시전자 엔티티 없음 → 무시");
            return SkillResult.CONDITION_FAILED;
        }

        Entity casterEntity = BukkitAdapter.adapt(data.getCaster().getEntity());
        if (casterEntity == null) {
            UndefinedWorldCorePlugin.sendLog("U_Damage 시전자 엔티티 변환 실패 → 무시");
            return SkillResult.CONDITION_FAILED;
        }

        A_LivingEntity defender = CommediaDellarte.getALivingEntity(targetEntity);
        A_Entity attacker = CommediaDellarte.getAEntity(casterEntity);

        CombatManager.damage(attacker, defender, this.type, this.damage, false);
        return SkillResult.SUCCESS;
    }
}
