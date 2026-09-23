package chunk.faye.mod_tog.faycore.client.gui;

import chunk.faye.mod_tog.faycore.client.gui.OneCmdManagerScreen.CommandEntryData;
import chunk.faye.mod_tog.faycore.client.gui.OneCmdManagerScreen.PageSaveData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Blocks;

public class OneCmdManagerScreen extends Screen {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private int currentPage = 1;
   private final int maxPages = 20;
   private final List<CommandEntryData> commandDataList = new ArrayList<>();
   private int scrollStartIndex = 0;
   private Button buttonDirection;
   private Button buttonAdd;
   private Button buttonExport;
   private Button buttonPrevPage;
   private Button buttonNextPage;
   private int directionMode = 0;
   private Button buttonScrollUp;
   private Button buttonScrollDown;
   private boolean genOptionA = false;
   private boolean genOptionB = true;
   private boolean genOptionC = false;
   private Button genOptionCheckboxA;
   private Button genOptionCheckboxB;
   private Button genOptionCheckboxC;
   private EditBox InputCmdblockPos;
   public static int CmdblockPosX = 0;
   public static int CmdblockPosY = 0;
   public static int CmdblockPosZ = 0;
   public static String FullInputCmdblockPos = null;
   private int OutputMode = 1;
   private boolean isDraggingScrollBar = false;

   public OneCmdManagerScreen() {
      super(Component.literal("FayCore 一鍵指令管理器"));
      this.loadCommandsFromLocal();
   }

   private File getPageFile(int page) {
      File configDir = new File(Minecraft.getInstance().gameDirectory, "config");
      if (!configDir.exists()) {
         configDir.mkdirs();
      }

      return new File(configDir, "faycore_onecmd_page_" + page + ".json");
   }

   private void loadCommandsFromLocal() {
      this.commandDataList.clear();
      this.scrollStartIndex = 0;
      this.directionMode = 0;
      File file = this.getPageFile(this.currentPage);
      if (file.exists()) {
         try (FileReader reader = new FileReader(file)) {
            PageSaveData loadedData = (PageSaveData)GSON.fromJson(reader, PageSaveData.class);
            if (loadedData != null) {
               this.directionMode = loadedData.savedDirectionMode;
               if (loadedData.savedCommands != null) {
                  this.commandDataList.addAll(loadedData.savedCommands);
               }
            }
         } catch (Exception var7) {
            var7.printStackTrace();
         }
      }

      if (this.commandDataList.isEmpty()) {
         this.commandDataList.add(new CommandEntryData("say Hello", 0));
      }
   }

   public void saveCommandsToLocal() {
      File file = this.getPageFile(this.currentPage);

      try (FileWriter writer = new FileWriter(file)) {
         PageSaveData dataToSave = new PageSaveData(this.directionMode, this.commandDataList);
         GSON.toJson(dataToSave, writer);
      } catch (Exception var7) {
         var7.printStackTrace();
      }
   }

   private void compileAndExportGiantCommand() {
      StringBuilder minecartChain = new StringBuilder();
      int currentOffset2 = 0;
      int currentOffset1 = 0;
      int validCommandCount = 0;
      boolean isContinuationOfLoop = false;

      for (CommandEntryData data : this.commandDataList) {
         String rawCmd = data.commandText;
         if (rawCmd != null && !rawCmd.trim().isEmpty()) {
            validCommandCount++;
            String blockType = "command_block";
            String nbtFlags = "auto:1b";
            String facing = "east";

            facing = switch (this.directionMode) {
               case 0 -> "east";
               case 1 -> "west";
               case 2 -> "south";
               case 3 -> "north";
               case 4, 5, 6, 7, 8 -> "up";
               default -> "east";
            };
            String blockState = String.format("facing=%s", facing);
            if (data.mode == 0) {
               blockType = "command_block";
               nbtFlags = "auto:0b";
               blockState = String.format("facing=%s", facing);
            }

            if (data.mode == 1) {
               if (validCommandCount > 1) {
                  currentOffset1++;
                  currentOffset2 = 0;
               }

               blockType = "repeating_command_block";
               nbtFlags = "auto:1b";
               blockState = String.format("facing=%s", facing);
               isContinuationOfLoop = false;
            }

            if (data.mode == 2) {
               currentOffset2++;
               blockType = "chain_command_block";
               nbtFlags = "auto:1b";
               blockState = String.format("facing=%s", facing);
               isContinuationOfLoop = false;
            }

            if (data.mode == 3) {
               currentOffset2++;
               blockType = "chain_command_block";
               nbtFlags = "auto:1b";
               blockState = String.format("conditional=true,facing=%s", facing);
               isContinuationOfLoop = false;
            }

            if (data.mode == 4) {
               if (validCommandCount > 1) {
                  currentOffset1++;
                  currentOffset2 = 0;
               }

               blockType = "command_block";
               nbtFlags = "auto:0b";
               blockState = String.format("facing=%s", facing);
               isContinuationOfLoop = false;
            }

            String FullInputCmdblockPos = this.InputCmdblockPos.getValue();
            if ((!FullInputCmdblockPos.equals("~") || FullInputCmdblockPos == null) && !FullInputCmdblockPos.trim().isEmpty()) {
               String[] variables = FullInputCmdblockPos.split("[ \\t]*,[ \\t]*");
               if (variables.length >= 3) {
                  try {
                     CmdblockPosX = Integer.parseInt(variables[0].trim());
                     CmdblockPosY = Integer.parseInt(variables[1].trim());
                     CmdblockPosZ = Integer.parseInt(variables[2].trim());
                  } catch (NumberFormatException var16) {
                     if (this.minecraft != null && this.minecraft.player != null) {
                        this.minecraft.player.sendSystemMessage(Component.literal("§9[FayCore] §cError: 座標內必須填寫純數字！"));
                        this.minecraft.setScreen(null);
                        return;
                     }
                  }
               } else if (this.minecraft != null && this.minecraft.player != null) {
                  this.minecraft.player.sendSystemMessage(Component.literal("§9[FayCore] §cError: 請確保使用逗號分隔 X,Y,Z 三軸座標！"));
                  this.minecraft.setScreen(null);
                  return;
               }
            }

            String safeUserCmd = rawCmd.trim();
            safeUserCmd = safeUserCmd.replaceAll("&(?=[0-9a-fk-orA-FK-OR])", "§");
            safeUserCmd = safeUserCmd.replace("\\", "\\\\");
            safeUserCmd = safeUserCmd.replace("'", "\\'");
            safeUserCmd = safeUserCmd.replace("\"", "\\\"");
            String innerPayload = String.format(
               "setblock ~%d ~1 ~%d %s[%s]{%s,Command:'%s'}", currentOffset2, currentOffset1, blockType, blockState, nbtFlags, safeUserCmd
            );
            if (FullInputCmdblockPos.equals("~")) {
               if (data.mode != 0) {
                  switch (this.directionMode) {
                     case 0:
                        innerPayload = String.format(
                           "setblock ~%d ~-2 ~%d %s[%s]{%s,Command:'%s'}",
                           currentOffset2,
                           currentOffset1 * -1 - 1,
                           blockType,
                           blockState,
                           nbtFlags,
                           safeUserCmd
                        );
                        break;
                     case 1:
                        innerPayload = String.format(
                           "setblock ~%d ~-2 ~%d %s[%s]{%s,Command:'%s'}",
                           currentOffset2 * -1,
                           currentOffset1 + 1,
                           blockType,
                           blockState,
                           nbtFlags,
                           safeUserCmd
                        );
                        break;
                     case 2:
                        innerPayload = String.format(
                           "setblock ~%d ~-2 ~%d %s[%s]{%s,Command:'%s'}", currentOffset1 + 1, currentOffset2, blockType, blockState, nbtFlags, safeUserCmd
                        );
                        break;
                     case 3:
                        innerPayload = String.format(
                           "setblock ~%d ~-2 ~%d %s[%s]{%s,Command:'%s'}",
                           currentOffset1 * -1 - 1,
                           currentOffset2 * -1,
                           blockType,
                           blockState,
                           nbtFlags,
                           safeUserCmd
                        );
                        break;
                     case 4:
                        innerPayload = String.format(
                           "setblock ~%d ~%d ~ %s[%s]{%s,Command:'%s'}", currentOffset1 + 1, currentOffset2 - 2, blockType, blockState, nbtFlags, safeUserCmd
                        );
                        break;
                     case 5:
                        innerPayload = String.format(
                           "setblock ~%d ~%d ~ %s[%s]{%s,Command:'%s'}",
                           currentOffset1 * -1 - 1,
                           currentOffset2 - 2,
                           blockType,
                           blockState,
                           nbtFlags,
                           safeUserCmd
                        );
                        break;
                     case 6:
                        innerPayload = String.format(
                           "setblock ~ ~%d ~%d %s[%s]{%s,Command:'%s'}", currentOffset2 - 2, currentOffset1 + 1, blockType, blockState, nbtFlags, safeUserCmd
                        );
                        break;
                     case 7:
                        innerPayload = String.format(
                           "setblock ~ ~%d ~%d %s[%s]{%s,Command:'%s'}",
                           currentOffset2 - 2,
                           currentOffset1 * -1 - 1,
                           blockType,
                           blockState,
                           nbtFlags,
                           safeUserCmd
                        );
                  }

                  minecartChain.append(String.format("{id:command_block_minecart,Command:\"%s\",Tags:[\"_.\"]},", innerPayload));
               } else {
                  innerPayload = String.format("%s", safeUserCmd);
                  minecartChain.append(String.format("{id:command_block_minecart,Command:\"%s\",Tags:[\"_.\"]},", innerPayload));
               }
            } else if (data.mode != 0) {
               switch (this.directionMode) {
                  case 0:
                     innerPayload = String.format(
                        "setblock %d %d %d %s[%s]{%s,Command:'%s'}",
                        CmdblockPosX + currentOffset2,
                        CmdblockPosY - 2,
                        CmdblockPosZ + (currentOffset1 * -1 - 1),
                        blockType,
                        blockState,
                        nbtFlags,
                        safeUserCmd
                     );
                     break;
                  case 1:
                     innerPayload = String.format(
                        "setblock %d %d %d %s[%s]{%s,Command:'%s'}",
                        CmdblockPosX + currentOffset2 * -1,
                        CmdblockPosY - 2,
                        CmdblockPosZ + currentOffset1 + 1,
                        blockType,
                        blockState,
                        nbtFlags,
                        safeUserCmd
                     );
                     break;
                  case 2:
                     innerPayload = String.format(
                        "setblock %d %d %d %s[%s]{%s,Command:'%s'}",
                        CmdblockPosX + currentOffset1 + 1,
                        CmdblockPosY - 2,
                        CmdblockPosZ + currentOffset2,
                        blockType,
                        blockState,
                        nbtFlags,
                        safeUserCmd
                     );
                     break;
                  case 3:
                     innerPayload = String.format(
                        "setblock %d %d %d %s[%s]{%s,Command:'%s'}",
                        CmdblockPosX + (currentOffset1 * -1 - 1),
                        CmdblockPosY - 2,
                        CmdblockPosZ + currentOffset2 * -1,
                        blockType,
                        blockState,
                        nbtFlags,
                        safeUserCmd
                     );
                     break;
                  case 4:
                     innerPayload = String.format(
                        "setblock %d %d %d %s[%s]{%s,Command:'%s'}",
                        CmdblockPosX + currentOffset1 + 1,
                        CmdblockPosY + (currentOffset2 - 2),
                        CmdblockPosZ,
                        blockType,
                        blockState,
                        nbtFlags,
                        safeUserCmd
                     );
                     break;
                  case 5:
                     innerPayload = String.format(
                        "setblock %d %d %d %s[%s]{%s,Command:'%s'}",
                        CmdblockPosX + (currentOffset1 * -1 - 1),
                        CmdblockPosY + (currentOffset2 - 2),
                        CmdblockPosZ,
                        blockType,
                        blockState,
                        nbtFlags,
                        safeUserCmd
                     );
                     break;
                  case 6:
                     innerPayload = String.format(
                        "setblock %d %d %d %s[%s]{%s,Command:'%s'}",
                        CmdblockPosX,
                        CmdblockPosY + (currentOffset2 - 2),
                        CmdblockPosZ + currentOffset1 + 1,
                        blockType,
                        blockState,
                        nbtFlags,
                        safeUserCmd
                     );
                     break;
                  case 7:
                     innerPayload = String.format(
                        "setblock %d %d %d %s[%s]{%s,Command:'%s'}",
                        CmdblockPosX,
                        CmdblockPosY + (currentOffset2 - 2),
                        CmdblockPosZ + (currentOffset1 * -1 - 1),
                        blockType,
                        blockState,
                        nbtFlags,
                        safeUserCmd
                     );
               }

               minecartChain.append(String.format("{id:command_block_minecart,Command:\"%s\",Tags:[\"_.\"]},", innerPayload));
            } else {
               innerPayload = String.format("%s", safeUserCmd);
               minecartChain.append(String.format("{id:command_block_minecart,Command:\"%s\",Tags:[\"_.\"]},", innerPayload));
            }
         }
      }

      if (validCommandCount == 0) {
         if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.sendSystemMessage(Component.literal("§9[FayCore] §cErrror: No commands!"));
         }
      } else {
         String finalOneCmd = "summon falling_block ~ ~.5 ~ {BlockState:{Name:redstone_block},Passengers:[{id:shulker,DeathTime:20,Health:0f,AttachFace:0b,Passengers:[{id:falling_block,BlockState:{Name:activator_rail},Time:1,Passengers:["
            + minecartChain.toString()
            + "{id:command_block_minecart,Command:\"setblock ~ ~1 ~ command_block{auto:1b,Command:'fill ~ ~ ~ ~ ~-3 ~ air'}\",Tags:[\"_.\"]},{id:command_block_minecart,Command:\"kill @e[tag=_.,distance=..0]\",Tags:[\"_.\"]}]}]}]}";
         if (this.minecraft != null) {
            if (this.OutputMode == 0) {
               this.minecraft.keyboardHandler.setClipboard(finalOneCmd);
               if (this.minecraft.player != null) {
                  this.minecraft.player.playSound(SoundEvents.ENDER_DRAGON_SHOOT, 1.0F, 1.2F);
                  this.minecraft.player.sendSystemMessage(Component.literal("§9[FayCore] §7Copied one cmd."));
                  this.minecraft.setScreen(null);
               }
            }

            if (this.OutputMode == 1 && this.minecraft != null && this.minecraft.player != null) {
               if (this.minecraft.player.isCreative()) {
                  ItemStack commandBlockItem = new ItemStack(Blocks.COMMAND_BLOCK, 1);
                  CompoundTag blockEntityNbt = new CompoundTag();
                  blockEntityNbt.putString("id", "minecraft:command_block");
                  blockEntityNbt.putString("Command", finalOneCmd);
                  blockEntityNbt.putByte("auto", (byte)1);
                  DataComponents.BLOCK_ENTITY_DATA
                     .codec()
                     .parse(NbtOps.INSTANCE, blockEntityNbt)
                     .result()
                     .ifPresent(structuralObject -> commandBlockItem.set(DataComponents.BLOCK_ENTITY_DATA, structuralObject));
                  CompoundTag backupTag = new CompoundTag();
                  backupTag.put("BlockEntityTag", blockEntityNbt);
                  commandBlockItem.set(DataComponents.CUSTOM_DATA, CustomData.of(backupTag));
                  commandBlockItem.set(DataComponents.CUSTOM_NAME, Component.literal("§9FayCore One Command").withStyle(style -> style.withItalic(false)));
                  commandBlockItem.set(
                     DataComponents.LORE,
                     new ItemLore(
                        List.of(
                           Component.literal("ꜰᴀʏᴄᴏʀᴇ").withStyle(style -> style.withColor(11599765).withItalic(false)),
                           Component.literal("§7Made by §cFayeCruz").withStyle(style -> style.withItalic(false))
                        )
                     )
                  );
                  int currentSlotIndex = this.minecraft.player.getInventory().getSelectedSlot();
                  if (this.minecraft.player.connection != null) {
                     this.minecraft.player.connection.send(new ServerboundSetCreativeModeSlotPacket(36 + currentSlotIndex, commandBlockItem));
                  }

                  if (this.minecraft.player == null) {
                     return;
                  }

                  this.minecraft.player.getInventory().setItem(currentSlotIndex, commandBlockItem);
                  this.minecraft.player.getInventory().setChanged();
                  this.minecraft.player.playSound((SoundEvent)SoundEvents.NOTE_BLOCK_PLING.value(), 1.0F, 2.0F);
                  this.minecraft.player.playSound(SoundEvents.ITEM_PICKUP, 1.0F, 1.5F);
                  this.minecraft.player.sendSystemMessage(Component.literal("§9[FayCore] §7Gave one cmd block."));
                  this.minecraft.setScreen(null);
               } else {
                  this.minecraft.player.playSound((SoundEvent)SoundEvents.NOTE_BLOCK_PLING.value(), 1.0F, 0.0F);
                  this.minecraft.player.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
                  this.minecraft.player.sendSystemMessage(Component.literal("§9[FayCore] §cErrror: Request creative!"));
                  this.minecraft.setScreen(null);
               }
            }

            if (this.OutputMode == 2 && this.minecraft != null && this.minecraft.player != null) {
               if (this.minecraft.player.isCreative()) {
                  ItemStack commandBlockItemx = new ItemStack(Items.VILLAGER_SPAWN_EGG, 1);
                  CompoundTag tileEntityDataNbt = new CompoundTag();
                  tileEntityDataNbt.putString("Command", finalOneCmd);
                  tileEntityDataNbt.putByte("auto", (byte)1);
                  CompoundTag blockStateNbt = new CompoundTag();
                  blockStateNbt.putString("Name", "minecraft:command_block");
                  CompoundTag entityDataNbt = new CompoundTag();
                  entityDataNbt.putString("id", "minecraft:falling_block");
                  entityDataNbt.putInt("Time", 1);
                  entityDataNbt.put("BlockState", blockStateNbt);
                  entityDataNbt.put("TileEntityData", tileEntityDataNbt);
                  DataComponents.ENTITY_DATA
                     .codec()
                     .parse(NbtOps.INSTANCE, entityDataNbt)
                     .result()
                     .ifPresent(structuralObject -> commandBlockItem.set(DataComponents.ENTITY_DATA, structuralObject));
                  commandBlockItemx.set(DataComponents.CUSTOM_NAME, Component.literal("§9FayCore One Command").withStyle(style -> style.withItalic(false)));
                  commandBlockItemx.set(
                     DataComponents.LORE,
                     new ItemLore(
                        List.of(
                           Component.literal("ꜰᴀʏᴄᴏʀᴇ").withStyle(style -> style.withColor(11599765).withItalic(false)),
                           Component.literal("§7Made by §cFayeCruz").withStyle(style -> style.withItalic(false))
                        )
                     )
                  );
                  int currentSlotIndexx = this.minecraft.player.getInventory().getSelectedSlot();
                  if (this.minecraft.player.connection != null) {
                     this.minecraft.player.connection.send(new ServerboundSetCreativeModeSlotPacket(36 + currentSlotIndexx, commandBlockItemx));
                  }

                  if (this.minecraft.player == null) {
                     return;
                  }

                  this.minecraft.player.getInventory().setItem(currentSlotIndexx, commandBlockItemx);
                  this.minecraft.player.getInventory().setChanged();
                  this.minecraft.player.playSound((SoundEvent)SoundEvents.NOTE_BLOCK_PLING.value(), 1.0F, 2.0F);
                  this.minecraft.player.playSound(SoundEvents.ITEM_PICKUP, 1.0F, 1.5F);
                  this.minecraft.player.sendSystemMessage(Component.literal("§9[FayCore] §7Gave one cmd block."));
                  this.minecraft.setScreen(null);
               } else {
                  this.minecraft.player.playSound((SoundEvent)SoundEvents.NOTE_BLOCK_PLING.value(), 1.0F, 0.0F);
                  this.minecraft.player.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
                  this.minecraft.player.sendSystemMessage(Component.literal("§9[FayCore] §cErrror: Request creative!"));
                  this.minecraft.setScreen(null);
               }
            }
         }
      }
   }

   protected void init() {
      super.init();
      int inputBoxWidth = 200;
      int inputBoxHeight = 20;
      int xPos = (this.width - inputBoxWidth) / 2;
      int yPos = this.height / 2 + 5;
      this.InputCmdblockPos = new EditBox(this.font, xPos, yPos, inputBoxWidth, inputBoxHeight, Component.literal("座標輸入框"));
      this.InputCmdblockPos.setMaxLength(32767);
      this.InputCmdblockPos.setValue("~");
      this.addRenderableWidget(this.InputCmdblockPos);
      String[] dirNames = new String[]{"X+", "X-", "Z+", "Z-", "YX+", "YX-", "YZ+", "YZ-"};
      String initialDirName = dirNames[this.directionMode >= 0 && this.directionMode < 8 ? this.directionMode : 0];
      this.buttonDirection = Button.builder(Component.literal(initialDirName), btn -> {
         String currentText = btn.getMessage().getString();
         if (currentText.equals("X+")) {
            this.directionMode = 1;
            btn.setMessage(Component.literal("X-"));
         } else if (currentText.equals("X-")) {
            this.directionMode = 2;
            btn.setMessage(Component.literal("Z+"));
         } else if (currentText.equals("Z+")) {
            this.directionMode = 3;
            btn.setMessage(Component.literal("Z-"));
         } else if (currentText.equals("Z-")) {
            this.directionMode = 4;
            btn.setMessage(Component.literal("YX+"));
         } else if (currentText.equals("YX+")) {
            this.directionMode = 5;
            btn.setMessage(Component.literal("YX-"));
         } else if (currentText.equals("YX-")) {
            this.directionMode = 6;
            btn.setMessage(Component.literal("YZ+"));
         } else if (currentText.equals("YZ+")) {
            this.directionMode = 7;
            btn.setMessage(Component.literal("YZ-"));
         } else {
            this.directionMode = 0;
            btn.setMessage(Component.literal("X+"));
         }

         this.saveCommandsToLocal();
         this.refreshList();
      }).bounds(this.width / 2 - 250, this.height - 35, 50, 20).build();
      this.buttonAdd = Button.builder(Component.literal("＋ Add"), btn -> {
         this.commandDataList.add(new CommandEntryData("", 0));
         if (this.commandDataList.size() > 4) {
            this.scrollStartIndex = this.commandDataList.size() - 4;
         }

         this.refreshList();
      }).bounds(this.width / 2 - 195, this.height - 35, 50, 20).build();
      this.buttonScrollUp = Button.builder(Component.literal("▲"), btn -> {
         if (this.scrollStartIndex > 0) {
            this.scrollStartIndex--;
            this.refreshList();
         }
      }).bounds(this.width / 2 - 140, this.height - 35, 20, 20).build();
      this.buttonScrollDown = Button.builder(Component.literal("▼"), btn -> {
         if (this.scrollStartIndex < this.commandDataList.size() - 4) {
            this.scrollStartIndex++;
            this.refreshList();
         }
      }).bounds(this.width / 2 - 116, this.height - 35, 20, 20).build();
      this.buttonPrevPage = Button.builder(Component.literal("◀ Prev"), btn -> {
         if (this.currentPage > 1) {
            this.saveCommandsToLocal();
            this.currentPage--;
            this.loadCommandsFromLocal();
            this.refreshList();
         }
      }).bounds(this.width / 2 - 91, this.height - 35, 55, 20).build();
      this.buttonExport = Button.builder(Component.literal("\ud83d\udcbe Export OneCmd"), btn -> {
         String FullInputCmdblockPos = this.InputCmdblockPos.getValue();
         this.saveCommandsToLocal();
         this.compileAndExportGiantCommand();
      }).bounds(this.width / 2 - 31, this.height - 35, 110, 20).build();
      this.buttonNextPage = Button.builder(Component.literal("Next ▶"), btn -> {
         if (this.currentPage < 20) {
            this.saveCommandsToLocal();
            this.currentPage++;
            this.loadCommandsFromLocal();
            this.refreshList();
         }
      }).bounds(this.width / 2 + 84, this.height - 35, 55, 20).build();
      this.refreshList();
   }

   public void updateButtonStates() {
      if (this.buttonPrevPage != null) {
         this.buttonPrevPage.active = this.currentPage > 1;
      }

      if (this.buttonNextPage != null) {
         this.buttonNextPage.active = this.currentPage < 20;
      }

      if (this.buttonScrollUp != null) {
         this.buttonScrollUp.active = this.scrollStartIndex > 0;
      }

      if (this.buttonScrollDown != null) {
         this.buttonScrollDown.active = this.scrollStartIndex < this.commandDataList.size() - 4;
      }
   }

   public void refreshList() {
      this.clearWidgets();
      if (this.InputCmdblockPos != null) {
         int inputBoxWidth = 100;
         int inputBoxHeight = 20;
         int xPos = this.width / 2 - 215;
         int yPos = 12;
         this.InputCmdblockPos = new EditBox(this.font, xPos, yPos, inputBoxWidth, inputBoxHeight, Component.literal("Edit cmd block pos"));
         this.InputCmdblockPos.setMaxLength(32767);
         this.InputCmdblockPos.setValue("~");
         this.InputCmdblockPos.setResponder(text -> {
            if (text != null && !text.equals("~") && !text.trim().isEmpty()) {
               String[] vars = text.split("[ \\t]*,[ \\t]*");
               if (vars.length >= 3) {
                  try {
                     CmdblockPosX = Integer.parseInt(vars[0].trim());
                     CmdblockPosY = Integer.parseInt(vars[1].trim());
                     CmdblockPosZ = Integer.parseInt(vars[2].trim());
                  } catch (NumberFormatException var4x) {
                  }
               }
            }

            this.saveCommandsToLocal();
         });
         this.addRenderableWidget(this.InputCmdblockPos);
      }

      int radioY = this.height - 60;
      int startRadioX = this.width / 2 - 150;
      if (this.buttonDirection != null) {
         this.addRenderableWidget(this.buttonDirection);
      }

      String textLabelA = this.genOptionA ? "§a[●] Copy" : "§7[○] Copy";
      this.genOptionCheckboxA = Button.builder(Component.literal(textLabelA), btn -> {
         if (!this.genOptionA) {
            this.genOptionA = true;
            this.genOptionB = false;
            this.genOptionC = false;
            this.OutputMode = 0;
            this.saveCommandsToLocal();
            this.refreshList();
         }
      }).bounds(startRadioX, radioY, 85, 20).build();
      String textLabelB = this.genOptionB ? "§a[●] Cmd Block" : "§7[○] Cmd Block";
      this.genOptionCheckboxB = Button.builder(Component.literal(textLabelB), btn -> {
         if (!this.genOptionB) {
            this.genOptionA = false;
            this.genOptionB = true;
            this.genOptionC = false;
            this.OutputMode = 1;
            this.saveCommandsToLocal();
            this.refreshList();
         }
      }).bounds(startRadioX + 100, radioY, 85, 20).build();
      String textLabelC = this.genOptionC ? "§a[●] Spawn Egg" : "§7[○] Spawn Egg";
      this.genOptionCheckboxC = Button.builder(Component.literal(textLabelC), btn -> {
         if (!this.genOptionC) {
            this.genOptionA = false;
            this.genOptionB = false;
            this.genOptionC = true;
            this.OutputMode = 2;
            this.saveCommandsToLocal();
            this.refreshList();
         }
      }).bounds(startRadioX + 200, radioY, 85, 20).build();
      if (this.genOptionCheckboxA != null) {
         this.addRenderableWidget(this.genOptionCheckboxA);
         this.addRenderableWidget(this.genOptionCheckboxB);
         this.addRenderableWidget(this.genOptionCheckboxC);
      }

      if (this.buttonAdd != null) {
         this.addRenderableWidget(this.buttonAdd);
         this.addRenderableWidget(this.buttonScrollUp);
         this.addRenderableWidget(this.buttonScrollDown);
         this.addRenderableWidget(this.buttonPrevPage);
         this.addRenderableWidget(this.buttonExport);
         this.addRenderableWidget(this.buttonNextPage);
         this.updateButtonStates();
      }

      int startY = 40;
      int startX = this.width / 2 - 215;
      int endIndex = Math.min(this.scrollStartIndex + 4, this.commandDataList.size());
      int renderRowIndex = 0;

      for (int i = this.scrollStartIndex; i < endIndex; i++) {
         int index = i;
         CommandEntryData data = this.commandDataList.get(index);
         int currentY = startY + renderRowIndex * 34;
         renderRowIndex++;
         String[] modeNames = new String[]{"§7[Once]", "§9[Repeat]", "§3[Chain]", "§3[§7@§3Chain]", "§6[Normal]"};
         String initialName = modeNames[data.mode >= 0 && data.mode < 5 ? data.mode : 0];
         Button btnMode = Button.builder(Component.literal(initialName), btn -> {
            String currentText = btn.getMessage().getString();
            if (currentText.equals(modeNames[0])) {
               data.mode = 1;
               btn.setMessage(Component.literal(modeNames[1]));
            } else if (currentText.equals(modeNames[1])) {
               data.mode = 2;
               btn.setMessage(Component.literal(modeNames[2]));
            } else if (currentText.equals(modeNames[2])) {
               data.mode = 3;
               btn.setMessage(Component.literal(modeNames[3]));
            } else if (currentText.equals(modeNames[3])) {
               data.mode = 4;
               btn.setMessage(Component.literal(modeNames[4]));
            } else {
               data.mode = 0;
               btn.setMessage(Component.literal(modeNames[0]));
            }

            this.saveCommandsToLocal();
         }).bounds(startX, currentY + 5, 85, 20).build();
         this.addRenderableWidget(btnMode);
         MultiLineEditBox multiLineBox = MultiLineEditBox.builder()
            .setX(startX + 89)
            .setY(currentY)
            .setPlaceholder(Component.literal("Type code payload..."))
            .build(this.font, 190, 30, Component.literal("OneCmdText"));
         multiLineBox.setCharacterLimit(32767);
         multiLineBox.setValue(data.commandText);
         multiLineBox.setValueListener(text -> {
            data.commandText = text;
            this.saveCommandsToLocal();
         });
         this.addRenderableWidget(multiLineBox);
         Button btnUp = Button.builder(Component.literal("▲"), btn -> {
            if (index > 0) {
               this.saveCommandsToLocal();
               Collections.swap(this.commandDataList, index, index - 1);
               if (this.scrollStartIndex > 0 && index == this.scrollStartIndex) {
                  this.scrollStartIndex--;
               }

               this.refreshList();
            }
         }).bounds(startX + 283, currentY, 20, 14).build();
         btnUp.active = index > 0;
         this.addRenderableWidget(btnUp);
         Button btnDown = Button.builder(Component.literal("▼"), btn -> {
            if (index < this.commandDataList.size() - 1) {
               this.saveCommandsToLocal();
               Collections.swap(this.commandDataList, index, index + 1);
               if (index == this.scrollStartIndex + 3) {
                  this.scrollStartIndex++;
               }

               this.refreshList();
            }
         }).bounds(startX + 283, currentY + 16, 20, 14).build();
         btnDown.active = index < this.commandDataList.size() - 1;
         this.addRenderableWidget(btnDown);
         Button btnDel = Button.builder(Component.literal("✕"), btn -> {
            this.commandDataList.remove(index);
            this.saveCommandsToLocal();
            if (this.scrollStartIndex > 0 && this.scrollStartIndex >= this.commandDataList.size() - 3) {
               this.scrollStartIndex--;
            }

            this.refreshList();
         }).bounds(startX + 307, currentY + 8, 20, 14).build();
         this.addRenderableWidget(btnDel);
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (verticalAmount > 0.0 && this.scrollStartIndex > 0) {
         this.scrollStartIndex--;
         this.refreshList();
         return true;
      } else if (verticalAmount < 0.0 && this.scrollStartIndex < this.commandDataList.size() - 4) {
         this.scrollStartIndex++;
         this.refreshList();
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
      }
   }

   public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
      double mouseX = event.x();
      double mouseY = event.y();
      int button = event.button();
      int scrollBarX = this.width / 2 + 120;
      if (button == 0 && mouseX >= (double)scrollBarX && mouseX <= (double)(scrollBarX + 6) && mouseY >= 40.0 && mouseY <= 176.0) {
         this.isDraggingScrollBar = true;
         return true;
      } else {
         return super.mouseClicked(event, bl);
      }
   }

   public void handleMouseReleaseState(int button) {
      if (button == 0) {
         this.isDraggingScrollBar = false;
      }
   }

   public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
      guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
      super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
      int totalItems = this.commandDataList.size();
      int maxVisible = 4;
      int barContainerHeight = 136;
      int scrollBarX = this.width / 2 + 120;
      guiGraphics.fillGradient(scrollBarX, 40, scrollBarX + 6, 176, -15658735, -15658735);
      if (totalItems > maxVisible) {
         int barHeight = Math.max(20, maxVisible * barContainerHeight / totalItems);
         int maxScrollSlots = totalItems - maxVisible;
         int currentBarY = 40 + (barContainerHeight - barHeight) * this.scrollStartIndex / maxScrollSlots;
         if (this.isDraggingScrollBar) {
            int boundedMouseY = Math.max(40, Math.min(176 - barHeight, mouseY - barHeight / 2));
            int targetIndex = (boundedMouseY - 40) * maxScrollSlots / (barContainerHeight - barHeight);
            if (targetIndex != this.scrollStartIndex && targetIndex >= 0 && targetIndex <= maxScrollSlots) {
               this.scrollStartIndex = targetIndex;
               this.refreshList();
            }
         }

         int barColor = this.isDraggingScrollBar ? -3355444 : -7829368;
         guiGraphics.fillGradient(scrollBarX, currentBarY, scrollBarX + 6, currentBarY + barHeight, barColor, barColor);
      }

      String pageInfoStr = String.format("§d§lFayCore OneCmd §e(Page: %d / %d)", this.currentPage, 20);
      guiGraphics.text(this.font, Component.literal(pageInfoStr), this.width / 2 - 120, 15, 16777215, true);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
