package org.red.minecraft.uw.core.event;

import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.uw.core.combat.buff.BuffData;
import org.red.minecraft.uw.core.combat.buff.BuffType;

/**
 * 버프가 대상에게 <b>실제로 적용된 뒤</b> 발행되는 통지 이벤트.
 *
 * BuffManager.applyBuff 가 적용에 성공한 경우에만 발행한다.
 * (적용 실패/롤백 경로에서는 발행되지 않는다)
 * 재적용(같은 버프를 다시 걸기)에서도 발행되므로, 구독자는 "이미 걸려 있는 상태"를 스스로 판정해야 한다.
 *
 * 이미 일어난 일의 통지라 <b>취소할 수 없다</b> (Cancellable 아님).
 *
 * 이 이벤트가 있는 이유: 버프가 걸리는 시점을 아는 것은 버프 도메인뿐인데,
 * 버프 도메인이 다른 도메인(스킬 등)을 직접 호출하면 패키지 순환 의존이 생긴다.
 * 통지만 발행하고 처리는 구독자에게 맡겨 의존을 단방향으로 유지한다.
 */
public class UWBuffApplyEvent extends EntityEvent implements UWEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final BuffData buffData;

    public UWBuffApplyEvent(@NotNull A_Entity target, @NotNull BuffData buffData) {
        super(target.getEntity());
        this.buffData = buffData;
    }

    /** 버프가 걸린 대상 */
    public A_Entity target() {
        return CommediaDellarte.getAEntity(this.getEntity());
    }

    public BuffData buffData() {
        return this.buffData;
    }

    public BuffType buffType() {
        return this.buffData.getType();
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
        return "Buff applied: " + buffData.getName() + " (level " + buffData.getBuff().context().level()
                + ") -> " + this.getEntity().getUniqueId();
    }
}
