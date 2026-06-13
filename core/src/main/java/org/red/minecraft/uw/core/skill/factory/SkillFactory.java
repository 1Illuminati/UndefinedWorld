package org.red.minecraft.uw.core.skill.factory;

import org.bukkit.configuration.ConfigurationSection;
import org.red.minecraft.uw.core.util.HasID;

/**
 * Nexo 호환용
 * @param <T>
 */
public interface SkillFactory<T> extends HasID {

    T create(ConfigurationSection section);
}
