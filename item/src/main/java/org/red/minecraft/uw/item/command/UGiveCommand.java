package org.red.minecraft.uw.item.command;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.uw.core.command.Command;

import java.util.List;
import java.util.Optional;

/**
 * Nexo 아이템 지급 명령어 (테스트용):
 *   /ugive <아이템ID> [수량]
 */
public class UGiveCommand extends Command {

    @Override
    public @NotNull String getName() {
        return "ugive";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull String s, String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }

        if (strings.length < 1) {
            commandSender.sendMessage("사용법: /ugive <아이템ID> [수량]");
            return true;
        }

        Optional<ItemBuilder> builder = NexoItems.optionalItemFromId(strings[0]);
        if (builder.isEmpty()) {
            commandSender.sendMessage("존재하지 않는 아이템입니다: " + strings[0]);
            return true;
        }

        int amount = 1;
        if (strings.length >= 2) {
            try {
                amount = Math.max(1, Integer.parseInt(strings[1]));
            } catch (NumberFormatException e) {
                commandSender.sendMessage("잘못된 수량입니다: " + strings[1]);
                return true;
            }
        }

        ItemStack stack = builder.get().build();
        stack.setAmount(amount);
        CommediaDellarte.getAPlayer(player).addItemNature(stack);
        commandSender.sendMessage(String.format("[%s] x%d 지급 완료", strings[0], amount));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull String s, String[] strings) {
        if (strings.length == 1) {
            String lower = strings[0].toLowerCase();
            return NexoItems.itemNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(lower))
                    .limit(50)
                    .toList();
        }

        if (strings.length == 2) return List.of("1", "16", "64");

        return List.of();
    }
}
