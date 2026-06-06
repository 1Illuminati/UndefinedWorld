package org.red.minecraft.uw.core.skill;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * 스킬 하나가 실행되는 동안 유지되는 공유 상태(Context).
 *
 * 핵심 설계 원칙:
 *  - 파츠가 순서대로 이 객체를 읽고 쓰며, 앞 파츠의 변경이 뒤 파츠에 전달된다.
 *  - 필드는 1:1 의미 고정 — 하나의 필드가 두 가지 의미를 갖지 않는다.
 *    (예: size=효과 크기, searchRange=탐지 반경, range=도달 거리 — 절대 혼용 안 함)
 *  - 모든 필드는 합리적 기본값을 가진다. 파츠가 명시하지 않으면 기본값으로 동작.
 *  - 효과 파츠는 폴백 체인(targets -> targetPoint -> searchRange 탐지)으로 대상을 찾는다.
 *  - 모르는 필드는 효과가 무시한다. 각 효과는 자신이 "소비할" 필드만 읽는다.
 */
public class SkillContext {

    // ════════════════════════════════════════════════
    // 시전자 (항상 존재)
    // ════════════════════════════════════════════════

    /** 스킬을 시전한 플레이어 */
    public Player caster;


    // ════════════════════════════════════════════════
    // ① 수치 (Magnitude) — "얼마나"
    //    대부분 Modifier 파츠가 배율로 누적한다.
    //    각 필드는 단 하나의 의미만 갖는다.
    // ════════════════════════════════════════════════

    /** 피해/회복 기본량 (효과가 최종 계산에 사용) */
    public double damage = 1.0;

    /** 범용 효과 강도 (버프 수치, 흡수량 등 damage가 아닌 강도) */
    public double power = 1.0;

    /** 효과의 물리적 크기 — 번개 굵기, 투사체 크기, 히트박스 크기. (탐지/거리와 무관) */
    public double size = 1.0;

    /** 광역 효과 반경 — 폭발 반경, 장판 반경. 0이면 단일 대상(광역 아님). */
    public double area = 0.0;

    /** 도달 거리 — 투사체 비행 거리, 시전 사거리. 0이면 근접/즉발(비행 없음). */
    public double range = 0.0;

    /** 대상 자동 탐지 반경 — targets/targetPoint가 모두 없을 때만 사용. */
    public double searchRange = 8.0;

    /** 투사체/이동 속도 배율 */
    public double speed = 1.0;

    /** 발사 수량 / 대상 수. 효과가 이 필드를 소비할지는 효과가 결정. */
    public int count = 1;

    /** 관통/체인 횟수 */
    public int pierce = 0;

    /** 치명타 확률 (0.0 ~ 1.0) */
    public double critChance = 0.0;

    /** 넉백 세기 */
    public double knockback = 0.0;


    // ════════════════════════════════════════════════
    // ② 시간 (Timing) — "언제, 얼마 동안" (단위: tick, 20tick = 1초)
    // ════════════════════════════════════════════════

    /** 효과 지속 시간 (버프/장판/소환물 수명 등) */
    public int duration = 0;

    /** 지속 효과의 발동 주기 (장판이 N틱마다 피해) */
    public int tickRate = 20;

    /** 반복 횟수 (같은 동작을 N번) */
    public int repeat = 1;

    /** 반복 간 간격 */
    public int repeatDelay = 0;

    /** 시전(채널) 시간 */
    public int castTime = 0;

    /** 발동 지연 */
    public int delay = 0;

    /** 스킬 재사용 대기시간 (초). Modifier가 줄일 수 있음. */
    public double cooldown = 5.0;


    // ════════════════════════════════════════════════
    // ③ 대상 (Target) — "누구에게"
    //    효과는 폴백 체인으로 대상을 결정한다:
    //      1순위 targets -> 2순위 targetPoint -> 3순위 searchRange 자동 탐지
    // ════════════════════════════════════════════════

    /** 진영 필터 — 효과의 성격에 맞는 기본값을 효과가 적용 (공격=ENEMY, 회복=ALLY 등) */
    public Faction faction = Faction.ENEMY;

    /** 확정된 대상 목록 (Targeting 파츠가 설정). 폴백 체인 1순위. */
    public List<Entity> targets = new ArrayList<>();

    /** 대상 좌표 (위치 기반 효과용). 폴백 체인 2순위. */
    public Location targetPoint = null;

    /** 속성 (화염/냉기/번개 등) — 피해 타입, 상호작용에 사용 */
    public Element element = Element.NONE;

    /** 부여할 상태이상 종류 (디버프/버프 효과용) */
    public StatusType statusType = StatusType.NONE;


    // ════════════════════════════════════════════════
    // ④ 형태 (Shape) — "어떤 모양으로"
    //    형태 프리셋 파츠가 origin/direction/shape/spread를 한 번에 설정한다.
    //    사용자에게는 프리셋 이름("정면 단발", "하늘에서 낙하" 등)으로 노출.
    // ════════════════════════════════════════════════

    /** 발사 시작점. null이면 효과가 caster 위치를 폴백으로 사용. */
    public Location origin = null;

    /** 발사 방향. null이면 효과가 caster 조준 방향을 폴백으로 사용. */
    public Vector direction = null;

    /** 발사 형태 (단일/부채꼴/원형/직선 등) */
    public Shape shape = Shape.SINGLE;

    /** 퍼짐 각도(도) 또는 범위 — shape에 따라 해석 */
    public double spread = 0.0;


    // ════════════════════════════════════════════════
    // ⑤ 메타 (Meta) — 시스템 내부용 (사용자 비노출)
    // ════════════════════════════════════════════════

    /** 연쇄용 — 직전에 효과가 적중한 엔티티 */
    public Entity lastHitTarget = null;

    /** 연쇄용 — 직전에 효과가 적중한 위치 */
    public Location lastHitPos = null;

    /** 연쇄 깊이 (무한 연쇄 방지, MAX 5) */
    public int chainDepth = 0;

    /** 실행된 파츠까지의 마나 비용 누산 */
    public double manaAccum = 0.0;


    // ════════════════════════════════════════════════
    // 폴백 체인 — 효과 파츠가 대상을 결정할 때 사용하는 헬퍼
    // ════════════════════════════════════════════════

    /**
     * 효과가 작용할 대상 목록을 폴백 체인으로 결정한다.
     *   1순위: targets 가 비어있지 않으면 그대로 반환
     *   2순위: targetPoint 가 있으면 그 위치 주변 대상 (효과가 위치 기반이면 빈 리스트로 두고 targetPoint 사용)
     *   3순위: searchRange 반경에서 faction 에 맞는 대상 자동 탐지
     *   최종 폴백: 그래도 없으면 빈 리스트 (효과가 자체 폴백 처리)
     *
     * 실제 엔티티 탐지 로직은 TargetResolver 유틸에 위임한다.
     * 이 메서드는 "어떤 순서로 찾을지"의 계약만 정의한다.
     */
    public List<Entity> resolveTargets(TargetResolver resolver) {
        // 1순위
        if (targets != null && !targets.isEmpty()) {
            return targets;
        }
        // 3순위 — 위치 기반(2순위)은 효과가 targetPoint를 직접 쓰므로 여기선 엔티티 탐지만
        List<Entity> found = resolver.searchNearby(
                origin != null ? origin : caster.getLocation(),
                searchRange,
                faction,
                caster,
                Math.max(1, count)   // count가 대상 수로 쓰이는 효과 대비
        );
        return found;
    }

    /**
     * 효과가 작용할 "위치"를 폴백 체인으로 결정한다.
     *   1순위: targetPoint 가 있으면 사용
     *   2순위: lastHitPos (연쇄 상황)
     *   3순위: 첫 번째 target 의 위치
     *   최종 폴백: origin -> caster 위치
     */
    public Location resolvePoint() {
        if (targetPoint != null) return targetPoint;
        if (lastHitPos != null) return lastHitPos;
        if (targets != null && !targets.isEmpty()) {
            return targets.get(0).getLocation();
        }
        if (origin != null) return origin;
        return caster.getLocation();
    }

    /** 발사 시작점 폴백 — origin 없으면 시전자 눈높이 */
    public Location resolveOrigin() {
        return origin != null ? origin : caster.getEyeLocation();
    }

    /** 발사 방향 폴백 — direction 없으면 시전자 조준 방향 */
    public Vector resolveDirection() {
        return direction != null ? direction.clone()
                : caster.getEyeLocation().getDirection();
    }


    // ════════════════════════════════════════════════
    // 연쇄 스킬용 자식 컨텍스트 생성
    //   수치 필드는 초기화하고, 연쇄 정보(lastHit*)만 물려준다.
    //   이유: 부모의 count/size 등이 자식에 누적되면 지수적 증폭 발생.
    // ════════════════════════════════════════════════

    public SkillContext createChildContext() {
        SkillContext child = new SkillContext();
        child.caster        = this.caster;
        child.lastHitTarget = this.lastHitTarget;
        child.lastHitPos    = this.lastHitPos;
        child.chainDepth    = this.chainDepth + 1;
        // 나머지 수치/형태/대상은 기본값으로 초기화됨 (자동)
        return child;
    }


    // ════════════════════════════════════════════════
    // 열거형 정의
    // ════════════════════════════════════════════════

    public enum Faction {
        ENEMY,   // 적
        ALLY,    // 아군
        SELF,    // 시전자 자신
        ALL      // 모두
    }

    public enum Element {
        NONE,
        PHYSICAL,
        FIRE,
        ICE,
        LIGHTNING,
        POISON,
        HOLY,
        DARK,
        ARCANE
    }

    public enum StatusType {
        NONE,
        STUN,
        SLOW,
        ROOT,
        SILENCE,
        BURN,
        FREEZE,
        POISON,
        WEAKNESS,
        REGENERATION,
        SHIELD,
        SPEED_UP,
        STRENGTH
    }

    public enum Shape {
        SINGLE,      // 단일 (정면 1발)
        SPREAD,      // 부채꼴 (spread 각도로 count발)
        CIRCLE,      // 원형 (360도 방사)
        LINE,        // 직선 (전방 관통)
        RAIN,        // 낙하 (하늘에서 아래로)
        SELF,        // 자신 대상
        POINT        // 지정 위치
    }


    /**
     * 대상 자동 탐지 로직의 계약 인터페이스.
     * 실제 구현(반경 내 엔티티 스캔, faction 필터링)은 별도 유틸에서.
     */
    public interface TargetResolver {
        List<Entity> searchNearby(Location center,
                                  double radius,
                                  Faction faction,
                                  Player caster,
                                  int maxCount);
    }
}