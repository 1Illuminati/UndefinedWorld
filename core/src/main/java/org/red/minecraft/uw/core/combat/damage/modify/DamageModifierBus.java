package org.red.minecraft.uw.core.combat.damage.modify;

import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.combat.ElementalType;
import org.red.minecraft.uw.core.combat.buff.BuffType;
import org.red.minecraft.uw.core.combat.damage.DamageCTX;
import org.red.minecraft.uw.core.combat.damage.DamageType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DamageModifierBus {
    private record Entry(int priority, DamageModifier mod) {}
    private final List<Entry> entries = new ArrayList<>();
    private final DamageCTX damageCTX;

    private DamageModifierBus(DamageCTX damageCTX) {
        this.damageCTX = damageCTX;
    }

    public DamageModifierBus add(int priority, DamageModifier mod) {
        entries.add(new Entry(priority, mod));
        return this;
    }
    public DamageModifierBus add(DamageModifier mod) { return add(1000, mod); }

    public DamageCTX flush() {
        entries.sort(Comparator.comparingInt(Entry::priority)); // 낮을수록 먼저
        for (Entry e : entries) e.mod().apply(damageCTX, this);

        return this.damageCTX.fixedData();
    }

    public static DamageModifierBus create(DamageCTX ctx) {
        DamageModifierBus damageModifierBus = new DamageModifierBus(ctx);
        DamageType type = ctx.type();

        // 치명타는 공격자 스텟(CRITICAL_DAMAGE)에서 나오므로 공격자가 없으면 성립하지 않는다.
        // (hasAttacker를 빼면 CriticalAtkModifier 없이 CriticalDefModifier만 등록되어 데미지가 일방적으로 깎였다)
        boolean isCri = type.isCritical && ctx.isCritical() && ctx.hasAttacker();

        // ── 공격 단계: 기본공격(0) → 속성공격(5) → 치명타(10) → 고정데미지(999)
        if (ctx.hasAttacker()) {
            switch (type) {
                case PHYSICAL -> damageModifierBus.add(0, new PhysicalAtkModifier());
                case MAGIC -> damageModifierBus.add(0, new MagicAtkModifier());
            }

            if (ctx.elementalType() != ElementalType.NONE) damageModifierBus.add(5, new ElementalAtkModifier());
            if (isCri) damageModifierBus.add(10, new CriticalAtkModifier());
            if (type.isTrueDamage) damageModifierBus.add(999, new TrueDamageModifier());
        }

        // ── 방어 단계: 기본방어(100) → 치명타방어(101) → 속성방어(105)
        switch (type) {
            case PHYSICAL -> damageModifierBus.add(100, new PhysicalDefModifier());
            case MAGIC -> damageModifierBus.add(100, new MagicDefModifier());
        }

        if (isCri) damageModifierBus.add(101, new CriticalDefModifier());
        if (ctx.elementalType() != ElementalType.NONE) damageModifierBus.add(105, new ElementalDefModifier());

        // 감전 상태의 방어자가 번개속성 데미지를 받으면 15% 추가 (방어 계산 이후)
        if (ctx.elementalType() == ElementalType.THUNDER
                && UndefinedWorldCore.getBuffManager().hasBuff(ctx.defender(), BuffType.SHOCK))
            damageModifierBus.add(200, new ShockedDefModifier());

        // 파쇄 중첩 상태의 방어자가 땅속성 데미지를 받으면 중첩당 추가 (방어 계산 이후)
        if (ctx.elementalType() == ElementalType.LAND
                && UndefinedWorldCore.getBuffManager().hasBuff(ctx.defender(), BuffType.SHATTER))
            damageModifierBus.add(200, new ShatterDefModifier());

        return damageModifierBus;
    }
}
