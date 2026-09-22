/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.mcreator.faycore.mixins;

import net.mcreator.faycore.FaycoreModClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Gui.class})
public class InGameHudMixin {
    @Inject(method={"render"}, at={@At(value="TAIL")})
    private void onRenderHUD(GuiGraphicsExtractor guiGraphics, float partialTick, CallbackInfo ci) {
        FaycoreModClient.renderNativeFayCoreHUD(guiGraphics);
    }
}

