package net.mcreator.faycore;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import java.util.List;

public class SkillInput {

    public static boolean wasAltDown = false;
    public static int currentHoveredSlot = -1;

    public static boolean isVKeyCurrentlyHolding = false;
    public static int vTickTimer = 0;
    private static boolean isScreenOpening = false;

    private static int tickdelay = 5;
    private static int vHoldIntialDelay = 0;

    public static boolean isVKeyWaitingForPhysicalRelease = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            var currentWindow = client.getWindow();
            boolean isAltDown = InputConstants.isKeyDown(currentWindow, GLFW.GLFW_KEY_LEFT_ALT);

            // Alt 選單邏輯
            if (isAltDown) {
                if (client.screen == null && !isScreenOpening) {
                    isScreenOpening = true;
                    client.execute(() -> {
                        client.setScreen(new SkillInteractScreen());
                        wasAltDown = true;
                        currentHoveredSlot = -1;
                    });
                }
            } else {
                if (isScreenOpening || wasAltDown) {
                    if (client.screen instanceof SkillInteractScreen) {
                        client.execute(() -> client.setScreen(null));
                    }
                    isScreenOpening = false;
                    wasAltDown = false;
                    currentHoveredSlot = -1;
                }
            }

            if (client.screen != null) {
                isVKeyCurrentlyHolding = false;
                vTickTimer = 0;
                vHoldIntialDelay = 0;
                isVKeyWaitingForPhysicalRelease = false;
                return;
            }

            boolean isVDown = InputConstants.isKeyDown(currentWindow, GLFW.GLFW_KEY_V);
            if (isVDown) {
                if (isVKeyWaitingForPhysicalRelease) return;

                if (!isVKeyCurrentlyHolding) {
                    int currentSelected = SkillState.getSelected();

                    SkillManager.castPressSkill(currentSelected);

                    isVKeyCurrentlyHolding = true;
                    vHoldIntialDelay = 0;
                    vTickTimer = 0;
                }
                else {
                    if (vHoldIntialDelay > 0) {
                        vHoldIntialDelay--;
                    } else {
                        if (vTickTimer <= 0) {
                            int currentSelected = SkillState.getSelected();

                            SkillManager.castHoldingSkill(currentSelected);

                            vTickTimer = tickdelay;
                        } else {
                            vTickTimer--;
                        }
                    }
                }

            } else {
                isVKeyWaitingForPhysicalRelease = false;

                if (isVKeyCurrentlyHolding) {
                    int currentSelected = SkillState.getSelected();

                    SkillManager.castReleaseSkill(currentSelected);

                    isVKeyCurrentlyHolding = false;
                    vTickTimer = 0;
                    vHoldIntialDelay = 0;
                }
            }
        });
    }
}
