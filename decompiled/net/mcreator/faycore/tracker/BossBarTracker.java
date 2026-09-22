/*
 * Decompiled with CFR 0.152.
 */
package net.mcreator.faycore.tracker;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class BossBarTracker {
    private static final Set<UUID> BOSS_BARS = new HashSet<UUID>();

    private BossBarTracker() {
    }

    public static boolean add(UUID uuid) {
        if (BOSS_BARS.size() >= 100) {
            return false;
        }
        BOSS_BARS.add(uuid);
        return true;
    }

    public static void remove(UUID uuid) {
        BOSS_BARS.remove(uuid);
    }

    public static boolean exists(UUID uuid) {
        return BOSS_BARS.contains(uuid);
    }

    public static int size() {
        return BOSS_BARS.size();
    }

    public static void clear() {
        BOSS_BARS.clear();
    }
}

