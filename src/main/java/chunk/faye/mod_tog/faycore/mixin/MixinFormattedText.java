package chunk.faye.mod_tog.faycore.mixin;

import net.minecraft.network.chat.FormattedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({FormattedText.class})
public interface MixinFormattedText {
   @Inject(
      method = {"getString()Ljava/lang/String;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetString(CallbackInfoReturnable<String> cir) {
      try {
         String rawText = this.toString();
         if (rawText != null) {
            int length = rawText.length();
            if (length > 2500) {
               cir.setReturnValue("§c[FayCore] 該物品包含超長危險文本，已自動隔離保護");
            }
         }
      } catch (Exception var4) {
      }
   }
}
