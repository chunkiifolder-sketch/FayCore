package chunk.faye.mod_tog.faycore.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   targets = {"net.minecraft.client.renderer.entity.LivingEntityRenderer"}
)
public class AntiUpsideDownCrashMixin {
   @Inject(
      method = {"isEntityUpsideDown(Lnet/minecraft/world/entity/LivingEntity;)Z"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = true
   )
   private static void faycore$stopNbtReading(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
      cir.setReturnValue(false);
   }
}
