/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.LevelAccessor
 */
package net.mcreator.faycore.procedures;

import net.mcreator.faycore.network.FaycoreModVariables;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;

public class SetCorePositionProcedure {
    public static boolean eventResult = true;

    public static void execute(LevelAccessor world, Entity entity) {
        if (entity == null) {
            return;
        }
        if (entity instanceof Player) {
            Player _player = (Player)entity;
            _player.containerMenu = _player.inventoryMenu;
        }
        if (!FaycoreModVariables.MapVariables.get((LevelAccessor)world).CorePreview) {
            FaycoreModVariables.MapVariables.get((LevelAccessor)world).CorePreview = true;
            FaycoreModVariables.MapVariables.get(world).markSyncDirty();
        } else {
            FaycoreModVariables.MapVariables.get((LevelAccessor)world).CorePreview = false;
            FaycoreModVariables.MapVariables.get(world).markSyncDirty();
        }
    }
}

