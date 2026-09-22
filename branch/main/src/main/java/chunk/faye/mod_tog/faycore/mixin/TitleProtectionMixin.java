/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
 *  net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.config.CrashProtectionConfig;
import chunk.faye.mod_tog.faycore.protection.TextLimiter;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public class TitleProtectionMixin {
    @Inject(method={"setTitleText"}, at={@At(value="HEAD")}, cancellable=true)
    private void faycore$protectTitle(ClientboundSetTitleTextPacket packet, CallbackInfo ci) {
        if (!CrashProtectionConfig.enableTextLimit) {
            return;
        }
        try {
            if (!TextLimiter.check(packet.text(), CrashProtectionConfig.maxTitleLength)) {
                ci.cancel();
            }
        }
        catch (Throwable throwable) {
            ci.cancel();
        }
    }

    @Inject(method={"setSubtitleText"}, at={@At(value="HEAD")}, cancellable=true)
    private void faycore$protectSubtitle(ClientboundSetSubtitleTextPacket packet, CallbackInfo ci) {
        if (!CrashProtectionConfig.enableTextLimit) {
            return;
        }
        try {
            if (!TextLimiter.check(packet.text(), CrashProtectionConfig.maxTitleLength)) {
                ci.cancel();
            }
        }
        catch (Throwable throwable) {
            ci.cancel();
        }
    }
}

