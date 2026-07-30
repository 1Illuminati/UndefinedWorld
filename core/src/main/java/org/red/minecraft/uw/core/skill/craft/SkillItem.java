package org.red.minecraft.uw.core.skill.craft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.item.gear.GearItem;
import org.red.minecraft.uw.core.skill.gear.Gear;

import java.util.ArrayList;
import java.util.List;

/**
 * 스킬 아이템. (§2.6 확정: 제작하면 완성된 스킬 아이템 1개로 제공된다)
 *
 * <p><b>스택 PDC가 스킬 구성의 단일 진실 공급원(SSOT)</b>이다. 플레이어 A_DataMap에 스킬 목록을
 * 따로 두지 않는다 — 아이템이 곧 저장소이므로 스킬 개수 상한이 없고 재부팅 복원도 아이템을 따라간다.
 * <ul>
 *   <li>{@code skill_name}  (STRING)       — 스킬 이름</li>
 *   <li>{@code skill_gears} (LIST&lt;STRING&gt;) — 기어 아이템 코드 목록 (앞에서부터 실행 순서)</li>
 * </ul>
 *
 * <p>기어 목록을 문자열 결합이 아니라 LIST 타입으로 저장하는 이유: 구분자가 없어 기어 코드에 어떤
 * 문자가 들어가도 복원이 깨지지 않고, 잘린 값이 조용히 다른 기어로 해석되지 않는다.
 *
 * <p>재질을 ENCHANTED_BOOK으로 잡은 이유는 <b>최대 스택이 1</b>이라 겹침이 원천적으로 불가능하기
 * 때문이다. 겹칠 수 있는 재질을 쓰면 구성이 같은 두 스킬이 하나로 합쳐져 사라진다.
 * todo 외형(재질/이름 색)은 임의 선택 — 추후 변경 가능
 */
public final class SkillItem {

    /** 최대 스택 1인 재질이어야 한다 (겹침으로 인한 스킬 소실 방지) */
    private static final Material MATERIAL = Material.ENCHANTED_BOOK;

    private static final String NAME_KEY_NAME = "skill_name";
    private static final String GEARS_KEY_NAME = "skill_gears";

    /** 1초에 해당하는 틱 수 (내부 처리는 틱, 플레이어 표기는 초 — §2.6) */
    private static final double TICKS_PER_SECOND = 20.0;

    private SkillItem() {}

    // ── 생성 ─────────────────────────────────────────

    /**
     * 스킬 아이템 1개를 만든다.
     * @param gearCodes 배치 순서대로의 기어 아이템 코드 (비어 있으면 안 된다)
     */
    public static ItemStack create(String skillName, List<String> gearCodes) {
        ItemStack stack = new ItemStack(MATERIAL);
        ItemMeta meta = stack.getItemMeta();

        meta.displayName(Component.text(skillName, NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
        meta.lore(buildLore(gearCodes));

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(nameKey(), PersistentDataType.STRING, skillName);
        pdc.set(gearsKey(), PersistentDataType.LIST.strings(), List.copyOf(gearCodes));

        stack.setItemMeta(meta);
        return stack;
    }

    // ── 판정/조회 ─────────────────────────────────────

    /**
     * 스킬 아이템인지 판정. 이름과 기어 목록이 모두 정상이어야 인정한다.
     * getItemMeta는 호출할 때마다 메타를 복사하므로 PDC를 한 번만 읽는다.
     */
    public static boolean isSkillItem(@Nullable ItemStack stack) {
        PersistentDataContainer pdc = containerOf(stack);
        if (pdc == null) return false;

        String name = pdc.get(nameKey(), PersistentDataType.STRING);
        if (name == null || name.isBlank()) return false;

        List<String> codes = pdc.get(gearsKey(), PersistentDataType.LIST.strings());
        return codes != null && !codes.isEmpty();
    }

    /** 스킬 이름. 스킬 아이템이 아니면 null */
    @Nullable
    public static String getSkillName(@Nullable ItemStack stack) {
        PersistentDataContainer pdc = containerOf(stack);
        if (pdc == null) return null;

        String name = pdc.get(nameKey(), PersistentDataType.STRING);
        return (name == null || name.isBlank()) ? null : name;
    }

    /** 기어 코드 목록(실행 순서). 스킬 아이템이 아니면 빈 리스트 */
    public static List<String> getGearCodes(@Nullable ItemStack stack) {
        PersistentDataContainer pdc = containerOf(stack);
        if (pdc == null) return List.of();

        List<String> codes = pdc.get(gearsKey(), PersistentDataType.LIST.strings());
        return codes == null ? List.of() : List.copyOf(codes);
    }

    @Nullable
    private static PersistentDataContainer containerOf(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;

        ItemMeta meta = stack.getItemMeta();
        return meta == null ? null : meta.getPersistentDataContainer();
    }

    private static NamespacedKey nameKey() {
        return new NamespacedKey(UndefinedWorldCorePlugin.instance, NAME_KEY_NAME);
    }

    private static NamespacedKey gearsKey() {
        return new NamespacedKey(UndefinedWorldCorePlugin.instance, GEARS_KEY_NAME);
    }

    // ── 기어 복원 ─────────────────────────────────────

    /**
     * 기어 코드를 Gear 데이터로 되살린다. 찾지 못한 코드는 <b>건너뛴다</b>(표기 계산용).
     * 실행/수정처럼 누락이 곧 손실인 경로에서는 {@link #resolveGearItems(List)}를 써야 한다.
     */
    public static List<Gear> resolveGears(List<String> gearCodes) {
        List<Gear> gears = new ArrayList<>();

        for (String code : gearCodes) {
            if (!(UndefinedWorldCore.getItem(code) instanceof GearItem gearItem)) {
                UndefinedWorldCorePlugin.sendLog("Gear not found while resolving skill item: " + code);
                continue;
            }
            gears.add(gearItem.toGear());
        }

        return gears;
    }

    /**
     * 기어 코드를 실제 기어 아이템으로 되살린다. (수정 GUI에서 기어를 꺼내줄 때 사용)
     *
     * <p>하나라도 되살리지 못하면 <b>null</b>을 돌려준다 — 일부만 꺼내주면 나머지 기어가 조용히
     * 사라지므로, 부분 성공을 허용하지 않고 수정 자체를 막는 편이 안전하다.
     */
    @Nullable
    public static List<ItemStack> resolveGearItems(List<String> gearCodes) {
        List<ItemStack> stacks = new ArrayList<>();

        for (String code : gearCodes) {
            if (!(UndefinedWorldCore.getItem(code) instanceof GearItem gearItem)) {
                UndefinedWorldCorePlugin.sendLog("Gear item not found, skill edit blocked: " + code);
                return null;
            }

            ItemStack stack = gearItem.createItemStack();
            if (stack == null || stack.isEmpty()) {
                UndefinedWorldCorePlugin.sendLog("Gear item could not be built, skill edit blocked: " + code);
                return null;
            }

            stack.setAmount(1); // 기어는 1칸 1개로 배치한다
            stacks.add(stack);
        }

        return stacks;
    }

    // ── 합계/표기 ─────────────────────────────────────

    /** 기어 합계 (SkillDefinition.calcStats와 동일 기준: 쿨타임 = 쿨 합 + 파워×2) */
    public record Summary(int count, int power, int coolDown, int castingTime) {}

    /**
     * 기어 목록의 파워/쿨타임/캐스팅 합계.
     * SkillDefinition을 만들지 않고 계산한다 — 생성자는 ConversionEffect에 상태를 심는 부수효과가
     * 있어 단순 표기 목적으로 호출하면 안 된다.
     */
    public static Summary summarize(List<Gear> gears) {
        int power = 0, cool = 0, casting = 0;

        for (Gear gear : gears) {
            power += gear.getPower();
            cool += gear.getCool();
            casting += gear.getCastingTime();
        }

        return new Summary(gears.size(), power, cool + power * 2, casting);
    }

    /**
     * 캐스팅 시간 표기. (§2.6 확정: 내부는 틱, 플레이어 표기는 초)
     * 틱이 20으로 나누어떨어지지 않을 수 있어 소수점 한 자리까지 보여준다.
     */
    public static String formatCastingTime(int ticks) {
        return String.format("%.1f초", ticks / TICKS_PER_SECOND);
    }

    /** 스킬 아이템 로어. 구성(기어 순서)과 합계를 아이템만 봐도 확인할 수 있게 한다 */
    private static List<Component> buildLore(List<String> gearCodes) {
        List<Gear> gears = resolveGears(gearCodes);
        Summary summary = summarize(gears);

        List<Component> lore = new ArrayList<>();
        lore.add(line("기어 " + gearCodes.size() + "개 (위에서부터 실행 순서)"));

        int index = 1;
        for (String code : gearCodes) {
            lore.add(line(" " + index + ". " + code));
            index++;
        }

        lore.add(line("파워: " + summary.power()));
        lore.add(line("쿨타임: " + summary.coolDown() + "초"));
        lore.add(line("캐스팅: " + formatCastingTime(summary.castingTime())));

        // 표기 합계는 되살린 기어만 반영한다 — 누락을 감추지 않고 드러낸다
        if (gears.size() != gearCodes.size())
            lore.add(line("※ 확인할 수 없는 기어가 있어 합계가 정확하지 않습니다"));

        lore.add(line("들고 우클릭하면 수정할 수 있습니다"));
        return lore; //todo 로어 문구/색상 사용자 확정 필요 (스킬 아이템은 core가 만드는 아이템이라 Nexo 로어가 없다)
    }

    private static Component line(String text) {
        return Component.text(text, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false);
    }
}
