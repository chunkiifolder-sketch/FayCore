package net.mcreator.faycore.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.Gui.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderHUD(net.minecraft.client.gui.GuiGraphicsExtractor guiGraphics, float partialTick, CallbackInfo ci) {
        // 直接在每一幀畫面渲染的最尾端（TAIL），呼叫你剛才編譯成功的原生 HUD 方法！
        net.mcreator.faycore.FaycoreModClient.renderNativeFayCoreHUD(guiGraphics);
    }
}
