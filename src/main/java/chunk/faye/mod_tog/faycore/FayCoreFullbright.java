package chunk.faye.mod_tog.faycore;

import chunk.faye.mod_tog.faycore.client.gui.FayCoreMacroEngine;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class FayCoreFullbright {
   private static final int TOGGLE_KEY = 67;
   public static boolean enabled = false;
   private static boolean wasDown = false;

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)var0 -> {
         if (var0.player != null) {
            Window var1 = var0.getWindow();
            boolean var2 = InputConstants.isKeyDown(var1, 67);
            if (var2 && !wasDown) {
               enabled = !enabled;
               Minecraft var3 = Minecraft.getInstance();
               if (enabled) {
                  FayCoreMacroEngine.autoFindAndInjectVCommand(var3, "effect give %player% minecraft:night_vision infinite 0 true");
               } else {
                  FayCoreMacroEngine.autoFindAndInjectVCommand(var3, "effect clear %player% minecraft:night_vision");
               }

               var0.player.sendSystemMessage(Component.literal("§9[FayCore] §fFullbright: " + (enabled ? "§aON" : "§cOFF")));
            }

            wasDown = var2;
         }
      });
   }
}
