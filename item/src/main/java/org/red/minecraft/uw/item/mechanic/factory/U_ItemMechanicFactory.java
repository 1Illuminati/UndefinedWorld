package org.red.minecraft.uw.item.mechanic.factory;

import com.nexomc.nexo.mechanics.Mechanic;
import com.nexomc.nexo.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.red.minecraft.uw.core.item.U_ItemType;
import org.red.minecraft.uw.item.LoreBuilder;
import org.red.minecraft.uw.item.mechanic.AccessoryItemMechanic;
import org.red.minecraft.uw.item.mechanic.ArmorItemMechanic;
import org.red.minecraft.uw.item.mechanic.GearItemMechanic;
import org.red.minecraft.uw.item.mechanic.U_ItemMechanic;
import org.red.minecraft.uw.item.mechanic.WeaponItemMechanic;

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

        Mechanic mechanic = switch (type) {
            case RESOURCE, ORE -> new U_ItemMechanic(this, configurationSection, type, new LoreBuilder());
            case FOOD -> null;
            case POTION -> null;
            case ARMOR -> new ArmorItemMechanic(this, configurationSection);
            case WEAPON -> new WeaponItemMechanic(this, configurationSection);
            case SUB_WEAPON -> null; // todo 서브무기(왼손) 설계 확정 후 (T19-4)
            case ACCESSORY -> new AccessoryItemMechanic(this, configurationSection);
            case SCROLL -> null;
            case SPECIAL -> null;
            case GEAR -> new GearItemMechanic(this, configurationSection);
            case SKILL_ARTIFACT -> null;
            case TOOL -> null;
        };

        // 팩토리에 등록해야 getMechanic(id/ItemStack) 조회가 가능하다
        // (미등록 시 아이템 판정 전부 실패 — 기어 배치/장비 GUI/무기 스캔 불가)
        if (mechanic != null) this.addToImplemented(mechanic);
        return mechanic;
    }
}
