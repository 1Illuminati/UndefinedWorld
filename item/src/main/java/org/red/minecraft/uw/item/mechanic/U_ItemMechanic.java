package org.red.minecraft.uw.item.mechanic;

import com.nexomc.nexo.mechanics.Mechanic;
import com.nexomc.nexo.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NonNull;
import org.red.minecraft.uw.core.item.U_Item;
import org.red.minecraft.uw.core.item.U_ItemGrade;
import org.red.minecraft.uw.core.item.U_ItemType;
import org.red.minecraft.uw.item.LoreBuilder;

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

    public void setLore() {
        this.lore.addLore(0, String.format("아이템 유형 : %s", getType().name()));
        this.lore.addLore(0, String.format("아이템 등급 : %s", getGrade().name()));

        if (!this.description.isEmpty())
            this.lore.addLore(999, description);
    }
}
