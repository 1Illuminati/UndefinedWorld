package org.red.minecraft.uw.core.skill;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.util.A_DataMap;

public class SkillCTX {
    private final A_DataMap map = new A_DataMap();

    public SkillCTX(A_Entity caster) {
        this.setCTX(CTXType.CASTER, caster);

        for (CTXType type : CTXType.values()) {
            if (type.defaultValue != null)
                setCTX(type, type.defaultValue);
        }
    }

    public <T> void setCTX(CTXType type, T value) {
        this.map.put(type.name(), value);
    }

    public <T> T getCTX(CTXType type) {
        return (T) this.map.getClass(type.name(), type.clazz);
    }

    public <T> T getCTX(CTXType type, T defaultValue) {
        return hasCTX(type) ? getCTX(type) : defaultValue;
    }

    public boolean hasCTX(CTXType type) {
        return map.containsKey(type.name());
    }
}
