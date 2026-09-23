package chunk.faye.mod_tog.faycore;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.world.level.block.entity.CommandBlockEntity.Mode;

public class FayCoreRepeatCommandManager {
   public static void spawnAndDestroyGhostCore(String targetCommand) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null && mc.player.connection != null) {
         BlockPos playerPos = mc.player.blockPosition();
         BlockPos ghostPos = playerPos.above(3);
         String currentBlock = mc.level.getBlockState(ghostPos).getBlock().getDescriptionId();
         if (!currentBlock.contains("air")) {
            ghostPos = playerPos.below(2);
            currentBlock = mc.level.getBlockState(ghostPos).getBlock().getDescriptionId();
            if (!currentBlock.contains("air")) {
               return;
            }
         }

         BlockPos finalGhostPos = ghostPos;
         new Thread(
               () -> {
                  try {
                     String placeCoreCmd = String.format(
                        "setblock %d %d %d minecraft:repeating_command_block replace", finalGhostPos.getX(), finalGhostPos.getY(), finalGhostPos.getZ()
                     );
                     mc.player.connection.send(new ServerboundChatCommandPacket(placeCoreCmd));
                     Thread.sleep(20L);
                     ServerboundSetCommandBlockPacket writePacket = new ServerboundSetCommandBlockPacket(
                        finalGhostPos, targetCommand, Mode.REDSTONE, false, false, false
                     );
                     mc.player.connection.send(writePacket);
                     Thread.sleep(15L);
                     ServerboundSetCommandBlockPacket activatePacket = new ServerboundSetCommandBlockPacket(
                        finalGhostPos, targetCommand, Mode.AUTO, false, false, true
                     );
                     mc.player.connection.send(activatePacket);
                     Thread.sleep(40L);
                     String destroyCoreCmd = String.format(
                        "setblock %d %d %d minecraft:air replace", finalGhostPos.getX(), finalGhostPos.getY(), finalGhostPos.getZ()
                     );
                     mc.player.connection.send(new ServerboundChatCommandPacket(destroyCoreCmd));
                     mc.player.sendSystemMessage(Component.literal("§a[FayCore 定點幽靈] 兩階段通電發射成功並抹除！"));
                  } catch (Exception var7) {
                     var7.printStackTrace();
                  }
               }
            )
            .start();
      }
   }

   public static void spawnAndDestroyRandomGhostCore(String targetCommand) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null && mc.player.connection != null) {
         BlockPos playerPos = mc.player.blockPosition();
         Random random = new Random();
         BlockPos chosenPos = null;

         for (int i = 0; i < 10; i++) {
            int randomX = random.nextInt(11) - 5;
            int randomZ = random.nextInt(11) - 5;
            int randomY = random.nextInt(4) + 1;
            BlockPos checkPos = playerPos.offset(randomX, randomY, randomZ);
            String blockName = mc.level.getBlockState(checkPos).getBlock().getDescriptionId();
            if (blockName.contains("air")) {
               chosenPos = checkPos;
               break;
            }
         }

         if (chosenPos == null) {
            chosenPos = playerPos.above(3);
         }

         BlockPos finalGhostPos = chosenPos;
         new Thread(
               () -> {
                  try {
                     String placeCoreCmd = String.format(
                        "setblock %d %d %d minecraft:repeating_command_block replace", finalGhostPos.getX(), finalGhostPos.getY(), finalGhostPos.getZ()
                     );
                     mc.player.connection.send(new ServerboundChatCommandPacket(placeCoreCmd));
                     Thread.sleep(20L);
                     ServerboundSetCommandBlockPacket writePacket = new ServerboundSetCommandBlockPacket(
                        finalGhostPos, targetCommand, Mode.REDSTONE, false, false, false
                     );
                     mc.player.connection.send(writePacket);
                     Thread.sleep(15L);
                     ServerboundSetCommandBlockPacket activatePacket = new ServerboundSetCommandBlockPacket(
                        finalGhostPos, targetCommand, Mode.AUTO, false, false, true
                     );
                     mc.player.connection.send(activatePacket);
                     Thread.sleep(40L);
                     String destroyCoreCmd = String.format(
                        "setblock %d %d %d minecraft:air destroy", finalGhostPos.getX(), finalGhostPos.getY(), finalGhostPos.getZ()
                     );
                     mc.player.connection.send(new ServerboundChatCommandPacket(destroyCoreCmd));
                  } catch (Exception var7x) {
                     var7x.printStackTrace();
                  }
               }
            )
            .start();
      }
   }
}
