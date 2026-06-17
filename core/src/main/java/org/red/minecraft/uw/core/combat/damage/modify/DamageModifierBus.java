package org.red.minecraft.uw.core.combat.damage.modify;

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

        boolean isCri = type.isCritical && ctx.isCritical();

        if (ctx.hasAttacker()) {
            switch (type) {
                case PHYSICAL -> damageModifierBus.add(0, new PhysicalAtkModifier());
                case MAGIC -> damageModifierBus.add(0, new MagicAtkModifier());
            }

            if (isCri) damageModifierBus.add(1, new CriticalAtkModifier());
            if (type.isTrueDamage) damageModifierBus.add(999, new TrueDamageModifier());
        }

        switch (type) {
            case PHYSICAL -> damageModifierBus.add(100, new PhysicalDefModifier());
            case MAGIC -> damageModifierBus.add(100, new MagicDefModifier());
        }

        if (isCri) damageModifierBus.add(101, new CriticalDefModifier());

        return damageModifierBus;
    }
}
