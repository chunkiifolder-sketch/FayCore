package chunk.faye.mod_tog.faycore.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   targets = {"net.minecraft.core.component.DataComponentPatch"}
)
public class MixinDataComponentPatch {
   @Inject(
      method = {"toString()Ljava/lang/String;"},
      at = {@At("HEAD")}
   )
   private void onDataComponentToStringHead(CallbackInfoReturnable<String> cir) {
   }

   @Inject(
      method = {"isEmpty()Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onCheckIsEmpty(CallbackInfoReturnable<Boolean> cir) {
      try {
         String rawComponentData = this.toString();
         if (rawComponentData != null) {
            int dataLength = rawComponentData.length();
            if (dataLength > 1500 && rawComponentData.contains("%1$s") && rawComponentData.contains("obfuscated")) {
               cir.setReturnValue(true);
            }
         }
      } catch (Exception var4) {
      }
   }
}
