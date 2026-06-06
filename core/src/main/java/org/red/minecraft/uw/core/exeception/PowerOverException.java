package org.red.minecraft.uw.core.exeception;

public class PowerOverException extends RuntimeException {
    public PowerOverException() {
        super("Skill power cannot be greater than 9");
    }
}
