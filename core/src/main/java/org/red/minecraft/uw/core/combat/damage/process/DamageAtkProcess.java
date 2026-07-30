package org.red.minecraft.uw.core.combat.damage.process;

import org.bukkit.damage.DamageSource;
import org.bukkit.event.entity.EntityDamageEvent;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
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

    /**
     * 공격자가 없는 DamageSource 로도 생성될 수 있다.
     * (CombatManager.locDamage 의 공격자 없는 오버로드, 발사체 발사자가 엔티티가 아닌 경우 등)
     * 치명타는 공격자 스텟(CRITICAL_CHANCE)에서 나오므로 공격자가 없으면 판정 자체가 성립하지 않는다.
     * (가드가 없으면 randomCriCheck → getAttributeManager(null) 로 NPE 가 나고 데미지가 통째로 사라졌다)
     */
    public void run() {
        if (getOriginCTX().hasAttacker() && getOriginCTX().type().isCritical && !getOriginCTX().isCritical())
            getOriginCTX().setCritical(CombatManager.randomCriCheck(getOriginCTX().attacker()));
        super.run();
    }

    /**
     * UWAtkDamageEvent 의 damager 는 @NotNull 계약이다.
     * 공격자가 없으면 공격 이벤트를 만들 수 없으므로 상위(UWDamageEvent) 생성으로 되돌린다.
     */
    @Override
    protected EntityDamageEvent createEvent(DamageCTX ctx) {
        if (!ctx.hasAttacker()) {
            UndefinedWorldCorePlugin.sendLog("DamageAtkProcess: 공격자 없음 → UWDamageEvent 로 대체 " + ctx);
            return super.createEvent(ctx);
        }

        DamageSource source = ctx.source().getMinecraftDamageSource();
        return new UWAtkDamageEvent(ctx.attacker(), ctx.defender(), source, ctx.finalDamage(), ctx.isCritical());
    }
}
