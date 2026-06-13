package org.red.minecraft.uw.mob.event;

import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.red.minecraft.dellarte.library.event.listener.A_Listener;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;

public class MythicDamageListener extends A_Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void mythicDamage(MythicDamageEvent event) {
        UndefinedWorldCorePlugin.sendLog(String.format("Damage:%f, Caster:%s", event.getDamage(), event.getCaster().getName()));
    }
}
