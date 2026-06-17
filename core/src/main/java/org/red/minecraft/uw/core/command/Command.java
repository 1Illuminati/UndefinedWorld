package org.red.minecraft.uw.core.command;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.red.minecraft.dellarte.library.command.AbstractCommand;

public abstract class Command extends AbstractCommand {
    public void register(JavaPlugin plugin) {
        PluginCommand command = plugin.getCommand(this.getName());
        command.setExecutor(this);
        command.setTabCompleter(this);
    }
}
