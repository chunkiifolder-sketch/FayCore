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

import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Component.class})
public interface AntiNbtTextBombMixin {
    @Inject(method={"getString()Ljava/lang/String;"}, at={@At(value="HEAD")}, cancellable=true)
    private void faycore$interceptTextBomb(CallbackInfoReturnable<String> cir) {
        Component self = (Component)this;
        try {
            String raw = self.getContents().toString();
            if (raw.length() > 2000) {
                cir.setReturnValue((Object)"\u00a7c[\u6514\u622a\u60e1\u610f\u6587\u672c]");
            }
        }
        catch (Throwable t) {
            cir.setReturnValue((Object)"\u00a7c[\u6514\u622a\u5b89\u5168\u5c4f\u853d]");
        }
    }
}

