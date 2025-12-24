package org.red.minecraft.undefinedworld.item;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.red.library.data.DataMap;
import org.red.minecraft.undefinedworld.UndefinedWorldPlugin;

public final class U_ItemManager {
    public static final String UITEM = "uitem";
    private final DataMap map;

    public U_ItemManager(DataMap map) {
        this.map = map;
    }

    @Nullable
    public U_Item get(String code) {
        return map.getClass(code, U_Item.class, null);
    }

    public void set(@NotNull String code, @NotNull U_Item item) {
        this.map.put(code, item);
    }

    public boolean has(String code) {
        return this.map.containsKey(code);
    }

    public void saveAll() {
        UndefinedWorldPlugin.stroage.saveData(UITEM);
    }

    public void loadAllKeyAll() {
        UndefinedWorldPlugin.stroage.loadData(UITEM);
    }
}
