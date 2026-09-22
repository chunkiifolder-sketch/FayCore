/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.mcreator.faycore.mixin;

import net.mcreator.faycore.config.CrashProtectionConfig;
import net.mcreator.faycore.protection.NbtLimiter;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public class BlockEntityProtectionMixin {
    @Inject(method={"handleBlockEntityData"}, at={@At(value="HEAD")}, cancellable=true)
    private void faycore$protectBlockEntity(ClientboundBlockEntityDataPacket packet, CallbackInfo ci) {
        if (!CrashProtectionConfig.enableNbtLimit) {
            return;
        }
        try {
            CompoundTag tag = packet.getTag();
            if (tag != null && !NbtLimiter.safe((Tag)tag)) {
                ci.cancel();
            }
        }
        catch (Throwable e) {
            ci.cancel();
        }
    }
}

