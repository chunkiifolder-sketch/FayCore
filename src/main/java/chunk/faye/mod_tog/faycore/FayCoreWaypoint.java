package chunk.faye.mod_tog.faycore;

import chunk.faye.mod_tog.faycore.client.gui.FayCoreMacroEngine;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class FayCoreWaypoint {
   private static final int SET_KEY = 89;
   private static final int TELEPORT_KEY = 85;
   public static final List<FayCoreWaypoint.Waypoint> waypoints = new ArrayList<>();
   private static boolean setWasDown = false;
   private static boolean tpWasDown = false;

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)var0 -> {
         if (var0.player != null) {
            if (var0.screen != null) {
               setWasDown = false;
               tpWasDown = false;
            } else {
               Window var1 = var0.getWindow();
               boolean var2 = InputConstants.isKeyDown(var1, 89);
               boolean var3 = InputConstants.isKeyDown(var1, 85);
               if (var2 && !setWasDown) {
                  addWaypoint(var0.player);
               }

               if (var3 && !tpWasDown) {
                  teleportToLast(var0);
               }

               setWasDown = var2;
               tpWasDown = var3;
            }
         }
      });
   }

   private static void addWaypoint(LocalPlayer var0) {
      BlockPos var1 = var0.blockPosition();
      waypoints.add(new FayCoreWaypoint.Waypoint(var1));
      var0.sendSystemMessage(Component.literal("§9[FayCore] §fWaypoint #" + (waypoints.size() - 1) + " set at §a" + var1.toShortString()));
   }

   private static void teleportToLast(Minecraft var0) {
      if (waypoints.isEmpty()) {
         var0.player.sendSystemMessage(Component.literal("§9[FayCore] §cNo waypoints saved yet."));
      } else {
         FayCoreWaypoint.Waypoint var1 = waypoints.get(waypoints.size() - 1);
         FayCoreMacroEngine.autoFindAndInjectVCommand(var0, "tp %player% " + var1.pos.getX() + " " + var1.pos.getY() + " " + var1.pos.getZ());
         var0.player.sendSystemMessage(Component.literal("§9[FayCore] §fTeleported to waypoint #" + (waypoints.size() - 1)));
      }
   }

   public static class Waypoint {
      public final BlockPos pos;

      public Waypoint(BlockPos var1) {
         this.pos = var1;
      }
   }
}
