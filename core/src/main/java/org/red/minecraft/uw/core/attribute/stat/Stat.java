package org.red.minecraft.uw.core.attribute.stat;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.attribute.AttributeViewer;

import java.util.*;

public record Stat(String name, Map<AttributeType, Double> map) implements AttributeViewer {
    private static final Map<String, Stat> defaultMap = new HashMap<>();

    public static Stat STR;
    public static Stat AGI;
    public static Stat HEL;
    public static Stat WIS;
    public static Stat FOC;
    public static Stat KNO;
    public static Stat SPI;

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

    public static Collection<Stat> stats() {
        return defaultMap.values();
    }

    public static Set<String> statKeys() {
        return defaultMap.keySet();
    }

    public static void configSet(ConfigurationSection section) {
        UndefinedWorldCorePlugin.sendLog("Stat set Start");
        for (String key : section.getKeys(false)) {
            // 하위 attribute 섹션이 없으면(빈 스탯 정의) 원인을 알 수 있게 메시지를 붙여 즉시 실패시킨다
            ConfigurationSection attSection = Objects.requireNonNull(
                    section.getConfigurationSection(key),
                    "StatSetting." + key + " 하위에 attribute 설정이 없습니다"
            );

            Map<AttributeType, Double> attMap = new HashMap<>();
            for (String attKey : attSection.getKeys(false)) {
                double value = attSection.getDouble(attKey);
                AttributeType type = AttributeType.valueOf(attKey);
                attMap.put(type, value);
            }

            UndefinedWorldCorePlugin.sendLog(key + " set Complete");
            defaultMap.put(key, new Stat(key, attMap));
        }

        STR = defaultMap.get("STR");
        AGI = defaultMap.get("AGI");
        HEL = defaultMap.get("HEL");
        WIS = defaultMap.get("WIS");
        FOC = defaultMap.get("FOC");
        KNO = defaultMap.get("KNO");
        SPI = defaultMap.get("SPI");

        // 기본 스탯 키가 빠지면 위 static 참조가 null로 남아 나중에 엉뚱한 위치에서 NPE가 난다 → 설정 시점에 알린다
        for (String required : new String[]{"STR", "AGI", "HEL", "WIS", "FOC", "KNO", "SPI"}) {
            if (!defaultMap.containsKey(required))
                UndefinedWorldCorePlugin.sendLog("StatSetting에 " + required + " 정의가 없습니다 (Stat." + required + " = null)");
        }
    }
}
