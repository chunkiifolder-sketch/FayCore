package chunk.faye.mod_tog.faycore;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;

public class FayCoreAutoTool {
   private static final int TOGGLE_KEY = 78;
   public static boolean enabled = false;
   private static boolean wasDown = false;

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)var0 -> {
         if (var0.player != null && var0.level != null) {
            Window var1 = var0.getWindow();
            boolean var2 = InputConstants.isKeyDown(var1, 78);
            if (var2 && !wasDown) {
               enabled = !enabled;
               var0.player.sendSystemMessage(Component.literal("§9[FayCore] §fAutoTool: " + (enabled ? "§aON" : "§cOFF")));
            }

            wasDown = var2;
            if (enabled && var0.hitResult != null && var0.hitResult.getType() == Type.BLOCK) {
               BlockHitResult var3 = (BlockHitResult)var0.hitResult;
               BlockPos var4 = var3.getBlockPos();
               BlockState var5 = var0.level.getBlockState(var4);
               if (!var5.isAir()) {
                  selectBestTool(var0.player, var5);
               }
            }
         }
      });
   }

   private static void selectBestTool(LocalPlayer var0, BlockState var1) {
      float var2 = 1.0F;
      int var3 = var0.getInventory().getSelectedSlot();

      for (int var4 = 0; var4 < 9; var4++) {
         ItemStack var5 = var0.getInventory().getItem(var4);
         if (!var5.isEmpty()) {
            float var6 = var5.getDestroySpeed(var1);
            if (var6 > var2) {
               var2 = var6;
               var3 = var4;
            }
         }
      }

      if (var3 != var0.getInventory().getSelectedSlot()) {
         var0.getInventory().setSelectedSlot(var3);
      }
   }
}
