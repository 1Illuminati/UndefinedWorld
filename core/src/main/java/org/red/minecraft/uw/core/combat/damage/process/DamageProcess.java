package org.red.minecraft.uw.core.combat.damage.process;

import org.bukkit.Bukkit;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.combat.CombatManager;
import org.red.minecraft.uw.core.combat.ElementalType;
import org.red.minecraft.uw.core.combat.buff.BuffType;
import org.red.minecraft.uw.core.combat.damage.DamageCTX;
import org.red.minecraft.uw.core.combat.damage.DamageType;
import org.red.minecraft.uw.core.combat.damage.modify.DamageModifierBus;
import org.red.minecraft.uw.core.event.UWDamageEvent;
import org.red.minecraft.uw.core.skill.CastingManager;

import java.util.EnumSet;
import java.util.Set;

public class DamageProcess {

    // 회피/막기/무적 안내 문구 — todo 문구/형식 사용자 확정 필요
    private static final String DODGE_DEFENDER_MESSAGE = "공격을 회피했습니다.";
    private static final String DODGE_ATTACKER_MESSAGE = "상대가 공격을 회피했습니다.";
    private static final String BLOCK_DEFENDER_MESSAGE = "공격을 막아냈습니다.";
    private static final String BLOCK_ATTACKER_MESSAGE = "상대가 공격을 막아냈습니다.";

    // 무적 안내는 회피/막기 관례를 그대로 따른다 (양쪽에 전송).
    // todo 빈도 확정 필요 — 회피/막기는 확률이라 가끔 뜨지만 무적은 <b>모든 피격마다 100%</b> 뜬다.
    //      무적 상태로 여러 대상에게 두들겨 맞으면 양쪽 채팅이 빠르게 밀린다.
    //      "매번 / 무적 1회당 1번 / 아예 안 보냄" 중 무엇인지 사용자 확정 필요.
    private static final String INVINCIBLE_DEFENDER_MESSAGE = "무적 상태라 피해를 받지 않았습니다.";
    private static final String INVINCIBLE_ATTACKER_MESSAGE = "상대가 무적 상태입니다.";

    /**
     * 무적(INVINCIBLE) 이 <b>무효화하지 않는</b> 데미지 타입. (사용자 확정)
     *
     * <p>독/화상 도트를 제외하는 근거는 캐스팅 취소 규칙(Process.md §2.5 캐스팅 규칙 1)과 같다.
     * 그 규칙은 "오로지 상대 엔티티의 공격에 의한 데미지"만 캐스팅을 끊게 하고 도트를 예외로 뒀다.
     * 무적을 "상대의 공격을 무효화하는 상태"로 같은 선에 두면 두 규칙이 하나로 유지된다.
     *
     * <p>COST 를 제외하는 근거는 악용 차단이다. COST 는 체력 비용 지불에 쓰는 타입이라
     * 무적이 이것까지 막으면 무적 도중 체력 비용 스킬을 <b>무한 무료 시전</b>할 수 있다.
     *
     * <p><b>버그로 오해하지 말 것</b> — {@code BURNING} 은 {@code canDeath=true} 이므로
     * <b>무적 상태에서도 화상 도트로 죽을 수 있다.</b> 사용자가 이 동작을 선택했다.
     * ({@code POISON} 은 {@code canDeath=false} 라 체력 1 미만으로는 떨어지지 않는다)
     */
    private static final Set<DamageType> INVINCIBLE_IGNORED_TYPES =
            EnumSet.of(DamageType.POISON, DamageType.BURNING, DamageType.COST);

    private final DamageCTX originCTX;
    public DamageProcess(DamageCTX ctx) {
        this.originCTX = ctx;
    }

    public DamageProcess(A_LivingEntity defender, DamageType damageType, ElementalType elementalType, double originDamage, double scale, boolean isCritical) {
        org.red.minecraft.uw.core.combat.damage.DamageSource source = new org.red.minecraft.uw.core.combat.damage.DamageSource(defender);
        this(new DamageCTX(source, damageType, elementalType, originDamage, scale, isCritical));
    }

    public DamageProcess(org.red.minecraft.uw.core.combat.damage.DamageSource source, DamageType damageType, ElementalType elementalType, double originDamage, double scale, boolean isCritical) {
        this(new DamageCTX(source, damageType, elementalType, originDamage, scale, isCritical));
    }

    protected DamageCTX getOriginCTX() {
        return originCTX;
    }

    public void run() {
        // 이미 죽은 대상에 데미지를 넣으면 setHealth(0) 이 사망 처리를 다시 태워
        // 드랍/경험치가 중복될 수 있다. (도트 틱과 공격이 같은 틱에 겹치거나, 연쇄 대상 스냅샷이 그 사이 죽는 경우)
        // VamfirePostProcessor / 연쇄 대상 필터와 동일한 가드를 진입점에도 둔다.
        if (this.originCTX.defender().isDead()) {
            UndefinedWorldCorePlugin.sendLog("이미 사망한 대상 → 데미지 무시: " + this.originCTX);
            return;
        }

        // 무적 판정 (Process.md §2.10 확정) — 회피/막기보다 <b>먼저</b> 본다.
        //   1. 무적은 확률이 아니라 확정 무효다. 뒤에 두면 "회피 성공" 안내가 나간 뒤 무적이 또 막는
        //      이중 판정이 되어 어느 쪽이 막았는지 알 수 없다.
        //   2. 회피/막기는 hasAttacker 한정이라 공격자 없는 데미지에는 걸리지 않는다.
        //      무적을 뒤에 두면 그 경로에서만 순서가 달라진다.
        //   3. 무효 여부가 데미지 값과 무관하므로 Modifier 계산 자체가 불필요하다.
        //      (DamageModifierBus 는 복제본만 건드리는 순수 계산이라 건너뛰어도 잃는 상태가 없다)
        if (isInvincibleBlocked(this.originCTX)) {
            UndefinedWorldCorePlugin.sendLog("Invincible! " + this.originCTX);
            onAvoided(this.originCTX, INVINCIBLE_DEFENDER_MESSAGE, INVINCIBLE_ATTACKER_MESSAGE);
            return;
        }

        DamageCTX resultCTX = DamageModifierBus.create(this.originCTX.copy()).flush();

        // 회피 판정 — 공격자가 있는 데미지만 대상 (todo 독/화상 등 타입별 회피 제외 여부 확정 필요)
        if (resultCTX.hasAttacker() && CombatManager.randomDodgeCheck(resultCTX.defender(), resultCTX.finalDamage())) {
            UndefinedWorldCorePlugin.sendLog("Dodge! " + resultCTX);
            onAvoided(resultCTX, DODGE_DEFENDER_MESSAGE, DODGE_ATTACKER_MESSAGE);
            return;
        }

        // 막기 판정 — 성공 시 데미지 완전 무효 (todo 회피/막기 판정 순서 확정 필요, 현재 회피 → 막기)
        if (resultCTX.hasAttacker() && CombatManager.randomBlockCheck(resultCTX.defender())) {
            UndefinedWorldCorePlugin.sendLog("Block! " + resultCTX);
            onAvoided(resultCTX, BLOCK_DEFENDER_MESSAGE, BLOCK_ATTACKER_MESSAGE);
            return;
        }

        EntityDamageEvent event = this.createEvent(resultCTX);
        setEvent(resultCTX, event);

        // 데미지 확정 이후 부수효과 (속성 디버프, 흡혈, 캐스팅 취소) — 이벤트 취소 시 미발동
        if (!event.isCancelled()) {
            ElementalPostProcessor.process(resultCTX, event);
            VamfirePostProcessor.process(resultCTX, event);
            cancelCastingIfAttacked(resultCTX);
        }

        UndefinedWorldCorePlugin.sendLog(resultCTX.toString());
    }

    /**
     * 무적(INVINCIBLE) 으로 이 데미지가 무효화되는지 판정한다.
     *
     * <p>판정을 데미지 도메인에 두는 이유: 이미 {@code combat.damage → combat.buff} 방향으로만
     * 의존이 흐른다(DamageModifierBus 가 SHOCK/SHATTER 를 읽는 것과 같은 방향).
     * 반대로 InvincibleBuff 안에 두면 버프가 DamageType 을 알아야 해서 두 패키지가 양방향이 된다.
     *
     * <p>무효 대상에서 빠지는 타입은 {@link #INVINCIBLE_IGNORED_TYPES} 한 곳에서만 정한다.
     */
    private boolean isInvincibleBlocked(DamageCTX ctx) {
        if (INVINCIBLE_IGNORED_TYPES.contains(ctx.type())) return false;
        return UndefinedWorldCore.getBuffManager().hasBuff(ctx.defender(), BuffType.INVINCIBLE);
    }

    /**
     * 회피/막기/무적으로 피해가 무효화됐을 때의 공통 처리 (사용자 확정).
     *
     * 피해는 0이지만 "공격을 받은 것"은 사실이므로 캐스팅은 취소한다.
     * (무적 경로에서는 CastingManager.onAttacked 가 INVINCIBLE 을 먼저 확인해 스스로 무동작이 되므로,
     *  여기서 같은 호출을 해도 무적 시전자의 캐스팅은 유지된다 — 규칙이 한 곳에만 있다)
     * 속성 디버프/흡혈은 피해가 없으므로 발동하지 않고, EntityDamageEvent 도 발행하지 않는다(현행 유지).
     *
     * 이 메서드를 부른 뒤에는 항상 run() 이 곧바로 return 하므로
     * 아래 정상 경로의 cancelCastingIfAttacked 와 이중 호출되지 않는다.
     * (설령 겹쳐도 CastingManager.onAttacked 는 isCasting 검사가 먼저라 두 번째는 무동작이다)
     */
    private void onAvoided(DamageCTX ctx, String defenderMessage, String attackerMessage) {
        sendAvoidMessage(ctx.defender(), defenderMessage);

        // 자기 자신을 공격한 경우(자해 스킬 등) 같은 플레이어에게 두 줄이 가므로 공격자 안내는 생략한다
        A_Entity attacker = ctx.attacker();
        if (attacker != null && !attacker.getUniqueId().equals(ctx.defender().getUniqueId()))
            sendAvoidMessage(attacker, attackerMessage);

        cancelCastingIfAttacked(ctx);
    }

    /** 안내는 플레이어 채팅창에만 보낸다 (CastingManager.cancel / SkillEngine 과 동일한 규약) */
    private void sendAvoidMessage(@Nullable A_Entity target, String message) {
        if (target instanceof A_Player player) player.sendMessage(message);
    }

    /**
     * 캐스팅 취소 규칙 1 (구조 결정 2.5): 상대 엔티티의 공격에 의한 데미지만 캐스팅을 취소한다.
     * 디버프 데미지(독/화상)는 caster 가 남아 있어 hasAttacker 가 true 여도 제외한다.
     * 무적 버프(INVINCIBLE) 예외는 CastingManager.onAttacked 내부가 담당한다.
     *
     * 회피/막기로 피해가 0이 된 경우에도 동일한 조건으로 취소한다 (사용자 확정).
     */
    private void cancelCastingIfAttacked(DamageCTX ctx) {
        if (!ctx.hasAttacker()) return;
        if (ctx.type() == DamageType.POISON || ctx.type() == DamageType.BURNING) return;

        CastingManager.onAttacked(ctx.defender());
    }

    /**
     * 이벤트 발행 → 실제 체력 차감.
     * ctx 를 함께 받는 이유는 발행 이후 다른 리스너가 바꿔놓은 데미지에도
     * DamageType 규칙(canDeath)을 다시 강제해야 하기 때문이다.
     */
    protected void setEvent(DamageCTX ctx, EntityDamageEvent event) {
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        // 이벤트 발행 이후의 데미지는 다른 리스너가 바꿔놓았을 수 있다.
        // finalDamage() 가 걸러준 비정상값이 다시 들어올 수 있으므로 적용 직전에 한 번 더 검사한다.
        double eventDamage = event.getDamage();
        if (!Double.isFinite(eventDamage)) {
            // 취소로 표시해야 run() 의 후처리(속성 디버프/흡혈/캐스팅 취소)도 함께 건너뛴다.
            // 그냥 return 하면 "데미지는 안 들어갔는데 부수효과만 발동"하는 상태가 된다.
            event.setCancelled(true);
            UndefinedWorldCorePlugin.sendLog("setEvent 비정상 데미지(NaN/Infinity) 차단 → 미적용: " + eventDamage);
            return;
        }

        LivingEntity livingEntity = (LivingEntity) event.getEntity();

        // canDeath=false 규칙(독 등)은 finalDamage() 에서 한 번 적용되지만,
        // 이벤트 발행 이후 다른 리스너가 데미지를 올리면 그대로 우회된다.
        // 실제로 체력을 깎는 여기서 다시 강제해야 "이 타입으로는 죽지 않는다"가 보장된다.
        if (!ctx.type().canDeath) {
            double survivable = Math.max(livingEntity.getHealth() - 1, 0);
            if (eventDamage > survivable) {
                UndefinedWorldCorePlugin.sendLog(String.format(
                        "canDeath=false 타입(%s) 데미지 상한 적용: %f → %f", ctx.type(), eventDamage, survivable));
                eventDamage = survivable;
            }
        }

        livingEntity.setLastDamage(eventDamage);
        livingEntity.setLastDamageCause(event);
        CombatManager.applyHitEffect(livingEntity, event.getDamageSource().getSourceLocation());

        // setHealth 는 [0, maxHealth] 를 벗어나면 IllegalArgumentException 을 던져 후처리까지 통째로 중단시킨다.
        // (리스너가 데미지를 음수로 바꾸면 회복이 되어 상한을 넘을 수 있다)
        double newHealth = Math.min(Math.max(livingEntity.getHealth() - eventDamage, 0), livingEntity.getMaxHealth());
        livingEntity.setHealth(newHealth);
    }

    protected EntityDamageEvent createEvent(DamageCTX ctx) {
        DamageSource source = ctx.source().getMinecraftDamageSource();
        return new UWDamageEvent(ctx.defender(), source, ctx.finalDamage());
    }
}
