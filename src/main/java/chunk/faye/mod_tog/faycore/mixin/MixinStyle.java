/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Style
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package chunk.faye.mod_tog.faycore.mixin;

import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Style.class})
public class MixinStyle {
    @Inject(method={"isObfuscated()Z"}, at={@At(value="HEAD")}, cancellable=true)
    private void onIsObfuscated(CallbackInfoReturnable<Boolean> cir) {
        try {
            String fullStyleData = this.toString();
            if (fullStyleData != null && fullStyleData.length() > 800) {
                cir.setReturnValue((Object)false);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Inject(method={"isEmpty()Z"}, at={@At(value="HEAD")}, cancellable=true)
    private void onIsEmpty(CallbackInfoReturnable<Boolean> cir) {
        try {
            String fullStyleData = this.toString();
            if (fullStyleData != null && fullStyleData.length() > 1500) {
                cir.setReturnValue((Object)true);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

