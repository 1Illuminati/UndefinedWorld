package org.red.minecraft.uw.core;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.red.minecraft.uw.core.attribute.stat.Stat;
import org.red.minecraft.uw.core.combat.buff.BuffLifecycleListener;
import org.red.minecraft.uw.core.command.EquipmentCommand;
import org.red.minecraft.uw.core.command.SkillCommand;
import org.red.minecraft.uw.core.command.SkillCraftCommand;
import org.red.minecraft.uw.core.command.StatCommand;
import org.red.minecraft.uw.core.player.ResourceRegenTask;
import org.red.minecraft.uw.core.player.equipment.EquipmentGUIListener;
import org.red.minecraft.uw.core.player.equipment.WeaponScanTask;
import org.red.minecraft.uw.core.skill.CastingMoveListener;
import org.red.minecraft.uw.core.skill.SkillEngine;
import org.red.minecraft.uw.core.skill.craft.SkillCraftGUIListener;
import org.red.minecraft.uw.core.skill.slot.SkillCastListener;
import org.red.minecraft.uw.core.util.papi.U_PapiPlayer;

import java.util.Objects;

public class UndefinedWorldCorePlugin extends JavaPlugin {
    public static void sendLog(Object message) {
        instance.getLogger().info(message.toString());
    }

    public static UndefinedWorldCorePlugin instance;
    public static FileConfiguration config;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        config = this.getConfig();

        // T20 확정: Gear 아이템(Nexo)이 로드 시 팩토리를 참조하므로 onEnable 초입에서 등록
        SkillEngine.setFactories();

        Stat.configSet(Objects.requireNonNull(config.getConfigurationSection("StatSetting")));
        new StatCommand().register(this);
        new EquipmentCommand().register(this);
        new SkillCommand().register(this);
        new SkillCraftCommand().register(this);
        new U_PapiPlayer().register();
        new BuffLifecycleListener().register(this);
        new CastingMoveListener().register(this);
        new EquipmentGUIListener().register(this);
        new SkillCraftGUIListener().register(this);
        new SkillCastListener().register(this);
        ResourceRegenTask.start(this);
        WeaponScanTask.start(this);
        Bukkit.getScheduler().runTaskLater(this, () -> {

        }, 1);
    }
}
