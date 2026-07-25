package org.red.minecraft.uw.core.skill.condition;

import org.bukkit.inventory.ItemStack;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.item.U_Item;
import org.red.minecraft.uw.core.item.attribute.equipment.WeaponItem;
import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;

/**
 * 시전자가 특정 무기를 주손에 들고 있을 때 통과하는 조건. (아이템 코드 기준)
 * 무기 감지 규칙(T19-4)과 동일하게 주손만 검사한다.
 * todo 무기 "종류"(EquipmentType) 기준 조건이 필요하면 별도 확정 후 추가
 */
public record WeaponCondition(String itemCode) implements Condition {

    @Override
    public boolean test(SkillCTX ctx) {
        A_Entity caster = ctx.getCTX(CTXType.CASTER);
        if (!(caster instanceof A_Player player)) return false;

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.isEmpty()) return false;

        U_Item item = UndefinedWorldCore.getItem(mainHand);
        return item instanceof WeaponItem weapon && weapon.getItemCode().equals(itemCode);
    }
}
