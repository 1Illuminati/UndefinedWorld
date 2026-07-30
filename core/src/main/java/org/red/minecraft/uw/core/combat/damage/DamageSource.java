package org.red.minecraft.uw.core.combat.damage;

import org.bukkit.Location;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;

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

    /**
     * 나머지 생성자와 동일하게 공격자 없는 데미지를 허용한다.
     * (이 생성자만 attacker 를 무조건 역참조해 null 이면 원인 없는 NPE 로 데미지가 사라졌다)
     */
    public DamageSource(@Nullable A_Entity attacker, A_LivingEntity defender) {
        this(attacker, defender, attacker != null ? attacker.getLocation() : null);
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
        org.bukkit.damage.DamageSource.Builder builder = org.bukkit.damage.DamageSource.builder(DamageType.GENERIC);

        // withDamageLocation은 @NotNull 계약 — 공격자/좌표 없는 데미지(독/화상/COST 등)에서 null을 넘기면 안 된다
        if (this.damageLocation != null) builder.withDamageLocation(this.damageLocation);

        // withCausingEntity/withDirectEntity 도 @NotNull 계약이다.
        // A_Entity 래퍼는 살아 있어도 대상이 사라졌거나(오프라인 플레이어/언로드) getEntity()가 null일 수 있어
        // 실제 Bukkit 엔티티를 확인한 뒤에만 넘긴다. (null을 넘기면 여기서 NPE로 데미지가 통째로 사라진다)
        // 또한 build() 는 "causingEntity 가 있으면 directEntity 도 있어야 한다"를 요구한다.
        // realAttacker 의 Bukkit 엔티티가 사라진 경우까지 감안해 공격자로 되돌린다.
        // (반대로 directEntity 만 있는 것은 정상이다 — 발사기가 쏜 화살처럼 원인 엔티티가 없는 경우)
        Entity causingEntity = hasAttacker() ? this.attacker.getEntity() : null;
        Entity realEntity = this.realAttacker != null ? this.realAttacker.getEntity() : null;
        Entity directEntity = realEntity != null ? realEntity : causingEntity;

        if (causingEntity != null) builder.withCausingEntity(causingEntity);
        if (directEntity != null) builder.withDirectEntity(directEntity);

        UndefinedWorldCorePlugin.sendLog(String.format("CausingEntity:%b, DirectEntity:%b", causingEntity != null, directEntity != null));
        return builder.build();
    }
}
