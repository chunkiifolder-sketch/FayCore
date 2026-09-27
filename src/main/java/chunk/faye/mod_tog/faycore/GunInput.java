package chunk.faye.mod_tog.faycore;

import chunk.faye.mod_tog.faycore.client.gui.FayCoreMacroEngine;
import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class GunInput {
   private static boolean lastRight = false;
   private static int INTERPOLATION_DURATION = 4;
   private static int delay = 0;
   private static boolean waiting = false;

   public static void waitAndRun() {
      waiting = true;
      delay = 10;
   }

   public static Vec3 getTargetLocation(Minecraft mc, double maxDistance) {
      if (mc.player != null && mc.level != null) {
         Vec3 eyePos = mc.player.getEyePosition();
         Vec3 lookVec = mc.player.getLookAngle();
         Vec3 endPos = eyePos.add(lookVec.x * maxDistance, lookVec.y * maxDistance, lookVec.z * maxDistance);
         BlockHitResult blockHit = mc.level.clip(new ClipContext(eyePos, endPos, Block.COLLIDER, Fluid.NONE, mc.player));
         Vec3 finalHitPos = blockHit.getType() != Type.MISS ? blockHit.getLocation() : endPos;
         double currentNearestDist = eyePos.distanceTo(finalHitPos);
         EntityHitResult entityHit = EntityRaycast.raycastEntity(mc, (double)((int)currentNearestDist));
         return entityHit != null && entityHit.getType() == Type.ENTITY ? entityHit.getLocation() : finalHitPos;
      } else {
         return Vec3.ZERO;
      }
   }

   public static BlockPos getTargetBlockPos(Minecraft mc, double maxDistance) {
      if (mc.player != null && mc.level != null) {
         Vec3 eyePos = mc.player.getEyePosition();
         Vec3 lookVec = mc.player.getLookAngle();
         Vec3 endPos = eyePos.add(lookVec.x * maxDistance, lookVec.y * maxDistance, lookVec.z * maxDistance);
         BlockHitResult blockHit = mc.level.clip(new ClipContext(eyePos, endPos, Block.COLLIDER, Fluid.NONE, mc.player));
         if (blockHit.getType() == Type.BLOCK) {
            return blockHit.getBlockPos();
         } else {
            Vec3 hitVec = getTargetLocation(mc, maxDistance);
            return BlockPos.containing(hitVec.x, hitVec.y, hitVec.z);
         }
      } else {
         return BlockPos.ZERO;
      }
   }

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && mc.level != null) {
            boolean right = mc.options.keyUse.isDown();
            if (mc.gui.screen() != null) {
               lastRight = false;
               return;
            }

            if (right && !lastRight && isGun(mc)) {
               fireGun(mc);
            }

            lastRight = right;
            if (waiting) {
               delay--;
               if (delay <= 0) {
                  waiting = false;
                  FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "kill @e[type=minecraft:block_display,tag=Faycore.gun.bullet]");
               }
            }
         }
      });
   }

   private static boolean isGun(Minecraft mc) {
      ItemStack offhand = mc.player.getOffhandItem();
      CustomData data = (CustomData)offhand.get(DataComponents.CUSTOM_DATA);
      return data == null ? false : data.copyTag().contains("Faycore.fakegun");
   }

   private static void fireGun(Minecraft mc) {
      Vec3 eye = mc.player.getEyePosition();
      Vec3 look = mc.player.getLookAngle();
      Vec3 end = eye.add(look.x * 100.0, look.y * 100.0, look.z * 100.0);
      BlockHitResult hit = mc.level.clip(new ClipContext(eye, end, Block.COLLIDER, Fluid.NONE, mc.player));
      Vec3 targetPos = hit.getType() == Type.BLOCK ? hit.getLocation() : end;

      Vec3 dir = targetPos.subtract(eye).normalize();
      double speed = 1.6;

      FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "tag %player% add no_damage");

      FayCoreMacroEngine.autoFindAndInjectVCommand(
         mc,
         String.format(
            "execute as %%player%% at @s anchored eyes positioned ^ ^ ^0.3 run summon minecraft:llama_spit ~ ~ ~ {Tags:[\"Faycore.gun.spit\"],Motion:[%f,%f,%f]}",
            dir.x * speed, dir.y * speed, dir.z * speed
         )
      );

      FayCoreMacroEngine.autoFindAndInjectVCommand(
         mc,
         String.format("summon minecraft:interaction %f %f %f {Tags:[\"Faycore.gun.hitbox\"],width:0.6f,height:0.6f}", targetPos.x, targetPos.y, targetPos.z)
      );

      FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "execute as %player% at @s run playsound minecraft:entity.llama.spit player @a ~ ~ ~ 1 1 1");

      FayCoreTickQueue.runLater(
         15,
         () -> {
            FayCoreMacroEngine.autoFindAndInjectVCommand(
               mc,
               "execute as @e[type=minecraft:interaction,tag=Faycore.gun.hitbox] at @s run damage @e[type=!minecraft:llama_spit,type=!minecraft:interaction,tag=!no_damage,distance=..1.5,limit=1] 40 minecraft:player_attack by %player%"
            );
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "kill @e[tag=Faycore.gun.spit]");
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "kill @e[tag=Faycore.gun.hitbox]");
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "tag %player% remove no_damage");
         }
      );
   }
}
