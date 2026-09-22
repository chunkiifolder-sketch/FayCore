/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.event.Event
 *  net.fabricmc.fabric.api.event.EventFactory
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 */
package chunk.faye.mod_tog.faycore.event;

import java.util.Arrays;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class PlayerEvents {
    public static final Event<TickEnd> END_PLAYER_TICK = EventFactory.createArrayBacked(TickEnd.class, callbacks -> entity -> Arrays.stream(callbacks).forEach(callback -> callback.onEndTick(entity)));
    public static final Event<XPChange> XP_CHANGE = EventFactory.createArrayBacked(XPChange.class, callbacks -> (entity, amount) -> Arrays.stream(callbacks).forEach(callback -> callback.onXpChange(entity, amount)));
    public static final Event<LevelChange> LEVEL_CHANGE = EventFactory.createArrayBacked(LevelChange.class, callbacks -> (entity, amount) -> Arrays.stream(callbacks).forEach(callback -> callback.onLevelChange(entity, amount)));
    public static final Event<PickupXp> PICKUP_XP = EventFactory.createArrayBacked(PickupXp.class, callbacks -> entity -> {
        for (PickupXp event : callbacks) {
            boolean result = event.onPickupXp(entity);
            if (result) continue;
            return false;
        }
        return true;
    });

    @FunctionalInterface
    public static interface PickupXp {
        public boolean onPickupXp(Entity var1);
    }

    @FunctionalInterface
    public static interface LevelChange {
        public void onLevelChange(Player var1, int var2);
    }

    @FunctionalInterface
    public static interface XPChange {
        public void onXpChange(Player var1, int var2);
    }

    @FunctionalInterface
    public static interface TickEnd {
        public void onEndTick(Player var1);
    }
}

