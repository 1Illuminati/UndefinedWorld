package org.red.minecraft.undefinedworld.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.undefinedworld.gui.creater.ItemCreaterMain;

public class TestCommand implements CommandExecutor {
    
    public TestCommand() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        player.openInventory(new ItemCreaterMain(args[0], player.getInventory().getItemInMainHand()).getInventory());
        A_Player p = CommediaDellarte.getAPlayer(player);
        return true;
    }
}
