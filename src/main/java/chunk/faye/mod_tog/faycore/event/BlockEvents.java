package chunk.faye.mod_tog.faycore.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEvents {
   public static final Event<BlockEvents.BlockMultiplace> BLOCK_MULTIPLACE = EventFactory.createArrayBacked(
      BlockEvents.BlockMultiplace.class, callbacks -> (position, entity, placed, placedAgainst) -> {
            for (BlockEvents.BlockMultiplace event : callbacks) {
               boolean result = event.onMultiplaced(position, entity, placed, placedAgainst);
               if (!result) {
                  return false;
               }
            }

            return true;
         }
   );
   public static final Event<BlockEvents.BlockPlace> BLOCK_PLACE = EventFactory.createArrayBacked(
      BlockEvents.BlockPlace.class, callbacks -> (position, entity, placed, placedAgainst) -> {
            for (BlockEvents.BlockPlace event : callbacks) {
               boolean result = event.onBlockPlaced(position, entity, placed, placedAgainst);
               if (!result) {
                  return false;
               }
            }

            return true;
         }
   );

   @FunctionalInterface
   public interface BlockMultiplace {
      boolean onMultiplaced(BlockPos var1, Entity var2, BlockState var3, BlockState var4);
   }

   @FunctionalInterface
   public interface BlockPlace {
      boolean onBlockPlaced(BlockPos var1, Entity var2, BlockState var3, BlockState var4);
   }
}
