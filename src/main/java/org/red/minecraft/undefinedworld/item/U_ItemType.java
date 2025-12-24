package org.red.minecraft.undefinedworld.item;

public enum U_ItemType {
    RESOURCE("재료"),
    ORE("광석"),
    FOOD("음식"),
    POTION("포션"),
    ARMOR("방어구"),
    WEAPON("무기"),
    SUB_WEAPON("보조무기"),
    ACCESSORY("장신구"),
    SCROLL("주문서"),
    SPECIAL("스페셜"),
    SKILL_GEAR("스킬기어"),
    SKILL_ARTIFACT("스킬"),
    TOOL("도구");

    public final String krName;
    U_ItemType(String krName) {
        this.krName = krName;
    }
}
