package org.red.minecraft.uw.core.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.attribute.stat.Stat;
import org.red.minecraft.uw.core.player.PlayerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StatCommand extends Command {
    private static final List<String> ACTIONS = List.of("set", "get", "add", "apply");

    @Override
    public @NotNull String getName() {
        return "stat";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull String s, String[] strings) {
        if (strings.length < 2) {
            commandSender.sendMessage("사용법: /stat <set|get|add|apply> <player> ~");
            return true;
        }

        String action = strings[0];
        String playerName = strings[1];
        String statType = strings.length > 2 ? strings[2] : null;

        A_Player target = CommediaDellarte.getAPlayer(playerName);
        if (target == null) {
            commandSender.sendMessage("Player " + playerName + " doesn't exist");
            return false;
        }

        PlayerHelper helper = new PlayerHelper(target);

        // apply는 statType/value가 필요 없음
        if (action.equals("apply")) {
            helper.applyStatToAttribute();
            commandSender.sendMessage(playerName + "의 스탯이 어트리뷰트에 적용되었습니다.");
            return true;
        }

        if (strings.length < 3) {
            commandSender.sendMessage("사용법: /stat <set|get|add> <player> [statType] [value]");
            return true;
        }

        // get을 제외하면 value가 필요함
        Integer value = null;
        if (!action.equals("get")) {
            if (strings.length < 4) {
                commandSender.sendMessage("값을 입력해야 합니다.");
                return true;
            }
            try {
                value = Integer.parseInt(strings[3]);
            } catch (NumberFormatException e) {
                commandSender.sendMessage("잘못된 숫자입니다: " + strings[3]);
                return true;
            }
        }

        boolean isStatPoint = statType.equalsIgnoreCase("statPoint");

        switch (action) {
            case "set" -> {
                if (isStatPoint) {
                    helper.setStatPoint(value);
                } else {
                    Stat stat = Stat.name(statType);
                    if (stat == null) {
                        commandSender.sendMessage("알 수 없는 Stat입니다: " + statType);
                        return true;
                    }
                    helper.setStatValue(stat, value);
                }
            }
            case "get" -> {
                if (isStatPoint) {
                    commandSender.sendMessage(String.format("%s StatPoint: %d", playerName, helper.getStatPoint()));
                } else {
                    Stat stat = Stat.name(statType);
                    if (stat == null) {
                        commandSender.sendMessage("알 수 없는 Stat입니다: " + statType);
                        return true;
                    }
                    commandSender.sendMessage(String.format("%s Stat %s: %d", playerName, statType, helper.getStatValue(stat)));
                }
            }
            case "add" -> {
                if (isStatPoint) {
                    helper.addStatPoint(value);
                } else {
                    Stat stat = Stat.name(statType);
                    if (stat == null) {
                        commandSender.sendMessage("알 수 없는 Stat입니다: " + statType);
                        return true;
                    }
                    helper.setStatValue(stat, helper.getStatValue(stat) + value);
                }
            }
            default -> commandSender.sendMessage("알 수 없는 액션입니다: " + action);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull String s, String[] strings) {
        if (strings.length == 1) {
            return filterByPrefix(ACTIONS, strings[0]);
        }

        if (strings.length == 2) {
            List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            return filterByPrefix(playerNames, strings[1]);
        }

        if (strings.length == 3) {
            List<String> statTypes = new ArrayList<>(Stat.statKeys()); // Stat 전체 이름 목록 반환 메서드 가정
            statTypes.add("statPoint");
            return filterByPrefix(statTypes, strings[2]);
        }

        if (strings.length == 4 && !strings[0].equalsIgnoreCase("get")) {
            return List.of("0", "1", "5", "10");
        }

        return List.of();
    }

    private List<String> filterByPrefix(List<String> candidates, String prefix) {
        String lower = prefix.toLowerCase();
        return candidates.stream()
                .filter(c -> c.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }
}
