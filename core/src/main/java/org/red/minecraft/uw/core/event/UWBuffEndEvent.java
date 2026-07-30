package org.red.minecraft.uw.core.event;

import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.combat.buff.BuffData;
import org.red.minecraft.uw.core.combat.buff.BuffRemoveReason;
import org.red.minecraft.uw.core.combat.buff.BuffType;

/**
 * 버프가 <b>종료된 뒤</b> 발행되는 통지 이벤트. 종료 사유 4가지(시간종료/사망/로그아웃/강제제거) 모두에서 발행된다.
 *
 * 발행 시점은 활성 맵 정리 + Buff.onRemove + BUFF attribute 정리가 <b>모두 끝난 뒤</b>다.
 * 구독자가 이벤트 안에서 상태를 조회하면 이미 종료가 반영된 값을 본다.
 *
 * 이미 일어난 일의 통지라 <b>취소할 수 없다</b> (Cancellable 아님).
 */
public class UWBuffEndEvent extends EntityEvent implements UWEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final BuffData buffData;
    private final BuffRemoveReason reason;

    public UWBuffEndEvent(@NotNull A_Entity target, @NotNull BuffData buffData, @NotNull BuffRemoveReason reason) {
        super(target.getEntity());
        this.buffData = buffData;
        this.reason = reason;
    }

    /** 버프가 걸려 있던 대상 */
    public A_Entity target() {
        return CommediaDellarte.getAEntity(this.getEntity());
    }

    public BuffData buffData() {
        return this.buffData;
    }

    public BuffType buffType() {
        return this.buffData.getType();
    }

    /** 종료 사유 */
    public BuffRemoveReason reason() {
        return this.reason;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public String getLoggerMessage() {
        return "Buff ended: " + buffData.getName() + " (" + reason + ") -> " + this.getEntity().getUniqueId();
    }
}
