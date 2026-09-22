/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.CommandBlockEntity
 *  net.minecraft.world.level.block.entity.CommandBlockEntity$Mode
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  org.lwjgl.glfw.GLFW
 */
package net.mcreator.faycore.client.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.mcreator.faycore.client.gui.FaycoreSettingsScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

public class FayCoreMacroEngine {
    private static final boolean[] coreFull = new boolean[10];
    private static final int[] nextSearchIndex = new int[10];
    public static final List<List<GroupLine>> dynamicGroupStorage = new ArrayList<List<GroupLine>>();
    public static final Map<String, Double> customVariables = new HashMap<String, Double>();
    public static int totalGroupsCount = 1;
    public static int selectedGroupIndex = 0;
    public static final List<Integer> groupModes = new ArrayList<Integer>();
    public static final List<Boolean> groupEnables = new ArrayList<Boolean>();
    public static final List<Integer> matrixRunningIndices = new ArrayList<Integer>();
    public static final List<Integer> matrixTickCooldowns = new ArrayList<Integer>();
    public static final List<Boolean> matrixPulseStates = new ArrayList<Boolean>();
    public static boolean isMacroRunning = false;
    public static boolean isSingleTriggerLocked = false;
    public static final List<List<BlockPos>> matrixOwnedBlocksCache = new ArrayList<List<BlockPos>>();
    private static final File CONFIG_FILE;
    public static String CustomName;
    public static String CustomLore;
    public static final Map<Integer, Integer> matrixReuseIndex;
    private static final ExecutorService FAYCORE_ASYNC_POOL;

    public static GroupLine getGroupLinePointer(int groupIdx, int lineIdx) {
        return dynamicGroupStorage.get(groupIdx).get(lineIdx);
    }

    public static void saveGroupsToDisk() {
        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }
            try (PrintWriter writer = new PrintWriter(new FileWriter(CONFIG_FILE));){
                writer.println("totalGroupsCount=" + totalGroupsCount);
                writer.println("selectedGroupIndex=" + selectedGroupIndex);
                for (int g = 0; g != 10; ++g) {
                    writer.println("g_" + g + "_mode=" + String.valueOf(groupModes.get(g)));
                    writer.println("g_" + g + "_enable=" + String.valueOf(groupEnables.get(g)));
                    List<GroupLine> currentList = dynamicGroupStorage.get(g);
                    writer.println("g_" + g + "_size=" + currentList.size());
                    for (int i = 0; i != currentList.size(); ++i) {
                        writer.println("g_" + g + "_line_" + i + "_cmd=" + currentList.get((int)i).command);
                        writer.println("g_" + g + "_line_" + i + "_del=" + currentList.get((int)i).tickDelay);
                    }
                }
                writer.flush();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static void loadGroupsFromDisk() {
        if (CONFIG_FILE.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE));){
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    int eq = trimmed.indexOf(61);
                    if (eq == -1) continue;
                    String val = trimmed.substring(eq + 1).trim();
                    if (trimmed.startsWith("totalGroupsCount=")) {
                        totalGroupsCount = Integer.parseInt(val);
                    }
                    if (trimmed.startsWith("selectedGroupIndex=")) {
                        selectedGroupIndex = Integer.parseInt(val);
                    }
                    for (int g = 0; g != 10; ++g) {
                        if (trimmed.startsWith("g_" + g + "_mode=")) {
                            groupModes.set(g, Integer.parseInt(val));
                        }
                        if (trimmed.startsWith("g_" + g + "_enable=")) {
                            groupEnables.set(g, Boolean.parseBoolean(val));
                        }
                        if (trimmed.startsWith("g_" + g + "_size=")) {
                            int size = Integer.parseInt(val);
                            dynamicGroupStorage.get(g).clear();
                            for (int k = 0; k != size; ++k) {
                                dynamicGroupStorage.get(g).add(new GroupLine("", 0));
                            }
                        }
                        int listSize = dynamicGroupStorage.get(g).size();
                        for (int i = 0; i != listSize; ++i) {
                            if (trimmed.startsWith("g_" + g + "_line_" + i + "_cmd=")) {
                                FayCoreMacroEngine.dynamicGroupStorage.get((int)g).get((int)i).command = val;
                            }
                            if (!trimmed.startsWith("g_" + g + "_line_" + i + "_del=")) continue;
                            FayCoreMacroEngine.dynamicGroupStorage.get((int)g).get((int)i).tickDelay = Integer.parseInt(val);
                        }
                    }
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public static BlockPos findNextEmptyBlock(Minecraft mc, int currentGroupIdx, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        if (coreFull[currentGroupIdx]) {
            return null;
        }
        int totalX = maxX - minX + 1;
        int totalY = maxY - minY + 1;
        int totalZ = maxZ - minZ + 1;
        int totalVolume = totalX * totalY * totalZ;
        List<BlockPos> myOwnedList = matrixOwnedBlocksCache.get(currentGroupIdx);
        if (myOwnedList.size() >= totalVolume) {
            FayCoreMacroEngine.coreFull[currentGroupIdx] = true;
            return null;
        }
        int start = nextSearchIndex[currentGroupIdx];
        for (int offset = 0; offset < totalVolume; ++offset) {
            CommandBlockEntity cb;
            String cmd;
            BlockEntity var19;
            int index = (start + offset) % totalVolume;
            int curX = minX + index % totalX;
            int curY = minY + index / totalX % totalY;
            int curZ = minZ + index / (totalX * totalY);
            BlockPos pos = new BlockPos(curX, curY, curZ);
            if (myOwnedList.contains(pos) || !mc.level.getBlockState(pos).is((Object)Blocks.COMMAND_BLOCK) || !((var19 = mc.level.getBlockEntity(pos)) instanceof CommandBlockEntity) || !(cmd = (cb = (CommandBlockEntity)var19).getCommandBlock().getCommand().trim()).isEmpty() && !cmd.equals("")) continue;
            boolean isClaimedByOthers = false;
            for (int otherG = 0; otherG != 10; ++otherG) {
                if (otherG >= matrixOwnedBlocksCache.size() || otherG == currentGroupIdx || !matrixOwnedBlocksCache.get(otherG).contains(pos)) continue;
                isClaimedByOthers = true;
            }
            if (isClaimedByOthers) continue;
            myOwnedList.add(pos);
            FayCoreMacroEngine.nextSearchIndex[currentGroupIdx] = (index + 1) % totalVolume;
            FayCoreMacroEngine.coreFull[currentGroupIdx] = false;
            return pos;
        }
        FayCoreMacroEngine.coreFull[currentGroupIdx] = true;
        return null;
    }

    public static void executeMacroEngineCore() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.player.connection == null) {
            return;
        }
        BlockPos pA = FaycoreSettingsScreen.posA;
        BlockPos pB = FaycoreSettingsScreen.posB;
        if (pA == null || pB == null) {
            return;
        }
        int minX = Math.min(pA.getX(), pB.getX());
        int maxX = Math.max(pA.getX(), pB.getX());
        int minY = Math.min(pA.getY(), pB.getY());
        int maxY = Math.max(pA.getY(), pB.getY());
        int minZ = Math.min(pA.getZ(), pB.getZ());
        int maxZ = Math.max(pA.getZ(), pB.getZ());
        for (int g = 0; g != 10; ++g) {
            BlockPos targetPos;
            List<GroupLine> activeList;
            int maxAllowedLines;
            if (g >= dynamicGroupStorage.size() || g >= groupEnables.size() || g >= groupModes.size() || g >= matrixRunningIndices.size() || g >= matrixTickCooldowns.size() || g >= matrixPulseStates.size()) continue;
            boolean isCurEnabled = groupEnables.get(g);
            int currentMode = groupModes.get(g);
            if (!isCurEnabled) {
                matrixRunningIndices.set(g, 0);
                matrixTickCooldowns.set(g, 0);
                matrixPulseStates.set(g, false);
                continue;
            }
            if (coreFull[g] || currentMode != 2 && (g != selectedGroupIndex || !isMacroRunning) || (maxAllowedLines = (activeList = dynamicGroupStorage.get(g)).size()) == 0) continue;
            int runIdx = matrixRunningIndices.get(g);
            int cooldown = matrixTickCooldowns.get(g);
            if (currentMode == 2) {
                targetPos = FayCoreMacroEngine.findNextEmptyBlock(mc, g, minX, maxX, minY, maxY, minZ, maxZ);
                if (targetPos == null) {
                    FayCoreMacroEngine.coreFull[g] = true;
                    int finalG = g;
                    FAYCORE_ASYNC_POOL.execute(() -> {
                        try {
                            if (matrixOwnedBlocksCache != null && matrixOwnedBlocksCache.get(finalG) != null) {
                                matrixOwnedBlocksCache.get(finalG).clear();
                            }
                            mc.execute(() -> {
                                if (mc.player != null && mc.player.connection != null) {
                                    FayCoreMacroEngine.nextSearchIndex[finalG] = 0;
                                    matrixRunningIndices.set(finalG, 0);
                                    matrixTickCooldowns.set(finalG, 0);
                                    matrixPulseStates.set(finalG, false);
                                    selectedGroupIndex = 0;
                                    FayCoreMacroEngine.coreFull[finalG] = false;
                                }
                            });
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    });
                    continue;
                }
                if (runIdx == maxAllowedLines) continue;
                boolean pulse = matrixPulseStates.get(g) == false;
                matrixPulseStates.set(g, pulse);
                String finalCmd = activeList.get((int)runIdx).command.replace('&', '\u00a7');
                finalCmd = FayCoreMacroEngine.parseDynamicVariables(finalCmd);
                mc.player.connection.send((Packet)new ServerboundSetCommandBlockPacket(targetPos, finalCmd, CommandBlockEntity.Mode.REDSTONE, false, false, pulse));
                matrixRunningIndices.set(g, (runIdx + 1) % maxAllowedLines);
                continue;
            }
            if (runIdx == maxAllowedLines) {
                matrixRunningIndices.set(g, 0);
                if (currentMode == 1) {
                    boolean isStillHolding;
                    long winHandle = GLFW.glfwGetCurrentContext();
                    boolean bl = isStillHolding = GLFW.glfwGetKey((long)winHandle, (int)72) == 1;
                    if (isStillHolding) continue;
                    isMacroRunning = false;
                    continue;
                }
                isMacroRunning = false;
                continue;
            }
            if (cooldown != 0) {
                matrixTickCooldowns.set(g, cooldown - 1);
                continue;
            }
            targetPos = FayCoreMacroEngine.findNextEmptyBlock(mc, g, minX, maxX, minY, maxY, minZ, maxZ);
            if (targetPos == null) continue;
            GroupLine currentLine = activeList.get(runIdx);
            String finalCmd = currentLine.command.replace('&', '\u00a7');
            finalCmd = FayCoreMacroEngine.parseDynamicVariables(finalCmd);
            mc.player.connection.send((Packet)new ServerboundSetCommandBlockPacket(targetPos, finalCmd, CommandBlockEntity.Mode.REDSTONE, false, false, false));
            mc.player.connection.send((Packet)new ServerboundSetCommandBlockPacket(targetPos, finalCmd, CommandBlockEntity.Mode.REDSTONE, false, false, true));
            matrixTickCooldowns.set(g, currentLine.tickDelay);
            matrixRunningIndices.set(g, runIdx + 1);
        }
    }

    public static String parseDynamicVariables(String originalCommand) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            int px = (int)Math.floor(mc.player.getX());
            int py = (int)Math.floor(mc.player.getY());
            int pz = (int)Math.floor(mc.player.getZ());
            String parsed = originalCommand.replace("%pos-x%", String.valueOf(px));
            parsed = parsed.replace("%pos-y%", String.valueOf(py));
            parsed = parsed.replace("%pos-z%", String.valueOf(pz));
            parsed = parsed.replace("%player%", mc.player.getScoreboardName());
            Direction dir = mc.player.getDirection();
            String directionName = dir.getName().toLowerCase();
            parsed = parsed.replace("%direction%", directionName);
            HitResult manualHit = mc.player.pick(300.0, 1.0f, false);
            if (manualHit != null && manualHit.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult)manualHit;
                BlockPos lookPos = blockHit.getBlockPos();
                parsed = parsed.replace("%look-x%", String.valueOf(lookPos.getX()));
                parsed = parsed.replace("%look-y%", String.valueOf(lookPos.getY()));
                parsed = parsed.replace("%look-z%", String.valueOf(lookPos.getZ()));
            }
            return parsed;
        }
        return originalCommand;
    }

    private static double evaluateExpression(String expr) {
        String cleanExpr;
        String readyExpr;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return 0.0;
        }
        int px = (int)Math.floor(mc.player.getX());
        int py = (int)Math.floor(mc.player.getY());
        int pz = (int)Math.floor(mc.player.getZ());
        int lx = px;
        int ly = py;
        int lz = pz;
        HitResult manualHit = mc.player.pick(500.0, 1.0f, false);
        if (manualHit != null && manualHit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult)manualHit;
            BlockPos lookPos = blockHit.getBlockPos();
            lx = lookPos.getX();
            ly = lookPos.getY();
            lz = lookPos.getZ();
        }
        if ((readyExpr = expr.trim()).contains("ox") && !customVariables.containsKey("ox")) {
            customVariables.put("ox", Double.valueOf(lx));
        }
        if (readyExpr.contains("oy") && !customVariables.containsKey("oy")) {
            customVariables.put("oy", Double.valueOf(ly));
        }
        if (readyExpr.contains("oz") && !customVariables.containsKey("oz")) {
            customVariables.put("oz", Double.valueOf(lz));
        }
        if (readyExpr.contains("px") && !customVariables.containsKey("px")) {
            customVariables.put("px", Double.valueOf(px));
        }
        if (readyExpr.contains("py") && !customVariables.containsKey("py")) {
            customVariables.put("py", Double.valueOf(py));
        }
        if (readyExpr.contains("pz") && !customVariables.containsKey("pz")) {
            customVariables.put("pz", Double.valueOf(pz));
        }
        for (Map.Entry entry : customVariables.entrySet()) {
            readyExpr = readyExpr.replace((CharSequence)entry.getKey(), String.valueOf(entry.getValue()));
        }
        final String string = cleanExpr = readyExpr.replaceAll("\\s+", "");
        try {
            return new Object(){
                int pos = -1;
                int ch;

                void nextChar() {
                    this.ch = ++this.pos < string.length() ? (int)string.charAt(this.pos) : -1;
                }

                boolean eat(int charToEat) {
                    while (this.ch == 32) {
                        this.nextChar();
                    }
                    if (this.ch == charToEat) {
                        this.nextChar();
                        return true;
                    }
                    return false;
                }

                double parse() {
                    this.nextChar();
                    double x = this.parseExpression();
                    return this.pos < string.length() ? 0.0 : x;
                }

                double parseExpression() {
                    double x = this.parseTerm();
                    while (true) {
                        if (!this.eat(43)) {
                            if (!this.eat(45)) {
                                return x;
                            }
                            x -= this.parseTerm();
                            continue;
                        }
                        x += this.parseTerm();
                    }
                }

                double parseTerm() {
                    double x = this.parseFactor();
                    while (true) {
                        if (!this.eat(42)) {
                            if (!this.eat(47)) {
                                return x;
                            }
                            double d = this.parseFactor();
                            x = d != 0.0 ? x / d : 0.0;
                            continue;
                        }
                        x *= this.parseFactor();
                    }
                }

                double parseFactor() {
                    if (this.eat(43)) {
                        return this.parseFactor();
                    }
                    if (this.eat(45)) {
                        return -this.parseFactor();
                    }
                    int startPos = this.pos;
                    if ((this.ch < 48 || this.ch > 57) && this.ch != 46) {
                        return 0.0;
                    }
                    while (this.ch >= 48 && this.ch <= 57 || this.ch == 46) {
                        this.nextChar();
                    }
                    double x = Double.parseDouble(string.substring(startPos, this.pos));
                    return x;
                }
            }.parse();
        }
        catch (Exception var13) {
            return 0.0;
        }
    }

    public static void executeVPressCommand(Object mcObj, String rawCommand) {
        int maxZ;
        int minZ;
        int maxY;
        int minY;
        int maxX;
        if (!(mcObj instanceof Minecraft)) {
            return;
        }
        Minecraft mc = (Minecraft)mcObj;
        if (mc.player == null || mc.level == null || mc.player.connection == null) {
            return;
        }
        BlockPos pA = FaycoreSettingsScreen.posA;
        BlockPos pB = FaycoreSettingsScreen.posB;
        if (pA == null || pB == null) {
            mc.gui.setOverlayMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a7cError: not yet set posA and posB!").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}), false);
            if (mc.level != null) {
                mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), (SoundEvent)SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 0.8f, 0.5f, false);
            }
            return;
        }
        int minX = Math.min(pA.getX(), pB.getX());
        BlockPos targetBlock = FayCoreMacroEngine.findNextEmptyBlock(mc, 9, minX, maxX = Math.max(pA.getX(), pB.getX()), minY = Math.min(pA.getY(), pB.getY()), maxY = Math.max(pA.getY(), pB.getY()), minZ = Math.min(pA.getZ(), pB.getZ()), maxZ = Math.max(pA.getZ(), pB.getZ()));
        if (targetBlock == null && FaycoreSettingsScreen.posA != null && FaycoreSettingsScreen.posB != null) {
            mc.gui.setOverlayMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a7cError: No empty cmd block!").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}), false);
            FaycoreSettingsScreen.FillCoreCmd();
            FaycoreSettingsScreen.FillRepeatingCore();
            matrixOwnedBlocksCache.get(9).clear();
            FayCoreMacroEngine.nextSearchIndex[9] = 0;
            FayCoreMacroEngine.coreFull[9] = false;
            if (mc.level != null) {
                mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), (SoundEvent)SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 0.8f, 0.5f, false);
            }
            return;
        }
        String finalCmd = rawCommand.replace('&', '\u00a7');
        finalCmd = FayCoreMacroEngine.parseDynamicVariables(finalCmd);
        mc.player.connection.send((Packet)new ServerboundSetCommandBlockPacket(targetBlock, finalCmd, CommandBlockEntity.Mode.REDSTONE, false, false, false));
        mc.player.connection.send((Packet)new ServerboundSetCommandBlockPacket(targetBlock, finalCmd, CommandBlockEntity.Mode.REDSTONE, false, false, true));
    }

    public static void autoFindAndInjectVCommand(Object mcObj, String rawCommand) {
        int maxZ;
        int minZ;
        int maxY;
        int minY;
        int maxX;
        if (!(mcObj instanceof Minecraft)) {
            return;
        }
        Minecraft mc = (Minecraft)mcObj;
        if (mc.player == null || mc.level == null || mc.player.connection == null) {
            return;
        }
        if (coreFull[9]) {
            return;
        }
        BlockPos pA = FaycoreSettingsScreen.posA;
        BlockPos pB = FaycoreSettingsScreen.posB;
        if (pA == null || pB == null) {
            return;
        }
        int minX = Math.min(pA.getX(), pB.getX());
        BlockPos targetBlock = FayCoreMacroEngine.findNextEmptyBlock(mc, 9, minX, maxX = Math.max(pA.getX(), pB.getX()), minY = Math.min(pA.getY(), pB.getY()), maxY = Math.max(pA.getY(), pB.getY()), minZ = Math.min(pA.getZ(), pB.getZ()), maxZ = Math.max(pA.getZ(), pB.getZ()));
        if (targetBlock == null) {
            FayCoreMacroEngine.coreFull[9] = true;
            FAYCORE_ASYNC_POOL.execute(() -> {
                try {
                    if (matrixOwnedBlocksCache != null && matrixOwnedBlocksCache.get(9) != null) {
                        matrixOwnedBlocksCache.get(9).clear();
                    }
                    FayCoreMacroEngine.nextSearchIndex[9] = 0;
                    mc.execute(() -> {
                        if (mc.player != null && mc.player.connection != null) {
                            FayCoreMacroEngine.coreFull[9] = false;
                        }
                    });
                }
                catch (Exception exception) {
                    // empty catch block
                }
            });
            return;
        }
        String finalCmd = rawCommand.replace('&', '\u00a7');
        finalCmd = FayCoreMacroEngine.parseDynamicVariables(finalCmd);
        mc.player.connection.send((Packet)new ServerboundSetCommandBlockPacket(targetBlock, finalCmd, CommandBlockEntity.Mode.REDSTONE, false, false, false));
        mc.player.connection.send((Packet)new ServerboundSetCommandBlockPacket(targetBlock, finalCmd, CommandBlockEntity.Mode.REDSTONE, false, false, true));
    }

    public static void executeVReleaseCommand(Object mcObj, String rawCommand) {
        int maxZ;
        int minZ;
        int maxY;
        int minY;
        int maxX;
        if (!(mcObj instanceof Minecraft)) {
            return;
        }
        Minecraft mc = (Minecraft)mcObj;
        if (mc.player == null || mc.level == null || mc.player.connection == null) {
            return;
        }
        BlockPos pA = FaycoreSettingsScreen.posA;
        BlockPos pB = FaycoreSettingsScreen.posB;
        if (pA == null || pB == null) {
            return;
        }
        int minX = Math.min(pA.getX(), pB.getX());
        BlockPos targetBlock = FayCoreMacroEngine.findNextEmptyBlock(mc, 9, minX, maxX = Math.max(pA.getX(), pB.getX()), minY = Math.min(pA.getY(), pB.getY()), maxY = Math.max(pA.getY(), pB.getY()), minZ = Math.min(pA.getZ(), pB.getZ()), maxZ = Math.max(pA.getZ(), pB.getZ()));
        if (targetBlock == null) {
            return;
        }
        String finalCmd = rawCommand.replace('&', '\u00a7');
        finalCmd = FayCoreMacroEngine.parseDynamicVariables(finalCmd);
        mc.player.connection.send((Packet)new ServerboundSetCommandBlockPacket(targetBlock, finalCmd, CommandBlockEntity.Mode.REDSTONE, false, false, false));
        mc.player.connection.send((Packet)new ServerboundSetCommandBlockPacket(targetBlock, finalCmd, CommandBlockEntity.Mode.REDSTONE, false, false, true));
    }

    static {
        CustomName = "{\"text\":\"\ua730\u1d00\u028f\u1d04\u1d0f\u0280\u1d07\",\"color\":\"#B0FF95\",italic:false}";
        CustomLore = "{\"minecraft:lore\": [{text:\"Made by FayeCruz\",color:gray,italic:false}]}";
        matrixReuseIndex = new HashMap<Integer, Integer>();
        FAYCORE_ASYNC_POOL = Executors.newSingleThreadExecutor();
        CONFIG_FILE = new File(Minecraft.getInstance().gameDirectory, "config/faycore_unlimited_groups.txt");
        for (int g = 0; g != 10; ++g) {
            ArrayList<GroupLine> singleGroup = new ArrayList<GroupLine>();
            singleGroup.add(new GroupLine("say \u6307\u4ee4 1", 5));
            singleGroup.add(new GroupLine("say \u6307\u4ee4 2", 5));
            singleGroup.add(new GroupLine("say \u6307\u4ee4 3", 5));
            dynamicGroupStorage.add(singleGroup);
            groupModes.add(0);
            groupEnables.add(true);
            matrixRunningIndices.add(0);
            matrixTickCooldowns.add(0);
            matrixPulseStates.add(false);
            matrixOwnedBlocksCache.add(new ArrayList());
        }
        FayCoreMacroEngine.loadGroupsFromDisk();
    }

    public static class GroupLine {
        public String command = "";
        public int tickDelay = 0;

        public GroupLine(String c, int d) {
            this.command = c;
            this.tickDelay = d;
        }
    }
}

