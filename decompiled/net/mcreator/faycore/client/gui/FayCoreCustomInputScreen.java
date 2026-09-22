/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundChatCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.CommandBlockEntity
 *  net.minecraft.world.level.block.entity.CommandBlockEntity$Mode
 */
package net.mcreator.faycore.client.gui;

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
import net.mcreator.faycore.client.gui.FayCoreMacroEngine;
import net.mcreator.faycore.client.gui.FaycoreSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;

public class FayCoreCustomInputScreen
extends Screen {
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
    private List<String> currentMatchedPlayers = new ArrayList<String>();
    private static final Map<Integer, String> prefixCache = new HashMap<Integer, String>();
    private static final Map<Integer, String> messageCache = new HashMap<Integer, String>();
    private static final File CONFIG_FILE;
    private EditBox targetPlayerInputBox;
    private static final Map<Integer, String> targetPlayerCache;

    private boolean checkClientHasOP() {
        return true;
    }

    public FayCoreCustomInputScreen() {
        super((Component)Component.literal((String)"FayCore \u5faa\u74b0\u6307\u4ee4\u65b9\u584a\u6392\u7a0b\u5668"));
        prefixCache.putIfAbsent(1, "&b[\u5ee3\u64ad\u524d\u7db4]");
        prefixCache.putIfAbsent(2, "&e[\u5718\u968a\u5ee3\u64ad]");
        prefixCache.putIfAbsent(3, "&c[\u8b66\u544a]");
        messageCache.putIfAbsent(1, "\u9019\u662f\u9810\u8a2d\u8a0a\u606f\u5167\u5bb9\u3002");
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
            try (PrintWriter writer = new PrintWriter(new FileWriter(CONFIG_FILE));){
                writer.println("currentSlot=" + this.currentSlot);
                for (int i = 1; i != 4; ++i) {
                    writer.println("slot_" + i + "_p=" + prefixCache.getOrDefault(i, ""));
                    writer.println("slot_" + i + "_m=" + messageCache.getOrDefault(i, ""));
                    writer.println("slot_" + i + "_t=" + targetPlayerCache.getOrDefault(i, ""));
                }
                writer.flush();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void loadSlotsFromDisk() {
        if (CONFIG_FILE.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE));){
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] csParts;
                    String trimmed = line.trim();
                    if (trimmed.startsWith("currentSlot=") && (csParts = trimmed.split("=", 2)).length == 2) {
                        String csVal = Arrays.asList(csParts).get(1);
                        this.currentSlot = Integer.parseInt(csVal);
                    }
                    for (int i = 1; i != 4; ++i) {
                        String[] tokens;
                        if (trimmed.startsWith("slot_" + i + "_t=") && (tokens = trimmed.split("=", 2)).length == 2) {
                            targetPlayerCache.put(i, Arrays.asList(tokens).get(1));
                        }
                        if (trimmed.startsWith("slot_" + i + "_p=") && (tokens = trimmed.split("=", 2)).length == 2) {
                            String pVal = Arrays.asList(tokens).get(1);
                            prefixCache.put(i, pVal);
                        }
                        if (!trimmed.startsWith("slot_" + i + "_m=") || (tokens = trimmed.split("=", 2)).length != 2) continue;
                        String mVal = Arrays.asList(tokens).get(1);
                        messageCache.put(i, mVal);
                    }
                }
            }
            catch (Exception exception) {
                // empty catch block
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
        this.bottomInputBox = new EditBox(this.font, paddingLeft, bottomY, boxWidth, boxHeight, (Component)Component.literal((String)"\u767c\u9001\u8a0a\u606f"));
        this.bottomInputBox.setHint((Component)Component.literal((String)"Chat..."));
        this.bottomInputBox.setMaxLength(255);
        this.bottomInputBox.setValue(messageCache.getOrDefault(this.currentSlot, ""));
        this.addRenderableWidget((GuiEventListener)this.bottomInputBox);
        this.bottomInputBox.setResponder(text -> {
            String intermediate;
            int cursorPos = this.bottomInputBox.getCursorPosition();
            int atIndex = text.lastIndexOf(64, cursorPos - 1);
            if (atIndex != -1 && !(intermediate = text.substring(atIndex, cursorPos)).contains(" ")) {
                String prefixQuery = intermediate.substring(1).toLowerCase();
                Minecraft innerMc = Minecraft.getInstance();
                if (innerMc.getConnection() != null) {
                    Collection players = innerMc.getConnection().getOnlinePlayers();
                    this.currentMatchedPlayers.clear();
                    for (PlayerInfo info : players) {
                        if (info.getProfile() == null) continue;
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
                        if (pName.isEmpty() || !pName.toLowerCase().startsWith(prefixQuery) && !prefixQuery.isEmpty()) continue;
                        this.currentMatchedPlayers.add(pName);
                    }
                }
                this.showSuggestionsList = !this.currentMatchedPlayers.isEmpty();
                return;
            }
            this.showSuggestionsList = false;
        });
        int upperY = bottomY - boxHeight - spacingY;
        this.upperInputBox = new EditBox(this.font, paddingLeft, upperY, boxWidth, boxHeight, (Component)Component.literal((String)"Prefix"));
        this.upperInputBox.setHint((Component)Component.literal((String)"Prefix"));
        this.upperInputBox.setMaxLength(64);
        this.upperInputBox.setValue(prefixCache.getOrDefault(this.currentSlot, ""));
        this.addRenderableWidget((GuiEventListener)this.upperInputBox);
        int btnX = paddingLeft + boxWidth + 8;
        int btnWidth = 85;
        this.toggleSlotButton = Button.builder((Component)Component.literal((String)("Slot: " + this.currentSlot)), button -> {
            prefixCache.put(this.currentSlot, this.upperInputBox.getValue());
            messageCache.put(this.currentSlot, this.bottomInputBox.getValue());
            this.injectCommandToSelectedArea();
            this.saveSlotsToDisk();
            this.currentSlot = this.currentSlot % 3 + 1;
            button.setMessage((Component)Component.literal((String)("Slot: " + this.currentSlot)));
            this.upperInputBox.setValue(prefixCache.getOrDefault(this.currentSlot, ""));
            this.bottomInputBox.setValue(messageCache.getOrDefault(this.currentSlot, ""));
        }).bounds(btnX, upperY, btnWidth, boxHeight).build();
        this.addRenderableWidget((GuiEventListener)this.toggleSlotButton);
        this.toggleSlotButton = Button.builder((Component)Component.literal((String)("Slot: " + this.currentSlot)), button -> {
            prefixCache.put(this.currentSlot, this.upperInputBox.getValue());
            messageCache.put(this.currentSlot, this.bottomInputBox.getValue());
            targetPlayerCache.put(this.currentSlot, this.targetPlayerInputBox.getValue());
            this.injectCommandToSelectedArea();
            this.saveSlotsToDisk();
            this.currentSlot = this.currentSlot % 3 + 1;
            button.setMessage((Component)Component.literal((String)("Slot: " + this.currentSlot)));
            this.upperInputBox.setValue(prefixCache.getOrDefault(this.currentSlot, ""));
            this.bottomInputBox.setValue(messageCache.getOrDefault(this.currentSlot, ""));
            this.targetPlayerInputBox.setValue(targetPlayerCache.getOrDefault(this.currentSlot, ""));
        }).bounds(btnX, upperY, btnWidth, boxHeight).build();
        int targetPlayerY = upperY + boxHeight + spacingY;
        this.targetPlayerInputBox = new EditBox(this.font, btnX, targetPlayerY, btnWidth, boxHeight, (Component)Component.literal((String)"Dm"));
        this.targetPlayerInputBox.setHint((Component)Component.literal((String)"Dm"));
        this.targetPlayerInputBox.setMaxLength(16);
        this.targetPlayerInputBox.setValue(targetPlayerCache.getOrDefault(this.currentSlot, ""));
        this.addRenderableWidget((GuiEventListener)this.targetPlayerInputBox);
        this.setInitialFocus((GuiEventListener)this.bottomInputBox);
    }

    private void handlePlayerTabCompletion() {
        int cursorPos;
        String currentText;
        int atIndex;
        if (this.showSuggestionsList && !this.currentMatchedPlayers.isEmpty() && (atIndex = (currentText = this.bottomInputBox.getValue()).lastIndexOf(64, (cursorPos = this.bottomInputBox.getCursorPosition()) - 1)) != -1) {
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void injectCommandToSelectedArea() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            BlockPos pA = FaycoreSettingsScreen.posA;
            BlockPos pB = FaycoreSettingsScreen.posB;
            if (pA != null && pB != null) {
                String commandTarget;
                String rawPrefix = this.upperInputBox.getValue();
                String rawMessage = FayCoreMacroEngine.parseDynamicVariables(this.bottomInputBox.getValue());
                String inputTargetPlayer = this.targetPlayerInputBox.getValue().trim();
                String string = commandTarget = inputTargetPlayer.isEmpty() ? "@a" : inputTargetPlayer;
                if (!rawMessage.trim().isEmpty()) {
                    String finalTellrawCommand;
                    String targetPlayer = null;
                    Pattern mentionPattern = Pattern.compile("@(\\S+)");
                    Matcher matcher = mentionPattern.matcher(rawMessage);
                    String formattedPrefix = rawPrefix.replace('&', '\u00a7');
                    boolean isOnline = false;
                    String finalTellrawCommand2 = "";
                    if (matcher.find()) {
                        targetPlayer = matcher.group(1);
                        if (mc.getConnection() != null) {
                            for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
                                if (info.getProfile() == null) continue;
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
                                if (!checkName.equalsIgnoreCase(targetPlayer)) continue;
                                isOnline = true;
                            }
                        }
                        if (isOnline) {
                            String part2;
                            String[] segments = rawMessage.split("@" + targetPlayer, 2);
                            String part1 = segments.length > 0 ? Arrays.asList(segments).get(0).replace('&', '\u00a7') : "";
                            String string2 = part2 = segments.length > 1 ? Arrays.asList(segments).get(1).replace('&', '\u00a7') : "";
                            if (commandTarget.equals("@a")) {
                                finalTellrawCommand = String.format("tellraw @a [\"\",{\"text\":\"%s\"},{\"text\":\"%s\"},{\"text\":\"@\",\"color\":\"gold\"},{\"selector\":\"%s\",\"color\":\"gold\"},{\"text\":\"%s\"}]", formattedPrefix, part1, targetPlayer, part2);
                            } else {
                                finalTellrawCommand = String.format("tellraw %s [\"\",{\"text\":\"%s\"},{\"text\":\"%s\"},{\"text\":\"@\",\"color\":\"gold\"},{\"selector\":\"%s\",\"color\":\"gold\"},{\"text\":\"%s\"}]", commandTarget, formattedPrefix, part1, targetPlayer, part2);
                                FayCoreMacroEngine.autoFindAndInjectVCommand(mc, String.format("tellraw %%player%% [\"\",{\"text\":\"\u00a7d[Fay] \u00a77(\u00a76You\u00a77 > \u00a7b%s\u00a77)\u00a7r %s%s%s\"}]", commandTarget, part1, targetPlayer, part2));
                            }
                        } else {
                            formattedMessage = rawMessage.replace('&', '\u00a7');
                            String colorizedMention = "\u00a76@" + targetPlayer + "\u00a7f";
                            String finalColorizedMessage = formattedMessage.replace("@" + targetPlayer, colorizedMention);
                            if (commandTarget.equals("@a")) {
                                finalTellrawCommand = String.format("tellraw %s [\"\",{\"text\":\"%s%s\"}]", commandTarget, formattedPrefix, finalColorizedMessage);
                            } else {
                                finalTellrawCommand = String.format("tellraw %s [\"\",{\"text\":\"\u00a7d[Fay] \u00a77(\u00a76%%player%%\u00a77 > \u00a7bYou\u00a77)\u00a7r %s\"}]", commandTarget, finalColorizedMessage);
                                FayCoreMacroEngine.autoFindAndInjectVCommand(mc, String.format("tellraw %%player%% [\"\",{\"text\":\"\u00a7d[Fay] \u00a77(\u00a76You\u00a77 > \u00a7b%s\u00a77)\u00a7r %s\"}]", commandTarget, formattedMessage));
                            }
                        }
                    } else {
                        formattedMessage = rawMessage.replace('&', '\u00a7');
                        if (commandTarget.equals("@a")) {
                            finalTellrawCommand = String.format("tellraw %s [\"\",{\"text\":\"%s%s\"}]", commandTarget, formattedPrefix, formattedMessage);
                        } else {
                            finalTellrawCommand = String.format("tellraw %s [\"\",{\"text\":\"\u00a7d[Fay] \u00a77(\u00a76%%player%%\u00a77 > \u00a7bYou\u00a77) %s\"}]", commandTarget, formattedMessage);
                            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, String.format("tellraw %%player%% [\"\",{\"text\":\"\u00a7d[Fay] \u00a77(\u00a76You\u00a77 > \u00a7b%s\u00a77) %s\"}]", commandTarget, formattedMessage));
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
                        ArrayList<BlockPos> emptyCommandBlocks = new ArrayList<BlockPos>();
                        for (int x = minX; x != endX; ++x) {
                            for (int y = minY; y != endY; ++y) {
                                for (int z = minZ; z != endZ; ++z) {
                                    CommandBlockEntity cb;
                                    BlockEntity var27;
                                    BlockPos targetPos = new BlockPos(x, y, z);
                                    if (!mc.level.getBlockState(targetPos).is((Object)Blocks.COMMAND_BLOCK) || !((var27 = mc.level.getBlockEntity(targetPos)) instanceof CommandBlockEntity) || !(cb = (CommandBlockEntity)var27).getCommandBlock().getCommand().trim().isEmpty()) continue;
                                    emptyCommandBlocks.add(targetPos);
                                }
                            }
                        }
                        int requiredBlocks = 2;
                        if (targetPlayer != null && isOnline && !commandTarget.equals("@a")) {
                            requiredBlocks = 3;
                        }
                        if (requiredBlocks > emptyCommandBlocks.size()) {
                            mc.player.sendSystemMessage((Component)Component.literal((String)("\u00a7c[FayCore] \u8b66\u544a\uff1a\u57fa\u5730\u9078\u5340\u7a7a\u683c\u4e0d\u8db3\uff01\u672c\u6b21\u96d9\u5f48\u5e55\u6392\u7a0b\u81f3\u5c11\u9700\u8981 " + requiredBlocks + " \u683c\u7a7a\u767d\u65b9\u584a\u3002")));
                            return;
                        }
                        try {
                            muteFeedbackActive = true;
                            if (targetPlayer != null && isOnline) {
                                BlockPos tellrawPos = (BlockPos)emptyCommandBlocks.get(0);
                                BlockPos soundPos = null;
                                if (!commandTarget.equals("@a")) {
                                    tellrawPos = (BlockPos)emptyCommandBlocks.get(1);
                                } else {
                                    tellrawPos = (BlockPos)emptyCommandBlocks.get(1);
                                    soundPos = (BlockPos)emptyCommandBlocks.get(2);
                                }
                                String finalSoundCommand = String.format("execute as %s at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1 1", targetPlayer);
                                ClientPacketListener currentConnection = Minecraft.getInstance().getConnection();
                                if (currentConnection != null) {
                                    String parsedSound;
                                    String parsedTellraw = FayCoreMacroEngine.parseDynamicVariables(finalTellrawCommand);
                                    currentConnection.send((Packet)new ServerboundSetCommandBlockPacket(tellrawPos, parsedTellraw, CommandBlockEntity.Mode.REDSTONE, false, false, false));
                                    if (commandTarget.equals("@a")) {
                                        parsedSound = FayCoreMacroEngine.parseDynamicVariables(finalSoundCommand);
                                        currentConnection.send((Packet)new ServerboundSetCommandBlockPacket(soundPos, parsedSound, CommandBlockEntity.Mode.REDSTONE, false, false, false));
                                    }
                                    currentConnection.send((Packet)new ServerboundSetCommandBlockPacket(tellrawPos, parsedTellraw, CommandBlockEntity.Mode.REDSTONE, false, false, true));
                                    if (commandTarget.equals("@a")) {
                                        parsedSound = FayCoreMacroEngine.parseDynamicVariables(finalSoundCommand);
                                        currentConnection.send((Packet)new ServerboundSetCommandBlockPacket(soundPos, parsedSound, CommandBlockEntity.Mode.REDSTONE, false, false, true));
                                    }
                                }
                                int usedSlots = !commandTarget.equals("@a") ? 3 : 2;
                                int remainingSlots = emptyCommandBlocks.size() - usedSlots;
                                if (remainingSlots <= 0 && FaycoreSettingsScreen.fillStage == 2 && FaycoreSettingsScreen.posA != null && FaycoreSettingsScreen.posB != null) {
                                    String resetFillCmd = String.format("fill %d %d %d %d %d %d minecraft:command_block[facing=up]{Command:'',CustomName:%s}", minX, minY, minZ, maxX, maxY, maxZ, FayCoreMacroEngine.CustomName);
                                    mc.player.connection.send((Packet)new ServerboundChatCommandPacket(resetFillCmd));
                                    mc.player.sendSystemMessage((Component)Component.literal((String)"\u00a7e\u00a7l[FayCore] \u6838\u5fc3\u9078\u5340\u5df2\u6eff\uff01\u5df2\u81ea\u52d5\u91cd\u7f6e\u3002"));
                                }
                            } else {
                                BlockPos nextAvailablePos = (BlockPos)emptyCommandBlocks.get(0);
                                String parsedTellraw = FayCoreMacroEngine.parseDynamicVariables(finalTellrawCommand);
                                mc.player.connection.send((Packet)new ServerboundSetCommandBlockPacket(nextAvailablePos, parsedTellraw, CommandBlockEntity.Mode.REDSTONE, false, false, false));
                                mc.player.connection.send((Packet)new ServerboundSetCommandBlockPacket(nextAvailablePos, parsedTellraw, CommandBlockEntity.Mode.REDSTONE, false, false, true));
                                if (FaycoreSettingsScreen.fillStage == 2 && FaycoreSettingsScreen.posA != null && FaycoreSettingsScreen.posB != null && emptyCommandBlocks.size() <= 1) {
                                    String resetFillCmd = String.format("fill %d %d %d %d %d %d minecraft:command_block[facing=up]{Command:'',CustomName:%s}", minX, minY, minZ, maxX, maxY, maxZ, FayCoreMacroEngine.CustomName);
                                    mc.player.connection.send((Packet)new ServerboundChatCommandPacket(resetFillCmd));
                                    mc.player.sendSystemMessage((Component)Component.literal((String)"\u00a7e\u00a7l[FayCore] \u6838\u5fc3\u9078\u5340\u5df2\u6eff\uff01\u5df2\u81ea\u52d5\u91cd\u7f6e\u3002"));
                                }
                            }
                        }
                        catch (Exception exception) {
                        }
                        finally {
                            new Thread(() -> {
                                try {
                                    Thread.sleep(500L);
                                }
                                catch (Exception exception) {
                                    // empty catch block
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
                mc.player.sendSystemMessage((Component)Component.literal((String)"\u00a7c[FayCore] \u932f\u8aa4\uff1a\u672a\u8a2d\u5b9a A\u3001B \u5340\u57df\u7bc4\u570d\uff01"));
            }
        }
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        if (this.upperInputBox != null && this.bottomInputBox != null) {
            guiGraphics.text(this.font, (Component)Component.literal((String)"\u00a77[Prefix]"), this.upperInputBox.getX(), this.upperInputBox.getY() - 11, 0xFFFFFF, false);
            guiGraphics.text(this.font, (Component)Component.literal((String)"\u00a7d[DM]"), this.targetPlayerInputBox.getX(), this.targetPlayerInputBox.getY() - 11, 0xFFFFFF, false);
            guiGraphics.text(this.font, (Component)Component.literal((String)"\u00a77[\u52d5\u614b\u8a0a\u606f\u6b04\u4f4d (\u6253 @ \u5f48\u51fa\u9078\u55ae)]"), this.bottomInputBox.getX(), this.bottomInputBox.getY() - 11, 0xFFFFFF, false);
        }
        if (this.showSuggestionsList && !this.currentMatchedPlayers.isEmpty()) {
            int renderX = this.bottomInputBox.getX() + 2;
            int renderY = this.bottomInputBox.getY() - 14;
            int maxShow = Math.min(this.currentMatchedPlayers.size(), 5);
            int boxHeight = maxShow * 11 + 4;
            int finalY = renderY - boxHeight;
            guiGraphics.fill(renderX - 2, finalY, renderX + 160, renderY, -872415232);
            for (int i = 0; i != maxShow; ++i) {
                String pName = this.currentMatchedPlayers.get(i);
                int textY = finalY + 3 + i * 11;
                if (i == this.tabCycleIndex) {
                    guiGraphics.text(this.font, (Component)Component.literal((String)("\u00a76> " + pName + " \u00a7f\\uE001")), renderX, textY, 16753920, false);
                    continue;
                }
                guiGraphics.text(this.font, (Component)Component.literal((String)("  " + pName + " \u00a77\\uE001")), renderX, textY, 0xAAAAAA, false);
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
        }
        if (key != 257 && key != 335) {
            if (key == 256) {
                if (this.upperInputBox != null && this.bottomInputBox != null) {
                    prefixCache.put(this.currentSlot, this.upperInputBox.getValue());
                    messageCache.put(this.currentSlot, this.bottomInputBox.getValue());
                    this.saveSlotsToDisk();
                }
                this.onClose();
                return true;
            }
            return super.keyPressed(event);
        }
        if (this.upperInputBox != null && this.bottomInputBox != null) {
            prefixCache.put(this.currentSlot, this.upperInputBox.getValue());
            messageCache.put(this.currentSlot, this.bottomInputBox.getValue());
            this.injectCommandToSelectedArea();
        }
        return true;
    }

    public boolean isPauseScreen() {
        return false;
    }

    static {
        targetPlayerCache = new HashMap<Integer, String>();
        CONFIG_FILE = new File(Minecraft.getInstance().gameDirectory, "config/faycore_slots_macros.txt");
    }
}

