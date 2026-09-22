/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
 *  net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.ComponentContents
 *  net.minecraft.network.chat.contents.TranslatableContents
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundChatCommandPacket
 *  net.minecraft.world.Container
 *  org.lwjgl.glfw.GLFW
 */
package net.mcreator.faycore;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mcreator.faycore.FayCoreInvseeManager;
import net.mcreator.faycore.FayCoreTickQueue;
import net.mcreator.faycore.FayCoreWings;
import net.mcreator.faycore.GunInput;
import net.mcreator.faycore.SkillInput;
import net.mcreator.faycore.SkillRenderer;
import net.mcreator.faycore.SkillState;
import net.mcreator.faycore.SkillTracker;
import net.mcreator.faycore.client.gui.FayCoreGhostCmdSettingsScreen;
import net.mcreator.faycore.client.gui.FayCoreInvseeScreen;
import net.mcreator.faycore.client.gui.FayCoreMacroEngine;
import net.mcreator.faycore.client.gui.FayCoreMacroScreen;
import net.mcreator.faycore.client.gui.FayCoreRepeatingCmdSettingsScreen;
import net.mcreator.faycore.client.gui.FaycoreSettingsScreen;
import net.mcreator.faycore.client.gui.OneCmdManagerScreen;
import net.mcreator.faycore.init.FaycoreModKeyMappings;
import net.mcreator.faycore.init.FaycoreModMenus;
import net.mcreator.faycore.init.FaycoreModScreens;
import net.mcreator.faycore.network.FaycoreModVariables;
import net.mcreator.faycore.world.inventory.FaycoreSettingsMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.world.Container;
import org.lwjgl.glfw.GLFW;

@Environment(value=EnvType.CLIENT)
public class FaycoreModClient
implements ClientModInitializer {
    private static int loadingTickCounter = 0;
    private static final int TIMEOUT_TICKS = 100;
    public static int hudDisplayTickCooldown = -1;

    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                SkillState.init();
            }
        });
        FaycoreModScreens.clientLoad();
        FaycoreModMenus.clientLoad();
        FaycoreModKeyMappings.clientLoad();
        SkillTracker.register();
        SkillRenderer.register();
        SkillInput.register();
        GunInput.register();
        FayCoreTickQueue.register();
        FayCoreWings.register();
        ClientPlayNetworking.registerGlobalReceiver(FaycoreModVariables.SavedDataSyncMessage.TYPE, FaycoreModVariables.SavedDataSyncMessage::handleData);
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            block5: {
                if (message == null) {
                    return true;
                }
                try {
                    String rawMessage = message.getString();
                    if (FayCoreInvseeManager.targetPlayerName.isEmpty() || !rawMessage.contains("Inventory") && !rawMessage.contains("\u5177\u6709\u4ee5\u4e0b\u5be6\u9ad4\u8cc7\u6599") && !rawMessage.contains("has the following") || !rawMessage.contains(FayCoreInvseeManager.targetPlayerName)) break block5;
                    try {
                        FayCoreInvseeManager.parseServerInventoryNbt(rawMessage);
                    }
                    catch (Exception nbtEx) {
                        System.err.println("\u00a79[FayCore] \u00a7cError: \u00a7" + nbtEx.getMessage());
                    }
                    return false;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            return true;
        });
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            block5: {
                if (message == null) {
                    return true;
                }
                try {
                    String rawMessage = message.getString();
                    if (FayCoreInvseeManager.targetPlayerName.isEmpty() || !rawMessage.contains("Inventory") && !rawMessage.contains("\u5177\u6709\u4ee5\u4e0b\u5be6\u9ad4\u8cc7\u6599") && !rawMessage.contains("\u5be6\u9ad4\u8cc7\u6599") && !rawMessage.contains("has the following") || !rawMessage.contains(FayCoreInvseeManager.targetPlayerName)) break block5;
                    try {
                        FayCoreInvseeManager.parseServerInventoryNbt(rawMessage);
                    }
                    catch (Exception nbtEx) {
                        System.err.println("[FayCore Invsee] NBT \u89e3\u6790\u7570\u5e38: " + nbtEx.getMessage());
                    }
                    return false;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            return true;
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.level != null) {
                try {
                    FayCoreInvseeManager.tickRefresh();
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.screen != null) {
                String screenName = client.screen.getClass().getSimpleName();
                if (screenName.contains("LevelLoading") || screenName.contains("ReceivingLevel") || screenName.contains("Terrain")) {
                    if (++loadingTickCounter >= 100) {
                        loadingTickCounter = 0;
                        client.execute(() -> {
                            if (client.getConnection() != null) {
                                client.getConnection().getConnection().disconnect((Component)Component.literal((String)"\u00a7c[FayCore \u4fdd\u8b77] \u8f09\u5165\u5730\u5f62\u903e\u6642 (Timeout)"));
                            }
                        });
                    }
                } else {
                    loadingTickCounter = 0;
                }
            } else {
                loadingTickCounter = 0;
            }
            if (client.player == null) {
                return;
            }
            int currentG = FayCoreMacroEngine.selectedGroupIndex;
            boolean isChannelEnabled = FayCoreMacroEngine.groupEnables.get(currentG);
            long windowHandle = GLFW.glfwGetCurrentContext();
            boolean isHPressed = isChannelEnabled && GLFW.glfwGetKey((long)windowHandle, (int)72) == 1;
            int curGroupMode = FayCoreMacroEngine.groupModes.get(FayCoreMacroEngine.selectedGroupIndex);
            boolean shouldClockRun = false;
            try {
                for (int g = 0; g != 10; ++g) {
                    if (FayCoreMacroEngine.groupModes.get(g) != 2 || !FayCoreMacroEngine.groupEnables.get(g).booleanValue() || FaycoreSettingsScreen.posA == null || FaycoreSettingsScreen.posB == null) continue;
                    shouldClockRun = true;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (curGroupMode == 1) {
                if (isHPressed) {
                    FayCoreMacroEngine.isMacroRunning = true;
                }
            } else if (curGroupMode == 0) {
                if (isHPressed) {
                    if (!(FayCoreMacroEngine.isSingleTriggerLocked || client.player.isSpectator() || FayCoreMacroEngine.isMacroRunning)) {
                        FayCoreMacroEngine.isMacroRunning = true;
                        FayCoreMacroEngine.isSingleTriggerLocked = true;
                    }
                } else {
                    FayCoreMacroEngine.isSingleTriggerLocked = false;
                }
            }
            if (FayCoreMacroEngine.isMacroRunning || shouldClockRun) {
                FayCoreMacroEngine.executeMacroEngineCore();
            }
        });
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (message.toLowerCase().startsWith("invsee ") || message.toLowerCase().startsWith("/invsee ")) {
                String[] parts = message.trim().split(" ");
                if (parts.length > 1) {
                    String targetName = parts[1].trim();
                    Minecraft mc = Minecraft.getInstance();
                    mc.execute(() -> {
                        if (mc.player != null) {
                            FayCoreInvseeManager.targetPlayerName = targetName;
                            mc.player.sendSystemMessage((Component)Component.literal((String)("\u00a79[FayCore] \u00a77Invsee target: \u00a73" + targetName)));
                            mc.setScreen((Screen)new FayCoreInvseeScreen());
                        }
                    });
                }
                return false;
            }
            if (message == null) {
                return true;
            }
            String rawText = message.trim();
            if (rawText.equalsIgnoreCase("fm") || rawText.equalsIgnoreCase("/fm")) {
                Minecraft mc = Minecraft.getInstance();
                mc.execute(() -> {
                    if (mc.player != null) {
                        mc.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a73Open Macro gui..."));
                        mc.setScreen((Screen)new FayCoreMacroScreen());
                    }
                });
                return false;
            }
            if (rawText.equalsIgnoreCase("frc") || rawText.equalsIgnoreCase("/frc")) {
                Minecraft mc = Minecraft.getInstance();
                mc.execute(() -> {
                    if (mc.player != null) {
                        mc.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a73Open repeat cmd gui..."));
                        mc.setScreen((Screen)new FayCoreRepeatingCmdSettingsScreen((Component)Component.literal((String)"FayCore Repeat Command")));
                    }
                });
                return false;
            }
            if (rawText.equalsIgnoreCase("fr") || rawText.equalsIgnoreCase("/fr")) {
                Minecraft mc = Minecraft.getInstance();
                mc.execute(() -> {
                    if (mc.player != null) {
                        mc.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a73Open FastRun gui..."));
                        mc.setScreen((Screen)new FayCoreGhostCmdSettingsScreen());
                    }
                });
                return false;
            }
            if (rawText.equalsIgnoreCase("fs") || rawText.equalsIgnoreCase("/fs")) {
                Minecraft mc = Minecraft.getInstance();
                new Thread(() -> {
                    try {
                        Thread.sleep(200L);
                        mc.execute(() -> {
                            if (mc.player != null && mc.level != null) {
                                try {
                                    LocalPlayer player = mc.player;
                                    FaycoreSettingsMenu menu = new FaycoreSettingsMenu(0, player.getInventory(), (Container)null);
                                    mc.setScreen((Screen)new FaycoreSettingsScreen(menu, player.getInventory(), (Component)Component.literal((String)"FayCore settings")));
                                }
                                catch (Exception ex) {
                                    mc.player.connection.send((Packet)new ServerboundChatCommandPacket("faycore"));
                                }
                            }
                        });
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }).start();
                return false;
            }
            if (rawText.equalsIgnoreCase("fo") || rawText.equalsIgnoreCase("/fo")) {
                Minecraft mc = Minecraft.getInstance();
                mc.execute(() -> {
                    if (mc.player != null) {
                        mc.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a73Open One Cmd Gui..."));
                        mc.setScreen((Screen)new OneCmdManagerScreen());
                    }
                });
                return false;
            }
            return true;
        });
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            TranslatableContents contents;
            ComponentContents patt0$temp = message.getContents();
            return !(patt0$temp instanceof TranslatableContents) || !(contents = (TranslatableContents)patt0$temp).getKey().equals("advMode.setCommand.success");
        });
    }

    public static void renderNativeFayCoreHUD(GuiGraphicsExtractor guiGraphics) {
        if (guiGraphics == null) {
            return;
        }
        if (FaycoreSettingsScreen.fillStage == 2) {
            hudDisplayTickCooldown = 35;
        }
    }
}

