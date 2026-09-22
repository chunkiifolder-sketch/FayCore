package chunk.faye.mod_tog.faycore;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * FayCore AutoSprint
 * Press K to toggle auto-sprint. While enabled the player stays sprinting
 * whenever they are moving forward on the ground.
 */
public class FayCoreAutoSprint {
    private static final int TOGGLE_KEY = GLFW.GLFW_KEY_K;

    public static boolean enabled = false;
    private static boolean wasDown = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }
            Window window = client.getWindow();
            boolean down = InputConstants.isKeyDown(window, TOGGLE_KEY);
            if (down && !wasDown) {
                enabled = !enabled;
                client.player.sendSystemMessage(Component.literal(
                        "\u00a79[FayCore] \u00a7fAutoSprint: " + (enabled ? "\u00a7aON" : "\u00a7cOFF")));
            }
            wasDown = down;

            if (enabled && client.player.onGround()
                    && client.options.keyUp.isDown()) {
                client.player.setSprinting(true);
            }
        });
    }
}
