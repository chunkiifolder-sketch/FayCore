package net.mcreator.faycore;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mcreator.faycore.init.FaycoreModKeyMappings;
import net.mcreator.faycore.init.FaycoreModMenus;
import net.mcreator.faycore.init.FaycoreModScreens;
import net.mcreator.faycore.network.FaycoreModVariables;
import net.fabricmc.api.ClientModInitializer;


@Environment(EnvType.CLIENT)
public class FaycoreModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Start of user code block mod constructor
        // End of user code block mod constructor
        FaycoreModScreens.clientLoad();
        FaycoreModMenus.clientLoad();
        FaycoreModKeyMappings.clientLoad();
        SkillRenderer.register();
        SkillInput.register(); // skillbar 初始化呼叫
        ClientPlayNetworking.registerGlobalReceiver(FaycoreModVariables.SavedDataSyncMessage.TYPE, FaycoreModVariables.SavedDataSyncMessage::handleData);
        // Start of user code block mod init

        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null)
                return;

            int currentG = net.mcreator.faycore.client.gui.FayCoreMacroEngine.selectedGroupIndex;
            boolean isChannelEnabled = net.mcreator.faycore.client.gui.FayCoreMacroEngine.groupEnables.get(currentG);


            long windowHandle = org.lwjgl.glfw.GLFW.glfwGetCurrentContext();

            boolean isHPressed = isChannelEnabled && (org.lwjgl.glfw.GLFW.glfwGetKey(windowHandle, org.lwjgl.glfw.GLFW.GLFW_KEY_H) == org.lwjgl.glfw.GLFW.GLFW_PRESS);

            int curGroupMode = net.mcreator.faycore.client.gui.FayCoreMacroEngine.groupModes.get(net.mcreator.faycore.client.gui.FayCoreMacroEngine.selectedGroupIndex);

            boolean shouldClockRun = false;
            try {
                for (int g = 0; g != 10; g++) {
                    if (net.mcreator.faycore.client.gui.FayCoreMacroEngine.groupModes.get(g) == 2 && net.mcreator.faycore.client.gui.FayCoreMacroEngine.groupEnables.get(g)) {
                        if (net.mcreator.faycore.client.gui.FaycoreSettingsScreen.posA != null && net.mcreator.faycore.client.gui.FaycoreSettingsScreen.posB != null) {
                            shouldClockRun = true;
                        }
                    }
                }
            } catch (Exception e) {
            }
            if (curGroupMode == 1) {
                if (isHPressed)
                    net.mcreator.faycore.client.gui.FayCoreMacroEngine.isMacroRunning = true;
            } else if (curGroupMode == 0) {
                if (isHPressed) {
                    if (!net.mcreator.faycore.client.gui.FayCoreMacroEngine.isSingleTriggerLocked && !client.player.isSpectator()) {
                        if (!net.mcreator.faycore.client.gui.FayCoreMacroEngine.isMacroRunning) {
                            net.mcreator.faycore.client.gui.FayCoreMacroEngine.isMacroRunning = true;
                            net.mcreator.faycore.client.gui.FayCoreMacroEngine.isSingleTriggerLocked = true;
                        }
                    }
                } else {
                    net.mcreator.faycore.client.gui.FayCoreMacroEngine.isSingleTriggerLocked = false;
                }
            }
            if (net.mcreator.faycore.client.gui.FayCoreMacroEngine.isMacroRunning || shouldClockRun) {
                net.mcreator.faycore.client.gui.FayCoreMacroEngine.executeMacroEngineCore();
            }
        });

        net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (message == null)
                return true;
            String rawText = message.trim();
            if (rawText.equalsIgnoreCase("faycoremacro") || rawText.equalsIgnoreCase("/faycoremacro")) {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                mc.execute(() -> {
                    if (mc.player != null) {
                        mc.player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§6[FayCore] 正在開啟客製化 10 組大巨集管理介面..."));
                        mc.setScreen(new net.mcreator.faycore.client.gui.FayCoreMacroScreen());
                    }
                });
                return false;
            }
            if (rawText.equalsIgnoreCase("faycoresettings") || rawText.equalsIgnoreCase("/faycoresettings")) {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                new Thread(() -> {
                    try {
                        Thread.sleep(200);
                        mc.execute(() -> {
                            if (mc.player != null && mc.level != null) {
                                try {
                                    net.minecraft.client.player.LocalPlayer player = mc.player;
                                    net.mcreator.faycore.world.inventory.FaycoreSettingsMenu menu = new net.mcreator.faycore.world.inventory.FaycoreSettingsMenu(0, player.getInventory(), (net.minecraft.world.Container) null);
                                    mc.setScreen(new net.mcreator.faycore.client.gui.FaycoreSettingsScreen(menu, player.getInventory(), net.minecraft.network.chat.Component.literal("FayCore 選區配置")));
                                } catch (Exception ex) {
                                    mc.player.connection.send(new net.minecraft.network.protocol.game.ServerboundChatCommandPacket("faycore"));
                                }
                            }
                        });
                    } catch (Exception e) {
                    }
                }).start();
                return false;
            }
            return true;
        });
        // 🛡️ 3. 智慧接收端聊天盾
        net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (message == null)
                return true;
            String text = message.getString().toLowerCase();
            if (text.contains("command set") || text.contains("指令設為") || text.contains("successfully filled") || text.contains("成功填充")) {
                return false;
            }
            return true;
        });
        net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (message == null)
                return true;
            String text = message.getString().toLowerCase();
            if (text.contains("command set") || text.contains("指令設為") || text.contains("successfully filled") || text.contains("成功填充")) {
                return false;
            }
            return true;
        });
        // End of user code block mod init
    }

    // Start of user code block mod methods
    public static int hudDisplayTickCooldown = -1;

    public static void renderNativeFayCoreHUD(net.minecraft.client.gui.GuiGraphicsExtractor guiGraphics) {
        if (guiGraphics == null)
            return;
        if (net.mcreator.faycore.client.gui.FaycoreSettingsScreen.fillStage == 2) {
            hudDisplayTickCooldown = 35;
            net.mcreator.faycore.client.gui.FaycoreSettingsScreen.fillStage = 0;
        }
        if (hudDisplayTickCooldown > 0) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player == null || mc.level == null)
                return;
            net.minecraft.core.BlockPos pA = net.mcreator.faycore.client.gui.FaycoreSettingsScreen.posA;
            net.minecraft.core.BlockPos pB = net.mcreator.faycore.client.gui.FaycoreSettingsScreen.posB;
            if (pA != null && pB != null) {
                int diffX = Math.abs(pA.getX() - pB.getX()) + 1;
                int diffY = Math.abs(pA.getY() - pB.getY()) + 1;
                int diffZ = Math.abs(pA.getZ() - pB.getZ()) + 1;
                int centerX = mc.getWindow().getGuiScaledWidth() / 2;
                int centerY = (mc.getWindow().getGuiScaledHeight() / 2) + 25;
                String infoText = "§e§l[FayCore 選區] §6§l" + diffX + "x" + diffY + "x" + diffZ + " §f§l(指令核心)";
                guiGraphics.text(mc.font, net.minecraft.network.chat.Component.literal(infoText), centerX - (mc.font.width(infoText) / 2), centerY, 0xFFFFFF, true);
            }
            hudDisplayTickCooldown--;
        }
    }
    // End of user code block mod methods
}