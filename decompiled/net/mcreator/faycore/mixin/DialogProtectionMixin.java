/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.common.ClientCommonPacketListener
 *  net.minecraft.network.protocol.common.ClientboundShowDialogPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.mcreator.faycore.mixin;

import net.mcreator.faycore.config.CrashProtectionConfig;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientboundShowDialogPacket.class})
public class DialogProtectionMixin {
    @Inject(method={"handle"}, at={@At(value="HEAD")}, cancellable=true)
    private void faycore$blockDialog(ClientCommonPacketListener listener, CallbackInfo ci) {
        if (!CrashProtectionConfig.enableDialogLimit) {
            return;
        }
        ci.cancel();
    }
}

