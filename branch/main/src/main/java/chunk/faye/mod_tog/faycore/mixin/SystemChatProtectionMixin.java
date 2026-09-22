/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.protocol.game.ClientboundSystemChatPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package chunk.faye.mod_tog.faycore.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public class SystemChatProtectionMixin {
    @Inject(method={"handleSystemChat"}, at={@At(value="HEAD")}, cancellable=true)
    private void faycore$blockCommandSet(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        String text = packet.content().getString().replace("\u00a7", "").toLowerCase();
        if (text.contains("command set") || text.contains("command set:") || text.contains("\u6307\u4ee4\u8a2d\u70ba")) {
            ci.cancel();
        }
    }
}

