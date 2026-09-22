/*
 * Decompiled with CFR 0.152.
 */
package net.mcreator.faycore.config;

public final class CrashProtectionConfig {
    public static boolean enabled = false;
    public static boolean debugLog = false;
    public static boolean cancelInvalidContent = false;
    public static boolean disconnectOnAttack = false;
    public static boolean printStackTrace = false;
    public static boolean enableParticleLimit = false;
    public static int maxParticlesPerSecond = 5000;
    public static int maxParticlesPerTick = 300;
    public static int maxParticlesTotal = 5000;
    public static int maxParticlePacketsPerSecond = 200;
    public static boolean enableEntityLimit = false;
    public static int maxEntities = 1000;
    public static int maxArmorStands = 100;
    public static int maxItemEntities = 300;
    public static int maxExperienceOrbs = 200;
    public static int maxProjectiles = 500;
    public static int maxAreaEffectClouds = 100;
    public static int maxDisplayEntities = 300;
    public static int maxTextDisplays = 100;
    public static int maxInteractionEntities = 300;
    public static boolean enableRenderProtection = false;
    public static boolean skipBrokenEntities = false;
    public static boolean ignoreRenderExceptions = false;
    public static boolean enableNameLimit = false;
    public static int maxCustomNameLength = 128;
    public static boolean enablePacketLimit = false;
    public static int maxPacketsPerSecond = 200;
    public static boolean enableItemTextLimit = false;
    public static int maxItemTextLength = 128;
    public static boolean enableTextLimit = false;
    public static boolean enableComponentProtection = false;
    public static boolean protectRecursiveComponents = false;
    public static int maxComponentDepth = 32;
    public static int maxComponentNodes = 2048;
    public static int maxTranslateArguments = 32;
    public static int maxResolvedComponentLength = 4096;
    public static boolean enableChatLimit = false;
    public static int maxChatLength = 512;
    public static int maxSystemMessageLength = 512;
    public static int maxActionBarLength = 256;
    public static int maxTitleLength = 256;
    public static int maxTitlesPerSecond = 5;
    public static boolean enableBossBarLimit = false;
    public static int maxBossBars = 10;
    public static int maxBossBarNameLength = 64;
    public static int maxBossBarUpdatesPerSecond = 20;
    public static boolean enableScoreboardLimit = false;
    public static int maxScoreboardLength = 64;
    public static int maxScoreboardLines = 15;
    public static boolean enableBookLimit = false;
    public static int maxBookPages = 100;
    public static int maxBookPageLength = 2048;
    public static boolean enableItemProtection = false;
    public static int maxItemNameLength = 256;
    public static int maxItemLoreLength = 4096;
    public static int maxEnchantments = 128;
    public static boolean enableJsonLimit = false;
    public static int maxJsonLength = 4096;
    public static boolean enableNbtLimit = false;
    public static int maxNbtDepth = 3200;
    public static int maxNbtStringLength = 204800;
    public static int maxCompoundEntries = 1024;
    public static int maxNbtListSize = 1024;
    public static int maxByteArrayLength = 65536;
    public static int maxIntArrayLength = 65536;
    public static int maxLongArrayLength = 65536;
    public static boolean enablePacketProtection = false;
    public static int maxPacketSize = 0x200000;
    public static boolean enableDialogLimit = true;
    public static boolean blockServerDialogs = false;
    public static boolean enableDimensionChangeLimit = false;
    public static int dimensionChangeWindowSeconds = 10;
    public static int maxDimensionChanges = 5;
    public static boolean enableSoundLimit = false;
    public static int maxSoundPacketsPerSecond = 200;
    public static boolean protectOpenScreenPackets = false;
    public static boolean protectInventoryPackets = false;
    public static boolean logBlockedParticles = false;
    public static boolean logBlockedEntities = false;
    public static boolean logBlockedComponents = false;
    public static boolean logBlockedPackets = false;
    public static boolean logBlockedNBT = false;

    private CrashProtectionConfig() {
    }
}

