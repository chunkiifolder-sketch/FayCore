/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.mojang.serialization.DynamicOps
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.MultiLineEditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtOps
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.component.CustomData
 *  net.minecraft.world.item.component.ItemLore
 *  net.minecraft.world.item.component.TypedEntityData
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Blocks
 */
package chunk.faye.mod_tog.faycore.client.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.DynamicOps;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class OneCmdManagerScreen
extends Screen {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private int currentPage = 1;
    private final int maxPages = 20;
    private final List<CommandEntryData> commandDataList = new ArrayList<CommandEntryData>();
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
        super((Component)Component.literal((String)"FayCore \u4e00\u9375\u6307\u4ee4\u7ba1\u7406\u5668"));
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
            try (FileReader reader = new FileReader(file);){
                PageSaveData loadedData = (PageSaveData)GSON.fromJson((Reader)reader, PageSaveData.class);
                if (loadedData != null) {
                    this.directionMode = loadedData.savedDirectionMode;
                    if (loadedData.savedCommands != null) {
                        this.commandDataList.addAll(loadedData.savedCommands);
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.commandDataList.isEmpty()) {
            this.commandDataList.add(new CommandEntryData("say Hello", 0));
        }
    }

    public void saveCommandsToLocal() {
        File file = this.getPageFile(this.currentPage);
        try (FileWriter writer = new FileWriter(file);){
            PageSaveData dataToSave = new PageSaveData(this.directionMode, this.commandDataList);
            GSON.toJson((Object)dataToSave, (Appendable)writer);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Unable to fully structure code
     */
    private void compileAndExportGiantCommand() {
        minecartChain = new StringBuilder();
        currentOffset2 = 0;
        currentOffset1 = 0;
        validCommandCount = 0;
        isContinuationOfLoop = false;
        for (CommandEntryData data : this.commandDataList) {
            rawCmd = data.commandText;
            if (rawCmd == null || rawCmd.trim().isEmpty()) continue;
            ++validCommandCount;
            blockType = "command_block";
            nbtFlags = "auto:1b";
            facing = "east";
            switch (this.directionMode) {
                case 0: {
                    facing = "east";
                    break;
                }
                case 1: {
                    facing = "west";
                    break;
                }
                case 2: {
                    facing = "south";
                    break;
                }
                case 3: {
                    facing = "north";
                    break;
                }
                case 4: 
                case 5: 
                case 6: 
                case 7: 
                case 8: {
                    facing = "up";
                    break;
                }
                default: {
                    facing = "east";
                }
            }
            blockState = String.format("facing=%s", new Object[]{facing});
            if (data.mode == 0) {
                blockType = "command_block";
                nbtFlags = "auto:0b";
                blockState = String.format("facing=%s", new Object[]{facing});
            }
            if (data.mode == 1) {
                if (validCommandCount > 1) {
                    ++currentOffset1;
                    currentOffset2 = 0;
                }
                blockType = "repeating_command_block";
                nbtFlags = "auto:1b";
                blockState = String.format("facing=%s", new Object[]{facing});
                isContinuationOfLoop = false;
            }
            if (data.mode == 2) {
                ++currentOffset2;
                blockType = "chain_command_block";
                nbtFlags = "auto:1b";
                blockState = String.format("facing=%s", new Object[]{facing});
                isContinuationOfLoop = false;
            }
            if (data.mode == 3) {
                ++currentOffset2;
                blockType = "chain_command_block";
                nbtFlags = "auto:1b";
                blockState = String.format("conditional=true,facing=%s", new Object[]{facing});
                isContinuationOfLoop = false;
            }
            if (data.mode == 4) {
                if (validCommandCount > 1) {
                    ++currentOffset1;
                    currentOffset2 = 0;
                }
                blockType = "command_block";
                nbtFlags = "auto:0b";
                blockState = String.format("facing=%s", new Object[]{facing});
                isContinuationOfLoop = false;
            }
            if (!((FullInputCmdblockPos = this.InputCmdblockPos.getValue()).equals("~") && FullInputCmdblockPos != null || FullInputCmdblockPos.trim().isEmpty())) {
                variables = FullInputCmdblockPos.split("[ \\t]*,[ \\t]*");
                if (variables.length >= 3) {
                    try {
                        OneCmdManagerScreen.CmdblockPosX = Integer.parseInt(variables[0].trim());
                        OneCmdManagerScreen.CmdblockPosY = Integer.parseInt(variables[1].trim());
                        OneCmdManagerScreen.CmdblockPosZ = Integer.parseInt(variables[2].trim());
                    }
                    catch (NumberFormatException e) {
                        if (this.minecraft == null || this.minecraft.player == null) ** GOTO lbl81
                        this.minecraft.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a7cError: \u5ea7\u6a19\u5167\u5fc5\u9808\u586b\u5beb\u7d14\u6578\u5b57\uff01"));
                        this.minecraft.setScreen(null);
                        return;
                    }
                } else if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a7cError: \u8acb\u78ba\u4fdd\u4f7f\u7528\u9017\u865f\u5206\u9694 X,Y,Z \u4e09\u8ef8\u5ea7\u6a19\uff01"));
                    this.minecraft.setScreen(null);
                    return;
                }
            }
lbl81:
            // 6 sources

            safeUserCmd = rawCmd.trim();
            safeUserCmd = safeUserCmd.replaceAll("&(?=[0-9a-fk-orA-FK-OR])", "\u00a7");
            safeUserCmd = safeUserCmd.replace("\\", "\\\\");
            safeUserCmd = safeUserCmd.replace("'", "\\'");
            safeUserCmd = safeUserCmd.replace("\"", "\\\"");
            innerPayload = String.format("setblock ~%d ~1 ~%d %s[%s]{%s,Command:'%s'}", new Object[]{currentOffset2, currentOffset1, blockType, blockState, nbtFlags, safeUserCmd});
            if (FullInputCmdblockPos.equals("~")) {
                if (data.mode != 0) {
                    switch (this.directionMode) {
                        case 0: {
                            innerPayload = String.format("setblock ~%d ~-2 ~%d %s[%s]{%s,Command:'%s'}", new Object[]{currentOffset2, currentOffset1 * -1 - 1, blockType, blockState, nbtFlags, safeUserCmd});
                            break;
                        }
                        case 1: {
                            innerPayload = String.format("setblock ~%d ~-2 ~%d %s[%s]{%s,Command:'%s'}", new Object[]{currentOffset2 * -1, currentOffset1 + 1, blockType, blockState, nbtFlags, safeUserCmd});
                            break;
                        }
                        case 2: {
                            innerPayload = String.format("setblock ~%d ~-2 ~%d %s[%s]{%s,Command:'%s'}", new Object[]{currentOffset1 + 1, currentOffset2, blockType, blockState, nbtFlags, safeUserCmd});
                            break;
                        }
                        case 3: {
                            innerPayload = String.format("setblock ~%d ~-2 ~%d %s[%s]{%s,Command:'%s'}", new Object[]{currentOffset1 * -1 - 1, currentOffset2 * -1, blockType, blockState, nbtFlags, safeUserCmd});
                            break;
                        }
                        case 4: {
                            innerPayload = String.format("setblock ~%d ~%d ~ %s[%s]{%s,Command:'%s'}", new Object[]{currentOffset1 + 1, currentOffset2 - 2, blockType, blockState, nbtFlags, safeUserCmd});
                            break;
                        }
                        case 5: {
                            innerPayload = String.format("setblock ~%d ~%d ~ %s[%s]{%s,Command:'%s'}", new Object[]{currentOffset1 * -1 - 1, currentOffset2 - 2, blockType, blockState, nbtFlags, safeUserCmd});
                            break;
                        }
                        case 6: {
                            innerPayload = String.format("setblock ~ ~%d ~%d %s[%s]{%s,Command:'%s'}", new Object[]{currentOffset2 - 2, currentOffset1 + 1, blockType, blockState, nbtFlags, safeUserCmd});
                            break;
                        }
                        case 7: {
                            innerPayload = String.format("setblock ~ ~%d ~%d %s[%s]{%s,Command:'%s'}", new Object[]{currentOffset2 - 2, currentOffset1 * -1 - 1, blockType, blockState, nbtFlags, safeUserCmd});
                        }
                    }
                    minecartChain.append(String.format("{id:command_block_minecart,Command:\"%s\",Tags:[\"_.\"]},", new Object[]{innerPayload}));
                    continue;
                }
                innerPayload = String.format("%s", new Object[]{safeUserCmd});
                minecartChain.append(String.format("{id:command_block_minecart,Command:\"%s\",Tags:[\"_.\"]},", new Object[]{innerPayload}));
                continue;
            }
            if (data.mode != 0) {
                switch (this.directionMode) {
                    case 0: {
                        innerPayload = String.format("setblock %d %d %d %s[%s]{%s,Command:'%s'}", new Object[]{OneCmdManagerScreen.CmdblockPosX + currentOffset2, OneCmdManagerScreen.CmdblockPosY - 2, OneCmdManagerScreen.CmdblockPosZ + (currentOffset1 * -1 - 1), blockType, blockState, nbtFlags, safeUserCmd});
                        break;
                    }
                    case 1: {
                        innerPayload = String.format("setblock %d %d %d %s[%s]{%s,Command:'%s'}", new Object[]{OneCmdManagerScreen.CmdblockPosX + currentOffset2 * -1, OneCmdManagerScreen.CmdblockPosY - 2, OneCmdManagerScreen.CmdblockPosZ + (currentOffset1 + 1), blockType, blockState, nbtFlags, safeUserCmd});
                        break;
                    }
                    case 2: {
                        innerPayload = String.format("setblock %d %d %d %s[%s]{%s,Command:'%s'}", new Object[]{OneCmdManagerScreen.CmdblockPosX + (currentOffset1 + 1), OneCmdManagerScreen.CmdblockPosY - 2, OneCmdManagerScreen.CmdblockPosZ + currentOffset2, blockType, blockState, nbtFlags, safeUserCmd});
                        break;
                    }
                    case 3: {
                        innerPayload = String.format("setblock %d %d %d %s[%s]{%s,Command:'%s'}", new Object[]{OneCmdManagerScreen.CmdblockPosX + (currentOffset1 * -1 - 1), OneCmdManagerScreen.CmdblockPosY - 2, OneCmdManagerScreen.CmdblockPosZ + currentOffset2 * -1, blockType, blockState, nbtFlags, safeUserCmd});
                        break;
                    }
                    case 4: {
                        innerPayload = String.format("setblock %d %d %d %s[%s]{%s,Command:'%s'}", new Object[]{OneCmdManagerScreen.CmdblockPosX + (currentOffset1 + 1), OneCmdManagerScreen.CmdblockPosY + (currentOffset2 - 2), OneCmdManagerScreen.CmdblockPosZ, blockType, blockState, nbtFlags, safeUserCmd});
                        break;
                    }
                    case 5: {
                        innerPayload = String.format("setblock %d %d %d %s[%s]{%s,Command:'%s'}", new Object[]{OneCmdManagerScreen.CmdblockPosX + (currentOffset1 * -1 - 1), OneCmdManagerScreen.CmdblockPosY + (currentOffset2 - 2), OneCmdManagerScreen.CmdblockPosZ, blockType, blockState, nbtFlags, safeUserCmd});
                        break;
                    }
                    case 6: {
                        innerPayload = String.format("setblock %d %d %d %s[%s]{%s,Command:'%s'}", new Object[]{OneCmdManagerScreen.CmdblockPosX, OneCmdManagerScreen.CmdblockPosY + (currentOffset2 - 2), OneCmdManagerScreen.CmdblockPosZ + (currentOffset1 + 1), blockType, blockState, nbtFlags, safeUserCmd});
                        break;
                    }
                    case 7: {
                        innerPayload = String.format("setblock %d %d %d %s[%s]{%s,Command:'%s'}", new Object[]{OneCmdManagerScreen.CmdblockPosX, OneCmdManagerScreen.CmdblockPosY + (currentOffset2 - 2), OneCmdManagerScreen.CmdblockPosZ + (currentOffset1 * -1 - 1), blockType, blockState, nbtFlags, safeUserCmd});
                    }
                }
                minecartChain.append(String.format("{id:command_block_minecart,Command:\"%s\",Tags:[\"_.\"]},", new Object[]{innerPayload}));
                continue;
            }
            innerPayload = String.format("%s", new Object[]{safeUserCmd});
            minecartChain.append(String.format("{id:command_block_minecart,Command:\"%s\",Tags:[\"_.\"]},", new Object[]{innerPayload}));
        }
        if (validCommandCount == 0) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a7cErrror: No commands!"));
            }
            return;
        }
        finalOneCmd = "summon falling_block ~ ~.5 ~ {BlockState:{Name:redstone_block},Passengers:[{id:shulker,DeathTime:20,Health:0f,AttachFace:0b,Passengers:[{id:falling_block,BlockState:{Name:activator_rail},Time:1,Passengers:[" + minecartChain.toString() + "{id:command_block_minecart,Command:\"setblock ~ ~1 ~ command_block{auto:1b,Command:'fill ~ ~ ~ ~ ~-3 ~ air'}\",Tags:[\"_.\"]},{id:command_block_minecart,Command:\"kill @e[tag=_.,distance=..0]\",Tags:[\"_.\"]}]}]}]}";
        if (this.minecraft != null) {
            if (this.OutputMode == 0) {
                this.minecraft.keyboardHandler.setClipboard(finalOneCmd);
                if (this.minecraft.player != null) {
                    this.minecraft.player.playSound(SoundEvents.ENDER_DRAGON_SHOOT, 1.0f, 1.2f);
                    this.minecraft.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a77Copied one cmd."));
                    this.minecraft.setScreen(null);
                }
            }
            if (this.OutputMode == 1 && this.minecraft != null && this.minecraft.player != null) {
                if (this.minecraft.player.isCreative()) {
                    commandBlockItem = new ItemStack((ItemLike)Blocks.COMMAND_BLOCK, 1);
                    blockEntityNbt = new CompoundTag();
                    blockEntityNbt.putString("id", "minecraft:command_block");
                    blockEntityNbt.putString("Command", finalOneCmd);
                    blockEntityNbt.putByte("auto", (byte)1);
                    DataComponents.BLOCK_ENTITY_DATA.codec().parse((DynamicOps)NbtOps.INSTANCE, (Object)blockEntityNbt).result().ifPresent((Consumer<TypedEntityData>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)V, lambda$compileAndExportGiantCommand$0(net.minecraft.world.item.ItemStack net.minecraft.world.item.component.TypedEntityData ), (Lnet/minecraft/world/item/component/TypedEntityData;)V)((ItemStack)commandBlockItem));
                    backupTag = new CompoundTag();
                    backupTag.put("BlockEntityTag", (Tag)blockEntityNbt);
                    commandBlockItem.set(DataComponents.CUSTOM_DATA, (Object)CustomData.of((CompoundTag)backupTag));
                    commandBlockItem.set(DataComponents.CUSTOM_NAME, (Object)Component.literal((String)"\u00a79FayCore One Command").withStyle((UnaryOperator)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, lambda$compileAndExportGiantCommand$1(net.minecraft.network.chat.Style ), (Lnet/minecraft/network/chat/Style;)Lnet/minecraft/network/chat/Style;)()));
                    commandBlockItem.set(DataComponents.LORE, (Object)new ItemLore(List.of(Component.literal((String)"\ua730\u1d00\u028f\u1d04\u1d0f\u0280\u1d07").withStyle((UnaryOperator)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, lambda$compileAndExportGiantCommand$2(net.minecraft.network.chat.Style ), (Lnet/minecraft/network/chat/Style;)Lnet/minecraft/network/chat/Style;)()), Component.literal((String)"\u00a77Made by \u00a7cFayeCruz").withStyle((UnaryOperator)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, lambda$compileAndExportGiantCommand$3(net.minecraft.network.chat.Style ), (Lnet/minecraft/network/chat/Style;)Lnet/minecraft/network/chat/Style;)()))));
                    currentSlotIndex = this.minecraft.player.getInventory().getSelectedSlot();
                    if (this.minecraft.player.connection != null) {
                        this.minecraft.player.connection.send((Packet)new ServerboundSetCreativeModeSlotPacket(36 + currentSlotIndex, commandBlockItem));
                    }
                    if (this.minecraft.player == null) {
                        return;
                    }
                    this.minecraft.player.getInventory().setItem(currentSlotIndex, commandBlockItem);
                    this.minecraft.player.getInventory().setChanged();
                    this.minecraft.player.playSound((SoundEvent)SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 2.0f);
                    this.minecraft.player.playSound(SoundEvents.ITEM_PICKUP, 1.0f, 1.5f);
                    this.minecraft.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a77Gave one cmd block."));
                    this.minecraft.setScreen(null);
                } else {
                    this.minecraft.player.playSound((SoundEvent)SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 0.0f);
                    this.minecraft.player.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                    this.minecraft.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a7cErrror: Request creative!"));
                    this.minecraft.setScreen(null);
                }
            }
            if (this.OutputMode == 2 && this.minecraft != null && this.minecraft.player != null) {
                if (this.minecraft.player.isCreative()) {
                    commandBlockItem = new ItemStack((ItemLike)Items.VILLAGER_SPAWN_EGG, 1);
                    tileEntityDataNbt = new CompoundTag();
                    tileEntityDataNbt.putString("Command", finalOneCmd);
                    tileEntityDataNbt.putByte("auto", (byte)1);
                    blockStateNbt = new CompoundTag();
                    blockStateNbt.putString("Name", "minecraft:command_block");
                    entityDataNbt = new CompoundTag();
                    entityDataNbt.putString("id", "minecraft:falling_block");
                    entityDataNbt.putInt("Time", 1);
                    entityDataNbt.put("BlockState", (Tag)blockStateNbt);
                    entityDataNbt.put("TileEntityData", (Tag)tileEntityDataNbt);
                    DataComponents.ENTITY_DATA.codec().parse((DynamicOps)NbtOps.INSTANCE, (Object)entityDataNbt).result().ifPresent((Consumer<TypedEntityData>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)V, lambda$compileAndExportGiantCommand$4(net.minecraft.world.item.ItemStack net.minecraft.world.item.component.TypedEntityData ), (Lnet/minecraft/world/item/component/TypedEntityData;)V)((ItemStack)commandBlockItem));
                    commandBlockItem.set(DataComponents.CUSTOM_NAME, (Object)Component.literal((String)"\u00a79FayCore One Command").withStyle((UnaryOperator)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, lambda$compileAndExportGiantCommand$5(net.minecraft.network.chat.Style ), (Lnet/minecraft/network/chat/Style;)Lnet/minecraft/network/chat/Style;)()));
                    commandBlockItem.set(DataComponents.LORE, (Object)new ItemLore(List.of(Component.literal((String)"\ua730\u1d00\u028f\u1d04\u1d0f\u0280\u1d07").withStyle((UnaryOperator)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, lambda$compileAndExportGiantCommand$6(net.minecraft.network.chat.Style ), (Lnet/minecraft/network/chat/Style;)Lnet/minecraft/network/chat/Style;)()), Component.literal((String)"\u00a77Made by \u00a7cFayeCruz").withStyle((UnaryOperator)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, lambda$compileAndExportGiantCommand$7(net.minecraft.network.chat.Style ), (Lnet/minecraft/network/chat/Style;)Lnet/minecraft/network/chat/Style;)()))));
                    currentSlotIndex = this.minecraft.player.getInventory().getSelectedSlot();
                    if (this.minecraft.player.connection != null) {
                        this.minecraft.player.connection.send((Packet)new ServerboundSetCreativeModeSlotPacket(36 + currentSlotIndex, commandBlockItem));
                    }
                    if (this.minecraft.player == null) {
                        return;
                    }
                    this.minecraft.player.getInventory().setItem(currentSlotIndex, commandBlockItem);
                    this.minecraft.player.getInventory().setChanged();
                    this.minecraft.player.playSound((SoundEvent)SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 2.0f);
                    this.minecraft.player.playSound(SoundEvents.ITEM_PICKUP, 1.0f, 1.5f);
                    this.minecraft.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a77Gave one cmd block."));
                    this.minecraft.setScreen(null);
                } else {
                    this.minecraft.player.playSound((SoundEvent)SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 0.0f);
                    this.minecraft.player.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                    this.minecraft.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a7cErrror: Request creative!"));
                    this.minecraft.setScreen(null);
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
        this.InputCmdblockPos = new EditBox(this.font, xPos, yPos, inputBoxWidth, inputBoxHeight, (Component)Component.literal((String)"\u5ea7\u6a19\u8f38\u5165\u6846"));
        this.InputCmdblockPos.setMaxLength(Short.MAX_VALUE);
        this.InputCmdblockPos.setValue("~");
        this.addRenderableWidget((GuiEventListener)this.InputCmdblockPos);
        String[] dirNames = new String[]{"X+", "X-", "Z+", "Z-", "YX+", "YX-", "YZ+", "YZ-"};
        String initialDirName = dirNames[this.directionMode >= 0 && this.directionMode < 8 ? this.directionMode : 0];
        this.buttonDirection = Button.builder((Component)Component.literal((String)initialDirName), btn -> {
            String currentText = btn.getMessage().getString();
            if (currentText.equals("X+")) {
                this.directionMode = 1;
                btn.setMessage((Component)Component.literal((String)"X-"));
            } else if (currentText.equals("X-")) {
                this.directionMode = 2;
                btn.setMessage((Component)Component.literal((String)"Z+"));
            } else if (currentText.equals("Z+")) {
                this.directionMode = 3;
                btn.setMessage((Component)Component.literal((String)"Z-"));
            } else if (currentText.equals("Z-")) {
                this.directionMode = 4;
                btn.setMessage((Component)Component.literal((String)"YX+"));
            } else if (currentText.equals("YX+")) {
                this.directionMode = 5;
                btn.setMessage((Component)Component.literal((String)"YX-"));
            } else if (currentText.equals("YX-")) {
                this.directionMode = 6;
                btn.setMessage((Component)Component.literal((String)"YZ+"));
            } else if (currentText.equals("YZ+")) {
                this.directionMode = 7;
                btn.setMessage((Component)Component.literal((String)"YZ-"));
            } else {
                this.directionMode = 0;
                btn.setMessage((Component)Component.literal((String)"X+"));
            }
            this.saveCommandsToLocal();
            this.refreshList();
        }).bounds(this.width / 2 - 250, this.height - 35, 50, 20).build();
        this.buttonAdd = Button.builder((Component)Component.literal((String)"\uff0b Add"), btn -> {
            this.commandDataList.add(new CommandEntryData("", 0));
            if (this.commandDataList.size() > 4) {
                this.scrollStartIndex = this.commandDataList.size() - 4;
            }
            this.refreshList();
        }).bounds(this.width / 2 - 195, this.height - 35, 50, 20).build();
        this.buttonScrollUp = Button.builder((Component)Component.literal((String)"\u25b2"), btn -> {
            if (this.scrollStartIndex > 0) {
                --this.scrollStartIndex;
                this.refreshList();
            }
        }).bounds(this.width / 2 - 140, this.height - 35, 20, 20).build();
        this.buttonScrollDown = Button.builder((Component)Component.literal((String)"\u25bc"), btn -> {
            if (this.scrollStartIndex < this.commandDataList.size() - 4) {
                ++this.scrollStartIndex;
                this.refreshList();
            }
        }).bounds(this.width / 2 - 116, this.height - 35, 20, 20).build();
        this.buttonPrevPage = Button.builder((Component)Component.literal((String)"\u25c0 Prev"), btn -> {
            if (this.currentPage > 1) {
                this.saveCommandsToLocal();
                --this.currentPage;
                this.loadCommandsFromLocal();
                this.refreshList();
            }
        }).bounds(this.width / 2 - 91, this.height - 35, 55, 20).build();
        this.buttonExport = Button.builder((Component)Component.literal((String)"\ud83d\udcbe Export OneCmd"), btn -> {
            String FullInputCmdblockPos = this.InputCmdblockPos.getValue();
            this.saveCommandsToLocal();
            this.compileAndExportGiantCommand();
        }).bounds(this.width / 2 - 31, this.height - 35, 110, 20).build();
        this.buttonNextPage = Button.builder((Component)Component.literal((String)"Next \u25b6"), btn -> {
            if (this.currentPage < this.maxPages) {
                this.saveCommandsToLocal();
                ++this.currentPage;
                this.loadCommandsFromLocal();
                this.refreshList();
            }
        }).bounds(this.width / 2 + 84, this.height - 35, 55, 20).build();
        this.refreshList();
    }

    public void updateButtonStates() {
        if (this.buttonPrevPage != null) {
            boolean bl = this.buttonPrevPage.active = this.currentPage > 1;
        }
        if (this.buttonNextPage != null) {
            boolean bl = this.buttonNextPage.active = this.currentPage < this.maxPages;
        }
        if (this.buttonScrollUp != null) {
            boolean bl = this.buttonScrollUp.active = this.scrollStartIndex > 0;
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
            this.InputCmdblockPos = new EditBox(this.font, xPos, yPos, inputBoxWidth, inputBoxHeight, (Component)Component.literal((String)"Edit cmd block pos"));
            this.InputCmdblockPos.setMaxLength(Short.MAX_VALUE);
            this.InputCmdblockPos.setValue("~");
            this.InputCmdblockPos.setResponder(text -> {
                String[] vars;
                if (text != null && !text.equals("~") && !text.trim().isEmpty() && (vars = text.split("[ \\t]*,[ \\t]*")).length >= 3) {
                    try {
                        CmdblockPosX = Integer.parseInt(vars[0].trim());
                        CmdblockPosY = Integer.parseInt(vars[1].trim());
                        CmdblockPosZ = Integer.parseInt(vars[2].trim());
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                }
                this.saveCommandsToLocal();
            });
            this.addRenderableWidget((GuiEventListener)this.InputCmdblockPos);
        }
        int radioY = this.height - 60;
        int startRadioX = this.width / 2 - 150;
        if (this.buttonDirection != null) {
            this.addRenderableWidget((GuiEventListener)this.buttonDirection);
        }
        String textLabelA = this.genOptionA ? "\u00a7a[\u25cf] Copy" : "\u00a77[\u25cb] Copy";
        this.genOptionCheckboxA = Button.builder((Component)Component.literal((String)textLabelA), btn -> {
            if (this.genOptionA) {
                return;
            }
            this.genOptionA = true;
            this.genOptionB = false;
            this.genOptionC = false;
            this.OutputMode = 0;
            this.saveCommandsToLocal();
            this.refreshList();
        }).bounds(startRadioX, radioY, 85, 20).build();
        String textLabelB = this.genOptionB ? "\u00a7a[\u25cf] Cmd Block" : "\u00a77[\u25cb] Cmd Block";
        this.genOptionCheckboxB = Button.builder((Component)Component.literal((String)textLabelB), btn -> {
            if (this.genOptionB) {
                return;
            }
            this.genOptionA = false;
            this.genOptionB = true;
            this.genOptionC = false;
            this.OutputMode = 1;
            this.saveCommandsToLocal();
            this.refreshList();
        }).bounds(startRadioX + 100, radioY, 85, 20).build();
        String textLabelC = this.genOptionC ? "\u00a7a[\u25cf] Spawn Egg" : "\u00a77[\u25cb] Spawn Egg";
        this.genOptionCheckboxC = Button.builder((Component)Component.literal((String)textLabelC), btn -> {
            if (this.genOptionC) {
                return;
            }
            this.genOptionA = false;
            this.genOptionB = false;
            this.genOptionC = true;
            this.OutputMode = 2;
            this.saveCommandsToLocal();
            this.refreshList();
        }).bounds(startRadioX + 200, radioY, 85, 20).build();
        if (this.genOptionCheckboxA != null) {
            this.addRenderableWidget((GuiEventListener)this.genOptionCheckboxA);
            this.addRenderableWidget((GuiEventListener)this.genOptionCheckboxB);
            this.addRenderableWidget((GuiEventListener)this.genOptionCheckboxC);
        }
        if (this.buttonAdd != null) {
            this.addRenderableWidget((GuiEventListener)this.buttonAdd);
            this.addRenderableWidget((GuiEventListener)this.buttonScrollUp);
            this.addRenderableWidget((GuiEventListener)this.buttonScrollDown);
            this.addRenderableWidget((GuiEventListener)this.buttonPrevPage);
            this.addRenderableWidget((GuiEventListener)this.buttonExport);
            this.addRenderableWidget((GuiEventListener)this.buttonNextPage);
            this.updateButtonStates();
        }
        int startY = 40;
        int startX = this.width / 2 - 215;
        int endIndex = Math.min(this.scrollStartIndex + 4, this.commandDataList.size());
        int renderRowIndex = 0;
        for (int i = this.scrollStartIndex; i < endIndex; ++i) {
            int index = i;
            CommandEntryData data = this.commandDataList.get(index);
            int currentY = startY + renderRowIndex * 34;
            ++renderRowIndex;
            String[] modeNames = new String[]{"\u00a77[Once]", "\u00a79[Repeat]", "\u00a73[Chain]", "\u00a73[\u00a77@\u00a73Chain]", "\u00a76[Normal]"};
            String initialName = modeNames[data.mode >= 0 && data.mode < 5 ? data.mode : 0];
            Button btnMode = Button.builder((Component)Component.literal((String)initialName), btn -> {
                String currentText = btn.getMessage().getString();
                if (currentText.equals(modeNames[0])) {
                    data.mode = 1;
                    btn.setMessage((Component)Component.literal((String)modeNames[1]));
                } else if (currentText.equals(modeNames[1])) {
                    data.mode = 2;
                    btn.setMessage((Component)Component.literal((String)modeNames[2]));
                } else if (currentText.equals(modeNames[2])) {
                    data.mode = 3;
                    btn.setMessage((Component)Component.literal((String)modeNames[3]));
                } else if (currentText.equals(modeNames[3])) {
                    data.mode = 4;
                    btn.setMessage((Component)Component.literal((String)modeNames[4]));
                } else {
                    data.mode = 0;
                    btn.setMessage((Component)Component.literal((String)modeNames[0]));
                }
                this.saveCommandsToLocal();
            }).bounds(startX, currentY + 5, 85, 20).build();
            this.addRenderableWidget((GuiEventListener)btnMode);
            MultiLineEditBox multiLineBox = MultiLineEditBox.builder().setX(startX + 89).setY(currentY).setPlaceholder((Component)Component.literal((String)"Type code payload...")).build(this.font, 190, 30, (Component)Component.literal((String)"OneCmdText"));
            multiLineBox.setCharacterLimit(Short.MAX_VALUE);
            multiLineBox.setValue(data.commandText);
            multiLineBox.setValueListener(text -> {
                data.commandText = text;
                this.saveCommandsToLocal();
            });
            this.addRenderableWidget((GuiEventListener)multiLineBox);
            Button btnUp = Button.builder((Component)Component.literal((String)"\u25b2"), btn -> {
                if (index > 0) {
                    this.saveCommandsToLocal();
                    Collections.swap(this.commandDataList, index, index - 1);
                    if (this.scrollStartIndex > 0 && index == this.scrollStartIndex) {
                        --this.scrollStartIndex;
                    }
                    this.refreshList();
                }
            }).bounds(startX + 283, currentY, 20, 14).build();
            btnUp.active = index > 0;
            this.addRenderableWidget((GuiEventListener)btnUp);
            Button btnDown = Button.builder((Component)Component.literal((String)"\u25bc"), btn -> {
                if (index < this.commandDataList.size() - 1) {
                    this.saveCommandsToLocal();
                    Collections.swap(this.commandDataList, index, index + 1);
                    if (index == this.scrollStartIndex + 3) {
                        ++this.scrollStartIndex;
                    }
                    this.refreshList();
                }
            }).bounds(startX + 283, currentY + 16, 20, 14).build();
            btnDown.active = index < this.commandDataList.size() - 1;
            this.addRenderableWidget((GuiEventListener)btnDown);
            Button btnDel = Button.builder((Component)Component.literal((String)"\u2715"), btn -> {
                this.commandDataList.remove(index);
                this.saveCommandsToLocal();
                if (this.scrollStartIndex > 0 && this.scrollStartIndex >= this.commandDataList.size() - 3) {
                    --this.scrollStartIndex;
                }
                this.refreshList();
            }).bounds(startX + 307, currentY + 8, 20, 14).build();
            this.addRenderableWidget((GuiEventListener)btnDel);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount > 0.0 && this.scrollStartIndex > 0) {
            --this.scrollStartIndex;
            this.refreshList();
            return true;
        }
        if (verticalAmount < 0.0 && this.scrollStartIndex < this.commandDataList.size() - 4) {
            ++this.scrollStartIndex;
            this.refreshList();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        int scrollBarX = this.width / 2 + 120;
        if (button == 0 && mouseX >= (double)scrollBarX && mouseX <= (double)(scrollBarX + 6) && mouseY >= 40.0 && mouseY <= 176.0) {
            this.isDraggingScrollBar = true;
            return true;
        }
        return super.mouseClicked(event, bl);
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
            int boundedMouseY;
            int targetIndex;
            int barHeight = Math.max(20, maxVisible * barContainerHeight / totalItems);
            int maxScrollSlots = totalItems - maxVisible;
            int currentBarY = 40 + (barContainerHeight - barHeight) * this.scrollStartIndex / maxScrollSlots;
            if (this.isDraggingScrollBar && (targetIndex = ((boundedMouseY = Math.max(40, Math.min(176 - barHeight, mouseY - barHeight / 2))) - 40) * maxScrollSlots / (barContainerHeight - barHeight)) != this.scrollStartIndex && targetIndex >= 0 && targetIndex <= maxScrollSlots) {
                this.scrollStartIndex = targetIndex;
                this.refreshList();
            }
            int barColor = this.isDraggingScrollBar ? -3355444 : -7829368;
            guiGraphics.fillGradient(scrollBarX, currentBarY, scrollBarX + 6, currentBarY + barHeight, barColor, barColor);
        }
        String pageInfoStr = String.format("\u00a7d\u00a7lFayCore OneCmd \u00a7e(Page: %d / %d)", this.currentPage, this.maxPages);
        guiGraphics.text(this.font, (Component)Component.literal((String)pageInfoStr), this.width / 2 - 120, 15, 0xFFFFFF, true);
    }

    public boolean isPauseScreen() {
        return false;
    }

    private static /* synthetic */ Style lambda$compileAndExportGiantCommand$7(Style style) {
        return style.withItalic(Boolean.valueOf(false));
    }

    private static /* synthetic */ Style lambda$compileAndExportGiantCommand$6(Style style) {
        return style.withColor(11599765).withItalic(Boolean.valueOf(false));
    }

    private static /* synthetic */ Style lambda$compileAndExportGiantCommand$5(Style style) {
        return style.withItalic(Boolean.valueOf(false));
    }

    private static /* synthetic */ void lambda$compileAndExportGiantCommand$4(ItemStack commandBlockItem, TypedEntityData structuralObject) {
        commandBlockItem.set(DataComponents.ENTITY_DATA, (Object)structuralObject);
    }

    private static /* synthetic */ Style lambda$compileAndExportGiantCommand$3(Style style) {
        return style.withItalic(Boolean.valueOf(false));
    }

    private static /* synthetic */ Style lambda$compileAndExportGiantCommand$2(Style style) {
        return style.withColor(11599765).withItalic(Boolean.valueOf(false));
    }

    private static /* synthetic */ Style lambda$compileAndExportGiantCommand$1(Style style) {
        return style.withItalic(Boolean.valueOf(false));
    }

    private static /* synthetic */ void lambda$compileAndExportGiantCommand$0(ItemStack commandBlockItem, TypedEntityData structuralObject) {
        commandBlockItem.set(DataComponents.BLOCK_ENTITY_DATA, (Object)structuralObject);
    }

    public static class PageSaveData {
        public int savedDirectionMode;
        public List<CommandEntryData> savedCommands;

        public PageSaveData(int directionMode, List<CommandEntryData> commands) {
            this.savedDirectionMode = directionMode;
            this.savedCommands = commands;
        }
    }

    public static class CommandEntryData {
        public String commandText;
        public int mode;

        public CommandEntryData(String text, int mode) {
            this.commandText = text;
            this.mode = mode;
        }
    }
}

