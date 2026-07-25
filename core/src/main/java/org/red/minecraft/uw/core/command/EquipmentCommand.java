package org.red.minecraft.uw.core.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.uw.core.player.equipment.EquipmentGUI;

import java.util.List;

/**
 * 장비 GUI 열기 명령어. (구조 결정 T19-2: /equipment, /equip, /eq — 별칭은 plugin.yml)
 */
public class EquipmentCommand extends Command {

    @Override
    public @NotNull String getName() {
        return "equipment";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull String s, String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }

        new EquipmentGUI(CommediaDellarte.getAPlayer(player)).open();
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull String s, String[] strings) {
        return List.of();
    }
}
