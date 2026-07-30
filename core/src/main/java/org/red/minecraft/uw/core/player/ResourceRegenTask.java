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
            // 한 명에서 터진 예외가 나머지 플레이어의 재생까지 막지 않도록 개별로 격리한다
            try {
                regen(CommediaDellarte.getAPlayer(player));
            } catch (Exception e) {
                UndefinedWorldCorePlugin.sendLog("Resource regen failed: " + player.getName() + " / " + e);
            }
        }
    }

    private void regen(A_Player player) {
        PlayerHelper helper = new PlayerHelper(player);

        double healthRegen = regenValue(helper, AttributeType.HEALTH_REGEN, AttributeType.HEALTH_REGEN_REDUCE);
        if (healthRegen > 0 && !player.isDead()) {
            double max = player.getMaxHealth();
            // setHealth는 범위를 벗어난 값에 예외를 던지므로 0~최대체력으로 자른다.
            // 최대체력이 0 이하인 비정상 상태에서는 회복 대신 아무것도 하지 않는다 (0으로 잘려 죽는 것 방지)
            if (max > 0) player.setHealth(Math.clamp(player.getHealth() + healthRegen, 0, max));
        }

        double manaRegen = regenValue(helper, AttributeType.MANA_REGEN, AttributeType.MANA_REGEN_REDUCE);
        if (manaRegen > 0) helper.addMana(manaRegen);

        double staminaRegen = regenValue(helper, AttributeType.STAMINA_REGEN, AttributeType.STAMINA_REGEN_REDUCE);
        if (staminaRegen > 0) helper.addStamina(staminaRegen);
    }

    /**
     * 재생량 = REGEN - REGEN_REDUCE.
     * attribute 값이 NaN/무한이면 이후 setHealth/자원 저장이 오염되므로 0으로 처리한다.
     */
    private double regenValue(PlayerHelper helper, AttributeType regen, AttributeType reduce) {
        double value = helper.getAttributeValue(regen) - helper.getAttributeValue(reduce);
        if (!Double.isFinite(value)) {
            UndefinedWorldCorePlugin.sendLog("Regen value is not finite, treated as 0: " + regen.name());
            return 0;
        }
        return value;
    }
}
