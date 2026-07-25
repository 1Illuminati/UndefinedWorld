package org.red.minecraft.uw.core.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.skill.slot.PlayerSkillManager;
import org.red.minecraft.uw.core.skill.slot.SkillSlot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 스킬 관리 명령어 (테스트용):
 *   /skill list                       — 보유 스킬 목록
 *   /skill slots                      — 슬롯 장착 현황
 *   /skill equip <슬롯> <스킬이름>     — 슬롯에 장착
 *   /skill unequip <슬롯>             — 슬롯 해제
 *   /skill cast <스킬이름>            — 직접 시전 (무기 조건 없이 테스트)
 */
public class SkillCommand extends Command {

    private static final List<String> ACTIONS = List.of("list", "slots", "equip", "unequip", "cast");

    @Override
    public @NotNull String getName() {
        return "skill";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull String s, String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }

        if (strings.length < 1) {
            commandSender.sendMessage("사용법: /skill <list|slots|equip|unequip|cast> ~");
            return true;
        }

        A_Player aPlayer = CommediaDellarte.getAPlayer(player);

        switch (strings[0].toLowerCase()) {
            case "list" -> commandSender.sendMessage("보유 스킬: " + String.join(", ", PlayerSkillManager.getSkillNames(aPlayer)));
            case "slots" -> {
                for (SkillSlot slot : SkillSlot.values()) {
                    String equipped = PlayerSkillManager.getEquipped(aPlayer, slot);
                    commandSender.sendMessage(String.format("%s(%s): %s", slot.krName, slot.name(), equipped == null ? "-" : equipped));
                }
            }
            case "equip" -> {
                if (strings.length < 3) {
                    commandSender.sendMessage("사용법: /skill equip <슬롯> <스킬이름>");
                    return true;
                }
                SkillSlot slot = SkillSlot.byName(strings[1]);
                if (slot == null) {
                    commandSender.sendMessage("알 수 없는 슬롯입니다: " + strings[1]);
                    return true;
                }
                if (!PlayerSkillManager.hasSkill(aPlayer, strings[2])) {
                    commandSender.sendMessage("보유하지 않은 스킬입니다: " + strings[2]);
                    return true;
                }
                PlayerSkillManager.equip(aPlayer, slot, strings[2]);
                commandSender.sendMessage(String.format("%s 슬롯에 [%s] 장착 완료", slot.krName, strings[2]));
            }
            case "unequip" -> {
                if (strings.length < 2) {
                    commandSender.sendMessage("사용법: /skill unequip <슬롯>");
                    return true;
                }
                SkillSlot slot = SkillSlot.byName(strings[1]);
                if (slot == null) {
                    commandSender.sendMessage("알 수 없는 슬롯입니다: " + strings[1]);
                    return true;
                }
                PlayerSkillManager.unequip(aPlayer, slot);
                commandSender.sendMessage(slot.krName + " 슬롯 해제 완료");
            }
            case "cast" -> {
                if (strings.length < 2) {
                    commandSender.sendMessage("사용법: /skill cast <스킬이름>");
                    return true;
                }
                PlayerSkillManager.castByName(aPlayer, strings[1]);
            }
            default -> commandSender.sendMessage("알 수 없는 액션입니다: " + strings[0]);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull String s, String[] strings) {
        if (strings.length == 1) return filterByPrefix(ACTIONS, strings[0]);

        if (strings.length == 2 && (strings[0].equalsIgnoreCase("equip") || strings[0].equalsIgnoreCase("unequip"))) {
            return filterByPrefix(Arrays.stream(SkillSlot.values()).map(Enum::name).toList(), strings[1]);
        }

        boolean needSkillName = (strings.length == 3 && strings[0].equalsIgnoreCase("equip"))
                || (strings.length == 2 && strings[0].equalsIgnoreCase("cast"));
        if (needSkillName && commandSender instanceof Player player) {
            List<String> names = new ArrayList<>(PlayerSkillManager.getSkillNames(CommediaDellarte.getAPlayer(player)));
            return filterByPrefix(names, strings[strings.length - 1]);
        }

        return List.of();
    }

    private List<String> filterByPrefix(List<String> candidates, String prefix) {
        String lower = prefix.toLowerCase();
        return candidates.stream().filter(c -> c.toLowerCase().startsWith(lower)).toList();
    }
}
