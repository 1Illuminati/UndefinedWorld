package org.red.minecraft.uw.core.skill.slot;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.dellarte.library.event.listener.A_Listener;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.item.U_Item;
import org.red.minecraft.uw.core.item.attribute.equipment.WeaponItem;
import org.red.minecraft.uw.core.skill.craft.SkillCraftGUI;
import org.red.minecraft.uw.core.skill.craft.SkillItem;

import java.util.List;

/**
 * 주손 아이템 우클릭/쉬프트클릭 처리.
 *
 * <p>스킬 발동 (확정: 무기를 들고 사용) — 주손에 WeaponItem을 든 상태에서:
 * <pre>
 *   우클릭          → RIGHT_CLICK 슬롯
 *   쉬프트+좌클릭    → SHIFT_LEFT_CLICK 슬롯
 *   쉬프트+우클릭    → SHIFT_RIGHT_CLICK 슬롯
 * </pre>
 * 슬롯 확장 시 resolveSlot에 판정만 추가하면 된다.
 *
 * <p>스킬 수정 (§2.6) — 주손에 <b>스킬 아이템</b>을 들고 우클릭하면 수정 GUI를 연다.
 * 스킬 아이템은 WeaponItem이 아니므로 발동 분기와 겹치지 않는다(두 분기는 상호 배타적이다).
 */
public class SkillCastListener extends A_Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // 오프핸드 중복 발화 방지 — 주손 클릭만 처리한다
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.isEmpty()) return;

        // 스킬 아이템 우클릭 → 수정 GUI (발동 분기로 내려가지 않는다)
        // 우클릭일 때만 판정해 좌클릭(공격)마다 PDC를 읽지 않게 한다
        if (isRightClick(event.getAction()) && SkillItem.isSkillItem(mainHand)) {
            event.setCancelled(true); // 블럭 설치/사용 등 기본 상호작용 차단
            openEditGui(player, mainHand);
            return;
        }

        SkillSlot slot = resolveSlot(event.getAction(), player.isSneaking());
        if (slot == null) return;

        // 확정: 무기를 들고 있어야 스킬 사용 가능 (주손 기준, T19-4와 동일)
        U_Item item = UndefinedWorldCore.getItem(mainHand);
        if (!(item instanceof WeaponItem)) return;

        boolean casted = PlayerSkillManager.cast(CommediaDellarte.getAPlayer(player), slot);
        if (casted) event.setCancelled(true); // 스킬 발동 시 기본 상호작용(블럭 설치 등) 차단
    }

    /**
     * 스킬 수정 GUI를 연다.
     *
     * <p>스킬 아이템을 <b>손에서 회수한 뒤</b> 기어를 GUI에 펼친다 —
     * 그래야 "스킬 아이템 + 그 구성 기어"가 동시에 존재하는 복사 상태가 만들어지지 않는다.
     * 기어를 하나라도 되살리지 못하면 회수하지 않고 수정을 막는다.
     */
    private void openEditGui(Player player, ItemStack skillItem) {
        A_Player aPlayer = CommediaDellarte.getAPlayer(player);

        // 스킬 아이템 재질(ENCHANTED_BOOK)은 겹치지 않지만, 겹친 상태로 들어오면 나머지가 사라지므로 막는다
        if (skillItem.getAmount() != 1) {
            aPlayer.sendMessage("스킬 아이템은 1개씩만 수정할 수 있습니다."); //todo 문구 확정
            return;
        }

        String skillName = SkillItem.getSkillName(skillItem);
        List<String> gearCodes = SkillItem.getGearCodes(skillItem);

        if (skillName == null || gearCodes.isEmpty()) return; // isSkillItem을 통과했으므로 없을 상황

        // 이름 규칙(§2.10)이 강화되기 전에 만들어진 스킬은 제작 확인 단계에서 거절된다.
        // 아이템을 회수한 뒤에 거절되면 스킬 아이템만 사라지고 기어만 남으므로
        // <b>회수하기 전에</b> 막는다. 원인을 알 수 있도록 이름 규칙을 함께 안내한다.
        if (!PlayerSkillManager.isValidName(skillName)) {
            aPlayer.sendMessage("이 스킬의 이름은 현재 이름 규칙에 맞지 않아 수정할 수 없습니다: " + skillName); //todo 문구 확정
            aPlayer.sendMessage(PlayerSkillManager.nameRuleMessage());
            return;
        }

        if (gearCodes.size() > SkillCraftGUI.GEAR_CAPACITY) {
            aPlayer.sendMessage("기어 수가 배치 칸을 넘어 수정할 수 없습니다."); //todo 문구 확정
            return;
        }

        List<ItemStack> gearStacks = SkillItem.resolveGearItems(gearCodes);
        if (gearStacks == null) {
            aPlayer.sendMessage("구성 기어를 찾을 수 없어 수정할 수 없습니다."); //todo 문구 확정
            return;
        }

        ItemStack taken = skillItem.clone();
        player.getInventory().setItemInMainHand(null); // 회수 — 이 시점부터 스킬 아이템은 GUI가 들고 있다

        boolean opened;
        try {
            opened = new SkillCraftGUI(aPlayer, skillName, gearStacks).open();
        } catch (RuntimeException e) {
            UndefinedWorldCorePlugin.sendLog("Skill edit GUI open failed: " + player.getName() + " / " + e);
            opened = false;
        }

        // 열리지 않았으면 닫힘 이벤트도 오지 않으므로 회수한 스킬 아이템을 여기서 돌려줘야 한다.
        // (기어는 코드로 새로 만든 것이라 GUI와 함께 버려지면 되고, 플레이어가 잃는 것은 없다)
        if (!opened) {
            aPlayer.addItemNature(taken);
            aPlayer.sendMessage("스킬 수정 GUI를 열지 못했습니다."); //todo 문구 확정
        }
    }

    private boolean isRightClick(Action action) {
        return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
    }

    @Nullable
    private SkillSlot resolveSlot(Action action, boolean sneaking) {
        boolean leftClick = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;

        if (isRightClick(action)) return sneaking ? SkillSlot.SHIFT_RIGHT_CLICK : SkillSlot.RIGHT_CLICK;
        if (leftClick && sneaking) return SkillSlot.SHIFT_LEFT_CLICK;
        return null; // 일반 좌클릭은 공격 유지
    }
}
