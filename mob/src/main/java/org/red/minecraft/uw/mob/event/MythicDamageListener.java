package org.red.minecraft.uw.mob.event;

import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.red.minecraft.dellarte.library.event.listener.A_Listener;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;

public class MythicDamageListener extends A_Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void mythicDamage(MythicDamageEvent event) {
        // 추적용 로그. caster 가 없는 데미지에서 NPE 로 로그 자체가 죽지 않게 한다.
        String casterName = event.getCaster() == null ? "null" : event.getCaster().getName();
        UndefinedWorldCorePlugin.sendLog(String.format("Damage:%f, Caster:%s", event.getDamage(), casterName));
    }
}
