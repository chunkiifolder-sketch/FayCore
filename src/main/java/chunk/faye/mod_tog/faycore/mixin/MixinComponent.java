package chunk.faye.mod_tog.faycore.mixin;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.FormattedText.StyledContentConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Component.class})
public interface MixinComponent {
   @Inject(
      method = {"visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onVisitStyled(StyledContentConsumer<?> consumer, Style style, CallbackInfoReturnable<Optional<?>> cir) {
      String fullStructure = this.toString();
      if (fullStructure != null && (fullStructure.length() > 3000 || fullStructure.contains("bundle") && fullStructure.contains("component"))) {
         cir.setReturnValue(Optional.empty());
      }
   }
}
