package chunk.faye.mod_tog.faycore.mixin;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ItemStack.class})
public abstract class MixinItemStackTooltip {
   @Shadow
   public abstract Item getItem();

   @Inject(
      method = {"getHoverName"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetHoverName(CallbackInfoReturnable<Component> cir) {
      try {
         String itemData = this.toString();
         if (itemData != null && (itemData.length() > 1200 || itemData.contains("%1$s") || itemData.contains("translate") && itemData.contains("with"))) {
            cir.setReturnValue(Component.literal("§6[FayCore] 已安全隔離該物品名稱"));
         }
      } catch (Exception var3) {
      }
   }

   @Inject(
      method = {"getTooltipLines"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetTooltipLines(TooltipContext context, Player player, TooltipFlag type, CallbackInfoReturnable<List<Component>> cir) {
      try {
         String itemData = this.toString();
         if (itemData != null) {
            boolean isBundle = itemData.contains("bundle") || this.getItem().toString().contains("bundle");
            if (isBundle && itemData.length() > 800 || itemData.length() > 1200) {
               List<Component> safeTooltip = new ArrayList<>();
               safeTooltip.add(Component.literal("§6[FayCore] 已成功隔離此過載危險物品"));
               safeTooltip.add(Component.literal("§7(該物品包含超長或異常 NBT 數據，已強制關閉預覽預防崩潰)"));
               cir.setReturnValue(safeTooltip);
            }
         }
      } catch (Exception var8) {
      }
   }
}
