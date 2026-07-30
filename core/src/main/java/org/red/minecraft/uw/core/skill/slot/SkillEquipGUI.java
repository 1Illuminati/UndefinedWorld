package org.red.minecraft.uw.core.skill.slot;

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
import org.red.minecraft.uw.core.skill.craft.SkillItem;

import java.util.List;

/**
 * 스킬 장착 GUI. (§2.10 확정: {@code /skill equip} 이 이 GUI를 연다, {@code /skill unequip} 폐지)
 *
 * <p>슬롯은 스킬 아이템 <b>실물</b>을 보관한다(§2.6). 장착하면 아이템이 인벤토리에서 빠져
 * 저장 데이터로 들어가고, 해제하면 돌려받는다 — 세상에 아이템이 1개만 존재해 복사가 원천 차단된다.
 * 장비 GUI({@code EquipmentGUI})와 같은 모델이므로 방어 규칙도 같다:
 * <ul>
 *   <li>슬롯 내용은 <b>여는 시점</b>에 채운다(생성 시점에 읽으면 이중 오픈 시 옛 스냅샷으로 덮어써진다)</li>
 *   <li>인벤토리/커서에서 읽은 ItemStack은 원본 슬롯 mirror일 수 있어 <b>읽는 즉시 clone</b> 한다</li>
 *   <li>슬롯 클릭은 바닐라 이동 규칙을 쓰지 않고 <b>취소 후 직접</b> 처리한다.
 *       그래야 결과를 예측할 수 있어 저장을 <b>클릭과 같은 틱</b>에 끝낼 수 있다
 *       (1틱 뒤 저장 예약은 그 사이 종료/강제퇴장 시 저장 데이터와 인벤토리가 어긋난다)</li>
 *   <li>빈 슬롯에는 안내 아이템(placeholder)을 넣고 PDC 표식으로 식별한다 — 스킬로 저장되지도,
 *       꺼내지지도 않는다</li>
 * </ul>
 *
 * <p>클릭 차단 정책(더블클릭/숫자키/오프핸드/소유자 검사/닫힘 멱등)은 {@link U_Gui}가 담당한다.
 * 여기서는 스킬 슬롯 고유의 장착/해제/교체만 처리한다.
 *
 * <p>슬롯 배치는 {@link SkillSlot}의 선언 순서를 그대로 따른다 — enum에 상수를 추가하면
 * GUI 칸도 따라 늘어난다(하드코딩 없음). 배치 줄(9칸)을 넘는 순간 생성에서 실패시켜
 * 슬롯이 조용히 사라지는 상태를 만들지 않는다.
 *
 * todo GUI 타이틀/색상/안내 문구는 사용자 확정 필요 (현재는 임시 표현)
 */
public class SkillEquipGUI extends U_Gui {

    private static final int SIZE = 27;

    /** 슬롯 배치 줄 (2번째 줄 9칸, 왼쪽부터 SkillSlot 선언 순서) */
    private static final int SLOT_START = 9;
    private static final int SLOT_END = 17;
    /** 배치 가능한 슬롯 수 */
    private static final int SLOT_CAPACITY = SLOT_END - SLOT_START + 1;

    /** 안내 아이템 위치 (필러 영역이라 클릭이 차단된다) */
    private static final int GUIDE_SLOT = 4;

    /** 필러/안내 아이템 식별 표식. 스킬 판정과 무관하게 "GUI 장식"임을 명시한다 */
    private static final String DECORATION_KEY_NAME = "skill_equip_gui_decoration";

    public SkillEquipGUI(A_Player player) {
        super(player, SIZE, "스킬 장착");

        // 슬롯이 늘어나 배치 줄을 넘으면 일부 슬롯이 GUI에 없는 채로 열려 "장착은 되는데 못 빼는" 상태가 된다
        int slotCount = SkillSlot.values().length;
        if (slotCount > SLOT_CAPACITY)
            throw new IllegalStateException("스킬 슬롯 수(" + slotCount + ")가 GUI 배치 칸(" + SLOT_CAPACITY + ")을 넘습니다");

        ItemStack filler = createDecoration(Material.GRAY_STAINED_GLASS_PANE, Component.empty(), null);
        for (int i = 0; i < SIZE; i++) {
            setItem(i, filler);
        }
        setItem(GUIDE_SLOT, createGuideItem());
    }

    // ── 슬롯 ↔ GUI 칸 대응 ──────────────────────────────

    private static int guiIndex(SkillSlot slot) {
        return SLOT_START + slot.ordinal();
    }

    /** GUI 칸에 대응하는 슬롯. 필러/안내 영역이면 null */
    @Nullable
    private static SkillSlot fromGuiIndex(int index) {
        int offset = index - SLOT_START;
        SkillSlot[] slots = SkillSlot.values();
        if (offset < 0 || offset >= slots.length) return null;
        return slots[offset];
    }

    // ── 열기 / 저장 ────────────────────────────────────

    /**
     * 슬롯 내용은 생성 시점이 아니라 여는 시점에 채운다.
     * (이미 스킬 장착 GUI가 열려 있는 상태에서 또 열면, 생성 시점에 읽은 옛 데이터로 덮어써져
     *  먼저 열린 GUI의 변경이 되돌아가거나 스킬 아이템이 복사될 수 있다)
     */
    @Override
    protected void prepareOpen() {
        A_Player player = getOwner();
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof SkillEquipGUI) {
            player.closeInventory(); // InventoryCloseEvent → 이전 GUI의 saveSlots 확정
        }

        loadSlots();
    }

    /** 저장된 스킬 아이템을 GUI 슬롯에 채운다. 비어있는 슬롯은 안내 아이템으로 채운다 */
    private void loadSlots() {
        for (SkillSlot slot : SkillSlot.values()) {
            ItemStack stored = PlayerSkillManager.getEquippedItem(getOwner(), slot); // 이미 복사본이다
            setItem(guiIndex(slot), stored == null ? createPlaceholder(slot) : stored);
        }
    }

    /**
     * GUI의 슬롯 내용을 저장 데이터에 반영한다. (클릭과 같은 틱에 호출한다)
     *
     * <p>슬롯 내용 검증을 여기서 한 번 더 한다:
     * <ul>
     *   <li>안내 아이템/빈칸 → 미장착으로 저장</li>
     *   <li>스킬 아이템이 아니거나 겹쳐진 아이템 → 저장하지 않고 플레이어에게 돌려준다.
     *       그냥 두면 다음에 GUI를 열 때 저장 데이터로 덮어써져 아이템이 사라진다</li>
     * </ul>
     */
    public void saveSlots() {
        A_Player player = getOwner();

        for (SkillSlot slot : SkillSlot.values()) {
            int index = guiIndex(slot);
            ItemStack stack = getItem(index);

            if (isSlotEmpty(stack)) {
                PlayerSkillManager.setEquippedItem(player, slot, null);
                // 이미 안내 아이템이면 그대로 둔다 (매 저장마다 새로 만들어 넣으면 불필요한 갱신이 반복된다)
                if (!isDecoration(stack)) setItem(index, createPlaceholder(slot));
                continue;
            }

            if (!isEquippable(stack)) {
                UndefinedWorldCorePlugin.sendLog("Invalid skill slot content returned to player: " + player.getName()
                        + " slot=" + slot.name() + " item=" + stack.getType() + " amount=" + stack.getAmount());

                // 슬롯을 먼저 비우면 stack이 참조하던 인벤토리 슬롯이 바뀌므로 복사본을 돌려준다
                ItemStack returned = stack.clone();
                PlayerSkillManager.setEquippedItem(player, slot, null);
                setItem(index, createPlaceholder(slot));
                player.addItemNature(returned);
                continue;
            }

            PlayerSkillManager.setEquippedItem(player, slot, stack); // 저장 시 복사된다
        }
    }

    @Override
    protected void onGuiClose() {
        saveSlots(); // 멱등 — 슬롯 내용을 그대로 다시 저장한다
    }

    // ── 클릭 처리 ──────────────────────────────────────

    /** 스킬 슬롯 클릭: 장착 / 해제 / 교체를 직접 수행한다 */
    @Override
    protected void onTopClick(InventoryClickEvent event, Player clicker) {
        event.setCancelled(true); // 스킬 슬롯은 전부 직접 처리

        try {
            handleSlotClick(event, clicker);
        } finally {
            // 취소된 클릭은 클라이언트 예측 화면이 서버 상태와 어긋날 수 있으므로 항상 다시 내려준다
            clicker.updateInventory();
        }
    }

    /**
     * 하단(플레이어 인벤) 클릭: 일반 조작은 그대로 두고, 쉬프트클릭만 자동 장착으로 처리한다.
     * 쉬프트클릭을 그대로 두면 바닐라가 필러(유리판)에 스택을 합쳐버릴 수 있어 반드시 막아야 한다.
     */
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

    private void handleSlotClick(InventoryClickEvent event, Player clicker) {
        SkillSlot slot = fromGuiIndex(event.getSlot());
        if (slot == null) return; // 필러/안내 영역

        ClickType click = event.getClick();
        boolean simpleClick = click == ClickType.LEFT || click == ClickType.RIGHT;
        boolean shiftClick = click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT;
        if (!simpleClick && !shiftClick) return; // 지원하지 않는 조작 (이미 취소됨)

        ItemStack current = getItem(guiIndex(slot));

        // 안내 아이템/빈칸이 아니면서 스킬로 인정되지 않는 내용물 → 회수하고 슬롯을 정리한다
        if (!isSlotEmpty(current) && !isEquippable(current)) {
            saveSlots();
            clicker.sendMessage("슬롯에 올릴 수 없는 아이템이 있어 회수했습니다."); //todo 문구 확정
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

    /** 하단 인벤의 스킬 아이템을 빈 슬롯(없으면 첫 교체 가능 슬롯)에 자동 장착한다 (쉬프트클릭) */
    private void autoEquip(InventoryClickEvent event, Player clicker) {
        // 원본 슬롯을 참조하는 mirror를 그대로 옮기지 않도록 읽는 즉시 복사한다
        ItemStack clicked = event.getCurrentItem() == null ? null : event.getCurrentItem().clone();
        if (clicked == null || clicked.isEmpty()) return;

        if (!SkillItem.isSkillItem(clicked)) {
            clicker.sendMessage("스킬 아이템만 장착할 수 있습니다."); //todo 문구 확정
            return;
        }
        if (clicked.getAmount() != 1) {
            clicker.sendMessage("스킬 아이템은 1개씩만 장착할 수 있습니다."); //todo 문구 확정
            return;
        }

        SkillSlot target = findTargetSlot();
        if (target == null) {
            clicker.sendMessage("장착할 수 있는 슬롯이 없습니다."); //todo 문구 확정
            return;
        }

        // findTargetSlot이 "빈 슬롯 또는 정상 스킬이 든 슬롯"만 돌려주므로
        // 여기서 덮어써서 사라질 수 있는 내용물은 없다 (안내 아이템은 되돌려주지 않는다)
        ItemStack previous = getItem(guiIndex(target));
        ItemStack back = isEquippable(previous) ? previous.clone() : null;

        setItem(guiIndex(target), clicked);
        event.setCurrentItem(back); // 클릭한 인벤토리 칸에 기존 스킬을 넣거나 비운다 (교환)

        saveSlots();
    }

    /** 스킬 해제: 쉬프트클릭이면 인벤토리로, 아니면 커서로 옮긴다 */
    private void unequip(Player clicker, SkillSlot slot, @Nullable ItemStack equipped, boolean toInventory) {
        if (equipped == null) return; // 빈 슬롯 클릭 — 안내 아이템은 꺼낼 수 없다

        if (toInventory) {
            // equipped는 isEquippable을 통과한 1개짜리라 addItem이 부분만 넣는 경우가 없다
            // (부분 추가가 가능하면 아래에서 슬롯을 비우지 않아 복사가 된다)
            if (!clicker.getInventory().addItem(equipped).isEmpty()) {
                clicker.sendMessage("인벤토리가 가득 차서 스킬을 해제할 수 없습니다."); //todo 문구 확정
                return;
            }
        } else {
            clicker.setItemOnCursor(equipped);
        }

        setItem(guiIndex(slot), null); // saveSlots가 안내 아이템으로 되돌린다
        saveSlots();
    }

    /** 스킬 장착(교체): 커서의 스킬 아이템을 슬롯에 넣고 기존 스킬을 커서로 되돌린다 */
    private void equip(Player clicker, SkillSlot slot, @Nullable ItemStack equipped, ItemStack cursor) {
        if (cursor.getAmount() != 1) {
            clicker.sendMessage("스킬 아이템은 1개씩만 장착할 수 있습니다. (겹쳐진 아이템은 나눠서 올려주세요)"); //todo 문구 확정
            return;
        }

        if (!SkillItem.isSkillItem(cursor)) {
            clicker.sendMessage("스킬 슬롯에는 스킬 아이템만 올릴 수 있습니다."); //todo 문구 확정
            return;
        }

        setItem(guiIndex(slot), cursor);
        clicker.setItemOnCursor(equipped); // 기존 스킬이 없으면 null → 커서 비움

        saveSlots();
    }

    /**
     * 자동 장착 대상 슬롯.
     * 빈 슬롯을 우선하고, 없으면 교체 가능한(기존 내용물이 정상 스킬 아이템인) 첫 슬롯을 돌려준다.
     */
    @Nullable
    private SkillSlot findTargetSlot() {
        SkillSlot swapTarget = null;

        for (SkillSlot slot : SkillSlot.values()) {
            ItemStack current = getItem(guiIndex(slot));
            if (isSlotEmpty(current)) return slot;
            if (swapTarget == null && isEquippable(current)) swapTarget = slot;
        }

        return swapTarget;
    }

    /** 슬롯에 보관할 수 있는 내용물인지 (스킬 아이템 1개) */
    private static boolean isEquippable(@Nullable ItemStack stack) {
        return SkillItem.isSkillItem(stack) && stack.getAmount() == 1;
    }

    /** 스킬이 없는 슬롯인지 (빈칸 또는 안내 아이템) */
    private static boolean isSlotEmpty(@Nullable ItemStack stack) {
        return stack == null || stack.isEmpty() || isDecoration(stack);
    }

    // ── 안내/필러 아이템 ────────────────────────────────

    /** 필러/안내 아이템 여부. 스킬로 저장하거나 꺼내가지 못하게 하는 판정에 사용한다 */
    public static boolean isDecoration(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(decorationKey(), PersistentDataType.BYTE);
    }

    private static NamespacedKey decorationKey() {
        return new NamespacedKey(UndefinedWorldCorePlugin.instance, DECORATION_KEY_NAME);
    }

    private ItemStack createPlaceholder(SkillSlot slot) {
        return createDecoration(
                Material.LIGHT_GRAY_STAINED_GLASS_PANE,
                Component.text(slot.krName + " 슬롯", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("여기에 스킬 아이템을 올리세요", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)
        );
    }

    private ItemStack createGuideItem() {
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text("스킬 장착 안내", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                line("아래 칸에 스킬 아이템을 올리면 장착됩니다."),
                line("슬롯을 클릭하면 장착된 스킬을 꺼냅니다."),
                line("쉬프트클릭하면 인벤토리로 바로 옮깁니다."),
                line("장착한 스킬은 무기를 들고 해당 조작으로 사용합니다.")
        )); //todo 안내 문구/아이콘 사용자 확정 필요
        meta.getPersistentDataContainer().set(decorationKey(), PersistentDataType.BYTE, (byte) 1);
        stack.setItemMeta(meta);
        return stack;
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

    private static Component line(String text) {
        return Component.text(text, NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false);
    }
}
