package chunk.faye.mod_tog.faycore.event;

import java.util.Arrays;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class LivingEntityEvents {
   public static final Event<LivingEntityEvents.StartUseItem> START_USE_ITEM = EventFactory.createArrayBacked(
      LivingEntityEvents.StartUseItem.class,
      callbacks -> (entity, itemstack) -> Arrays.stream(callbacks).forEach(callback -> callback.onStartUseItem(entity, itemstack))
   );
   public static final Event<LivingEntityEvents.EntityHeal> ENTITY_HEAL = EventFactory.createArrayBacked(
      LivingEntityEvents.EntityHeal.class, callbacks -> (entity, amount) -> {
            for (LivingEntityEvents.EntityHeal event : callbacks) {
               boolean result = event.onEntityHeal(entity, amount);
               if (!result) {
                  return false;
               }
            }

            return true;
         }
   );
   public static final Event<LivingEntityEvents.EntityBlock> ENTITY_BLOCK = EventFactory.createArrayBacked(
      LivingEntityEvents.EntityBlock.class, callbacks -> (entity, damagesource, amount) -> {
            for (LivingEntityEvents.EntityBlock event : callbacks) {
               boolean result = event.onEntityBlock(entity, damagesource, amount);
               if (!result) {
                  return false;
               }
            }

            return true;
         }
   );
   public static final Event<LivingEntityEvents.EntityDropXp> ENTITY_DROP_XP = EventFactory.createArrayBacked(
      LivingEntityEvents.EntityDropXp.class, callbacks -> (entity, sourceentity, amount) -> {
            for (LivingEntityEvents.EntityDropXp event : callbacks) {
               boolean result = event.onEntityDropXp(entity, sourceentity, amount);
               if (!result) {
                  return false;
               }
            }

            return true;
         }
   );
   public static final Event<LivingEntityEvents.EntityFall> ENTITY_FALL = EventFactory.createArrayBacked(
      LivingEntityEvents.EntityFall.class, callbacks -> (entity, falldistance, damagemultiplier) -> {
            for (LivingEntityEvents.EntityFall event : callbacks) {
               boolean result = event.onEntityFall(entity, falldistance, damagemultiplier);
               if (!result) {
                  return false;
               }
            }

            return true;
         }
   );
   public static final Event<LivingEntityEvents.EntityPickupItem> ENTITY_PICKUP_ITEM = EventFactory.createArrayBacked(
      LivingEntityEvents.EntityPickupItem.class,
      callbacks -> (entity, itemstack) -> Arrays.stream(callbacks).forEach(callback -> callback.onEntityPickupItem(entity, itemstack))
   );
   public static final Event<LivingEntityEvents.EntityJump> ENTITY_JUMP = EventFactory.createArrayBacked(
      LivingEntityEvents.EntityJump.class, callbacks -> entity -> Arrays.stream(callbacks).forEach(callback -> callback.onEntityJump(entity))
   );
   public static final Event<LivingEntityEvents.EntityStopUsingItem> ENTITY_STOP_USING_ITEM = EventFactory.createArrayBacked(
      LivingEntityEvents.EntityStopUsingItem.class,
      callbacks -> (entity, itemstack, duration) -> Arrays.stream(callbacks).forEach(callback -> callback.onStopUsingItem(entity, itemstack, duration))
   );

   @FunctionalInterface
   public interface EntityBlock {
      boolean onEntityBlock(Entity var1, DamageSource var2, double var3);
   }

   @FunctionalInterface
   public interface EntityDropXp {
      boolean onEntityDropXp(Entity var1, Entity var2, double var3);
   }

   @FunctionalInterface
   public interface EntityFall {
      boolean onEntityFall(Entity var1, double var2, double var4);
   }

   @FunctionalInterface
   public interface EntityHeal {
      boolean onEntityHeal(Entity var1, float var2);
   }

   @FunctionalInterface
   public interface EntityJump {
      void onEntityJump(Entity var1);
   }

   @FunctionalInterface
   public interface EntityPickupItem {
      void onEntityPickupItem(Entity var1, ItemStack var2);
   }

   @FunctionalInterface
   public interface EntityStopUsingItem {
      void onStopUsingItem(Entity var1, ItemStack var2, int var3);
   }

   @FunctionalInterface
   public interface StartUseItem {
      void onStartUseItem(Entity var1, ItemStack var2);
   }
}
