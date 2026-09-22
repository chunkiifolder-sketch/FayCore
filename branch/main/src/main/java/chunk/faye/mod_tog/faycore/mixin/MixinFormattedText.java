/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.FormattedText
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package chunk.faye.mod_tog.faycore.mixin;

import net.minecraft.network.chat.FormattedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={FormattedText.class})
public interface MixinFormattedText {
    @Inject(method={"getString()Ljava/lang/String;"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetString(CallbackInfoReturnable<String> cir) {
        try {
            int length;
            String rawText = this.toString();
            if (rawText != null && (length = rawText.length()) > 2500) {
                cir.setReturnValue((Object)"\u00a7c[FayCore] \u8a72\u7269\u54c1\u5305\u542b\u8d85\u9577\u5371\u96aa\u6587\u672c\uff0c\u5df2\u81ea\u52d5\u9694\u96e2\u4fdd\u8b77");
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

