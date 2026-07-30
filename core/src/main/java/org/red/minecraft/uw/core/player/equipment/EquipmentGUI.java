package org.red.minecraft.uw.core.player.equipment;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.gui.U_Gui;

import java.util.List;

/**
 * 장비 전용 GUI. (구조 결정 T19-2)
 * 마인크래프트 장비창을 사용하지 않는 완전 별도 GUI — 여기 올려두면 감지해서 EQUIPMENT 컨테이너를 재계산한다.
 * 장착해도 플레이어 갑옷창에는 추가되지 않는다.
 *
 * 클릭 차단 정책(더블클릭/숫자키/오프핸드/소유자 검사)은 U_Gui가 담당하고,
 * 여기서는 장비 슬롯 고유의 장착/해제/교체만 처리한다.
 *
 * 장비 슬롯 클릭은 바닐라 이동 규칙(스택 병합/부분 이동 등)을 쓰지 않고 이벤트를 취소한 뒤 직접 처리한다:
 * - 바닐라 병합이 일어나면 슬롯에 2개 이상이 쌓여 "1칸 1장비" 규칙이 깨진다
 * - 클릭 결과를 예측할 수 있어야 저장 시점을 클릭과 같은 틱으로 맞출 수 있다
 *   (1틱 뒤 저장 예약 방식은 그 사이 종료/강제퇴장 시 저장 데이터와 인벤토리가 어긋난다)
 *
 * 슬롯 표시 규칙:
 * - 장비가 없는 슬롯에는 슬롯 이름이 붙은 안내 아이템(placeholder)을 넣는다.
 * - 안내 아이템/필러는 PDC 표식(DECORATION_KEY)으로 식별하며 장비로 저장되지 않고 꺼낼 수도 없다.
 *
 * todo GUI 타이틀/색상/안내 문구는 사용자 확정 필요 (현재는 임시 표현)
 */
public class EquipmentGUI extends U_Gui {

    private static final int SIZE = 27;

    /** 필러/안내 아이템 식별 표식. 장비 판정과 무관하게 "GUI 장식"임을 명시한다 */
    private static final String DECORATION_KEY_NAME = "equipment_gui_decoration";

    public EquipmentGUI(A_Player player) {
        super(player, SIZE, "장비");

        ItemStack filler = createDecoration(Material.GRAY_STAINED_GLASS_PANE, Component.empty(), null);
        for (int i = 0; i < SIZE; i++) {
            setItem(i, filler);
        }
    }

    /**
     * 슬롯 내용은 생성 시점이 아니라 여는 시점에 채운다.
     * (이미 장비 GUI가 열려 있는 상태에서 또 열면, 생성 시점에 읽은 옛 데이터로 덮어써져
     *  먼저 열린 GUI의 변경이 되돌아가거나 장비가 복사될 수 있다)
     */
    @Override
    protected void prepareOpen() {
        A_Player player = getOwner();
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof EquipmentGUI) {
            player.closeInventory(); // InventoryCloseEvent → 이전 GUI의 saveAndApply 확정
        }

        loadSlots();
    }

    /** 저장된 장비를 GUI 슬롯에 채운다. 비어있는 슬롯은 안내 아이템으로 채운다 */
    private void loadSlots() {
        for (EquipSlot slot : EquipSlot.values()) {
            ItemStack stored = EquipmentManager.getEquipment(getOwner(), slot);
            setItem(slot.guiIndex, stored == null ? createPlaceholder(slot) : stored);
        }
    }

    /**
     * GUI의 장비 슬롯 내용을 저장하고 EQUIPMENT 컨테이너를 재계산한다.
     *
     * 슬롯 내용 검증을 여기서 한 번 더 한다:
     * - 안내 아이템/빈칸 → 장비 없음으로 저장
     * - 슬롯에 맞지 않는 아이템 → 저장하지 않고 플레이어에게 돌려준다
     *   (그냥 버리면 다음에 GUI를 열 때 저장 데이터로 덮어써져 아이템이 사라진다)
     */
    public void saveAndApply() {
        A_Player player = getOwner();

        for (EquipSlot slot : EquipSlot.values()) {
            ItemStack stack = getItem(slot.guiIndex);

            if (stack == null || stack.isEmpty() || isDecoration(stack)) {
                EquipmentManager.setEquipment(player, slot, null);
                // 이미 안내 아이템이면 그대로 둔다 (매 저장마다 새로 만들어 넣으면 불필요한 갱신이 반복된다)
                if (!isDecoration(stack)) setItem(slot.guiIndex, createPlaceholder(slot));
                continue;
            }

            if (!EquipmentManager.canEquip(slot, stack)) {
                UndefinedWorldCorePlugin.sendLog("Invalid equipment returned to player: " + player.getName()
                        + " slot=" + slot.name() + " item=" + stack.getType() + " amount=" + stack.getAmount());

                // 슬롯을 먼저 비우면 stack이 참조하던 인벤토리 슬롯이 바뀌므로 복사본을 돌려준다
                ItemStack returned = stack.clone();
                clearSlot(slot);
                player.addItemNature(returned);
                continue;
            }

            EquipmentManager.setEquipment(player, slot, stack);
        }

        EquipmentManager.applyEquipmentAttributes(player);
    }

    /** 슬롯을 비우고(저장 데이터 + GUI 표시) 안내 아이템을 되돌려 놓는다 */
    private void clearSlot(EquipSlot slot) {
        EquipmentManager.setEquipment(getOwner(), slot, null);
        setItem(slot.guiIndex, createPlaceholder(slot));
    }

    // ── 클릭 처리 ──────────────────────────────────────

    /** 장비 슬롯 클릭: 장착 / 해제 / 교체를 직접 수행한다 */
    @Override
    protected void onTopClick(InventoryClickEvent event, Player clicker) {
        event.setCancelled(true); // 장비 슬롯은 전부 직접 처리

        try {
            handleSlotClick(event, clicker);
        } finally {
            // 취소된 클릭은 클라이언트 예측 화면이 서버 상태와 어긋날 수 있으므로 항상 다시 내려준다
            clicker.updateInventory();
        }
    }

    /** 하단(플레이어 인벤) 클릭: 일반 조작은 그대로 두고, 쉬프트클릭만 자동 장착으로 처리 */
    @Override
    protected void onBottomClick(InventoryClickEvent event, Player clicker) {
        // 하단 칸을 대상으로 하는 나머지 조작(숫자키/오프핸드 스왑/버리기 등)은 상단 슬롯을 건드리지 않는다
        if (!event.getClick().isShiftClick()) return;

        event.setCancelled(true);
        try {
            autoEquip(event, clicker);
        } finally {
            clicker.updateInventory();
        }
    }

    @Override
    protected void onGuiClose() {
        saveAndApply(); // 멱등 — 슬롯 내용을 그대로 다시 저장한다
    }

    private void handleSlotClick(InventoryClickEvent event, Player clicker) {
        EquipSlot slot = EquipSlot.fromGuiIndex(event.getSlot());
        if (slot == null) return; // 필러 영역

        ClickType click = event.getClick();
        boolean simpleClick = click == ClickType.LEFT || click == ClickType.RIGHT;
        boolean shiftClick = click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT;
        if (!simpleClick && !shiftClick) return; // 지원하지 않는 조작 (이미 취소됨)

        ItemStack current = getItem(slot.guiIndex);

        // 안내 아이템/빈칸이 아니면서 장비로 인정되지 않는 내용물 → 회수하고 슬롯을 정리한다
        if (!isSlotEmpty(current) && !EquipmentManager.canEquip(slot, current)) {
            saveAndApply();
            clicker.sendMessage("슬롯에 올릴 수 없는 아이템이 있어 회수했습니다.");
            return;
        }

        // 인벤토리/커서에서 꺼낸 ItemStack은 원본 슬롯을 참조하는 mirror일 수 있어
        // 슬롯을 갱신하는 순간 값이 변할 여지가 있다 → 읽는 즉시 복사해서 다룬다
        ItemStack equipped = isSlotEmpty(current) ? null : current.clone();
        ItemStack cursor = clicker.getItemOnCursor().clone();

        if (cursor.isEmpty()) {
            unequip(clicker, slot, equipped, shiftClick);
            return;
        }

        equip(clicker, slot, equipped, cursor);
    }

    /** 하단 인벤의 장비를 맞는 슬롯에 자동 장착한다 (쉬프트클릭) */
    private void autoEquip(InventoryClickEvent event, Player clicker) {
        // 원본 슬롯을 참조하는 mirror를 그대로 옮기지 않도록 읽는 즉시 복사한다
        ItemStack clicked = event.getCurrentItem() == null ? null : event.getCurrentItem().clone();
        if (clicked == null || clicked.isEmpty()) return;

        if (clicked.getAmount() != 1) {
            clicker.sendMessage("장비는 1개씩만 장착할 수 있습니다.");
            return;
        }

        EquipSlot target = findSlotFor(clicked);
        if (target == null) {
            clicker.sendMessage("이 아이템을 장착할 수 있는 슬롯이 없습니다.");
            return;
        }

        ItemStack previous = getItem(target.guiIndex);
        ItemStack back = EquipmentManager.canEquip(target, previous) ? previous.clone() : null;

        setItem(target.guiIndex, clicked);
        event.setCurrentItem(back); // 클릭한 인벤토리 칸에 기존 장비를 넣거나 비운다 (교환)

        saveAndApply();
    }

    /** 장비 해제: 쉬프트클릭이면 인벤토리로, 아니면 커서로 옮긴다 */
    private void unequip(Player clicker, EquipSlot slot, @Nullable ItemStack equipped, boolean toInventory) {
        if (equipped == null) return; // 빈 슬롯 클릭 — 안내 아이템은 꺼낼 수 없다

        if (toInventory) {
            if (!clicker.getInventory().addItem(equipped).isEmpty()) {
                clicker.sendMessage("인벤토리가 가득 차서 장비를 해제할 수 없습니다.");
                return;
            }
        } else {
            clicker.setItemOnCursor(equipped);
        }

        setItem(slot.guiIndex, null); // saveAndApply가 안내 아이템으로 되돌린다
        saveAndApply();
    }

    /** 장비 장착(교체): 커서의 장비를 슬롯에 넣고 기존 장비를 커서로 되돌린다 */
    private void equip(Player clicker, EquipSlot slot, @Nullable ItemStack equipped, ItemStack cursor) {
        if (cursor.getAmount() != 1) {
            clicker.sendMessage("장비는 1개씩만 장착할 수 있습니다. (겹쳐진 아이템은 나눠서 올려주세요)");
            return;
        }

        if (!EquipmentManager.canEquip(slot, cursor)) {
            clicker.sendMessage(slot.krName + " 슬롯에는 해당 부위의 장비만 올릴 수 있습니다.");
            return;
        }

        setItem(slot.guiIndex, cursor);
        clicker.setItemOnCursor(equipped); // 기존 장비가 없으면 null → 커서 비움

        saveAndApply();
    }

    /**
     * 해당 아이템을 올릴 장비 슬롯을 찾는다.
     * 빈 슬롯을 우선하고, 없으면 교체 가능한(기존 내용물이 정상 장비인) 첫 슬롯을 돌려준다.
     */
    @Nullable
    private EquipSlot findSlotFor(ItemStack stack) {
        EquipSlot swapTarget = null;

        for (EquipSlot slot : EquipSlot.values()) {
            if (!EquipmentManager.canEquip(slot, stack)) continue;

            ItemStack current = getItem(slot.guiIndex);
            if (isSlotEmpty(current)) return slot;
            if (swapTarget == null && EquipmentManager.canEquip(slot, current)) swapTarget = slot;
        }

        return swapTarget;
    }

    /** 장비가 없는 슬롯인지 (빈칸 또는 안내 아이템) */
    private boolean isSlotEmpty(@Nullable ItemStack stack) {
        return stack == null || stack.isEmpty() || isDecoration(stack);
    }

    // ── 안내/필러 아이템 ────────────────────────────────

    /** 필러/안내 아이템 여부. 장비로 저장하거나 꺼내가지 못하게 하는 판정에 사용한다 */
    public static boolean isDecoration(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(decorationKey(), PersistentDataType.BYTE);
    }

    private static NamespacedKey decorationKey() {
        return new NamespacedKey(UndefinedWorldCorePlugin.instance, DECORATION_KEY_NAME);
    }

    private ItemStack createPlaceholder(EquipSlot slot) {
        return createDecoration(
                Material.LIGHT_GRAY_STAINED_GLASS_PANE,
                Component.text(slot.krName + " 슬롯", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("여기에 " + slot.krName + " 장비를 올리세요", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)
        );
    }

    private ItemStack createDecoration(Material material, Component name, @Nullable Component lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(name);
        if (lore != null) meta.lore(List.of(lore));
        meta.getPersistentDataContainer().set(decorationKey(), PersistentDataType.BYTE, (byte) 1);
        stack.setItemMeta(meta);
        return stack;
    }
}
