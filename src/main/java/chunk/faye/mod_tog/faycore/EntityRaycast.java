package chunk.faye.mod_tog.faycore;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class EntityRaycast {
   public static EntityHitResult raycastEntity(Minecraft mc, double range) {
      if (mc.player != null && mc.level != null) {
         Vec3 eye = mc.player.getEyePosition();
         Vec3 look = mc.player.getLookAngle();
         Vec3 end = eye.add(look.x * range, look.y * range, look.z * range);
         AABB box = mc.player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
         Entity target = null;
         double closest = range * range;

         for (Entity entity : mc.level.getEntities(mc.player, box, e -> e.isPickable())) {
            AABB hitbox = entity.getBoundingBox().inflate(0.2);
            Optional<Vec3> result = hitbox.clip(eye, end);
            if (result.isPresent()) {
               double distance = eye.distanceToSqr(result.get());
               if (distance < closest) {
                  closest = distance;
                  target = entity;
               }
            }
         }

         return target != null ? new EntityHitResult(target) : null;
      } else {
         return null;
      }
   }
}
