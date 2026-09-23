package chunk.faye.mod_tog.faycore;

import chunk.faye.mod_tog.faycore.client.gui.FayCoreMacroEngine;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class FayCoreDash {
   private static final int DASH_KEY = 88;
   private static final double DASH_POWER = 1.6;
   private static final double DASH_UP = 0.35;
   private static final int COOLDOWN_TICKS = 8;
   private static boolean wasDown = false;
   private static int cooldown = 0;

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)var0 -> {
         if (var0.player != null && var0.level != null) {
            if (cooldown > 0) {
               cooldown--;
            }

            if (var0.screen != null) {
               wasDown = false;
            } else {
               Window var1 = var0.getWindow();
               boolean var2 = InputConstants.isKeyDown(var1, 88);
               if (var2 && !wasDown && cooldown == 0) {
                  dash(var0.player);
                  cooldown = 8;
               }

               wasDown = var2;
            }
         }
      });
   }

   private static void dash(LocalPlayer var0) {
      Vec3 var1 = var0.getLookAngle();
      var0.setDeltaMovement(var1.x * 1.6, 0.35, var1.z * 1.6);
      var0.hurtMarked = true;
      Minecraft var2 = Minecraft.getInstance();
      if (var2.level != null) {
         var2.level.playLocalSound(var0.getX(), var0.getY(), var0.getZ(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.7F, 1.6F, false);
      }

      FayCoreMacroEngine.autoFindAndInjectVCommand(var2, "execute as %player% at @s run particle minecraft:cloud ~ ~0.2 ~ 0.15 0.05 0.15 0.02 12 force @a");
   }
}
