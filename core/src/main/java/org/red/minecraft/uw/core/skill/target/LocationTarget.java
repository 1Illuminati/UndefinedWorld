package org.red.minecraft.uw.core.skill.target;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * 모든 로케이션은 블럭 한칸을 기준으로 한다
 * @param range 감지 범위
 * @param box 감지 영역
 * @param type 감지 타입
 */
public record LocationTarget(Location center, int targetCount, double range, BoundingBox[] box, SearchType type) implements Target<Location> {
    public LocationTarget(Location center, int targetCount, double range, SearchType type) {
        this(center, targetCount, range, new BoundingBox[]{}, type);
    }

    public LocationTarget(Location center, int targetCount, BoundingBox[] box, SearchType type) {
        this(center, targetCount, -1, box, type);
    }

    private static final int MAX_ATTEMPTS = 100;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public Location[] getTargets(@Nullable Predicate<Location> predicate) {
        List<Location> locations = new ArrayList<>();

        // BOX/RANGE_BOX용 totalVolume 사전 계산
        double totalVolume = (box != null && box.length > 0)
                ? Arrays.stream(box).mapToDouble(b -> b.getWidthX() * b.getWidthZ()).sum()
                : 0;

        outer:
        for (int i = 0; i < this.targetCount; i++) {
            int attempts = 0;
            while (attempts++ < MAX_ATTEMPTS) {
                switch (type) {
                    case RANGE_CIRCLE -> {
                        double angle = RANDOM.nextDouble(0, 2 * Math.PI);
                        double radius = range * Math.sqrt(RANDOM.nextDouble());

                        Location loc = new Location(
                                center.getWorld(),
                                center.x() + radius * Math.cos(angle),
                                center.getY(),
                                center.z() + radius * Math.sin(angle)
                        ).toBlockLocation();

                        if ((predicate == null || predicate.test(loc)) && duplicationCheck(locations, loc)) {
                            locations.add(loc);
                            continue outer;
                        }
                    }
                    case RANGE_SQUARE -> {
                        Location loc = new Location(
                                center.getWorld(),
                                center.x() + RANDOM.nextDouble(-range, range),
                                center.getY(),
                                center.z() + RANDOM.nextDouble(-range, range)
                        ).toBlockLocation();

                        if ((predicate == null || predicate.test(loc)) && duplicationCheck(locations, loc)) {
                            locations.add(loc);
                            continue outer;
                        }
                    }
                    case BOX -> {
                        if (box == null || box.length == 0) continue outer;

                        BoundingBox selectedBox = selectWeightedBox(box, totalVolume);

                        Location loc = new Location(
                                center.getWorld(),
                                RANDOM.nextDouble(selectedBox.getMinX(), selectedBox.getMaxX()),
                                center.getY(),
                                RANDOM.nextDouble(selectedBox.getMinZ(), selectedBox.getMaxZ())
                        ).toBlockLocation();

                        if ((predicate == null || predicate.test(loc)) && duplicationCheck(locations, loc)) {
                            locations.add(loc);
                            continue outer;
                        }
                    }
                    case RANGE_BOX -> {
                        if (box == null || box.length == 0) continue outer;

                        List<BoundingBox> validBoxes = Arrays.stream(box)
                                .filter(b -> {
                                    double nearX = Math.clamp(center.getX(), b.getMinX(), b.getMaxX());
                                    double nearZ = Math.clamp(center.getZ(), b.getMinZ(), b.getMaxZ());
                                    double distX = center.getX() - nearX;
                                    double distZ = center.getZ() - nearZ;
                                    return (distX * distX + distZ * distZ) <= (range * range);
                                })
                                .toList();

                        if (validBoxes.isEmpty()) continue outer;

                        double validVolume = validBoxes.stream()
                                .mapToDouble(b -> b.getWidthX() * b.getWidthZ())
                                .sum();

                        BoundingBox selectedBox = selectWeightedBox(validBoxes, validVolume);

                        double minX = Math.max(selectedBox.getMinX(), center.getX() - range);
                        double maxX = Math.min(selectedBox.getMaxX(), center.getX() + range);
                        double minZ = Math.max(selectedBox.getMinZ(), center.getZ() - range);
                        double maxZ = Math.min(selectedBox.getMaxZ(), center.getZ() + range);

                        double randX = RANDOM.nextDouble(minX, maxX);
                        double randZ = RANDOM.nextDouble(minZ, maxZ);

                        double distX = randX - center.getX();
                        double distZ = randZ - center.getZ();
                        if ((distX * distX + distZ * distZ) > (range * range)) continue; // retry

                        Location loc = new Location(
                                center.getWorld(),
                                randX,
                                center.getY(),
                                randZ
                        ).toBlockLocation();

                        if ((predicate == null || predicate.test(loc)) && duplicationCheck(locations, loc)) {
                            locations.add(loc);
                            continue outer;
                        }
                    }
                }
            }
            // MAX_ATTEMPTS 초과 시 해당 타겟 슬롯 스킵
        }

        return locations.toArray(new Location[0]);
    }

    private BoundingBox selectWeightedBox(List<BoundingBox> boxes, double totalVolume) {
        double rand = RANDOM.nextDouble(totalVolume);
        double cumulative = 0;
        for (BoundingBox b : boxes) {
            cumulative += b.getWidthX() * b.getWidthZ();
            if (rand <= cumulative) return b;
        }
        return boxes.get(boxes.size() - 1); // 부동소수점 오차 방어
    }

    private BoundingBox selectWeightedBox(BoundingBox[] boxes, double totalVolume) {
        double rand = RANDOM.nextDouble(totalVolume);
        double cumulative = 0;
        for (BoundingBox b : boxes) {
            cumulative += b.getWidthX() * b.getWidthZ();
            if (rand <= cumulative) return b;
        }
        return boxes[boxes.length - 1]; // 부동소수점 오차 방어
    }

    private boolean duplicationCheck(List<Location> locs, Location loc) {
        return locs.stream().noneMatch(l -> l.equals(loc));
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
