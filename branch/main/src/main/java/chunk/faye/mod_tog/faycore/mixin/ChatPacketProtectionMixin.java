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
package chunk.faye.mod_tog.faycore.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public class ChatPacketProtectionMixin {
    @Inject(method={"handleSystemChat"}, at={@At(value="HEAD")}, cancellable=true)
    private void faycore$blockSystemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        Component msg = packet.content();
        if (msg == null) {
            return;
        }
        String text = msg.getString().toLowerCase();
        if (text.contains("\u6307\u4ee4\u8a2d\u70ba") || text.contains("command set")) {
            ci.cancel();
        }
    }
}

