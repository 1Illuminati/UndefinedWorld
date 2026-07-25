package org.red.minecraft.uw.core.skill;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.combat.buff.BuffType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 캐스팅 상태 관리. (확정 규칙, Process.md 2.5 참조)
 *
 * 취소 규칙:
 *   1. 공격받을 시 취소 — 무적(INVINCIBLE) 버프 상태면 예외.
 *      디버프 데미지(독/화상)는 취소시키지 않음 (DamageProcess의 타입 필터에서 처리)
 *   2. 다른 스킬 사용 시 기존 캐스팅 취소 후 새 스킬 작동 (SkillEngine.runSkill에서 처리)
 * 제약:
 *   - 캐스팅 중 이동 불가 (CastingMoveListener가 MoveEvent 취소)
 *
 * 메인스레드 사용 전제.
 */
public final class CastingManager {

    /** 캐스팅 중인 엔티티: UUID -> 완료 예약 태스크 */
    private static final Map<UUID, BukkitTask> casting = new HashMap<>();

    private CastingManager() {}

    public static boolean isCasting(A_Entity entity) {
        return isCasting(entity.getUniqueId());
    }

    public static boolean isCasting(UUID id) {
        return casting.containsKey(id);
    }

    /**
     * 캐스팅 시작. 이미 캐스팅 중이면 기존 캐스팅을 취소하고 새로 시작한다.
     * @param caster     시전자
     * @param ticks      캐스팅 시간 (틱)
     * @param onComplete 캐스팅 완료 시 실행 (비용 지불/쿨타임 적용/스킬 실행)
     */
    public static void startCast(A_Entity caster, int ticks, Runnable onComplete) {
        cancelCast(caster);

        UUID id = caster.getUniqueId();
        BukkitTask task = Bukkit.getScheduler().runTaskLater(UndefinedWorldCorePlugin.instance, () -> {
            casting.remove(id);
            onComplete.run();
        }, ticks);

        casting.put(id, task);
    }

    /** 캐스팅 취소. 캐스팅 중이 아니면 무시한다. */
    public static void cancelCast(A_Entity caster) {
        BukkitTask task = casting.remove(caster.getUniqueId());
        if (task != null) task.cancel();
    }

    /**
     * 피격 시 취소 훅 (규칙 1). DamageProcess에서 엔티티 공격 데미지(독/화상 제외)에만 호출된다.
     * 무적 버프 상태면 캐스팅을 유지한다.
     */
    public static void onAttacked(A_LivingEntity defender) {
        if (!isCasting(defender)) return;
        if (UndefinedWorldCore.getBuffManager().hasBuff(defender, BuffType.INVINCIBLE)) return;

        cancelCast(defender);
        if (defender instanceof A_Player) defender.sendMessage("피격으로 캐스팅이 취소되었습니다."); //todo 문구/형식 사용자 확정 필요
    }
}
