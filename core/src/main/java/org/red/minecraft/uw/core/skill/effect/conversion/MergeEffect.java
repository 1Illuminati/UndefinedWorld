package org.red.minecraft.uw.core.skill.effect.conversion;

import org.red.minecraft.uw.core.skill.SkillDefinition;
import org.red.minecraft.uw.core.skill.gear.Gear;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 병합 변환 이펙트. (구조 결정 2.5 T16)
 *
 * MergeNum = 한번에 합치는 기어의 갯수.
 * 예) 3번째 기어가 MergeEffect(mergeNum=3)이면 4, 5, 6번 기어는 하나의 병렬 그룹으로 동시에 실행되고,
 *     7번째 기어가 존재할 경우 그룹의 마지막(6번) 기어 이후에 체인 실행된다.
 *
 * setConversion은 기어 목록 전체에서 모든 MergeEffect를 스캔해 노드 구조를 재구성한다.
 * 파생적(stateless) 재구성이므로 MergeEffect가 여러 개여도 호출 순서와 무관하게 같은 결과가 나온다 (멱등).
 */
public class MergeEffect extends ConversionEffect {

    private final int mergeNum;

    public MergeEffect(int mergeNum) {
        this.mergeNum = mergeNum;
    }

    public int getMergeNum() {
        return mergeNum;
    }

    @Override
    public void setConversion(SkillDefinition skillDef, SkillDefinition.SkillNode effectNode) {
        skillDef.setNodes(rebuildNodes(skillDef.getGears()));
    }

    /**
     * 기어 목록에서 모든 MergeEffect를 반영한 노드 레벨 구조를 재구성한다.
     * 병렬 그룹에서는 마지막 기어만 다음 레벨로 체인한다 (명세: "7번째 기어는 6번 기어 이후 실행").
     */
    private static List<List<SkillDefinition.SkillNode>> rebuildNodes(List<Gear> gears) {
        // 1) 레벨(동시 실행 그룹) 인덱스 구성
        List<List<Integer>> levels = new ArrayList<>();
        int i = 0;
        while (i < gears.size()) {
            levels.add(List.of(i));

            if (gears.get(i).getEffect() instanceof MergeEffect merge) {
                int start = i + 1;
                int end = Math.min(i + merge.getMergeNum(), gears.size() - 1);

                if (start <= end) {
                    // todo 병합 그룹 내부에 또 MergeEffect가 있는 경우(중첩 병합)는 미정의 — 일반 기어로 취급
                    List<Integer> group = new ArrayList<>();
                    for (int j = start; j <= end; j++) group.add(j);
                    levels.add(group);
                    i = end + 1;
                    continue;
                }
            }

            i++;
        }

        // 2) 꼬리부터 노드 체인 구성 — 각 레벨에서 마지막 노드만 다음 레벨로 체인한다
        LinkedList<List<SkillDefinition.SkillNode>> result = new LinkedList<>();
        List<SkillDefinition.SkillNode> next = List.of();

        for (int l = levels.size() - 1; l >= 0; l--) {
            List<Integer> indices = levels.get(l);
            List<SkillDefinition.SkillNode> level = new ArrayList<>();

            for (int pos = 0; pos < indices.size(); pos++) {
                boolean isLast = pos == indices.size() - 1;
                level.add(new SkillDefinition.SkillNode(gears.get(indices.get(pos)), isLast ? next : List.of()));
            }

            List<SkillDefinition.SkillNode> fixed = List.copyOf(level);
            result.addFirst(fixed);
            next = fixed;
        }

        return result;
    }
}
