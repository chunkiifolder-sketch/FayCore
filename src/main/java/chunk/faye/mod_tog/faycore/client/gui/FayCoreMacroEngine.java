package chunk.faye.mod_tog.faycore.client.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity.Mode;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import org.lwjgl.glfw.GLFW;

public class FayCoreMacroEngine {
   private static final boolean[] coreFull = new boolean[10];
   private static final int[] nextSearchIndex = new int[10];
   public static final List<List<FayCoreMacroEngine.GroupLine>> dynamicGroupStorage = new ArrayList<>();
   public static final Map<String, Double> customVariables = new HashMap<>();
   public static int totalGroupsCount = 1;
   public static int selectedGroupIndex = 0;
   public static final List<Integer> groupModes = new ArrayList<>();
   public static final List<Boolean> groupEnables = new ArrayList<>();
   public static final List<Integer> matrixRunningIndices = new ArrayList<>();
   public static final List<Integer> matrixTickCooldowns = new ArrayList<>();
   public static final List<Boolean> matrixPulseStates = new ArrayList<>();
   public static boolean isMacroRunning = false;
   public static boolean isSingleTriggerLocked = false;
   public static final List<List<BlockPos>> matrixOwnedBlocksCache = new ArrayList<>();
   private static final File CONFIG_FILE = new File(Minecraft.getInstance().gameDirectory, "config/faycore_unlimited_groups.txt");
   public static String CustomName = "{\"text\":\"ꜰᴀʏᴄᴏʀᴇ\",\"color\":\"#B0FF95\",italic:false}";
   public static String CustomLore = "{\"minecraft:lore\": [{text:\"Made by FayeCruz\",color:gray,italic:false}]}";
   public static final Map<Integer, Integer> matrixReuseIndex = new HashMap<>();
   private static final ExecutorService FAYCORE_ASYNC_POOL = Executors.newSingleThreadExecutor();

   public static FayCoreMacroEngine.GroupLine getGroupLinePointer(int groupIdx, int lineIdx) {
      return dynamicGroupStorage.get(groupIdx).get(lineIdx);
   }

   public static void saveGroupsToDisk() {
      try {
         if (!CONFIG_FILE.getParentFile().exists()) {
            CONFIG_FILE.getParentFile().mkdirs();
         }

         try (PrintWriter writer = new PrintWriter(new FileWriter(CONFIG_FILE))) {
            writer.println("totalGroupsCount=" + totalGroupsCount);
            writer.println("selectedGroupIndex=" + selectedGroupIndex);

            for (int g = 0; g != 10; g++) {
               writer.println("g_" + g + "_mode=" + groupModes.get(g));
               writer.println("g_" + g + "_enable=" + groupEnables.get(g));
               List<FayCoreMacroEngine.GroupLine> currentList = dynamicGroupStorage.get(g);
               writer.println("g_" + g + "_size=" + currentList.size());

               for (int i = 0; i != currentList.size(); i++) {
                  writer.println("g_" + g + "_line_" + i + "_cmd=" + currentList.get(i).command);
                  writer.println("g_" + g + "_line_" + i + "_del=" + currentList.get(i).tickDelay);
               }
            }

            writer.flush();
         }
      } catch (Exception var6) {
      }
   }

   public static void loadGroupsFromDisk() {
      if (CONFIG_FILE.exists()) {
         String line;
         try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE))) {
            while ((line = reader.readLine()) != null) {
               String trimmed = line.trim();
               int eq = trimmed.indexOf(61);
               if (eq != -1) {
                  String val = trimmed.substring(eq + 1).trim();
                  if (trimmed.startsWith("totalGroupsCount=")) {
                     totalGroupsCount = Integer.parseInt(val);
                  }

                  if (trimmed.startsWith("selectedGroupIndex=")) {
                     selectedGroupIndex = Integer.parseInt(val);
                  }

                  for (int g = 0; g != 10; g++) {
                     if (trimmed.startsWith("g_" + g + "_mode=")) {
                        groupModes.set(g, Integer.parseInt(val));
                     }

                     if (trimmed.startsWith("g_" + g + "_enable=")) {
                        groupEnables.set(g, Boolean.parseBoolean(val));
                     }

                     if (trimmed.startsWith("g_" + g + "_size=")) {
                        int size = Integer.parseInt(val);
                        dynamicGroupStorage.get(g).clear();

                        for (int k = 0; k != size; k++) {
                           dynamicGroupStorage.get(g).add(new FayCoreMacroEngine.GroupLine("", 0));
                        }
                     }

                     int listSize = dynamicGroupStorage.get(g).size();

                     for (int i = 0; i != listSize; i++) {
                        if (trimmed.startsWith("g_" + g + "_line_" + i + "_cmd=")) {
                           dynamicGroupStorage.get(g).get(i).command = val;
                        }

                        if (trimmed.startsWith("g_" + g + "_line_" + i + "_del=")) {
                           dynamicGroupStorage.get(g).get(i).tickDelay = Integer.parseInt(val);
                        }
                     }
                  }
               }
            }
         } catch (Exception var10) {
         }
      }
   }

   public static BlockPos findNextEmptyBlock(Minecraft mc, int currentGroupIdx, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
      if (coreFull[currentGroupIdx]) {
         return null;
      } else {
         int totalX = maxX - minX + 1;
         int totalY = maxY - minY + 1;
         int totalZ = maxZ - minZ + 1;
         int totalVolume = totalX * totalY * totalZ;
         List<BlockPos> myOwnedList = matrixOwnedBlocksCache.get(currentGroupIdx);
         if (myOwnedList.size() >= totalVolume) {
            coreFull[currentGroupIdx] = true;
            return null;
         } else {
            int start = nextSearchIndex[currentGroupIdx];

            for (int offset = 0; offset < totalVolume; offset++) {
               int index = (start + offset) % totalVolume;
               int curX = minX + index % totalX;
               int curY = minY + index / totalX % totalY;
               int curZ = minZ + index / (totalX * totalY);
               BlockPos pos = new BlockPos(curX, curY, curZ);
               if (!myOwnedList.contains(pos) && mc.level.getBlockState(pos).is(Blocks.COMMAND_BLOCK)) {
                  BlockEntity var19 = mc.level.getBlockEntity(pos);
                  if (var19 instanceof CommandBlockEntity) {
                     CommandBlockEntity cb = (CommandBlockEntity)var19;
                     String cmd = cb.getCommandBlock().getCommand().trim();
                     if (cmd.isEmpty() || cmd.equals("")) {
                        boolean isClaimedByOthers = false;

                        for (int otherG = 0; otherG != 10; otherG++) {
                           if (otherG < matrixOwnedBlocksCache.size() && otherG != currentGroupIdx && matrixOwnedBlocksCache.get(otherG).contains(pos)) {
                              isClaimedByOthers = true;
                           }
                        }

                        if (!isClaimedByOthers) {
                           myOwnedList.add(pos);
                           nextSearchIndex[currentGroupIdx] = (index + 1) % totalVolume;
                           coreFull[currentGroupIdx] = false;
                           return pos;
                        }
                     }
                  }
               }
            }

            coreFull[currentGroupIdx] = true;
            return null;
         }
      }
   }

   public static void executeMacroEngineCore() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null && mc.player.connection != null) {
         BlockPos pA = FaycoreSettingsScreen.posA;
         BlockPos pB = FaycoreSettingsScreen.posB;
         if (pA != null && pB != null) {
            int minX = Math.min(pA.getX(), pB.getX());
            int maxX = Math.max(pA.getX(), pB.getX());
            int minY = Math.min(pA.getY(), pB.getY());
            int maxY = Math.max(pA.getY(), pB.getY());
            int minZ = Math.min(pA.getZ(), pB.getZ());
            int maxZ = Math.max(pA.getZ(), pB.getZ());

            for (int g = 0; g != 10; g++) {
               if (g < dynamicGroupStorage.size()
                  && g < groupEnables.size()
                  && g < groupModes.size()
                  && g < matrixRunningIndices.size()
                  && g < matrixTickCooldowns.size()
                  && g < matrixPulseStates.size()) {
                  boolean isCurEnabled = groupEnables.get(g);
                  int currentMode = groupModes.get(g);
                  if (!isCurEnabled) {
                     matrixRunningIndices.set(g, 0);
                     matrixTickCooldowns.set(g, 0);
                     matrixPulseStates.set(g, false);
                  } else if (!coreFull[g] && (currentMode == 2 || g == selectedGroupIndex && isMacroRunning)) {
                     List<FayCoreMacroEngine.GroupLine> activeList = dynamicGroupStorage.get(g);
                     int maxAllowedLines = activeList.size();
                     if (maxAllowedLines != 0) {
                        int runIdx = matrixRunningIndices.get(g);
                        int cooldown = matrixTickCooldowns.get(g);
                        if (currentMode == 2) {
                           BlockPos targetPos = findNextEmptyBlock(mc, g, minX, maxX, minY, maxY, minZ, maxZ);
                           if (targetPos == null) {
                              coreFull[g] = true;
                              FAYCORE_ASYNC_POOL.execute(() -> {
                                 try {
                                    if (matrixOwnedBlocksCache != null && matrixOwnedBlocksCache.get(g) != null) {
                                       matrixOwnedBlocksCache.get(g).clear();
                                    }

                                    mc.execute(() -> {
                                       if (mc.player != null && mc.player.connection != null) {
                                          nextSearchIndex[g] = 0;
                                          matrixRunningIndices.set(g, 0);
                                          matrixTickCooldowns.set(g, 0);
                                          matrixPulseStates.set(g, false);
                                          selectedGroupIndex = 0;
                                          coreFull[g] = false;
                                       }
                                    });
                                 } catch (Exception var3x) {
                                 }
                              });
                           } else if (runIdx != maxAllowedLines) {
                              boolean pulse = !matrixPulseStates.get(g);
                              matrixPulseStates.set(g, pulse);
                              String finalCmd = activeList.get(runIdx).command.replace('&', '§');
                              finalCmd = parseDynamicVariables(finalCmd);
                              mc.player.connection.send(new ServerboundSetCommandBlockPacket(targetPos, finalCmd, Mode.REDSTONE, false, false, pulse));
                              matrixRunningIndices.set(g, (runIdx + 1) % maxAllowedLines);
                           }
                        } else if (runIdx == maxAllowedLines) {
                           matrixRunningIndices.set(g, 0);
                           if (currentMode == 1) {
                              long winHandle = GLFW.glfwGetCurrentContext();
                              boolean isStillHolding = GLFW.glfwGetKey(winHandle, 72) == 1;
                              if (!isStillHolding) {
                                 isMacroRunning = false;
                              }
                           } else {
                              isMacroRunning = false;
                           }
                        } else if (cooldown != 0) {
                           matrixTickCooldowns.set(g, cooldown - 1);
                        } else {
                           BlockPos targetPos = findNextEmptyBlock(mc, g, minX, maxX, minY, maxY, minZ, maxZ);
                           if (targetPos != null) {
                              FayCoreMacroEngine.GroupLine currentLine = activeList.get(runIdx);
                              String finalCmd = currentLine.command.replace('&', '§');
                              finalCmd = parseDynamicVariables(finalCmd);
                              mc.player.connection.send(new ServerboundSetCommandBlockPacket(targetPos, finalCmd, Mode.REDSTONE, false, false, false));
                              mc.player.connection.send(new ServerboundSetCommandBlockPacket(targetPos, finalCmd, Mode.REDSTONE, false, false, true));
                              matrixTickCooldowns.set(g, currentLine.tickDelay);
                              matrixRunningIndices.set(g, runIdx + 1);
                           }
                        }
                     }
                  }
               }
            }
         }
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
         HitResult manualHit = mc.player.pick(300.0, 1.0F, false);
         if (manualHit != null && manualHit.getType() == Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult)manualHit;
            BlockPos lookPos = blockHit.getBlockPos();
            parsed = parsed.replace("%look-x%", String.valueOf(lookPos.getX()));
            parsed = parsed.replace("%look-y%", String.valueOf(lookPos.getY()));
            parsed = parsed.replace("%look-z%", String.valueOf(lookPos.getZ()));
         }

         return parsed;
      } else {
         return originalCommand;
      }
   }

   private static double evaluateExpression(String expr) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return 0.0;
      } else {
         int px = (int)Math.floor(mc.player.getX());
         int py = (int)Math.floor(mc.player.getY());
         int pz = (int)Math.floor(mc.player.getZ());
         int lx = px;
         int ly = py;
         int lz = pz;
         HitResult manualHit = mc.player.pick(500.0, 1.0F, false);
         if (manualHit != null && manualHit.getType() == Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult)manualHit;
            BlockPos lookPos = blockHit.getBlockPos();
            lx = lookPos.getX();
            ly = lookPos.getY();
            lz = lookPos.getZ();
         }

         String readyExpr = expr.trim();
         if (readyExpr.contains("ox") && !customVariables.containsKey("ox")) {
            customVariables.put("ox", (double)lx);
         }

         if (readyExpr.contains("oy") && !customVariables.containsKey("oy")) {
            customVariables.put("oy", (double)ly);
         }

         if (readyExpr.contains("oz") && !customVariables.containsKey("oz")) {
            customVariables.put("oz", (double)lz);
         }

         if (readyExpr.contains("px") && !customVariables.containsKey("px")) {
            customVariables.put("px", (double)px);
         }

         if (readyExpr.contains("py") && !customVariables.containsKey("py")) {
            customVariables.put("py", (double)py);
         }

         if (readyExpr.contains("pz") && !customVariables.containsKey("pz")) {
            customVariables.put("pz", (double)pz);
         }

         for (Entry<String, Double> entry : customVariables.entrySet()) {
            readyExpr = readyExpr.replace(entry.getKey(), String.valueOf(entry.getValue()));
         }

         String cleanExpr = readyExpr.replaceAll("\\s+", "");
         final String finalCleanExpr = cleanExpr;

         try {
            return (new Object() {
               int pos = -1;
               int ch;

               void nextChar() {
                  this.ch = ++this.pos < finalCleanExpr.length() ? finalCleanExpr.charAt(this.pos) : -1;
               }

               boolean eat(int charToEat) {
                  while (this.ch == 32) {
                     this.nextChar();
                  }

                  if (this.ch == charToEat) {
                     this.nextChar();
                     return true;
                  } else {
                     return false;
                  }
               }

               double parse() {
                  this.nextChar();
                  double x = this.parseExpression();
                  return this.pos < finalCleanExpr.length() ? 0.0 : x;
               }

               double parseExpression() {
                  double x = this.parseTerm();

                  while (true) {
                     while (this.eat(43)) {
                        x += this.parseTerm();
                     }

                     if (!this.eat(45)) {
                        return x;
                     }

                     x -= this.parseTerm();
                  }
               }

               double parseTerm() {
                  double x = this.parseFactor();

                  while (true) {
                     while (this.eat(42)) {
                        x *= this.parseFactor();
                     }

                     if (!this.eat(47)) {
                        return x;
                     }

                     double d = this.parseFactor();
                     x = d != 0.0 ? x / d : 0.0;
                  }
               }

               double parseFactor() {
                  if (this.eat(43)) {
                     return this.parseFactor();
                  } else if (this.eat(45)) {
                     return -this.parseFactor();
                  } else {
                     int startPos = this.pos;
                     if ((this.ch < 48 || this.ch > 57) && this.ch != 46) {
                        return 0.0;
                     } else {
                        while (this.ch >= 48 && this.ch <= 57 || this.ch == 46) {
                           this.nextChar();
                        }

                        return Double.parseDouble(finalCleanExpr.substring(startPos, this.pos));
                     }
                  }
               }
            }).parse();
         } catch (Exception var13) {
            return 0.0;
         }
      }
   }

   public static void executeVPressCommand(Object mcObj, String rawCommand) {
      if (mcObj instanceof Minecraft mc) {
         if (mc.player != null && mc.level != null && mc.player.connection != null) {
            BlockPos pA = FaycoreSettingsScreen.posA;
            BlockPos pB = FaycoreSettingsScreen.posB;
            if (pA != null && pB != null) {
               int minX = Math.min(pA.getX(), pB.getX());
               int maxX = Math.max(pA.getX(), pB.getX());
               int minY = Math.min(pA.getY(), pB.getY());
               int maxY = Math.max(pA.getY(), pB.getY());
               int minZ = Math.min(pA.getZ(), pB.getZ());
               int maxZ = Math.max(pA.getZ(), pB.getZ());
               BlockPos targetBlock = findNextEmptyBlock(mc, 9, minX, maxX, minY, maxY, minZ, maxZ);
               if (targetBlock == null && FaycoreSettingsScreen.posA != null && FaycoreSettingsScreen.posB != null) {
                  mc.gui
                     .setOverlayMessage(
                        Component.literal("§9[FayCore] §cError: No empty cmd block!").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}),
                        false
                     );
                  FaycoreSettingsScreen.FillCoreCmd();
                  FaycoreSettingsScreen.FillRepeatingCore();
                  matrixOwnedBlocksCache.get(9).clear();
                  nextSearchIndex[9] = 0;
                  coreFull[9] = false;
                  if (mc.level != null) {
                     mc.level
                        .playLocalSound(
                           mc.player.getX(),
                           mc.player.getY(),
                           mc.player.getZ(),
                           (SoundEvent)SoundEvents.NOTE_BLOCK_PLING.value(),
                           SoundSource.PLAYERS,
                           0.8F,
                           0.5F,
                           false
                        );
                  }
               } else {
                  String finalCmd = rawCommand.replace('&', '§');
                  finalCmd = parseDynamicVariables(finalCmd);
                  mc.player.connection.send(new ServerboundSetCommandBlockPacket(targetBlock, finalCmd, Mode.REDSTONE, false, false, false));
                  mc.player.connection.send(new ServerboundSetCommandBlockPacket(targetBlock, finalCmd, Mode.REDSTONE, false, false, true));
               }
            } else {
               mc.gui
                  .setOverlayMessage(
                     Component.literal("§9[FayCore] §cError: not yet set posA and posB!")
                        .withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}),
                     false
                  );
               if (mc.level != null) {
                  mc.level
                     .playLocalSound(
                        mc.player.getX(),
                        mc.player.getY(),
                        mc.player.getZ(),
                        (SoundEvent)SoundEvents.NOTE_BLOCK_PLING.value(),
                        SoundSource.PLAYERS,
                        0.8F,
                        0.5F,
                        false
                     );
               }
            }
         }
      }
   }

   public static void autoFindAndInjectVCommand(Object mcObj, String rawCommand) {
      if (mcObj instanceof Minecraft mc) {
         if (mc.player != null && mc.level != null && mc.player.connection != null) {
            if (!coreFull[9]) {
               BlockPos pA = FaycoreSettingsScreen.posA;
               BlockPos pB = FaycoreSettingsScreen.posB;
               if (pA != null && pB != null) {
                  int minX = Math.min(pA.getX(), pB.getX());
                  int maxX = Math.max(pA.getX(), pB.getX());
                  int minY = Math.min(pA.getY(), pB.getY());
                  int maxY = Math.max(pA.getY(), pB.getY());
                  int minZ = Math.min(pA.getZ(), pB.getZ());
                  int maxZ = Math.max(pA.getZ(), pB.getZ());
                  BlockPos targetBlock = findNextEmptyBlock(mc, 9, minX, maxX, minY, maxY, minZ, maxZ);
                  if (targetBlock == null) {
                     coreFull[9] = true;
                     FAYCORE_ASYNC_POOL.execute(() -> {
                        try {
                           if (matrixOwnedBlocksCache != null && matrixOwnedBlocksCache.get(9) != null) {
                              matrixOwnedBlocksCache.get(9).clear();
                           }

                           nextSearchIndex[9] = 0;
                           mc.execute(() -> {
                              if (mc.player != null && mc.player.connection != null) {
                                 coreFull[9] = false;
                              }
                           });
                        } catch (Exception var2x) {
                        }
                     });
                  } else {
                     String finalCmd = rawCommand.replace('&', '§');
                     finalCmd = parseDynamicVariables(finalCmd);
                     mc.player.connection.send(new ServerboundSetCommandBlockPacket(targetBlock, finalCmd, Mode.REDSTONE, false, false, false));
                     mc.player.connection.send(new ServerboundSetCommandBlockPacket(targetBlock, finalCmd, Mode.REDSTONE, false, false, true));
                  }
               }
            }
         }
      }
   }

   public static void executeVReleaseCommand(Object mcObj, String rawCommand) {
      if (mcObj instanceof Minecraft mc) {
         if (mc.player != null && mc.level != null && mc.player.connection != null) {
            BlockPos pA = FaycoreSettingsScreen.posA;
            BlockPos pB = FaycoreSettingsScreen.posB;
            if (pA != null && pB != null) {
               int minX = Math.min(pA.getX(), pB.getX());
               int maxX = Math.max(pA.getX(), pB.getX());
               int minY = Math.min(pA.getY(), pB.getY());
               int maxY = Math.max(pA.getY(), pB.getY());
               int minZ = Math.min(pA.getZ(), pB.getZ());
               int maxZ = Math.max(pA.getZ(), pB.getZ());
               BlockPos targetBlock = findNextEmptyBlock(mc, 9, minX, maxX, minY, maxY, minZ, maxZ);
               if (targetBlock != null) {
                  String finalCmd = rawCommand.replace('&', '§');
                  finalCmd = parseDynamicVariables(finalCmd);
                  mc.player.connection.send(new ServerboundSetCommandBlockPacket(targetBlock, finalCmd, Mode.REDSTONE, false, false, false));
                  mc.player.connection.send(new ServerboundSetCommandBlockPacket(targetBlock, finalCmd, Mode.REDSTONE, false, false, true));
               }
            }
         }
      }
   }

   static {
      for (int g = 0; g != 10; g++) {
         List<FayCoreMacroEngine.GroupLine> singleGroup = new ArrayList<>();
         singleGroup.add(new FayCoreMacroEngine.GroupLine("say 指令 1", 5));
         singleGroup.add(new FayCoreMacroEngine.GroupLine("say 指令 2", 5));
         singleGroup.add(new FayCoreMacroEngine.GroupLine("say 指令 3", 5));
         dynamicGroupStorage.add(singleGroup);
         groupModes.add(0);
         groupEnables.add(true);
         matrixRunningIndices.add(0);
         matrixTickCooldowns.add(0);
         matrixPulseStates.add(false);
         matrixOwnedBlocksCache.add(new ArrayList<>());
      }

      loadGroupsFromDisk();
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
