package org.red.minecraft.uw.core;

import org.bukkit.plugin.java.JavaPlugin;

public class UndefinedWorldCorePlugin extends JavaPlugin {
    public static void sendLog(Object message) {
        instance.getLogger().info(message.toString());
    }

    public static UndefinedWorldCorePlugin instance;

    @Override
    public void onEnable() {
        instance = this;
    }
}
