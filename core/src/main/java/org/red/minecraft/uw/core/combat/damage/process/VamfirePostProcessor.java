package org.red.minecraft.uw.core.combat.damage.process;

import org.bukkit.event.entity.EntityDamageEvent;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.combat.damage.DamageCTX;

/**
 * 흡혈 후처리 — 데미지 확정 이후 공격자의 체력을 회복한다. (구조 결정 2.5-1: DamageProcess 내부 훅)
 *
 * 적용 조건: DamageType.isVamfire && 공격자 존재 && 공격자가 리빙 엔티티
 * 회복량 = 최종데미지 * (VAMFIRE / 100)
 *          * (1 + VAMFIRE_MULTIPLY / 100)        — 공격자 흡혈 증폭
 *          * (1 - 방어자 VAMFIRE_RESISTANCE / 100) — 방어자 흡혈 저항
 * todo 흡혈 공식(%해석/기본단위) 밸런스 확정 필요
 */
public final class VamfirePostProcessor {

    private VamfirePostProcessor() {}

    public static void process(DamageCTX ctx, EntityDamageEvent event) {
        if (!ctx.type().isVamfire || !ctx.hasAttacker()) return;

        A_LivingEntity attacker = ctx.attacker().getALivingEntity();
        if (attacker == null || attacker.isDead()) return;

        AttributeManager atkManager = UndefinedWorldCore.getAttributeManager(ctx.attacker());
        double vamfire = atkManager.getAttributeValue(AttributeType.VAMFIRE);
        if (vamfire <= 0) return;

        double multiply = atkManager.getAttributeValue(AttributeType.VAMFIRE_MULTIPLY);
        double resistance = UndefinedWorldCore.getAttributeManager(ctx.defender())
                .getAttributeValue(AttributeType.VAMFIRE_RESISTANCE);

        double heal = event.getDamage()
                * (vamfire / 100)
                * (1 + multiply / 100)
                * (1 - resistance / 100);

        if (heal <= 0) return;

        attacker.setHealth(Math.min(attacker.getMaxHealth(), attacker.getHealth() + heal));
        UndefinedWorldCorePlugin.sendLog(String.format("Vamfire heal:%f (%s)", heal, ctx));
    }
}
