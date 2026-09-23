package chunk.faye.mod_tog.faycore.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public class ChatPacketProtectionMixin {
   @Inject(
      method = {"handleSystemChat"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void faycore$blockSystemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
      Component msg = packet.content();
      if (msg != null) {
         String text = msg.getString().toLowerCase();
         if (text.contains("指令設為") || text.contains("command set")) {
            ci.cancel();
         }
      }
   }
}
