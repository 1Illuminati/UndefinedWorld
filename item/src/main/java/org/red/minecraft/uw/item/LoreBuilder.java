package org.red.minecraft.uw.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;

import java.util.*;

public class LoreBuilder {
    private final Map<Integer, List<String>> lore = new TreeMap<>();
    private int layerHeight; //각 레이어 간의 공백 크기 1당 1칸씩

    public LoreBuilder setLayerHeight(int layerHeight) {
        // 음수면 build()의 Collections.nCopies가 예외를 던져 아이템 생성 자체가 실패한다
        this.layerHeight = Math.max(0, layerHeight);
        return this;
    }

    public LoreBuilder addLore(int layer, String lore) {
        this.getLayerLore(layer).add(lore);
        return this;
    }

    public List<String> getLayerLore(int layer) {
        return lore.computeIfAbsent(layer, _ -> new ArrayList<>());
    }

    public List<? extends Component> build() {
        UndefinedWorldCorePlugin.sendLog("LoreBuilder Build Test");
        List<Component> result = new ArrayList<>();
        MiniMessage mm = MiniMessage.miniMessage();
        List<Component> padding = Collections.nCopies(layerHeight, Component.empty());

        Iterator<List<String>> iterator = lore.values().iterator();

        while (iterator.hasNext()) {
            for (String line : iterator.next())
                result.add(mm.deserialize(line));

            if (iterator.hasNext())
                result.addAll(padding);
        }

        return result;
    }
}
