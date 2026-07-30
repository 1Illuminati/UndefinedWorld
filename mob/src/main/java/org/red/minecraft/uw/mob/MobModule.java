package org.red.minecraft.uw.mob;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.module.IMobModule;
import org.red.minecraft.uw.core.player.PlayerHelper;
import org.red.minecraft.uw.mob.attribute.MythicAttributeManager;

public class MobModule implements IMobModule {
    @Override
    public boolean isMythicMob(A_Entity entity) {
        return this.isMythicMob(entity.getEntity());
    }

    @Override
    public boolean isMythicMob(Entity entity) {
        return MythicBukkit.inst().getMobManager().isMythicMob(entity);
    }

    @Override
    public boolean isDamageableMob(A_Entity entity) {
        return this.isDamageableMob(entity.getEntity());
    }

    @Override
    public boolean isDamageableMob(Entity entity) {
        return entity instanceof LivingEntity;
    }

    @Override
    public AttributeManager getAttributeHolder(A_Entity entity) {
        if (entity instanceof A_Player player) return new PlayerHelper(player);

        if (isMythicMob(entity)) {
            // isMythicMob 이 true 여도 ActiveMob 등록이 이미 풀렸을 수 있다(디스폰/MythicMobs 리로드 직후).
            // null 을 그대로 넘기면 MythicAttributeManager 생성자에서 NPE 가 나 데미지 계산 전체가 죽는다.
            ActiveMob activeMob = MythicBukkit.inst().getMobManager().getMythicMobInstance(entity.getEntity());
            if (activeMob != null) return new MythicAttributeManager(activeMob);

            UndefinedWorldCorePlugin.sendLog("ActiveMob 조회 실패 → 기본 AttributeManager 로 대체: " + entity.getUniqueIdStr());
        }

        return new AttributeManager(entity);
    }
}
