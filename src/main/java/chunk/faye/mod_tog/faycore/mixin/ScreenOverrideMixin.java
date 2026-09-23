package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.config.CrashProtectionConfig;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Minecraft.class})
public class ScreenOverrideMixin {
   private Button faycoreToggleButton;

   @Inject(
      method = {"runTick"},
      at = {@At("HEAD")},
      remap = true
   )
   private void faycore$renderButtonOnGameTick(boolean renderWorld, CallbackInfo ci) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         if (this.faycoreToggleButton == null) {
            int buttonWidth = 120;
            int buttonHeight = 20;
            int xPos = (mc.getWindow().getGuiScaledWidth() - buttonWidth) / 2;
            int yPos = 10;
            String buttonText = CrashProtectionConfig.enableDialogLimit ? "§aDialog: ON" : "§cDialog: OFF";
            this.faycoreToggleButton = Button.builder(Component.literal(buttonText), button -> {
               CrashProtectionConfig.enableDialogLimit = !CrashProtectionConfig.enableDialogLimit;
               button.setMessage(Component.literal(CrashProtectionConfig.enableDialogLimit ? "§aDialog: ON" : "§cDialog: OFF"));
               if (!CrashProtectionConfig.enableDialogLimit) {
                  mc.execute(() -> {
                     mc.setScreen(null);
                     mc.player.sendSystemMessage(Component.literal("§d[FayCore] Block dialog!"));
                  });
               }
            }).bounds(xPos, yPos, buttonWidth, buttonHeight).build();
         }

         if (mc.screen == null || mc.screen.getClass().getSimpleName().contains("Dialog") || !CrashProtectionConfig.enableDialogLimit) {
            try {
               Object mouseHandler = mc.mouseHandler;
               double rawX = 0.0;
               double rawY = 0.0;

               for (Field field : mouseHandler.getClass().getDeclaredFields()) {
                  if (field.getType() == double.class) {
                     field.setAccessible(true);
                     if (rawX == 0.0) {
                        rawX = field.getDouble(mouseHandler);
                     } else if (rawY == 0.0) {
                        rawY = field.getDouble(mouseHandler);
                        break;
                     }
                  }
               }

               double mouseX = rawX * (double)mc.getWindow().getGuiScaledWidth() / (double)mc.getWindow().getWidth();
               double mouseY = rawY * (double)mc.getWindow().getGuiScaledHeight() / (double)mc.getWindow().getHeight();
               float deltaTicks = 1.0F;

               for (Field fieldx : mc.getClass().getDeclaredFields()) {
                  if (fieldx.getType().getSimpleName().contains("Timer") || fieldx.getType().getName().contains("Timer")) {
                     fieldx.setAccessible(true);
                     Object timerObj = fieldx.get(mc);
                     if (timerObj != null) {
                        for (Method method : timerObj.getClass().getDeclaredMethods()) {
                           if (method.getReturnType() == float.class && method.getParameterCount() == 0) {
                              method.setAccessible(true);
                              deltaTicks = (Float)method.invoke(timerObj);
                              break;
                           }
                        }
                     }
                  }
               }

               if (this.faycoreToggleButton != null && mc.gui != null) {
                  Object currentGraphics = mc.gui;

                  for (Method renderMethod : this.faycoreToggleButton.getClass().getMethods()) {
                     if (renderMethod.getName().equals("render") && renderMethod.getParameterCount() == 4) {
                        Class<?>[] params = renderMethod.getParameterTypes();
                        if (params[1] == int.class && params[2] == int.class && params[3] == float.class) {
                           renderMethod.setAccessible(true);
                           renderMethod.invoke(this.faycoreToggleButton, currentGraphics, (int)mouseX, (int)mouseY, deltaTicks);
                           break;
                        }
                     }
                  }
               }
            } catch (Exception var23) {
            }
         }
      }
   }
}
