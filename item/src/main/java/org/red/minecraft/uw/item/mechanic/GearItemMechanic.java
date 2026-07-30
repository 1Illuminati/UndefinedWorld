package org.red.minecraft.uw.item.mechanic;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import com.nexomc.nexo.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
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
import java.util.Optional;

public class GearItemMechanic extends U_ItemMechanic implements GearItem, Gear {
    private final List<Cost> costs = new ArrayList<>();
    private final Effect effect;
    private final List<Condition> conditions = new ArrayList<>();
    private final int cool;
    private final int power;
    private final int castingTime;
    public GearItemMechanic(@NonNull MechanicFactory factory, @NonNull ConfigurationSection section) {
        super(factory, section, U_ItemType.GEAR, new LoreBuilder());
        // 숫자가 아닌 값이 들어오면 조용히 0(쿨 없음/파워 0)이 되어 밸런스가 깨지므로 parseInt로 로그를 남긴다
        this.cool = parseInt(section, "cool", 0);
        this.power = parseInt(section, "power", 0);
        this.castingTime = parseInt(section, "cast", 0);

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

            if (factory == null) {
                // 등록되지 않은 조건 키를 조용히 넘기면 조건 없는 기어가 되어 오타 추적이 불가능하다
                UndefinedWorldCorePlugin.sendLog("Condition factory not found, ignored: " + key
                        + " (" + this.getItemID() + ")");
                continue;
            }
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

            if (factory == null) {
                // 팩토리가 없는 타입(CostType.NONE 등)을 조용히 넘기면 "설정했는데 안 먹는" 상태가 되고
                // 오타 추적이 불가능하다. 잘못된 attribute 키와 동일하게 로그 후 무시한다.
                UndefinedWorldCorePlugin.sendLog("Cost factory not found, ignored: " + key
                        + " (" + this.getItemID() + ")");
                continue;
            }
            this.costs.add(factory.create(section.getConfigurationSection(key)));
        }
    }

    @Override
    public Gear toGear() {
        return this;
    }

    /**
     * 이 기어의 Nexo 아이템 1개를 만든다. (스킬 수정 GUI의 기어 회수용)
     * Nexo에서 아이템 정의가 사라졌으면 null — 호출부가 회수를 중단해야 기어가 조용히 사라지지 않는다.
     */
    @Override
    public @Nullable ItemStack createItemStack() {
        Optional<ItemBuilder> builder = NexoItems.optionalItemFromId(this.getItemID());
        if (builder.isEmpty()) {
            UndefinedWorldCorePlugin.sendLog("Nexo item not found for gear: " + this.getItemID());
            return null;
        }

        return builder.get().build();
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
