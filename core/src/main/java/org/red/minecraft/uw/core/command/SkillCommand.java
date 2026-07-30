package org.red.minecraft.uw.core.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.skill.SkillDebugManager;
import org.red.minecraft.uw.core.skill.craft.SkillCraftGUI;
import org.red.minecraft.uw.core.skill.craft.SkillItem;
import org.red.minecraft.uw.core.skill.slot.PlayerSkillManager;
import org.red.minecraft.uw.core.skill.slot.SkillEquipGUI;
import org.red.minecraft.uw.core.skill.slot.SkillSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 스킬 명령어: (§2.10 확정 — /skillcraft 를 /skill craft 로 통합, /skill unequip 폐지)
 * <pre>
 *   /skill list            — 인벤토리에 있는 스킬 아이템 목록
 *   /skill slots           — 슬롯 장착 현황 (텍스트)
 *   /skill equip           — 스킬 장착 GUI (장착/해제 모두 여기서 한다)
 *   /skill craft &lt;이름&gt;    — 스킬 제작 GUI
 *   /skill cast            — 주손의 스킬 아이템을 직접 시전 (op 전용 테스트)
 *   /skill debug           — 스킬 디버그 로그 토글 (op 전용)
 * </pre>
 *
 * <p>스킬 구성은 아이템 PDC가 SSOT이므로 이름으로 스킬을 지정하는 인자는 없다. (§2.6)
 * 슬롯 지정 인자도 없다 — 장착/해제는 슬롯이 아이템 실물을 보관하는 GUI에서만 한다(§2.10).
 */
public class SkillCommand extends Command {

    private static final List<String> ACTIONS = List.of("list", "slots", "equip", "craft", "cast", "debug");

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
            commandSender.sendMessage("사용법: /skill <list|slots|equip|craft <이름>|cast|debug>");
            return true;
        }

        A_Player aPlayer = CommediaDellarte.getAPlayer(player);

        // Locale.ROOT 고정 — 기본 로케일이 터키어면 'I'가 'ı'로 바뀌어 "list" 등이 매칭되지 않는다
        switch (strings[0].toLowerCase(Locale.ROOT)) {
            case "list" -> listSkillItems(commandSender, player);
            case "slots" -> showSlots(commandSender, aPlayer);
            case "equip" -> openEquipGui(commandSender, aPlayer);
            case "craft" -> openCraftGui(commandSender, aPlayer, strings);
            case "cast" -> cast(commandSender, player, aPlayer);
            case "debug" -> toggleDebug(commandSender, aPlayer);
            // 폐지된 액션 안내 (§2.10-8). ACTIONS에는 넣지 않아 탭완성에는 뜨지 않는다 —
            // 쓰던 사람이 "알 수 없는 액션"만 보고 스킬이 슬롯에 갇혔다고 오해하지 않게 한다
            case "unequip" -> commandSender.sendMessage("해제는 /skill equip 으로 여는 장착 GUI에서 합니다.");
            default -> commandSender.sendMessage("알 수 없는 액션입니다: " + strings[0]);
        }

        return true;
    }

    /** 인벤토리에 들고 있는 스킬 아이템 목록 (슬롯에 보관된 것은 /skill slots 로 확인한다) */
    private void listSkillItems(CommandSender sender, Player player) {
        List<String> names = new ArrayList<>();

        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (!SkillItem.isSkillItem(stack)) continue;
            names.add(SkillItem.getSkillName(stack));
        }

        if (names.isEmpty()) {
            sender.sendMessage("가지고 있는 스킬 아이템이 없습니다. /skill craft <이름> 으로 제작하세요.");
            return;
        }
        sender.sendMessage(String.format("스킬 아이템(%d): %s", names.size(), String.join(", ", names)));
    }

    /** 슬롯 장착 현황 (GUI를 열지 않고 확인하는 용도) */
    private void showSlots(CommandSender sender, A_Player aPlayer) {
        sender.sendMessage("스킬 슬롯 장착 현황 (무기를 주손에 들고 사용):");

        for (SkillSlot slot : SkillSlot.values()) {
            String equipped = PlayerSkillManager.getEquippedName(aPlayer, slot);
            sender.sendMessage(String.format(" - %s [%s]: %s",
                    slot.krName, slot.name(), equipped == null ? "(미장착)" : equipped));
        }
        sender.sendMessage("장착/해제는 /skill equip 로 엽니다.");
    }

    /**
     * 스킬 장착 GUI를 연다. (§2.10-6: /skill equip 은 인자 없이 GUI를 연다)
     * 이 명령은 아이템을 미리 옮기지 않으므로 열리지 않아도 잃는 것은 없다 —
     * 다만 조용히 아무 일도 없으면 원인을 알 수 없어 실패를 알린다.
     */
    private void openEquipGui(CommandSender sender, A_Player aPlayer) {
        if (!new SkillEquipGUI(aPlayer).open())
            sender.sendMessage("스킬 장착 GUI를 열지 못했습니다."); //todo 문구 확정
    }

    /**
     * 스킬 제작 GUI를 연다. (§2.10-7: /skillcraft 통합)
     * 이름에 공백을 쓸 수 없으므로 인자는 정확히 1개여야 한다 —
     * 여러 개를 받아 첫 번째만 쓰면 사용자가 의도한 이름과 조용히 달라진다.
     * todo 스킬 이름 입력 방식(명령어 인자)은 임시 — GUI 내 입력 방식 확정 시 교체
     */
    private void openCraftGui(CommandSender sender, A_Player aPlayer, String[] strings) {
        if (strings.length < 2) {
            sender.sendMessage("사용법: /skill craft <스킬이름>");
            return;
        }
        if (strings.length > 2) {
            sender.sendMessage("스킬 이름에는 공백을 사용할 수 없습니다.");
            return;
        }

        String skillName = strings[1];

        // 표시 길이/문자 제약을 GUI를 열기 전에 확인한다 (제작 확인 시점에 걸러지면 헛수고가 된다)
        if (!PlayerSkillManager.isValidName(skillName)) {
            sender.sendMessage(PlayerSkillManager.nameRuleMessage() + " 입력: " + skillName);
            return;
        }

        // 스킬 구성은 아이템 PDC가 SSOT이므로 이름 중복 검사는 하지 않는다 (§2.6: 스킬 개수 상한 없음)
        if (!new SkillCraftGUI(aPlayer, skillName).open())
            sender.sendMessage("스킬 제작 GUI를 열지 못했습니다."); //todo 문구 확정
    }

    /**
     * 주손의 스킬 아이템을 직접 시전한다.
     * 확정된 정상 발동 경로는 "무기를 들고 슬롯 클릭"이다(SkillCastListener).
     * 이 서브커맨드는 그 조건을 우회하는 테스트 수단이므로 op 로 제한한다. (§2.6 확정)
     */
    private void cast(CommandSender sender, Player player, A_Player aPlayer) {
        if (!sender.isOp()) {
            sender.sendMessage("테스트용 명령입니다. 스킬은 무기를 들고 슬롯 클릭으로 사용하세요.");
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!SkillItem.isSkillItem(hand)) {
            sender.sendMessage("주손에 스킬 아이템을 들고 사용하세요.");
            return;
        }
        PlayerSkillManager.castItem(aPlayer, hand);
    }

    /** 스킬 디버그 로그 토글. (§2.10 추가개발 2 — op 전용) */
    private void toggleDebug(CommandSender sender, A_Player aPlayer) {
        if (!sender.isOp()) {
            sender.sendMessage("디버그용 명령입니다.");
            return;
        }

        boolean enabled = SkillDebugManager.toggle(aPlayer);
        sender.sendMessage("스킬 디버그 모드: " + (enabled ? "켜짐" : "꺼짐")); //todo 문구 확정
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull String s, String[] strings) {
        // 스킬 이름은 자유 입력이고 슬롯 인자는 사라졌으므로 완성 대상은 액션뿐이다
        if (strings.length == 1) return filterByPrefix(ACTIONS, strings[0]);

        return List.of();
    }

    private List<String> filterByPrefix(List<String> candidates, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return candidates.stream().filter(c -> c.toLowerCase(Locale.ROOT).startsWith(lower)).toList();
    }
}
