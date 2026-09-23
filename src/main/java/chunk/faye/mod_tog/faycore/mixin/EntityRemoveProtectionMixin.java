package chunk.faye.mod_tog.faycore.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity.RemovalReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientLevel.class})
public class EntityRemoveProtectionMixin {
   @Inject(
      method = {"removeEntity"},
      at = {@At("HEAD")}
   )
   private void faycore$resetEntity(int id, RemovalReason reason, CallbackInfo ci) {
   }
}
