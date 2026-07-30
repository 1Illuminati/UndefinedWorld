package org.red.minecraft.uw.core.skill;

import org.red.minecraft.dellarte.library.entity.A_Entity;
import org.red.minecraft.dellarte.library.util.A_DataMap;

import java.util.Objects;

public class SkillCTX {
    private final A_DataMap map = new A_DataMap();

    public SkillCTX(A_Entity caster) {
        // CASTER는 모든 Effect/Condition이 읽는 값이라, null이면 원인에서 멀리 떨어진 곳에서 터진다
        this.setCTX(CTXType.CASTER, Objects.requireNonNull(caster, "caster"));

        for (CTXType type : CTXType.values()) {
            if (type.defaultValue != null)
                setCTX(type, type.defaultValue);
        }
    }

    private SkillCTX(A_DataMap map) {
        this.map.copy(map);
    }

    public <T> void setCTX(CTXType type, T value) {
        this.map.put(type.name(), value);
    }

    /**
     * CTX 값 조회. 값이 없으면 null.
     * <p>A_DataMap의 get 계열은 키가 없으면 기본값(여기선 null)을 <b>삽입</b>한다(널포인터 방지 설계).
     * 조회만으로 맵이 커지지 않도록 존재 여부를 containsKey로 먼저 판정한다(§2.6 A_DataMap 원칙).
     * <p>type과 다른 타입 변수로 받으면 호출 지점에서 ClassCastException이 발생한다.
     */
    public <T> T getCTX(CTXType type) {
        if (!this.map.containsKey(type.name())) return null;
        return (T) this.map.getClass(type.name(), type.clazz);
    }

    public <T> T getCTX(CTXType type, T defaultValue) {
        T value = getCTX(type);
        return value != null ? value : defaultValue;
    }

    /** 값이 실제로 들어있는지 (null 값은 미보유로 취급) */
    public boolean hasCTX(CTXType type) {
        return this.map.containsKey(type.name()) && this.map.get(type.name()) != null;
    }

    /**
     * 동시 실행 노드 간 격리용 복사. (A_DataMap 생성자가 putAll로 새 맵을 만들므로 실제로 분리된다)
     * <p>얕은 복사다 — 값 객체 자체는 공유된다.
     * todo 확정 필요 — SEARCH_AREA(BoundingBox[]) / LAST_TARGET_INFO(A_Entity[]) 같은 배열 값은
     *      병렬 노드가 같은 배열을 공유한다. 한 노드가 배열 내용을 바꾸면 형제 노드에 영향이 간다.
     *      깊은 복사 대상/방식은 구조 결정 사항이라 임의 처리하지 않았다.
     */
    public SkillCTX copy() {
        return new SkillCTX(new A_DataMap(map.getMap()));
    }
}
