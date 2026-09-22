/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EntityType
 */
package chunk.faye.mod_tog.faycore.tracker;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.EntityType;

public final class EntityTracker {
    private static int total = 0;
    private static final Map<EntityType<?>, Integer> TYPES = new HashMap();

    private EntityTracker() {
    }

    public static boolean add(EntityType<?> type) {
        ++total;
        TYPES.put(type, TYPES.getOrDefault(type, 0) + 1);
        return true;
    }

    public static void remove(EntityType<?> type) {
        if (total > 0) {
            --total;
        }
        TYPES.computeIfPresent(type, (k, v) -> v > 1 ? Integer.valueOf(v - 1) : null);
    }

    public static int count() {
        return total;
    }

    public static int count(EntityType<?> type) {
        return TYPES.getOrDefault(type, 0);
    }

    public static void clear() {
        total = 0;
        TYPES.clear();
    }
}

