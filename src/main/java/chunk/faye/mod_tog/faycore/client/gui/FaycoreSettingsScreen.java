package chunk.faye.mod_tog.faycore.client.gui;

import chunk.faye.mod_tog.faycore.init.FaycoreModScreens;
import chunk.faye.mod_tog.faycore.world.inventory.FaycoreSettingsMenu;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CommandBlockEntity.Mode;

public class FaycoreSettingsScreen extends AbstractContainerScreen<FaycoreSettingsMenu> implements FaycoreModScreens.FabricScreenAccessor {
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
   private static final File CONFIG_FILE = new File(Minecraft.getInstance().gameDirectory, "config/faycore_config.txt");
   private static boolean hasOverlayShownThisStage = false;
   public static String CoreGlass = "red_stained_glass";
   public static int EmptyCoreSize = 2;
   public static int FRC_down = 2;
   private static int particleTick = 0;
   private static final Gson FAYCORE_INTERNAL_GSON = new Gson();

   public FaycoreSettingsScreen(FaycoreSettingsMenu container, Inventory inventory, Component text) {
      super(container, inventory, text, 176, 166);
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
      guiGraphics.blit(
         RenderPipelines.GUI_TEXTURED,
         Identifier.parse("faycore:textures/screens/faycore_settings.png"),
         this.leftPos,
         this.topPos,
         0.0F,
         0.0F,
         this.imageWidth,
         this.imageHeight,
         this.imageWidth,
         this.imageHeight
      );
   }

   public boolean keyPressed(KeyEvent event) {
      if (event.key() == 256) {
         this.minecraft.player.closeContainer();
         return true;
      } else {
         return super.keyPressed(event);
      }
   }

   protected void extractLabels(GuiGraphicsExtractor g, int mX, int mY) {
      g.text(this.font, Component.translatable("gui.faycore.faycore_settings.label_faycore_setting"), 48, 8, -26164, false);
   }

   public void init() {
      super.init();
      if (posA == null || posB == null) {
         this.loadCoordinatesFromLocal();
      }

      String displayLabel = fillStage == 2 ? "§cDelete Core" : (fillStage == 1 ? "Save and set §7B pos" : "Save and set §7A pos");
      this.button_set_core_position = Button.builder(
            Component.literal(displayLabel),
            btn -> {
               Minecraft mc = Minecraft.getInstance();
               if (mc.player != null) {
                  if (fillStage == 0) {
                     int ax = this.parseInputField("input_ax", mc.player.blockPosition().getX());
                     int ay = this.parseInputField("input_ay", mc.player.blockPosition().getY());
                     int az = this.parseInputField("input_az", mc.player.blockPosition().getZ());
                     posA = new BlockPos(ax, ay, az);
                     fillStage = 1;
                     this.saveCoordinatesToLocal();
                     mc.player.sendSystemMessage(Component.literal("§9[FayCore] §lA §7Pos set: §e" + ax + ", " + ay + ", " + az));
                     this.minecraft.setScreen((Screen)null);
                  } else if (fillStage == 1) {
                     int bx = this.parseInputField("input_bx", mc.player.blockPosition().getX());
                     int by = this.parseInputField("input_by", mc.player.blockPosition().getY());
                     int bz = this.parseInputField("input_bz", mc.player.blockPosition().getZ());
                     int sizeX = Math.abs(posA.getX() - bx) + 1;
                     int sizeY = Math.abs(posA.getY() - by) + 1;
                     int sizeZ = Math.abs(posA.getZ() - bz) + 1;
                     if (sizeX * sizeY * sizeZ > 32768 || sizeX > 32 || sizeY > 32 || sizeZ > 32) {
                        mc.player.sendSystemMessage(Component.literal("§9[FayCore] §7The size too big, max is 32*32*32"));
                        return;
                     }

                     posB = new BlockPos(bx, by, bz);
                     fillStage = 2;
                     this.saveCoordinatesToLocal();
                     new Thread(
                           () -> {
                              try {
                                 if (mc.player != null && mc.player.connection != null) {
                                    List<String> innerCommandsx = new ArrayList<>();
                                    innerCommandsx.add(String.format("forceload add %d %d %d %d", posA.getX(), posA.getZ(), posB.getX(), posB.getZ()));
                                    innerCommandsx.add(
                                       String.format(
                                          "fill %d %d %d %d %d %d minecraft:command_block[facing=up]{CustomName:"
                                             + FayCoreMacroEngine.CustomName
                                             + ",components: "
                                             + FayCoreMacroEngine.CustomLore
                                             + "}",
                                          posA.getX(),
                                          posA.getY(),
                                          posA.getZ(),
                                          posB.getX(),
                                          posB.getY(),
                                          posB.getZ()
                                       )
                                    );
                                    FayCoreFastRun.fireGhostPayloadQueue(new ArrayList<>(innerCommandsx));
                                    int minX = Math.min(posA.getX(), posB.getX());
                                    int minYx = Math.min(posA.getY(), posB.getY());
                                    int minZ = Math.min(posA.getZ(), posB.getZ());
                                    FillRepeatingCore();
                                    autoFillCommandsViaPacket();
                                 }
                              } catch (Exception var5x) {
                              }
                           }
                        )
                        .start();
                     this.minecraft.setScreen((Screen)null);
                     startParticleThread(mc);
                     int curMode = FayCoreMacroEngine.groupModes.get(FayCoreMacroEngine.selectedGroupIndex);
                     boolean curEnable = FayCoreMacroEngine.groupEnables.get(FayCoreMacroEngine.selectedGroupIndex);
                     if (curMode == 2 && curEnable) {
                        FayCoreMacroEngine.isMacroRunning = true;
                     }
                  } else {
                     if (posA != null && posB != null && mc.player.connection != null) {
                        int minY = Math.min(posA.getY(), posB.getY());
                        int maxY = Math.max(posA.getY(), posB.getY());
                        String wipeCoreBlocks = String.format(
                           "fill %d %d %d %d %d %d minecraft:air", posA.getX(), maxY, posA.getZ(), posB.getX(), minY - (EmptyCoreSize + 7), posB.getZ()
                        );
                        String unloadChunks = String.format("forceload remove %d %d %d %d", posA.getX(), posA.getZ(), posB.getX(), posB.getZ());
                        List<String> innerCommands = new ArrayList<>();
                        innerCommands.add(wipeCoreBlocks);
                        innerCommands.add(unloadChunks);
                        FayCoreFastRun.fireGhostPayloadQueue(new ArrayList<>(innerCommands));
                        hasOverlayShownThisStage = false;
                     }

                     fillStage = 0;
                     posA = null;
                     posB = null;
                     this.minecraft.setScreen((Screen)null);
                  }
               }
            }
         )
         .bounds(this.leftPos + 32, this.topPos + 28, 110, 20)
         .build();
      this.addRenderableWidget(this.button_set_core_position);
      this.button_fast_fill = Button.builder(
            Component.literal("§7Place Core"),
            btn -> {
               Minecraft mc = Minecraft.getInstance();
               if (mc.player != null) {
                  this.loadCoordinatesFromLocal();
                  if (posA != null && posB != null) {
                     fillStage = 2;
                     new Thread(
                           () -> {
                              try {
                                 if (mc.player != null && mc.player.connection != null) {
                                    String forceLoadCmd = String.format("forceload add %d %d %d %d", posA.getX(), posA.getZ(), posB.getX(), posB.getZ());
                                    String fillSolidCore = String.format(
                                       "fill %d %d %d %d %d %d minecraft:command_block[facing=up]{CustomName:"
                                          + FayCoreMacroEngine.CustomName
                                          + ",components: "
                                          + FayCoreMacroEngine.CustomLore
                                          + "}",
                                       posA.getX(),
                                       posA.getY(),
                                       posA.getZ(),
                                       posB.getX(),
                                       posB.getY(),
                                       posB.getZ()
                                    );
                                    int minX = Math.min(posA.getX(), posB.getX());
                                    int minY = Math.min(posA.getY(), posB.getY());
                                    int minZ = Math.min(posA.getZ(), posB.getZ());
                                    String lockSystemBlock = String.format(
                                       "setblock %d %d %d minecraft:command_block[facing=up]{Command:\"#FAYCORE_SYSTEM_LOCK\",CustomName:"
                                          + FayCoreMacroEngine.CustomName
                                          + ",components: "
                                          + FayCoreMacroEngine.CustomLore
                                          + "} replace",
                                       minX,
                                       minY,
                                       minZ
                                    );
                                    List<String> innerCommands = new ArrayList<>();
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
                                    innerCommands.add(
                                       String.format(
                                          "fill %d %d %d %d %d %d minecraft:repeating_command_block[facing=up]{CustomName:"
                                             + FayCoreMacroEngine.CustomName
                                             + ",components: "
                                             + FayCoreMacroEngine.CustomLore
                                             + "}",
                                          RepeatingCorePosA.getX(),
                                          RepeatingCorePosA.getY() - 1,
                                          RepeatingCorePosA.getZ(),
                                          RepeatingCorePosB.getX(),
                                          RepeatingCorePosB.getY(),
                                          RepeatingCorePosB.getZ()
                                       )
                                    );
                                    innerCommands.add(
                                       String.format(
                                          "fill %d %d %d %d %d %d minecraft:%s outline",
                                          RepeatingCorePosA.getX(),
                                          Max_RCy,
                                          RepeatingCorePosA.getZ(),
                                          RepeatingCorePosB.getX(),
                                          RepeatingCorePosB.getY() - 1,
                                          RepeatingCorePosB.getZ(),
                                          CoreGlass
                                       )
                                    );
                                    innerCommands.add(
                                       String.format(
                                          "fill %d %d %d %d %d %d minecraft:%s outline",
                                          RepeatingCorePosA.getX(),
                                          Max_RCy,
                                          RepeatingCorePosA.getZ(),
                                          RepeatingCorePosB.getX(),
                                          RepeatingCorePosB.getY() - (EmptyCoreSize + 2),
                                          RepeatingCorePosB.getZ(),
                                          CoreGlass
                                       )
                                    );
                                    List<String> myCommands = getInternalSavedCommands();
                                    FayCoreFastRun.fireGhostPayloadQueue(new ArrayList<>(innerCommands));
                                    FillRepeatingCore();
                                 }
                              } catch (Exception var15) {
                              }
                           }
                        )
                        .start();
                     this.minecraft.setScreen((Screen)null);
                     startParticleThread(mc);
                     int curMode = FayCoreMacroEngine.groupModes.get(FayCoreMacroEngine.selectedGroupIndex);
                     boolean curEnable = FayCoreMacroEngine.groupEnables.get(FayCoreMacroEngine.selectedGroupIndex);
                     if (curMode == 2 && curEnable) {
                        FayCoreMacroEngine.isMacroRunning = true;
                     }
                  } else {
                     mc.player.sendSystemMessage(Component.literal("§9[FayCore] §4ERROR§8: §7No history!"));
                  }
               }
            }
         )
         .bounds(this.leftPos + 32, this.topPos + 58, 110, 20)
         .build();
      this.button_fast_fill.active = true;
      this.addRenderableWidget(this.button_fast_fill);
      new Thread(() -> {
         try {
            for (int tick = 0; tick != 20; tick++) {
               Thread.sleep(50L);
               Minecraft mcInstance = Minecraft.getInstance();
               if (mcInstance != null) {
                  mcInstance.execute(() -> {
                     try {
                        for (GuiEventListener listener : this.children()) {
                           if (listener instanceof EditBox) {
                              EditBox box = (EditBox)listener;
                              String val = box.getValue();
                              if (val != null && (val.equals("b") || val.equalsIgnoreCase("b") || val.contains("b") || val.contains("B"))) {
                                 box.setValue("");
                              }

                              box.setFocused(false);
                           }
                        }

                        this.setFocused((GuiEventListener)null);
                     } catch (Exception var5) {
                     }
                  });
               }
            }
         } catch (Exception var3) {
         }
      }).start();
   }

   private static void startParticleThread(Minecraft mc) {
      new Thread(() -> {
         try {
            for (SimpleParticleType pType = ParticleTypes.GLOW; fillStage == 2 && mc.player != null; Thread.sleep(150L)) {
               if (mc.level != null && posA != null && posB != null) {
                  double minX = (double)Math.min(posA.getX(), posB.getX());
                  double maxX = (double)Math.max(posA.getX(), posB.getX()) + 1.0;
                  double minY = (double)Math.min(posA.getY(), posB.getY());
                  double maxY = (double)Math.max(posA.getY(), posB.getY()) + 1.0;
                  double minZ = (double)Math.min(posA.getZ(), posB.getZ());
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
                                 mc.gui.setOverlayMessage(Component.literal("§a§l[FayCore 選區] §e§l" + sX + "x" + sY + "x" + sZ + " §f§l(雙層指令核心)"), false);
                                 ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                                 executor.schedule(() -> {
                                    mc.execute(() -> {
                                       if (mc.gui != null) {
                                          mc.gui.setOverlayMessage(Component.empty(), false);
                                       }
                                    });
                                    executor.shutdown();
                                 }, 3000L, TimeUnit.MILLISECONDS);
                              }
                           });
                        }
                     }
                  });
                  particleTick++;
                  if (particleTick % 5 != 0) {
                     return;
                  }

                  mc.execute(() -> {
                     ClientLevel lvl = mc.level;
                     if (lvl != null) {
                        for (double x = minX; x <= maxX; x += 0.5) {
                           lvl.addParticle(pType, x, minY, minZ, 0.0, 0.01, 0.0);
                           lvl.addParticle(pType, x, maxY, minZ, 0.0, 0.01, 0.0);
                           lvl.addParticle(pType, x, minY, maxZ, 0.0, 0.01, 0.0);
                           lvl.addParticle(pType, x, maxY, maxZ, 0.0, 0.01, 0.0);
                        }

                        for (double y = minY; y <= maxY; y += 0.5) {
                           lvl.addParticle(pType, minX, y, minZ, 0.0, 0.01, 0.0);
                           lvl.addParticle(pType, maxX, y, minZ, 0.0, 0.01, 0.0);
                           lvl.addParticle(pType, minX, y, maxZ, 0.0, 0.01, 0.0);
                           lvl.addParticle(pType, maxX, y, maxZ, 0.0, 0.01, 0.0);
                        }

                        for (double z = minZ; z <= maxZ; z += 0.5) {
                           lvl.addParticle(pType, minX, minY, z, 0.0, 0.01, 0.0);
                           lvl.addParticle(pType, maxX, minY, z, 0.0, 0.01, 0.0);
                           lvl.addParticle(pType, minX, maxY, z, 0.0, 0.01, 0.0);
                           lvl.addParticle(pType, maxX, maxY, z, 0.0, 0.01, 0.0);
                        }
                     }
                  });
               }
            }
         } catch (Exception var17) {
         }
      }).start();
   }

   private int parseInputField(String name, int defaultValue) {
      try {
         for (GuiEventListener listener : this.children()) {
            if (listener instanceof EditBox box) {
               String value = box.getValue().trim();
               if (!value.isEmpty() && value.matches("-?\\d+")) {
                  return Integer.parseInt(value);
               }
            }
         }
      } catch (Exception var7) {
      }

      return defaultValue;
   }

   private void saveCoordinatesToLocal() {
      try {
         if (!CONFIG_FILE.getParentFile().exists()) {
            CONFIG_FILE.getParentFile().mkdirs();
         }

         try (PrintWriter writer = new PrintWriter(new FileWriter(CONFIG_FILE))) {
            writer.println(posA != null ? posA.getX() + "," + posA.getY() + "," + posA.getZ() : "null");
            writer.println(posB != null ? posB.getX() + "," + posB.getY() + "," + posB.getZ() : "null");
            writer.flush();
         }
      } catch (Exception var6) {
      }
   }

   private void loadCoordinatesFromLocal() {
      if (CONFIG_FILE.exists()) {
         try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE))) {
            String lineA = reader.readLine();
            String lineB = reader.readLine();
            if (lineA != null && !lineA.equalsIgnoreCase("null")) {
               String[] tokens = lineA.split(",");
               if (tokens.length == 3) {
                  posA = new BlockPos(
                     Integer.parseInt(Arrays.asList(tokens).get(0)),
                     Integer.parseInt(Arrays.asList(tokens).get(1)),
                     Integer.parseInt(Arrays.asList(tokens).get(2))
                  );
               }
            }

            if (lineB != null && !lineB.equalsIgnoreCase("null")) {
               String[] tokens = lineB.split(",");
               if (tokens.length == 3) {
                  posB = new BlockPos(
                     Integer.parseInt(Arrays.asList(tokens).get(0)),
                     Integer.parseInt(Arrays.asList(tokens).get(1)),
                     Integer.parseInt(Arrays.asList(tokens).get(2))
                  );
               }
            }
         } catch (Exception var7) {
         }
      }
   }

   public static void FillRepeatingCore() {
      Minecraft mc = Minecraft.getInstance();
      String RepeatingCore = "";
   }

   public static List<String> getInternalSavedCommands() {
      List<String> commands = new ArrayList<>();
      File configFile = new File(Minecraft.getInstance().gameDirectory, "config/faycore_commands.json");
      if (!configFile.exists()) {
         return commands;
      } else {
         try (Reader reader = new FileReader(configFile)) {
            List<String> loaded = (List<String>)FAYCORE_INTERNAL_GSON.fromJson(reader, (new TypeToken<List<String>>() {
            }).getType());
            if (loaded != null) {
               commands.addAll(loaded);
            }
         } catch (Exception var7) {
            var7.printStackTrace();
         }

         return commands;
      }
   }

   public static void autoFillCommandsViaPacket() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null && mc.player.connection != null) {
         if (RepeatingCorePosA != null && RepeatingCorePosB != null) {
            List<String> myCommands = getInternalSavedCommands();
            int Max_RCy = Math.max(posA.getY(), posB.getY());
            int Min_RCy = Math.min(posA.getY(), posB.getY());
            String fillRepeatingGlass = String.format(
               "fill %d %d %d %d %d %d minecraft:%s outline",
               RepeatingCorePosA.getX(),
               Max_RCy,
               RepeatingCorePosA.getZ(),
               RepeatingCorePosB.getX(),
               RepeatingCorePosB.getY() - 1,
               RepeatingCorePosB.getZ(),
               CoreGlass
            );
            String fillRepeatingGlass2 = String.format(
               "fill %d %d %d %d %d %d minecraft:%s outline",
               RepeatingCorePosA.getX(),
               Max_RCy,
               RepeatingCorePosA.getZ(),
               RepeatingCorePosB.getX(),
               RepeatingCorePosB.getY() - (EmptyCoreSize + 2),
               RepeatingCorePosB.getZ(),
               CoreGlass
            );
            myCommands.add(0, fillRepeatingGlass);
            myCommands.add(1, fillRepeatingGlass2);
            if (myCommands.isEmpty()) {
               mc.player.sendSystemMessage(Component.literal("§9[FayCore] 提示：指令清單為空，取消注入。"));
            } else {
               int minX = Math.min(RepeatingCorePosA.getX(), RepeatingCorePosB.getX());
               int maxX = Math.max(RepeatingCorePosA.getX(), RepeatingCorePosB.getX());
               int minY = Math.min(RepeatingCorePosA.getY(), RepeatingCorePosB.getY());
               int maxY = Math.max(RepeatingCorePosA.getY(), RepeatingCorePosB.getY());
               int minZ = Math.min(RepeatingCorePosA.getZ(), RepeatingCorePosB.getZ());
               int maxZ = Math.max(RepeatingCorePosA.getZ(), RepeatingCorePosB.getZ());
               int commandIndex = 0;

               for (int y = minY; y <= maxY; y++) {
                  for (int x = minX; x <= maxX; x++) {
                     for (int z = minZ; z <= maxZ; z++) {
                        if (commandIndex >= myCommands.size()) {
                           mc.player.sendSystemMessage(Component.literal("§9[FayCore] 區域內所有自訂指令已全數成功強制注入！"));
                           return;
                        }

                        BlockPos targetPos = new BlockPos(x, y, z);
                        String blockName = mc.level.getBlockState(targetPos).getBlock().getDescriptionId();
                        if (blockName.contains("command_block")) {
                           String finalCmd = myCommands.get(commandIndex);
                           CompoundTag nbt = new CompoundTag();
                           nbt.putString("Command", finalCmd);
                           nbt.putBoolean("auto", true);
                           nbt.putBoolean("powered", false);
                           nbt.putString("id", "minecraft:command_block");
                           ServerboundSetCommandBlockPacket setPacket = new ServerboundSetCommandBlockPacket(targetPos, finalCmd, Mode.AUTO, false, false, true);
                           mc.player.connection.send(setPacket);
                           commandIndex++;

                           try {
                              Thread.sleep(10L);
                           } catch (Exception var22) {
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void FillCoreCmd() {
      Minecraft mc = Minecraft.getInstance();
      String forceLoadCmd = String.format("forceload add %d %d %d %d", posA.getX(), posA.getZ(), posB.getX(), posB.getZ());
      String fillSolidCore = String.format(
         "fill %d %d %d %d %d %d minecraft:command_block[facing=up]{CustomName:"
            + FayCoreMacroEngine.CustomName
            + ",components: "
            + FayCoreMacroEngine.CustomLore
            + "}",
         posA.getX(),
         posA.getY(),
         posA.getZ(),
         posB.getX(),
         posB.getY(),
         posB.getZ()
      );
      int minX = Math.min(posA.getX(), posB.getX());
      int minY = Math.min(posA.getY(), posB.getY());
      int minZ = Math.min(posA.getZ(), posB.getZ());
      mc.player.connection.send(new ServerboundChatCommandPacket(forceLoadCmd));

      try {
         Thread.sleep(20L);
      } catch (InterruptedException var8) {
         var8.printStackTrace();
      }

      mc.player.connection.send(new ServerboundChatCommandPacket(fillSolidCore));

      try {
         Thread.sleep(20L);
      } catch (InterruptedException var7) {
         var7.printStackTrace();
      }

      FillRepeatingCore();
      autoFillCommandsViaPacket();
   }
}
