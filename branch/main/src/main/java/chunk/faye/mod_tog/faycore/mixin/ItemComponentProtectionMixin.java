/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
 *  net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.protection.ComponentLimiter;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public class ItemComponentProtectionMixin {
    @Inject(method={"handleContainerSetSlot"}, at={@At(value="HEAD")}, cancellable=true)
    private void faycore$protectSlot(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        if (!ComponentLimiter.safe(packet.getItem())) {
            ci.cancel();
        }
    }

    @Inject(method={"handleContainerContent"}, at={@At(value="HEAD")}, cancellable=true)
    private void faycore$protectContent(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        for (ItemStack stack : packet.items()) {
            if (ComponentLimiter.safe(stack)) continue;
            ci.cancel();
            return;
        }
    }
}

