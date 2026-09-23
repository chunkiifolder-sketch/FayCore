package chunk.faye.mod_tog.faycore.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   targets = {"net.minecraft.client.ClientBrandRetriever"}
)
public class CustomClient {
   @Inject(
      at = {@At("HEAD")},
      method = {"getClientModName"},
      cancellable = true,
      remap = false
   )
   private static void spoofBrand(CallbackInfoReturnable<String> cir) {
      cir.setReturnValue("FayCore Client");
   }
}
