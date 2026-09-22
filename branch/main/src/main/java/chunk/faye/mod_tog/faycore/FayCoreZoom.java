package chunk.faye.mod_tog.faycore;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * FayCore Zoom
 * Hold Z to zoom in (lowers the FOV). Releases back to the previous FOV.
 *
 * Note: the exact FOV accessor (client.options.fov()) can vary slightly
 * between Minecraft versions. The implementation below is defensive so it
 * never crashes; if it does not zoom on your build, adjust ZOOM_FOV or the
 * fov() call to match your mapping.
 */
public class FayCoreZoom {
    private static final int ZOOM_KEY = GLFW.GLFW_KEY_Z;
    private static final int ZOOM_FOV = 20;

    private static boolean zooming = false;
    private static Integer baseFov = null;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.options == null) {
                return;
            }
            Window window = client.getWindow();
            boolean down = InputConstants.isKeyDown(window, ZOOM_KEY) && client.screen == null;
            if (down != zooming) {
                zooming = down;
                applyZoom(client);
            }
        });
    }

    private static void applyZoom(Minecraft client) {
        try {
            if (baseFov == null) {
                Object value = client.options.fov().get();
                baseFov = value instanceof Number ? ((Number) value).intValue() : 70;
            }
            client.options.fov().set(zooming ? ZOOM_FOV : baseFov.intValue());
        } catch (Throwable ignored) {
            // FOV API differs across versions; never crash the client.
        }
    }
}
