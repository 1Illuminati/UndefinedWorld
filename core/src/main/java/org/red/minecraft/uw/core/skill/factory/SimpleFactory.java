package org.red.minecraft.uw.core.skill.factory;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;

/**
 * ConfigurationSection 의 단일 값(value) 하나를 읽어, 그 값을 인자로 받는 생성자를 호출해
 * {@code T} 인스턴스를 만드는 범용 SkillFactory.
 *
 * <p>지원하는 {@code A} 타입:
 * <ul>
 *     <li>enum         - String 으로 저장된 상수명을 {@code Enum.valueOf} 로 복원</li>
 *     <li>숫자 래퍼/원시 - YAML 리터럴 형태(5 vs 5.0)와 무관하게 목표 타입으로 강제변환</li>
 *     <li>String 등    - 그대로 사용</li>
 * </ul>
 *
 * <p>{@code valueClass} 에는 원시 클래스({@code double.class})와 래퍼 클래스({@code Double.class})
 * 어느 쪽을 넘겨도 동작한다. 생성자 조회 시 원시/래퍼 양방향 폴백을 수행하기 때문이다.
 *
 * @param <T> 생성 대상 타입
 * @param <A> value 의 타입
 */
public class SimpleFactory<T, A> implements SkillFactory<T> {

    private static final String VALUE = "value";

    private final String id;
    private final String valueName;
    private final Class<T> factoryClass;
    private final Class<A> valueClass;
    private final A defaultValue;

    public SimpleFactory(String id, String valueName, Class<T> factoryClass, Class<A> valueClass, A defaultValue) {
        this.id = id;
        this.valueName = valueName;
        this.factoryClass = factoryClass;
        this.valueClass = valueClass;
        this.defaultValue = defaultValue;
    }

    public SimpleFactory(String id, String valueName, Class<T> factoryClass, Class<A> valueClass) {
        this(id, valueName, factoryClass, valueClass, null);
    }

    public SimpleFactory(String id, Class<T> factoryClass, Class<A> valueClass, A defaultValue) {
        this(id, VALUE, factoryClass, valueClass, defaultValue);
    }

    public SimpleFactory(String id, Class<T> factoryClass, Class<A> valueClass) {
        this(id, VALUE, factoryClass, valueClass, null);
    }

    @Override
    public T create(ConfigurationSection section) {
        A value = resolveValue(section);

        if (value == null) {
            if (defaultValue == null) {
                throw new IllegalArgumentException(String.format("%s is not a valid value", valueName));
            }
            value = defaultValue;
        }

        try {
            return resolveConstructor().newInstance(value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Failed to construct " + factoryClass.getName() + " from '" + valueName + "' (value type: "
                            + valueClass.getName() + ")", e);
        }
    }

    // ---- value 해석 ----

    @SuppressWarnings("unchecked")
    private A resolveValue(ConfigurationSection section) {
        if (valueClass.isEnum()) {
            return getEnumValue(section);
        }

        Object raw = section.get(valueName);
        if (raw == null) {
            return null;
        }

        Class<?> wrapper = primitiveToWrapper(valueClass);

        // YAML 숫자 리터럴 형태(5 vs 5.0)에 상관없이 목표 타입으로 강제변환
        if (raw instanceof Number n) {
            if (wrapper == Integer.class) return (A) Integer.valueOf(n.intValue());
            if (wrapper == Double.class)  return (A) Double.valueOf(n.doubleValue());
            if (wrapper == Long.class)    return (A) Long.valueOf(n.longValue());
            if (wrapper == Float.class)   return (A) Float.valueOf(n.floatValue());
            if (wrapper == Short.class)   return (A) Short.valueOf(n.shortValue());
            if (wrapper == Byte.class)    return (A) Byte.valueOf(n.byteValue());
        }

        return wrapper.isInstance(raw) ? (A) raw : null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private A getEnumValue(ConfigurationSection section) {
        String raw = section.getString(valueName);
        if (raw == null) {
            return null;
        }
        // valueClass 는 isEnum() 으로 이미 검증됨. raw Class 로 캐스팅해 valueOf 추론 충돌 회피.
        return (A) Enum.valueOf((Class) valueClass, raw);
    }

    // ---- 생성자 해석 (원시/래퍼 양방향 폴백) ----

    private Constructor<T> resolveConstructor() throws NoSuchMethodException {
        try {
            return factoryClass.getConstructor(valueClass);
        } catch (NoSuchMethodException primary) {
            Class<?> alt = valueClass.isPrimitive()
                    ? primitiveToWrapper(valueClass)
                    : wrapperToPrimitive(valueClass);
            if (alt != null && alt != valueClass) {
                try {
                    return factoryClass.getConstructor(alt);
                } catch (NoSuchMethodException ignored) {
                    // 폴백도 실패 -> 원래 예외를 던짐
                }
            }
            throw primary;
        }
    }

    // ---- 원시/래퍼 매핑 ----

    private static Class<?> primitiveToWrapper(Class<?> clazz) {
        if (clazz == boolean.class) return Boolean.class;
        if (clazz == byte.class)    return Byte.class;
        if (clazz == char.class)    return Character.class;
        if (clazz == double.class)  return Double.class;
        if (clazz == float.class)   return Float.class;
        if (clazz == int.class)     return Integer.class;
        if (clazz == long.class)    return Long.class;
        if (clazz == short.class)   return Short.class;
        return clazz;
    }

    private static Class<?> wrapperToPrimitive(Class<?> clazz) {
        if (clazz == Boolean.class)   return boolean.class;
        if (clazz == Byte.class)      return byte.class;
        if (clazz == Character.class) return char.class;
        if (clazz == Double.class)    return double.class;
        if (clazz == Float.class)     return float.class;
        if (clazz == Integer.class)   return int.class;
        if (clazz == Long.class)      return long.class;
        if (clazz == Short.class)     return short.class;
        return null;
    }

    // ---- getters ----

    @Override
    public String getID() {
        return id;
    }

    public String getValueName() {
        return valueName;
    }

    public Class<T> getFactoryClass() {
        return factoryClass;
    }

    public Class<A> getValueClass() {
        return valueClass;
    }

    public @Nullable A getDefaultValue() {
        return defaultValue;
    }
}