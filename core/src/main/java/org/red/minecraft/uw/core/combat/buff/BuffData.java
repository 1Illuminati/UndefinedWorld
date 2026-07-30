package org.red.minecraft.uw.core.combat.buff;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_Player;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * 하나의 적용된 버프 인스턴스. 대상 / 버프 효과 / 남은 시간 / 비동기 여부를 들고,
 * 내부에서 BukkitTask를 돌리며 4가지 종료 조건을 처리한다.
 *
 * 태스크 2개를 사용한다.
 *   - tickTask   : buff.tickCount() 주기로 buff.tick() 실행 + 사망/로그아웃 자체 감지 (async 옵션 적용 대상)
 *   - expireTask : durationTicks 후 1회 실행되는 시간 종료 전용 태스크 (항상 메인스레드)
 * 시간 종료를 tick 주기에 맞춰 차감하면 duration이 주기의 배수가 아닐 때 최대 (주기-1)틱 만큼
 * 더 지속되므로, 만료만 별도 태스크로 분리해 정확한 시점에 끝낸다.
 *
 * 생성자 시그니처: (Entity, Buff, 지속시간(틱), 비동기여부)
 * 스케줄링에 필요한 Plugin / 종료 콜백은 start()에서 매니저가 주입한다.
 */
public class BuffData {

    private final A_Entity entity;
    private final Buff buff;
    private final boolean async;
    private final long durationTicks;   // 음수면 무한 지속

    private volatile Plugin plugin;
    private volatile BukkitTask tickTask;
    private volatile BukkitTask expireTask;

    /** 적용 시점의 서버 틱. 남은 시간을 tick 주기와 무관하게 계산하기 위해 보관한다. (-1 = start() 이전) */
    private volatile int startTick = -1;

    /** 중복 종료 방지. 검사와 설정이 갈라지면 onRemove가 두 번 돌아 스탯이 이중 차감되므로 CAS로 묶는다. */
    private final AtomicBoolean ended = new AtomicBoolean(false);

    /**
     * 이 버프의 <b>종료 안내 메시지만</b> 보내지 않는다는 표시. 효과 원복(onRemove)·맵 정리는
     * 일반 FORCED 종료와 완전히 동일하다.
     *
     * 종료 사유(BuffRemoveReason)를 새로 만들지 않은 이유: Buff.onRemove 구현들이 reason 으로 분기하고 있어
     * 사유가 늘면 그 분기들의 의미가 함께 바뀐다. (Process.md §2.6 의 "종료 4가지"도 그대로 유지된다)
     *
     * 사용처는 BuffManager 두 곳뿐이다 — 재적용 교체, 서버 종료.
     */
    private volatile boolean endNoticeSuppressed = false;

    private BiConsumer<BuffData, BuffRemoveReason> onEnd = (d, r) -> {};

    public BuffData(A_Entity entity, Buff buff, long durationTicks, boolean async) {
        this.entity = entity;
        this.buff = buff;
        this.durationTicks = durationTicks;
        this.async = async;
    }

    /**
     * 매니저가 호출: 플러그인/종료콜백 주입 → onApply → 태스크 등록. (메인스레드에서 호출)
     *
     * 실패 시 계약:
     *   - onApply 에서 실패 → 적용된 것이 없으므로 onRemove 없이 그대로 전파 (매니저가 맵에서 되돌린다)
     *   - 태스크 등록에서 실패 → onApply 는 이미 적용됐으므로 여기서 즉시 onRemove 로 원복 후 전파
     * 두 경우 모두 onEnd(매니저 콜백)는 호출하지 않는다.
     */
    void start(Plugin plugin, BiConsumer<BuffData, BuffRemoveReason> onEnd) {
        this.plugin = plugin;
        this.onEnd = onEnd;
        this.startTick = Bukkit.getCurrentTick();

        buff.onApply(entity);

        try {
            registerTasks(plugin);
        } catch (Throwable t) {
            // 타이머 없이 남으면 효과가 영구 잔류한다 (예: BUFF 컨테이너 수치 영구 증가)
            ended.set(true);
            cancelTasks();
            safeOnRemove(BuffRemoveReason.FORCED);
            throw t;
        }
    }

    private void registerTasks(Plugin plugin) {
        int period = period();
        Runnable run = this::run;
        this.tickTask = async
                ? Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, run, period, period)
                : Bukkit.getScheduler().runTaskTimer(plugin, run, period, period);

        // 1. 시간 종료 (durationTicks < 0 이면 무한 지속이라 등록하지 않는다)
        if (durationTicks >= 0) {
            long delay = Math.max(1L, durationTicks);   // 0틱 지속은 다음 틱에 종료
            this.expireTask = Bukkit.getScheduler()
                    .runTaskLater(plugin, () -> end(BuffRemoveReason.EXPIRED), delay);
        }
    }

    private void run() {
        if (ended.get()) return;

        // --- 종료 조건 자체 감지 (이벤트를 연결하지 않아도 안전망으로 동작) ---
        // todo async=true 로 적용하면 아래 상태 조회와 buff.tick() 이 비동기 스레드에서 돈다.
        //      Bukkit 상태 조회/변경은 스레드 안전이 보장되지 않으므로,
        //      엔티티 상태를 만지는 버프(체력 회복/도트 등)는 async=false 로만 적용해야 한다. (규약 확정 필요)
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
            warn("tick 오류", t);
        }
    }

    private boolean isOffline() {
        return entity instanceof A_Player p && !p.isOnline();
    }

    private int period() {
        return Math.max(1, buff.tickCount());
    }

    /**
     * 종료 사유를 감지한 지점에서 호출.
     * 태스크를 먼저 끊어 종료 처리 대기 중에 tick 이 더 돌지 않게 하고(도트 추가 피해 방지),
     * 실제 종료는 메인스레드에서 처리한다.
     */
    private void finish(BuffRemoveReason reason) {
        cancelTasks();
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
        if (!ended.compareAndSet(false, true)) return;
        cancelTasks();
        onEnd.accept(this, reason);
    }

    private void cancelTasks() {
        BukkitTask tick = this.tickTask;
        if (tick != null) tick.cancel();

        BukkitTask expire = this.expireTask;
        if (expire != null) expire.cancel();
    }

    /** start() 롤백 전용. onRemove 예외 때문에 원복 흐름이 끊기지 않도록 감싼다. */
    private void safeOnRemove(BuffRemoveReason reason) {
        try {
            buff.onRemove(entity, reason);
        } catch (Throwable t) {
            warn("start 롤백 onRemove 오류", t);
        }
    }

    private void warn(String what, Throwable t) {
        Plugin p = this.plugin;
        if (p == null) return;
        p.getLogger().warning("[Buff] " + what + " (" + buff.type() + ", " + entity.getUniqueId() + "): " + t);
    }

    public A_Entity getEntity()     { return entity; }
    public Buff getBuff()           { return buff; }
    public BuffType getType()       { return buff.type(); }
    public String getName()         { return buff.getName(); }
    public boolean isAsync()        { return async; }
    public boolean isEnded()        { return ended.get(); }
    public UUID getEntityId()       { return entity.getUniqueId(); }

    /**
     * 종료 안내를 보내지 않도록 표시한다. (BuffManager 가 end 직전에 호출)
     * 이미 끝난 뒤에 불려도 안내가 한 번 더 나가지는 않는다 — end 가 CAS 로 한 번만 통과하기 때문이다.
     */
    void suppressEndNotice()             { endNoticeSuppressed = true; }
    public boolean isEndNoticeSuppressed() { return endNoticeSuppressed; }

    /**
     * 남은 지속시간(틱). 무한 지속이면 durationTicks(음수)를 그대로 돌려준다.
     * tick 주기와 무관하게 적용 시점 기준으로 계산하므로 로그아웃 스냅샷에 그대로 쓸 수 있다.
     */
    public long getRemainingTicks() {
        if (durationTicks < 0 || startTick < 0) return durationTicks;
        long elapsed = Bukkit.getCurrentTick() - startTick;
        return Math.max(0L, durationTicks - elapsed);
    }
}
