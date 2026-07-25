package org.red.minecraft.uw.item.mechanic;

import com.nexomc.nexo.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NonNull;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.item.U_ItemType;
import org.red.minecraft.uw.core.item.attribute.equipment.ArmorItem;
import org.red.minecraft.uw.core.item.attribute.equipment.EquipmentType;
import org.red.minecraft.uw.item.LoreBuilder;

import java.util.Map;

/**
 * 갑옷 아이템 Nexo 메커니즘. (구조 결정 T19)
 *
 * YAML 형식:
 *   type: ARMOR
 *   slot: HELMET | CHESTPLATE | LEGGINGS | BOOTS
 *   defense: 10          # 고유 방어값 — ALL_DEFENSE 대응, attributes와 별도 정의 (T19-6)
 *   attributes:
 *     PHYSICS_DAMAGE_DEFENSE: 5
 */
public class ArmorItemMechanic extends U_ItemMechanic implements ArmorItem {

    private final EquipmentType equipmentType;
    private final double defense;
    private final Map<AttributeType, Double> attributes;

    public ArmorItemMechanic(@NonNull MechanicFactory factory, @NonNull ConfigurationSection section) {
        super(factory, section, U_ItemType.ARMOR, new LoreBuilder());

        this.defense = section.getDouble("defense", 0);
        this.attributes = parseAttributes(section);
        this.equipmentType = parseSlot(section);
    }

    private EquipmentType parseSlot(ConfigurationSection section) {
        EquipmentType type;
        try {
            type = EquipmentType.valueOf(section.getString("slot", ""));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid armor slot specified: " + section.getString("slot"));
        }

        if (!type.isArmor()) throw new RuntimeException("Not an armor slot: " + type);
        return type;
    }

    @Override
    public double getDefense() {
        return this.defense;
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
