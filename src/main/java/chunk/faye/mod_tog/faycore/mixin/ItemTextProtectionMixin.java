package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.config.CrashProtectionConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ItemStack.class})
public class ItemTextProtectionMixin {
   @Inject(
      method = {"getHoverName"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void faycore$limitItemName(CallbackInfoReturnable<Component> cir) {
      if (CrashProtectionConfig.enableItemTextLimit) {
         Component text = (Component)cir.getReturnValue();
         if (text != null && text.getString().length() > CrashProtectionConfig.maxItemTextLength) {
            cir.setReturnValue(Component.literal("[Blocked Item Name]"));
         }
      }
   }
}
