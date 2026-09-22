/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.event.Event
 *  net.fabricmc.fabric.api.event.EventFactory
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.ItemStack
 */
package net.mcreator.faycore.event;

import java.util.Arrays;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class LivingEntityEvents {
    public static final Event<StartUseItem> START_USE_ITEM = EventFactory.createArrayBacked(StartUseItem.class, callbacks -> (entity, itemstack) -> Arrays.stream(callbacks).forEach(callback -> callback.onStartUseItem(entity, itemstack)));
    public static final Event<EntityHeal> ENTITY_HEAL = EventFactory.createArrayBacked(EntityHeal.class, callbacks -> (entity, amount) -> {
        for (EntityHeal event : callbacks) {
            boolean result = event.onEntityHeal(entity, amount);
            if (result) continue;
            return false;
        }
        return true;
    });
    public static final Event<EntityBlock> ENTITY_BLOCK = EventFactory.createArrayBacked(EntityBlock.class, callbacks -> (entity, damagesource, amount) -> {
        for (EntityBlock event : callbacks) {
            boolean result = event.onEntityBlock(entity, damagesource, amount);
            if (result) continue;
            return false;
        }
        return true;
    });
    public static final Event<EntityDropXp> ENTITY_DROP_XP = EventFactory.createArrayBacked(EntityDropXp.class, callbacks -> (entity, sourceentity, amount) -> {
        for (EntityDropXp event : callbacks) {
            boolean result = event.onEntityDropXp(entity, sourceentity, amount);
            if (result) continue;
            return false;
        }
        return true;
    });
    public static final Event<EntityFall> ENTITY_FALL = EventFactory.createArrayBacked(EntityFall.class, callbacks -> (entity, falldistance, damagemultiplier) -> {
        for (EntityFall event : callbacks) {
            boolean result = event.onEntityFall(entity, falldistance, damagemultiplier);
            if (result) continue;
            return false;
        }
        return true;
    });
    public static final Event<EntityPickupItem> ENTITY_PICKUP_ITEM = EventFactory.createArrayBacked(EntityPickupItem.class, callbacks -> (entity, itemstack) -> Arrays.stream(callbacks).forEach(callback -> callback.onEntityPickupItem(entity, itemstack)));
    public static final Event<EntityJump> ENTITY_JUMP = EventFactory.createArrayBacked(EntityJump.class, callbacks -> entity -> Arrays.stream(callbacks).forEach(callback -> callback.onEntityJump(entity)));
    public static final Event<EntityStopUsingItem> ENTITY_STOP_USING_ITEM = EventFactory.createArrayBacked(EntityStopUsingItem.class, callbacks -> (entity, itemstack, duration) -> Arrays.stream(callbacks).forEach(callback -> callback.onStopUsingItem(entity, itemstack, duration)));

    @FunctionalInterface
    public static interface EntityStopUsingItem {
        public void onStopUsingItem(Entity var1, ItemStack var2, int var3);
    }

    @FunctionalInterface
    public static interface EntityJump {
        public void onEntityJump(Entity var1);
    }

    @FunctionalInterface
    public static interface EntityPickupItem {
        public void onEntityPickupItem(Entity var1, ItemStack var2);
    }

    @FunctionalInterface
    public static interface EntityFall {
        public boolean onEntityFall(Entity var1, double var2, double var4);
    }

    @FunctionalInterface
    public static interface EntityDropXp {
        public boolean onEntityDropXp(Entity var1, Entity var2, double var3);
    }

    @FunctionalInterface
    public static interface EntityBlock {
        public boolean onEntityBlock(Entity var1, DamageSource var2, double var3);
    }

    @FunctionalInterface
    public static interface EntityHeal {
        public boolean onEntityHeal(Entity var1, float var2);
    }

    @FunctionalInterface
    public static interface StartUseItem {
        public void onStartUseItem(Entity var1, ItemStack var2);
    }
}

