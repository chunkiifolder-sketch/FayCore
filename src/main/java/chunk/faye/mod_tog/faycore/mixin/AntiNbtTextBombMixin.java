package chunk.faye.mod_tog.faycore.mixin;

import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Component.class})
public interface AntiNbtTextBombMixin {
   @Inject(
      method = {"getString()Ljava/lang/String;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void faycore$interceptTextBomb(CallbackInfoReturnable<String> cir) {
      Component self = (Component)this;

      try {
         String raw = self.getContents().toString();
         if (raw.length() > 2000) {
            cir.setReturnValue("§c[攔截惡意文本]");
         }
      } catch (Throwable var4) {
         cir.setReturnValue("§c[攔截安全屏蔽]");
      }
   }
}
