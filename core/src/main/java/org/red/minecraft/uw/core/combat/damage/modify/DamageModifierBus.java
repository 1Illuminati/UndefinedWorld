package org.red.minecraft.uw.core.combat.damage.modify;

import org.red.minecraft.uw.core.combat.damage.DamageCTX;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DamageModifierBus {
    private record Entry(int priority, DamageModifier mod) {}
    private final List<Entry> entries = new ArrayList<>();

    public DamageModifierBus add(int priority, DamageModifier mod) {
        entries.add(new Entry(priority, mod));
        return this;
    }
    public DamageModifierBus add(DamageModifier mod) { return add(1000, mod); }

    void flush(DamageCTX ctx) {
        entries.sort(Comparator.comparingInt(Entry::priority)); // 낮을수록 먼저
        for (Entry e : entries) e.mod().apply(ctx, this);
    }
}
