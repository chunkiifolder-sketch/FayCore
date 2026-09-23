package chunk.faye.mod_tog.faycore.client.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity.Mode;

public class FayCoreCustomInputScreen extends Screen {
   private EditBox bottomInputBox;
   private EditBox upperInputBox;
   private Button toggleSlotButton;
   private int currentSlot = 1;
   private final int maxSlots = 3;
   public static boolean muteFeedbackActive = false;
   private String lastSearchQuery = "";
   private int tabCycleIndex = 0;
   private int queryStartIndex = -1;
   private boolean showSuggestionsList = false;
   private List<String> currentMatchedPlayers = new ArrayList<>();
   private static final Map<Integer, String> prefixCache = new HashMap<>();
   private static final Map<Integer, String> messageCache = new HashMap<>();
   private static final File CONFIG_FILE = new File(Minecraft.getInstance().gameDirectory, "config/faycore_slots_macros.txt");
   private EditBox targetPlayerInputBox;
   private static final Map<Integer, String> targetPlayerCache = new HashMap<>();

   private boolean checkClientHasOP() {
      return true;
   }

   public FayCoreCustomInputScreen() {
      super(Component.literal("FayCore 循環指令方塊排程器"));
      prefixCache.putIfAbsent(1, "&b[廣播前綴]");
      prefixCache.putIfAbsent(2, "&e[團隊廣播]");
      prefixCache.putIfAbsent(3, "&c[警告]");
      messageCache.putIfAbsent(1, "這是預設訊息內容。");
      messageCache.putIfAbsent(2, "");
      messageCache.putIfAbsent(3, "");
      targetPlayerCache.putIfAbsent(1, "");
      targetPlayerCache.putIfAbsent(2, "");
      targetPlayerCache.putIfAbsent(3, "");
      this.loadSlotsFromDisk();
   }

   private void saveSlotsToDisk() {
      try {
         if (!CONFIG_FILE.getParentFile().exists()) {
            CONFIG_FILE.getParentFile().mkdirs();
         }

         try (PrintWriter writer = new PrintWriter(new FileWriter(CONFIG_FILE))) {
            writer.println("currentSlot=" + this.currentSlot);

            for (int i = 1; i != 4; i++) {
               writer.println("slot_" + i + "_p=" + prefixCache.getOrDefault(i, ""));
               writer.println("slot_" + i + "_m=" + messageCache.getOrDefault(i, ""));
               writer.println("slot_" + i + "_t=" + targetPlayerCache.getOrDefault(i, ""));
            }

            writer.flush();
         }
      } catch (Exception var6) {
      }
   }

   private void loadSlotsFromDisk() {
      if (CONFIG_FILE.exists()) {
         String line;
         try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE))) {
            while ((line = reader.readLine()) != null) {
               String trimmed = line.trim();
               if (trimmed.startsWith("currentSlot=")) {
                  String[] csParts = trimmed.split("=", 2);
                  if (csParts.length == 2) {
                     String csVal = Arrays.asList(csParts).get(1);
                     this.currentSlot = Integer.parseInt(csVal);
                  }
               }

               for (int i = 1; i != 4; i++) {
                  if (trimmed.startsWith("slot_" + i + "_t=")) {
                     String[] tokens = trimmed.split("=", 2);
                     if (tokens.length == 2) {
                        targetPlayerCache.put(i, Arrays.asList(tokens).get(1));
                     }
                  }

                  if (trimmed.startsWith("slot_" + i + "_p=")) {
                     String[] tokens = trimmed.split("=", 2);
                     if (tokens.length == 2) {
                        String pVal = Arrays.asList(tokens).get(1);
                        prefixCache.put(i, pVal);
                     }
                  }

                  if (trimmed.startsWith("slot_" + i + "_m=")) {
                     String[] tokens = trimmed.split("=", 2);
                     if (tokens.length == 2) {
                        String mVal = Arrays.asList(tokens).get(1);
                        messageCache.put(i, mVal);
                     }
                  }
               }
            }
         } catch (Exception var9) {
         }
      }
   }

   protected void init() {
      super.init();
      int boxWidth = 140;
      int boxHeight = 20;
      int paddingLeft = 15;
      int paddingBottom = 15;
      int spacingY = 12;
      int bottomY = this.height - paddingBottom - boxHeight;
      this.bottomInputBox = new EditBox(this.font, paddingLeft, bottomY, boxWidth, boxHeight, Component.literal("發送訊息"));
      this.bottomInputBox.setHint(Component.literal("Chat..."));
      this.bottomInputBox.setMaxLength(255);
      this.bottomInputBox.setValue(messageCache.getOrDefault(this.currentSlot, ""));
      this.addRenderableWidget(this.bottomInputBox);
      this.bottomInputBox.setResponder(text -> {
         int cursorPos = this.bottomInputBox.getCursorPosition();
         int atIndex = text.lastIndexOf(64, cursorPos - 1);
         if (atIndex != -1) {
            String intermediate = text.substring(atIndex, cursorPos);
            if (!intermediate.contains(" ")) {
               String prefixQuery = intermediate.substring(1).toLowerCase();
               Minecraft innerMc = Minecraft.getInstance();
               if (innerMc.getConnection() != null) {
                  Collection<PlayerInfo> players = innerMc.getConnection().getOnlinePlayers();
                  this.currentMatchedPlayers.clear();

                  for (PlayerInfo info : players) {
                     if (info.getProfile() != null) {
                        String pStr = info.getProfile().toString();
                        String pName = "";
                        int nIdx = pStr.indexOf("name=");
                        if (nIdx != -1) {
                           int sPos = nIdx + 5;
                           int ePos = pStr.indexOf(44, sPos);
                           if (ePos == -1) {
                              ePos = pStr.indexOf(93, sPos);
                           }

                           if (ePos != -1) {
                              pName = pStr.substring(sPos, ePos).trim();
                           }
                        }

                        if (!pName.isEmpty() && (pName.toLowerCase().startsWith(prefixQuery) || prefixQuery.isEmpty())) {
                           this.currentMatchedPlayers.add(pName);
                        }
                     }
                  }
               }

               this.showSuggestionsList = !this.currentMatchedPlayers.isEmpty();
               return;
            }
         }

         this.showSuggestionsList = false;
      });
      int upperY = bottomY - boxHeight - spacingY;
      this.upperInputBox = new EditBox(this.font, paddingLeft, upperY, boxWidth, boxHeight, Component.literal("Prefix"));
      this.upperInputBox.setHint(Component.literal("Prefix"));
      this.upperInputBox.setMaxLength(64);
      this.upperInputBox.setValue(prefixCache.getOrDefault(this.currentSlot, ""));
      this.addRenderableWidget(this.upperInputBox);
      int btnX = paddingLeft + boxWidth + 8;
      int btnWidth = 85;
      this.toggleSlotButton = Button.builder(Component.literal("Slot: " + this.currentSlot), button -> {
         prefixCache.put(this.currentSlot, this.upperInputBox.getValue());
         messageCache.put(this.currentSlot, this.bottomInputBox.getValue());
         this.injectCommandToSelectedArea();
         this.saveSlotsToDisk();
         this.currentSlot = this.currentSlot % 3 + 1;
         button.setMessage(Component.literal("Slot: " + this.currentSlot));
         this.upperInputBox.setValue(prefixCache.getOrDefault(this.currentSlot, ""));
         this.bottomInputBox.setValue(messageCache.getOrDefault(this.currentSlot, ""));
      }).bounds(btnX, upperY, btnWidth, boxHeight).build();
      this.addRenderableWidget(this.toggleSlotButton);
      this.toggleSlotButton = Button.builder(Component.literal("Slot: " + this.currentSlot), button -> {
         prefixCache.put(this.currentSlot, this.upperInputBox.getValue());
         messageCache.put(this.currentSlot, this.bottomInputBox.getValue());
         targetPlayerCache.put(this.currentSlot, this.targetPlayerInputBox.getValue());
         this.injectCommandToSelectedArea();
         this.saveSlotsToDisk();
         this.currentSlot = this.currentSlot % 3 + 1;
         button.setMessage(Component.literal("Slot: " + this.currentSlot));
         this.upperInputBox.setValue(prefixCache.getOrDefault(this.currentSlot, ""));
         this.bottomInputBox.setValue(messageCache.getOrDefault(this.currentSlot, ""));
         this.targetPlayerInputBox.setValue(targetPlayerCache.getOrDefault(this.currentSlot, ""));
      }).bounds(btnX, upperY, btnWidth, boxHeight).build();
      int targetPlayerY = upperY + boxHeight + spacingY;
      this.targetPlayerInputBox = new EditBox(this.font, btnX, targetPlayerY, btnWidth, boxHeight, Component.literal("Dm"));
      this.targetPlayerInputBox.setHint(Component.literal("Dm"));
      this.targetPlayerInputBox.setMaxLength(16);
      this.targetPlayerInputBox.setValue(targetPlayerCache.getOrDefault(this.currentSlot, ""));
      this.addRenderableWidget(this.targetPlayerInputBox);
      this.setInitialFocus(this.bottomInputBox);
   }

   private void handlePlayerTabCompletion() {
      if (this.showSuggestionsList && !this.currentMatchedPlayers.isEmpty()) {
         String currentText = this.bottomInputBox.getValue();
         int cursorPos = this.bottomInputBox.getCursorPosition();
         int atIndex = currentText.lastIndexOf(64, cursorPos - 1);
         if (atIndex != -1) {
            String selectedPlayer = this.currentMatchedPlayers.get(this.tabCycleIndex);
            String textBeforeAt = currentText.substring(0, atIndex);
            String textAfterCursor = currentText.substring(cursorPos);
            String newText = textBeforeAt + "@" + selectedPlayer + textAfterCursor;
            this.bottomInputBox.setValue(newText);
            int newCursorPos = atIndex + selectedPlayer.length() + 1;
            this.bottomInputBox.setCursorPosition(newCursorPos);
            this.bottomInputBox.setHighlightPos(newCursorPos);
            this.tabCycleIndex = (this.tabCycleIndex + 1) % this.currentMatchedPlayers.size();
         }
      }
   }

   private void injectCommandToSelectedArea() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         BlockPos pA = FaycoreSettingsScreen.posA;
         BlockPos pB = FaycoreSettingsScreen.posB;
         if (pA != null && pB != null) {
            String rawPrefix = this.upperInputBox.getValue();
            String rawMessage = FayCoreMacroEngine.parseDynamicVariables(this.bottomInputBox.getValue());
            String inputTargetPlayer = this.targetPlayerInputBox.getValue().trim();
            String commandTarget = inputTargetPlayer.isEmpty() ? "@a" : inputTargetPlayer;
            if (!rawMessage.trim().isEmpty()) {
               String targetPlayer = null;
               Pattern mentionPattern = Pattern.compile("@(\\S+)");
               Matcher matcher = mentionPattern.matcher(rawMessage);
               String formattedPrefix = rawPrefix.replace('&', '§');
               boolean isOnline = false;
               String finalTellrawCommand2 = "";
               String finalTellrawCommand;
               if (!matcher.find()) {
                  String formattedMessage = rawMessage.replace('&', '§');
                  if (commandTarget.equals("@a")) {
                     finalTellrawCommand = String.format("tellraw %s [\"\",{\"text\":\"%s%s\"}]", commandTarget, formattedPrefix, formattedMessage);
                  } else {
                     finalTellrawCommand = String.format(
                        "tellraw %s [\"\",{\"text\":\"§d[Fay] §7(§6%%player%%§7 > §bYou§7) %s\"}]", commandTarget, formattedMessage
                     );
                     FayCoreMacroEngine.autoFindAndInjectVCommand(
                        mc, String.format("tellraw %%player%% [\"\",{\"text\":\"§d[Fay] §7(§6You§7 > §b%s§7) %s\"}]", commandTarget, formattedMessage)
                     );
                  }
               } else {
                  targetPlayer = matcher.group(1);
                  if (mc.getConnection() != null) {
                     for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
                        if (info.getProfile() != null) {
                           String checkStr = info.getProfile().toString();
                           String checkName = "";
                           int checkIdx = checkStr.indexOf("name=");
                           if (checkIdx != -1) {
                              int sP = checkIdx + 5;
                              int eP = checkStr.indexOf(44, sP);
                              if (eP == -1) {
                                 eP = checkStr.indexOf(93, sP);
                              }

                              if (eP != -1) {
                                 checkName = checkStr.substring(sP, eP).trim();
                              }
                           }

                           if (checkName.equalsIgnoreCase(targetPlayer)) {
                              isOnline = true;
                           }
                        }
                     }
                  }

                  if (isOnline) {
                     String[] segments = rawMessage.split("@" + targetPlayer, 2);
                     String part1 = segments.length > 0 ? Arrays.asList(segments).get(0).replace('&', '§') : "";
                     String part2 = segments.length > 1 ? Arrays.asList(segments).get(1).replace('&', '§') : "";
                     if (commandTarget.equals("@a")) {
                        finalTellrawCommand = String.format(
                           "tellraw @a [\"\",{\"text\":\"%s\"},{\"text\":\"%s\"},{\"text\":\"@\",\"color\":\"gold\"},{\"selector\":\"%s\",\"color\":\"gold\"},{\"text\":\"%s\"}]",
                           formattedPrefix,
                           part1,
                           targetPlayer,
                           part2
                        );
                     } else {
                        finalTellrawCommand = String.format(
                           "tellraw %s [\"\",{\"text\":\"%s\"},{\"text\":\"%s\"},{\"text\":\"@\",\"color\":\"gold\"},{\"selector\":\"%s\",\"color\":\"gold\"},{\"text\":\"%s\"}]",
                           commandTarget,
                           formattedPrefix,
                           part1,
                           targetPlayer,
                           part2
                        );
                        FayCoreMacroEngine.autoFindAndInjectVCommand(
                           mc,
                           String.format(
                              "tellraw %%player%% [\"\",{\"text\":\"§d[Fay] §7(§6You§7 > §b%s§7)§r %s%s%s\"}]", commandTarget, part1, targetPlayer, part2
                           )
                        );
                     }
                  } else {
                     String formattedMessage = rawMessage.replace('&', '§');
                     String colorizedMention = "§6@" + targetPlayer + "§f";
                     String finalColorizedMessage = formattedMessage.replace("@" + targetPlayer, colorizedMention);
                     if (commandTarget.equals("@a")) {
                        finalTellrawCommand = String.format("tellraw %s [\"\",{\"text\":\"%s%s\"}]", commandTarget, formattedPrefix, finalColorizedMessage);
                     } else {
                        finalTellrawCommand = String.format(
                           "tellraw %s [\"\",{\"text\":\"§d[Fay] §7(§6%%player%%§7 > §bYou§7)§r %s\"}]", commandTarget, finalColorizedMessage
                        );
                        FayCoreMacroEngine.autoFindAndInjectVCommand(
                           mc, String.format("tellraw %%player%% [\"\",{\"text\":\"§d[Fay] §7(§6You§7 > §b%s§7)§r %s\"}]", commandTarget, formattedMessage)
                        );
                     }
                  }
               }

               if (mc.player.connection != null) {
                  int minX = Math.min(pA.getX(), pB.getX());
                  int maxX = Math.max(pA.getX(), pB.getX());
                  int minY = Math.min(pA.getY(), pB.getY());
                  int maxY = Math.max(pA.getY(), pB.getY());
                  int minZ = Math.min(pA.getZ(), pB.getZ());
                  int maxZ = Math.max(pA.getZ(), pB.getZ());
                  int endX = maxX + 1;
                  int endY = maxY + 1;
                  int endZ = maxZ + 1;
                  List<BlockPos> emptyCommandBlocks = new ArrayList<>();

                  for (int x = minX; x != endX; x++) {
                     for (int y = minY; y != endY; y++) {
                        for (int z = minZ; z != endZ; z++) {
                           BlockPos targetPos = new BlockPos(x, y, z);
                           if (mc.level.getBlockState(targetPos).is(Blocks.COMMAND_BLOCK)) {
                              BlockEntity var27 = mc.level.getBlockEntity(targetPos);
                              if (var27 instanceof CommandBlockEntity) {
                                 CommandBlockEntity cb = (CommandBlockEntity)var27;
                                 if (cb.getCommandBlock().getCommand().trim().isEmpty()) {
                                    emptyCommandBlocks.add(targetPos);
                                 }
                              }
                           }
                        }
                     }
                  }

                  int requiredBlocks = 2;
                  if (targetPlayer != null && isOnline && !commandTarget.equals("@a")) {
                     requiredBlocks = 3;
                  }

                  if (requiredBlocks > emptyCommandBlocks.size()) {
                     mc.player.sendSystemMessage(Component.literal("§c[FayCore] 警告：基地選區空格不足！本次雙彈幕排程至少需要 " + requiredBlocks + " 格空白方塊。"));
                     return;
                  }

                  try {
                     muteFeedbackActive = true;
                     if (targetPlayer != null && isOnline) {
                        BlockPos tellrawPos = emptyCommandBlocks.get(0);
                        BlockPos soundPos = null;
                        if (!commandTarget.equals("@a")) {
                           tellrawPos = emptyCommandBlocks.get(1);
                        } else {
                           tellrawPos = emptyCommandBlocks.get(1);
                           soundPos = emptyCommandBlocks.get(2);
                        }

                        String finalSoundCommand = String.format(
                           "execute as %s at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1 1", targetPlayer
                        );
                        ClientPacketListener currentConnection = Minecraft.getInstance().getConnection();
                        if (currentConnection != null) {
                           String parsedTellraw = FayCoreMacroEngine.parseDynamicVariables(finalTellrawCommand);
                           currentConnection.send(new ServerboundSetCommandBlockPacket(tellrawPos, parsedTellraw, Mode.REDSTONE, false, false, false));
                           if (commandTarget.equals("@a")) {
                              String parsedSound = FayCoreMacroEngine.parseDynamicVariables(finalSoundCommand);
                              currentConnection.send(new ServerboundSetCommandBlockPacket(soundPos, parsedSound, Mode.REDSTONE, false, false, false));
                           }

                           currentConnection.send(new ServerboundSetCommandBlockPacket(tellrawPos, parsedTellraw, Mode.REDSTONE, false, false, true));
                           if (commandTarget.equals("@a")) {
                              String parsedSound = FayCoreMacroEngine.parseDynamicVariables(finalSoundCommand);
                              currentConnection.send(new ServerboundSetCommandBlockPacket(soundPos, parsedSound, Mode.REDSTONE, false, false, true));
                           }
                        }

                        int usedSlots = !commandTarget.equals("@a") ? 3 : 2;
                        int remainingSlots = emptyCommandBlocks.size() - usedSlots;
                        if (remainingSlots <= 0
                           && FaycoreSettingsScreen.fillStage == 2
                           && FaycoreSettingsScreen.posA != null
                           && FaycoreSettingsScreen.posB != null) {
                           String resetFillCmd = String.format(
                              "fill %d %d %d %d %d %d minecraft:command_block[facing=up]{Command:'',CustomName:%s}",
                              minX,
                              minY,
                              minZ,
                              maxX,
                              maxY,
                              maxZ,
                              FayCoreMacroEngine.CustomName
                           );
                           mc.player.connection.send(new ServerboundChatCommandPacket(resetFillCmd));
                           mc.player.sendSystemMessage(Component.literal("§e§l[FayCore] 核心選區已滿！已自動重置。"));
                        }
                     } else {
                        BlockPos nextAvailablePos = emptyCommandBlocks.get(0);
                        String parsedTellrawx = FayCoreMacroEngine.parseDynamicVariables(finalTellrawCommand);
                        mc.player.connection.send(new ServerboundSetCommandBlockPacket(nextAvailablePos, parsedTellrawx, Mode.REDSTONE, false, false, false));
                        mc.player.connection.send(new ServerboundSetCommandBlockPacket(nextAvailablePos, parsedTellrawx, Mode.REDSTONE, false, false, true));
                        if (FaycoreSettingsScreen.fillStage == 2
                           && FaycoreSettingsScreen.posA != null
                           && FaycoreSettingsScreen.posB != null
                           && emptyCommandBlocks.size() <= 1) {
                           String resetFillCmd = String.format(
                              "fill %d %d %d %d %d %d minecraft:command_block[facing=up]{Command:'',CustomName:%s}",
                              minX,
                              minY,
                              minZ,
                              maxX,
                              maxY,
                              maxZ,
                              FayCoreMacroEngine.CustomName
                           );
                           mc.player.connection.send(new ServerboundChatCommandPacket(resetFillCmd));
                           mc.player.sendSystemMessage(Component.literal("§e§l[FayCore] 核心選區已滿！已自動重置。"));
                        }
                     }
                  } catch (Exception var36) {
                  } finally {
                     new Thread(() -> {
                        try {
                           Thread.sleep(500L);
                        } catch (Exception var1x) {
                        }

                        muteFeedbackActive = false;
                     }).start();
                  }

                  this.bottomInputBox.setValue("");
                  messageCache.put(this.currentSlot, "");
                  this.saveSlotsToDisk();
               }
            }
         } else {
            mc.player.sendSystemMessage(Component.literal("§c[FayCore] 錯誤：未設定 A、B 區域範圍！"));
         }
      }
   }

   public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
   }

   public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
      if (this.upperInputBox != null && this.bottomInputBox != null) {
         guiGraphics.text(this.font, Component.literal("§7[Prefix]"), this.upperInputBox.getX(), this.upperInputBox.getY() - 11, 16777215, false);
         guiGraphics.text(this.font, Component.literal("§d[DM]"), this.targetPlayerInputBox.getX(), this.targetPlayerInputBox.getY() - 11, 16777215, false);
         guiGraphics.text(this.font, Component.literal("§7[動態訊息欄位 (打 @ 彈出選單)]"), this.bottomInputBox.getX(), this.bottomInputBox.getY() - 11, 16777215, false);
      }

      if (this.showSuggestionsList && !this.currentMatchedPlayers.isEmpty()) {
         int renderX = this.bottomInputBox.getX() + 2;
         int renderY = this.bottomInputBox.getY() - 14;
         int maxShow = Math.min(this.currentMatchedPlayers.size(), 5);
         int boxHeight = maxShow * 11 + 4;
         int finalY = renderY - boxHeight;
         guiGraphics.fill(renderX - 2, finalY, renderX + 160, renderY, -872415232);

         for (int i = 0; i != maxShow; i++) {
            String pName = this.currentMatchedPlayers.get(i);
            int textY = finalY + 3 + i * 11;
            if (i == this.tabCycleIndex) {
               guiGraphics.text(this.font, Component.literal("§6> " + pName + " §f\\uE001"), renderX, textY, 16753920, false);
            } else {
               guiGraphics.text(this.font, Component.literal("  " + pName + " §7\\uE001"), renderX, textY, 11184810, false);
            }
         }
      }
   }

   public boolean keyPressed(KeyEvent event) {
      int key = event.key();
      if (this.showSuggestionsList && !this.currentMatchedPlayers.isEmpty()) {
         if (key == 264) {
            this.tabCycleIndex = (this.tabCycleIndex + 1) % this.currentMatchedPlayers.size();
            return true;
         }

         if (key == 265) {
            this.tabCycleIndex = (this.tabCycleIndex - 1 + this.currentMatchedPlayers.size()) % this.currentMatchedPlayers.size();
            return true;
         }
      }

      if (key == 258) {
         this.handlePlayerTabCompletion();
         return true;
      } else if (key != 257 && key != 335) {
         if (key == 256) {
            if (this.upperInputBox != null && this.bottomInputBox != null) {
               prefixCache.put(this.currentSlot, this.upperInputBox.getValue());
               messageCache.put(this.currentSlot, this.bottomInputBox.getValue());
               this.saveSlotsToDisk();
            }

            this.onClose();
            return true;
         } else {
            return super.keyPressed(event);
         }
      } else {
         if (this.upperInputBox != null && this.bottomInputBox != null) {
            prefixCache.put(this.currentSlot, this.upperInputBox.getValue());
            messageCache.put(this.currentSlot, this.bottomInputBox.getValue());
            this.injectCommandToSelectedArea();
         }

         return true;
      }
   }

   public boolean isPauseScreen() {
      return false;
   }
}
