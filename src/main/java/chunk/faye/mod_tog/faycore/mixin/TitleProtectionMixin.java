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

@Mixin({ClientPacketListener.class})
public class TitleProtectionMixin {
   @Inject(
      method = {"setTitleText"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void faycore$protectTitle(ClientboundSetTitleTextPacket packet, CallbackInfo ci) {
      if (CrashProtectionConfig.enableTextLimit) {
         try {
            if (!TextLimiter.check(packet.text(), CrashProtectionConfig.maxTitleLength)) {
               ci.cancel();
            }
         } catch (Throwable var4) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"setSubtitleText"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void faycore$protectSubtitle(ClientboundSetSubtitleTextPacket packet, CallbackInfo ci) {
      if (CrashProtectionConfig.enableTextLimit) {
         try {
            if (!TextLimiter.check(packet.text(), CrashProtectionConfig.maxTitleLength)) {
               ci.cancel();
            }
         } catch (Throwable var4) {
            ci.cancel();
         }
      }
   }
}
