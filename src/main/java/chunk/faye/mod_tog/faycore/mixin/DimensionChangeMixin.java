/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.core.Holder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.game.ClientboundRespawnPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.config.CrashProtectionConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public class DimensionChangeMixin {
    @Unique
    private long faycore$dimensionWindowStart = 0L;
    @Unique
    private int faycore$dimensionChangeCount = 0;
    @Unique
    private Object faycore$lastDimension = null;

    @Inject(method={"handleRespawn"}, at={@At(value="HEAD")})
    private void faycore$checkDimensionChange(ClientboundRespawnPacket packet, CallbackInfo ci) {
        if (!CrashProtectionConfig.enableDimensionChangeLimit) {
            return;
        }
        Holder newDimension = packet.commonPlayerSpawnInfo().dimensionType();
        if (this.faycore$lastDimension != null && this.faycore$lastDimension.equals(newDimension)) {
            return;
        }
        this.faycore$lastDimension = newDimension;
        long now = System.currentTimeMillis();
        if (this.faycore$dimensionWindowStart == 0L || now - this.faycore$dimensionWindowStart > (long)CrashProtectionConfig.dimensionChangeWindowSeconds * 1000L) {
            this.faycore$dimensionWindowStart = now;
            this.faycore$dimensionChangeCount = 0;
        }
        ++this.faycore$dimensionChangeCount;
        if (this.faycore$dimensionChangeCount > CrashProtectionConfig.maxDimensionChanges) {
            Minecraft client = Minecraft.getInstance();
            if (client.getConnection() != null) {
                client.getConnection().getConnection().disconnect((Component)Component.literal((String)"FayCore: Too many dimension changes"));
            }
            this.faycore$dimensionChangeCount = 0;
            this.faycore$dimensionWindowStart = 0L;
        }
    }
}

