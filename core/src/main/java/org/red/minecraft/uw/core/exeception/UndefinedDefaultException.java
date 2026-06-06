package org.red.minecraft.uw.core.exeception;

import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.util.HasID;

public class UndefinedDefaultException extends RuntimeException {
    public UndefinedDefaultException(HasID id, CTXType type) {
        super(String.format("Undefined default exception for %s with id %s", type.name(), id));
    }
}
