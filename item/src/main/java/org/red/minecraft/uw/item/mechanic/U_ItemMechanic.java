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
        // ItemModifier(로어 주입)를 등록하지 않는다. (§2.6 확정: 로어는 Nexo에서 구현 — 플러그인에서 만들지 않는다)
        // 등록하면 Nexo가 YAML lore를 채운 뒤(ItemParser.applyConfig) 메커니즘 modifier가 실행되어
        // setLore()가 호출되지 않는 현 상태에서는 빈 리스트로 YAML 로어를 지워버린다.
        super(factory, section);
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
     * 숫자 설정값을 읽는다.
     * getDouble/getInt는 값이 숫자가 아니면 조용히 0을 반환해 "설정했는데 안 먹는" 상태가 되므로
     * 키가 있는데 숫자가 아닌 경우를 로그로 남긴다. (attributes 파싱과 동일한 규칙)
     */
    protected static double parseNumber(ConfigurationSection section, String key, double def) {
        if (!section.contains(key)) return def;

        Object value = section.get(key);
        if (!(value instanceof Number number)) {
            UndefinedWorldCorePlugin.sendLog("Value is not a number: " + key + "=" + value + " (" + section.getName() + ")");
            return def;
        }

        return number.doubleValue();
    }

    /** parseNumber의 정수 버전 */
    protected static int parseInt(ConfigurationSection section, String key, int def) {
        return (int) parseNumber(section, key, def);
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

            // getDouble은 숫자가 아니면 조용히 0을 반환해 "설정했는데 안 먹는" 상태가 된다 → 값 타입을 검사한다
            if (!(attSection.get(key) instanceof Number)) {
                UndefinedWorldCorePlugin.sendLog("Attribute value is not a number: " + key + "=" + attSection.get(key) + " (" + section.getName() + ")");
                continue;
            }

            map.put(type, attSection.getDouble(key));
        }

        return map;
    }

    /**
     * <b>현재 어디서도 호출하지 않는다 — 의도된 상태다.</b>
     * (§2.6 확정: 장비 로어는 Nexo에서 구현한다. 플러그인은 로어를 만들지 않는다)
     * 호출하더라도 생성자에서 로어 ItemModifier를 등록하지 않으므로 아이템에 반영되지 않는다.
     * 플러그인 측 로어가 필요해지면 그때 표시 형식(순서/색상/포맷)을 확정하고 modifier를 다시 붙인다.
     */
    public void setLore() {
        this.lore.addLore(0, String.format("아이템 유형 : %s", getType().name()));
        this.lore.addLore(0, String.format("아이템 등급 : %s", getGrade().name()));

        if (!this.description.isEmpty())
            this.lore.addLore(999, description);
    }
}
