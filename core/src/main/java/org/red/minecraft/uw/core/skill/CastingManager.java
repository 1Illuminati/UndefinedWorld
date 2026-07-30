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
 * 캐스팅 상태 관리. (확정 규칙, Process.md 2.5 / 2.6 "스킬 코어" 참조)
 *
 * 취소 규칙:
 *   1. 공격받을 시 취소 — 무적(INVINCIBLE) 버프 상태면 예외.
 *      디버프 데미지(독/화상)는 취소시키지 않음 (DamageProcess의 타입 필터에서 처리)
 *   2. 다른 스킬 사용 시 기존 캐스팅 취소 후 새 스킬 작동 (SkillEngine.runSkill에서 처리)
 *   3. 침묵(SILENCE) 부여 시 취소 ({@link #onSilenced})
 *   4. 탈것 탑승 시 취소 ({@link #onMounted}) — <b>플레이어 한정</b> ({@link #isVehicleRuleTarget})
 *   5. 텔레포트 시 취소 ({@link #onTeleported}) — 텔레포트 자체는 막지 않는다
 * 제약:
 *   - 캐스팅 중 이동 불가 (CastingMoveListener가 MoveEvent 취소)
 *
 * 메인스레드 사용 전제.
 *
 * 퇴장/사망 시 즉시 정리는 CastingLifecycleListener 가 담당한다
 * (정리하지 않으면 남은 캐스팅 시간 동안 엔트리가 남아 재접속 후에도 이동이 막힌다).
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

            // 캐스팅 중 로그아웃/사망/엔티티 제거된 시전자로 스킬이 발동되는 것을 막는다.
            // (막지 않으면 오프라인 플레이어에게 비용/쿨타임이 적용되고 스킬 효과까지 발동된다)
            if (!isCasterAvailable(caster)) {
                UndefinedWorldCorePlugin.sendLog("Casting aborted (caster unavailable): " + caster.getUniqueIdStr());
                // 사망(접속 상태)이면 안내가 닿으므로 사유를 남기고, 추적 정보는 정리한다
                SkillDebugManager.onChainAborted(caster, "캐스팅 완료 시점 시전자 상태 불가(사망/퇴장)");
                SkillDebugManager.onCastEnd(id);
                return;
            }

            SkillDebugManager.onCastComplete(caster);
            onComplete.run();
        }, ticks);

        casting.put(id, task);
        SkillDebugManager.onCastStart(caster, ticks);
    }

    /** 캐스팅 취소. 캐스팅 중이 아니면 무시한다. */
    public static void cancelCast(A_Entity caster) {
        cancelCast(caster.getUniqueId());
    }

    /**
     * 캐스팅 취소 (UUID 기준).
     * 퇴장/사망처럼 A_Entity 를 새로 감싸기 어렵거나 이미 무효해진 대상을 정리할 때 사용한다.
     */
    public static void cancelCast(UUID id) {
        BukkitTask task = casting.remove(id);
        if (task != null) task.cancel();

        // 모든 취소 경로가 지나는 지점이므로 디버그 추적 정리도 여기 한 곳에서만 한다.
        // (취소 사유 출력은 cancel(reason, message)가 이 호출 전에 이미 끝낸다)
        SkillDebugManager.onCastEnd(id);
    }

    /**
     * 시전자가 아직 스킬 처리를 진행해도 되는 상태인지 판정.
     * (캐스팅 완료 시점과 노드 체인 진행 중간 — SkillEngine — 양쪽에서 같은 기준을 쓴다)
     */
    public static boolean isCasterAvailable(A_Entity caster) {
        if (caster instanceof A_Player player) return player.isOnline() && !player.isDead();
        return caster.isValid() && !caster.isDead();
    }

    /**
     * 피격 시 취소 훅 (규칙 1). DamageProcess에서 엔티티 공격 데미지(독/화상 제외)에만 호출된다.
     * 무적 버프 상태면 캐스팅을 유지한다.
     */
    public static void onAttacked(A_LivingEntity defender) {
        if (!isCasting(defender)) return;
        if (UndefinedWorldCore.getBuffManager().hasBuff(defender, BuffType.INVINCIBLE)) return;

        cancel(defender, "attacked", "피격으로 캐스팅이 취소되었습니다."); //todo 문구/형식 사용자 확정 필요
    }

    /**
     * 침묵 부여 시 취소 훅 (규칙 3).
     * <p>버프 부여 시점을 아는 것은 버프 도메인(combat/buff)이므로,
     * SILENCE 버프가 실제로 적용된 직후 이 메서드를 호출해야 한다.
     * SkillEngine은 캐스팅 완료 직전에 침묵 상태를 한 번 더 확인해 방어한다.
     */
    public static void onSilenced(A_Entity caster) {
        if (!isCasting(caster)) return;

        cancel(caster, "silenced", "침묵으로 캐스팅이 취소되었습니다."); //todo 문구/형식 사용자 확정 필요
    }

    /**
     * 탈것 규칙(탑승 중 스킬 사용 금지 / 탑승 시 캐스팅 취소)의 <b>적용 대상</b> 판정.
     * <p><b>플레이어 한정이다</b> (사용자 확정 2026-07-30). 이 규칙의 근거는
     * "탑승 중에는 PlayerMoveEvent가 발생하지 않아 캐스팅 이동 차단이 우회된다"는 것인데,
     * CastingMoveListener는 PlayerMoveEvent만 보므로 그 우회는 플레이어에게만 존재한다.
     * 몹까지 막으면 스파이더 자키처럼 탑승 상태로 스폰되는 몹이 스킬을 영영 쓰지 못한다.
     * <p>탈것 규칙의 대상 판정은 갈라지지 않게 여기 한 곳에서만 한다
     * (SkillEngine의 사용 차단, 아래 onMounted의 캐스팅 취소가 모두 이 판정을 쓴다).
     */
    public static boolean isVehicleRuleTarget(A_Entity caster) {
        return caster instanceof A_Player;
    }

    /** 탈것 탑승 시 취소 훅 (규칙 4). CastingMoveListener가 호출한다. */
    public static void onMounted(A_Entity caster) {
        if (!isVehicleRuleTarget(caster)) return;
        if (!isCasting(caster)) return;

        cancel(caster, "mounted", "탈것에 탑승해 캐스팅이 취소되었습니다."); //todo 문구/형식 사용자 확정 필요
    }

    /** 텔레포트 시 취소 훅 (규칙 5). 텔레포트 자체는 통과시키고 캐스팅만 끊는다. */
    public static void onTeleported(A_Entity caster) {
        if (!isCasting(caster)) return;

        cancel(caster, "teleported", "이동으로 캐스팅이 취소되었습니다."); //todo 문구/형식 사용자 확정 필요
    }

    /**
     * 플러그인 비활성화 시 캐스팅 상태 정리.
     * <p>비활성화 시 Bukkit이 태스크를 취소하므로 완료 콜백의 {@code casting.remove}가 돌지 않는다.
     * 정리하지 않으면 정적 맵에 엔트리가 남아, 상태가 유지되는 재활성화 경로에서
     * 플레이어가 영구히 "캐스팅 중"으로 판정되어 이동이 막힌다.
     */
    public static void shutdown() {
        for (BukkitTask task : casting.values()) task.cancel();
        casting.clear();

        // 스킬 디버그의 정적 상태도 같은 이유(상태가 유지되는 재활성화 경로)로 함께 비운다.
        // 캐스팅 도중 비활성화되면 cancelCast를 거치지 않아 추적 정보가 남는다.
        SkillDebugManager.shutdown();
    }

    /** 취소 사유가 있는 공통 취소 처리 — 어떤 규칙으로 끊겼는지 추적 가능하게 로그를 남긴다. */
    private static void cancel(A_Entity caster, String reason, String message) {
        // 진행도(경과/전체)를 알려면 추적 정보가 살아 있어야 한다 → cancelCast(정리) 전에 출력한다
        SkillDebugManager.onCastCancelled(caster, reason);
        cancelCast(caster);
        UndefinedWorldCorePlugin.sendLog("Casting cancelled (" + reason + "): " + caster.getUniqueIdStr());
        if (caster instanceof A_Player) caster.sendMessage(message);
    }
}
