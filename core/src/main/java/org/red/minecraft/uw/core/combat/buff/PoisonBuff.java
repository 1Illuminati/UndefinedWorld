package org.red.minecraft.uw.core.combat.buff;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;

public class PoisonBuff implements Buff {

    private final BuffContext ctx;

    public PoisonBuff(BuffContext ctx) { this.ctx = ctx; }

    @Override public BuffContext context() { return ctx; }
    @Override public BuffType type()       { return BuffType.POISON; }
    @Override public int tickCount()       { return 20; }

    @Override
    public void tick(A_Entity entity) {
        if (!(entity instanceof A_LivingEntity living) || living.isDead()) return;
        double dmg = ctx.level();
        // 시전자가 살아있으면 데미지 소스로 지정 (킬 크레딧/처치 로그 반영)
        if (ctx.caster() instanceof A_LivingEntity src && src.isValid()) {
            living.damage(dmg, src.getEntity());
        } else {
            living.damage(dmg);
        }
    }
}
