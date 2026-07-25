package org.red.minecraft.uw.core.combat.damage.modify;

import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.combat.damage.DamageCTX;

/**
 * 감전 상태의 방어자가 번개속성 데미지를 받을 때 15% 추가 데미지.
 * (ElementalType.THUNDER 명세: "감전 디버프가 부여된 상태에서 데미지를 입을 경우 번개속성의 데미지를 15% 추가로 받으며")
 * 등록 조건은 DamageModifierBus.create 참조 (방어 계산 이후 적용, priority 200)
 */
public class ShockedDefModifier implements DamageModifier {

    /** 감전 시 추가 데미지 비율 (명세 고정값 15%) */
    public static final double SHOCK_EXTRA_RATE = 0.15;

    @Override
    public void apply(DamageCTX ctx, DamageModifierBus bus) {
        ctx.multiply(1 + SHOCK_EXTRA_RATE);
        UndefinedWorldCorePlugin.sendLog("ShockedDef +15% " + ctx);
    }
}
