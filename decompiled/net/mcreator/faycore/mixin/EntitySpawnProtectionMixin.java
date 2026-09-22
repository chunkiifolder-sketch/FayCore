/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.protocol.game.ClientboundAddEntityPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.mcreator.faycore.mixin;

import net.mcreator.faycore.config.CrashProtectionConfig;
import net.mcreator.faycore.tracker.EntityTracker;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public class EntitySpawnProtectionMixin {
    @Inject(method={"handleAddEntity"}, at={@At(value="HEAD")}, cancellable=true)
    private void faycore$limitEntities(ClientboundAddEntityPacket packet, CallbackInfo ci) {
        if (!CrashProtectionConfig.enableEntityLimit) {
            return;
        }
        try {
            if (EntityTracker.count() >= CrashProtectionConfig.maxEntities) {
                ci.cancel();
                return;
            }
        }
        catch (Throwable throwable) {
            ci.cancel();
        }
    }
}

