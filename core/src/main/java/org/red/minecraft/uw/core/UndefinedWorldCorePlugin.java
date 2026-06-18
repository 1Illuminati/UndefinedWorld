package org.red.minecraft.uw.core;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.red.minecraft.uw.core.attribute.stat.Stat;
import org.red.minecraft.uw.core.command.StatCommand;
import org.red.minecraft.uw.core.util.papi.U_PapiPlayer;

import java.util.Objects;

public class UndefinedWorldCorePlugin extends JavaPlugin {
    public static void sendLog(Object message) {
        instance.getLogger().info(message.toString());
    }

    public static UndefinedWorldCorePlugin instance;
    public static FileConfiguration config;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        config = this.getConfig();

        Stat.configSet(Objects.requireNonNull(config.getConfigurationSection("StatSetting")));
        new StatCommand().register(this);
        new U_PapiPlayer().register();
        Bukkit.getScheduler().runTaskLater(this, () -> {

        }, 1);
    }
}
