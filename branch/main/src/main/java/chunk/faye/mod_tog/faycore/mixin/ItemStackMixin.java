/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.block.state.BlockState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.event.BlockEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ItemStack.class})
public abstract class ItemStackMixin {
    @Inject(method={"useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"}, at={@At(value="TAIL")}, cancellable=true)
    public void useOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        boolean result;
        Player player = context.getPlayer();
        ItemStack copy = context.getItemInHand().copy();
        BlockState placedAgainst = player.level().getBlockState(context.getClickedPos().relative(context.getClickedFace()));
        if (!player.level().isEmptyBlock(context.getClickedPos().relative(context.getClickedFace())) && !(result = ((BlockEvents.BlockMultiplace)BlockEvents.BLOCK_MULTIPLACE.invoker()).onMultiplaced(context.getClickedPos().relative(context.getClickedFace()), (Entity)player, placedAgainst, player.level().getBlockState(context.getClickedPos())))) {
            cir.setReturnValue((Object)InteractionResult.FAIL);
        }
    }
}

