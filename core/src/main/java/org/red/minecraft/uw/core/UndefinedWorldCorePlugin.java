package org.red.minecraft.uw.core;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;
import org.red.minecraft.uw.core.attribute.stat.Stat;
import org.red.minecraft.uw.core.combat.buff.BuffLifecycleListener;
import org.red.minecraft.uw.core.command.EquipmentCommand;
import org.red.minecraft.uw.core.command.SkillCommand;
import org.red.minecraft.uw.core.command.StatCommand;
import org.red.minecraft.uw.core.player.ResourceRegenTask;
import org.red.minecraft.uw.core.gui.U_Gui;
import org.red.minecraft.uw.core.player.equipment.EquipmentGUIListener;
import org.red.minecraft.uw.core.player.equipment.WeaponScanTask;
import org.red.minecraft.uw.core.skill.CastingLifecycleListener;
import org.red.minecraft.uw.core.skill.CastingManager;
import org.red.minecraft.uw.core.skill.CastingMoveListener;
import org.red.minecraft.uw.core.skill.SilenceCastListener;
import org.red.minecraft.uw.core.skill.SkillEngine;
import org.red.minecraft.uw.core.skill.craft.SkillCraftGUIListener;
import org.red.minecraft.uw.core.skill.slot.SkillCastListener;
import org.red.minecraft.uw.core.skill.slot.SkillEquipGUIListener;
import org.red.minecraft.uw.core.util.papi.U_PapiPlayer;

import java.util.List;
import java.util.Objects;

public class UndefinedWorldCorePlugin extends JavaPlugin {
    public static void sendLog(Object message) {
        // instance 설정 전(클래스 초기화 등)에 호출되어도 로그 때문에 죽지 않도록 방어한다
        if (instance == null) {
            Bukkit.getLogger().info("[UndefinedWorldCore] " + message);
            return;
        }
        instance.getLogger().info(String.valueOf(message));
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
        new U_PapiPlayer().register();
        new BuffLifecycleListener().register(this);
        new CastingMoveListener().register(this);
        new CastingLifecycleListener().register(this);
        new SilenceCastListener().register(this);
        new EquipmentGUIListener().register(this);
        new SkillCraftGUIListener().register(this);
        new SkillCastListener().register(this);
        new SkillEquipGUIListener().register(this);
        ResourceRegenTask.start(this);
        WeaponScanTask.start(this);
        Bukkit.getScheduler().runTaskLater(this, () -> {

        }, 1);
    }

    @Override
    public void onDisable() {
        closeOpenGuis();

        // 진행 중이던 캐스팅 정리. 비활성화 시 스케줄러가 태스크를 취소하면서 완료 콜백이 돌지 않아
        // casting 맵 엔트리가 그대로 남는다.
        CastingManager.shutdown();

        // 활성 버프 태스크 종료 및 모듈 등록 해제 (재활성화 시 이전 plugin 인스턴스 참조 방지)
        UndefinedWorldCore.shutdown();
    }

    /**
     * 플러그인 비활성화 시 열려 있는 커스텀 GUI를 정리한다.
     * 정리하지 않으면 GUI에 올려둔 기어/장비가 그대로 사라진다(리로드·종료 시 아이템 소실).
     *
     * 닫기로 InventoryCloseEvent 가 발생하면 각 GUI 가 처리하지만, 비활성화 시점의 이벤트 전달은
     * 보장되지 않으므로 정리를 직접 한 번 더 호출한다. handleClose() 는 멱등이다.
     *
     * 3종 GUI 가 모두 U_Gui 를 상속하므로 개별 분기 없이 공통 진입점 하나로 처리한다
     * (GUI 마다 다른 정리 메서드를 부르면 새 GUI 가 추가될 때 여기에 누락이 생긴다).
     * enchant 모듈의 GUI 는 그쪽 플러그인이 자체 onDisable 에서 정리한다.
     */
    private void closeOpenGuis() {
        for (Player player : List.copyOf(Bukkit.getOnlinePlayers())) {
            InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
            if (!(holder instanceof U_Gui gui)) continue;

            try {
                player.closeInventory();
                gui.handleClose();
            } catch (Exception e) {
                sendLog("GUI 정리 실패: " + player.getName() + " / " + e);
            }
        }
    }
}
