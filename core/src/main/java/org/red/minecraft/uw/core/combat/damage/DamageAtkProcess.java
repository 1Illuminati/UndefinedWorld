package org.red.minecraft.uw.core.combat.damage;

import org.bukkit.Bukkit;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.combat.CombatManager;
import org.red.minecraft.uw.core.combat.damage.resolver.CriticalResolver;
import org.red.minecraft.uw.core.combat.damage.resolver.DamageResolver;
import org.red.minecraft.uw.core.event.UWAtkDamageEvent;

public class DamageAtkProcess extends DamageProcess {
    private final A_Entity atkEntity;

    public DamageAtkProcess(A_Entity atkEntity, A_LivingEntity defEntity, DamageType type, double originDamage, boolean isCritical) {
        super(defEntity, type, originDamage, isCritical);
        this.atkEntity = atkEntity;
    }

    public A_Entity getAtkEntity() {
        return this.atkEntity;
    }

    @Override
    public void process() {
        double damage = this.resolveDef(this.resolveAtk(this.getOriginDamage(), getScale()));
        this.complete(new DamageInfo(damage, this.getType(), this.isCritical()));
    }

    public double resolveAtk(double originDamage, double scale) {
        DamageResolver atkResolver = CombatManager.getResolverByType(getAtkEntity(), this.getType());

        if (isCritical() && atkResolver instanceof CriticalResolver critResolver) {
            return critResolver.resolveAtkCritDamage(originDamage, scale);
        }

        return atkResolver.resolveAtkDamage(originDamage, scale);
    }

    @Override
    public void complete(DamageInfo info) {
        UWAtkDamageEvent event = new UWAtkDamageEvent(this.getAtkEntity(), this.getDefEntity(), info);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        this.getDefEntity().setLastDamage(info.damage());
        this.getDefEntity().setLastDamageCause(event);
        CombatManager.applyHitEffect(this.getDefEntity().getLivingEntity(), this.getAtkEntity().getLocation().toVector());
        this.getDefEntity().setHealth(Math.max(this.getDefEntity().getHealth() - info.damage(), 0));
        UndefinedWorldCorePlugin.sendLog(event.getLoggerMessage());
    }
}
