package org.red.minecraft.uw.core.combat.buff;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.util.A_DataMap;

import java.util.Optional;

/**
 * 버프 적용 1건의 불변 컨텍스트. level은 핵심 값이고,
 * caster / data는 시전자·커스텀 수치 등 확장용이다.
 *
 * record라 모든 필드는 생성 후 변경 불가. data는 방어적 복사된다.
 * (A_DataMap은 불변 뷰를 제공하지 않으므로 복사까지만 보장한다)
 */
public record BuffContext(int level, A_Entity caster, A_DataMap data) {

    // 정규화: level은 최소 1로 클램프, data는 null-safe + 방어적 복사
    // 복사하지 않으면 적용 이후에도 호출자가 들고 있는 A_DataMap으로 컨텍스트를 바꿀 수 있다
    // (파쇄 중첩 등 level 외 수치를 data로 옮길 때 상태 추적이 불가능해진다)
    public BuffContext {
        if (level < 1) level = 1;
        data = (data == null) ? new A_DataMap() : new A_DataMap(data.getMap());
    }

    // --- 간편 생성 ---
    public static BuffContext of(int level) {
        return new BuffContext(level, null, null);
    }

    public static Builder builder(int level) {
        return new Builder(level);
    }

    // --- 접근 ---
    /** caster는 nullable이므로 Optional로 감싸 제공 */
    public Optional<A_Entity> casterOpt() {
        return Optional.ofNullable(caster);
    }

    /**
     * data 조회.
     * A_DataMap.get(key)는 값이 없을 때 기본값을 맵에 넣어버려서(= 조회에 숨은 side effect가 생긴다)
     * 불변이어야 할 컨텍스트가 조회만으로 바뀐다. 그래서 containsKey로 먼저 확인한다.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) (data.containsKey(key) ? data.get(key) : null);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T def) {
        if (!data.containsKey(key)) return def;
        Object v = data.get(key);
        return v == null ? def : (T) v;
    }

    // --- 빌더 ---
    public static final class Builder {
        private final int level;
        private A_Entity caster;
        private final A_DataMap data = new A_DataMap();

        private Builder(int level) { this.level = level; }

        public Builder caster(A_Entity caster) { this.caster = caster; return this; }
        public Builder put(String key, Object value) { this.data.put(key, value); return this; }

        public BuffContext build() {
            return new BuffContext(level, caster, data);
        }
    }
}
