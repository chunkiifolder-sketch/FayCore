/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.mcreator.faycore.mixin;

import net.mcreator.faycore.protection.ComponentLimiter;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Component.class})
public interface ComponentProtectionMixin {
    @Inject(method={"getString"}, at={@At(value="HEAD")}, cancellable=true)
    private void faycore$protectComponent(CallbackInfoReturnable<String> cir) {
        Component self = (Component)this;
        if (!ComponentLimiter.safe(self)) {
            cir.setReturnValue((Object)"[Blocked Component]");
        }
    }
}

