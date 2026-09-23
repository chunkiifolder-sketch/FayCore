package chunk.faye.mod_tog.faycore;

import chunk.faye.mod_tog.faycore.client.gui.FayCoreGhostCmdSettingsScreen;
import chunk.faye.mod_tog.faycore.client.gui.FayCoreInvseeScreen;
import chunk.faye.mod_tog.faycore.client.gui.FayCoreMacroEngine;
import chunk.faye.mod_tog.faycore.client.gui.FayCoreMacroScreen;
import chunk.faye.mod_tog.faycore.client.gui.FayCoreRepeatingCmdSettingsScreen;
import chunk.faye.mod_tog.faycore.client.gui.FaycoreSettingsScreen;
import chunk.faye.mod_tog.faycore.client.gui.OneCmdManagerScreen;
import chunk.faye.mod_tog.faycore.init.FaycoreModKeyMappings;
import chunk.faye.mod_tog.faycore.init.FaycoreModMenus;
import chunk.faye.mod_tog.faycore.init.FaycoreModScreens;
import chunk.faye.mod_tog.faycore.network.FaycoreModVariables;
import chunk.faye.mod_tog.faycore.world.inventory.FaycoreSettingsMenu;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.AllowGame;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents.AllowChat;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.world.Container;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class FaycoreModClient implements ClientModInitializer {
   private static int loadingTickCounter = 0;
   private static final int TIMEOUT_TICKS = 100;
   public static int hudDisplayTickCooldown = -1;

   public void onInitializeClient() {
      FayCoreExtraFeatures.register();
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
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
      ClientReceiveMessageEvents.ALLOW_GAME
         .register(
            (AllowGame)(message, overlay) -> {
               if (message == null) {
                  return true;
               } else {
                  try {
                     String rawMessage = message.getString();
                     if (!FayCoreInvseeManager.targetPlayerName.isEmpty()
                        && (rawMessage.contains("Inventory") || rawMessage.contains("具有以下實體資料") || rawMessage.contains("has the following"))
                        && rawMessage.contains(FayCoreInvseeManager.targetPlayerName)) {
                        try {
                           FayCoreInvseeManager.parseServerInventoryNbt(rawMessage);
                        } catch (Exception var4) {
                           System.err.println("§9[FayCore] §cError: §" + var4.getMessage());
                        }

                        return false;
                     }
                  } catch (Exception var5) {
                  }

                  return true;
               }
            }
         );
      ClientReceiveMessageEvents.ALLOW_GAME
         .register(
            (AllowGame)(message, overlay) -> {
               if (message == null) {
                  return true;
               } else {
                  try {
                     String rawMessage = message.getString();
                     if (!FayCoreInvseeManager.targetPlayerName.isEmpty()
                        && (
                           rawMessage.contains("Inventory")
                              || rawMessage.contains("具有以下實體資料")
                              || rawMessage.contains("實體資料")
                              || rawMessage.contains("has the following")
                        )
                        && rawMessage.contains(FayCoreInvseeManager.targetPlayerName)) {
                        try {
                           FayCoreInvseeManager.parseServerInventoryNbt(rawMessage);
                        } catch (Exception var4) {
                           System.err.println("[FayCore Invsee] NBT 解析異常: " + var4.getMessage());
                        }

                        return false;
                     }
                  } catch (Exception var5) {
                  }

                  return true;
               }
            }
         );
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         if (client.player != null && client.level != null) {
            try {
               FayCoreInvseeManager.tickRefresh();
            } catch (Exception var2) {
            }
         }
      });
      ClientTickEvents.END_CLIENT_TICK
         .register(
            (EndTick)client -> {
               if (client.screen != null) {
                  String screenName = client.screen.getClass().getSimpleName();
                  if (!screenName.contains("LevelLoading") && !screenName.contains("ReceivingLevel") && !screenName.contains("Terrain")) {
                     loadingTickCounter = 0;
                  } else {
                     loadingTickCounter++;
                     if (loadingTickCounter >= 100) {
                        loadingTickCounter = 0;
                        client.execute(() -> {
                           if (client.getConnection() != null) {
                              client.getConnection().getConnection().disconnect(Component.literal("§c[FayCore 保護] 載入地形逾時 (Timeout)"));
                           }
                        });
                     }
                  }
               } else {
                  loadingTickCounter = 0;
               }

               if (client.player != null) {
                  int currentG = FayCoreMacroEngine.selectedGroupIndex;
                  boolean isChannelEnabled = FayCoreMacroEngine.groupEnables.get(currentG);
                  long windowHandle = GLFW.glfwGetCurrentContext();
                  boolean isHPressed = isChannelEnabled && GLFW.glfwGetKey(windowHandle, 72) == 1;
                  int curGroupMode = FayCoreMacroEngine.groupModes.get(FayCoreMacroEngine.selectedGroupIndex);
                  boolean shouldClockRun = false;

                  try {
                     for (int g = 0; g != 10; g++) {
                        if (FayCoreMacroEngine.groupModes.get(g) == 2
                           && FayCoreMacroEngine.groupEnables.get(g)
                           && FaycoreSettingsScreen.posA != null
                           && FaycoreSettingsScreen.posB != null) {
                           shouldClockRun = true;
                        }
                     }
                  } catch (Exception var9) {
                  }

                  if (curGroupMode == 1) {
                     if (isHPressed) {
                        FayCoreMacroEngine.isMacroRunning = true;
                     }
                  } else if (curGroupMode == 0) {
                     if (isHPressed) {
                        if (!FayCoreMacroEngine.isSingleTriggerLocked && !client.player.isSpectator() && !FayCoreMacroEngine.isMacroRunning) {
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
               }
            }
         );
      ClientSendMessageEvents.ALLOW_CHAT.register((AllowChat)message -> {
         if (message.toLowerCase().startsWith("invsee ") || message.toLowerCase().startsWith("/invsee ")) {
            String[] parts = message.trim().split(" ");
            if (parts.length > 1) {
               String targetName = parts[1].trim();
               Minecraft mc = Minecraft.getInstance();
               mc.execute(() -> {
                  if (mc.player != null) {
                     FayCoreInvseeManager.targetPlayerName = targetName;
                     mc.player.sendSystemMessage(Component.literal("§9[FayCore] §7Invsee target: §3" + targetName));
                     mc.setScreen(new FayCoreInvseeScreen());
                  }
               });
            }

            return false;
         } else if (message == null) {
            return true;
         } else {
            String rawText = message.trim();
            if (rawText.equalsIgnoreCase("fm") || rawText.equalsIgnoreCase("/fm")) {
               Minecraft mc = Minecraft.getInstance();
               mc.execute(() -> {
                  if (mc.player != null) {
                     mc.player.sendSystemMessage(Component.literal("§9[FayCore] §3Open Macro gui..."));
                     mc.setScreen(new FayCoreMacroScreen());
                  }
               });
               return false;
            } else if (rawText.equalsIgnoreCase("frc") || rawText.equalsIgnoreCase("/frc")) {
               Minecraft mc = Minecraft.getInstance();
               mc.execute(() -> {
                  if (mc.player != null) {
                     mc.player.sendSystemMessage(Component.literal("§9[FayCore] §3Open repeat cmd gui..."));
                     mc.setScreen(new FayCoreRepeatingCmdSettingsScreen(Component.literal("FayCore Repeat Command")));
                  }
               });
               return false;
            } else if (rawText.equalsIgnoreCase("fr") || rawText.equalsIgnoreCase("/fr")) {
               Minecraft mc = Minecraft.getInstance();
               mc.execute(() -> {
                  if (mc.player != null) {
                     mc.player.sendSystemMessage(Component.literal("§9[FayCore] §3Open FastRun gui..."));
                     mc.setScreen(new FayCoreGhostCmdSettingsScreen());
                  }
               });
               return false;
            } else if (rawText.equalsIgnoreCase("fs") || rawText.equalsIgnoreCase("/fs")) {
               Minecraft mc = Minecraft.getInstance();
               new Thread(() -> {
                  try {
                     Thread.sleep(200L);
                     mc.execute(() -> {
                        if (mc.player != null && mc.level != null) {
                           try {
                              LocalPlayer player = mc.player;
                              FaycoreSettingsMenu menu = new FaycoreSettingsMenu(0, player.getInventory(), (Container)null);
                              mc.setScreen(new FaycoreSettingsScreen(menu, player.getInventory(), Component.literal("FayCore settings")));
                           } catch (Exception var3x) {
                              mc.player.connection.send(new ServerboundChatCommandPacket("faycore"));
                           }
                        }
                     });
                  } catch (Exception var2x) {
                  }
               }).start();
               return false;
            } else if (!rawText.equalsIgnoreCase("fo") && !rawText.equalsIgnoreCase("/fo")) {
               return true;
            } else {
               Minecraft mc = Minecraft.getInstance();
               mc.execute(() -> {
                  if (mc.player != null) {
                     mc.player.sendSystemMessage(Component.literal("§9[FayCore] §3Open One Cmd Gui..."));
                     mc.setScreen(new OneCmdManagerScreen());
                  }
               });
               return false;
            }
         }
      });
      ClientReceiveMessageEvents.ALLOW_GAME.register((AllowGame)(message, overlay) -> {
         if (message.getContents() instanceof TranslatableContents contents && contents.getKey().equals("advMode.setCommand.success")) {
            return false;
         }

         return true;
      });
   }

   public static void renderNativeFayCoreHUD(GuiGraphicsExtractor guiGraphics) {
      if (guiGraphics != null) {
         if (FaycoreSettingsScreen.fillStage == 2) {
            hudDisplayTickCooldown = 35;
         }
      }
   }
}
