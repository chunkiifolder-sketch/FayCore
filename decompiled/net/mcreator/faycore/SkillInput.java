/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.minecraft.client.gui.screens.Screen
 */
package net.mcreator.faycore;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.mcreator.faycore.SkillInteractScreen;
import net.mcreator.faycore.SkillManager;
import net.mcreator.faycore.SkillState;
import net.mcreator.faycore.SkillTracker;
import net.minecraft.client.gui.screens.Screen;

public class SkillInput {
    public static boolean wasAltDown = false;
    public static int currentHoveredSlot = -1;
    public static boolean isVKeyCurrentlyHolding = false;
    private static boolean isScreenOpening = false;
    public static boolean isVKeyWaitingForPhysicalRelease = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }
            Window currentWindow = client.getWindow();
            boolean isAltDown = InputConstants.isKeyDown((Window)currentWindow, (int)342);
            if (isAltDown) {
                if (client.screen == null && !isScreenOpening) {
                    isScreenOpening = true;
                    client.execute(() -> {
                        client.setScreen((Screen)new SkillInteractScreen());
                        SkillTracker.skillMenuOpen = true;
                        wasAltDown = true;
                        currentHoveredSlot = -1;
                    });
                }
            } else if (isScreenOpening || wasAltDown) {
                if (client.screen instanceof SkillInteractScreen) {
                    client.execute(() -> client.setScreen(null));
                }
                SkillTracker.skillMenuOpen = false;
                isScreenOpening = false;
                wasAltDown = false;
                currentHoveredSlot = -1;
            }
            if (client.screen != null) {
                isVKeyCurrentlyHolding = false;
                isVKeyWaitingForPhysicalRelease = false;
                return;
            }
            boolean isVDown = InputConstants.isKeyDown((Window)currentWindow, (int)86);
            if (isVDown) {
                if (isVKeyWaitingForPhysicalRelease) {
                    return;
                }
                if (!isVKeyCurrentlyHolding) {
                    int currentSelected = SkillState.getSelected();
                    SkillManager.castPressSkill(currentSelected);
                    isVKeyCurrentlyHolding = true;
                } else {
                    int currentSelected = SkillState.getSelected();
                    SkillManager.castHoldingSkill(currentSelected);
                }
            } else {
                isVKeyWaitingForPhysicalRelease = false;
                if (isVKeyCurrentlyHolding) {
                    int currentSelected = SkillState.getSelected();
                    SkillManager.castReleaseSkill(currentSelected);
                    isVKeyCurrentlyHolding = false;
                }
            }
        });
    }
}

