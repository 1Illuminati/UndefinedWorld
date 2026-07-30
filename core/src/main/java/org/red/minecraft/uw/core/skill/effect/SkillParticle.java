package org.red.minecraft.uw.core.skill.effect;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.red.minecraft.uw.core.combat.ElementalType;

/**
 * 스킬 시각효과의 색 결정을 한 곳에 모아둔 곳. (확정: 파티클은 레드스톤(DUST)으로 통일)
 *
 * <p>검기(SwordAuraEffect)와 발사체(ProjectileEffect)가 <b>같은 규칙</b>을 써야 하므로
 * 색을 각 이펙트에 흩어 두지 않는다. 새 이펙트가 생겨도 여기만 참조하면 된다.
 *
 * <p>색 규칙 (확정):
 * <ul>
 *   <li>무속성 + 물리 → 회색</li>
 *   <li>무속성 + 마법 → 파란색</li>
 *   <li>그 외 → 각 속성 색. 특히 WATER(청록)는 무속성 마법(파랑)과 시각적으로 구분되게 골랐다.</li>
 * </ul>
 * 색값은 임시 선택이며 추후 변경 가능하도록 상수로 분리했다.
 */
public final class SkillParticle {

    // ── 속성별 색 (todo 밸런스/아트 확정 시 이 값만 교체하면 된다) ──
    private static final Color NEUTRAL_PHYSICAL = Color.fromRGB(0x9E9E9E); // 회색
    private static final Color NEUTRAL_MAGIC    = Color.fromRGB(0x3B6FE0); // 파랑 (로열블루)
    private static final Color FIRE             = Color.fromRGB(0xFF5A1E); // 주황빨강
    private static final Color WATER            = Color.fromRGB(0x00C8C8); // 청록 — 무속성 마법 파랑과 구분
    private static final Color ICE              = Color.fromRGB(0xAEEBFF); // 옅은 하늘
    private static final Color WIND             = Color.fromRGB(0xA8F0A0); // 연녹
    private static final Color LAND             = Color.fromRGB(0x8B5A2B); // 갈색
    private static final Color THUNDER          = Color.fromRGB(0xFFE23A); // 노랑

    /** DUST 파티클 크기 — todo 아트 확정 필요 (기존 검기 값 유지) */
    private static final float DUST_SIZE = 1.5f;

    private SkillParticle() {}

    /**
     * 속성에 맞는 DUST 파티클 옵션을 만든다.
     *
     * @param elemental 스킬 속성 (CTX.ELEMENTAL)
     * @param physical  무속성일 때 물리(true)/마법(false) 구분용.
     *                  속성이 NONE이 아니면 이 값은 색에 영향을 주지 않는다.
     */
    public static Particle.DustOptions dust(ElementalType elemental, boolean physical) {
        return new Particle.DustOptions(color(elemental, physical), DUST_SIZE);
    }

    private static Color color(ElementalType elemental, boolean physical) {
        // elemental 이 null 인 경로는 없어야 하지만, 시각효과 때문에 스킬이 죽으면 안 되므로 무속성으로 본다
        if (elemental == null) return physical ? NEUTRAL_PHYSICAL : NEUTRAL_MAGIC;

        return switch (elemental) {
            case NONE -> physical ? NEUTRAL_PHYSICAL : NEUTRAL_MAGIC;
            case FIRE -> FIRE;
            case WATER -> WATER;
            case ICE -> ICE;
            case WIND -> WIND;
            case LAND -> LAND;
            case THUNDER -> THUNDER;
        };
    }
}
