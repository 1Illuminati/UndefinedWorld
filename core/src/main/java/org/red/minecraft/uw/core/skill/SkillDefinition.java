package org.red.minecraft.uw.core.skill;

import org.jetbrains.annotations.Nullable;
import org.red.minecraft.uw.core.exeception.PowerOverException;
import org.red.minecraft.uw.core.skill.cost.CostData;
import org.red.minecraft.uw.core.skill.gear.Gear;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SkillDefinition {

    private final String skillName;
    private final List<Gear> gears;
    private final CostData costData;
    private final int skillCoolDown;
    private final int skillPower;
    private final List<SkillNode> nodes; // 외부엔 불변으로 노출

    SkillDefinition(String skillName, List<Gear> gears) {
        this.skillName = skillName;
        this.gears = List.copyOf(gears); // 방어적 복사 + 불변

        validateGears(gears);

        this.costData = buildCostData(gears);
        this.nodes = buildNodes(gears);

        int[] stats = calcStats(gears);
        this.skillPower = stats[0];
        this.skillCoolDown = stats[1] + (stats[0] * 2);
    }

    // ── 검증 ──────────────────────────────────────────

    private static void validateGears(List<Gear> gears) {
        if (gears == null || gears.isEmpty())
            throw new IllegalArgumentException("Gears must not be empty");
    }

    // ── 빌드 헬퍼 ─────────────────────────────────────

    private static CostData buildCostData(List<Gear> gears) {
        CostData costData = new CostData();
        for (Gear gear : gears)
            costData.addCost(gear.getCosts());
        return costData;
    }

    private static int[] calcStats(List<Gear> gears) {
        int cool = 0, power = 0;
        for (Gear gear : gears) {
            power += gear.getPower();
            if (power > 9) throw new PowerOverException();
            cool += gear.getCool();
        }
        return new int[]{ power, cool };
    }

    private static List<SkillNode> buildNodes(List<Gear> gears) {
        SkillNode next = null;
        LinkedList<SkillNode> nodes = new LinkedList<>();
        for (int i = gears.size() - 1; i >= 0; i--) {
            next = new SkillNode(gears.get(i), next);
            nodes.addFirst(next);
        }
        return Collections.unmodifiableList(nodes);
    }

    // ── Getters ───────────────────────────────────────

    public String getSkillName()      { return skillName; }
    public List<Gear> getGears()      { return gears; }       // 이미 불변
    public CostData getCostData()     { return costData; }
    public int getSkillCoolDown()     { return skillCoolDown; }
    public int getSkillPower()        { return skillPower; }
    public List<SkillNode> getNodes() { return nodes; }

    public SkillNode getFirstNode() {
        return nodes.isEmpty() ? null : nodes.get(0);
    }

    // ── SkillNode ─────────────────────────────────────

    public record SkillNode(Gear gear, @Nullable SkillNode nextNode) {}
}
