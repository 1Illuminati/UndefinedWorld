package org.red.minecraft.uw.core.combat.buff;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.combat.CombatManager;
import org.red.minecraft.uw.core.combat.ElementalType;
import org.red.minecraft.uw.core.combat.damage.DamageType;

/**
 * 화상(화염속성) 디버프 — 주기 도트 데미지.
 * 명세(ElementalType.FIRE): 데미지 = 대상 체력 * 0.5% * (화염 공격력/방어력 추가 연산)
 * 공/방 연산은 BURNING 데미지에 ElementalType.FIRE를 실어 데미지 파이프라인(T9 속성 Modifier)에서 처리한다.
 * BURNING 타입은 화상 재부여를 발생시키지 않는다 (ElementalPostProcessor의 BURNING 가드 참조).
 */
public class BurnDebuff implements Buff {

    /** 명세 고정값: 대상 최대체력의 0.5% */
    private static final double HEALTH_RATE = 0.005;

    private final BuffContext ctx;

    public BurnDebuff(BuffContext ctx) { this.ctx = ctx; }

    @Override public BuffContext context() { return ctx; }
    @Override public BuffType type()       { return BuffType.BURN; }
    @Override public int tickCount()       { return 20; } // todo 도트 주기 밸런스 확정 필요 (임시 1초)

    @Override
    public void tick(A_Entity entity) {
        if (!(entity instanceof A_LivingEntity living) || living.isDead()) return;

        double damage = living.getMaxHealth() * HEALTH_RATE;

        if (ctx.caster() != null) {
            CombatManager.damage(ctx.caster(), living, DamageType.BURNING, ElementalType.FIRE, damage);
        } else {
            CombatManager.damage(living, DamageType.BURNING, ElementalType.FIRE, damage);
        }
    }
}
