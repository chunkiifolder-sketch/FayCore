package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.config.CrashProtectionConfig;
import chunk.faye.mod_tog.faycore.protection.SafeComponent;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntityRenderer.class})
public class EntityRenderProtectionMixin {
   @Inject(
      method = {"isEntityUpsideDown"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void faycore$protectEntityName(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
      if (CrashProtectionConfig.enableRenderProtection) {
         try {
            if (entity.hasCustomName()) {
               Component name = entity.getCustomName();
               if (name != null) {
                  String text = SafeComponent.getString(name);
                  if (text.length() > CrashProtectionConfig.maxCustomNameLength) {
                     if (CrashProtectionConfig.debugLog) {
                        System.out.println("[FayCore] Blocked bad entity name: " + entity.getType());
                     }

                     cir.setReturnValue(false);
                  }
               }
            }
         } catch (Throwable var4) {
            if (CrashProtectionConfig.debugLog) {
               var4.printStackTrace();
            }

            cir.setReturnValue(false);
         }
      }
   }
}
