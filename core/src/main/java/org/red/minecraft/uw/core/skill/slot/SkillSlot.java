package org.red.minecraft.uw.core.skill.slot;

import org.jetbrains.annotations.Nullable;

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

    @Nullable
    public static SkillSlot byName(String name) {
        try {
            return SkillSlot.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
