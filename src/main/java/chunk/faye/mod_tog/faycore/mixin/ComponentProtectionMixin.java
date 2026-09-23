package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.protection.ComponentLimiter;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Component.class})
public interface ComponentProtectionMixin {
   @Inject(
      method = {"getString"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void faycore$protectComponent(CallbackInfoReturnable<String> cir) {
      Component self = (Component)this;
      if (!ComponentLimiter.safe(self)) {
         cir.setReturnValue("[Blocked Component]");
      }
   }
}
