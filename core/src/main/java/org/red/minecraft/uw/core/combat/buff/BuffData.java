package org.red.minecraft.uw.core.combat.buff;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.red.minecraft.dellarte.library.entity.A_Entity;

import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * 하나의 적용된 버프 인스턴스. 대상 / 버프 효과 / 남은 시간 / 비동기 여부를 들고,
 * 내부에서 BukkitTask를 돌리며 4가지 종료 조건을 처리한다.
 *
 * 생성자 시그니처: (Entity, Buff, 지속시간(틱), 비동기여부)
 * 스케줄링에 필요한 Plugin / 종료 콜백은 start()에서 매니저가 주입한다.
 */
public class BuffData {

    private final A_Entity entity;
    private final Buff buff;
    private final boolean async;
    private final long durationTicks;   // 음수면 무한 지속
    private long remainingTicks;

    private Plugin plugin;
    private BukkitTask task;
    private volatile boolean ended = false;                 // 중복 종료 방지
    private BiConsumer<BuffData, BuffRemoveReason> onEnd = (d, r) -> {};

    public BuffData(A_Entity entity, Buff buff, long durationTicks, boolean async) {
        this.entity = entity;
        this.buff = buff;
        this.durationTicks = durationTicks;
        this.remainingTicks = durationTicks;
        this.async = async;
    }

    /** 매니저가 호출: 플러그인/종료콜백 주입 → onApply → 반복 태스크 등록. (메인스레드에서 호출) */
    void start(Plugin plugin, BiConsumer<BuffData, BuffRemoveReason> onEnd) {
        this.plugin = plugin;
        this.onEnd = onEnd;

        buff.onApply(entity);

        int period = period();
        Runnable run = this::run;
        this.task = async
                ? Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, run, period, period)
                : Bukkit.getScheduler().runTaskTimer(plugin, run, period, period);
    }

    private void run() {
        if (ended) return;

        // --- 종료 조건 자체 감지 (이벤트를 연결하지 않아도 안전망으로 동작) ---
        // 2. 사망 / 무효(몹 디스폰 등)
        if (entity.isDead() || !entity.isValid()) {
            finish(isOffline() ? BuffRemoveReason.QUIT : BuffRemoveReason.DEATH);
            return;
        }
        // 3. 로그아웃
        if (isOffline()) {
            finish(BuffRemoveReason.QUIT);
            return;
        }

        // --- 실제 버프 효과 ---
        try {
            buff.tick(entity);
        } catch (Throwable t) {
            if (plugin != null) {
                plugin.getLogger().warning("[Buff] tick 오류 (" + buff.type() + "): " + t);
            }
        }

        // --- 1. 시간 종료 (durationTicks < 0 이면 무한 지속) ---
        if (durationTicks >= 0) {
            remainingTicks -= period();
            if (remainingTicks <= 0) {
                finish(BuffRemoveReason.EXPIRED);
            }
        }
    }

    private boolean isOffline() {
        return entity instanceof Player p && !p.isOnline();
    }

    private int period() {
        return Math.max(1, buff.tickCount());
    }

    /** async 태스크 안에서 감지한 종료는 메인스레드로 넘겨 정리한다. */
    private void finish(BuffRemoveReason reason) {
        if (async && plugin != null) {
            Bukkit.getScheduler().runTask(plugin, () -> end(reason));
        } else {
            end(reason);
        }
    }

    /**
     * 실제 종료. (메인스레드에서 호출) 매니저의 강제 종료/이벤트 처리에서도 직접 호출한다.
     * 태스크를 취소하고 매니저 콜백(맵 정리 + onRemove)을 실행한다. 중복 호출은 무시된다.
     */
    void end(BuffRemoveReason reason) {
        if (ended) return;
        ended = true;
        if (task != null) task.cancel();
        onEnd.accept(this, reason);
    }

    public A_Entity getEntity()       { return entity; }
    public Buff getBuff()           { return buff; }
    public BuffType getType()       { return buff.type(); }
    public boolean isAsync()        { return async; }
    public long getRemainingTicks() { return remainingTicks; }
    public boolean isEnded()        { return ended; }
    public UUID getEntityId()       { return entity.getUniqueId(); }
}
