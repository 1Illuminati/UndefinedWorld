package org.red.minecraft.uw.core.command;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.red.minecraft.dellarte.library.command.AbstractCommand;

public abstract class Command extends AbstractCommand {
    public void register(JavaPlugin plugin) {
        PluginCommand command = plugin.getCommand(this.getName());
        // plugin.yml에 미등록이면 NPE만 남아 원인 추적이 어려우므로 명령어 이름을 드러낸다
        if (command == null)
            throw new IllegalStateException("plugin.yml에 등록되지 않은 명령어: " + this.getName()
                    + " (plugin: " + plugin.getName() + ")");

        command.setExecutor(this);
        command.setTabCompleter(this);
    }
}
