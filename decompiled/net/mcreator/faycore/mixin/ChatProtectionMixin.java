/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.game.ClientboundSystemChatPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.mcreator.faycore.mixin;

import net.mcreator.faycore.config.CrashProtectionConfig;
import net.mcreator.faycore.protection.TextLimiter;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public class ChatProtectionMixin {
    @Inject(method={"handleSystemChat"}, at={@At(value="HEAD")}, cancellable=true)
    private void faycore$protectSystemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        if (!CrashProtectionConfig.enableChatLimit) {
            return;
        }
        try {
            Component message = packet.content();
            if (!TextLimiter.check(message, CrashProtectionConfig.maxSystemMessageLength)) {
                ci.cancel();
            }
        }
        catch (Throwable throwable) {
            ci.cancel();
        }
    }
}

