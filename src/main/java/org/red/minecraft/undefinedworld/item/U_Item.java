package org.red.minecraft.undefinedworld.item;

import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.red.library.data.serialize.DataMapSerializable;

/**
 * 1. 아이템 기본 골격 50% 이상 완성
 * 2. 아이템 매니져 및 보조 코드들 완성
 * - 아이템 매니져, 아이템toDataMap, 저장시스템, 로드시스템, ItemStack -> U_Item
 * # 고민과제: 각각 개별의 특정 데이터를 가진 아이템들의 로어 생성
 * 3. 아이템 제작을 위한 ui완성
 * 4. 커맨드 완성
 */
public interface U_Item extends DataMapSerializable {
    NamespacedKey KEY = new NamespacedKey("test", "itemcode");

    String getItemCode();

    U_ItemType getType();

    U_ItemGrade getGrade();

    String getDisplayName();

    List<String> getDescription();

    void setType(U_ItemType type);

    void setGrade(U_ItemGrade grade);

    void setDisplayName(String displayName);

    void setDescription(List<String> description);

    void setUpgradeAble(boolean able);

    boolean isUpgradeAble();

    List<String> createLore();

    ItemStack setItem(ItemStack itemStack);
}
