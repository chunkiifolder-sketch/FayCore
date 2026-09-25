package chunk.faye.mod_tog.faycore.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

import chunk.faye.mod_tog.faycore.network.FaycoreModVariables;

public class SetCorePositionProcedure {
	public static boolean eventResult = true;

	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof Player _player)
			_player.containerMenu = _player.inventoryMenu;
		if (FaycoreModVariables.MapVariables.get(world).CorePreview == false) {
			FaycoreModVariables.MapVariables.get(world).CorePreview = true;
			FaycoreModVariables.MapVariables.get(world).markSyncDirty();
		} else {
			FaycoreModVariables.MapVariables.get(world).CorePreview = false;
			FaycoreModVariables.MapVariables.get(world).markSyncDirty();
		}
	}
}