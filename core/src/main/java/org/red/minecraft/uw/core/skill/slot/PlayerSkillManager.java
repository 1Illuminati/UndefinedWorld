package org.red.minecraft.uw.core.skill.slot;

import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.dellarte.library.util.A_DataMap;
import org.red.minecraft.uw.core.StaticValue;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.exeception.PowerOverException;
import org.red.minecraft.uw.core.item.U_Item;
import org.red.minecraft.uw.core.item.gear.GearItem;
import org.red.minecraft.uw.core.skill.SkillDefinition;
import org.red.minecraft.uw.core.skill.SkillEngine;
import org.red.minecraft.uw.core.skill.gear.Gear;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 플레이어의 제작 스킬 저장/슬롯 장착/발동 관리.
 *
 * 저장 구조 (플레이어 A_DataMap의 skill_data 하위):
 *   skills: { 스킬이름 -> 기어 아이템 코드 리스트 }   — 제작 GUI가 저장 (기어 코드로 저장해 재부팅에도 유지)
 *   slots:  { 슬롯이름 -> 스킬이름 }                — 슬롯 장착 정보
 *
 * 시전 시 기어 코드를 아이템 데이터(IItemModule)로 되살려 SkillDefinition을 조립한다.
 */
public final class PlayerSkillManager {

    private PlayerSkillManager() {}

    // ── 스킬 저장/조회 ─────────────────────────────────

    public static void saveSkill(A_Player player, String name, List<String> gearCodes) {
        skills(player).put(name, gearCodes);
    }

    public static boolean hasSkill(A_Player player, String name) {
        return skills(player).containsKey(name);
    }

    public static Set<String> getSkillNames(A_Player player) {
        return skills(player).keySet();
    }

    // ── 슬롯 장착 ─────────────────────────────────────

    public static void equip(A_Player player, SkillSlot slot, String skillName) {
        slots(player).put(slot.name(), skillName);
    }

    public static void unequip(A_Player player, SkillSlot slot) {
        slots(player).remove(slot.name());
    }

    @Nullable
    public static String getEquipped(A_Player player, SkillSlot slot) {
        return slots(player).getString(slot.name());
    }

    // ── 시전 ─────────────────────────────────────────

    /** 슬롯에 장착된 스킬 시전. 장착 스킬이 없으면 false */
    public static boolean cast(A_Player player, SkillSlot slot) {
        String name = getEquipped(player, slot);
        if (name == null) return false;

        return castByName(player, name);
    }

    /** 이름으로 스킬 시전 (테스트용 직접 시전 포함) */
    public static boolean castByName(A_Player player, String name) {
        SkillDefinition skill = buildSkill(player, name);
        if (skill == null) {
            player.sendMessage("스킬을 구성할 수 없습니다: " + name); //todo 문구/형식 사용자 확정 필요
            return false;
        }

        SkillEngine.runSkill(player, skill);
        return true;
    }

    /**
     * 저장된 기어 코드 목록으로 SkillDefinition 조립.
     * 기어 아이템이 사라졌거나(Nexo 설정 변경 등) 파워 초과면 null.
     */
    @Nullable
    public static SkillDefinition buildSkill(A_Player player, String name) {
        List<String> gearCodes = skills(player).getList(name);
        if (gearCodes == null || gearCodes.isEmpty()) return null;

        List<Gear> gears = new ArrayList<>();
        for (String code : gearCodes) {
            U_Item item = UndefinedWorldCore.getItem(code);
            if (!(item instanceof GearItem gearItem)) {
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
        }
    }

    // ── 저장소 ────────────────────────────────────────

    private static A_DataMap root(A_Player player) {
        return player.getDataMap(UndefinedWorldCorePlugin.instance).getDataMap(StaticValue.SKILL_MAP_KEY);
    }

    private static A_DataMap skills(A_Player player) {
        return root(player).getDataMap("skills");
    }

    private static A_DataMap slots(A_Player player) {
        return root(player).getDataMap("slots");
    }
}
