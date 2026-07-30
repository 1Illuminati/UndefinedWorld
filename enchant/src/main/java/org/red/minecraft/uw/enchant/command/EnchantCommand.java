package org.red.minecraft.uw.enchant.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.command.Command;
import org.red.minecraft.uw.enchant.gui.EnchantGUI;

import java.util.List;

/**
 * 인챈트 강화 GUI 오픈: /u_enchant
 */
public class EnchantCommand extends Command {

    @Override
    public @NotNull String getName() {
        return "u_enchant";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }

        A_Player aPlayer = CommediaDellarte.getAPlayer(player);
        new EnchantGUI(aPlayer).open(); // U_Gui.open() — 다른 GUI(제작/장비)와 동일한 진입 경로
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        return List.of();
    }
}
