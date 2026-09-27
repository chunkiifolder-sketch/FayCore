package chunk.faye.mod_tog.faycore.protection;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public final class EntityTypeLimiter {
   private EntityTypeLimiter() {
   }

   public static boolean allow(EntityType<?> type) {
      if (type == EntityTypes.ARMOR_STAND) {
         return true;
      } else if (type == EntityTypes.ITEM) {
         return true;
      } else {
         return type == EntityTypes.EXPERIENCE_ORB ? true : true;
      }
   }
}
