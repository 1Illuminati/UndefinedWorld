package org.red.minecraft.uw.core.combat.buff;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.dellarte.library.util.A_DataMap;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.event.UWBuffApplyEvent;
import org.red.minecraft.uw.core.event.UWBuffEndEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 버프 적용/조회/제거 및 종료 흐름 전체를 관리한다.
 *
 * 종료 4가지:
 *   1. 시간 종료  → BuffData 내부 타이머가 EXPIRED로 자동 종료
 *   2. 사망       → onDeath(entity) (또는 BuffData의 isDead 감지)
 *   3. 로그아웃   → onQuit(player) 로 보류 저장, onJoin(player) 로 복원
 *   4. 강제 제거  → removeBuff / removeAll (FORCED)
 *
 * 이벤트는 직접 연결하지 않고, 리스너 등에서 이 매니저의 onDeath/onQuit/onJoin 메서드를 호출하면 된다.
 * (메인스레드에서 사용하는 것을 전제로 한다)
 */
public class BuffManager {

    /** 로그아웃 보류 스냅샷을 담는 플레이어 데이터 키 */
    private static final String SUSPEND_KEY = "buff_suspends";

    /**
     * 버프 시작/종료 안내 문구. (Process.md §2.10 — "버프 안내 문구는 임의 제작, 시작과 종료만")
     * 나중에 바꿀 수 있게 여기 한 곳에만 둔다.
     *
     * 적용: 이름 / 레벨(중첩 수) / 지속시간(초 표기)
     * 종료: 이름
     *
     * //todo 문구/형식 사용자 확정 필요 (임의 제작분)
     */
    private static final String APPLY_MESSAGE_FORMAT = "[버프] %s 적용 (레벨 %d, %s)";
    private static final String END_MESSAGE_FORMAT = "[버프] %s 종료";

    private final Plugin plugin;

    /**
     * 활성 버프: 엔티티 UUID -> (BuffKey -> BuffData)
     *
     * 키가 타입 단독이 아니라 (타입, 이름) 이다. (Process.md §2.6 버프 구조 개편 3)
     * 타입만으로 묶으면 스탯이 서로 다른 ATTRIBUTE_BUFF 들이 서로를 덮어써 하나만 살아남는다.
     */
    private final Map<UUID, Map<BuffKey, BuffData>> active = new ConcurrentHashMap<>();

    /**
     * 이 서버 세션의 식별자. 매니저가 만들어질 때(= 플러그인 활성화 시) 1회 생성한다.
     *
     * 로그아웃 스냅샷은 다중서버를 고려해 플레이어 PDC(디스크)에 저장되므로 서버를 재시작해도 남는다.
     * 그런데 버프 자체는 재시작하면 사라지는 것이 확정된 동작이므로(Process.md §2.6 버프 구조 개편 1),
     * 스냅샷에 이 값을 함께 적어두고 복원 시 세션이 다르면 버린다.
     *   같은 세션 안에서의 로그아웃 -> 재접속 = 복원 / 서버 재시작 = 소멸
     */
    private final UUID sessionId = UUID.randomUUID();

    public BuffManager(Plugin plugin) {
        this.plugin = plugin;
        UndefinedWorldCorePlugin.sendLog("[Buff] 버프 세션 시작: " + sessionId);
    }

    /** plugin 미주입 상태에서도 원인 로그가 사라지지 않도록 폴백을 둔다 */
    private void warn(String message) {
        if (plugin == null) {
            UndefinedWorldCorePlugin.sendLog(message);
            return;
        }
        plugin.getLogger().warning(message);
    }

    // ---------------------------------------------------------------
    // 적용
    // ---------------------------------------------------------------

    /** level만 필요할 때의 간편 오버로드 */
    public BuffData applyBuff(A_Entity entity, BuffType type, int level, long durationTicks, boolean async) {
        return applyBuff(entity, type, BuffContext.of(level), durationTicks, async);
    }

    /** 컨텍스트를 직접 넘겨 적용 */
    public BuffData applyBuff(A_Entity entity, BuffType type, BuffContext ctx, long durationTicks, boolean async) {
        return applyBuff(entity, type.create(ctx), durationTicks, async);
    }

    /**
     * Buff 인스턴스를 직접 적용 (core). <b>타입과 이름이 모두 같은</b> 버프가 있으면 교체한다.
     * (이름이 다르면 같은 타입이어도 별개 버프로 함께 유지된다)
     *
     * 일반 버프는 재적용 = 덮어쓰기(지속시간·레벨 전부 새 값).
     * 중첩 버프는 기존 레벨에 요청 레벨을 누적한다 (resolveStack 참조).
     *
     * 주의: 중첩 버프는 레벨이 바뀌면 BuffType 팩토리로 다시 만들어지므로,
     *       팩토리가 만드는 클래스와 다른 구현체를 직접 넘기면 그 인스턴스가 유지되지 않는다.
     */
    public BuffData applyBuff(A_Entity entity, Buff buff, long durationTicks, boolean async) {
        UUID id = entity.getUniqueId();

        // 중첩 판정은 기존 버프를 종료시키기 전에 해야 한다 (종료하면 기존 레벨을 읽을 수 없다)
        BuffData old = getBuff(entity, keyOf(buff));
        Buff applied = resolveStack(buff, old);

        // 키는 실제로 등록할 인스턴스에서 뽑는다.
        // putActive 와 handleEnd 의 removeActive 가 같은 키를 써야 활성 맵에서 확실히 빠진다.
        BuffKey key = keyOf(applied);

        if (old != null) {
            // 교체 종료는 안내하지 않는다. 안내하면 화상/감전처럼 피격마다 재적용되는 버프에서
            // "종료 → 적용" 두 줄이 매 피격마다 도배된다. (효과 원복은 평소와 동일하게 진행된다)
            old.suppressEndNotice();
            old.end(BuffRemoveReason.FORCED);
        }

        BuffData data = new BuffData(entity, applied, durationTicks, async);
        putActive(id, key, data);

        try {
            data.start(plugin, this::handleEnd);
        } catch (Throwable t) {
            // start 실패(onApply 예외 / 스케줄러 등록 거부)로 타이머 없는 유령 버프가 맵에 남으면
            // hasBuff 가 영구히 true 가 된다. (원복 자체는 BuffData.start 가 담당)
            removeActive(id, key, data);
            // start 롤백의 onRemove 도 뺄셈이라 부동소수 잔재가 남을 수 있다. 종료 경로와 동일하게 정리한다.
            purgeBuffAttributes(id);
            warn("[Buff] 적용 실패로 되돌림 (" + key + ", " + id + "): " + t);
            throw t;
        }

        notifyApply(data, old);

        // 적용 확정 통지. 다른 도메인(예: 침묵 → 캐스팅 취소)은 이 이벤트를 구독해 처리한다.
        // 버프 도메인이 스킬 도메인을 직접 호출하면 패키지 순환 의존이 생기므로 통지만 한다.
        // 재적용에서도 발행한다 — 조건을 나누면 침묵 취소를 놓치는 경로가 생긴다.
        fireEvent(() -> new UWBuffApplyEvent(entity, data));
        return data;
    }

    /**
     * 이벤트 발행. 구독자에서 터진 예외가 버프 처리를 되돌리거나 호출자를 끊지 않게 감싼다.
     * (ElementalPostProcessor 가 데미지 처리 도중 applyBuff 를 호출하므로,
     *  여기서 예외가 새면 데미지 파이프라인 전체가 멈춘다. 버프는 이미 적용/종료된 상태다)
     *
     * 이벤트를 완성된 객체가 아니라 supplier 로 받는 이유: 생성 자체가 호출부에서 일어나면
     * 생성 시 예외(@NotNull 런타임 검사 등)가 이 가드 밖에서 터진다. 생성까지 감싼다.
     */
    private void fireEvent(Supplier<Event> eventSupplier) {
        try {
            Bukkit.getPluginManager().callEvent(eventSupplier.get());
        } catch (Throwable t) {
            warn("[Buff] 이벤트 발행 오류: " + t);
        }
    }

    /**
     * 중첩 규칙 적용. (Process.md §2.6 버프 구조 개편 2·4)
     *
     * - 중첩 버프(StackableBuff): 타입·이름이 같은 기존 버프가 있으면 <b>매니저가 레벨을 누적</b>한다.
     *   새 레벨 = min(maxStack, 기존레벨 + 요청레벨). 호출자는 "이번에 추가할 양"만 넘긴다.
     * - 일반 버프: 누적하지 않고 <b>덮어쓴다</b> (지속시간·레벨 전부 새 값).
     *
     * 중첩 수는 BuffContext.level 이고 BuffContext 는 record 라 값을 고칠 수 없으므로,
     * 레벨이 실제로 바뀔 때만 같은 caster/data 로 인스턴스를 다시 만든다.
     */
    private Buff resolveStack(Buff buff, BuffData old) {
        if (!(buff instanceof StackableBuff stackable)) return buff;

        int max = Math.max(1, stackable.maxStack());
        BuffContext ctx = buff.context();
        int previous = (old == null) ? 0 : old.getBuff().context().level();

        // level 은 BuffContext 에서 최소 1로 정규화되지만 요청값이 커도 오버플로가 나지 않게 long 으로 더한다
        int level = (int) Math.min(max, (long) previous + ctx.level());
        if (level == ctx.level()) return buff;

        UndefinedWorldCorePlugin.sendLog("[Buff] 중첩 갱신 (" + buff.getName() + "): "
                + previous + " + " + ctx.level() + " -> " + level);
        return buff.type().create(new BuffContext(level, ctx.caster(), ctx.data()));
    }

    /**
     * 활성 버프 키 생성. 이름이 비어 있는 구현체는 타입 이름으로 대체해 키가 깨지지 않게 한다.
     * (키가 null 이면 ConcurrentHashMap 에 넣을 수 없어 적용 자체가 실패한다)
     */
    private BuffKey keyOf(Buff buff) {
        String name = buff.getName();
        if (name == null || name.isBlank()) {
            warn("[Buff] getName() 이 비어 있어 타입 이름으로 대체 (" + buff.getClass().getName() + ")");
            name = buff.type().name();
        }
        return new BuffKey(buff.type(), name);
    }

    // ---------------------------------------------------------------
    // active 맵 갱신 (엔티티 키 단위로 원자 처리)
    //  - 내부 맵을 직접 put/remove 하면 "비었으니 엔티티 엔트리 제거" 와 경합해
    //    방금 넣은 버프가 맵째로 떨어져 나가(유령 버프) 정리 대상에서 빠질 수 있다.
    // ---------------------------------------------------------------

    private void putActive(UUID id, BuffKey buffKey, BuffData data) {
        active.compute(id, (key, map) -> {
            Map<BuffKey, BuffData> target = (map == null) ? new ConcurrentHashMap<>() : map;
            target.put(buffKey, data);
            return target;
        });
    }

    /** 해당 인스턴스일 때만 제거한다. (이미 새 버프로 교체됐다면 최신 버프를 지우지 않아야 한다) */
    private void removeActive(UUID id, BuffKey buffKey, BuffData data) {
        active.computeIfPresent(id, (key, map) -> {
            map.remove(buffKey, data);
            return map.isEmpty() ? null : map;
        });
    }

    // ---------------------------------------------------------------
    // 종료 콜백: 맵 정리 + onRemove + QUIT이면 컨텍스트 스냅샷 저장
    // ---------------------------------------------------------------

    /**
     * 어떤 단계가 실패해도 onRemove(효과 원복)까지는 반드시 도달해야 한다.
     * 중간에 예외가 빠져나가면 BUFF 컨테이너에 더해둔 수치가 영구히 남고(영구 스탯 증가),
     * removeAll 루프도 중단되어 나머지 버프가 활성 상태로 남는다.
     */
    private void handleEnd(BuffData data, BuffRemoveReason reason) {
        UUID id = data.getEntityId();

        removeActive(id, keyOf(data.getBuff()), data);

        if (reason == BuffRemoveReason.QUIT) {
            try {
                saveSuspend(data);
            } catch (Throwable t) {
                warn("[Buff] 로그아웃 스냅샷 저장 실패 (" + data.getName() + ", " + id + "): " + t);
            }
        }

        try {
            data.getBuff().onRemove(data.getEntity(), reason);
        } catch (Throwable t) {
            warn("[Buff] onRemove 오류 (" + data.getName() + ", " + id + "): " + t);
        }

        purgeBuffAttributes(id);

        // 상태 정리가 모두 끝난 뒤에 통지한다 (구독자가 조회하면 종료가 반영된 값을 본다)
        fireEvent(() -> new UWBuffEndEvent(data.getEntity(), data, reason));
        notifyEnd(data, reason);
    }

    /**
     * 이 엔티티의 버프가 하나도 남지 않았으면 BUFF attribute 메모리를 통째로 비운다.
     *
     * AttributeBuff.onRemove 의 뺄셈만으로는 부동소수 오차가 남아(여러 수치를 더했다 빼면 정확히 0 이 되지 않는다)
     * 미세한 영구 스탯과 맵 엔트리가 함께 잔존한다. 버프가 전부 끝난 시점의 정답은 항상 "0" 이므로
     * 그 시점에 명시적으로 비워 잔재와 누수를 동시에 막는다.
     * (BUFF 컨테이너에 값을 쓰는 곳은 AttributeBuff 뿐이라 잃는 상태가 없다)
     */
    private void purgeBuffAttributes(UUID id) {
        if (active.containsKey(id)) return;
        AttributeManager.clearBuffAttributes(id);
    }

    /** 재접속 복원용 스냅샷 저장 (QUIT 전용) */
    private void saveSuspend(BuffData data) {
        A_Player player = resolvePlayer(data);
        if (player == null) {
            warn("[Buff] QUIT 스냅샷 대상 플레이어를 찾을 수 없어 건너뜀 ("
                    + data.getType() + ", " + data.getEntityId() + ")");
            return;
        }

        BuffContext ctx = data.getBuff().context();
        UUID casterId = (ctx.caster() != null) ? ctx.caster().getUniqueId() : null;

        A_DataMap dataMap = player.getDataMap(UndefinedWorldCorePlugin.instance);
        List<Suspended> list = dataMap.getList(SUSPEND_KEY);
        list.add(new Suspended(sessionId, data.getType(), data.getRemainingTicks(), data.isAsync(),
                ctx.level(), casterId, ctx.data()));

        //없어도 될텐데 혹시 몰라서
        dataMap.set(SUSPEND_KEY, list);
    }

    /**
     * 로그아웃 시점에는 Bukkit.getPlayer(uuid) 가 null 이 되어
     * CommediaDellarte.getAPlayer(uuid) 도 null 을 돌려준다.
     * 그래서 적용 시점에 들고 있던 대상을 먼저 쓰고, 그게 플레이어가 아닐 때만 재조회한다.
     */
    private A_Player resolvePlayer(BuffData data) {
        if (data.getEntity() instanceof A_Player player) return player;
        return CommediaDellarte.getAPlayer(data.getEntityId());
    }

    // ---------------------------------------------------------------
    // 제거
    // ---------------------------------------------------------------

    /**
     * 해당 타입의 버프를 <b>전부</b> 제거한다.
     * 이름이 다른 같은 타입 버프가 여러 개 있을 수 있으므로(ATTRIBUTE_BUFF) 하나만 지우면
     * "제거했는데 hasBuff 가 여전히 true" 인 상태가 된다.
     */
    public void removeBuff(A_Entity entity, BuffType type) {
        for (BuffData data : getBuffs(entity, type)) {
            data.end(BuffRemoveReason.FORCED);
        }
    }

    /** 타입과 이름이 모두 일치하는 버프 하나만 제거한다. */
    public void removeBuff(A_Entity entity, BuffType type, String name) {
        BuffData data = getBuff(entity, type, name);
        if (data != null) data.end(BuffRemoveReason.FORCED);
    }

    public void removeAll(A_Entity entity) {
        removeAll(entity, BuffRemoveReason.FORCED);
    }

    private void removeAll(A_Entity entity, BuffRemoveReason reason) {
        Map<BuffKey, BuffData> map = active.get(entity.getUniqueId());
        if (map == null) return;
        for (BuffData data : new ArrayList<>(map.values())) {
            data.end(reason);
        }
    }

    // ---------------------------------------------------------------
    // 이벤트 훅
    // ---------------------------------------------------------------

    public void onDeath(A_Entity entity) {
        removeAll(entity, BuffRemoveReason.DEATH);
    }

    public void onQuit(A_Player player) {
        removeAll(player, BuffRemoveReason.QUIT);
    }

    public void onJoin(A_Player player) {
        // 이전 버전에서 PDC 에 영속 저장된 BUFF attribute 잔재 정리 (BUFF 는 이제 메모리 저장이다)
        AttributeManager.purgePersistedBuffContainer(player);

        A_DataMap dataMap = player.getDataMap(UndefinedWorldCorePlugin.instance);
        // getList 는 키가 없으면 빈 리스트를 만들어 저장한다(조회만으로 데이터가 늘어난다).
        // 보류 버프가 없는 접속이 대부분이므로 존재 여부를 먼저 확인한다. (Process.md §2.6 A_DataMap 원칙)
        if (!dataMap.containsKey(SUSPEND_KEY)) return;

        List<Suspended> list = dataMap.getList(SUSPEND_KEY);
        if (list == null || list.isEmpty()) return;

        // 복원 전에 보류 목록을 비운다.
        // 비우지 않으면 재접속마다 같은 스냅샷이 다시 복원되고 목록이 무한히 쌓인다.
        List<Suspended> restoring = new ArrayList<>(list);
        list.clear();
        dataMap.set(SUSPEND_KEY, list);

        for (Suspended s : restoring) {
            // 손상/구버전 엔트리(type 해석 실패)는 건너뛴다
            if (s == null || s.type() == null) continue;

            // 다른 서버 세션에서 저장된 스냅샷은 버린다 (서버 재시작 = 버프 소멸).
            // 조용히 사라지면 "버프가 왜 없어졌는지" 추적이 불가능하므로 로그를 남긴다.
            if (!sessionId.equals(s.sessionId())) {
                UndefinedWorldCorePlugin.sendLog("[Buff] 이전 서버 세션의 보류 버프 폐기 ("
                        + s.type() + ", " + player.getUniqueId() + ", 저장 세션=" + s.sessionId() + ")");
                continue;
            }

            // 0 = 이미 만료된 스냅샷, 음수 = 무한 지속이므로 복원 대상
            if (s.remainingTicks() == 0) continue;

            try {
                // caster를 다시 resolve (이미 없으면 null로 적용)
                Entity e = (s.casterId() == null) ? null : Bukkit.getEntity(s.casterId());
                A_Entity caster = (e == null) ? null : CommediaDellarte.getAEntity(e);

                BuffContext ctx = new BuffContext(s.level(), caster, s.data());
                applyBuff(player, s.type(), ctx, s.remainingTicks(), s.async());
            } catch (Throwable t) {
                // 한 버프 복원 실패가 나머지 복원과 접속 처리를 막지 않게 한다
                warn("[Buff] 복원 실패 (" + s.type() + ", " + player.getUniqueId() + "): " + t);
            }
        }
    }

    // ---------------------------------------------------------------
    // 조회
    // ---------------------------------------------------------------

    public boolean hasBuff(A_Entity entity) {
        Map<BuffKey, BuffData> map = active.get(entity.getUniqueId());
        return map != null && !map.isEmpty();
    }

    /** 그 타입의 버프가 <b>하나라도</b> 있는지. (이름은 보지 않는다) */
    public boolean hasBuff(A_Entity entity, BuffType type) {
        return getBuff(entity, type) != null;
    }

    /** 타입과 이름이 모두 일치하는 버프가 있는지. */
    public boolean hasBuff(A_Entity entity, BuffType type, String name) {
        return getBuff(entity, type, name) != null;
    }

    /**
     * 그 타입의 버프 중 하나를 돌려준다. 없으면 null.
     *
     * 같은 타입을 이름으로 여러 개 유지할 수 있으므로(ATTRIBUTE_BUFF) 어느 것이 나올지는 정해져 있지 않다.
     * 타입당 하나만 존재하는 상태 마커(SHOCK/SHATTER/SILENCE 등)를 위한 조회이고,
     * 특정 버프를 정확히 집어야 하면 이름까지 넘기는 오버로드를 쓴다.
     */
    public BuffData getBuff(A_Entity entity, BuffType type) {
        Map<BuffKey, BuffData> map = active.get(entity.getUniqueId());
        if (map == null) return null;

        for (Map.Entry<BuffKey, BuffData> entry : map.entrySet()) {
            if (entry.getKey().type() == type) return entry.getValue();
        }
        return null;
    }

    /** 타입과 이름이 모두 일치하는 버프. 없으면 null. */
    public BuffData getBuff(A_Entity entity, BuffType type, String name) {
        if (name == null) return null;
        Map<BuffKey, BuffData> map = active.get(entity.getUniqueId());
        return map == null ? null : map.get(new BuffKey(type, name));
    }

    private BuffData getBuff(A_Entity entity, BuffKey key) {
        Map<BuffKey, BuffData> map = active.get(entity.getUniqueId());
        return map == null ? null : map.get(key);
    }

    public Collection<BuffData> getBuffs(A_Entity entity) {
        Map<BuffKey, BuffData> map = active.get(entity.getUniqueId());
        return map == null ? Collections.emptyList() : new ArrayList<>(map.values());
    }

    /** 그 타입의 버프 전부. (이름만 다른 같은 타입 버프들을 한 번에 볼 때) */
    public Collection<BuffData> getBuffs(A_Entity entity, BuffType type) {
        Map<BuffKey, BuffData> map = active.get(entity.getUniqueId());
        if (map == null) return Collections.emptyList();

        List<BuffData> result = new ArrayList<>();
        for (Map.Entry<BuffKey, BuffData> entry : map.entrySet()) {
            if (entry.getKey().type() == type) result.add(entry.getValue());
        }
        return result;
    }

    // ---------------------------------------------------------------
    // 정리
    // ---------------------------------------------------------------

    /**
     * 플러그인 비활성화 정리. 남은 버프를 전부 강제 종료해 효과(BUFF 컨테이너 수치 등)를 원복한다.
     *
     * 종료 안내는 보내지 않는다 — 서버가 내려가는 순간 접속자 전원이 자기 버프 수만큼 줄을 받게 되고,
     * 어차피 곧 끊기므로 정보 가치도 없다. (notifyEnd 주석의 QUIT 제외 근거와 같다)
     */
    public void shutdown() {
        for (Map<BuffKey, BuffData> map : new ArrayList<>(active.values())) {
            for (BuffData data : new ArrayList<>(map.values())) {
                data.suppressEndNotice();
                data.end(BuffRemoveReason.FORCED);
            }
        }
        active.clear();
    }

    // ---------------------------------------------------------------
    // 안내 메시지 (Process.md §2.6 버프 구조 개편 5 — 플레이어 채팅창)
    // ---------------------------------------------------------------

    /**
     * 버프 적용 안내.
     *
     * 지속시간만 갱신되는 재적용은 알리지 않는다.
     * 속성 디버프(화상/감전/침묵)는 피격마다 재적용되므로 매번 보내면 채팅이 도배된다.
     * 신규 적용이거나 레벨(중첩 수)이 바뀐 경우에만 보낸다.
     */
    private void notifyApply(BuffData data, BuffData old) {
        int level = data.getBuff().context().level();
        if (old != null && old.getBuff().context().level() == level) return;

        sendNotice(data.getEntity(), String.format(Locale.ROOT, APPLY_MESSAGE_FORMAT,
                data.getName(), level, formatDuration(data.getRemainingTicks())));
    }

    /**
     * 버프 종료 안내. (Process.md §2.10 — "버프 안내 문구는 시작과 종료만")
     *
     * <p>종료 4가지 중 <b>EXPIRED / FORCED</b> 만 보낸다. 나머지 둘을 뺀 근거는 아래와 같다.
     * <ul>
     *   <li><b>QUIT</b> — 로그아웃은 "종료"가 아니라 <b>보류</b>다(스냅샷으로 재접속 시 복원된다).
     *       게다가 이 시점의 클라이언트는 이미 끊기는 중이라 메시지가 화면에 뜨지도 않는다.
     *       가진 버프 수만큼 도달하지 않을 줄을 만들어내는 것뿐이다.</li>
     *   <li><b>DEATH</b> — 사망은 <b>모든 버프를 한 번에</b> 끝내므로 사망할 때마다 버프 수만큼 줄이 쌓인다.
     *       또 "죽었다"는 것 자체가 이미 충분한 피드백이라 어느 버프가 끝났는지는 정보 가치가 낮다.</li>
     * </ul>
     * 이 두 가지는 확정 문구("종료만")를 벗어나는 판단이므로 사용자 확인이 필요하다.
     *
     * <p>재적용 교체·서버 종료로 끝나는 FORCED 는 {@code isEndNoticeSuppressed} 로 걸러낸다.
     * 전자를 보내면 화상/감전처럼 피격마다 갱신되는 버프에서 "종료 → 적용"이 매 피격마다 두 줄씩 나가고,
     * 후자를 보내면 서버가 내려가는 순간 접속자 전원이 가진 버프 수만큼 줄을 받는다.
     */
    private void notifyEnd(BuffData data, BuffRemoveReason reason) {
        if (data.isEndNoticeSuppressed()) return;
        if (reason == BuffRemoveReason.QUIT || reason == BuffRemoveReason.DEATH) return;

        sendNotice(data.getEntity(), String.format(Locale.ROOT, END_MESSAGE_FORMAT, data.getName()));
    }

    /**
     * 채팅 안내는 접속 중인 플레이어에게만 의미가 있다.
     * 메시지 전송 실패가 적용/종료 흐름을 끊지 않도록 감싼다.
     */
    private void sendNotice(A_Entity entity, String message) {
        if (!(entity instanceof A_Player player) || !player.isOnline()) return;

        try {
            player.sendMessage(message);
        } catch (Throwable t) {
            warn("[Buff] 안내 메시지 전송 실패 (" + entity.getUniqueId() + "): " + t);
        }
    }

    /** 지속시간 표기는 틱이 아니라 초로 보여준다 (§2.6 스킬 코어 1과 동일 규칙). */
    private String formatDuration(long ticks) {
        if (ticks < 0) return "무한";
        return String.format(Locale.ROOT, "%.1f초", ticks / 20.0);
    }

    /**
     * 활성 버프 식별 키. (Process.md §2.6 버프 구조 개편 3)
     * 같은 타입이어도 이름이 다르면 다른 버프이므로 둘을 함께 키로 쓴다.
     */
    private record BuffKey(BuffType type, String name) {
        @Override
        public String toString() {
            return type + "/" + name;
        }
    }

    /**
     * 재접속 복원용 스냅샷.
     * caster는 로그아웃/사망으로 무효해질 수 있으므로 live Entity 대신 UUID만 보관한다.
     *
     * 다중서버 사용 고려를 위한 저장 위치를 플레이어 내부로 변경
     * 서버 이동을 통한 로그아웃은 상태에서는 안사라지게
     *
     * 소멸 시점은 sessionId 로 정한다 — 저장한 서버 세션과 복원하는 세션이 다르면 버린다.
     * (버프는 서버 재시작으로 사라지는 것이 확정 동작이므로, 디스크에 남은 스냅샷이 되살리면 안 된다)
     *
     * type 해석에 실패하면(버프 종류 삭제/이름 변경) null type 으로 만든다.
     * sessionId 해석에 실패하면 null 이 되고, 세션 비교에서 걸러진다.
     * 여기서 예외를 던지면 플레이어 데이터 전체 로드가 깨져 접속 자체가 막힌다.
     */
    private record Suspended(UUID sessionId, BuffType type, long remainingTicks, boolean async,
                             int level, UUID casterId, A_DataMap data) implements ConfigurationSerializable {

        @Override
        public @NotNull Map<String, Object> serialize() {
            A_DataMap map = new A_DataMap();
            map.put("sessionId", sessionId == null ? null : sessionId.toString());
            map.put("type", type == null ? null : type.name());
            map.put("remainingTicks", String.valueOf(remainingTicks));
            map.put("async", async);
            map.put("level", level);
            map.put("casterId", casterId == null ? null : casterId.toString());
            map.put("data", data);
            return map.serialize();
        }

        public static @NotNull Suspended deserialize(Map<String, Object> m) {
            A_DataMap map = new A_DataMap(m);

            return new Suspended(readUUID(map, "sessionId"), readType(map), readRemainingTicks(map),
                    map.getBoolean("async"), map.getInt("level"), readUUID(map, "casterId"),
                    map.getClass("data", A_DataMap.class));
        }

        private static BuffType readType(A_DataMap map) {
            String name = map.getString("type");
            if (name.isEmpty()) return null;
            try {
                return BuffType.valueOf(name);
            } catch (IllegalArgumentException e) {
                UndefinedWorldCorePlugin.sendLog("[Buff] 알 수 없는 보류 버프 타입, 복원 대상에서 제외: " + name);
                return null;
            }
        }

        /** 0 이면 onJoin 에서 복원 대상에서 제외된다 */
        private static long readRemainingTicks(A_DataMap map) {
            String raw = map.getString("remainingTicks");
            if (raw.isEmpty()) return 0L;
            try {
                return Long.parseLong(raw);
            } catch (NumberFormatException e) {
                UndefinedWorldCorePlugin.sendLog("[Buff] 보류 버프 남은시간 해석 실패, 복원 대상에서 제외: " + raw);
                return 0L;
            }
        }

        /** UUID 해석 실패는 null 로 처리한다 (casterId = caster 없이 복원, sessionId = 복원 대상에서 제외) */
        private static UUID readUUID(A_DataMap map, String key) {
            String raw = map.getString(key);
            if (raw.isEmpty()) return null;
            try {
                return UUID.fromString(raw);
            } catch (IllegalArgumentException e) {
                UndefinedWorldCorePlugin.sendLog("[Buff] 보류 버프 " + key + " 해석 실패: " + raw);
                return null;
            }
        }
    }
}
