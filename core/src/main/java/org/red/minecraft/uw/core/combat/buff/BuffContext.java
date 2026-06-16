package org.red.minecraft.uw.core.combat.buff;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.util.A_DataMap;

import java.util.Optional;

/**
 * 버프 적용 1건의 불변 컨텍스트. level은 핵심 값이고,
 * caster / data는 시전자·커스텀 수치 등 확장용이다.
 *
 * record라 모든 필드는 생성 후 변경 불가. data는 방어적 복사 + 불변 래핑된다.
 */
public record BuffContext(int level, A_Entity caster, A_DataMap data) {

    // 정규화: level은 최소 1로 클램프, data는 null-safe + 불변화
    public BuffContext {
        if (level < 1) level = 1;
        data = (data == null) ? new A_DataMap() : data;
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

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T def) {
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
