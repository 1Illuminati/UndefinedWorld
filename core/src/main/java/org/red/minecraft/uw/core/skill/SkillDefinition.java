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

        this.costData = buildCostData();
        this.nodes = buildNodes();
        this.setConversions(); // ConversionEffect(Merge 등) 노드 구조 변환 적용

        int[] stats = calcStats();
        this.skillPower = stats[0];
        this.skillCoolDown = stats[1] + (stats[0] * 2);
        this.castingTime = stats[2];
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

    private int[] calcStats() {
        int cool = 0, power = 0, cast = 0;
        for (Gear gear : gears) {
            power += gear.getPower();
            if (power > 9) throw new PowerOverException();
            cool += gear.getCool();
            cast += gear.getCastingTime();
        }
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

    public int getSkillCoolDown()     { return skillCoolDown; }
    public int getSkillPower()        { return skillPower; }
    public int getCastingTime()    { return castingTime; }
    public List<List<SkillNode>> getNodes() { return nodes; }
    public void setSkillCoolDown(int skillCoolDown) {  this.skillCoolDown = skillCoolDown; }
    public void setSkillPower(int skillPower) {  this.skillPower = skillPower; }
    public void setCastingTime(int castingTime) {  this.castingTime = castingTime; }
    public void setNodes(List<List<SkillNode>> nodes) { this.nodes = nodes; }

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

    private final class FixedSkillDef extends SkillDefinition {
        public FixedSkillDef(String skillName, List<Gear> gears, CostData costData, int  skillCoolDown, int skillPower, int castingTime, List<List<SkillNode>> nodes) {
            super(skillName, gears, costData, skillCoolDown, skillPower, castingTime, nodes);
        }

        @Override
        public void setSkillCoolDown(int skillCoolDown) { throw new FixedClassSetException(); }
        @Override
        public void setSkillPower(int skillPower) { throw new FixedClassSetException(); }
        @Override
        public void setCastingTime(int castingTime) { throw new FixedClassSetException(); }
        @Override
        public void setNodes(List<List<SkillNode>> nodes) { throw new FixedClassSetException(); }

        @Override
        public boolean isFixed() {
            return true;
        }
    }
}
