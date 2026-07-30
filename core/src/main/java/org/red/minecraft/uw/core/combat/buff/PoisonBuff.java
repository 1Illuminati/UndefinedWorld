package org.red.minecraft.uw.core.combat.buff;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.combat.CombatManager;
import org.red.minecraft.uw.core.combat.damage.DamageType;

public class PoisonBuff implements Buff {

    private final BuffContext ctx;

    public PoisonBuff(BuffContext ctx) { this.ctx = ctx; }

    @Override public BuffContext context() { return ctx; }
    @Override public BuffType type()       { return BuffType.POISON; }
    @Override public String getName()      { return type().name(); }
    @Override public int tickCount()       { return 20; }

    @Override
    public void tick(A_Entity entity) {
        if (!(entity instanceof A_LivingEntity living) || living.isDead()) return;
        double dmg = ctx.level();

        // 바닐라 damage() 대신 UW 파이프라인의 POISON 타입 사용
        // (canDeath=false 반영 + 캐스팅 취소 규칙의 "디버프 데미지 제외" 판별을 타입으로 가능하게)
        if (ctx.caster() instanceof A_LivingEntity src && src.isValid()) {
            CombatManager.damage(src, living, DamageType.POISON, dmg);
        } else {
            CombatManager.damage(living, DamageType.POISON, dmg);
        }
    }
}
