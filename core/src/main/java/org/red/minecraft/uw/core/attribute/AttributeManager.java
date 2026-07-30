package org.red.minecraft.uw.core.attribute;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.util.A_DataMap;
import org.red.minecraft.uw.core.StaticValue;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 업데이트 버전 각 장비, 스텟별 attribute저장을 다르게 해서 관리
 *
 * 저장 위치가 컨테이너마다 다르다.
 *   - EQUIPMENT / STAT : 엔티티 DataMap (PDC 영속)
 *   - BUFF             : 메모리 (비영속) — 아래 BUFF_VALUES 주석 참조
 */
public class AttributeManager {

    /**
     * BUFF 컨테이너 전용 메모리 저장소. 엔티티 UUID -> (AttributeType -> 값)
     *
     * BUFF 는 PDC(디스크)에 영속되면 안 된다 (Process.md §2.6 버프 구조 개편 1).
     * 영속 저장하면 크래시/강제종료로 Buff.onRemove 가 돌지 못했을 때 더해둔 수치가
     * 영구 스탯 증가로 남아 반복 시 무한 증폭 악용이 가능했다.
     * 서버가 꺼지면 이 맵도 함께 사라지는 것이 확정된 동작이다.
     *
     * AttributeManager 는 조회할 때마다 새로 생성되므로(IMobModule.getAttributeHolder)
     * 인스턴스 필드로는 값이 유지되지 않아 클래스 단위로 보관한다.
     *
     * 누수 방지: 값이 0 이 되면 키를 지우고, 엔티티의 값이 전부 비면 엔티티 엔트리째 제거한다.
     * 부동소수 오차로 정확히 0 이 되지 않는 경우까지 감안해 BuffManager 가 마지막 버프 종료 시
     * clearBuffAttributes(UUID) 로 명시적으로 비운다.
     *
     * 스레드: 맵 자체는 동시 접근에 안전하지만 addBaseAttributeValue 는 읽고-쓰기라 원자적이지 않다.
     * 버프 적용/해제는 메인스레드에서만 일어난다는 전제를 지켜야 한다. (BuffManager 클래스 주석 참조)
     */
    private static final Map<UUID, Map<AttributeType, Double>> BUFF_VALUES = new ConcurrentHashMap<>();

    private final Map<ContainerType, Container> containerMap = new HashMap<>();
    private final A_Entity entity;
    public AttributeManager(A_Entity entity) {
        this.entity = entity;
    }

    /**
     * BUFF 메모리 저장소에서 해당 엔티티의 값을 전부 제거한다.
     *
     * 버프가 모두 끝난 시점에 BuffManager 가 호출한다. Buff.onRemove 의 뺄셈만으로는
     * 부동소수 오차(수치를 여러 개 더했다 빼면 정확히 0 이 되지 않는다)가 남아
     * 미세한 영구 스탯과 맵 엔트리가 함께 잔존하기 때문이다.
     *
     * BUFF 컨테이너에 값을 쓰는 곳은 AttributeBuff 뿐이므로 통째로 비워도 잃는 상태가 없다.
     */
    public static void clearBuffAttributes(UUID entityId) {
        BUFF_VALUES.remove(entityId);
    }

    /**
     * 이전 버전에서 PDC 에 영속 저장돼 남아있는 BUFF 컨테이너 잔재를 제거한다. (1회성 정리)
     *
     * BUFF 가 메모리로 분리된 뒤로는 이 키를 읽지 않으므로 스탯에는 이미 영향이 없지만,
     * 죽은 데이터가 계속 남아 원인 추적을 방해하므로 접속 시 지운다.
     * A_DataMap.getDataMap 은 없는 키를 만들어 넣으므로 containsKey 로 먼저 판정한다.
     */
    public static void purgePersistedBuffContainer(A_Entity entity) {
        String key = StaticValue.ATTRIBUTE_CONTAINER_KEY + ContainerType.BUFF.name();
        A_DataMap map = entity.getDataMap(UndefinedWorldCorePlugin.instance);
        if (!map.containsKey(key)) return;

        map.remove(key);
        UndefinedWorldCorePlugin.sendLog("[Attribute] 영속 저장된 BUFF 컨테이너 잔재 제거: " + entity.getUniqueId());
    }

    public A_Entity getEntity() {
        return entity;
    }

    public double getBaseAttributeValue(AttributeType aType, ContainerType cType) {
        return container(cType).getAttributeValue(aType);
    }

    public boolean hasBaseAttributeValue(AttributeType aType, ContainerType cType) {
        return container(cType).hasAttributeValue(aType);
    }

    public void setBaseAttributeValue(AttributeType aType, ContainerType cType, double value) {
        container(cType).setAttributeValue(aType, value);
    }

    /** 컨테이너 종류에 맞는 저장소 구현을 돌려준다. (BUFF 만 메모리, 나머지는 DataMap) */
    private Container container(ContainerType cType) {
        return containerMap.computeIfAbsent(cType, c -> c == ContainerType.BUFF
                ? new BuffAttributeContainer(getEntity())
                : new AttributeContainer(getEntity(), c));
    }

    public void addBaseAttributeValue(AttributeType aType, ContainerType cType, double value) {
        this.setBaseAttributeValue(aType, cType, value + getBaseAttributeValue(aType, cType));
    }

    /**
     * 컨테이너 하나를 비운다. (재계산 직전 초기화용)
     *
     * AttributeType 전체를 0으로 덮어쓰면 저장 DataMap에 전체 키가 기록되므로
     * (플레이어당 AttributeType 개수만큼, 무기 스캔 주기마다 반복) 키 자체를 제거한다.
     * 키가 없으면 getAttributeValue가 0을 반환하므로 결과값은 동일하다.
     */
    public void clearBaseAttributeValues(ContainerType cType) {
        container(cType).clear();
    }

    /**
     * 최종적으로 계산에 사용되는 attribute값을 가져올때 사용
     * LivingEntity일 경우 체력은 다르게 처리 된다
     *
     * @param aType 가져올 attributeType
     * @return 최종값
     */
    public double getAttributeValue(AttributeType aType) {
        double result = 0;

        for (ContainerType cType : ContainerType.values()) {
            result += getBaseAttributeValue(aType, cType);
        }

        return result;
    }

    /** 컨테이너 저장소 공통 계약. 저장 위치(DataMap / 메모리)만 다르고 동작은 동일하다. */
    protected interface Container extends AttributeHolder {
        void clear();
    }

    protected record AttributeContainer(A_Entity entity, ContainerType cType) implements Container {
        @Override
        public double getAttributeValue(AttributeType type) {
            // getDataMap()은 매번 하위 DataMap을 새로 조회하므로(데미지 계산에서 반복 호출된다) 한 번만 가져온다
            A_DataMap map = this.getDataMap();
            if (!map.containsKey(type.name())) {
                return 0;
            }

            return map.getDouble(type.name());
        }

        @Override
        public void setAttributeValue(AttributeType type, double value) {
            this.getDataMap().put(type.name(), value);
        }

        @Override
        public boolean hasAttributeValue(AttributeType type) {
            return this.getDataMap().containsKey(type.name());
        }

        @Override
        public void clear() {
            this.getDataMap().clear();
        }

        public A_DataMap getDataMap() {
            return entity.getDataMap(UndefinedWorldCorePlugin.instance).getDataMap(StaticValue.ATTRIBUTE_CONTAINER_KEY + cType.name());
        }
    }

    /**
     * BUFF 전용 메모리 컨테이너. 저장 위치만 BUFF_VALUES 이고 나머지 동작은 AttributeContainer 와 같다.
     *
     * 값 갱신은 전부 ConcurrentHashMap.compute 안에서 처리한다.
     * 바깥에서 get 한 뒤 put/remove 하면 "비었으니 엔티티 엔트리 제거" 와 경합해
     * 방금 넣은 값이 버려진 맵에 들어갈 수 있다. (BuffManager.putActive 와 동일한 이유)
     */
    protected record BuffAttributeContainer(A_Entity entity) implements Container {

        @Override
        public double getAttributeValue(AttributeType type) {
            Map<AttributeType, Double> values = BUFF_VALUES.get(entity.getUniqueId());
            if (values == null) return 0;

            Double value = values.get(type);
            return value == null ? 0 : value;
        }

        /** 0 은 합산 결과를 바꾸지 않는다. 키를 남기면 엔티티가 사라져도 맵이 계속 자라므로 지운다. */
        @Override
        public void setAttributeValue(AttributeType type, double value) {
            UUID id = entity.getUniqueId();

            if (value == 0) {
                BUFF_VALUES.computeIfPresent(id, (key, values) -> {
                    values.remove(type);
                    return values.isEmpty() ? null : values;
                });
                return;
            }

            BUFF_VALUES.compute(id, (key, values) -> {
                Map<AttributeType, Double> target = (values == null) ? new ConcurrentHashMap<>() : values;
                target.put(type, value);
                return target;
            });
        }

        @Override
        public boolean hasAttributeValue(AttributeType type) {
            Map<AttributeType, Double> values = BUFF_VALUES.get(entity.getUniqueId());
            return values != null && values.containsKey(type);
        }

        @Override
        public void clear() {
            clearBuffAttributes(entity.getUniqueId());
        }
    }

    public enum ContainerType {
        EQUIPMENT,
        STAT,
        BUFF
    }
}
