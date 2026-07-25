package org.red.minecraft.uw.core.player.equipment;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.item.U_Item;
import org.red.minecraft.uw.core.item.attribute.equipment.WeaponItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 주손 무기 주기 스캔 태스크. (구조 결정 T19-4: 주손만 감지)
 * 들고 있는 무기가 바뀌면 EQUIPMENT 컨테이너를 재계산한다.
 * 접속 후 첫 스캔에서도 재계산되므로 저장된 GUI 장비의 접속 시 적용도 여기서 겸한다.
 *
 * todo 왼손 서브무기 확장: 스캔 대상을 슬롯 목록(주손/왼손)으로 일반화 (서브무기 설계 확정 후)
 */
public final class WeaponScanTask implements Runnable {

    /** 플레이어별 마지막 감지 무기 코드 (무기 아님/빈손이면 null) */
    private static final Map<UUID, String> lastWeaponCode = new HashMap<>();

    /** onEnable에서 호출. config 주기로 반복 태스크 등록 */
    public static void start(UndefinedWorldCorePlugin plugin) {
        long period = plugin.getConfig().getLong("EquipmentSetting.weaponScanTicks", 10L);
        Bukkit.getScheduler().runTaskTimer(plugin, new WeaponScanTask(), period, period);
    }

    /** 퇴장 시 캐시 정리 (EquipmentGUIListener.onQuit에서 호출) */
    public static void remove(UUID id) {
        lastWeaponCode.remove(id);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID id = player.getUniqueId();
            String code = resolveWeaponCode(player.getInventory().getItemInMainHand());

            // 접속 후 첫 스캔(캐시 없음)이거나 무기가 바뀐 경우에만 재계산
            if (lastWeaponCode.containsKey(id) && Objects.equals(code, lastWeaponCode.get(id))) continue;

            lastWeaponCode.put(id, code);
            EquipmentManager.applyEquipmentAttributes(CommediaDellarte.getAPlayer(player));
        }
    }

    @Nullable
    private String resolveWeaponCode(ItemStack mainHand) {
        if (mainHand.isEmpty()) return null;

        U_Item item = UndefinedWorldCore.getItem(mainHand);
        return item instanceof WeaponItem weapon ? weapon.getItemCode() : null;
    }
}
