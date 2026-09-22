/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.LivingEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.config.CrashProtectionConfig;
import chunk.faye.mod_tog.faycore.protection.SafeComponent;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntityRenderer.class})
public class EntityRenderProtectionMixin {
    @Inject(method={"isEntityUpsideDown"}, at={@At(value="HEAD")}, cancellable=true)
    private static void faycore$protectEntityName(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!CrashProtectionConfig.enableRenderProtection) {
            return;
        }
        try {
            String text;
            Component name;
            if (entity.hasCustomName() && (name = entity.getCustomName()) != null && (text = SafeComponent.getString(name)).length() > CrashProtectionConfig.maxCustomNameLength) {
                if (CrashProtectionConfig.debugLog) {
                    System.out.println("[FayCore] Blocked bad entity name: " + String.valueOf(entity.getType()));
                }
                cir.setReturnValue((Object)false);
            }
        }
        catch (Throwable throwable) {
            if (CrashProtectionConfig.debugLog) {
                throwable.printStackTrace();
            }
            cir.setReturnValue((Object)false);
        }
    }
}

