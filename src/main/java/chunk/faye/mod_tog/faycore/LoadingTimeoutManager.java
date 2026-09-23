package chunk.faye.mod_tog.faycore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.network.chat.Component;

public class LoadingTimeoutManager {
   private static int loadingTickCounter = 0;
   private static final int TIMEOUT_TICKS = 100;

   public static void clientTick(Minecraft mc) {
      if (mc.player == null) {
         loadingTickCounter = 0;
      } else {
         if (mc.screen instanceof LevelLoadingScreen) {
            loadingTickCounter++;
            if (loadingTickCounter >= 100) {
               loadingTickCounter = 0;
               mc.execute(() -> {
                  if (mc.getConnection() != null) {
                     mc.player.sendSystemMessage(Component.literal("§c[FayCore 保護] 地形下載時間過長，已自動斷開連線防止卡死！"));
                     mc.getConnection().getConnection().disconnect(Component.literal("§c載入地形逾時 (Timeout)"));
                  }
               });
            }
         } else {
            loadingTickCounter = 0;
         }
      }
   }
}
