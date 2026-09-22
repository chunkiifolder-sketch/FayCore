/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.BoneMealItem
 *  net.minecraft.world.item.context.UseOnContext
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.event.ItemEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={BoneMealItem.class})
public abstract class BoneMealItemMixin {
    @Inject(method={"useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"}, at={@At(value="HEAD")}, cancellable=true)
    public void useOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        boolean result;
        if (context.getLevel() instanceof ServerLevel && !(result = ((ItemEvents.BonemealUsed)ItemEvents.BONEMEAL_USED.invoker()).onBonemealUsed(context.getClickedPos(), (Entity)context.getPlayer(), context.getItemInHand(), context.getLevel().getBlockState(context.getClickedPos())))) {
            cir.setReturnValue((Object)InteractionResult.FAIL);
        }
    }
}

