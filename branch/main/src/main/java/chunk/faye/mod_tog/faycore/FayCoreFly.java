package chunk.faye.mod_tog.faycore;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * FayCore Fly
 * Press H to toggle client-side flight.
 */
public class FayCoreFly {
    private static final int TOGGLE_KEY = GLFW.GLFW_KEY_H;
    private static final float FLY_SPEED = 0.05f;

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
                applyFlight(client.player, enabled);
                client.player.sendSystemMessage(Component.literal(
                        "\u00a79[FayCore] \u00a7fFly: " + (enabled ? "\u00a7aON" : "\u00a7cOFF")));
            }
            wasDown = down;
        });
    }

    private static void applyFlight(LocalPlayer player, boolean fly) {
        player.getAbilities().mayfly = fly;
        player.getAbilities().flying = fly;
        if (fly) {
            player.getAbilities().setFlyingSpeed(FLY_SPEED);
        }
        player.onUpdateAbilities();
    }
}
