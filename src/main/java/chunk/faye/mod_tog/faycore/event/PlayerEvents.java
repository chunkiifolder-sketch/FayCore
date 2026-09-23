package chunk.faye.mod_tog.faycore.event;

import java.util.Arrays;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class PlayerEvents {
   public static final Event<PlayerEvents.TickEnd> END_PLAYER_TICK = EventFactory.createArrayBacked(
      PlayerEvents.TickEnd.class, callbacks -> entity -> Arrays.stream(callbacks).forEach(callback -> callback.onEndTick(entity))
   );
   public static final Event<PlayerEvents.XPChange> XP_CHANGE = EventFactory.createArrayBacked(
      PlayerEvents.XPChange.class, callbacks -> (entity, amount) -> Arrays.stream(callbacks).forEach(callback -> callback.onXpChange(entity, amount))
   );
   public static final Event<PlayerEvents.LevelChange> LEVEL_CHANGE = EventFactory.createArrayBacked(
      PlayerEvents.LevelChange.class, callbacks -> (entity, amount) -> Arrays.stream(callbacks).forEach(callback -> callback.onLevelChange(entity, amount))
   );
   public static final Event<PlayerEvents.PickupXp> PICKUP_XP = EventFactory.createArrayBacked(PlayerEvents.PickupXp.class, callbacks -> entity -> {
         for (PlayerEvents.PickupXp event : callbacks) {
            boolean result = event.onPickupXp(entity);
            if (!result) {
               return false;
            }
         }

         return true;
      });

   @FunctionalInterface
   public interface LevelChange {
      void onLevelChange(Player var1, int var2);
   }

   @FunctionalInterface
   public interface PickupXp {
      boolean onPickupXp(Entity var1);
   }

   @FunctionalInterface
   public interface TickEnd {
      void onEndTick(Player var1);
   }

   @FunctionalInterface
   public interface XPChange {
      void onXpChange(Player var1, int var2);
   }
}
