package org.red.minecraft.uw.item.mechanic;

import com.nexomc.nexo.mechanics.Mechanic;
import com.nexomc.nexo.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NonNull;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.item.U_Item;
import org.red.minecraft.uw.core.item.U_ItemGrade;
import org.red.minecraft.uw.core.item.U_ItemType;
import org.red.minecraft.uw.item.LoreBuilder;

import java.util.HashMap;
import java.util.Map;

public class U_ItemMechanic extends Mechanic implements U_Item {
    private final U_ItemType type;
    private final U_ItemGrade grade;
    private final String description;
    private final LoreBuilder lore;
    public U_ItemMechanic(@NonNull MechanicFactory factory, @NonNull ConfigurationSection section, U_ItemType type, LoreBuilder loreBuilder) {
        super(factory, section, itemBuilder ->  itemBuilder.lore(loreBuilder.build()));
        this.type = type;
        this.description = section.getString("description", "");
        this.lore = loreBuilder;
        try {
            this.grade = U_ItemGrade.valueOf(section.getString("grade", "NORMAL"));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid grade specified: " + section.getString("grade"));
        }
    }

    @Override
    public String getItemCode() {
        return this.getItemID();
    }

    @Override
    public U_ItemType getType() {
        return this.type;
    }

    @Override
    public U_ItemGrade getGrade() {
        return this.grade;
    }

    /**
     * YAML의 attributes 섹션을 파싱한다. (구조 결정 T19-5)
     * 형식: attributes: { PHYSICS_DAMAGE_DEFENSE: 10, ... }
     * 잘못된 AttributeType 키는 로그 후 무시한다.
     */
    protected static Map<AttributeType, Double> parseAttributes(ConfigurationSection section) {
        Map<AttributeType, Double> map = new HashMap<>();

        ConfigurationSection attSection = section.getConfigurationSection("attributes");
        if (attSection == null) return map;

        for (String key : attSection.getKeys(false)) {
            AttributeType type = AttributeType.byName(key);
            if (type == null) {
                UndefinedWorldCorePlugin.sendLog("Invalid attribute key: " + key + " (" + section.getName() + ")");
                continue;
            }
            map.put(type, attSection.getDouble(key));
        }

        return map;
    }

    public void setLore() {
        this.lore.addLore(0, String.format("아이템 유형 : %s", getType().name()));
        this.lore.addLore(0, String.format("아이템 등급 : %s", getGrade().name()));

        if (!this.description.isEmpty())
            this.lore.addLore(999, description);
    }
}
