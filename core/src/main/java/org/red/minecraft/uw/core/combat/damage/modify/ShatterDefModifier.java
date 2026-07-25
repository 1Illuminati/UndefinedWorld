package org.red.minecraft.uw.core.combat.damage.modify;

import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.combat.buff.BuffData;
import org.red.minecraft.uw.core.combat.buff.BuffType;
import org.red.minecraft.uw.core.combat.damage.DamageCTX;

/**
 * 파쇄 중첩 상태의 방어자가 땅속성 데미지를 받을 때 중첩당 추가 데미지.
 * (ElementalType.LAND 명세: "파쇄 디버프가 부여될 수록 해당 객체는 땅속성 데미지를 더 높게 받게 된다")
 * 등록 조건은 DamageModifierBus.create 참조 (방어 계산 이후 적용, priority 200)
 */
public class ShatterDefModifier implements DamageModifier {

    /** 중첩당 추가 데미지 비율 — todo 밸런스 확정 필요 (임시 5%/중첩) */
    public static final double RATE_PER_STACK = 0.05;

    @Override
    public void apply(DamageCTX ctx, DamageModifierBus bus) {
        BuffData data = UndefinedWorldCore.getBuffManager().getBuff(ctx.defender(), BuffType.SHATTER);
        if (data == null) return;

        int stacks = data.getBuff().context().level();
        ctx.multiply(1 + (stacks * RATE_PER_STACK));
        UndefinedWorldCorePlugin.sendLog("ShatterDef x" + stacks + " " + ctx);
    }
}
