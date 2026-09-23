package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.protection.ParticleLimiter;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public class ParticleProtectionMixin {
   @Inject(
      method = {"handleParticleEvent"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void faycore$limitParticles(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
      if (!ParticleLimiter.allow()) {
         ci.cancel();
      }
   }
}
