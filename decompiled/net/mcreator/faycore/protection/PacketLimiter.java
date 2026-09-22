/*
 * Decompiled with CFR 0.152.
 */
package net.mcreator.faycore.protection;

import net.mcreator.faycore.config.CrashProtectionConfig;

public final class PacketLimiter {
    private static long lastPacketTime = 0L;
    private static int packetCount = 0;

    private PacketLimiter() {
    }

    public static boolean allowPacket() {
        if (!CrashProtectionConfig.enablePacketLimit) {
            return true;
        }
        long now = System.currentTimeMillis();
        if (now - lastPacketTime > 1000L) {
            lastPacketTime = now;
            packetCount = 0;
        }
        return ++packetCount <= CrashProtectionConfig.maxPacketsPerSecond;
    }

    public static boolean allowText(String text) {
        if (text == null) {
            return true;
        }
        return text.length() <= CrashProtectionConfig.maxChatLength;
    }
}

