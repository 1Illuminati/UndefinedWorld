package org.red.minecraft.uw.core.skill.target;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

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