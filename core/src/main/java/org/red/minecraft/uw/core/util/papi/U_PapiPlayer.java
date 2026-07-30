package org.red.minecraft.uw.core.util.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.dellarte.library.CommediaDellarte;
import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.attribute.stat.Stat;
import org.red.minecraft.uw.core.player.PlayerHelper;

import java.util.Locale;

public class U_PapiPlayer extends PlaceholderExpansion {

    private static final String ATTRIBUTE_PREFIX = "attribute_";
    private static final String STAT_PREFIX = "stat_";

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

    /**
     * 알 수 없는 플레이스홀더는 null을 돌려줘야 PAPI가 원문을 그대로 남긴다.
     * (값을 무조건 String.valueOf로 감싸면 "null" 이라는 문자열이 화면에 표시된다)
     */
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return null; // PAPI는 플레이어 없는 문맥에서도 호출한다

        A_Player aPlayer = CommediaDellarte.getAPlayer(player);
        PlayerHelper helper = new PlayerHelper(aPlayer);

        Object value = resolve(helper, identifier);
        return value == null ? null : format(value);
    }

    /**
     * 표시용 문자열 변환. (확정: 수치는 소수점 한 자리까지 표기)
     * 정수 계열(스탯 포인트 등)은 그대로 두고, 실수만 한 자리로 자른다.
     */
    private String format(Object value) {
        if (value instanceof Double d) {
            if (!Double.isFinite(d)) return "0.0"; // NaN/무한이 화면에 그대로 노출되지 않게 한다
            return String.format(Locale.ROOT, "%.1f", d);
        }
        return String.valueOf(value);
    }

    @Nullable
    private Object resolve(PlayerHelper helper, String identifier) {
        switch (identifier) {
            case "statPoint" -> {
                return helper.getStatPoint();
            }
            case "mana" -> {
                return helper.getMana();
            }
            case "stamina" -> {
                return helper.getStamina();
            }
        }

        if (identifier.startsWith(ATTRIBUTE_PREFIX)) {
            // replace가 아니라 접두어 길이만큼 자른다 (이름 안에 접두어가 또 있어도 안전)
            String name = identifier.substring(ATTRIBUTE_PREFIX.length()).toUpperCase(Locale.ROOT);
            AttributeType type = AttributeType.byName(name);
            return type == null ? null : helper.getAttributeValue(type);
        }

        if (identifier.startsWith(STAT_PREFIX)) {
            String name = identifier.substring(STAT_PREFIX.length()).toUpperCase(Locale.ROOT);
            Stat type = Stat.name(name);
            return type == null ? null : helper.getStatValue(type);
        }

        return null;
    }
}
