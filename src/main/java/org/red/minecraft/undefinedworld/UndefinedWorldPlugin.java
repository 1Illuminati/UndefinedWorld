package org.red.minecraft.undefinedworld;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.red.library.data.DataMap;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.data.IDataStroage;
import org.red.minecraft.dellarte.library.event.FirstLoadEvent;
import org.red.minecraft.undefinedworld.attribute.stat.Stat;
import org.red.minecraft.undefinedworld.command.TestCommand;
import org.red.minecraft.undefinedworld.item.U_ItemManager;

public class UndefinedWorldPlugin extends JavaPlugin implements Listener {
    public static UndefinedWorldPlugin instance;
    public static IDataStroage stroage; 
    public static U_ItemManager itemManager = new U_ItemManager(new DataMap());

    @Override
    public void onEnable() {
        Stat.statSetting();
        this.getCommand("test").setExecutor(new TestCommand());
        Bukkit.getPluginManager().registerEvents(this, this);
        UndefinedWorldPlugin.instance = this;
    }

    @EventHandler
    public void firstLoadEvent(FirstLoadEvent event) {
        UndefinedWorldPlugin.stroage = CommediaDellarte.getStorage(new NamespacedKey(this, "stroage"));
        stroage.loadAll();
        UndefinedWorldPlugin.itemManager = new U_ItemManager(stroage.getDataMap(U_ItemManager.UITEM));
    }
}
