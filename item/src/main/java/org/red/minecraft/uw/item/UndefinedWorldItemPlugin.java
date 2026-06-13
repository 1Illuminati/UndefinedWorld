package org.red.minecraft.uw.item;

import com.nexomc.nexo.api.events.NexoMechanicsRegisteredEvent;
import com.nexomc.nexo.mechanics.MechanicsManager;
import com.nexomc.nexo.utils.logs.Logs;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.red.minecraft.uw.item.mechanic.factory.U_ItemMechanicFactory;

public class UndefinedWorldItemPlugin extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void nRegister(NexoMechanicsRegisteredEvent event) {
        MechanicsManager.INSTANCE.registerMechanicFactory(new U_ItemMechanicFactory(), true);
        Logs.logInfo("Registered Mechanic!");
    }
}
