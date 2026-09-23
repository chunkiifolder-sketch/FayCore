package chunk.faye.mod_tog.faycore.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public class SystemChatProtectionMixin {
   @Inject(
      method = {"handleSystemChat"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void faycore$blockCommandSet(ClientboundSystemChatPacket packet, CallbackInfo ci) {
      String text = packet.content().getString().replace("§", "").toLowerCase();
      if (text.contains("command set") || text.contains("command set:") || text.contains("指令設為")) {
         ci.cancel();
      }
   }
}
