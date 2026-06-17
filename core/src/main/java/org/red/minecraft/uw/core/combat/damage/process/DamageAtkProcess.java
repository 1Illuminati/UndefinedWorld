package org.red.minecraft.uw.core.combat.damage.process;

import org.bukkit.damage.DamageSource;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.combat.CombatManager;
import org.red.minecraft.uw.core.combat.ElementalType;
import org.red.minecraft.uw.core.combat.damage.DamageCTX;
import org.red.minecraft.uw.core.combat.damage.DamageType;
import org.red.minecraft.uw.core.event.UWAtkDamageEvent;

public class DamageAtkProcess extends DamageProcess {
    public DamageAtkProcess(DamageCTX ctx) {
        super(ctx);
    }

    public DamageAtkProcess(A_Entity attacker, A_LivingEntity defender, DamageType damageType, ElementalType elementalType, double originDamage, double scale, boolean isCritical) {
        org.red.minecraft.uw.core.combat.damage.DamageSource source = new org.red.minecraft.uw.core.combat.damage.DamageSource(attacker, defender);
        this(new DamageCTX(source, damageType, elementalType, originDamage, scale, isCritical));
    }

    public DamageAtkProcess(org.red.minecraft.uw.core.combat.damage.DamageSource source, DamageType damageType, ElementalType elementalType, double originDamage, double scale, boolean isCritical) {
        this(new DamageCTX(source, damageType, elementalType, originDamage, scale, isCritical));
    }

    public void run() {
        if (getOriginCTX().type().isCritical && !getOriginCTX().isCritical())
            getOriginCTX().setCritical(CombatManager.randomCriCheck(getOriginCTX().attacker()));
        super.run();
    }

    @Override
    protected EntityDamageByEntityEvent createEvent(DamageCTX ctx) {
        DamageSource source = ctx.source().getMinecraftDamageSource();
        return new UWAtkDamageEvent(ctx.attacker(), ctx.defender(), source, ctx.finalDamage(), ctx.isCritical());
    }
}
