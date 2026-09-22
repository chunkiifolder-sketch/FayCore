/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText$StyledContentConsumer
 *  net.minecraft.network.chat.Style
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package chunk.faye.mod_tog.faycore.mixin;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Component.class})
public interface MixinComponent {
    @Inject(method={"visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;"}, at={@At(value="HEAD")}, cancellable=true)
    private void onVisitStyled(FormattedText.StyledContentConsumer<?> consumer, Style style, CallbackInfoReturnable<Optional<?>> cir) {
        String fullStructure = this.toString();
        if (fullStructure != null && (fullStructure.length() > 3000 || fullStructure.contains("bundle") && fullStructure.contains("component"))) {
            cir.setReturnValue(Optional.empty());
        }
    }
}

