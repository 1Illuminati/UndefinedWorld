package org.red.minecraft.uw.core.attribute.stat;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.attribute.AttributeViewer;

import java.util.HashMap;
import java.util.Map;

public record Stat(String name, Map<AttributeType, Double> map) implements AttributeViewer {
    private static final Map<String, Stat> defaultMap = new HashMap<>();

    public static Stat STR;
    public static Stat AGI;
    public static Stat HEL;
    public static Stat WIS;
    public static Stat FOC;
    public static Stat KNO;
    public static Stat SPI;
    public static Stat[] values;


    @Override
    public double getAttributeValue(AttributeType type) {
        return map.getOrDefault(type, 0.0);
    }

    @Override
    public boolean hasAttributeValue(AttributeType type) {
        return map.containsKey(type);
    }

    public static @Nullable Stat name(String name) {
        return defaultMap.getOrDefault(name, null);
    }

    public static void configSet(ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            ConfigurationSection attSection = section.getConfigurationSection(key);
            Map<AttributeType, Double> attMap = new HashMap<>();
            for (String attKey : attSection.getKeys(false)) {
                double value = attSection.getDouble(attKey);
                AttributeType type = AttributeType.valueOf(attKey);
                attMap.put(type, value);
            }

            defaultMap.put(key, new Stat(key, attMap));
        }

        STR = defaultMap.get("STR");
        AGI = defaultMap.get("AGI");
        HEL = defaultMap.get("HEL");
        WIS = defaultMap.get("WIS");
        FOC = defaultMap.get("FOC");
        KNO = defaultMap.get("KNO");
        SPI = defaultMap.get("SPI");
        values = new Stat[]{STR, AGI, HEL, WIS, FOC, KNO, SPI};
    }
}
