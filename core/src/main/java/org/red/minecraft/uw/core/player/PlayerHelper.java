package org.red.minecraft.uw.core.player;

import org.red.minecraft.dellarte.library.entity.A_Player;
import org.red.minecraft.dellarte.library.util.A_DataMap;
import org.red.minecraft.uw.core.StaticValue;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;
import org.red.minecraft.uw.core.attribute.AttributeManager;
import org.red.minecraft.uw.core.attribute.AttributeType;
import org.red.minecraft.uw.core.attribute.stat.Stat;
import org.red.minecraft.uw.core.attribute.stat.StatHolder;

/**
 * 플레이어용 함수 모아두는 클래스
 */
public final class PlayerHelper extends AttributeManager implements StatHolder {

    /** 바닐라 플레이어 기본 최대 체력. HEALTH_MAX 는 여기에 더해지는 보너스로 취급한다. */
    private static final double VANILLA_BASE_HEALTH = 20.0;

    public PlayerHelper(A_Player player) {
        super(player);
    }

    public A_Player getPlayer() {
        return this.getEntity();
    }

    @Override
    public A_Player getEntity() {
        return (A_Player) super.getEntity();
    }

    /**
     * A_DataMap의 getX(key)는 키가 없으면 기본값을 맵에 써넣는다 (조회만으로 저장 데이터가 늘어난다).
     * 자원 조회는 PAPI/리젠 태스크에서 반복 호출되므로 없는 키를 만들지 않도록 먼저 확인한다.
     */
    public double getMana() {
        return getResource(StaticValue.MANA_KEY);
    }

    public double getStamina() {
        return getResource(StaticValue.STAMINA_KEY);
    }

    private double getResource(String key) {
        A_DataMap map = getPlayer().getDataMap(UndefinedWorldCorePlugin.instance);
        if (!map.containsKey(key)) return 0;
        return map.getDouble(key);
    }

    public void setMana(double value) {
        getPlayer().getDataMap(UndefinedWorldCorePlugin.instance).put(StaticValue.MANA_KEY, clampToMax(value, AttributeType.MANA_MAX));
    }

    public void setStamina(double value) {
        getPlayer().getDataMap(UndefinedWorldCorePlugin.instance).put(StaticValue.STAMINA_KEY, clampToMax(value, AttributeType.STAMINA_MAX));
    }

    /**
     * 자원값을 0 ~ 최대치로 자른다.
     *
     * Math.clamp는 min > max이거나 NaN이면 IllegalArgumentException을 던진다.
     * 최대치는 감소 attribute(버프/장비 해제)로 음수가 될 수 있으므로 0으로 올려서 막고,
     * NaN은 저장값을 오염시키므로 0으로 처리한다. (예외로 재생 태스크/스킬 비용 처리가 멈추지 않게)
     */
    private double clampToMax(double value, AttributeType maxType) {
        if (Double.isNaN(value)) {
            UndefinedWorldCorePlugin.sendLog("Resource value is NaN, treated as 0: " + getPlayer().getName() + " " + maxType.name());
            return 0;
        }

        double max = this.getAttributeValue(maxType);
        if (Double.isNaN(max)) {
            UndefinedWorldCorePlugin.sendLog("Resource max is NaN, treated as 0: " + getPlayer().getName() + " " + maxType.name());
            max = 0;
        }

        return Math.clamp(value, 0, Math.max(0, max));
    }

    /**
     * 최대치 변동(장비/스탯 변경) 후 현재 자원값을 최대치 안으로 다시 자른다.
     * 재생량이 0인 플레이어는 setMana/setStamina가 호출되지 않아 초과 상태가 유지되므로 명시적으로 호출한다.
     */
    public void clampResourcesToMax() {
        setMana(getMana());
        setStamina(getStamina());
    }

    public void addMana(double value) {
        setMana(getMana() + value);
    }

    public void addStamina(double value) {
        setStamina(getStamina() + value);
    }

    @Override
    public void setStatValue(Stat type, int value) {
        this.getStatDataMap().put(type.name(), value);
    }

    @Override
    public int getStatValue(Stat type) {
        if (!hasStatValue(type)) return 0;
        return this.getStatDataMap().getInt(type.name());
    }

    @Override
    public boolean hasStatValue(Stat type) {
        return this.getStatDataMap().containsKey(type.name());
    }

    @Override
    public int getStatPoint() {
        A_DataMap map = this.getStatDataMap();
        if (!map.containsKey(StaticValue.STAT_POINT_KEY)) return 0; // 조회만으로 키가 생기지 않게 (getMana와 동일 규칙)
        return map.getInt(StaticValue.STAT_POINT_KEY);
    }

    @Override
    public void setStatPoint(int statPoint) {
        this.getStatDataMap().put(StaticValue.STAT_POINT_KEY, statPoint);
    }

    @Override
    public void addStatPoint(int statPoint) {
        this.setStatPoint(this.getStatPoint() + statPoint);
    }

    public A_DataMap getStatDataMap() {
        return this.getPlayer().getDataMap(UndefinedWorldCorePlugin.instance).getDataMap(StaticValue.STAT_MAP_KEY);
    }

    /**
     * 플레이어의 스텟을 attribute에 적용시키는 함수
     */
    @Override
    public void applyStatToAttribute() {
        this.clearBaseAttributeValues(ContainerType.STAT);

        for (Stat stat : Stat.stats()) {
            double value = getStatValue(stat);

            stat.map().forEach((k, v) -> {
                this.addBaseAttributeValue(k, ContainerType.STAT, v * value);
            });
        }

        this.applyMaxHealth();

        // 스탯 변경으로 MANA_MAX/STAMINA_MAX가 줄었을 수 있으므로 현재값을 다시 자른다
        this.clampResourcesToMax();
    }

    /**
     * HEALTH_MAX 계열 attribute 를 실제 엔티티 최대 체력에 반영한다. (확정 공식: max * (1 + mul - div))
     * MULTIPLY/DIVIDE 는 배율 % 처리이므로 100 으로 나눠 적용한다.
     *
     * 장비/스탯 변경으로 최대치가 줄면 현재 체력이 그 위에 남을 수 있어 함께 잘라준다.
     *
     * todo 확정 필요 — 여기서 쓰는 max 는 "바닐라 기본 체력 + HEALTH_MAX" 로 해석했다.
     *      HEALTH_MAX 만으로 계산하면 HEL 스탯이 0 인 플레이어의 최대 체력이 0 이 되어 즉사하므로
     *      기본 체력을 더하는 쪽으로 두었다. 기본 체력을 빼는 것이 의도라면 알려달라.
     */
    public void applyMaxHealth() {
        double bonus = this.getAttributeValue(AttributeType.HEALTH_MAX);
        double multiply = this.getAttributeValue(AttributeType.HEALTH_MULTIPLY);
        double divide = this.getAttributeValue(AttributeType.HEALTH_DIVIDE);

        double max = (VANILLA_BASE_HEALTH + bonus) * (1 + (multiply - divide) / 100);

        if (!Double.isFinite(max)) {
            UndefinedWorldCorePlugin.sendLog("Max health is not finite, skipped: " + getPlayer().getName());
            return;
        }

        // 최대 체력이 0 이하가 되면 즉사하므로 최소 1 로 막는다
        max = Math.max(1, max);

        A_Player player = this.getPlayer();
        player.setMaxHealth(max);
        if (player.getHealth() > max) player.setHealth(max);
    }
}
