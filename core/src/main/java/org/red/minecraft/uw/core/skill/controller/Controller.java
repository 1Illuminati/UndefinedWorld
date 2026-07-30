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

    /** onExpire 중복 실행 방지 플래그. 수명 종료는 컨트롤러 생애에 단 한 번만 처리된다. */
    private boolean expired = false;

    public Controller(@Nullable Runnable expire) {
        this.onExpire = expire;
    }

    /**
     * 틱 동작 시작. 이미 실행 중이면 무시한다.
     * 수명이 끝난 컨트롤러는 다시 시작하지 않는다 — onExpire가 이미 1회 소비됐으므로
     * 재시작하면 종료 콜백 없이 도는 태스크가 된다.
     */
    public void start() {
        if (expired) {
            UndefinedWorldCorePlugin.sendLog("Controller 재시작 무시 — 이미 수명 종료됨: " + getClass().getSimpleName());
            return;
        }
        if (task != null) return;
        task = Bukkit.getScheduler().runTaskTimer(UndefinedWorldCorePlugin.instance, this::runTick, 0L, 1L);
    }

    /** 강제 종료. onExpire 콜백은 호출하지 않는다. */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * 수명 종료. 정지 후 onExpire 콜백을 호출한다. 구현체의 tick()에서 수명이 다했을 때 부른다.
     * 중복 호출은 무시된다 (onExpire를 완료 집계에 쓰는 이펙트가 이중 집계되지 않도록).
     */
    protected void expire() {
        // 정지는 중복 호출이어도 무조건 수행한다.
        // (expired 검사를 먼저 하면, 이미 만료된 컨트롤러가 어떤 경로로든 다시 돌고 있을 때
        //  expire()가 조용히 반환만 하고 태스크를 멈추지 않아 매 틱 도는 태스크가 영구히 남는다)
        stop();

        if (expired) return;
        expired = true;

        if (onExpire != null) onExpire.run();
    }

    /**
     * 틱 실행 래퍼.
     * tick()이 예외를 던지면 Bukkit 스케줄러는 태스크를 유지하므로 매 틱 예외가 반복되고,
     * onExpire를 기다리는 이펙트의 CompletableFuture가 영구 미완료로 남는다.
     * 따라서 예외를 잡아 위치를 로그로 남기고 수명 종료로 정리한다.
     */
    private void runTick() {
        try {
            tick();
        } catch (Throwable throwable) {
            UndefinedWorldCorePlugin.sendLog("Controller tick 오류 — 수명 종료 처리: "
                    + getClass().getSimpleName() + " / " + throwable);
            expire();
        }
    }

    public boolean isRunning() {
        return task != null;
    }

    /** 매 틱 실행되는 동작 */
    protected abstract void tick();
}
