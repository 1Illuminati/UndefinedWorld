package org.red.minecraft.uw.item.mechanic.factory;

import com.nexomc.nexo.mechanics.Mechanic;
import com.nexomc.nexo.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.red.minecraft.uw.core.item.U_ItemType;
import org.red.minecraft.uw.item.LoreBuilder;
import org.red.minecraft.uw.item.mechanic.GearItemMechanic;
import org.red.minecraft.uw.item.mechanic.U_ItemMechanic;

public class U_ItemMechanicFactory extends MechanicFactory {
    public U_ItemMechanicFactory() {
        super("u_item");
    }

    @Override @Nullable
    public U_ItemMechanic getMechanic(String itemId) {
        return (U_ItemMechanic) super.getMechanic(itemId);
    }

    @Override @Nullable
    public U_ItemMechanic getMechanic(ItemStack itemStack) {
        return (U_ItemMechanic) super.getMechanic(itemStack);
    }

    @Override
    public @Nullable Mechanic parse(@NonNull ConfigurationSection configurationSection) {
        U_ItemType type;
        try {
            type = U_ItemType.valueOf(configurationSection.getString("type", "RESOURCE"));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }

        return switch (type) {
            case RESOURCE, ORE -> new U_ItemMechanic(this, configurationSection, type, new LoreBuilder());
            case FOOD -> null;
            case POTION -> null;
            case ARMOR -> null;
            case WEAPON -> null;
            case SUB_WEAPON -> null;
            case ACCESSORY -> null;
            case SCROLL -> null;
            case SPECIAL -> null;
            case GEAR -> new GearItemMechanic(this, configurationSection);
            case SKILL_ARTIFACT -> null;
            case TOOL -> null;
        };
    }
}
