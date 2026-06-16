package org.red.minecraft.uw.core.combat.buff;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.dellarte.library.util.A_DataMap;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    private final Plugin plugin;

    /** 활성 버프: 엔티티 UUID -> (BuffType -> BuffData) */
    private final Map<UUID, Map<BuffType, BuffData>> active = new ConcurrentHashMap<>();

    public BuffManager(Plugin plugin) {
        this.plugin = plugin;
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

    /** Buff 인스턴스를 직접 적용 (core). 같은 타입이 있으면 갱신. */
    public BuffData applyBuff(A_Entity entity, Buff buff, long durationTicks, boolean async) {
        UUID id = entity.getUniqueId();

        BuffData old = getBuff(entity, buff.type());
        if (old != null) old.end(BuffRemoveReason.FORCED);

        Map<BuffType, BuffData> map = active.computeIfAbsent(id, k -> new HashMap<>());
        BuffData data = new BuffData(entity, buff, durationTicks, async);
        map.put(buff.type(), data);
        data.start(plugin, this::handleEnd);
        return data;
    }

    // ---------------------------------------------------------------
    // 종료 콜백: 맵 정리 + onRemove + QUIT이면 컨텍스트 스냅샷 저장
    // ---------------------------------------------------------------

    private void handleEnd(BuffData data, BuffRemoveReason reason) {
        UUID id = data.getEntityId();

        Map<BuffType, BuffData> map = active.get(id);
        if (map != null) {
            map.remove(data.getType());
            if (map.isEmpty()) active.remove(id);
        }

        if (reason == BuffRemoveReason.QUIT) {
            BuffContext ctx = data.getBuff().context();
            UUID casterId = (ctx.caster() != null) ? ctx.caster().getUniqueId() : null;

            A_Player player = CommediaDellarte.getAPlayer(id);
            List<Suspended> list = player.getDataMap(UndefinedWorldCorePlugin.instance).getList("buff_suspends");
            list.add(new Suspended(data.getType(), data.getRemainingTicks(), data.isAsync(), ctx.level(), casterId, ctx.data()));

            //없어도 될텐데 혹시 몰라서
            player.getDataMap(UndefinedWorldCorePlugin.instance).set("buff_suspends", list);
        }

        data.getBuff().onRemove(data.getEntity(), reason);
    }

    // ---------------------------------------------------------------
    // 제거
    // ---------------------------------------------------------------

    public void removeBuff(A_Entity entity, BuffType type) {
        BuffData data = getBuff(entity, type);
        if (data != null) data.end(BuffRemoveReason.FORCED);
    }

    public void removeAll(A_Entity entity) {
        removeAll(entity, BuffRemoveReason.FORCED);
    }

    private void removeAll(A_Entity entity, BuffRemoveReason reason) {
        Map<BuffType, BuffData> map = active.get(entity.getUniqueId());
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
        List<Suspended> list = player.getDataMap(UndefinedWorldCorePlugin.instance).getList("buff_suspends");
        if (list == null || list.isEmpty()) return;
        for (Suspended s : list) {
            // caster를 다시 resolve (이미 없으면 null로 적용)
            Entity e = (s.casterId() == null) ? null : Bukkit.getEntity(s.casterId());
            A_Entity caster = (e == null) ? null : CommediaDellarte.getAEntity(e);

            BuffContext ctx = new BuffContext(s.level(), caster, s.data());
            applyBuff(player, s.type(), ctx, s.remainingTicks(), s.async());
        }
    }

    // ---------------------------------------------------------------
    // 조회
    // ---------------------------------------------------------------

    public boolean hasBuff(A_Entity entity) {
        Map<BuffType, BuffData> map = active.get(entity.getUniqueId());
        return map != null && !map.isEmpty();
    }

    public boolean hasBuff(A_Entity entity, BuffType type) {
        Map<BuffType, BuffData> map = active.get(entity.getUniqueId());
        return map != null && map.containsKey(type);
    }

    public BuffData getBuff(A_Entity entity, BuffType type) {
        Map<BuffType, BuffData> map = active.get(entity.getUniqueId());
        return map == null ? null : map.get(type);
    }

    public Collection<BuffData> getBuffs(A_Entity entity) {
        Map<BuffType, BuffData> map = active.get(entity.getUniqueId());
        return map == null ? Collections.emptyList() : new ArrayList<>(map.values());
    }

    // ---------------------------------------------------------------
    // 정리
    // ---------------------------------------------------------------

    public void shutdown() {
        for (Map<BuffType, BuffData> map : new ArrayList<>(active.values())) {
            for (BuffData data : new ArrayList<>(map.values())) {
                data.end(BuffRemoveReason.FORCED);
            }
        }
        active.clear();
    }

    /**
     * 재접속 복원용 스냅샷.
     * caster는 로그아웃/사망으로 무효해질 수 있으므로 live Entity 대신 UUID만 보관한다.
     *
     * 다중서버 사용 고려를 위한 저장 위치를 플레이어 내부로 변경
     * 서버 이동을 통한 로그아웃은 상태에서는 안사라지게
     * 어느시점에서 사라지게 할지는 고려 필요 -> todo
     */
    private record Suspended(BuffType type, long remainingTicks, boolean async,
                             int level, UUID casterId, A_DataMap data) implements ConfigurationSerializable {

        @Override
        public @NotNull Map<String, Object> serialize() {
            A_DataMap map = new A_DataMap();
            map.put("type", type.name());
            map.put("remainingTicks", String.valueOf(remainingTicks));
            map.put("async", async);
            map.put("level", level);
            map.put("casterId", casterId);
            map.put("data", data);
            return map.serialize();
        }

        public static @NotNull Suspended deserialize(Map<String, Object> m) {
            A_DataMap map = new A_DataMap(m);
            return  new Suspended(BuffType.valueOf(map.getString("type")), Long.valueOf(map.getString("remainingTicks")), map.getBoolean("async"),
                    map.getInt("level"), map.getUUID("casterId"), map.getClass("data", A_DataMap.class));
        }
    }
}
