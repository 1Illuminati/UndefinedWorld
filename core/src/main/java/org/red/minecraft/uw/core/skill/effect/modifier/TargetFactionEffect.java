package org.red.minecraft.uw.core.skill.effect.modifier;

import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.target.faction.FactionType;

/**
 * 대상 진영({@link CTXType#TARGET_FACTION})을 지정하는 수정자 기어. (ElementalEffect 와 동일한 패턴)
 *
 * <p>열거형이므로 <b>덮어쓴다.</b> 이 기어 이후의 타겟/발사체 기어들이 지정한 진영을 대상으로 삼는다.
 * (예: {@code FRIENDLY} 로 바꾸면 아군에게 날아가는 회복 발사체가 된다)
 *
 * <p>⚠️ 이 기어를 넣으면 뒤따르는 타겟 기어의 <b>YAML {@code faction} 값이 무시된다.</b>
 * CTXType 에 기본값이 없으므로, 이 기어를 쓰지 않으면 각 소비처의 기본값(ENEMY)이 유지된다.
 *
 * <p>소비처: {@code TargetEffect}, {@code ThunderEffect}, {@code ProjectileEffect}, {@code SwordAuraEffect}.
 */
public class TargetFactionEffect extends SimpleModifierEffect<FactionType> {

    public TargetFactionEffect(FactionType faction) {
        super(CTXType.TARGET_FACTION, faction);
    }
}
