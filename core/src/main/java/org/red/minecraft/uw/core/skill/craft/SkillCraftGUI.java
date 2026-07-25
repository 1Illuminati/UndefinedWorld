package org.red.minecraft.uw.core.skill.craft;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.exeception.PowerOverException;
import org.red.minecraft.uw.core.item.U_Item;
import org.red.minecraft.uw.core.item.gear.GearItem;
import org.red.minecraft.uw.core.skill.SkillDefinition;
import org.red.minecraft.uw.core.skill.gear.Gear;
import org.red.minecraft.uw.core.skill.slot.PlayerSkillManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 스킬 제작 GUI. (확정: 기어 순서 배치형)
 * 두 번째 줄 9칸에 Gear 아이템을 순서대로 배치(배치 순서 = 노드 실행 순서)하고
 * 확인 버튼을 누르면 검증(Power<=9) 후 스킬이 플레이어에 저장된다.
 *
 * todo 제작 시 기어 아이템 소모 여부 미확정 — 현재는 소모하지 않고 돌려준다
 * todo GUI 타이틀/디자인 사용자 확정 필요
 */
public class SkillCraftGUI implements InventoryHolder {

    private static final int SIZE = 27;
    /** 기어 배치 슬롯 (2번째 줄 9칸, 왼쪽부터 실행 순서) */
    private static final int GEAR_SLOT_START = 9;
    private static final int GEAR_SLOT_END = 17;
    /** 확인 버튼 위치 */
    private static final int CONFIRM_SLOT = 22;

    private final A_Player player;
    private final String skillName;
    private final Inventory inventory;
    private boolean confirmed = false;

    public SkillCraftGUI(A_Player player, String skillName) {
        this.player = player;
        this.skillName = skillName;
        this.inventory = Bukkit.createInventory(this, SIZE, Component.text("스킬 제작: " + skillName));

        ItemStack filler = createButton(Material.GRAY_STAINED_GLASS_PANE, Component.empty());
        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, filler);
        }
        for (int i = GEAR_SLOT_START; i <= GEAR_SLOT_END; i++) {
            inventory.setItem(i, null);
        }
        inventory.setItem(CONFIRM_SLOT, createButton(Material.LIME_CONCRETE, Component.text("제작 확인")));
    }

    public void open() {
        player.openInventory(inventory);
    }

    public static boolean isGearSlot(int index) {
        return index >= GEAR_SLOT_START && index <= GEAR_SLOT_END;
    }

    public static boolean isConfirmSlot(int index) {
        return index == CONFIRM_SLOT;
    }

    /** 배치 가능한 아이템인지 판정 (Gear 아이템만) */
    public static boolean isGearItem(ItemStack stack) {
        return UndefinedWorldCore.getItem(stack) instanceof GearItem;
    }

    /**
     * 확인 처리: 배치 순서대로 기어 수집 → SkillDefinition 검증 → 저장.
     * 성공/실패와 무관하게 배치된 아이템은 돌려준다 (소모 여부 미확정).
     */
    public void confirm() {
        List<String> gearCodes = new ArrayList<>();
        List<Gear> gears = new ArrayList<>();

        for (int i = GEAR_SLOT_START; i <= GEAR_SLOT_END; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack == null || stack.isEmpty()) continue;

            U_Item item = UndefinedWorldCore.getItem(stack);
            if (!(item instanceof GearItem gearItem)) continue; // 배치 검증상 없을 상황

            gearCodes.add(item.getItemCode());
            gears.add(gearItem.toGear());
        }

        if (gears.isEmpty()) {
            player.sendMessage("기어를 1개 이상 배치해야 합니다."); //todo 문구/형식 사용자 확정 필요
            return;
        }

        SkillDefinition skill;
        try {
            skill = new SkillDefinition(skillName, gears);
        } catch (PowerOverException e) {
            player.sendMessage("스킬 파워 총합이 9를 초과했습니다."); //todo 문구/형식 사용자 확정 필요
            return;
        } catch (IllegalArgumentException e) {
            player.sendMessage("스킬 구성이 잘못되었습니다: " + e.getMessage());
            return;
        }

        PlayerSkillManager.saveSkill(player, skillName, gearCodes);
        player.sendMessage(String.format("스킬 [%s] 제작 완료! (파워 %d, 쿨타임 %d초, 캐스팅 %d)",
                skillName, skill.getSkillPower(), skill.getSkillCoolDown(), skill.getCastingTime())); //todo 문구 확정

        confirmed = true;
        returnGearItems();
        player.closeInventory();
    }

    /** 배치된 기어 아이템을 플레이어에게 반환하고 슬롯을 비운다 */
    public void returnGearItems() {
        for (int i = GEAR_SLOT_START; i <= GEAR_SLOT_END; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack == null || stack.isEmpty()) continue;

            player.addItemNature(stack);
            inventory.setItem(i, null);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public A_Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    private ItemStack createButton(Material material, Component name) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(name);
        stack.setItemMeta(meta);
        return stack;
    }
}
