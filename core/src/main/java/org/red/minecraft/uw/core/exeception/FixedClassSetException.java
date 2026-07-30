package org.red.minecraft.uw.core.exeception;

public class FixedClassSetException extends RuntimeException {
    public FixedClassSetException() {
        super();
    }

    /** 어떤 값을 바꾸려다 막혔는지 남긴다 (메시지 없는 예외는 원인 추적이 불가능하다) */
    public FixedClassSetException(String property) {
        super("Cannot modify fixed object: " + property);
    }
}
