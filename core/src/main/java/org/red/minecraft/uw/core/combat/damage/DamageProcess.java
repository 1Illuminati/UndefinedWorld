package org.red.minecraft.uw.core.combat.damage;

import org.bukkit.Bukkit;
import org.bukkit.util.Vector;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.combat.CombatManager;
import org.red.minecraft.uw.core.combat.damage.resolver.CriticalResolver;
import org.red.minecraft.uw.core.combat.damage.resolver.DamageResolver;
import org.red.minecraft.uw.core.event.UWDamageEvent;

public class DamageProcess {
    private final A_LivingEntity defEntity;
    private final DamageType type;
    private final double originDamage;
    private final boolean isCritical;

    public DamageProcess(A_LivingEntity defEntity, DamageType type, double originDamage, boolean isCritical) {
        this.defEntity = defEntity;
        this.type = type;
        this.originDamage = originDamage;
        this.isCritical = isCritical;
    }

    public A_LivingEntity getDefEntity() {
        return this.defEntity;
    }

    public DamageType getType() {
        return this.type;
    }

    public double getOriginDamage() {
        return this.originDamage;
    }

    public boolean isCritical() {
        return this.isCritical;
    }

    public void process() {
        double damage = resolveDef(this.originDamage);
        this.complete(new DamageInfo(damage, this.getType(), this.isCritical));
    }

    public double resolveDef(double damage) {
        DamageResolver resolver = CombatManager.getResolverByType(this.getDefEntity(), this.getType());

        if (isCritical() && resolver instanceof CriticalResolver critResolver)
            return critResolver.resolveDefCritDamage(damage);

        return resolver.resolveDefDamage(damage);
    }

    public void complete(DamageInfo info) {
        UWDamageEvent event = new UWDamageEvent(this.getDefEntity(), info);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        this.getDefEntity().setLastDamage(info.damage());
        this.getDefEntity().setLastDamageCause(event);
        CombatManager.applyHitEffect(this.getDefEntity().getLivingEntity(), new Vector(0, 0, 0));
        this.getDefEntity().setHealth(Math.max(this.getDefEntity().getHealth() - info.damage(), 0));
        UndefinedWorldCorePlugin.sendLog(event.getLoggerMessage());
    }
}
