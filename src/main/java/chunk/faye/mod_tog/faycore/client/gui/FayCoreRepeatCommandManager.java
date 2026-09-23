package chunk.faye.mod_tog.faycore.client.gui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.world.level.block.entity.CommandBlockEntity.Mode;

public class FayCoreRepeatCommandManager {
   private static final File CONFIG_FILE = new File(Minecraft.getInstance().gameDirectory, "config/faycore_commands.json");
   private static final Gson GSON = new Gson();
   private static final Gson FAYCORE_INTERNAL_GSON = new Gson();

   public static void autoFillCommandsViaPacket() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null && mc.player.connection != null) {
         if (FaycoreSettingsScreen.RepeatingCorePosA != null && FaycoreSettingsScreen.RepeatingCorePosB != null) {
            List<String> myCommands = getInternalSavedCommands();
            int Max_RCy = Math.max(FaycoreSettingsScreen.RepeatingCorePosA.getY(), FaycoreSettingsScreen.RepeatingCorePosB.getY());
            int minX = Math.min(FaycoreSettingsScreen.RepeatingCorePosA.getX(), FaycoreSettingsScreen.RepeatingCorePosB.getX());
            int maxX = Math.max(FaycoreSettingsScreen.RepeatingCorePosA.getX(), FaycoreSettingsScreen.RepeatingCorePosB.getX());
            int minY = Math.min(FaycoreSettingsScreen.RepeatingCorePosA.getY(), FaycoreSettingsScreen.RepeatingCorePosB.getY());
            int maxY = Math.max(FaycoreSettingsScreen.RepeatingCorePosA.getY(), FaycoreSettingsScreen.RepeatingCorePosB.getY());
            int minZ = Math.min(FaycoreSettingsScreen.RepeatingCorePosA.getZ(), FaycoreSettingsScreen.RepeatingCorePosB.getZ());
            int maxZ = Math.max(FaycoreSettingsScreen.RepeatingCorePosA.getZ(), FaycoreSettingsScreen.RepeatingCorePosB.getZ());
            int totalCommandBlocksFound = 0;
            int commandIndex = 0;

            for (int y = minY; y <= maxY; y++) {
               for (int x = minX; x <= maxX; x++) {
                  for (int z = minZ; z <= maxZ; z++) {
                     if (commandIndex >= myCommands.size()) {
                        mc.player.sendSystemMessage(Component.literal("§9[FayCore] §7Core filled!"));
                        return;
                     }

                     BlockPos targetPos = new BlockPos(x, y, z);
                     String blockName = mc.level.getBlockState(targetPos).getBlock().getDescriptionId();
                     if (blockName.contains("repeating_command_block")) {
                        String finalCmd = myCommands.get(commandIndex);
                        ServerboundSetCommandBlockPacket setPacket = new ServerboundSetCommandBlockPacket(targetPos, finalCmd, Mode.AUTO, false, false, true);
                        mc.player.connection.send(setPacket);
                        commandIndex++;

                        try {
                           Thread.sleep(5L);
                        } catch (Exception var19) {
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static List<String> getInternalSavedCommands() {
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
}
