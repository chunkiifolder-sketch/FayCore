/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.ExperienceOrb
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.event.PlayerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ExperienceOrb.class})
public abstract class ExperienceOrbMixin {
    @Inject(method={"playerTouch(Lnet/minecraft/world/entity/player/Player;)V"}, at={@At(value="HEAD")}, cancellable=true)
    public void playerTouch(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            if (serverPlayer.takeXpDelay == 0 && !((PlayerEvents.PickupXp)PlayerEvents.PICKUP_XP.invoker()).onPickupXp((Entity)serverPlayer)) {
                ci.cancel();
            }
        }
    }
}

