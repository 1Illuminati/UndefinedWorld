package org.red.minecraft.uw.core.skill;

import org.jetbrains.annotations.Nullable;
import org.red.minecraft.uw.core.exeception.FixedClassSetException;
import org.red.minecraft.uw.core.exeception.PowerOverException;
import org.red.minecraft.uw.core.skill.cost.CostData;
import org.red.minecraft.uw.core.skill.effect.conversion.ConversionEffect;
import org.red.minecraft.uw.core.skill.gear.Gear;
import org.red.minecraft.uw.core.util.CanFix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SkillDefinition implements CanFix {

    private final String skillName;
    private final List<Gear> gears;
    private final CostData costData;
    private int skillCoolDown;
    private int skillPower;
    private int castingTime;
    private List<List<SkillNode>> nodes; // 외부엔 불변으로 노출

    public SkillDefinition(String skillName, List<Gear> gears) {
        this.skillName = skillName;
        this.gears = List.copyOf(gears); // 방어적 복사 + 불변

        validateGears();

        // 파워 검증(calcStats)을 노드/변환 구성보다 먼저 한다.
        // ConversionEffect.setConversion은 기어 아이템당 싱글턴인 Effect 인스턴스에 상태를 심으므로,
        // 검증에서 버려질 정의를 만들면서 전역 상태를 건드리는 순서가 되면 안 된다.
        int[] stats = calcStats();
        this.skillPower = stats[0];
        this.skillCoolDown = stats[1] + (stats[0] * 2);
        this.castingTime = stats[2];

        this.costData = buildCostData();
        this.nodes = buildNodes();
        this.setConversions(); // ConversionEffect(Merge 등) 노드 구조 변환 적용
    }

    private SkillDefinition(String skillName, List<Gear> gears, CostData costData, int  skillCoolDown, int skillPower, int castingTime, List<List<SkillNode>> nodes) {
        this.skillName = skillName;
        this.gears = gears;
        this.costData = costData;
        this.skillCoolDown = skillCoolDown;
        this.skillPower = skillPower;
        this.castingTime = castingTime;
        this.nodes = nodes;
    }


    /**
     * ConversionEffect(Merge 등)에 노드 구조 변환을 위임한다.
     * <p>주의: 구현체가 setNodes로 this.nodes를 교체할 수 있다. 이 순회는 교체 이전의 리스트 객체를
     * 대상으로 계속 진행되므로 모든 기어가 빠짐없이 방문되지만, 두 번째 이후의 ConversionEffect에
     * 전달되는 effectNode는 교체 전 구조의 노드다.
     * todo 확정 필요 — effectNode를 실제로 사용하는 ConversionEffect가 추가되면 이 순회 방식을
     *      재검토해야 한다. (MergeEffect는 gears만 참조해 멱등이라 현재는 문제없음)
     */
    private void setConversions() {
        for (List<SkillNode> nodes : this.nodes) {
            for (SkillNode node : nodes) {
                if (node.gear.getEffect() instanceof ConversionEffect conversionEffect)
                    conversionEffect.setConversion(this, node);
            }
        }
    }

    // ── 검증 ──────────────────────────────────────────

    private void validateGears() {
        if (gears == null || gears.isEmpty())
            throw new IllegalArgumentException("Gears must not be empty");
    }

    // ── 빌드 헬퍼 ─────────────────────────────────────

    private CostData buildCostData() {
        CostData costData = new CostData();
        for (Gear gear : gears)
            costData.addCost(gear.getCosts());
        return costData;
    }

    /**
     * 파워/쿨/캐스팅 합산.
     * <p>파워는 <b>총합이 9 이하이기만 하면 된다</b>. 음수 파워 기어는 허용이다(§2.6 스킬 코어 6).
     * 그래서 루프 안에서 부분합을 검사하지 않는다 — 부분합 검사는 뒤에 음수 기어가 오는 조합을
     * 총합이 9 이하인데도 거부한다.
     * <p>쿨타임/캐스팅 합산값도 음수가 될 수 있다. 소비 측(SkillEngine)에서 0 이하를 "없음"으로 처리한다.
     * 쿨타임 단위는 초, 캐스팅 단위는 틱이다.
     */
    private int[] calcStats() {
        int cool = 0, power = 0, cast = 0;
        for (Gear gear : gears) {
            power += gear.getPower();
            cool += gear.getCool();
            cast += gear.getCastingTime();
        }

        if (power > 9) throw new PowerOverException(power);

        return new int[]{ power, cool, cast };
    }

    private List<List<SkillNode>> buildNodes() {
        List<SkillNode> next = new ArrayList<>();
        LinkedList<List<SkillNode>> nodes = new LinkedList<>();
        for (int i = gears.size() - 1; i >= 0; i--) {
            next = List.of(new SkillNode(gears.get(i), next));
            nodes.addFirst(next);
        }
        return nodes;
        //return Collections.unmodifiableList(nodes);
    }

    // ── Getters ───────────────────────────────────────

    public String getSkillName()      { return skillName; }
    public List<Gear> getGears()      { return gears; }       // 이미 불변
    public CostData getCostData()     { return costData; }

    /** 쿨타임 (초). 0 이하면 쿨타임 없음. */
    public int getSkillCoolDown()     { return skillCoolDown; }
    public int getSkillPower()        { return skillPower; }
    /** 캐스팅 시간 (<b>틱</b>). 플레이어에게 보여줄 때만 초로 환산한다 (§2.6). 0 이하면 즉시 시전. */
    public int getCastingTime()    { return castingTime; }
    // 필드 주석대로 외부엔 불변으로 노출한다 (레벨 리스트 자체는 이미 List.of/List.copyOf로 불변)
    public List<List<SkillNode>> getNodes() { return Collections.unmodifiableList(nodes); }
    public void setSkillCoolDown(int skillCoolDown) {  this.skillCoolDown = skillCoolDown; }
    public void setSkillPower(int skillPower) {  this.skillPower = skillPower; }
    public void setCastingTime(int castingTime) {  this.castingTime = castingTime; }
    public void setNodes(List<List<SkillNode>> nodes) { this.nodes = Objects.requireNonNull(nodes, "nodes"); }

    public List<SkillNode> getFirstNode() {
        return nodes.isEmpty() ? null : nodes.getFirst();
    }

    @Override
    public boolean isFixed() {
        return false;
    }

    @Override
    public SkillDefinition getFixed() {
        return new FixedSkillDef(skillName, gears, costData, skillCoolDown, skillPower, castingTime, Collections.unmodifiableList(nodes));
    }

    // ── SkillNode ─────────────────────────────────────

    public record SkillNode(Gear gear, List<SkillNode> nextNode) {}

    // static 중첩 클래스여야 한다. non-static inner로 두면 getFixed()로 만든 객체가
    // 원본 SkillDefinition 인스턴스를 암묵적으로 계속 참조한다.
    private static final class FixedSkillDef extends SkillDefinition {
        public FixedSkillDef(String skillName, List<Gear> gears, CostData costData, int  skillCoolDown, int skillPower, int castingTime, List<List<SkillNode>> nodes) {
            super(skillName, gears, costData, skillCoolDown, skillPower, castingTime, nodes);
        }

        @Override
        public void setSkillCoolDown(int skillCoolDown) { throw new FixedClassSetException("SkillDefinition.skillCoolDown"); }
        @Override
        public void setSkillPower(int skillPower) { throw new FixedClassSetException("SkillDefinition.skillPower"); }
        @Override
        public void setCastingTime(int castingTime) { throw new FixedClassSetException("SkillDefinition.castingTime"); }
        @Override
        public void setNodes(List<List<SkillNode>> nodes) { throw new FixedClassSetException("SkillDefinition.nodes"); }

        @Override
        public boolean isFixed() {
            return true;
        }
    }
}
