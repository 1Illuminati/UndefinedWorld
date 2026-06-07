package org.red.minecraft.uw.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class UndefinedWorldCorePlugin extends JavaPlugin {
    private static UndefinedWorldCore core;
    private static boolean coreLock = true;
    public static void sendLog(Object message) {
        instance.getLogger().info(message.toString());
    }

    public static UndefinedWorldCorePlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        core = new UndefinedWorldCore();

        Bukkit.getScheduler().runTaskLater(this, () -> {

        }, 1);
    }
}
