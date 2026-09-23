package chunk.faye.mod_tog.faycore.procedures;

import chunk.faye.mod_tog.faycore.network.FaycoreModVariables;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;

public class SetCorePositionProcedure {
   public static boolean eventResult = true;

   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         if (entity instanceof Player _player) {
            _player.containerMenu = _player.inventoryMenu;
         }

         if (!FaycoreModVariables.MapVariables.get(world).CorePreview) {
            FaycoreModVariables.MapVariables.get(world).CorePreview = true;
            FaycoreModVariables.MapVariables.get(world).markSyncDirty();
         } else {
            FaycoreModVariables.MapVariables.get(world).CorePreview = false;
            FaycoreModVariables.MapVariables.get(world).markSyncDirty();
         }
      }
   }
}
