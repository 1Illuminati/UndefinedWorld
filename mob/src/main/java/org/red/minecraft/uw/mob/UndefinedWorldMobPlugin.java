package org.red.minecraft.uw.mob;

import org.bukkit.plugin.java.JavaPlugin;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.mob.event.EntityDamageListener;
import org.red.minecraft.uw.mob.event.MythicDamageListener;
import org.red.minecraft.uw.mob.event.MythicMechanicLoadListener;

public class UndefinedWorldMobPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        UndefinedWorldCore.registerModule(new MobModule());
        new MythicDamageListener().register(this);
        new MythicMechanicLoadListener().register(this);
        new EntityDamageListener().register(this);
    }
}
