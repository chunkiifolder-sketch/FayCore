/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.event.Event
 *  net.fabricmc.fabric.api.event.EventFactory
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.block.state.BlockState
 */
package net.mcreator.faycore.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEvents {
    public static final Event<BlockMultiplace> BLOCK_MULTIPLACE = EventFactory.createArrayBacked(BlockMultiplace.class, callbacks -> (position, entity, placed, placedAgainst) -> {
        for (BlockMultiplace event : callbacks) {
            boolean result = event.onMultiplaced(position, entity, placed, placedAgainst);
            if (result) continue;
            return false;
        }
        return true;
    });
    public static final Event<BlockPlace> BLOCK_PLACE = EventFactory.createArrayBacked(BlockPlace.class, callbacks -> (position, entity, placed, placedAgainst) -> {
        for (BlockPlace event : callbacks) {
            boolean result = event.onBlockPlaced(position, entity, placed, placedAgainst);
            if (result) continue;
            return false;
        }
        return true;
    });

    @FunctionalInterface
    public static interface BlockPlace {
        public boolean onBlockPlaced(BlockPos var1, Entity var2, BlockState var3, BlockState var4);
    }

    @FunctionalInterface
    public static interface BlockMultiplace {
        public boolean onMultiplaced(BlockPos var1, Entity var2, BlockState var3, BlockState var4);
    }
}

