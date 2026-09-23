package chunk.faye.mod_tog.faycore.mixin;

import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Style.class})
public class MixinStyle {
   @Inject(
      method = {"isObfuscated()Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onIsObfuscated(CallbackInfoReturnable<Boolean> cir) {
      try {
         String fullStyleData = this.toString();
         if (fullStyleData != null && fullStyleData.length() > 800) {
            cir.setReturnValue(false);
         }
      } catch (Exception var3) {
      }
   }

   @Inject(
      method = {"isEmpty()Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onIsEmpty(CallbackInfoReturnable<Boolean> cir) {
      try {
         String fullStyleData = this.toString();
         if (fullStyleData != null && fullStyleData.length() > 1500) {
            cir.setReturnValue(true);
         }
      } catch (Exception var3) {
      }
   }
}
