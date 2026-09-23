package chunk.faye.mod_tog.faycore;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class FayCoreFly {
   private static final int TOGGLE_KEY = 72;
   private static final float FLY_SPEED = 0.05F;
   public static boolean enabled = false;
   private static boolean wasDown = false;

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)var0 -> {
         if (var0.player != null) {
            Window var1 = var0.getWindow();
            boolean var2 = InputConstants.isKeyDown(var1, 72);
            if (var2 && !wasDown) {
               enabled = !enabled;
               applyFlight(var0.player, enabled);
               var0.player.sendSystemMessage(Component.literal("§9[FayCore] §fFly: " + (enabled ? "§aON" : "§cOFF")));
            }

            wasDown = var2;
         }
      });
   }

   private static void applyFlight(LocalPlayer var0, boolean var1) {
      var0.getAbilities().mayfly = var1;
      var0.getAbilities().flying = var1;
      if (var1) {
         var0.getAbilities().setFlyingSpeed(0.05F);
      }

      var0.onUpdateAbilities();
   }
}
