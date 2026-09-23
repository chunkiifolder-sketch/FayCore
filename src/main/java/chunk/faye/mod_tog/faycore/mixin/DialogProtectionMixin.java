package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.config.CrashProtectionConfig;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientboundShowDialogPacket.class})
public class DialogProtectionMixin {
   @Inject(
      method = {"handle"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void faycore$blockDialog(ClientCommonPacketListener listener, CallbackInfo ci) {
      if (CrashProtectionConfig.enableDialogLimit) {
         ci.cancel();
      }
   }
}
