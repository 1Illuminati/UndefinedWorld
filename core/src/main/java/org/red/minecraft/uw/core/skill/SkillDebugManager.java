package org.red.minecraft.uw.core.skill;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.skill.condition.Condition;
import org.red.minecraft.uw.core.skill.cost.Cost;
import org.red.minecraft.uw.core.skill.cost.CostData;
import org.red.minecraft.uw.core.skill.cost.CostType;
import org.red.minecraft.uw.core.skill.effect.EffectResult;
import org.red.minecraft.uw.core.skill.gear.Gear;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 스킬 디버그 모드. ({@code /skill debug} — op 전용, 명령어 등록은 command 담당)
 *
 * <p>켜 둔 시전자에게만 스킬 실행 과정을 채팅으로 흘려보낸다.
 * 계측 지점(호출부)은 전부 같은 패키지 안에 있다 — {@link SkillEngine}, {@link CastingManager},
 * {@link CastingLifecycleListener}. 그래서 토글/조회 두 개만 public 이고 나머지 훅은 package-private 이다.
 *
 * <p><b>디버그 OFF 일 때의 규약</b>
 * <ul>
 *     <li>모든 훅은 {@link #isDebug} 판정을 <b>맨 처음</b> 한다 → 꺼져 있으면 문자열을 만들지 않는다.</li>
 *     <li>훅은 값을 계산하지 않은 원본 인자(Gear/SkillNode/CostData)를 받는다 →
 *         호출부에서 디버그용 계산이 미리 일어나지 않는다.</li>
 *     <li>모든 훅 본문은 {@link RuntimeException} 을 삼킨다 → 계측이 스킬 실행을 죽이지 않는다.</li>
 * </ul>
 *
 * <p><b>상태 저장 위치: 메모리(정적 Set/Map). 영속 저장하지 않는다.</b>
 * 디버그 토글은 운영자가 그 순간의 스킬 동작을 들여다보려고 켜는 일회성 진단 도구다.
 * PDC/파일로 남기면 서버 재시작 후에도 디버그 채팅이 되살아나고, 임시 진단값 때문에 저장 스키마가 늘어난다.
 * 대신 <b>퇴장 시 반드시 해제</b>한다({@link #clear}) — 정적 맵에 UUID 가 쌓이는 누수를 막기 위함이며,
 * 그래서 재접속하면 디버그는 다시 켜야 한다.
 *
 * <p>메인스레드 사용 전제(스킬/캐스팅 흐름과 동일).
 */
public final class SkillDebugManager {

    //todo 문구/형식 사용자 확정 필요 (접두사·색상·표기 단위)
    private static final String PREFIX = "[스킬디버그] ";

    /** 디버그를 켠 시전자 UUID. 퇴장 시 제거된다. */
    private static final Set<UUID> debugging = new HashSet<>();

    /**
     * 진행 중인 캐스팅의 진행도 추적. <b>디버그가 켜진 상태로 캐스팅을 시작한 경우에만</b> 기록된다.
     * (캐스팅 도중에 디버그를 켠 경우는 추적 대상이 아니다 — 시작 시각을 알 수 없다)
     */
    private static final Map<UUID, CastTrace> castTraces = new HashMap<>();

    private SkillDebugManager() {}

    /** @param startTick 캐스팅 시작 시점의 서버 틱, @param totalTicks 캐스팅 총 길이(틱) */
    private record CastTrace(int startTick, int totalTicks) {}

    // ── 토글 / 조회 (명령어 담당이 호출하는 공개 계약) ──────────────

    /**
     * 디버그 모드를 켜고 끈다.
     * @return 토글 <b>이후</b>의 상태 (true = 켜짐)
     */
    public static boolean toggle(A_Player player) {
        UUID id = player.getUniqueId();

        if (debugging.remove(id)) {
            castTraces.remove(id); // 꺼진 뒤에 남은 추적 정보는 쓰이지 않는다
            return false;
        }

        debugging.add(id);
        return true;
    }

    /** 비플레이어(몹 시전자)는 항상 false — 디버그는 플레이어 UUID 로만 켜진다. */
    public static boolean isDebug(A_Entity entity) {
        if (entity == null) return false; // CTX 의 CASTER 가 비어 있는 방어 경로에서도 호출된다
        return debugging.contains(entity.getUniqueId());
    }

    // ── 정리 ───────────────────────────────────────────────

    /** 퇴장 시 정리 ({@link CastingLifecycleListener}). 정적 맵 누수 방지. */
    static void clear(UUID id) {
        debugging.remove(id);
        castTraces.remove(id);
    }

    /** 플러그인 비활성화 시 정리 ({@link CastingManager#shutdown()} 에서 함께 호출). */
    static void shutdown() {
        debugging.clear();
        castTraces.clear();
    }

    // ── 스킬 진입 ──────────────────────────────────────────

    static void onSkillStart(A_Entity caster, SkillDefinition skill) {
        if (!isDebug(caster)) return;

        try {
            send(caster, "── 스킬 시작: " + skill.getSkillName()
                    + " | 기어 " + skill.getGears().size() + "개"
                    + " | 파워 " + skill.getSkillPower()
                    + " | 쿨타임 " + skill.getSkillCoolDown() + "초"
                    + " | 캐스팅 " + seconds(skill.getCastingTime()) + "초(" + skill.getCastingTime() + "틱)");
        } catch (RuntimeException exception) {
            fail("onSkillStart", exception);
        }
    }

    /** 침묵/탈것 등 사용 자체가 막힌 경우 ({@code SkillEngine.reportBlocked}) */
    static void onBlocked(A_Entity caster, String reason) {
        if (!isDebug(caster)) return;

        try {
            send(caster, "차단: " + reason);
        } catch (RuntimeException exception) {
            fail("onBlocked", exception);
        }
    }

    static void onCoolDownBlocked(A_Entity caster, double remainSeconds) {
        if (!isDebug(caster)) return;

        try {
            send(caster, "차단: 쿨타임 — 남은 " + String.format("%.1f", remainSeconds) + "초");
        } catch (RuntimeException exception) {
            fail("onCoolDownBlocked", exception);
        }
    }

    /**
     * 자원 부족.
     * @param phase 어느 시점의 검사인지 ("사전 체크" / "캐스팅 완료")
     */
    static void onCostLacking(A_Entity caster, CostType type, CostData costData, String phase) {
        if (!isDebug(caster)) return;

        try {
            send(caster, "차단(" + phase + "): 자원 부족 — " + type.name()
                    + " 필요 " + String.format("%.1f", sumValues(costData.getCost(type))));
        } catch (RuntimeException exception) {
            fail("onCostLacking", exception);
        }
    }

    // ── 캐스팅 ─────────────────────────────────────────────

    /** 캐스팅 시간이 0 이하라 캐스팅 단계를 건너뛴 경우 (즉시 시전) */
    static void onCastSkipped(A_Entity caster, int castTicks) {
        if (!isDebug(caster)) return;

        try {
            send(caster, "캐스팅 없음 — 즉시 시전 (합산 " + castTicks + "틱)");
        } catch (RuntimeException exception) {
            fail("onCastSkipped", exception);
        }
    }

    static void onCastStart(A_Entity caster, int totalTicks) {
        if (!isDebug(caster)) return;

        try {
            int startTick = Bukkit.getCurrentTick();
            castTraces.put(caster.getUniqueId(), new CastTrace(startTick, totalTicks));
            // 시작 시각은 서버 틱으로 남긴다 — 다른 로그(sendLog)와 같은 시간축에서 맞춰보기 위함이다
            send(caster, "캐스팅 시작 (tick " + startTick + ") — 총 " + seconds(totalTicks) + "초(" + totalTicks + "틱)");
        } catch (RuntimeException exception) {
            fail("onCastStart", exception);
        }
    }

    /**
     * 캐스팅 취소. 사유 문자열은 {@link CastingManager} 의 취소 사유를 그대로 받는다
     * (사유 구분의 SSOT 는 CastingManager 쪽이다 — 여기서는 표기만 한다).
     * <p>추적 정보 제거는 하지 않는다 — 취소 경로는 전부 {@link CastingManager#cancelCast(UUID)} 를
     * 지나므로 {@link #onCastEnd} 한 곳에서만 지운다.
     */
    static void onCastCancelled(A_Entity caster, String reason) {
        if (!isDebug(caster)) return;

        try {
            CastTrace trace = castTraces.get(caster.getUniqueId());
            if (trace == null) return; // 캐스팅 중이 아니면 알릴 진행도가 없다

            int elapsed = Bukkit.getCurrentTick() - trace.startTick();
            send(caster, "캐스팅 취소 (" + reasonLabel(reason) + ") — 진행 "
                    + seconds(elapsed) + "초 / " + seconds(trace.totalTicks()) + "초 ("
                    + percent(elapsed, trace.totalTicks()) + "%)");
        } catch (RuntimeException exception) {
            fail("onCastCancelled", exception);
        }
    }

    /**
     * 캐스팅 정상 완료. 취소와 달리 {@link CastingManager#cancelCast(UUID)} 를 거치지 않는 유일한 종료 경로라
     * <b>추적 정보 제거를 여기서 직접</b> 한다.
     * <p>제거는 isDebug 판정보다 먼저 한다 — 캐스팅 도중 디버그를 끈 경우에도 흔적이 남지 않아야 한다.
     */
    static void onCastComplete(A_Entity caster) {
        // 아무도 디버그를 켜지 않은 서버에서는 여기서 바로 빠진다 (모든 캐스팅 완료가 지나는 경로다)
        if (castTraces.isEmpty()) return;

        try {
            CastTrace trace = castTraces.remove(caster.getUniqueId());
            if (trace == null || !isDebug(caster)) return;

            send(caster, "캐스팅 완료 — 실제 " + seconds(Bukkit.getCurrentTick() - trace.startTick())
                    + "초 / 설정 " + seconds(trace.totalTicks()) + "초");
        } catch (RuntimeException exception) {
            fail("onCastComplete", exception);
        }
    }

    /**
     * 캐스팅 종료 시 추적 정보 정리 (출력 없음).
     * 모든 취소 경로가 지나는 {@link CastingManager#cancelCast(UUID)} 에서 호출한다.
     */
    static void onCastEnd(UUID id) {
        // 디버그를 아무도 안 켠 서버에서는 여기서 바로 빠진다 (모든 캐스팅 취소가 지나는 경로다)
        if (castTraces.isEmpty()) return;
        castTraces.remove(id);
    }

    // ── 비용 / 쿨타임 ──────────────────────────────────────

    static void onCostPaid(A_Entity caster, CostType type, List<Cost<?>> costs) {
        if (!isDebug(caster)) return;

        try {
            send(caster, "비용 지불: " + type.name() + " " + String.format("%.1f", sumValues(costs))
                    + " (기어 " + costs.size() + "건 합산)");
        } catch (RuntimeException exception) {
            fail("onCostPaid", exception);
        }
    }

    static void onCostPayFailed(A_Entity caster, CostType type, Exception exception) {
        if (!isDebug(caster)) return;

        try {
            send(caster, "비용 지불 실패: " + type.name() + " — " + exception);
        } catch (RuntimeException runtimeException) {
            fail("onCostPayFailed", runtimeException);
        }
    }

    /** @param coolDownSeconds 스킬 합산 쿨타임(초). 0 이하는 "쿨타임 없음"이며 실제로 적용되지 않는다. */
    static void onCoolDownApplied(A_Entity caster, int coolDownSeconds) {
        if (!isDebug(caster)) return;

        try {
            send(caster, coolDownSeconds <= 0
                    ? "쿨타임 적용 안 함 (합산 " + coolDownSeconds + "초 — 0 이하는 쿨타임 없음)"
                    : "쿨타임 적용: " + coolDownSeconds + "초");
        } catch (RuntimeException exception) {
            fail("onCoolDownApplied", exception);
        }
    }

    // ── 기어 체인 ──────────────────────────────────────────

    /** 노드 체인이 시작도 못 하고 끊긴 경우 (노드 없음 / 시전자 상태 불가 등) */
    static void onChainAborted(A_Entity caster, String reason) {
        if (!isDebug(caster)) return;

        try {
            send(caster, "체인 중단: " + reason);
        } catch (RuntimeException exception) {
            fail("onChainAborted", exception);
        }
    }

    /** 노드 진입 — 조건을 전부 통과해 Effect 실행 직전 */
    static void onGearStart(A_Entity caster, SkillDefinition.SkillNode node) {
        if (!isDebug(caster)) return;

        try {
            Gear gear = node.gear();
            send(caster, "기어 실행: " + gear.getID()
                    + " | 조건 " + gear.getConditions().size() + "개 통과");
        } catch (RuntimeException exception) {
            fail("onGearStart", exception);
        }
    }

    /**
     * 조건 실패로 노드가 스킵됨.
     * @param exception 조건 평가 중 예외가 났으면 그 예외, 단순히 false 를 반환했으면 null
     */
    static void onConditionFailed(A_Entity caster, SkillDefinition.SkillNode node,
                                  Condition condition, @Nullable RuntimeException exception) {
        if (!isDebug(caster)) return;

        try {
            send(caster, "기어 스킵: " + node.gear().getID()
                    + " | 조건 실패 " + condition.getClass().getSimpleName()
                    + (exception == null ? "" : " (예외: " + exception + ")"));
        } catch (RuntimeException runtimeException) {
            fail("onConditionFailed", runtimeException);
        }
    }

    /** Effect 실행 결과. SUCCESS 인데 다음 노드가 없으면 체인이 정상 종료된 지점이다. */
    static void onGearResult(A_Entity caster, SkillDefinition.SkillNode node, EffectResult result) {
        if (!isDebug(caster)) return;

        try {
            String gearID = node.gear().getID();
            if (result != EffectResult.SUCCESS) {
                send(caster, "기어 결과: " + gearID + " → " + result.name() + " (체인 중단)");
                return;
            }

            boolean hasNext = node.nextNode() != null && !node.nextNode().isEmpty();
            send(caster, "기어 결과: " + gearID + " → SUCCESS"
                    + (hasNext ? " (다음 노드 " + node.nextNode().size() + "개)" : " (마지막 기어 — 체인 종료)"));
        } catch (RuntimeException exception) {
            fail("onGearResult", exception);
        }
    }

    /**
     * Effect 가 결과를 내지 못하고 끊긴 경우.
     * @param stage     어느 단계에서 끊겼는지 ("실행 예외" / "null future" / "비동기 예외" / "null 결과" / "후처리 예외")
     * @param throwable 원인 (없으면 null)
     */
    static void onGearBroken(A_Entity caster, SkillDefinition.SkillNode node,
                             String stage, @Nullable Throwable throwable) {
        if (!isDebug(caster)) return;

        try {
            send(caster, "기어 중단: " + node.gear().getID() + " | " + stage
                    + (throwable == null ? "" : " — " + throwable));
        } catch (RuntimeException exception) {
            fail("onGearBroken", exception);
        }
    }

    // ── 내부 유틸 ──────────────────────────────────────────

    /** 출력 창구는 여기 하나뿐이다 — 문구/형식을 바꿀 때 이 클래스만 보면 된다. */
    private static void send(A_Entity target, String message) {
        //todo 문구/형식 사용자 확정 필요 (이 클래스의 모든 메시지 문구는 임의 제작본이다)
        target.sendMessage(PREFIX + message);
    }

    /** 계측 실패는 스킬 실행과 무관하게 서버 로그로만 남긴다 (플레이어 출력 경로가 이미 깨진 상태다) */
    private static void fail(String hook, RuntimeException exception) {
        UndefinedWorldCorePlugin.sendLog("SkillDebug 출력 실패 (" + hook + "): " + exception);
    }

    /** 틱 → 초 표기 (SkillEngine 의 플레이어 표기 규칙과 동일 — 내부 계산은 전부 틱이다) */
    private static String seconds(int ticks) {
        return String.format("%.1f", ticks / 20.0);
    }

    private static int percent(int elapsed, int total) {
        if (total <= 0) return 100;
        return (int) Math.round(Math.min(elapsed, total) * 100.0 / total);
    }

    /** 수치형(Number) 비용만 합산한다 (SkillEngine.sumRequired 와 같은 기준) */
    private static double sumValues(List<Cost<?>> costs) {
        double sum = 0;
        for (Cost<?> cost : costs) {
            if (cost.getValue() instanceof Number number) sum += number.doubleValue();
        }
        return sum;
    }

    /** CastingManager 의 취소 사유 코드 → 표기 문구 */
    private static String reasonLabel(String reason) {
        return switch (reason) { //todo 문구 사용자 확정 필요
            case "attacked" -> "피격";
            case "silenced" -> "침묵";
            case "mounted" -> "탈것 탑승";
            case "teleported" -> "텔레포트(이동)";
            case "other_skill" -> "다른 스킬 사용";
            default -> reason;
        };
    }
}
