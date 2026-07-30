package org.red.minecraft.uw.core.skill;

import org.bukkit.event.EventHandler;
import org.red.minecraft.dellarte.library.event.listener.A_Listener;
import org.red.minecraft.uw.core.combat.buff.BuffType;
import org.red.minecraft.uw.core.event.UWBuffApplyEvent;

/**
 * 침묵(SILENCE) 버프가 부여되면 진행 중인 캐스팅을 취소한다. (§2.6 스킬 코어 2)
 *
 * <p>버프 도메인이 캐스팅 매니저를 직접 호출하면 {@code combat.buff} → {@code skill} 의존이 생겨
 * 패키지 순환참조가 된다(CLAUDE.md 금지). 그래서 버프 쪽은 생명주기 이벤트만 발행하고,
 * 구독은 이쪽(skill)에서 한다 — 의존 방향이 {@code skill} → {@code combat.buff} 단방향으로 유지된다.
 *
 * <p>{@link CastingManager#onSilenced} 는 내부에서 캐스팅 중인지 먼저 확인하므로
 * 캐스팅과 무관한 침묵 부여에는 아무 일도 하지 않는다.
 */
public class SilenceCastListener extends A_Listener {

    @EventHandler
    public void onBuffApply(UWBuffApplyEvent event) {
        if (event.buffType() != BuffType.SILENCE) return;

        CastingManager.onSilenced(event.target());
    }
}
