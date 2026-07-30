package org.red.minecraft.uw.core.skill.effect.modifier;

import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.effect.Effect;
import org.red.minecraft.uw.core.skill.effect.EffectResult;
import org.red.minecraft.uw.core.skill.effect.EffectType;

import java.util.concurrent.CompletableFuture;

/**
 * CTX 값을 지정한 값으로 <b>덮어쓰는</b> 수정자 이펙트.
 * (누산/배율이 필요한 수정자는 {@code CountIncreaseEffect}/{@code SizeMultiplyEffect} 처럼 별도 구현체를 쓴다)
 *
 * <h2>수정자 이펙트 연산 규칙 (사용자 확정 §2.10 — 새 수정자는 반드시 이 규칙을 따를 것)</h2>
 * 연산은 <b>{@link CTXType} 의 선언 타입</b>이 정한다. 이펙트 이름도 연산에 맞춘다.
 * <table border="1">
 *   <caption>CTX 선언 타입별 수정자 연산</caption>
 *   <tr><th>CTX 선언 타입</th><th>연산</th><th>팩토리 id 규칙</th><th>예</th></tr>
 *   <tr><td>{@code double.class}</td><td><b>곱셈</b>(배율)</td><td>{@code *_multiply}</td>
 *       <td>SIZE / RANGE / SPEED / DAMAGE / TIME / RENDER</td></tr>
 *   <tr><td>{@code int.class}</td><td><b>덧셈</b></td><td>{@code *_increase}</td>
 *       <td>PIERCE / COUNT / REPEAT / CHAIN / LEVEL / TARGET_COUNT</td></tr>
 *   <tr><td>열거형 · 객체</td><td><b>덮어쓰기</b>(이 클래스)</td><td>{@code *_effect}</td>
 *       <td>ELEMENTAL / DAMAGE_TYPE / TARGET_FACTION / SEARCH_TYPE / PROJECTILE_TYPE / PROJECTILE_SHAPE</td></tr>
 * </table>
 *
 * <p><b>확정된 예외 1건 — {@code SEARCH_RANGE}.</b> {@code double.class} 지만 곱셈이 아니라 <b>덮어쓰기</b>다.
 * 탐색 반경 배율은 {@code RANGE} 하나로 통일하기로 했고 SEARCH_RANGE 는 절대값 지정용으로 남겼다
 * ({@code SearchRangeEffect} 주석 참고).
 *
 * <p><b>연산 계열 수치 CTX 는 기본값이 반드시 항등원이어야 한다.</b> 그래야 "수정자 기어가 없으면 기어 YAML 값
 * 그대로"라는 §2.6 폴백이 성립한다. 곱셈 CTX 는 기본값 {@code 1.0}, 덧셈 CTX 중 <b>소비처가 자기 기본값에
 * 더해서 쓰는 것</b>({@code TARGET_COUNT})은 기본값 {@code 0} 이다.
 * <p>반대로 {@code COUNT}/{@code PIERCE}/{@code REPEAT}/{@code CHAIN} 은 소비처가 CTX 를 <b>절대 개수로 직접</b>
 * 쓰므로 기본값이 {@code 1} 이다. <b>이 둘을 섞지 말 것</b> — 소비처가 CTX 를 어떻게 읽는지에 따라
 * 기본값이 0 이어야 할지 1 이어야 할지가 갈린다. (기본값 0 인 CTX 를 절대 개수로 읽으면 결과가 항상 0 이 된다)
 *
 * <h2>⚠️ 배율 수정자는 중첩 시 지수적으로 커진다 — 상한을 넣지 않는 것이 확정 사항이다</h2>
 * {@code *_multiply} 수정자는 기존 배율에 <b>곱한다.</b> 기어는 서버가 쓰는 설정이 아니라
 * <b>플레이어가 조합하는 아이템</b>이고(스킬 최대 9기어), §2.6 스킬 코어 6 에서 <b>기어 파워 음수를 허용</b>하므로
 * 파워 총합 9 제한은 같은 수정자 기어를 여러 개 넣는 것을 막지 못한다. 그 결과:
 * <ul>
 *   <li>{@code damage_multiply: 3} 기어 9개 → 3<sup>9</sup> = <b>19,683배</b> 데미지</li>
 *   <li>{@code range_multiply: 5} 기어 9개 → 사거리 배율 약 <b>195만 배</b>.
 *       {@code ProjectileEffect} 는 사거리 상한이 없고 PIERCE 는 블록도 통과하므로,
 *       발사체 1기가 매 틱 {@code getNearbyEntities} 를 돌며 사실상 무한히 나는 태스크가 된다</li>
 *   <li>덧셈이었다면 각각 19배 / 46배로 <b>선형</b>에 그친다</li>
 * </ul>
 * <b>사용자가 이 위험을 인지한 상태에서 곱셈 유지를 확정했다.</b> 통제 수단은 코드 상한이 아니라
 * <b>서버가 만드는 기어 YAML 의 배율값</b>이다(큰 배율 기어를 애초에 만들지 않는 것이 운영 책임).
 * <p>⛔ <b>여기에 배율 상한을 넣지 마라.</b> 버그로 보고 클램프를 추가하는 것은 확정 사항 위반이다.
 * <p>단, <b>무효값 방어는 상한이 아니라 안전장치이므로 반드시 유지</b>한다 — 각 수정자의 {@code isFinite}
 * 검사, {@code ProjectileController.isTravelable()}, 파티클 확산값·{@code targetRender} 검사 등.
 * 배율이 커지다 {@code Infinity} 가 되면 이 가드들이 잡아준다.
 * <p>테스트 데이터({@code test_items.yml})에 수정자 기어를 넣을 때는 배율을 보수적으로(1.2~1.5) 잡을 것 —
 * 테스트 값이 그대로 운영 기준처럼 굳는 일이 잦다.
 *
 * <p><b>수정자 층과 소비 층은 별개다.</b> 위 규칙은 <b>CTX 값을 어떻게 바꾸는가</b>만 정한다.
 * 그 값을 소비처가 배율로 쓸지 절대값으로 쓸지는 각 소비처의 규약이다
 * (예: {@code LEVEL} 은 수정자에서 덧셈이지만 {@code BuffEffect} 는 배율로 소비한다 —
 *  {@code LevelIncreaseEffect} 주석 참고).
 *
 * <p><b>수정자를 만들지 않는 CTX</b>: {@code CASTER}, {@code LAST_TARGET_INFO} 는 기어가 지정하는 설정값이 아니라
 * 스킬 실행 중 채워지는 <b>런타임 상태값</b>이다. 특히 {@code CASTER} 덮어쓰기는 시전자를 위조해 데미지 귀속·
 * 진영 판정을 우회하는 악용 경로가 되므로 수정자를 만들지 않는다.
 */
public class SimpleModifierEffect<T> implements Effect {
    private final CTXType type;
    private final T value;

    /**
     * @throws IllegalArgumentException value 가 null 이거나 CTXType 이 요구하는 타입이 아닐 때.
     *         잘못된 조합은 생성 시점에 막지 않으면 나중에 getCTX 호출 지점에서 ClassCastException 으로 터진다.
     */
    public SimpleModifierEffect(CTXType type, T value) {
        if (value == null)
            throw new IllegalArgumentException("SimpleModifierEffect Error: value is null (type=" + type + ")");

        // == 비교는 하위 타입을 전부 거부한다.
        // (CASTER 처럼 인터페이스로 선언된 CTX, 상수 본문을 가진 enum 의 익명 하위 클래스가 여기 걸린다)
        // CTXType.clazz 는 원시 타입이 래퍼로 정규화돼 있으므로 isInstance 로 검사하면 된다.
        if (!type.clazz.isInstance(value))
            throw new IllegalArgumentException("SimpleModifierEffect Error:" + type.clazz + " != " + value.getClass());

        this.type = type;
        this.value = value;
    }

    public CTXType getType() {
        return type;
    }

    public T value() {
        return value;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        ctx.setCTX(type, this.value);
        return CompletableFuture.completedFuture(EffectResult.SUCCESS);
    }

    /** PierceIncreaseEffect/SizeMultiplyEffect 와 동일하게 수정자로 분류한다 (기존엔 빈 배열이라 분류가 누락됐다) */
    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.MODIFIER};
    }
}
