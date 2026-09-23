package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.SkillTracker;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntityRenderer.class})
public class LivingEntityRendererMixin {
   @Inject(
      method = {"extractRenderState"},
      at = {@At("TAIL")}
   )
   private void faycore$outline(LivingEntity entity, LivingEntityRenderState state, float partialTicks, CallbackInfo ci) {
      if (SkillTracker.glowTarget != null && entity.getUUID().equals(SkillTracker.glowTarget)) {
         state.outlineColor = -65536;
      } else {
         state.outlineColor = 0;
      }
   }
}
