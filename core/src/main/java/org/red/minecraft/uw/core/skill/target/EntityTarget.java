package org.red.minecraft.uw.core.skill.target;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;
import org.red.minecraft.uw.core.UndefinedWorldCorePlugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * 엔티티 기반 타겟 탐색
 * @param center 중심 좌표
 * @param targetCount 최대 탐색 엔티티 수
 * @param range 감지 범위 (-1이면 미사용)
 * @param box 감지 영역 (비어있으면 미사용)
 * @param type 감지 타입
 */
public record EntityTarget(Location center, int targetCount, double range, BoundingBox[] box, SearchType type) implements Target<Entity> {
    public EntityTarget(Location center, int targetCount, double range, SearchType type) {
        this(center, targetCount, range, new BoundingBox[]{}, type);
    }

    public EntityTarget(Location center, int targetCount, BoundingBox[] box, SearchType type) {
        this(center, targetCount, -1, box, type);
    }

    @Override
    public Entity[] getTargets(@Nullable Predicate<Entity> predicate) {
        if (center.getWorld() == null) return new Entity[0];

        // Stream.limit 은 음수 인자에 IllegalArgumentException 을 던진다.
        // targetCount 는 기어 YAML(count)/CTX(TARGET_COUNT)에서 오므로 0 이하가 들어올 수 있다.
        // LocationTarget 이 targetCount 0 이하일 때 빈 결과를 내는 것과 동일하게 맞춘다.
        if (targetCount <= 0) {
            UndefinedWorldCorePlugin.sendLog("EntityTarget: targetCount 가 0 이하라 탐색을 건너뜀 (targetCount=" + targetCount + ")");
            return new Entity[0];
        }

        // range 는 기어 YAML(range)과 CTX 배율의 곱이라 무한대/NaN 이 들어올 수 있다.
        // 그대로 getNearbyEntities 에 넘기면 탐색 박스가 사실상 월드 전체가 되어 서버가 멈춘다.
        if (type != SearchType.BOX && !Double.isFinite(range)) {
            UndefinedWorldCorePlugin.sendLog("EntityTarget: range 가 유한하지 않아 탐색을 건너뜀 (range=" + range + ", type=" + type + ")");
            return new Entity[0];
        }

        return switch (type) {
            case RANGE_CIRCLE -> {
                double rangeSq = range * range;
                yield center.getWorld().getNearbyEntities(center, range, range, range).stream()
                        .filter(e -> e.getLocation().distanceSquared(center) <= rangeSq)
                        .filter(e -> predicate == null || predicate.test(e))
                        .limit(targetCount)
                        .toArray(Entity[]::new);
            }
            case RANGE_SQUARE -> center.getWorld().getNearbyEntities(center, range, range, range).stream()
                    .filter(e -> predicate == null || predicate.test(e))
                    .limit(targetCount)
                    .toArray(Entity[]::new);
            case BOX -> {
                if (box == null || box.length == 0) yield new Entity[0];

                yield Arrays.stream(box)
                        .map(b -> center.getWorld().getNearbyEntities(b))
                        .flatMap(Collection::stream)
                        .distinct()
                        .filter(e -> predicate == null || predicate.test(e))
                        .limit(targetCount)
                        .toArray(Entity[]::new);
            }
            case RANGE_BOX -> {
                if (box == null || box.length == 0) yield new Entity[0];

                double rangeSq = range * range;
                yield Arrays.stream(box)
                        .map(b -> center.getWorld().getNearbyEntities(b))
                        .flatMap(Collection::stream)
                        .distinct()
                        .filter(e -> e.getLocation().distanceSquared(center) <= rangeSq)
                        .filter(e -> predicate == null || predicate.test(e))
                        .limit(targetCount)
                        .toArray(Entity[]::new);
            }
        };
    }

    @Override
    public Location getLocation() {
        return this.center;
    }

    @Override
    public int targetCount() {
        return this.targetCount;
    }

    @Override
    public double targetSearchRange() {
        return this.range;
    }

    @Override
    public BoundingBox[] targetSearchBox() {
        return this.box;
    }

    @Override
    public SearchType getSearchType() {
        return this.type;
    }
}