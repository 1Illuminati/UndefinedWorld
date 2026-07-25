package org.red.minecraft.uw.core.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeType;

/**
 * 온라인 플레이어의 체력/마나/스테미나 주기 재생 태스크.
 *
 * 재생량 = (*_REGEN - *_REGEN_REDUCE), 0 미만이면 재생하지 않는다 (재생 감소로 체력이 깎이지는 않음)
 * 주기: config RegenSetting.periodTicks (기본 20틱 = 1초마다 재생량 1회 적용)
 *
 * 몹 리젠은 범위 외 (MythicMobs 자체 처리)
 * todo 재생량 배율/주기 밸런스는 사용자 확정 필요
 */
public final class ResourceRegenTask implements Runnable {

    /** onEnable에서 호출. config 주기로 반복 태스크 등록 */
    public static void start(UndefinedWorldCorePlugin plugin) {
        long period = plugin.getConfig().getLong("RegenSetting.periodTicks", 20L);
        Bukkit.getScheduler().runTaskTimer(plugin, new ResourceRegenTask(), period, period);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            regen(CommediaDellarte.getAPlayer(player));
        }
    }

    private void regen(A_Player player) {
        PlayerHelper helper = new PlayerHelper(player);

        double healthRegen = regenValue(helper, AttributeType.HEALTH_REGEN, AttributeType.HEALTH_REGEN_REDUCE);
        if (healthRegen > 0 && !player.isDead()) {
            double max = player.getMaxHealth();
            player.setHealth(Math.min(max, player.getHealth() + healthRegen));
        }

        double manaRegen = regenValue(helper, AttributeType.MANA_REGEN, AttributeType.MANA_REGEN_REDUCE);
        if (manaRegen > 0) helper.addMana(manaRegen);

        double staminaRegen = regenValue(helper, AttributeType.STAMINA_REGEN, AttributeType.STAMINA_REGEN_REDUCE);
        if (staminaRegen > 0) helper.addStamina(staminaRegen);
    }

    private double regenValue(PlayerHelper helper, AttributeType regen, AttributeType reduce) {
        return helper.getAttributeValue(regen) - helper.getAttributeValue(reduce);
    }
}
