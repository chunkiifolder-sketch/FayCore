package chunk.faye.mod_tog.faycore.protection;

import net.minecraft.world.entity.EntityType;

public final class EntityTypeLimiter {
   private EntityTypeLimiter() {
   }

   public static boolean allow(EntityType<?> type) {
      if (type == EntityType.ARMOR_STAND) {
         return true;
      } else if (type == EntityType.ITEM) {
         return true;
      } else {
         return type == EntityType.EXPERIENCE_ORB ? true : true;
      }
   }
}
