package org.red.minecraft.uw.core.util.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.dellarte.library.util.A_DataMap;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.attribute.stat.Stat;
import org.red.minecraft.uw.core.player.PlayerHelper;

public class U_PapiPlayer extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "uplayer";
    }

    @Override
    public @NotNull String getAuthor() {
        return "RedKiller";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        A_Player aPlayer = CommediaDellarte.getAPlayer(player);
        PlayerHelper helper = new PlayerHelper(aPlayer);

        return String.valueOf(switch (identifier) {
            case "statPoint" -> helper.getStatPoint();
            case "mana" -> helper.getMana();
            case "stamina"-> helper.getStamina();
            default -> {
                if (identifier.startsWith("attribute_")) {
                    identifier = identifier.replace("attribute_", "");
                    AttributeType type = AttributeType.byName(identifier);

                    if (type == null) yield null;

                    yield  helper.getAttributeValue(type);
                } else if (identifier.startsWith("stat_")) {
                    identifier = identifier.replace("stat_", "");
                    Stat type = Stat.name(identifier);

                    if (type == null) yield null;

                    yield helper.getStatValue(type);
                } else yield null;
            }
        });
    }
}
