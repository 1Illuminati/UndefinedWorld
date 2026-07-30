package org.red.minecraft.uw.core.exeception;

public class PowerOverException extends RuntimeException {
    public PowerOverException() {
        super("Skill power cannot be greater than 9");
    }

    /** 실제 초과값을 남긴다 (어느 기어에서 넘쳤는지 추적용) */
    public PowerOverException(int power) {
        super("Skill power cannot be greater than 9 (was " + power + ")");
    }
}
