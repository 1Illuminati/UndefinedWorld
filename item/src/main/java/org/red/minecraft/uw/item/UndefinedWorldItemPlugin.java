package org.red.minecraft.uw.item;

import com.nexomc.nexo.api.events.NexoMechanicsRegisteredEvent;
import com.nexomc.nexo.mechanics.MechanicsManager;
import com.nexomc.nexo.utils.logs.Logs;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.item.command.UGiveCommand;
import org.red.minecraft.uw.item.mechanic.factory.U_ItemMechanicFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

public class UndefinedWorldItemPlugin extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        new UGiveCommand().register(this);
        this.installTestItems();
    }

    /**
     * 테스트 아이템 정의(test_items.yml)를 Nexo items 폴더에 설치한다.
     * 이미 존재하면 덮어쓰지 않는다. (테스트용 — 파일 삭제로 제거 가능)
     */
    private void installTestItems() {
        File nexoItemsDir = new File(this.getDataFolder().getParentFile(), "Nexo/items");
        if (!nexoItemsDir.isDirectory()) {
            this.getLogger().warning("Nexo items 폴더를 찾을 수 없어 테스트 아이템을 설치하지 못했습니다.");
            return;
        }

        File target = new File(nexoItemsDir, "uw_test_items.yml");
        if (target.exists()) return;

        try (InputStream in = this.getResource("test_items.yml")) {
            if (in == null) return;
            Files.copy(in, target.toPath());
            this.getLogger().info("테스트 아이템 설치 완료: " + target.getPath());
        } catch (Exception e) {
            this.getLogger().warning("테스트 아이템 설치 실패: " + e.getMessage());
        }
    }

    @EventHandler
    public void nRegister(NexoMechanicsRegisteredEvent event) {
        U_ItemMechanicFactory factory = new U_ItemMechanicFactory();
        MechanicsManager.INSTANCE.registerMechanicFactory(factory, true);
        UndefinedWorldCore.registerModule(new ItemModule(factory));
        Logs.logInfo("Registered Mechanic!");
    }
}
