package org.red.minecraft.uw.core.skill.slot;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * 스킬 장착 슬롯. (확정: 우클릭 / 쉬프트+좌클릭 / 쉬프트+우클릭 3종)
 * 추후 확장은 상수 추가만으로 가능하다 (저장 키 = name(), 발동 판정은 SkillCastListener).
 */
public enum SkillSlot {
    RIGHT_CLICK("우클릭"),
    SHIFT_LEFT_CLICK("쉬프트+좌클릭"),
    SHIFT_RIGHT_CLICK("쉬프트+우클릭");

    public final String krName;

    SkillSlot(String krName) {
        this.krName = krName;
    }

    /**
     * 슬롯 이름(enum name) 파싱. 실패 시 null.
     * Locale.ROOT 고정 — 기본 로케일이 터키어 등이면 toUpperCase 결과가 달라져 valueOf가 실패한다.
     */
    @Nullable
    public static SkillSlot byName(@Nullable String name) {
        if (name == null || name.isBlank()) return null;
        try {
            return SkillSlot.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
