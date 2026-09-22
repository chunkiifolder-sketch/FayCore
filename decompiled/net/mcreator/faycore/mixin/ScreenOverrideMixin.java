/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.MouseHandler
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.network.chat.Component
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.mcreator.faycore.mixin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.mcreator.faycore.config.CrashProtectionConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Minecraft.class})
public class ScreenOverrideMixin {
    private Button faycoreToggleButton;

    @Inject(method={"runTick"}, at={@At(value="HEAD")}, remap=true)
    private void faycore$renderButtonOnGameTick(boolean renderWorld, CallbackInfo ci) {
        block10: {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) {
                return;
            }
            if (this.faycoreToggleButton == null) {
                int buttonWidth = 120;
                int buttonHeight = 20;
                int xPos = (mc.getWindow().getGuiScaledWidth() - buttonWidth) / 2;
                int yPos = 10;
                String buttonText = CrashProtectionConfig.enableDialogLimit ? "\u00a7aDialog: ON" : "\u00a7cDialog: OFF";
                this.faycoreToggleButton = Button.builder((Component)Component.literal((String)buttonText), button -> {
                    CrashProtectionConfig.enableDialogLimit = !CrashProtectionConfig.enableDialogLimit;
                    button.setMessage((Component)Component.literal((String)(CrashProtectionConfig.enableDialogLimit ? "\u00a7aDialog: ON" : "\u00a7cDialog: OFF")));
                    if (!CrashProtectionConfig.enableDialogLimit) {
                        mc.execute(() -> {
                            mc.setScreen(null);
                            mc.player.sendSystemMessage((Component)Component.literal((String)"\u00a7d[FayCore] Block dialog!"));
                        });
                    }
                }).bounds(xPos, yPos, buttonWidth, buttonHeight).build();
            }
            if (mc.screen == null || mc.screen.getClass().getSimpleName().contains("Dialog") || !CrashProtectionConfig.enableDialogLimit) {
                try {
                    MouseHandler mouseHandler = mc.mouseHandler;
                    double rawX = 0.0;
                    double rawY = 0.0;
                    for (Field field : mouseHandler.getClass().getDeclaredFields()) {
                        if (field.getType() != Double.TYPE) continue;
                        field.setAccessible(true);
                        if (rawX == 0.0) {
                            rawX = field.getDouble(mouseHandler);
                            continue;
                        }
                        if (rawY != 0.0) continue;
                        rawY = field.getDouble(mouseHandler);
                        break;
                    }
                    double mouseX = rawX * (double)mc.getWindow().getGuiScaledWidth() / (double)mc.getWindow().getWidth();
                    double mouseY = rawY * (double)mc.getWindow().getGuiScaledHeight() / (double)mc.getWindow().getHeight();
                    float deltaTicks = 1.0f;
                    block3: for (Field field : mc.getClass().getDeclaredFields()) {
                        if (!field.getType().getSimpleName().contains("Timer") && !field.getType().getName().contains("Timer")) continue;
                        field.setAccessible(true);
                        Object timerObj = field.get(mc);
                        if (timerObj == null) continue;
                        for (Method method : timerObj.getClass().getDeclaredMethods()) {
                            if (method.getReturnType() != Float.TYPE || method.getParameterCount() != 0) continue;
                            method.setAccessible(true);
                            deltaTicks = ((Float)method.invoke(timerObj, new Object[0])).floatValue();
                            continue block3;
                        }
                    }
                    if (this.faycoreToggleButton == null || mc.gui == null) break block10;
                    Gui currentGraphics = mc.gui;
                    for (Method renderMethod : this.faycoreToggleButton.getClass().getMethods()) {
                        Class<?>[] params;
                        if (!renderMethod.getName().equals("render") || renderMethod.getParameterCount() != 4 || (params = renderMethod.getParameterTypes())[1] != Integer.TYPE || params[2] != Integer.TYPE || params[3] != Float.TYPE) continue;
                        renderMethod.setAccessible(true);
                        renderMethod.invoke((Object)this.faycoreToggleButton, currentGraphics, (int)mouseX, (int)mouseY, Float.valueOf(deltaTicks));
                        break;
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
    }
}

