package org.red.minecraft.uw.core.skill.effect;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.entity.A_LivingEntity;
import org.red.minecraft.uw.core.UndefinedWorldCore;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.combat.CombatManager;
import org.red.minecraft.uw.core.combat.ElementalType;
import org.red.minecraft.uw.core.combat.damage.DamageType;
import org.red.minecraft.uw.core.skill.CTXType;
import org.red.minecraft.uw.core.skill.SkillCTX;
import org.red.minecraft.uw.core.skill.projectile.ProjectileController;
import org.red.minecraft.uw.core.skill.projectile.ProjectileData;
import org.red.minecraft.uw.core.skill.projectile.ProjectileType;
import org.red.minecraft.uw.core.skill.target.faction.Faction;
import org.red.minecraft.uw.core.skill.target.faction.FactionType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 검기 이펙트 — 레드스톤(DUST) 파티클 궤적의 관통 검기를 전방으로 날린다.
 * 경로상의 적에게 물리 데미지(PHYSICS_DAMAGE × scale × CTX.DAMAGE)를 즉시 가하고,
 * 종료 시 적중 대상들을 LAST_TARGET_INFO에 저장해 후속 노드에 전달한다.
 *
 * SPEED/RANGE/SIZE/DAMAGE CTX 배율이 각각 속도/사거리/판정크기/데미지에 적용되고,
 * 충돌 대상은 TARGET_FACTION CTX를 따른다(없으면 ENEMY). ELEMENTAL/DAMAGE_TYPE도 CTX를 따른다
 * (DAMAGE_TYPE 없으면 PHYSICAL). PROJECTILE_TYPE은 따르지 않는다 — 검기는 관통 고정이다.
 */
public class SwordAuraEffect implements Effect {

    /**
     * 발사 높이 보정.
     * <p>§2.10 확정: <b>발사 높이는 스킬마다 다르게 둔다 — ProjectileEffect(1.5)와 통일하지 않는다.</b>
     */
    private static final double LAUNCH_HEIGHT = 1.2;

    private final double speed;
    private final double range;
    private final double size;
    private final double scale;

    public SwordAuraEffect(double speed, double range, double size, double scale) {
        this.speed = speed;
        this.range = range;
        this.size = size;
        this.scale = scale;
    }

    @Override
    public CompletableFuture<EffectResult> execute(SkillCTX ctx) {
        A_Entity caster = ctx.getCTX(CTXType.CASTER);

        double finalSpeed = speed * (double) ctx.getCTX(CTXType.SPEED);
        double finalRange = range * (double) ctx.getCTX(CTXType.RANGE);
        double finalSize = size * (double) ctx.getCTX(CTXType.SIZE);

        // 데미지 유형은 CTX 우선, 없으면 PHYSICAL (= 이 이펙트가 보유한 기본값).
        // 검기의 PHYSICAL 은 "일반 물리 데미지"라는 기본값일 뿐 이펙트 정체성이 아니라서 CTX를 따르게 했다.
        // (마법 검기는 장르적으로 자연스럽다. 반면 ThunderEffect 의 CHAIN_LIGHTING 은 고유 플래그를 가진
        //  낙뢰 메커니즘 자체라 고정이다 — 두 이펙트의 처리가 다른 이유다)
        DamageType damageType = ctx.getCTX(CTXType.DAMAGE_TYPE, DamageType.PHYSICAL);

        // 공격력 기준 속성도 최종 유형을 따라간다 (DamageEffect 와 동일한 규약)
        AttributeType baseAttribute = damageType == DamageType.MAGIC ? AttributeType.MAGIC_DAMAGE : AttributeType.PHYSICS_DAMAGE;
        double baseDamage = UndefinedWorldCore.getAttributeManager(caster).getAttributeValue(baseAttribute);
        double damage = baseDamage * scale * (double) ctx.getCTX(CTXType.DAMAGE);
        ElementalType elemental = ctx.getCTX(CTXType.ELEMENTAL);

        // 충돌 대상은 CTX 우선, 없으면 ENEMY (ProjectileEffect와 동일한 관용구)
        FactionType faction = ctx.getCTX(CTXType.TARGET_FACTION, FactionType.ENEMY);

        Location start = caster.getLocation().clone().add(0, LAUNCH_HEIGHT, 0);
        // 검기는 관통이 본질이라 PROJECTILE_TYPE CTX를 따르지 않고 PIERCE로 고정한다.
        // (NORMAL이면 첫 적 한 명만 맞고 사라져 "검기"가 성립하지 않는다)
        ProjectileData data = new ProjectileData(
                caster, start, caster.getLocation().getDirection(), finalSpeed, finalRange, finalSize, ProjectileType.PIERCE);

        CompletableFuture<EffectResult> future = new CompletableFuture<>();
        List<A_Entity> hits = new ArrayList<>();

        ProjectileController controller = new ProjectileController(
                data,
                Faction.predicate(caster, faction),
                hitData -> {
                    for (A_Entity target : hitData.entities()) {
                        if (target == null) continue;

                        A_LivingEntity living = target.getALivingEntity();
                        if (living == null || living.isDead()) continue;

                        // 다른 이펙트와 동일하게 대상 단위로 예외를 격리한다.
                        // (여기서 예외가 나가면 Controller.runTick 이 잡아 검기 비행 자체가 즉시 끝난다)
                        try {
                            CombatManager.damage(caster, living, damageType, elemental, damage);
                            hits.add(target);
                        } catch (RuntimeException exception) {
                            UndefinedWorldCorePlugin.sendLog("SwordAuraEffect 대상 처리 실패 target:"
                                    + target.getUniqueIdStr() + " - " + exception);
                        }
                    }
                },
                // PIERCE 타입이라 적중으로는 종료되지 않는다. 모든 종료 경로(사거리 도달 / 월드 소실 /
                // 이동 불가 파라미터 / tick 예외)가 Controller.expire() 로 모이므로 이 콜백이 정확히 1회 실행된다.
                // 따라서 future 완료는 여기 한 곳에서만 한다.
                // 이 콜백이 예외로 빠져나가면 future 가 영구 미완료로 남아 스킬 체인이 조용히 멈춘다.
                // 종료 콜백은 다시 오지 않으므로 어떤 경우에도 여기서 완료시킨다.
                () -> {
                    try {
                        if (hits.isEmpty()) {
                            UndefinedWorldCorePlugin.sendLog("SwordAuraEffect: 적중 대상 없음");
                            future.complete(EffectResult.FAIL);
                            return;
                        }
                        ctx.setCTX(CTXType.LAST_TARGET_INFO, hits.toArray(new A_Entity[0]));
                        future.complete(EffectResult.SUCCESS);
                    } catch (RuntimeException exception) {
                        UndefinedWorldCorePlugin.sendLog("SwordAuraEffect 종료 처리 실패 - " + exception);
                        future.complete(EffectResult.ERROR);
                    }
                }
        );

        // 검기 시각효과: 레드스톤 파티클 궤적 (확정: 파티클 종류 DUST, 색은 속성 규칙을 따른다)
        // 무속성일 때의 회색/파랑은 실제로 가할 데미지 유형을 그대로 쓴다 (색과 데미지가 어긋나지 않도록).
        Particle.DustOptions dust = SkillParticle.dust(elemental, damageType != DamageType.MAGIC);
        controller.setMoveVisual(loc -> {
            if (loc.getWorld() == null) return;
            loc.getWorld().spawnParticle(Particle.DUST, loc, 12, finalSize * 0.4, finalSize * 0.4, finalSize * 0.4, 0, dust);
        });

        controller.start();
        return future;
    }

    @Override
    public EffectType[] getEffectTypes() {
        return new EffectType[]{EffectType.MELEE, EffectType.TARGET};
    }
}
