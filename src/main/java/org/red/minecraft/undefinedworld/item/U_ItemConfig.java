package org.red.minecraft.undefinedworld.item;

import java.util.List;

public record U_ItemConfig(U_ItemType type, U_ItemGrade grade, String displayName, List<String> description, boolean isUpgradable) {

}
