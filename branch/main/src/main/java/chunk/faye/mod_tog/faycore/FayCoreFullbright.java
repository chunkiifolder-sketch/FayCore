package chunk.faye.mod_tog.faycore;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import chunk.faye.mod_tog.faycore.client.gui.FayCoreMacroEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * FayCore Fullbright
 * Press C to toggle fullbright (infinite night vision via command).
 */
public class FayCoreFullbright {
    private static final int TOGGLE_KEY = GLFW.GLFW_KEY_C;

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
                Minecraft mc = Minecraft.getInstance();
                if (enabled) {
                    FayCoreMacroEngine.autoFindAndInjectVCommand(mc,
                            "effect give %player% minecraft:night_vision infinite 0 true");
                } else {
                    FayCoreMacroEngine.autoFindAndInjectVCommand(mc,
                            "effect clear %player% minecraft:night_vision");
                }
                client.player.sendSystemMessage(Component.literal(
                        "\u00a79[FayCore] \u00a7fFullbright: " + (enabled ? "\u00a7aON" : "\u00a7cOFF")));
            }
            wasDown = down;
        });
    }
}
