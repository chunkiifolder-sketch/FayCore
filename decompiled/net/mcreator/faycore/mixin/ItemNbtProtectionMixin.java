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
package net.mcreator.faycore.mixin;

import net.mcreator.faycore.config.CrashProtectionConfig;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public class ItemNbtProtectionMixin {
    @Inject(method={"handleContainerSetSlot"}, at={@At(value="HEAD")}, cancellable=true)
    private void faycore$protectSlot(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        if (!CrashProtectionConfig.enableNbtLimit) {
            return;
        }
        try {
            ItemStack stack = packet.getItem();
            if (!this.checkItem(stack)) {
                ci.cancel();
            }
        }
        catch (Throwable e) {
            ci.cancel();
        }
    }

    @Inject(method={"handleContainerContent"}, at={@At(value="HEAD")}, cancellable=true)
    private void faycore$protectContent(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        if (!CrashProtectionConfig.enableNbtLimit) {
            return;
        }
        try {
            for (ItemStack stack : packet.items()) {
                if (this.checkItem(stack)) continue;
                ci.cancel();
                return;
            }
            if (!this.checkItem(packet.carriedItem())) {
                ci.cancel();
            }
        }
        catch (Throwable e) {
            ci.cancel();
        }
    }

    private boolean checkItem(ItemStack stack) {
        try {
            if (stack == null || stack.isEmpty()) {
                return true;
            }
            String data = stack.toString();
            return data.length() <= CrashProtectionConfig.maxNbtStringLength;
        }
        catch (Throwable e) {
            return false;
        }
    }
}

