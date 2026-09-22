/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.LevelLoadingScreen
 *  net.minecraft.network.chat.Component
 */
package net.mcreator.faycore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.network.chat.Component;

public class LoadingTimeoutManager {
    private static int loadingTickCounter = 0;
    private static final int TIMEOUT_TICKS = 100;

    public static void clientTick(Minecraft mc) {
        if (mc.player == null) {
            loadingTickCounter = 0;
            return;
        }
        if (mc.screen instanceof LevelLoadingScreen) {
            if (++loadingTickCounter >= 100) {
                loadingTickCounter = 0;
                mc.execute(() -> {
                    if (mc.getConnection() != null) {
                        mc.player.sendSystemMessage((Component)Component.literal((String)"\u00a7c[FayCore \u4fdd\u8b77] \u5730\u5f62\u4e0b\u8f09\u6642\u9593\u904e\u9577\uff0c\u5df2\u81ea\u52d5\u65b7\u958b\u9023\u7dda\u9632\u6b62\u5361\u6b7b\uff01"));
                        mc.getConnection().getConnection().disconnect((Component)Component.literal((String)"\u00a7c\u8f09\u5165\u5730\u5f62\u903e\u6642 (Timeout)"));
                    }
                });
            }
        } else {
            loadingTickCounter = 0;
        }
    }
}

