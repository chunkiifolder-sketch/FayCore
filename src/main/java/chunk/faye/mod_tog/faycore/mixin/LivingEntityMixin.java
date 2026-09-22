/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.gamerules.GameRules
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.event.LivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntity.class})
public abstract class LivingEntityMixin {
    @Shadow
    protected int lastHurtByPlayerMemoryTime;

    @Shadow
    protected boolean isAlwaysExperienceDropper() {
        return false;
    }

    @Inject(method={"swing(Lnet/minecraft/world/InteractionHand;Z)V"}, at={@At(value="HEAD")})
    public void swing(InteractionHand hand, boolean updateSelf, CallbackInfo ci) {
        ItemStack stack = ((LivingEntity)this).getItemInHand(hand);
        if (!stack.isEmpty()) {
            // empty if block
        }
    }

    @Inject(method={"startUsingItem(Lnet/minecraft/world/InteractionHand;)V"}, at={@At(value="HEAD")})
    public void startUsingItem(InteractionHand hand, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)this;
        ItemStack stack = entity.getItemInHand(hand);
        if (!stack.isEmpty() && !entity.isUsingItem()) {
            ((LivingEntityEvents.StartUseItem)LivingEntityEvents.START_USE_ITEM.invoker()).onStartUseItem((Entity)entity, stack);
        }
    }

    @Inject(method={"heal(F)V"}, at={@At(value="HEAD")}, cancellable=true)
    public void heal(float amount, CallbackInfo ci) {
        if (!((LivingEntityEvents.EntityHeal)LivingEntityEvents.ENTITY_HEAL.invoker()).onEntityHeal((Entity)((LivingEntity)this), amount)) {
            ci.cancel();
        }
    }

    @Inject(method={"applyItemBlocking(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)F"}, at={@At(value="HEAD")}, cancellable=true)
    public void applyItemBlocking(ServerLevel serverLevel, DamageSource damageSource, float f, CallbackInfoReturnable<Float> cir) {
        if (!((LivingEntityEvents.EntityBlock)LivingEntityEvents.ENTITY_BLOCK.invoker()).onEntityBlock((Entity)((LivingEntity)this), damageSource, f)) {
            cir.cancel();
        }
    }

    @Inject(method={"dropExperience(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)V"}, at={@At(value="HEAD")}, cancellable=true)
    public void dropExperience(ServerLevel serverLevel, Entity entity, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)this;
        if (!self.wasExperienceConsumed() && (this.isAlwaysExperienceDropper() || this.lastHurtByPlayerMemoryTime > 0 && self.shouldDropExperience() && ((Boolean)serverLevel.getGameRules().get(GameRules.MOB_DROPS)).booleanValue()) && !((LivingEntityEvents.EntityDropXp)LivingEntityEvents.ENTITY_DROP_XP.invoker()).onEntityDropXp((Entity)self, (Entity)self.getLastHurtByPlayer(), self.getExperienceReward(serverLevel, entity))) {
            ci.cancel();
        }
    }

    @Inject(method={"causeFallDamage(DFLnet/minecraft/world/damagesource/DamageSource;)Z"}, at={@At(value="HEAD")}, cancellable=true)
    public void causeFallDamage(double d, float f, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (!((LivingEntityEvents.EntityFall)LivingEntityEvents.ENTITY_FALL.invoker()).onEntityFall((Entity)((LivingEntity)this), d, f)) {
            cir.setReturnValue((Object)false);
        }
    }

    @Inject(method={"onItemPickup(Lnet/minecraft/world/entity/item/ItemEntity;)V"}, at={@At(value="HEAD")})
    public void onItemPickup(ItemEntity itemEntity, CallbackInfo ci) {
        ((LivingEntityEvents.EntityPickupItem)LivingEntityEvents.ENTITY_PICKUP_ITEM.invoker()).onEntityPickupItem(itemEntity.getOwner(), itemEntity.getItem());
    }

    @Inject(method={"jumpFromGround()V"}, at={@At(value="TAIL")})
    public void jumpFromGround(CallbackInfo ci) {
        ((LivingEntityEvents.EntityJump)LivingEntityEvents.ENTITY_JUMP.invoker()).onEntityJump((Entity)((LivingEntity)this));
    }

    @Inject(method={"releaseUsingItem()V"}, at={@At(value="HEAD")})
    public void releaseUsingItem(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)this;
        if (!entity.getUseItem().isEmpty()) {
            ((LivingEntityEvents.EntityStopUsingItem)LivingEntityEvents.ENTITY_STOP_USING_ITEM.invoker()).onStopUsingItem((Entity)entity, entity.getUseItem(), entity.getUseItemRemainingTicks());
        }
    }
}

