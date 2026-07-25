package org.red.minecraft.uw.core.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.skill.craft.SkillCraftGUI;
import org.red.minecraft.uw.core.skill.slot.PlayerSkillManager;

import java.util.List;

/**
 * 스킬 제작 GUI 열기: /skillcraft <스킬이름>
 * todo 스킬 이름 입력 방식(명령어 인자)은 임시 — GUI 내 입력 방식 확정 시 교체
 */
public class SkillCraftCommand extends Command {

    @Override
    public @NotNull String getName() {
        return "skillcraft";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull String s, String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }

        if (strings.length < 1) {
            commandSender.sendMessage("사용법: /skillcraft <스킬이름>");
            return true;
        }

        String skillName = strings[0];
        A_Player aPlayer = CommediaDellarte.getAPlayer(player);

        if (PlayerSkillManager.hasSkill(aPlayer, skillName)) {
            commandSender.sendMessage("이미 존재하는 스킬 이름입니다: " + skillName);
            return true;
        }

        new SkillCraftGUI(aPlayer, skillName).open();
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull String s, String[] strings) {
        return List.of();
    }
}
