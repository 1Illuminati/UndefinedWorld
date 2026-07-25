package org.red.minecraft.uw.core.player.equipment;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.dellarte.library.util.A_DataMap;
import org.red.minecraft.uw.core.StaticValue;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.item.U_Item;
import org.red.minecraft.uw.core.item.attribute.AttributeItem;
import org.red.minecraft.uw.core.item.attribute.equipment.ArmorItem;
import org.red.minecraft.uw.core.item.attribute.equipment.EquipmentItem;
import org.red.minecraft.uw.core.item.attribute.equipment.WeaponItem;
import org.red.minecraft.uw.core.player.PlayerHelper;

/**
 * 플레이어 장비 저장/조회 및 EQUIPMENT 컨테이너 재계산. (구조 결정 T19)
 *
 * 저장: 플레이어 A_DataMap의 equipment_data 하위에 슬롯명 -> ItemStack (T19-3)
 * 적용: GUI 장비(EquipSlot 전체) + 주손 무기를 합산해 EQUIPMENT 컨테이너를 재계산
 *   - AttributeItem의 attributes 합산
 *   - ArmorItem.getDefense() -> ALL_DEFENSE (T19-6)
 *   - WeaponItem.getDamage() -> ALL_DAMAGE (T19-6)
 */
public final class EquipmentManager {

    private EquipmentManager() {}

    @Nullable
    public static ItemStack getEquipment(A_Player player, EquipSlot slot) {
        return dataMap(player).getItemStack(slot.name());
    }

    public static void setEquipment(A_Player player, EquipSlot slot, @Nullable ItemStack stack) {
        A_DataMap map = dataMap(player);
        if (stack == null || stack.isEmpty()) map.remove(slot.name());
        else map.put(slot.name(), stack);
    }

    /** 해당 슬롯에 장착 가능한 아이템인지 판정 */
    public static boolean canEquip(EquipSlot slot, ItemStack stack) {
        U_Item item = UndefinedWorldCore.getItem(stack);
        return item instanceof EquipmentItem equipment && equipment.getEquipmentType() == slot.acceptType;
    }

    /**
     * GUI 장비 + 주손 무기를 EQUIPMENT 컨테이너에 재계산 적용.
     * todo 왼손 서브무기 확장 시 무기 수집부를 슬롯 목록 순회로 일반화
     */
    public static void applyEquipmentAttributes(A_Player player) {
        PlayerHelper helper = new PlayerHelper(player);

        // 초기화
        for (AttributeType type : AttributeType.values()) {
            helper.setBaseAttributeValue(type, AttributeManager.ContainerType.EQUIPMENT, 0);
        }

        // GUI 장비 합산
        for (EquipSlot slot : EquipSlot.values()) {
            ItemStack stack = getEquipment(player, slot);
            if (stack == null || stack.isEmpty()) continue;

            applyItem(helper, UndefinedWorldCore.getItem(stack));
        }

        // 주손 무기 (T19-4: 주손만 감지)
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (!mainHand.isEmpty()) {
            U_Item item = UndefinedWorldCore.getItem(mainHand);
            if (item instanceof WeaponItem) applyItem(helper, item);
        }

        UndefinedWorldCorePlugin.sendLog("Equipment applied: " + player.getName());
    }

    /** 아이템 1개의 attribute + 고유 스탯(방어/공격값)을 EQUIPMENT 컨테이너에 합산 */
    private static void applyItem(PlayerHelper helper, @Nullable U_Item item) {
        if (!(item instanceof AttributeItem attributeItem)) return;

        for (AttributeType type : AttributeType.values()) {
            if (!attributeItem.hasAttributeValue(type)) continue;
            helper.addBaseAttributeValue(type, AttributeManager.ContainerType.EQUIPMENT, attributeItem.getAttributeValue(type));
        }

        if (item instanceof ArmorItem armor)
            helper.addBaseAttributeValue(AttributeType.ALL_DEFENSE, AttributeManager.ContainerType.EQUIPMENT, armor.getDefense());

        if (item instanceof WeaponItem weapon)
            helper.addBaseAttributeValue(AttributeType.ALL_DAMAGE, AttributeManager.ContainerType.EQUIPMENT, weapon.getDamage());
    }

    private static A_DataMap dataMap(A_Player player) {
        return player.getDataMap(UndefinedWorldCorePlugin.instance).getDataMap(StaticValue.EQUIPMENT_MAP_KEY);
    }
}
