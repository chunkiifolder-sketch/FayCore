/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.event.Event
 *  net.fabricmc.fabric.api.event.EventFactory
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.state.BlockState
 */
package net.mcreator.faycore.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ItemEvents {
    public static final Event<BonemealUsed> BONEMEAL_USED = EventFactory.createArrayBacked(BonemealUsed.class, callbacks -> (position, entity, itemstack, blockstate) -> {
        for (BonemealUsed event : callbacks) {
            boolean result = event.onBonemealUsed(position, entity, itemstack, blockstate);
            if (result) continue;
            return false;
        }
        return true;
    });

    @FunctionalInterface
    public static interface BonemealUsed {
        public boolean onBonemealUsed(BlockPos var1, Entity var2, ItemStack var3, BlockState var4);
    }
}

