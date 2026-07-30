package org.red.minecraft.uw.enchant;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;
import org.red.minecraft.uw.enchant.command.EnchantCommand;
import org.red.minecraft.uw.enchant.gui.EnchantGUI;
import org.red.minecraft.uw.enchant.gui.EnchantGUIListener;

import java.util.List;

public class UndefinedWorldEnchantPlugin extends JavaPlugin {
    public static UndefinedWorldEnchantPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        new EnchantCommand().register(this);
        new EnchantGUIListener().register(this);
    }

    /**
     * 플러그인 종료/리로드 시 강화 GUI에 올려둔 아이템이 사라지지 않도록 반환한다.
     * GUI 목록을 따로 들고 있지 않고 열려 있는 인벤토리에서 찾는다 (상태 중복 저장 방지).
     */
    @Override
    public void onDisable() {
        for (Player player : List.copyOf(Bukkit.getOnlinePlayers())) {
            InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
            if (!(holder instanceof EnchantGUI gui)) continue;

            player.closeInventory();
            gui.handleClose(); // 멱등 — 닫힘 처리가 이미 반환했으면 슬롯이 비어 있어 중복 반환되지 않는다
        }
    }
}
