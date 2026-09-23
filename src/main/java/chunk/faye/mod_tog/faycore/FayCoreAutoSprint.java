package chunk.faye.mod_tog.faycore;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.network.chat.Component;

public class FayCoreAutoSprint {
   private static final int TOGGLE_KEY = 75;
   public static boolean enabled = false;
   private static boolean wasDown = false;

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)var0 -> {
         if (var0.player != null) {
            Window var1 = var0.getWindow();
            boolean var2 = InputConstants.isKeyDown(var1, 75);
            if (var2 && !wasDown) {
               enabled = !enabled;
               var0.player.sendSystemMessage(Component.literal("§9[FayCore] §fAutoSprint: " + (enabled ? "§aON" : "§cOFF")));
            }

            wasDown = var2;
            if (enabled && var0.player.onGround() && var0.options.keyUp.isDown()) {
               var0.player.setSprinting(true);
            }
         }
      });
   }
}
