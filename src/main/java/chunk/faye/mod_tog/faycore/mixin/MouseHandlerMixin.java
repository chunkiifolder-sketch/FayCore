package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.SkillState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(
            method = "onScroll",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onScroll(
            long handle,
            double xoffset,
            double yoffset,
            CallbackInfo ci
    ) {

        Minecraft client = Minecraft.getInstance();

        if (client.player == null) {
            return;
        }

        if (GLFW.glfwGetKey(
                client.getWindow().handle(),
                GLFW.GLFW_KEY_LEFT_ALT
        ) == GLFW.GLFW_PRESS) {

            if (yoffset > 0) {
                SkillState.previous();
            }

            if (yoffset < 0) {
                SkillState.next();
            }

            ci.cancel();
        }
    }
}