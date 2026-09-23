package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.SkillInteractScreen;
import chunk.faye.mod_tog.faycore.SkillManager;
import chunk.faye.mod_tog.faycore.SkillState;
import chunk.faye.mod_tog.faycore.SkillTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MouseHandler.class})
public class MouseHandlerMixin {
   @Shadow
   @Final
   private Minecraft minecraft;

   @Inject(
      method = {"onScroll"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onScroll(long handle, double xoffset, double yoffset, CallbackInfo ci) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null) {
         boolean alt = GLFW.glfwGetKey(client.getWindow().handle(), 342) == 1 || GLFW.glfwGetKey(client.getWindow().handle(), 346) == 1;
         boolean ctrl = GLFW.glfwGetKey(client.getWindow().handle(), 341) == 1 || GLFW.glfwGetKey(client.getWindow().handle(), 345) == 1;
         if (GLFW.glfwGetKey(client.getWindow().handle(), 340) != 1 && GLFW.glfwGetKey(client.getWindow().handle(), 344) != 1) {
            boolean var12 = false;
         } else {
            boolean var10000 = true;
         }

         if (SkillState.getSelected() == 3 && ctrl) {
            if (yoffset > 0.0) {
               SkillTracker.controlDistance++;
            }

            if (yoffset < 0.0) {
               SkillTracker.controlDistance--;
            }

            SkillTracker.controlDistance = Math.clamp((long)SkillTracker.controlDistance, 1, 64);
            client.gui.setOverlayMessage(Component.literal("§9[FayCore] §e控制距離：§4 " + SkillTracker.controlDistance), false);
            ci.cancel();
         } else {
            if (ctrl && this.minecraft.player != null && this.minecraft.level != null) {
               this.minecraft
                  .level
                  .playLocalSound(
                     this.minecraft.player.getX(),
                     this.minecraft.player.getY(),
                     this.minecraft.player.getZ(),
                     (SoundEvent)SoundEvents.NOTE_BLOCK_SNARE.value(),
                     SoundSource.PLAYERS,
                     1.0F,
                     1.0F,
                     false
                  );
            }

            if (SkillState.getSelected() == 4 && ctrl) {
               if (yoffset > 0.0) {
                  SkillManager.teleportDistance++;
               }

               if (yoffset < 0.0) {
                  SkillManager.teleportDistance--;
               }

               SkillManager.teleportDistance = Math.clamp((long)SkillManager.teleportDistance, 1, 64);
               client.gui.setOverlayMessage(Component.literal("§9[FayCore] §e傳送距離：§4 " + SkillManager.teleportDistance), false);
               ci.cancel();
            } else {
               if (SkillState.getSelected() == 1 && ctrl) {
                  if (SkillInteractScreen.LaserMode == 0) {
                     if (yoffset > 0.0) {
                        SkillManager.TNT_POWER += 0.1F;
                     }

                     if (yoffset < 0.0) {
                        SkillManager.TNT_POWER -= 0.1F;
                     }

                     SkillManager.TNT_POWER = Math.clamp(SkillManager.TNT_POWER, 1.0F, 10.0F);
                     client.gui.setOverlayMessage(Component.literal(String.format("§9[FayCore] §e爆炸威力：§4 %.1f", SkillManager.TNT_POWER)), false);
                     ci.cancel();
                     return;
                  }

                  if (SkillInteractScreen.LaserMode == 1) {
                     if (yoffset > 0.0) {
                        SkillManager.DELETE_POWER++;
                     }

                     if (yoffset < 0.0) {
                        SkillManager.DELETE_POWER--;
                     }

                     SkillManager.DELETE_POWER = Math.clamp((long)SkillManager.DELETE_POWER, 1, 3);
                     client.gui.setOverlayMessage(Component.literal(String.format("§9[FayCore] §e破壞威力：§4 %d", SkillManager.DELETE_POWER)), false);
                     ci.cancel();
                     return;
                  }
               }

               if (SkillInteractScreen.LaserMode == 3 && ctrl) {
                  if (yoffset > 0.0) {
                     SkillManager.FLING_POWER++;
                  }

                  if (yoffset < 0.0) {
                     SkillManager.FLING_POWER--;
                  }

                  SkillManager.FLING_POWER = Math.clamp((long)SkillManager.FLING_POWER, 1, 3);
                  client.gui.setOverlayMessage(Component.literal(String.format("§9[FayCore] §e飄塊威力：§4 %d", SkillManager.FLING_POWER)), false);
                  ci.cancel();
               } else {
                  if (alt) {
                     if (yoffset > 0.0) {
                        SkillState.previous();
                     }

                     if (yoffset < 0.0) {
                        SkillState.next();
                     }

                     ci.cancel();
                  }
               }
            }
         }
      }
   }
}
