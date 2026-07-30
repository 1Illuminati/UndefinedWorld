package org.red.minecraft.uw.item.mechanic;

import com.nexomc.nexo.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NonNull;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.item.U_ItemType;
import org.red.minecraft.uw.core.item.attribute.equipment.EquipmentType;
import org.red.minecraft.uw.core.item.attribute.equipment.WeaponItem;
import org.red.minecraft.uw.item.LoreBuilder;

import java.util.Map;

/**
 * 무기 아이템 Nexo 메커니즘. (구조 결정 T19)
 *
 * YAML 형식:
 *   type: WEAPON
 *   slot: SWORD | HEAVY_SWORD | DAGGER | ... (EquipmentType의 무기류)
 *   damage: 10           # 고유 공격값 — ALL_DAMAGE 대응, attributes와 별도 정의 (T19-6)
 *   attributes:
 *     PHYSICS_DAMAGE: 5
 */
public class WeaponItemMechanic extends U_ItemMechanic implements WeaponItem {

    private final EquipmentType equipmentType;
    private final double damage;
    private final Map<AttributeType, Double> attributes;

    public WeaponItemMechanic(@NonNull MechanicFactory factory, @NonNull ConfigurationSection section) {
        super(factory, section, U_ItemType.WEAPON, new LoreBuilder());

        this.damage = parseNumber(section, "damage", 0);
        this.attributes = parseAttributes(section);
        this.equipmentType = parseSlot(section);
    }

    private EquipmentType parseSlot(ConfigurationSection section) {
        EquipmentType type;
        try {
            type = EquipmentType.valueOf(section.getString("slot", ""));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid weapon slot specified: " + section.getString("slot"));
        }

        if (!type.isWeapon()) throw new RuntimeException("Not a weapon slot: " + type);
        return type;
    }

    @Override
    public double getDamage() {
        return this.damage;
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
