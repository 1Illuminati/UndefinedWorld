package org.red.minecraft.uw.core.skill.slot;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.dellarte.library.util.A_DataMap;
import org.red.minecraft.uw.core.StaticValue;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.exeception.PowerOverException;
import org.red.minecraft.uw.core.item.gear.GearItem;
import org.red.minecraft.uw.core.skill.SkillDefinition;
import org.red.minecraft.uw.core.skill.SkillEngine;
import org.red.minecraft.uw.core.skill.craft.SkillItem;
import org.red.minecraft.uw.core.skill.gear.Gear;

import java.util.ArrayList;
import java.util.List;

/**
 * 스킬 슬롯 장착/발동 관리. (§2.6 확정: 슬롯이 스킬 아이템을 보관한다)
 *
 * <p>저장 구조 (플레이어 A_DataMap의 skill_data 하위):
 * <pre>
 *   slots: { 슬롯이름 -> 스킬 아이템 ItemStack }
 * </pre>
 *
 * <p>장착하면 스킬 아이템이 인벤토리에서 <b>빠져 슬롯에 보관</b>되고 해제하면 돌려받는다.
 * 아이템이 세상에 1개만 존재하게 만들어 복사를 원천 차단하는 구조이므로,
 * 보관/반환은 반드시 "인벤토리에서 제거 → 저장" / "저장 제거 → 반환" 순서를 지켜야 한다.
 *
 * <p>스킬 구성 자체는 아이템 PDC가 SSOT다({@link SkillItem}). 예전의
 * {@code skill_data.skills}(이름 → 기어코드 리스트) 맵은 더 이상 쓰지 않는다.
 * todo 기존 저장 데이터 이관 여부 미확정 — 예전 skills 맵과 문자열로 저장된 slots 값은
 *      읽지 않고 <b>그대로 둔다</b>(임의 삭제 금지). 이관/삭제는 사용자 확정 후 진행.
 */
public final class PlayerSkillManager {

    /**
     * 스킬 이름 최대 길이.
     * todo 상한값은 임시 — 사용자 확정 필요 (§2.6: 이름 길이 상한은 현행 유지)
     */
    public static final int MAX_NAME_LENGTH = 32;

    private PlayerSkillManager() {}

    // ── 이름 검증 ─────────────────────────────────────

    /**
     * 저장 가능한 스킬 이름인지 판정. (§2.10 확정: 문자·숫자만, 특수문자 불가)
     *
     * <p>공백도 특수문자로 보아 거절한다(§2.10 "공백 포함 불가" 확정).
     * 길이 상한은 현행 유지({@link #MAX_NAME_LENGTH}).
     *
     * <p>판정은 로케일에 의존하지 않는다 — {@code toLowerCase} 같은 로케일 변환을 쓰지 않고
     * 코드포인트를 직접 본다. 서로게이트 페어의 반쪽(대리 문자)만 검사해 이모지 같은 보충 평면
     * 문자가 통과하지 않도록 <b>코드포인트 단위</b>로 순회한다.
     */
    public static boolean isValidName(@Nullable String name) {
        // 빈 문자열은 아래 순회가 한 번도 돌지 않아 통과해버린다 — 여기서 먼저 걸러야 한다
        if (name == null || name.isBlank()) return false;
        // 길이는 현행대로 UTF-16 길이로 잰다 (보충 평면 문자는 2로 세어 더 엄격해진다 — 상한 정책 변경 아님)
        if (name.length() > MAX_NAME_LENGTH) return false;

        for (int i = 0; i < name.length(); ) {
            int codePoint = name.codePointAt(i);
            if (!isAllowedNameChar(codePoint)) return false;
            i += Character.charCount(codePoint);
        }
        return true;
    }

    /**
     * 스킬 이름에 쓸 수 있는 문자인지. (확정: 유니코드 문자(letter) 또는 숫자(digit)만)
     *
     * <p>한글·영문자뿐 아니라 일본어·키릴 문자·전각 숫자 등도 문자/숫자이므로 허용된다.
     * 한글을 따로 판정하지 않는 이유가 이것이다 — 한글 음절도 자모도 유니코드상 문자다.
     * 반대로 기호·구두점·공백·이모지는 문자도 숫자도 아니라 자연히 거절된다.
     */
    private static boolean isAllowedNameChar(int codePoint) {
        return Character.isLetterOrDigit(codePoint);
    }

    /**
     * 이름 규칙 안내 문구.
     * 명령어·제작 GUI·수정 차단이 모두 같은 문구를 쓰도록 한 곳에 둔다
     * (규칙이 바뀌었을 때 안내만 옛 규칙으로 남는 상황을 막는다).
     */
    public static String nameRuleMessage() {
        return String.format("스킬 이름은 문자와 숫자만 사용할 수 있으며 1~%d자여야 합니다. (공백·특수문자 불가)",
                MAX_NAME_LENGTH); //todo 문구/형식 사용자 확정 필요
    }

    // ── 슬롯 보관/반환 ─────────────────────────────────

    /**
     * 슬롯에 보관된 스킬 아이템. 미장착이면 null.
     *
     * <p>A_DataMap은 조회만으로 기본값을 써넣으므로 containsKey로 먼저 확인한다.
     * 저장 데이터와 호출부가 같은 인스턴스를 공유하지 않도록 복사해서 돌려준다.
     */
    @Nullable
    public static ItemStack getEquippedItem(A_Player player, SkillSlot slot) {
        A_DataMap slots = slots(player);
        if (!slots.containsKey(slot.name())) return null;

        // getItemStack은 무조건 ItemStack으로 캐스팅하므로 타입을 직접 확인해야 예외로 죽지 않는다.
        Object stored = slots.getMap().get(slot.name());
        if (stored instanceof ItemStack stack) {
            if (stack.isEmpty() || !SkillItem.isSkillItem(stack)) return null;
            return stack.clone();
        }

        // 예전 구조에서는 슬롯에 스킬 "이름"(String)이 들어 있었다 — 알려진 상태라 로그를 남기지 않는다
        // (매 우클릭마다 호출되는 경로라 로그를 남기면 스팸이 된다. 해제 명령으로 정리된다)
        if (!(stored instanceof String))
            UndefinedWorldCorePlugin.sendLog("Skill slot ignored (unexpected value): " + player.getName()
                    + " slot=" + slot.name() + " value=" + stored);

        return null;
    }

    /**
     * 슬롯 보관 내용 갱신.
     * GUI/인벤토리에서 꺼낸 ItemStack은 슬롯을 비추는 참조일 수 있어 그대로 저장하면
     * 이후 인벤토리 조작이 저장 데이터를 조용히 바꾼다 → 반드시 복사해서 저장한다.
     */
    public static void setEquippedItem(A_Player player, SkillSlot slot, @Nullable ItemStack stack) {
        A_DataMap slots = slots(player);
        if (stack == null || stack.isEmpty()) slots.remove(slot.name());
        else slots.put(slot.name(), stack.clone());
    }

    /** 슬롯에 장착된 스킬 이름. 미장착이면 null (표시용) */
    @Nullable
    public static String getEquippedName(A_Player player, SkillSlot slot) {
        return SkillItem.getSkillName(getEquippedItem(player, slot));
    }

    // ── 시전 ─────────────────────────────────────────

    /** 슬롯에 보관된 스킬 시전. 장착 스킬이 없으면 false */
    public static boolean cast(A_Player player, SkillSlot slot) {
        ItemStack skillItem = getEquippedItem(player, slot);
        if (skillItem == null) return false;

        return castItem(player, skillItem);
    }

    /** 스킬 아이템으로 직접 시전 */
    public static boolean castItem(A_Player player, ItemStack skillItem) {
        SkillDefinition skill = buildSkill(skillItem);
        if (skill == null) {
            player.sendMessage("스킬을 구성할 수 없습니다: " + SkillItem.getSkillName(skillItem)); //todo 문구/형식 사용자 확정 필요
            return false;
        }

        SkillEngine.runSkill(player, skill);
        return true;
    }

    /**
     * 스킬 아이템의 기어 코드로 SkillDefinition을 조립한다.
     * 기어 아이템이 사라졌거나(Nexo 설정 변경 등) 파워 초과면 null.
     */
    @Nullable
    public static SkillDefinition buildSkill(@Nullable ItemStack skillItem) {
        String name = SkillItem.getSkillName(skillItem);
        if (name == null) return null;

        List<String> gearCodes = SkillItem.getGearCodes(skillItem);
        if (gearCodes.isEmpty()) return null;

        List<Gear> gears = new ArrayList<>();
        for (String code : gearCodes) {
            // 표기용 resolveGears와 달리 여기서는 하나라도 빠지면 안 된다 (다른 스킬이 되어버린다)
            if (!(UndefinedWorldCore.getItem(code) instanceof GearItem gearItem)) {
                UndefinedWorldCorePlugin.sendLog("Gear not found: " + code + " (skill: " + name + ")");
                return null;
            }
            gears.add(gearItem.toGear());
        }

        try {
            return new SkillDefinition(name, gears);
        } catch (PowerOverException e) {
            UndefinedWorldCorePlugin.sendLog("Skill power over: " + name);
            return null;
        } catch (IllegalArgumentException e) {
            UndefinedWorldCorePlugin.sendLog("Invalid skill composition: " + name + " / " + e.getMessage());
            return null;
        }
    }

    // ── 저장소 ────────────────────────────────────────

    private static A_DataMap root(A_Player player) {
        return player.getDataMap(UndefinedWorldCorePlugin.instance).getDataMap(StaticValue.SKILL_MAP_KEY);
    }

    private static A_DataMap slots(A_Player player) {
        return root(player).getDataMap("slots");
    }
}
