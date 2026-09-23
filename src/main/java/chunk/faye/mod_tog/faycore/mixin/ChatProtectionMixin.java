package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.config.CrashProtectionConfig;
import chunk.faye.mod_tog.faycore.protection.TextLimiter;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public class ChatProtectionMixin {
   @Inject(
      method = {"handleSystemChat"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void faycore$protectSystemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
      if (CrashProtectionConfig.enableChatLimit) {
         try {
            Component message = packet.content();
            if (!TextLimiter.check(message, CrashProtectionConfig.maxSystemMessageLength)) {
               ci.cancel();
            }
         } catch (Throwable var4) {
            ci.cancel();
         }
      }
   }
}
