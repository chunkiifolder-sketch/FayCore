/*
 * Decompiled with CFR 0.152.
 */
package chunk.faye.mod_tog.faycore.protection;

import chunk.faye.mod_tog.faycore.config.CrashProtectionConfig;

public final class ParticleLimiter {
    private static long lastTick = 0L;
    private static int count = 0;

    private ParticleLimiter() {
    }

    public static boolean allow() {
        if (!CrashProtectionConfig.enableParticleLimit) {
            return true;
        }
        long now = System.currentTimeMillis();
        if (now - lastTick > 1000L) {
            lastTick = now;
            count = 0;
        }
        return ++count <= CrashProtectionConfig.maxParticlesPerSecond;
    }
}

