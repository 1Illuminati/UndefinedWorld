package org.red.minecraft.uw.core.skill.slot;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.event.listener.A_Listener;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.item.U_Item;
import org.red.minecraft.uw.core.item.attribute.equipment.WeaponItem;
import org.red.minecraft.uw.core.skill.slot.SkillSlot;

/**
 * 스킬 발동 리스너. (확정: 무기를 들고 사용)
 * 주손에 WeaponItem을 든 상태에서:
 *   우클릭          → RIGHT_CLICK 슬롯
 *   쉬프트+좌클릭    → SHIFT_LEFT_CLICK 슬롯
 *   쉬프트+우클릭    → SHIFT_RIGHT_CLICK 슬롯
 * 슬롯 확장 시 resolveSlot에 판정만 추가하면 된다.
 */
public class SkillCastListener extends A_Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // 오프핸드 중복 발화 방지
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        SkillSlot slot = resolveSlot(event.getAction(), player.isSneaking());
        if (slot == null) return;

        // 확정: 무기를 들고 있어야 스킬 사용 가능 (주손 기준, T19-4와 동일)
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.isEmpty()) return;

        U_Item item = UndefinedWorldCore.getItem(mainHand);
        if (!(item instanceof WeaponItem)) return;

        boolean casted = PlayerSkillManager.cast(CommediaDellarte.getAPlayer(player), slot);
        if (casted) event.setCancelled(true); // 스킬 발동 시 기본 상호작용(블럭 설치 등) 차단
    }

    @Nullable
    private SkillSlot resolveSlot(Action action, boolean sneaking) {
        boolean rightClick = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
        boolean leftClick = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;

        if (rightClick) return sneaking ? SkillSlot.SHIFT_RIGHT_CLICK : SkillSlot.RIGHT_CLICK;
        if (leftClick && sneaking) return SkillSlot.SHIFT_LEFT_CLICK;
        return null; // 일반 좌클릭은 공격 유지
    }
}
