package org.red.minecraft.uw.item.mechanic;

import com.nexomc.nexo.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NonNull;
import org.red.minecraft.uw.core.item.U_ItemType;
import org.red.minecraft.uw.core.item.gear.GearItem;
import org.red.minecraft.uw.core.skill.SkillEngine;
import org.red.minecraft.uw.core.skill.condition.Condition;
import org.red.minecraft.uw.core.skill.cost.Cost;
import org.red.minecraft.uw.core.skill.cost.CostType;
import org.red.minecraft.uw.core.skill.effect.Effect;
import org.red.minecraft.uw.core.skill.factory.SkillFactory;
import org.red.minecraft.uw.core.skill.gear.Gear;
import org.red.minecraft.uw.item.LoreBuilder;

import java.util.ArrayList;
import java.util.List;

public class GearItemMechanic extends U_ItemMechanic implements GearItem, Gear {
    private final List<Cost> costs = new ArrayList<>();
    private final Effect effect;
    private final List<Condition> conditions = new ArrayList<>();
    private final int cool;
    private final int power;
    private final int castingTime;
    public GearItemMechanic(@NonNull MechanicFactory factory, @NonNull ConfigurationSection section) {
        super(factory, section, U_ItemType.GEAR, new LoreBuilder());
        this.cool = section.getInt("cool", 0);
        this.power = section.getInt("power", 0);
        this.castingTime = section.getInt("cast", 0);

        ConfigurationSection effectSection = section.getConfigurationSection("effect");
        if (effectSection == null || !SkillEngine.hasEffectFactory(effectSection.getString("id"))) throw new IllegalArgumentException("Effect not found");
        this.effect = SkillEngine.getEffectFactory(effectSection.getString("id")).create(effectSection);

        this.setConditions();
        this.setCost();
    }

    private void setConditions() {
        ConfigurationSection section = this.getSection().getConfigurationSection("conditions");
        if (section == null) return; // conditions 미정의 기어 허용

        for (String key : section.getKeys(false)) {
            SkillFactory<? extends Condition> factory = SkillEngine.getConditionFactory(key);

            if (factory == null) continue;
            this.conditions.add(factory.create(section.getConfigurationSection(key)));
        }
    }

    private void setCost() {
        ConfigurationSection section = this.getSection().getConfigurationSection("costs");
        if (section == null) return; // costs 미정의 기어 허용

        // 스키마: costs.<타입명>.value (conditions와 동일하게 키 = 타입명)
        // ex) costs: { mana: { value: 10 } }
        for (String key : section.getKeys(false)) {
            CostType type;
            try {
                type = CostType.valueOf(key.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid cost type: " + key);
            }

            SkillFactory<? extends Cost<?>> factory = SkillEngine.getCostFactory(type);

            if (factory == null) continue;
            this.costs.add(factory.create(section.getConfigurationSection(key)));
        }
    }

    @Override
    public Gear toGear() {
        return this;
    }

    @Override
    public List<Cost> getCosts() {
        return this.costs;
    }

    @Override
    public Effect getEffect() {
        return this.effect;
    }

    @Override
    public List<Condition> getConditions() {
        return this.conditions;
    }

    @Override
    public int getCool() {
        return this.cool;
    }

    @Override
    public int getPower() {
        return this.power;
    }

    @Override
    public int getCastingTime() {
        return this.castingTime;
    }

    @Override
    public String getID() {
        return this.getItemCode();
    }
}
