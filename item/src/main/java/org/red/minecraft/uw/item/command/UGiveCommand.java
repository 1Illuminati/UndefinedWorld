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

    /** 플레이어 인벤토리 칸 수 (지급 상한 계산 기준) */
    private static final int INVENTORY_SLOTS = 36;

    /**
     * 1회 지급 절대 상한. (§2.6 확정: 1000)
     * 상한이 없으면 /ugive item 2147483647 로 서버를 멈출 수 있고,
     * 겹치지 않는 아이템(무기/갑옷)은 남는 수량이 전부 바닥에 드롭되어 엔티티 폭탄이 된다.
     */
    private static final int MAX_AMOUNT = 1000;

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
                amount = Integer.parseInt(strings[1]);
            } catch (NumberFormatException e) {
                commandSender.sendMessage("잘못된 수량입니다: " + strings[1]);
                return true;
            }
        }

        ItemStack stack = builder.get().build();

        // 인벤토리를 가득 채울 수 있는 양까지만 허용 (겹치지 않는 아이템은 상한도 그만큼 낮아진다)
        int max = Math.min(MAX_AMOUNT, stack.getMaxStackSize() * INVENTORY_SLOTS);
        if (amount < 1 || amount > max) {
            commandSender.sendMessage(String.format("수량은 1 ~ %d 사이여야 합니다.", max));
            return true;
        }

        stack.setAmount(amount);
        CommediaDellarte.getAPlayer(player).addItemNature(stack);
        commandSender.sendMessage(String.format("[%s] x%d 지급 완료", strings[0], amount));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull String s, String[] strings) {
        if (strings.length == 1) {
            String lower = strings[0].toLowerCase();
            // 정렬 후 자르지 않으면 매번 다른 후보 50개가 잘려 나와 원하는 아이템을 찾기 어렵다
            return NexoItems.itemNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(lower))
                    .sorted()
                    .limit(50)
                    .toList();
        }

        if (strings.length == 2) return List.of("1", "16", "64");

        return List.of();
    }
}
