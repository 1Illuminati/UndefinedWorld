package org.red.minecraft.uw.core.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.command.AbstractCommand;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.attribute.stat.Stat;
import org.red.minecraft.uw.core.player.PlayerHelper;

import java.util.List;

public class StatCommand extends Command {
    @Override
    public @NotNull String getName() {
        return "stat";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull String s, String[] strings) {
        if (strings.length < 2) return true;

        A_Player target = CommediaDellarte.getAPlayer(strings[1]);
        if (target == null) {
            commandSender.sendMessage("Player " + strings[1] + " is exist");
            return false;
        }

        PlayerHelper helper = new PlayerHelper(target);
        switch (strings[0]) {
            case "set" -> helper.setStatValue(Stat.name(strings[2]),  Integer.parseInt(strings[3]));
            case "get" -> commandSender.sendMessage(String.format("%s Stat %s: %d", strings[1], strings[2], helper.getStatValue(Stat.name(strings[2]))));
            case "apply" -> helper.applyStatToAttribute();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull String s, String[] strings) {
        return null;
    }
}
