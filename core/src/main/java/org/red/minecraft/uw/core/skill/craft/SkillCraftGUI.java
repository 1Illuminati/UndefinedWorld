package org.red.minecraft.uw.core.skill.craft;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.exeception.PowerOverException;
import org.red.minecraft.uw.core.gui.U_Gui;
import org.red.minecraft.uw.core.item.U_Item;
import org.red.minecraft.uw.core.item.gear.GearItem;
import org.red.minecraft.uw.core.skill.SkillDefinition;
import org.red.minecraft.uw.core.skill.gear.Gear;
import org.red.minecraft.uw.core.skill.slot.PlayerSkillManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 스킬 제작/수정 GUI. (확정: 기어 순서 배치형)
 * 두 번째 줄 9칸에 Gear 아이템을 순서대로 배치(배치 순서 = 노드 실행 순서)하고
 * 확인 버튼을 누르면 검증(Power<=9) 후 <b>기어를 소모하고 스킬 아이템 1개</b>를 준다. (§2.6)
 *
 * <p>수정 모드: 스킬 아이템을 들고 우클릭하면 그 아이템을 <b>회수해</b> 구성 기어를 이 GUI에 펼친다
 * (SkillCastListener). 확인을 누르면 배치된 기어로 스킬 아이템이 다시 만들어지고,
 * 확인하지 않고 닫으면 기어만 돌려받는다 — 어느 경로에서도 기어와 스킬 아이템이 동시에 존재하지 않는다.
 *
 * <p>클릭 차단 정책(더블클릭/숫자키/오프핸드/소유자 검사)은 U_Gui가 담당하고,
 * 여기서는 기어 슬롯의 내용물 검증만 처리한다.
 *
 * todo GUI 타이틀/디자인 사용자 확정 필요
 */
public class SkillCraftGUI extends U_Gui {

    private static final int SIZE = 27;
    /** 기어 배치 슬롯 (2번째 줄 9칸, 왼쪽부터 실행 순서) */
    private static final int GEAR_SLOT_START = 9;
    private static final int GEAR_SLOT_END = 17;
    /** 배치 가능한 기어 수 */
    public static final int GEAR_CAPACITY = GEAR_SLOT_END - GEAR_SLOT_START + 1;
    /** 안내 아이템 위치 (기어 줄 바로 위 중앙, 필러 영역이라 클릭 차단됨) */
    private static final int GUIDE_SLOT = 4;
    /** 확인 버튼 위치 */
    private static final int CONFIRM_SLOT = 22;
    /** 스킬 파워 상한 (SkillDefinition.calcStats와 동일 기준) */
    private static final int MAX_POWER = 9;

    private final String skillName;
    private boolean confirmed = false;

    /** 새 스킬 제작 */
    public SkillCraftGUI(A_Player player, String skillName) {
        this(player, skillName, List.of());
    }

    /**
     * 기존 스킬 수정 — 회수한 스킬 아이템의 기어를 그대로 배치한 상태로 연다.
     * @param gearStacks 배치 순서대로의 기어 아이템 (호출자가 이미 스킬 아이템을 회수한 상태여야 한다)
     */
    public SkillCraftGUI(A_Player player, String skillName, List<ItemStack> gearStacks) {
        super(player, SIZE, "스킬 제작: " + skillName);
        this.skillName = skillName;

        ItemStack filler = createButton(Material.GRAY_STAINED_GLASS_PANE, Component.empty(), List.of());
        for (int i = 0; i < SIZE; i++) {
            setItem(i, filler);
        }
        for (int i = GEAR_SLOT_START; i <= GEAR_SLOT_END; i++) {
            setItem(i, null);
        }

        placeGears(gearStacks);
        setItem(GUIDE_SLOT, createGuideItem());
        refreshConfirmButton();
    }

    /** 수정 모드 초기 배치. 칸 수를 넘는 기어는 애초에 저장될 수 없으므로 넘치면 만들지 않는다 */
    private void placeGears(List<ItemStack> gearStacks) {
        if (gearStacks.size() > GEAR_CAPACITY)
            throw new IllegalArgumentException("기어 수가 배치 칸(" + GEAR_CAPACITY + ")을 넘습니다: " + gearStacks.size());

        for (int i = 0; i < gearStacks.size(); i++) {
            setItem(GEAR_SLOT_START + i, gearStacks.get(i));
        }
    }

    private static boolean isGearSlot(int index) {
        return index >= GEAR_SLOT_START && index <= GEAR_SLOT_END;
    }

    private static boolean isConfirmSlot(int index) {
        return index == CONFIRM_SLOT;
    }

    /** 배치 가능한 아이템인지 판정 (Gear 아이템만) */
    private static boolean isGearItem(ItemStack stack) {
        return UndefinedWorldCore.getItem(stack) instanceof GearItem;
    }

    // ── 클릭 처리 ──────────────────────────────────────

    @Override
    protected void onTopClick(InventoryClickEvent event, Player clicker) {
        int index = event.getSlot();

        if (isConfirmSlot(index)) {
            event.setCancelled(true);
            confirm();
            return;
        }

        if (!isGearSlot(index)) { // 필러/안내 영역
            event.setCancelled(true);
            return;
        }

        // 기어 슬롯 배치 검증 (꺼내기는 항상 허용)
        ItemStack cursor = event.getCursor();
        if (!cursor.isEmpty() && !isGearItem(cursor)) {
            event.setCancelled(true);
            return;
        }

        scheduleRefresh();
    }

    @Override
    protected void onBottomClick(InventoryClickEvent event, Player clicker) {
        // 쉬프트클릭 이동은 기어 아이템만 허용 (GUI의 빈 칸은 기어 슬롯뿐이라 기어 슬롯으로만 들어간다)
        if (!event.getClick().isShiftClick()) return;

        ItemStack current = event.getCurrentItem();
        if (current == null || current.isEmpty() || !isGearItem(current)) {
            event.setCancelled(true);
            return;
        }

        scheduleRefresh();
    }

    @Override
    protected void onGuiClose() {
        returnGearItems(); // 남아있는 배치 기어 반환 (제작 완료 시엔 이미 비어 있어 중복 반환되지 않는다)
    }

    /**
     * 확인 처리: 배치 순서대로 기어 수집 → SkillDefinition 검증 → 기어 소모 후 스킬 아이템 지급.
     * 검증에 실패하면 아무것도 소모하지 않는다.
     */
    public void confirm() {
        if (confirmed) return; // 제작 완료 후 재클릭(중복 지급) 차단

        A_Player player = getOwner();

        // 이름 검증: 명령어에서 걸렀더라도 한 번 더 확인한다 (수정 모드는 아이템에서 읽어온 이름이다)
        if (!PlayerSkillManager.isValidName(skillName)) {
            player.sendMessage("사용할 수 없는 스킬 이름입니다: " + skillName);
            player.sendMessage(PlayerSkillManager.nameRuleMessage());
            return;
        }

        List<String> gearCodes = new ArrayList<>();
        List<Gear> gears = new ArrayList<>();

        for (int i = GEAR_SLOT_START; i <= GEAR_SLOT_END; i++) {
            ItemStack stack = getItem(i);
            if (stack == null || stack.isEmpty()) continue;

            // 확인 시 기어 슬롯의 내용물은 전부 소모된다 → 기어로 인식되지 않는 것이 하나라도 있으면
            // 그대로 진행할 수 없다. 건너뛰면 그 아이템이 스킬에 들어가지도 못한 채 사라진다.
            // (배치 검증을 통과했어도 GUI를 열어둔 사이 아이템 모듈이 다시 로드되면 인식이 풀릴 수 있다)
            U_Item item = UndefinedWorldCore.getItem(stack);
            if (!(item instanceof GearItem gearItem)) {
                player.sendMessage("기어로 인식되지 않는 아이템이 있습니다. 빼고 다시 시도하세요. (슬롯 " + i + ")"); //todo 문구 확정
                return;
            }

            String code = item.getItemCode();
            if (code == null) { // 코드가 없으면 스킬 아이템에 저장할 수 없다
                player.sendMessage("아이템 코드를 확인할 수 없는 기어가 있습니다. (슬롯 " + i + ")"); //todo 문구 확정
                return;
            }

            // 겹쳐진 기어는 1개만 소모하고 나머지가 사라지므로 미리 막는다
            if (stack.getAmount() != 1) {
                player.sendMessage("기어는 한 칸에 1개씩만 배치할 수 있습니다. (슬롯 " + i + ")"); //todo 문구 확정
                return;
            }

            gearCodes.add(code);
            gears.add(gearItem.toGear());
        }

        if (gears.isEmpty()) {
            player.sendMessage("기어를 1개 이상 배치해야 합니다."); //todo 문구/형식 사용자 확정 필요
            return;
        }

        SkillDefinition skill;
        try {
            skill = new SkillDefinition(skillName, gears);
        } catch (PowerOverException e) {
            player.sendMessage(String.format("스킬 파워 총합이 상한(%d)을 초과했습니다.", MAX_POWER)); //todo 문구/형식 사용자 확정 필요
            return;
        } catch (IllegalArgumentException e) {
            player.sendMessage("스킬 구성이 잘못되었습니다: " + e.getMessage());
            return;
        }

        // 아이템을 먼저 만든 뒤 기어를 소모한다 — 생성이 실패하면 기어가 사라지지 않도록
        ItemStack skillItem = SkillItem.create(skillName, gearCodes);

        confirmed = true;
        clearGearSlots(); // 기어 소모 (§2.6 확정)
        player.addItemNature(skillItem);

        player.sendMessage(String.format("스킬 [%s] 제작 완료! (파워 %d, 쿨타임 %d초, 캐스팅 %s)",
                skillName, skill.getSkillPower(), skill.getSkillCoolDown(),
                SkillItem.formatCastingTime(skill.getCastingTime()))); //todo 문구 확정

        player.closeInventory();
    }

    /** 기어 슬롯을 비운다 (제작 확정 시 소모). 반환하지 않으므로 confirm 성공 경로에서만 호출한다 */
    private void clearGearSlots() {
        for (int i = GEAR_SLOT_START; i <= GEAR_SLOT_END; i++) {
            setItem(i, null);
        }
    }

    /**
     * 배치된 기어 아이템을 플레이어에게 반환하고 슬롯을 비운다.
     * 슬롯을 즉시 비우므로 여러 번 호출해도 중복 반환되지 않는다 (닫힘 처리가 호출한다).
     */
    public void returnGearItems() {
        for (int i = GEAR_SLOT_START; i <= GEAR_SLOT_END; i++) {
            ItemStack stack = getItem(i);
            if (stack == null || stack.isEmpty()) continue;

            // getItem은 인벤토리 슬롯을 비추는 참조일 수 있으므로 복사해두고 슬롯을 먼저 비운다
            // (슬롯을 비운 뒤 반환해야 반환 도중 예외가 나도 GUI에 남아 복사되지 않는다)
            ItemStack returning = stack.clone();
            setItem(i, null);
            getOwner().addItemNature(returning);
        }
    }

    /** 확인 버튼의 배치 현황(파워/쿨타임) 표기를 갱신한다. 닫힌 GUI에는 쓰지 않는다. */
    public void refreshConfirmButton() {
        if (isClosed()) return;
        setItem(CONFIRM_SLOT, createConfirmButton());
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    /** 클릭 처리가 끝난 뒤(1틱 후) 확인 버튼의 파워/쿨타임 표기를 갱신한다 */
    private void scheduleRefresh() {
        Bukkit.getScheduler().runTask(UndefinedWorldCorePlugin.instance, this::refreshConfirmButton);
    }

    // ── 버튼/안내 아이템 ───────────────────────────────

    private ItemStack createGuideItem() {
        return createButton(Material.PAPER, Component.text("기어 배치 안내"), List.of(
                Component.text("아래 줄 9칸에 기어를 왼쪽부터"),
                Component.text("순서대로 놓으세요 (배치 순서 = 실행 순서)."),
                Component.text("기어가 아닌 아이템은 올라가지 않습니다."),
                Component.text("파워 총합은 " + MAX_POWER + " 이하여야 합니다."),
                Component.text("제작하면 기어가 소모되고 스킬 아이템을 받습니다."),
                Component.text("확인을 누르지 않고 닫으면 기어를 그대로 돌려받습니다.")
        )); //todo 안내 문구/아이콘 사용자 확정 필요
    }

    /** 현재 배치 상태를 반영한 확인 버튼 (파워 초과/미배치 상태를 색과 lore로 구분) */
    private ItemStack createConfirmButton() {
        SkillItem.Summary preview = calcPreview();

        boolean placeable = preview.count() > 0 && preview.power() <= MAX_POWER;
        Material material = placeable ? Material.LIME_CONCRETE : Material.RED_CONCRETE;

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("스킬 이름: " + skillName));
        lore.add(Component.text(String.format("배치 기어: %d개", preview.count())));
        lore.add(Component.text(String.format("파워: %d / %d", preview.power(), MAX_POWER)));
        lore.add(Component.text(String.format("예상 쿨타임: %d초", preview.coolDown())));
        lore.add(Component.text("예상 캐스팅: " + SkillItem.formatCastingTime(preview.castingTime())));
        if (preview.count() == 0) lore.add(Component.text("기어를 1개 이상 배치하세요."));
        else if (preview.power() > MAX_POWER) lore.add(Component.text("파워 총합이 상한을 초과했습니다."));
        else lore.add(Component.text("클릭하여 제작 (기어 소모)"));

        return createButton(material, Component.text("제작 확인"), lore);
    }

    /** 배치된 기어의 파워/쿨타임/캐스팅 합계 미리보기 (합계 공식은 SkillItem.summarize 한 곳만 사용) */
    private SkillItem.Summary calcPreview() {
        List<Gear> gears = new ArrayList<>();

        for (int i = GEAR_SLOT_START; i <= GEAR_SLOT_END; i++) {
            ItemStack stack = getItem(i);
            if (stack == null || stack.isEmpty()) continue;
            if (!(UndefinedWorldCore.getItem(stack) instanceof GearItem gearItem)) continue;

            gears.add(gearItem.toGear());
        }

        return SkillItem.summarize(gears);
    }

    private ItemStack createButton(Material material, Component name, List<Component> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(name);
        if (!lore.isEmpty()) meta.lore(lore);
        stack.setItemMeta(meta);
        return stack;
    }
}
