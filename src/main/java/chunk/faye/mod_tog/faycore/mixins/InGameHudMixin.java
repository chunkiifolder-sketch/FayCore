package chunk.faye.mod_tog.faycore.mixins;

import chunk.faye.mod_tog.faycore.FaycoreModClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Gui.class})
public class InGameHudMixin {
   @Inject(
      method = {"render"},
      at = {@At("TAIL")}
   )
   private void onRenderHUD(GuiGraphicsExtractor guiGraphics, float partialTick, CallbackInfo ci) {
      FaycoreModClient.renderNativeFayCoreHUD(guiGraphics);
   }
}
