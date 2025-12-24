package org.red.minecraft.undefinedworld.attribute.stat;

import java.util.HashMap;
import java.util.Map;

import org.red.minecraft.undefinedworld.attribute.AttributeHolder;
import org.red.minecraft.undefinedworld.attribute.AttributeType;

/**
 * 근력
 * - 물리공격력 증가
 * - 체력 소폭 증가
 * - 기력 소폭 증가
 * 
 * 민첩
 * - 이동속도 증가
 * - 회피 확률 증가
 * - 기력 소폭 증가
 * 
 * 체력
 * - 체력 증가
 * - 기력 증가
 * 
 * 집중
 * - 치명타 확률 증가
 * - 치명타 데미지 소폭 증가
 * 
 * 지식
 * - 마법데미지 증가
 * - 마나 소폭 증가
 * 
 * 지혜
 * - 마나 증가
 * - 마법데미지 소폭 증가
 * - 정신데미지 소폭 증가
 * 
 * 정신
 * - 정신력 증가
 * - 마나 증가
 */
public final class Stat implements AttributeHolder {
    public static final Stat STR = new Stat("근력");
    public static final Stat AGI = new Stat("민첩");
    public static final Stat HEL = new Stat("체력");
    public static final Stat FOC = new Stat("집중");
    public static final Stat KNO = new Stat("지식");
    public static final Stat WIS = new Stat("지혜");
    public static final Stat SPI = new Stat("정신");

    private final HashMap<AttributeType, Double> attributeMap = new HashMap<>();
    public final String krName;

    private Stat(String krName) {
        this.krName = krName;
    }

    @Override
    public double getAttributeValue(AttributeType type) {
        return attributeMap.getOrDefault(type, 0d);
    }

    @Override
    public void setAttributeValue(AttributeType type, double value) {
        attributeMap.put(type, value);
    }

    @Override
    public boolean hasAttributeValue(AttributeType type) {
        return attributeMap.containsKey(type);
    }

    public Map<AttributeType, Double> getMap() {
        return (Map<AttributeType, Double>) this.attributeMap.clone();
    }

    public static Stat getStatByStr(String str) {
        Stat stat = null;
        switch (str) {
            case "STR", "str", "근력" -> stat = Stat.STR;
            case "AGI", "agi", "민첩" -> stat = Stat.AGI;
            case "HEL", "hel", "체력" -> stat = Stat.HEL;
            case "FOC", "foc", "집중" -> stat = Stat.FOC;
            case "KNO", "kno", "지식" -> stat = Stat.KNO;
            case "WIS", "wis", "지혜" -> stat = Stat.WIS;
            case "SPI", "spi", "정신" -> stat = Stat.SPI;
        }

        return stat;
    }

    /**
     * 서버 시작과 동시에 실행
     */
    public static void statSetting() {

        //근력 설정
        Stat.STR.setAttributeValue(AttributeType.PHYSICS_DAMAGE, 15);
        Stat.STR.setAttributeValue(AttributeType.HEALTH, 30);
        Stat.STR.setAttributeValue(AttributeType.STAMINA, 30);

        //민첩 설정
        Stat.AGI.setAttributeValue(AttributeType.SPEED, 1);
        Stat.AGI.setAttributeValue(AttributeType.DODGE, 1);
        Stat.AGI.setAttributeValue(AttributeType.STAMINA, 50);

        //체력 설정
        Stat.HEL.setAttributeValue(AttributeType.HEALTH, 100);
        Stat.HEL.setAttributeValue(AttributeType.STAMINA, 100);

        //집중 설정
        Stat.FOC.setAttributeValue(AttributeType.CRITICAL_CHANCE, 1);
        Stat.FOC.setAttributeValue(AttributeType.CRITICAL_DAMAGE, 1);

        //지식 설정
        Stat.KNO.setAttributeValue(AttributeType.MAGIC_DAMAGE, 15);
        Stat.KNO.setAttributeValue(AttributeType.MANA, 30);

        //지혜 설정
        Stat.WIS.setAttributeValue(AttributeType.MANA, 100);
        Stat.WIS.setAttributeValue(AttributeType.MAGIC_DAMAGE, 5);
        Stat.WIS.setAttributeValue(AttributeType.MENTAL_DAMAGE, 5);

        //명상 설정
        Stat.WIS.setAttributeValue(AttributeType.MANA, 100);
        Stat.WIS.setAttributeValue(AttributeType.MENTAL, 100);
    }
}
