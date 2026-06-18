package org.red.minecraft.uw.core.skill.factory;

import org.bukkit.configuration.ConfigurationSection;

/**
 * 인스터스가 double 변수 하나만을 요구할때 사용하는 매우 간단하게 쓰는용도
 * 생성자가 무조건 double 파라미터 하나만 요구해야한다 아닐 경우 당연히 에러
 * @param <T> 뭐겠냐
 */
public class DoubleSimpleFactory<T> implements SkillFactory<T> {
    private final Class<T> clazz;
    private final String id;
    private final String valueName;
    public DoubleSimpleFactory(Class<T> clazz, String id, String valueName) {
        this.clazz = clazz;
        this.id = id;
        this.valueName = valueName;
    }

    @Override
    public T create(ConfigurationSection section) {
        try {
            double value = section.getDouble(valueName, 0);
            return clazz.getConstructor(double.class).newInstance(value);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getID() {
        return this.id;
    }
}
