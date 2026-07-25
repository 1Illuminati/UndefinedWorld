package org.red.minecraft.uw.core.skill.controller;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;

/**
 * 스킬 내부에서 틱 단위로 동작하는 모든 개념의 공통 부모. (구조 결정 2.5 T18)
 * ex) 발사체(ProjectileController), 소환수, 장판 ...
 * 버프/디버프는 해당되지 않는다 (BuffData가 자체 타이머를 관리).
 *
 * 종료는 두 가지로 구분한다:
 *   - expire(): 수명 종료 — 정지 후 onExpire 콜백 실행 (사거리 도달, 지속시간 만료 등)
 *   - stop():   강제 종료 — 콜백 없이 정지 (외부 취소)
 */
public abstract class Controller {

    @Nullable
    private final Runnable onExpire;

    @Nullable
    private BukkitTask task;

    public Controller(@Nullable Runnable expire) {
        this.onExpire = expire;
    }

    /** 틱 동작 시작. 이미 실행 중이면 무시한다. */
    public void start() {
        if (task != null) return;
        task = Bukkit.getScheduler().runTaskTimer(UndefinedWorldCorePlugin.instance, this::tick, 0L, 1L);
    }

    /** 강제 종료. onExpire 콜백은 호출하지 않는다. */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /** 수명 종료. 정지 후 onExpire 콜백을 호출한다. 구현체의 tick()에서 수명이 다했을 때 부른다. */
    protected void expire() {
        stop();
        if (onExpire != null) onExpire.run();
    }

    public boolean isRunning() {
        return task != null;
    }

    /** 매 틱 실행되는 동작 */
    protected abstract void tick();
}
