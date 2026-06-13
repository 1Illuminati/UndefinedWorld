package org.red.minecraft.uw.mob.event;

import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import org.bukkit.event.EventHandler;
import org.red.minecraft.dellarte.library.event.listener.A_Listener;
import org.red.minecraft.uw.mob.mechanic.DamageMechanic;

public class MythicMechanicLoadListener extends A_Listener {
    @EventHandler
    public void mythicMechanicLoad(MythicMechanicLoadEvent event) {
        if(event.getMechanicName().equalsIgnoreCase("U_Damage"))	{
            event.register(new DamageMechanic(event.getConfig()));
        }
    }
}
