package net.mcreator.faycore;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.mcreator.faycore.client.gui.FayCoreMacroEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

/**
 * FayCore Dash
 * Press X to dash forward in the direction you are looking.
 * Uses client-side velocity + a particle/sound feedback command, matching
 * the existing command-injection style of the mod.
 */
public class FayCoreDash {
    private static final int DASH_KEY = GLFW.GLFW_KEY_X;
    private static final double DASH_POWER = 1.6;
    private static final double DASH_UP = 0.35;
    private static final int COOLDOWN_TICKS = 8;

    private static boolean wasDown = false;
    private static int cooldown = 0;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                return;
            }
            if (cooldown > 0) {
                cooldown--;
            }
            if (client.screen != null) {
                wasDown = false;
                return;
            }
            Window window = client.getWindow();
            boolean down = InputConstants.isKeyDown(window, DASH_KEY);
            if (down && !wasDown && cooldown == 0) {
                dash(client.player);
                cooldown = COOLDOWN_TICKS;
            }
            wasDown = down;
        });
    }

    private static void dash(LocalPlayer player) {
        Vec3 look = player.getLookAngle();
        player.setDeltaMovement(look.x * DASH_POWER, DASH_UP, look.z * DASH_POWER);
        player.hurtMarked = true;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            mc.level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.7f, 1.6f, false);
        }
        FayCoreMacroEngine.autoFindAndInjectVCommand(mc,
                "execute as %player% at @s run particle minecraft:cloud ~ ~0.2 ~ 0.15 0.05 0.15 0.02 12 force @a");
    }
}
