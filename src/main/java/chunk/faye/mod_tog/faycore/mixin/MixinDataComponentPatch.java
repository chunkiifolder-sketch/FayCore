/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package chunk.faye.mod_tog.faycore.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets={"net.minecraft.core.component.DataComponentPatch"})
public class MixinDataComponentPatch {
    @Inject(method={"toString()Ljava/lang/String;"}, at={@At(value="HEAD")})
    private void onDataComponentToStringHead(CallbackInfoReturnable<String> cir) {
    }

    @Inject(method={"isEmpty()Z"}, at={@At(value="HEAD")}, cancellable=true)
    private void onCheckIsEmpty(CallbackInfoReturnable<Boolean> cir) {
        try {
            int dataLength;
            String rawComponentData = this.toString();
            if (rawComponentData != null && (dataLength = rawComponentData.length()) > 1500 && rawComponentData.contains("%1$s") && rawComponentData.contains("obfuscated")) {
                cir.setReturnValue((Object)true);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

