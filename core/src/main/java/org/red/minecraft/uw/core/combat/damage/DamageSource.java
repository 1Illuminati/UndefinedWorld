package org.red.minecraft.uw.core.combat.damage;

import org.bukkit.Location;
import org.bukkit.damage.DamageType;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;

public final class DamageSource {
    private final A_Entity attacker;
    private final A_LivingEntity defender;
    private final A_Entity realAttacker;
    private final Location damageLocation;
    public DamageSource(@Nullable A_Entity attacker, A_LivingEntity defender, @Nullable A_Entity realAttacker, @Nullable Location damageLocation) {
        this.attacker = attacker;
        this.defender = defender;
        this.realAttacker = realAttacker;
        this.damageLocation = damageLocation;
    }

    public DamageSource(@Nullable A_Entity attacker, A_LivingEntity defender, @Nullable A_Entity realAttacker) {
        this(attacker, defender, realAttacker, realAttacker != null ? realAttacker.getLocation() : null);
    }

    public DamageSource(@Nullable A_Entity attacker, A_LivingEntity defender, Location damageLocation) {
        this(attacker, defender, null, damageLocation);
    }

    public DamageSource(A_Entity attacker, A_LivingEntity defender) {
        this(attacker, defender, attacker.getLocation());
    }

    public DamageSource(A_LivingEntity defender) {
        this(null, defender, null, null);
    }

    public boolean hasAttacker() {
        return this.attacker != null;
    }

    @Nullable
    public Location getDamageLocation() {
        return this.damageLocation;
    }

    @Nullable
    public A_Entity getAttacker() {
        return this.attacker;
    }

    public A_LivingEntity getDefender() {
        return this.defender;
    }

    @Nullable
    public A_Entity getRealAttacker() {
        return this.realAttacker;
    }

    public org.bukkit.damage.DamageSource getMinecraftDamageSource() {
        org.bukkit.damage.DamageSource.Builder builder = org.bukkit.damage.DamageSource.builder(DamageType.GENERIC).withDamageLocation(this.damageLocation);

        if (this.realAttacker != null) builder.withDirectEntity(this.realAttacker.getEntity());
        if (hasAttacker()) builder.withCausingEntity(this.attacker.getEntity());

        return builder.build();
    }
}
