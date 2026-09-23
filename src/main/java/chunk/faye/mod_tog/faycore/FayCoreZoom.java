package chunk.faye.mod_tog.faycore;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.client.Minecraft;

public class FayCoreZoom {
   private static final int ZOOM_KEY = 90;
   private static final int ZOOM_FOV = 20;
   private static boolean zooming = false;
   private static Integer baseFov = null;

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)var0 -> {
         if (var0.player != null && var0.options != null) {
            Window var1 = var0.getWindow();
            boolean var2 = InputConstants.isKeyDown(var1, 90) && var0.screen == null;
            if (var2 != zooming) {
               zooming = var2;
               applyZoom(var0);
            }
         }
      });
   }

   private static void applyZoom(Minecraft var0) {
      try {
         if (baseFov == null) {
            Object var1 = var0.options.fov().get();
            baseFov = var1 instanceof Number ? ((Number)var1).intValue() : 70;
         }

         var0.options.fov().set(zooming ? 20 : baseFov);
      } catch (Throwable var2) {
      }
   }
}
