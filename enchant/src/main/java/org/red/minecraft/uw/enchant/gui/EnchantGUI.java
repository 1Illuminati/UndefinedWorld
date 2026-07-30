package org.red.minecraft.uw.enchant.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.gui.U_Gui;
import org.red.minecraft.uw.enchant.UndefinedWorldEnchantPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 인챈트 강화 GUI. (기존 모루와 별개의 커스텀 강화 시스템)
 * 장비칸 + 인챈트북칸 + 강화시작칸 + 결과칸으로 구성된다.
 *
 * 클릭 차단 정책(더블클릭/숫자키/오프핸드/소유자 검사)은 U_Gui가 담당하고,
 * 여기서는 각 기능 슬롯의 내용물 검증만 처리한다.
 *
 * 확률 규칙 (사용자 확정):
 * - 성공률 = 100% - (장비 기존 인챈트 개수 × 20%) - ((인챈트북 내 인챈트 개수 - 1) × 10%), 0% 미만 불가
 * - 강화 후 결과 인챈트 총개수(기존∪신규) ≥ 4 부터 파괴확률 존재: 실패시 (결과 총개수 - 3) × 10%
 * - 강화 비용은 없다 (§2.6 확정: 인챈트북 1권 소모만)
 *
 * todo GUI 타이틀/디자인, 강화 결과 메시지 문구는 사용자 확정 필요
 */
public class EnchantGUI extends U_Gui {

    private static final int SIZE = 54;
    private static final int ITEM_SLOT = 29;
    private static final int BOOK_SLOT = 31;
    private static final int START_SLOT = 33;
    private static final int RESULT_SLOT = 35;
    /** 각 기능 슬롯 바로 위(필러 영역)의 안내 아이템 위치 */
    private static final int ITEM_GUIDE_SLOT = ITEM_SLOT - 9;
    private static final int BOOK_GUIDE_SLOT = BOOK_SLOT - 9;
    private static final int RESULT_GUIDE_SLOT = RESULT_SLOT - 9;
    /** 파괴 확률이 발생하기 시작하는 결과 인챈트 총개수 (사용자 확정 규칙) */
    private static final int DESTROY_THRESHOLD = 4;

    public EnchantGUI(A_Player player) {
        super(player, SIZE, "인챈트 강화"); //todo GUI 타이틀/디자인 사용자 확정 필요

        ItemStack filler = createFiller();
        for (int i = 0; i < SIZE; i++) {
            setItem(i, filler);
        }

        setItem(ITEM_SLOT, null);
        setItem(BOOK_SLOT, null);
        setItem(RESULT_SLOT, null);
        setItem(ITEM_GUIDE_SLOT, createGuide("장비 칸", List.of(
                Component.text("아래 칸에 강화할 장비를 올리세요."),
                Component.text("인챈트가 가능한 장비 1개만 올라갑니다.")
        )));
        setItem(BOOK_GUIDE_SLOT, createGuide("인챈트북 칸", List.of(
                Component.text("아래 칸에 인챈트된 책을 올리세요."),
                Component.text("강화 시 1권만 소모됩니다.")
        )));
        setItem(RESULT_GUIDE_SLOT, createGuide("결과 칸", List.of(
                Component.text("강화 결과가 아래 칸에 나옵니다."),
                Component.text("결과를 비우지 않으면 다시 강화할 수 없습니다.")
        )));
        refreshStartButton();
    }

    // ── 클릭 처리 ──────────────────────────────────────

    @Override
    protected void onTopClick(InventoryClickEvent event, Player clicker) {
        int slot = event.getSlot();

        if (slot == START_SLOT) {
            event.setCancelled(true);
            attemptEnchant();
            return;
        }

        if (slot == RESULT_SLOT) {
            // 결과 칸은 강화 로직으로만 채워짐, 플레이어가 직접 넣는 것은 차단
            if (!event.getCursor().isEmpty()) {
                event.setCancelled(true);
                return;
            }
            scheduleRefresh(); // 결과를 꺼내면 다시 강화할 수 있으므로 버튼 갱신
            return;
        }

        if (slot == ITEM_SLOT) {
            ItemStack cursor = event.getCursor();
            if (!cursor.isEmpty() && !isEnchantableEquipment(cursor)) {
                event.setCancelled(true);
                return;
            }
            scheduleRefresh();
            return;
        }

        if (slot == BOOK_SLOT) {
            ItemStack cursor = event.getCursor();
            if (!cursor.isEmpty() && !isValidEnchantBook(cursor)) {
                event.setCancelled(true);
                return;
            }
            scheduleRefresh();
            return;
        }

        // 필러/안내 영역
        event.setCancelled(true);
    }

    @Override
    protected void onBottomClick(InventoryClickEvent event, Player clicker) {
        // 쉬프트클릭으로 GUI에 자동 이동하는 것은 슬롯별 검증이 불가능하므로 차단
        if (event.getClick().isShiftClick()) event.setCancelled(true);
    }

    @Override
    protected void onGuiClose() {
        returnAllItems();
    }

    /**
     * 플레이어가 올려둔 아이템과 결과물을 모두 반환한다.
     * 슬롯을 즉시 비우므로 여러 번 호출해도 중복 반환되지 않는다.
     */
    public void returnAllItems() {
        returnItem(ITEM_SLOT);
        returnItem(BOOK_SLOT);
        returnItem(RESULT_SLOT);
    }

    private void returnItem(int slot) {
        ItemStack stack = getItem(slot);
        if (stack == null || stack.isEmpty()) return;

        // getItem은 인벤토리 슬롯을 비추는 참조일 수 있으므로 복사해두고 슬롯을 먼저 비운다
        // (슬롯을 비운 뒤 반환해야 반환 도중 예외가 나도 GUI에 남아 복사되지 않는다)
        ItemStack returning = stack.clone();
        setItem(slot, null);
        getOwner().addItemNature(returning);
    }

    /** 조건8: 인챈트 가능한 장비만 인정, 책/인챈트된 책은 제외 */
    private boolean isEnchantableEquipment(ItemStack item) {
        if (item == null || item.isEmpty()) return false;
        Material type = item.getType();
        if (type == Material.BOOK || type == Material.ENCHANTED_BOOK) return false;

        for (Enchantment enchantment : RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)) {
            if (enchantment.canEnchantItem(item)) return true;
        }
        return false;
    }

    /** 바닐라 인챈트된 책만 인정 */
    private boolean isValidEnchantBook(ItemStack item) {
        if (item == null || item.isEmpty()) return false;
        if (item.getType() != Material.ENCHANTED_BOOK) return false;
        if (!(item.getItemMeta() instanceof EnchantmentStorageMeta meta)) return false;
        return meta.hasStoredEnchants();
    }

    /**
     * 강화 시작이 불가능한 이유. 가능하면 null.
     * 활성/비활성 판정과 실패 안내 메시지가 같은 기준을 쓰도록 한 곳에서 계산한다.
     * 결과칸 상태도 함께 본다 — 결과칸이 차 있는데 강화하면 기존 결과물이 덮어써져 사라진다.
     */
    @Nullable
    private String findBlockReason(@Nullable ItemStack equipment, @Nullable ItemStack book) {
        ItemStack result = getItem(RESULT_SLOT);
        if (result != null && !result.isEmpty()) return "결과 칸을 먼저 비워주세요.";

        if (!isEnchantableEquipment(equipment)) return "인챈트할 수 있는 장비를 올려주세요.";
        // 스택으로 올리면 한 번의 강화로 전체가 인챈트되어 복사와 같아지므로 1개만 허용한다
        if (equipment.getAmount() != 1) return "장비는 1개만 올릴 수 있습니다.";
        if (!isValidEnchantBook(book)) return "인챈트된 책을 올려주세요.";

        if (!canApply(equipment, book)) return "이 장비에 적용할 수 없거나 상충하는 인챈트입니다.";
        return null;
    }

    /**
     * 조건4: 책의 모든 인챈트가 장비에 적용 가능해야 하고,
     * 장비의 기존 인챈트(동일 종류 제외) 및 책 내부 인챈트끼리 상충이 없어야 강화시작 활성화.
     * 동일 종류 인챈트는 레벨 무관 항상 덮어쓰기 허용(사용자 확정).
     */
    private boolean canApply(ItemStack equipment, ItemStack book) {
        if (!isEnchantableEquipment(equipment) || !isValidEnchantBook(book)) return false;

        EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) book.getItemMeta();
        Map<Enchantment, Integer> bookEnchants = bookMeta.getStoredEnchants();

        ItemMeta equipmentMeta = equipment.getItemMeta();
        Map<Enchantment, Integer> existingEnchants = equipmentMeta.getEnchants();

        for (Enchantment enchant : bookEnchants.keySet()) {
            if (!enchant.canEnchantItem(equipment)) return false;

            for (Enchantment existing : existingEnchants.keySet()) {
                if (!existing.equals(enchant) && enchant.conflictsWith(existing)) return false;
            }

            for (Enchantment other : bookEnchants.keySet()) {
                if (!other.equals(enchant) && enchant.conflictsWith(other)) return false;
            }
        }
        return true;
    }

    /** 클릭 처리가 끝난 뒤(1틱 후) 강화 버튼을 갱신한다 */
    private void scheduleRefresh() {
        Bukkit.getScheduler().runTask(UndefinedWorldEnchantPlugin.instance, this::refreshStartButton);
    }

    /** 강화 버튼(활성 여부 + 확률 표기) 갱신. 닫힌 GUI에는 쓰지 않는다. */
    private void refreshStartButton() {
        if (isClosed()) return;
        setItem(START_SLOT, createStartButton(getItem(ITEM_SLOT), getItem(BOOK_SLOT)));
    }

    /**
     * 강화 시도: 성공/실패/파괴 판정 후 아이템칸·인챈트북칸을 비우고 결과칸에 결과를 채운다.
     * 인챈트북은 항상 1개 소모, 실패(비파괴)시 장비는 원본 그대로 결과칸에 반환된다.
     */
    private void attemptEnchant() {
        ItemStack equipment = getItem(ITEM_SLOT);
        ItemStack book = getItem(BOOK_SLOT);

        String reason = findBlockReason(equipment, book); // 비활성 상태에서의 클릭 방어 + 이유 안내
        if (reason != null) {
            getOwner().sendMessage(reason); //todo 문구 확정 필요
            refreshStartButton(); // 버튼 표기가 실제 상태와 어긋나 있었다면 여기서 맞춘다
            return;
        }

        ItemStack originalEquipment = equipment.clone();
        ItemStack resultEquipment = equipment.clone();
        ItemMeta resultMeta = resultEquipment.getItemMeta();
        Map<Enchantment, Integer> existingEnchants = new HashMap<>(resultMeta.getEnchants());

        EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) book.getItemMeta();
        Map<Enchantment, Integer> bookEnchants = bookMeta.getStoredEnchants();

        Rate rate = calcRate(existingEnchants.keySet(), bookEnchants.keySet());

        boolean success = ThreadLocalRandom.current().nextInt(100) < rate.successRate();

        consumeBook(book);
        setItem(ITEM_SLOT, null);

        if (success) {
            bookEnchants.forEach((enchant, level) -> resultMeta.addEnchant(enchant, level, true));
            resultEquipment.setItemMeta(resultMeta);
            setItem(RESULT_SLOT, resultEquipment);
            getOwner().sendMessage("강화에 성공했습니다."); //todo 문구 확정 필요
            refreshStartButton();
            return;
        }

        if (rate.destroyRate() > 0 && ThreadLocalRandom.current().nextInt(100) < rate.destroyRate()) {
            getOwner().sendMessage("강화에 실패하여 장비가 파괴되었습니다."); //todo 문구 확정 필요
            refreshStartButton();
            return;
        }

        setItem(RESULT_SLOT, originalEquipment);
        getOwner().sendMessage("강화에 실패했습니다."); //todo 문구 확정 필요
        refreshStartButton();
    }

    /**
     * 확률 계산 (사용자 확정 규칙 — 수치 변경 금지).
     * 성공률 = 100 - 기존인챈트수×20 - (책인챈트수-1)×10 (0 미만 불가)
     * 파괴율 = 결과 총개수 ≥ 4 일 때 (결과 총개수 - 3)×10, 그 미만은 0
     */
    private Rate calcRate(Set<Enchantment> existingTypes, Set<Enchantment> bookTypes) {
        int successRate = Math.max(0, 100 - existingTypes.size() * 20 - (bookTypes.size() - 1) * 10);

        Set<Enchantment> unionTypes = new HashSet<>(existingTypes);
        unionTypes.addAll(bookTypes);
        int resultTotalCount = unionTypes.size();

        int destroyRate = resultTotalCount >= DESTROY_THRESHOLD ? (resultTotalCount - 3) * 10 : 0;
        return new Rate(successRate, destroyRate, resultTotalCount);
    }

    private record Rate(int successRate, int destroyRate, int resultTotalCount) {}

    private void consumeBook(ItemStack book) {
        if (book.getAmount() <= 1) {
            setItem(BOOK_SLOT, null);
            return;
        }
        ItemStack remaining = book.clone();
        remaining.setAmount(book.getAmount() - 1);
        setItem(BOOK_SLOT, remaining);
    }

    private ItemStack createFiller() {
        return createNamed(Material.GRAY_STAINED_GLASS_PANE, Component.empty(), List.of());
    }

    private ItemStack createGuide(String name, List<Component> lore) {
        return createNamed(Material.PAPER, Component.text(name), lore); //todo 안내 아이콘/문구 사용자 확정 필요
    }

    /**
     * 강화 버튼. 비활성일 때도 필러와 구분되도록 다른 재질을 쓰고,
     * 성공률/파괴율을 lore에 표기해 확률을 모른 채 강화하지 않도록 한다.
     */
    private ItemStack createStartButton(@Nullable ItemStack equipment, @Nullable ItemStack book) {
        String reason = findBlockReason(equipment, book);
        boolean active = reason == null;

        List<Component> lore = new ArrayList<>();
        if (active) {
            Rate rate = calcRate(
                    equipment.getItemMeta().getEnchants().keySet(),
                    ((EnchantmentStorageMeta) book.getItemMeta()).getStoredEnchants().keySet());

            lore.add(Component.text(String.format("성공 확률: %d%%", rate.successRate())));
            lore.add(Component.text(String.format("실패 시 파괴 확률: %d%%", rate.destroyRate())));
            lore.add(Component.text(String.format("강화 후 인챈트 수: %d", rate.resultTotalCount())));
            lore.add(Component.text("실패해도 인챈트북은 1권 소모됩니다."));
            lore.add(Component.text("클릭하여 강화"));
        } else {
            lore.add(Component.text(reason));
        }

        return createNamed(active ? Material.LIME_CONCRETE : Material.RED_CONCRETE,
                Component.text("강화 시작"), lore);
    }

    private ItemStack createNamed(Material material, Component name, List<Component> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(name);
        if (!lore.isEmpty()) meta.lore(lore);
        stack.setItemMeta(meta);
        return stack;
    }
}
