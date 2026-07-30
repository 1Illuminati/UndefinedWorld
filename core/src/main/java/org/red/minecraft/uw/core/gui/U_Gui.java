package org.red.minecraft.uw.core.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.dellarte.library.inventory.CustomGui;

/**
 * UW 커스텀 GUI 공통 베이스. (§2.6 확정: GUI는 전부 CustomGui 사용 + 클릭 차단 정책 통일)
 *
 * <p>클릭 차단 정책을 <b>이 클래스 한 곳에서만</b> 정의한다. GUI마다 따로 적으면 한 곳만 빠져도
 * 아이템 복사 경로가 생기기 때문이다(실제로 장비 GUI에만 있던 방어가 제작 GUI에 없어 복사가 가능했다).
 *
 * <p>상단(GUI) 클릭:
 * <ul>
 *   <li>숫자키/오프핸드 스왑/더블클릭/커서 수집은 전부 차단 — 슬롯 검증을 우회하는 이동 경로다</li>
 *   <li>나머지는 각 GUI의 {@link #onTopClick}에 위임</li>
 * </ul>
 *
 * <p>하단(플레이어 인벤) 클릭:
 * <ul>
 *   <li>더블클릭 스택 모으기는 허용하되 <b>하단 안에서만</b> 동작하도록 직접 구현한다
 *       (바닐라 COLLECT_TO_CURSOR는 상단 GUI 슬롯까지 긁어간다)</li>
 *   <li>나머지는 각 GUI의 {@link #onBottomClick}에 위임</li>
 * </ul>
 *
 * <p>클릭 디스패치는 dellarte의 InventoryEventListener가 {@link CustomGui#onClick}을 호출해 이뤄진다.
 * 드래그는 dellarte가 처리하지 않으므로 각 GUI 리스너가 {@link #blockTopDrag}로 위임한다.
 */
public abstract class U_Gui extends CustomGui {

    private final A_Player owner;
    /** 닫힘 처리(내용물 반환/저장)가 끝난 상태. 늦게 도착한 클릭을 막는 데도 쓴다 */
    private boolean closed = false;

    protected U_Gui(A_Player owner, int size, String title) {
        super(size, title);
        this.owner = owner;
    }

    public A_Player getOwner() {
        return owner;
    }

    public boolean isClosed() {
        return closed;
    }

    /**
     * GUI를 연다.
     * 여는 시점에 채워야 하는 내용(저장 데이터 로드 등)은 {@link #prepareOpen()}에서 처리한다.
     * — 생성 시점에 채우면 같은 GUI를 두 번 열었을 때 옛 스냅샷으로 덮어써진다.
     *
     * @return 실제로 열렸으면 true. 다른 플러그인이 InventoryOpenEvent를 취소하면 <b>예외 없이</b>
     *         열리지 않고, 그 경우 닫힘 이벤트도 오지 않는다. GUI에 아이템을 미리 옮겨둔 호출부는
     *         반드시 이 값을 확인해 옮긴 아이템을 되돌려야 한다.
     */
    public final boolean open() {
        prepareOpen();
        closed = false; // prepareOpen이 이전 GUI를 닫으면서 닫힘 처리가 돌 수 있어 그 뒤에 표시한다
        return owner.openInventory(getInventory()) != null;
    }

    /** 여는 시점 준비. 필요 없으면 구현하지 않아도 된다 */
    protected void prepareOpen() {}

    // ── 클릭 정책 (공통) ────────────────────────────────

    @Override
    public final void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player clicker)) {
            event.setCancelled(true);
            return;
        }

        // 저장/반환은 항상 GUI 소유자 기준이므로 소유자가 아닌 사람의 조작은 전부 막는다
        // (같은 인벤토리를 남의 화면에 열면 내용물이 소유자에게 복사될 수 있다)
        if (!clicker.getUniqueId().equals(owner.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        // 닫힘 처리로 내용물이 이미 반환된 GUI — 늦게 도착한 클릭은 무시한다
        if (closed) {
            event.setCancelled(true);
            return;
        }

        if (event.getClickedInventory() != getInventory()) {
            handleBottomClick(event, clicker);
            return;
        }

        handleTopClick(event, clicker);
    }

    private void handleTopClick(InventoryClickEvent event, Player clicker) {
        ClickType click = event.getClick();

        // 숫자키/오프핸드 스왑은 슬롯 검증을 거치지 않고 내용물을 바꾼다.
        // 더블클릭(COLLECT_TO_CURSOR)은 상단 슬롯을 통째로 커서에 긁어모아 복사 경로가 된다.
        if (click == ClickType.NUMBER_KEY || click == ClickType.SWAP_OFFHAND
                || click == ClickType.DOUBLE_CLICK
                || event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
            clicker.updateInventory();
            return;
        }

        onTopClick(event, clicker);
    }

    private void handleBottomClick(InventoryClickEvent event, Player clicker) {
        // 클릭한 칸이 하단이어도 바닐라 수집은 "열린 창 전체"를 대상으로 한다 → 취소하고 하단만 직접 모은다
        if (event.getClick() == ClickType.DOUBLE_CLICK
                || event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
            collectInBottom(clicker);
            return;
        }

        onBottomClick(event, clicker);
    }

    /**
     * 더블클릭 스택 모으기를 <b>하단(플레이어) 인벤토리 안에서만</b> 수행한다. (§2.6 아이템·장비 5)
     *
     * <p>대상은 플레이어 주 인벤토리(핫바 포함 36칸)뿐이며 갑옷/오프핸드는 제외한다.
     * 가득 찬 스택을 먼저 깨지 않도록 덜 찬 스택부터 모은다(바닐라와 같은 순서).
     */
    private void collectInBottom(Player clicker) {
        ItemStack cursor = clicker.getItemOnCursor();
        if (cursor.isEmpty()) return;

        int max = cursor.getMaxStackSize();
        if (cursor.getAmount() >= max) return;

        // getItemOnCursor는 커서를 비추는 참조라 setItemOnCursor 이후 값이 변한다 → 원래 수량을 먼저 기록한다
        int beforeAmount = cursor.getAmount();
        ItemStack collected = cursor.clone();

        PlayerInventory inventory = clicker.getInventory();
        int storageSize = inventory.getStorageContents().length; // 갑옷/오프핸드 제외 범위

        collectPass(inventory, storageSize, collected, max, true);
        if (collected.getAmount() < max) collectPass(inventory, storageSize, collected, max, false);

        if (collected.getAmount() != beforeAmount) clicker.setItemOnCursor(collected);
        clicker.updateInventory(); // 이벤트를 취소했으므로 클라이언트 예측 화면을 서버 상태로 되돌린다
    }

    /**
     * 수집 1회 순회.
     * @param partialOnly true면 덜 찬 스택만 대상으로 한다
     */
    private void collectPass(PlayerInventory inventory, int storageSize, ItemStack collected, int max, boolean partialOnly) {
        for (int i = 0; i < storageSize; i++) {
            if (collected.getAmount() >= max) return;

            ItemStack stack = inventory.getItem(i);
            if (stack == null || stack.isEmpty()) continue;

            // getItem은 슬롯을 비추는 참조라 setItem 이후 값이 변한다 → 필요한 값을 먼저 읽어둔다
            int amount = stack.getAmount();
            if (partialOnly && amount >= stack.getMaxStackSize()) continue;
            if (!collected.isSimilar(stack)) continue;

            int move = Math.min(max - collected.getAmount(), amount);
            collected.setAmount(collected.getAmount() + move);

            if (amount == move) {
                inventory.setItem(i, null);
                continue;
            }

            ItemStack remain = stack.clone();
            remain.setAmount(amount - move);
            inventory.setItem(i, remain);
        }
    }

    // ── 닫힘 처리 (공통) ────────────────────────────────

    @Override
    public final void onClose(InventoryCloseEvent event) {
        handleClose();
    }

    /**
     * 닫힘 처리 — 닫힘 표시 후 각 GUI의 정리({@link #onGuiClose()})를 수행한다.
     *
     * <p>dellarte의 닫힘 디스패치는 A_Player가 IgnoreInvClose 상태면 {@link CustomGui#onClose}를
     * 호출하지 않고 넘긴다. 그대로 두면 GUI에 올려둔 아이템이 사라지므로 각 GUI 리스너의
     * InventoryCloseEvent와 플러그인 종료 처리에서도 이 메서드를 직접 호출한다.
     * 여러 번 호출될 수 있으므로 {@link #onGuiClose()} 구현은 반드시 멱등이어야 한다.
     */
    public final void handleClose() {
        if (closed) return; // 이미 정리됨 (dellarte 디스패치와 GUI 리스너가 모두 호출한다)

        closed = true;
        try {
            onGuiClose();
        } catch (RuntimeException e) {
            // 정리에 실패했으면 다음 호출이 다시 시도할 수 있게 되돌린다 (내용물이 남은 채 잠기면 소실된다)
            closed = false;
            throw e;
        }
    }

    // ── 구현 지점 ──────────────────────────────────────

    /** 상단(GUI) 슬롯 클릭. 공통 차단(숫자키/오프핸드/더블클릭)을 통과한 클릭만 전달된다 */
    protected abstract void onTopClick(InventoryClickEvent event, Player clicker);

    /** 하단(플레이어 인벤) 슬롯 클릭. 더블클릭 수집은 공통 처리에서 이미 끝난 뒤다 */
    protected abstract void onBottomClick(InventoryClickEvent event, Player clicker);

    /** 닫힘 시 정리(아이템 반환/저장). <b>여러 번 호출돼도 안전해야 한다</b> */
    protected abstract void onGuiClose();

    // ── 드래그 차단 (공통) ──────────────────────────────

    /**
     * 드래그 차단. CustomGui는 드래그를 처리하지 않으므로 각 GUI 리스너가 이 함수로 위임한다.
     * 상단 슬롯이 하나라도 포함되면 취소한다 — 드래그는 여러 칸에 나눠 넣는 조작이라 슬롯 검증을 우회한다.
     */
    public static void blockTopDrag(InventoryDragEvent event) {
        int topSize = event.getInventory().getSize();
        if (event.getRawSlots().stream().noneMatch(raw -> raw < topSize)) return;

        event.setCancelled(true);
        if (event.getWhoClicked() instanceof Player clicker) clicker.updateInventory();
    }
}
