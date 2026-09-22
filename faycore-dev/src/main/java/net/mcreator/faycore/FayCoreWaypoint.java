package net.mcreator.faycore;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.mcreator.faycore.client.gui.FayCoreMacroEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * FayCore Waypoint
 * Press Y to save your current position as a waypoint.
 * Press U to teleport back to the most recent waypoint.
 */
public class FayCoreWaypoint {
    private static final int SET_KEY = GLFW.GLFW_KEY_Y;
    private static final int TELEPORT_KEY = GLFW.GLFW_KEY_U;

    public static final List<Waypoint> waypoints = new ArrayList<Waypoint>();

    private static boolean setWasDown = false;
    private static boolean tpWasDown = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }
            if (client.screen != null) {
                setWasDown = false;
                tpWasDown = false;
                return;
            }
            Window window = client.getWindow();
            boolean setDown = InputConstants.isKeyDown(window, SET_KEY);
            boolean tpDown = InputConstants.isKeyDown(window, TELEPORT_KEY);

            if (setDown && !setWasDown) {
                addWaypoint(client.player);
            }
            if (tpDown && !tpWasDown) {
                teleportToLast(client);
            }
            setWasDown = setDown;
            tpWasDown = tpDown;
        });
    }

    private static void addWaypoint(LocalPlayer player) {
        BlockPos pos = player.blockPosition();
        waypoints.add(new Waypoint(pos));
        player.sendSystemMessage(Component.literal(
                "\u00a79[FayCore] \u00a7fWaypoint #" + (waypoints.size() - 1)
                        + " set at \u00a7a" + pos.toShortString()));
    }

    private static void teleportToLast(Minecraft client) {
        if (waypoints.isEmpty()) {
            client.player.sendSystemMessage(Component.literal(
                    "\u00a79[FayCore] \u00a7cNo waypoints saved yet."));
            return;
        }
        Waypoint wp = waypoints.get(waypoints.size() - 1);
        FayCoreMacroEngine.autoFindAndInjectVCommand(client,
                "tp %player% " + wp.pos.getX() + " " + wp.pos.getY() + " " + wp.pos.getZ());
        client.player.sendSystemMessage(Component.literal(
                "\u00a79[FayCore] \u00a7fTeleported to waypoint #" + (waypoints.size() - 1)));
    }

    public static class Waypoint {
        public final BlockPos pos;

        public Waypoint(BlockPos pos) {
            this.pos = pos;
        }
    }
}
