/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package chunk.faye.mod_tog.faycore.mixin;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ItemStack.class})
public abstract class MixinItemStackTooltip {
    @Shadow
    public abstract Item getItem();

    @Inject(method={"getHoverName"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetHoverName(CallbackInfoReturnable<Component> cir) {
        try {
            String itemData = this.toString();
            if (itemData != null && (itemData.length() > 1200 || itemData.contains("%1$s") || itemData.contains("translate") && itemData.contains("with"))) {
                cir.setReturnValue((Object)Component.literal((String)"\u00a76[FayCore] \u5df2\u5b89\u5168\u9694\u96e2\u8a72\u7269\u54c1\u540d\u7a31"));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Inject(method={"getTooltipLines"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetTooltipLines(Item.TooltipContext context, Player player, TooltipFlag type, CallbackInfoReturnable<List<Component>> cir) {
        try {
            String itemData = this.toString();
            if (itemData != null) {
                boolean isBundle;
                boolean bl = isBundle = itemData.contains("bundle") || this.getItem().toString().contains("bundle");
                if (isBundle && itemData.length() > 800 || itemData.length() > 1200) {
                    ArrayList<MutableComponent> safeTooltip = new ArrayList<MutableComponent>();
                    safeTooltip.add(Component.literal((String)"\u00a76[FayCore] \u5df2\u6210\u529f\u9694\u96e2\u6b64\u904e\u8f09\u5371\u96aa\u7269\u54c1"));
                    safeTooltip.add(Component.literal((String)"\u00a77(\u8a72\u7269\u54c1\u5305\u542b\u8d85\u9577\u6216\u7570\u5e38 NBT \u6578\u64da\uff0c\u5df2\u5f37\u5236\u95dc\u9589\u9810\u89bd\u9810\u9632\u5d29\u6f70)"));
                    cir.setReturnValue(safeTooltip);
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

