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

    /**
     * 저장된 장비 조회.
     *
     * A_DataMap.getItemStack(key)는 키가 없으면 기본값(AIR)을 맵에 써넣고 그 값을 돌려주므로
     * (조회만 해도 저장 데이터가 늘어나고 null 계약이 깨진다) containsKey로 먼저 확인한다.
     * 저장 데이터와 호출부가 같은 ItemStack 인스턴스를 공유하지 않도록 복사해서 돌려준다.
     */
    @Nullable
    public static ItemStack getEquipment(A_Player player, EquipSlot slot) {
        A_DataMap map = dataMap(player);
        if (!map.containsKey(slot.name())) return null;

        ItemStack stack = map.getItemStack(slot.name());
        if (stack == null || stack.isEmpty()) return null;
        return stack.clone();
    }

    /**
     * 장비 저장.
     * GUI 인벤토리에서 꺼낸 ItemStack은 인벤토리 슬롯을 직접 참조하는 mirror일 수 있어
     * 그대로 저장하면 이후 인벤토리 조작이 저장 데이터를 조용히 바꾼다 → 반드시 복사해서 저장한다.
     */
    public static void setEquipment(A_Player player, EquipSlot slot, @Nullable ItemStack stack) {
        A_DataMap map = dataMap(player);
        if (stack == null || stack.isEmpty()) map.remove(slot.name());
        else map.put(slot.name(), stack.clone());
    }

    /**
     * 해당 슬롯에 장착 가능한 아이템인지 판정.
     * 장비 슬롯은 1칸에 1개만 취급하므로 겹쳐진 아이템(amount > 1)은 거부한다.
     * (허용하면 장비칸이 창고가 되고, 스택 분리 시 저장 데이터가 함께 변한다)
     */
    public static boolean canEquip(EquipSlot slot, @Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getAmount() != 1) return false;

        U_Item item = UndefinedWorldCore.getItem(stack);
        return item instanceof EquipmentItem equipment && equipment.getEquipmentType() == slot.acceptType;
    }

    /**
     * GUI 장비 + 주손 무기를 EQUIPMENT 컨테이너에 재계산 적용.
     * todo 왼손 서브무기 확장 시 무기 수집부를 슬롯 목록 순회로 일반화
     */
    public static void applyEquipmentAttributes(A_Player player) {
        // item 모듈 등록 전에는 모든 장비가 미인식으로 처리되어 EQUIPMENT 컨테이너를 0으로 밀어버린다.
        // 조회가 불가능한 시점에는 재계산 자체를 하지 않는 것이 옳다 (기존 값 유지).
        if (!UndefinedWorldCore.hasItemModule()) {
            UndefinedWorldCorePlugin.sendLog("Equipment recalculation skipped (item module not registered): " + player.getName());
            return;
        }

        PlayerHelper helper = new PlayerHelper(player);

        // 초기화 (전체 AttributeType을 0으로 쓰면 저장 DataMap에 전체 키가 남으므로 컨테이너를 비운다)
        helper.clearBaseAttributeValues(AttributeManager.ContainerType.EQUIPMENT);

        // GUI 장비 합산
        for (EquipSlot slot : EquipSlot.values()) {
            ItemStack stack = getEquipment(player, slot);
            if (stack == null || stack.isEmpty()) continue;

            // 저장 데이터가 슬롯 규칙에 맞는지 여기서도 확인한다 (외부에서 저장 데이터가 바뀐 경우 대비)
            if (!canEquip(slot, stack)) {
                UndefinedWorldCorePlugin.sendLog("Stored equipment ignored (slot mismatch or unknown item): "
                        + player.getName() + " slot=" + slot.name() + " item=" + stack.getType());
                continue;
            }

            applyItem(helper, UndefinedWorldCore.getItem(stack));
        }

        // 주손 무기 (T19-4: 주손만 감지)
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (!mainHand.isEmpty()) {
            U_Item item = UndefinedWorldCore.getItem(mainHand);
            if (item instanceof WeaponItem) applyItem(helper, item);
        }

        // 장비 변경으로 MANA_MAX/STAMINA_MAX가 줄었을 수 있으므로 현재값을 다시 자른다
        helper.clampResourcesToMax();

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
