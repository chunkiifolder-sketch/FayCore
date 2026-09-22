package net.mcreator.faycore;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

/**
 * FayCore AutoTool
 * Press N to toggle auto-tool. While enabled and looking at a block, the
 * hotbar automatically selects the item with the fastest destroy speed.
 */
public class FayCoreAutoTool {
    private static final int TOGGLE_KEY = GLFW.GLFW_KEY_N;

    public static boolean enabled = false;
    private static boolean wasDown = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                return;
            }
            Window window = client.getWindow();
            boolean down = InputConstants.isKeyDown(window, TOGGLE_KEY);
            if (down && !wasDown) {
                enabled = !enabled;
                client.player.sendSystemMessage(Component.literal(
                        "\u00a79[FayCore] \u00a7fAutoTool: " + (enabled ? "\u00a7aON" : "\u00a7cOFF")));
            }
            wasDown = down;

            if (enabled && client.hitResult != null
                    && client.hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult hit = (BlockHitResult) client.hitResult;
                BlockPos pos = hit.getBlockPos();
                BlockState state = client.level.getBlockState(pos);
                if (!state.isAir()) {
                    selectBestTool(client.player, state);
                }
            }
        });
    }

    private static void selectBestTool(LocalPlayer player, BlockState state) {
        float bestSpeed = 1.0f;
        int bestSlot = player.getInventory().getSelectedSlot();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        if (bestSlot != player.getInventory().getSelectedSlot()) {
            player.getInventory().setSelectedSlot(bestSlot);
        }
    }
}
