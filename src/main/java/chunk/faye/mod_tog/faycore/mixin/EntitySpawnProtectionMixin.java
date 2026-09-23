package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.config.CrashProtectionConfig;
import chunk.faye.mod_tog.faycore.tracker.EntityTracker;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public class EntitySpawnProtectionMixin {
   @Inject(
      method = {"handleAddEntity"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void faycore$limitEntities(ClientboundAddEntityPacket packet, CallbackInfo ci) {
      if (CrashProtectionConfig.enableEntityLimit) {
         try {
            if (EntityTracker.count() >= CrashProtectionConfig.maxEntities) {
               ci.cancel();
               return;
            }
         } catch (Throwable var4) {
            ci.cancel();
         }
      }
   }
}
