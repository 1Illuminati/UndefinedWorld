package org.red.minecraft.uw.core.skill.factory;

import org.bukkit.configuration.ConfigurationSection;

import java.util.function.Function;

/**
 * ConfigurationSection에서 여러 값을 읽어 생성하는 범용 팩토리.
 * SimpleFactory(단일 값 전용)로 처리 불가능한 다중 파라미터 생성에 사용한다.
 * 파싱 로직은 등록부(SkillEngine.setFactories)의 람다에 명시적으로 둔다.
 */
public class FunctionFactory<T> implements SkillFactory<T> {

    private final String id;
    private final Function<ConfigurationSection, T> function;

    public FunctionFactory(String id, Function<ConfigurationSection, T> function) {
        this.id = id;
        this.function = function;
    }

    @Override
    public T create(ConfigurationSection section) {
        return function.apply(section);
    }

    @Override
    public String getID() {
        return id;
    }
}
