/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.reflect.TypeToken
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.particles.SimpleParticleType
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundChatCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.CommandBlockEntity$Mode
 */
package net.mcreator.faycore.client.gui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.mcreator.faycore.client.gui.FayCoreFastRun;
import net.mcreator.faycore.client.gui.FayCoreMacroEngine;
import net.mcreator.faycore.init.FaycoreModScreens;
import net.mcreator.faycore.world.inventory.FaycoreSettingsMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CommandBlockEntity;

public class FaycoreSettingsScreen
extends AbstractContainerScreen<FaycoreSettingsMenu>
implements FaycoreModScreens.FabricScreenAccessor {
    private final Level world;
    private final int x;
    private final int y;
    private final int z;
    private final Player entity;
    private boolean menuStateUpdateActive = false;
    private Button button_set_core_position;
    private Button button_fast_fill;
    public static int fillStage = 0;
    public static BlockPos posA = null;
    public static BlockPos posB = null;
    public static BlockPos RepeatingCorePosA = null;
    public static BlockPos RepeatingCorePosB = null;
    public static final double STEP_SIZE = 0.5;
    private static final File CONFIG_FILE;
    private static boolean hasOverlayShownThisStage;
    public static String CoreGlass;
    public static int EmptyCoreSize;
    public static int FRC_down;
    private static int particleTick;
    private static final Gson FAYCORE_INTERNAL_GSON;

    public FaycoreSettingsScreen(FaycoreSettingsMenu container, Inventory inventory, Component text) {
        super((AbstractContainerMenu)container, inventory, text, 176, 166);
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
    }

    @Override
    public void updateMenuState(int e, String n, Object s) {
        this.menuStateUpdateActive = true;
        this.menuStateUpdateActive = false;
    }

    public void extractRenderState(GuiGraphicsExtractor g, int mX, int mY, float pT) {
        super.extractRenderState(g, mX, mY, pT);
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse((String)"faycore:textures/screens/faycore_settings.png"), this.leftPos, this.topPos, 0.0f, 0.0f, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }

    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) {
            this.minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(event);
    }

    protected void extractLabels(GuiGraphicsExtractor g, int mX, int mY) {
        g.text(this.font, (Component)Component.translatable((String)"gui.faycore.faycore_settings.label_faycore_setting"), 48, 8, -26164, false);
    }

    public void init() {
        super.init();
        if (posA == null || posB == null) {
            this.loadCoordinatesFromLocal();
        }
        String displayLabel = fillStage == 2 ? "\u00a7cDelete Core" : (fillStage == 1 ? "Save and set \u00a77B pos" : "Save and set \u00a77A pos");
        this.button_set_core_position = Button.builder((Component)Component.literal((String)displayLabel), btn -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                if (fillStage == 0) {
                    int ax = this.parseInputField("input_ax", mc.player.blockPosition().getX());
                    int ay = this.parseInputField("input_ay", mc.player.blockPosition().getY());
                    int az = this.parseInputField("input_az", mc.player.blockPosition().getZ());
                    posA = new BlockPos(ax, ay, az);
                    fillStage = 1;
                    this.saveCoordinatesToLocal();
                    mc.player.sendSystemMessage((Component)Component.literal((String)("\u00a79[FayCore] \u00a7lA \u00a77Pos set: \u00a7e" + ax + ", " + ay + ", " + az)));
                    this.minecraft.setScreen((Screen)null);
                } else if (fillStage == 1) {
                    int sizeZ;
                    int sizeY;
                    int bx = this.parseInputField("input_bx", mc.player.blockPosition().getX());
                    int by = this.parseInputField("input_by", mc.player.blockPosition().getY());
                    int bz = this.parseInputField("input_bz", mc.player.blockPosition().getZ());
                    int sizeX = Math.abs(posA.getX() - bx) + 1;
                    if (sizeX * (sizeY = Math.abs(posA.getY() - by) + 1) * (sizeZ = Math.abs(posA.getZ() - bz) + 1) > 32768 || sizeX > 32 || sizeY > 32 || sizeZ > 32) {
                        mc.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a77The size too big, max is 32*32*32"));
                        return;
                    }
                    posB = new BlockPos(bx, by, bz);
                    fillStage = 2;
                    this.saveCoordinatesToLocal();
                    new Thread(() -> {
                        try {
                            if (mc.player != null && mc.player.connection != null) {
                                ArrayList<String> innerCommands = new ArrayList<String>();
                                innerCommands.add(String.format("forceload add %d %d %d %d", posA.getX(), posA.getZ(), posB.getX(), posB.getZ()));
                                innerCommands.add(String.format("fill %d %d %d %d %d %d minecraft:command_block[facing=up]{CustomName:" + FayCoreMacroEngine.CustomName + ",components: " + FayCoreMacroEngine.CustomLore + "}", posA.getX(), posA.getY(), posA.getZ(), posB.getX(), posB.getY(), posB.getZ()));
                                FayCoreFastRun.fireGhostPayloadQueue(new ArrayList<String>(innerCommands));
                                int minX = Math.min(posA.getX(), posB.getX());
                                int minY = Math.min(posA.getY(), posB.getY());
                                int minZ = Math.min(posA.getZ(), posB.getZ());
                                FaycoreSettingsScreen.FillRepeatingCore();
                                FaycoreSettingsScreen.autoFillCommandsViaPacket();
                            }
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }).start();
                    this.minecraft.setScreen((Screen)null);
                    FaycoreSettingsScreen.startParticleThread(mc);
                    int curMode = FayCoreMacroEngine.groupModes.get(FayCoreMacroEngine.selectedGroupIndex);
                    boolean curEnable = FayCoreMacroEngine.groupEnables.get(FayCoreMacroEngine.selectedGroupIndex);
                    if (curMode == 2 && curEnable) {
                        FayCoreMacroEngine.isMacroRunning = true;
                    }
                } else {
                    if (posA != null && posB != null && mc.player.connection != null) {
                        int minY = Math.min(posA.getY(), posB.getY());
                        int maxY = Math.max(posA.getY(), posB.getY());
                        String wipeCoreBlocks = String.format("fill %d %d %d %d %d %d minecraft:air", posA.getX(), maxY, posA.getZ(), posB.getX(), minY - (EmptyCoreSize + 7), posB.getZ());
                        String unloadChunks = String.format("forceload remove %d %d %d %d", posA.getX(), posA.getZ(), posB.getX(), posB.getZ());
                        ArrayList<String> innerCommands = new ArrayList<String>();
                        innerCommands.add(wipeCoreBlocks);
                        innerCommands.add(unloadChunks);
                        FayCoreFastRun.fireGhostPayloadQueue(new ArrayList<String>(innerCommands));
                        hasOverlayShownThisStage = false;
                    }
                    fillStage = 0;
                    posA = null;
                    posB = null;
                    this.minecraft.setScreen((Screen)null);
                }
            }
        }).bounds(this.leftPos + 32, this.topPos + 28, 110, 20).build();
        this.addRenderableWidget((GuiEventListener)this.button_set_core_position);
        this.button_fast_fill = Button.builder((Component)Component.literal((String)"\u00a77Place Core"), btn -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                this.loadCoordinatesFromLocal();
                if (posA != null && posB != null) {
                    fillStage = 2;
                    new Thread(() -> {
                        try {
                            if (mc.player != null && mc.player.connection != null) {
                                String forceLoadCmd = String.format("forceload add %d %d %d %d", posA.getX(), posA.getZ(), posB.getX(), posB.getZ());
                                String fillSolidCore = String.format("fill %d %d %d %d %d %d minecraft:command_block[facing=up]{CustomName:" + FayCoreMacroEngine.CustomName + ",components: " + FayCoreMacroEngine.CustomLore + "}", posA.getX(), posA.getY(), posA.getZ(), posB.getX(), posB.getY(), posB.getZ());
                                int minX = Math.min(posA.getX(), posB.getX());
                                int minY = Math.min(posA.getY(), posB.getY());
                                int minZ = Math.min(posA.getZ(), posB.getZ());
                                String lockSystemBlock = String.format("setblock %d %d %d minecraft:command_block[facing=up]{Command:\"#FAYCORE_SYSTEM_LOCK\",CustomName:" + FayCoreMacroEngine.CustomName + ",components: " + FayCoreMacroEngine.CustomLore + "} replace", minX, minY, minZ);
                                ArrayList<String> innerCommands = new ArrayList<String>();
                                innerCommands.add(forceLoadCmd);
                                innerCommands.add(fillSolidCore);
                                innerCommands.add(lockSystemBlock);
                                int Max_RCy = Math.max(posA.getY(), posB.getY());
                                int Min_RCy = Math.min(posA.getY(), posB.getY());
                                int A_RCx = posA.getX();
                                int A_RCz = posA.getZ();
                                int B_RCx = posB.getX();
                                int B_RCz = posB.getZ();
                                RepeatingCorePosA = new BlockPos(A_RCx, Min_RCy, A_RCz);
                                RepeatingCorePosB = new BlockPos(B_RCx, Min_RCy - FRC_down, B_RCz);
                                innerCommands.add(String.format("fill %d %d %d %d %d %d minecraft:repeating_command_block[facing=up]{CustomName:" + FayCoreMacroEngine.CustomName + ",components: " + FayCoreMacroEngine.CustomLore + "}", RepeatingCorePosA.getX(), RepeatingCorePosA.getY() - 1, RepeatingCorePosA.getZ(), RepeatingCorePosB.getX(), RepeatingCorePosB.getY(), RepeatingCorePosB.getZ()));
                                innerCommands.add(String.format("fill %d %d %d %d %d %d minecraft:%s outline", RepeatingCorePosA.getX(), Max_RCy, RepeatingCorePosA.getZ(), RepeatingCorePosB.getX(), RepeatingCorePosB.getY() - 1, RepeatingCorePosB.getZ(), CoreGlass));
                                innerCommands.add(String.format("fill %d %d %d %d %d %d minecraft:%s outline", RepeatingCorePosA.getX(), Max_RCy, RepeatingCorePosA.getZ(), RepeatingCorePosB.getX(), RepeatingCorePosB.getY() - (EmptyCoreSize + 2), RepeatingCorePosB.getZ(), CoreGlass));
                                List<String> myCommands = FaycoreSettingsScreen.getInternalSavedCommands();
                                FayCoreFastRun.fireGhostPayloadQueue(new ArrayList<String>(innerCommands));
                                FaycoreSettingsScreen.FillRepeatingCore();
                            }
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }).start();
                    this.minecraft.setScreen((Screen)null);
                    FaycoreSettingsScreen.startParticleThread(mc);
                    int curMode = FayCoreMacroEngine.groupModes.get(FayCoreMacroEngine.selectedGroupIndex);
                    boolean curEnable = FayCoreMacroEngine.groupEnables.get(FayCoreMacroEngine.selectedGroupIndex);
                    if (curMode == 2 && curEnable) {
                        FayCoreMacroEngine.isMacroRunning = true;
                    }
                } else {
                    mc.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a74ERROR\u00a78: \u00a77No history!"));
                }
            }
        }).bounds(this.leftPos + 32, this.topPos + 58, 110, 20).build();
        this.button_fast_fill.active = true;
        this.addRenderableWidget((GuiEventListener)this.button_fast_fill);
        new Thread(() -> {
            try {
                for (int tick = 0; tick != 20; ++tick) {
                    Thread.sleep(50L);
                    Minecraft mcInstance = Minecraft.getInstance();
                    if (mcInstance == null) continue;
                    mcInstance.execute(() -> {
                        try {
                            for (GuiEventListener listener : this.children()) {
                                if (!(listener instanceof EditBox)) continue;
                                EditBox box = (EditBox)listener;
                                String val = box.getValue();
                                if (val != null && (val.equals("b") || val.equalsIgnoreCase("b") || val.contains("b") || val.contains("B"))) {
                                    box.setValue("");
                                }
                                box.setFocused(false);
                            }
                            this.setFocused(null);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    });
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }).start();
    }

    private static void startParticleThread(Minecraft mc) {
        new Thread(() -> {
            try {
                SimpleParticleType pType = ParticleTypes.GLOW;
                while (fillStage == 2 && mc.player != null) {
                    if (mc.level != null && posA != null && posB != null) {
                        double minX = Math.min(posA.getX(), posB.getX());
                        double maxX = (double)Math.max(posA.getX(), posB.getX()) + 1.0;
                        double minY = Math.min(posA.getY(), posB.getY());
                        double maxY = (double)Math.max(posA.getY(), posB.getY()) + 1.0;
                        double minZ = Math.min(posA.getZ(), posB.getZ());
                        double maxZ = (double)Math.max(posA.getZ(), posB.getZ()) + 1.0;
                        int sX = (int)(maxX - minX);
                        int sY = (int)(maxY - minY);
                        int sZ = (int)(maxZ - minZ);
                        mc.execute(() -> {
                            if (mc.gui != null && fillStage == 2) {
                                if (fillStage != 2) {
                                    hasOverlayShownThisStage = false;
                                }
                                if (fillStage == 2 && !hasOverlayShownThisStage) {
                                    hasOverlayShownThisStage = true;
                                    mc.execute(() -> {
                                        if (mc.gui != null) {
                                            mc.gui.setOverlayMessage((Component)Component.literal((String)("\u00a7a\u00a7l[FayCore \u9078\u5340] \u00a7e\u00a7l" + sX + "x" + sY + "x" + sZ + " \u00a7f\u00a7l(\u96d9\u5c64\u6307\u4ee4\u6838\u5fc3)")), false);
                                            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                                            executor.schedule(() -> {
                                                mc.execute(() -> {
                                                    if (mc.gui != null) {
                                                        mc.gui.setOverlayMessage((Component)Component.empty(), false);
                                                    }
                                                });
                                                executor.shutdown();
                                            }, 3000L, TimeUnit.MILLISECONDS);
                                        }
                                    });
                                }
                            }
                        });
                        if (++particleTick % 5 != 0) {
                            return;
                        }
                        mc.execute(() -> {
                            ClientLevel lvl = mc.level;
                            if (lvl != null) {
                                for (double x = minX; x <= maxX; x += 0.5) {
                                    lvl.addParticle((ParticleOptions)pType, x, minY, minZ, 0.0, 0.01, 0.0);
                                    lvl.addParticle((ParticleOptions)pType, x, maxY, minZ, 0.0, 0.01, 0.0);
                                    lvl.addParticle((ParticleOptions)pType, x, minY, maxZ, 0.0, 0.01, 0.0);
                                    lvl.addParticle((ParticleOptions)pType, x, maxY, maxZ, 0.0, 0.01, 0.0);
                                }
                                for (double y = minY; y <= maxY; y += 0.5) {
                                    lvl.addParticle((ParticleOptions)pType, minX, y, minZ, 0.0, 0.01, 0.0);
                                    lvl.addParticle((ParticleOptions)pType, maxX, y, minZ, 0.0, 0.01, 0.0);
                                    lvl.addParticle((ParticleOptions)pType, minX, y, maxZ, 0.0, 0.01, 0.0);
                                    lvl.addParticle((ParticleOptions)pType, maxX, y, maxZ, 0.0, 0.01, 0.0);
                                }
                                for (double z = minZ; z <= maxZ; z += 0.5) {
                                    lvl.addParticle((ParticleOptions)pType, minX, minY, z, 0.0, 0.01, 0.0);
                                    lvl.addParticle((ParticleOptions)pType, maxX, minY, z, 0.0, 0.01, 0.0);
                                    lvl.addParticle((ParticleOptions)pType, minX, maxY, z, 0.0, 0.01, 0.0);
                                    lvl.addParticle((ParticleOptions)pType, maxX, maxY, z, 0.0, 0.01, 0.0);
                                }
                            }
                        });
                    }
                    Thread.sleep(150L);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }).start();
    }

    private int parseInputField(String name, int defaultValue) {
        try {
            for (GuiEventListener listener : this.children()) {
                EditBox box;
                String value;
                if (!(listener instanceof EditBox) || (value = (box = (EditBox)listener).getValue().trim()).isEmpty() || !value.matches("-?\\d+")) continue;
                return Integer.parseInt(value);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return defaultValue;
    }

    private void saveCoordinatesToLocal() {
        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }
            try (PrintWriter writer = new PrintWriter(new FileWriter(CONFIG_FILE));){
                writer.println((String)(posA != null ? posA.getX() + "," + posA.getY() + "," + posA.getZ() : "null"));
                writer.println((String)(posB != null ? posB.getX() + "," + posB.getY() + "," + posB.getZ() : "null"));
                writer.flush();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void loadCoordinatesFromLocal() {
        if (CONFIG_FILE.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE));){
                String[] tokens;
                String lineA = reader.readLine();
                String lineB = reader.readLine();
                if (lineA != null && !lineA.equalsIgnoreCase("null") && (tokens = lineA.split(",")).length == 3) {
                    posA = new BlockPos(Integer.parseInt(Arrays.asList(tokens).get(0)), Integer.parseInt(Arrays.asList(tokens).get(1)), Integer.parseInt(Arrays.asList(tokens).get(2)));
                }
                if (lineB != null && !lineB.equalsIgnoreCase("null") && (tokens = lineB.split(",")).length == 3) {
                    posB = new BlockPos(Integer.parseInt(Arrays.asList(tokens).get(0)), Integer.parseInt(Arrays.asList(tokens).get(1)), Integer.parseInt(Arrays.asList(tokens).get(2)));
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public static void FillRepeatingCore() {
        Minecraft mc = Minecraft.getInstance();
        String RepeatingCore = "";
    }

    public static List<String> getInternalSavedCommands() {
        ArrayList<String> commands = new ArrayList<String>();
        File configFile = new File(Minecraft.getInstance().gameDirectory, "config/faycore_commands.json");
        if (!configFile.exists()) {
            return commands;
        }
        try (FileReader reader = new FileReader(configFile);){
            List loaded = (List)FAYCORE_INTERNAL_GSON.fromJson((Reader)reader, new TypeToken<List<String>>(){}.getType());
            if (loaded != null) {
                commands.addAll(loaded);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return commands;
    }

    public static void autoFillCommandsViaPacket() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.player.connection == null) {
            return;
        }
        if (RepeatingCorePosA == null || RepeatingCorePosB == null) {
            return;
        }
        List<String> myCommands = FaycoreSettingsScreen.getInternalSavedCommands();
        int Max_RCy = Math.max(posA.getY(), posB.getY());
        int Min_RCy = Math.min(posA.getY(), posB.getY());
        String fillRepeatingGlass = String.format("fill %d %d %d %d %d %d minecraft:%s outline", RepeatingCorePosA.getX(), Max_RCy, RepeatingCorePosA.getZ(), RepeatingCorePosB.getX(), RepeatingCorePosB.getY() - 1, RepeatingCorePosB.getZ(), CoreGlass);
        String fillRepeatingGlass2 = String.format("fill %d %d %d %d %d %d minecraft:%s outline", RepeatingCorePosA.getX(), Max_RCy, RepeatingCorePosA.getZ(), RepeatingCorePosB.getX(), RepeatingCorePosB.getY() - (EmptyCoreSize + 2), RepeatingCorePosB.getZ(), CoreGlass);
        myCommands.add(0, fillRepeatingGlass);
        myCommands.add(1, fillRepeatingGlass2);
        if (myCommands.isEmpty()) {
            mc.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u63d0\u793a\uff1a\u6307\u4ee4\u6e05\u55ae\u70ba\u7a7a\uff0c\u53d6\u6d88\u6ce8\u5165\u3002"));
            return;
        }
        int minX = Math.min(RepeatingCorePosA.getX(), RepeatingCorePosB.getX());
        int maxX = Math.max(RepeatingCorePosA.getX(), RepeatingCorePosB.getX());
        int minY = Math.min(RepeatingCorePosA.getY(), RepeatingCorePosB.getY());
        int maxY = Math.max(RepeatingCorePosA.getY(), RepeatingCorePosB.getY());
        int minZ = Math.min(RepeatingCorePosA.getZ(), RepeatingCorePosB.getZ());
        int maxZ = Math.max(RepeatingCorePosA.getZ(), RepeatingCorePosB.getZ());
        int commandIndex = 0;
        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    if (commandIndex >= myCommands.size()) {
                        mc.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u5340\u57df\u5167\u6240\u6709\u81ea\u8a02\u6307\u4ee4\u5df2\u5168\u6578\u6210\u529f\u5f37\u5236\u6ce8\u5165\uff01"));
                        return;
                    }
                    BlockPos targetPos = new BlockPos(x, y, z);
                    String blockName = mc.level.getBlockState(targetPos).getBlock().getDescriptionId();
                    if (!blockName.contains("command_block")) continue;
                    String finalCmd = myCommands.get(commandIndex);
                    CompoundTag nbt = new CompoundTag();
                    nbt.putString("Command", finalCmd);
                    nbt.putBoolean("auto", true);
                    nbt.putBoolean("powered", false);
                    nbt.putString("id", "minecraft:command_block");
                    ServerboundSetCommandBlockPacket setPacket = new ServerboundSetCommandBlockPacket(targetPos, finalCmd, CommandBlockEntity.Mode.AUTO, false, false, true);
                    mc.player.connection.send((Packet)setPacket);
                    ++commandIndex;
                    try {
                        Thread.sleep(10L);
                        continue;
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
        }
    }

    public static void FillCoreCmd() {
        Minecraft mc = Minecraft.getInstance();
        String forceLoadCmd = String.format("forceload add %d %d %d %d", posA.getX(), posA.getZ(), posB.getX(), posB.getZ());
        String fillSolidCore = String.format("fill %d %d %d %d %d %d minecraft:command_block[facing=up]{CustomName:" + FayCoreMacroEngine.CustomName + ",components: " + FayCoreMacroEngine.CustomLore + "}", posA.getX(), posA.getY(), posA.getZ(), posB.getX(), posB.getY(), posB.getZ());
        int minX = Math.min(posA.getX(), posB.getX());
        int minY = Math.min(posA.getY(), posB.getY());
        int minZ = Math.min(posA.getZ(), posB.getZ());
        mc.player.connection.send((Packet)new ServerboundChatCommandPacket(forceLoadCmd));
        try {
            Thread.sleep(20L);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        mc.player.connection.send((Packet)new ServerboundChatCommandPacket(fillSolidCore));
        try {
            Thread.sleep(20L);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        FaycoreSettingsScreen.FillRepeatingCore();
        FaycoreSettingsScreen.autoFillCommandsViaPacket();
    }

    static {
        hasOverlayShownThisStage = false;
        CoreGlass = "red_stained_glass";
        EmptyCoreSize = 2;
        FRC_down = 2;
        particleTick = 0;
        CONFIG_FILE = new File(Minecraft.getInstance().gameDirectory, "config/faycore_config.txt");
        FAYCORE_INTERNAL_GSON = new Gson();
    }
}

