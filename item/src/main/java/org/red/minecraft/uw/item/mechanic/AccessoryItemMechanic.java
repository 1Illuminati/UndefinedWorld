package org.red.minecraft.uw.item.mechanic;

import com.nexomc.nexo.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NonNull;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.item.U_ItemType;
import org.red.minecraft.uw.core.item.attribute.equipment.AccessoryItem;
import org.red.minecraft.uw.core.item.attribute.equipment.EquipmentType;
import org.red.minecraft.uw.item.LoreBuilder;

import java.util.Map;

/**
 * 장신구 아이템 Nexo 메커니즘. (구조 결정 T19)
 * 갑옷/무기와 달리 고유 고정 스탯이 없다 — attributes만 정의한다 (T19-7).
 *
 * YAML 형식:
 *   type: ACCESSORY
 *   slot: NECKLACE | RING
 *   attributes:
 *     MANA_MAX: 20
 */
public class AccessoryItemMechanic extends U_ItemMechanic implements AccessoryItem {

    private final EquipmentType equipmentType;
    private final Map<AttributeType, Double> attributes;

    public AccessoryItemMechanic(@NonNull MechanicFactory factory, @NonNull ConfigurationSection section) {
        super(factory, section, U_ItemType.ACCESSORY, new LoreBuilder());

        this.attributes = parseAttributes(section);
        this.equipmentType = parseSlot(section);
    }

    private EquipmentType parseSlot(ConfigurationSection section) {
        EquipmentType type;
        try {
            type = EquipmentType.valueOf(section.getString("slot", ""));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid accessory slot specified: " + section.getString("slot"));
        }

        if (!type.isAccessory()) throw new RuntimeException("Not an accessory slot: " + type);
        return type;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return this.equipmentType;
    }

    @Override
    public double getAttributeValue(AttributeType type) {
        return this.attributes.getOrDefault(type, 0.0);
    }

    @Override
    public boolean hasAttributeValue(AttributeType type) {
        return this.attributes.containsKey(type);
    }
}
