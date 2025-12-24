package org.red.minecraft.undefinedworld.item;

public enum U_ItemGrade {
    BEGIN("§7시작"), //기본템
    NORMAL("§f노말"), //노말
    RARE("§9레어"), //레어
    EPIC("§5에픽"), //에픽
    UNIQUE("§e유니크"), //유니크
    LEGEND("§6전설"), //전설
    ACIENT("§4고대"), //고대
    MYTHIC("§d신화"), //신화, 종결장비급
    STAR("§b별"),
    NULL("§0무"),
    END("§8끝");

    public final String krName;
    U_ItemGrade(String krName) {
        this.krName = krName;
    }
}
