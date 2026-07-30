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

    /**
     * 파싱 실패 시 어떤 팩토리/어떤 설정 경로에서 터졌는지 알 수 없으면 원인 추적이 불가능하므로
     * (예: {@code BuffType.valueOf} 실패 메시지에는 기어 정보가 전혀 없다) 컨텍스트를 붙여 재던진다.
     */
    @Override
    public T create(ConfigurationSection section) {
        try {
            return function.apply(section);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Failed to create '" + id + "' from section '"
                    + (section == null ? "null" : section.getCurrentPath()) + "': " + exception, exception);
        }
    }

    @Override
    public String getID() {
        return id;
    }
}
