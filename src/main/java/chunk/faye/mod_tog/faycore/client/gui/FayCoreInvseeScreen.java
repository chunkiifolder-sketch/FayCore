package chunk.faye.mod_tog.faycore.client.gui;

import chunk.faye.mod_tog.faycore.FayCoreInvseeManager;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

public class FayCoreInvseeScreen extends Screen {
   private final int imageWidth = 176;
   private final int imageHeight = 166;
   private int leftPos;
   private int topPos;
   private int lastHoveredSlot = -1;
   private long lastRequestTime = 0L;

   public FayCoreInvseeScreen() {
      super(Component.literal("§6FayCore 獨立遠端背包監視盾"));
   }

   protected void init() {
      super.init();
      this.leftPos = (this.width - 176) / 2;
      this.topPos = (this.height - 166) / 2;
      this.addRenderableOnly((graphicsObj, mouseX, mouseY, partialTick) -> {
         if (graphicsObj != null) {
            try {
               int currentHover = -1;

               for (int slot = 0; slot < 45; slot++) {
                  int col = slot % 9;
                  int row = slot / 9;
                  int gridX = this.leftPos + 8 + col * 18;
                  int gridY = this.topPos + 35 + row * 18;
                  if (mouseX >= gridX && mouseX <= gridX + 16 && mouseY >= gridY && mouseY <= gridY + 16) {
                     currentHover = slot;
                     break;
                  }
               }

               if (currentHover != this.lastHoveredSlot) {
                  this.lastHoveredSlot = currentHover;
                  long now = System.currentTimeMillis();
                  if (currentHover != -1 && now - this.lastRequestTime > 150L) {
                     this.lastRequestTime = now;
                     ItemStack stack = FayCoreInvseeManager.virtualInventory.get(currentHover);
                     if (stack != null && !stack.isEmpty()) {
                        FayCoreInvseeManager.requestSingleSlotData(currentHover);
                     } else {
                        FayCoreInvseeManager.currentHoverTooltip.clear();
                     }
                  } else if (currentHover == -1) {
                     FayCoreInvseeManager.currentHoverTooltip.clear();
                  }
               }

               if (currentHover != -1 && !FayCoreInvseeManager.currentHoverTooltip.isEmpty()) {
                  Class<?> clazz = graphicsObj.getClass();

                  for (Method m : clazz.getMethods()) {
                     Class<?>[] p = m.getParameterTypes();
                     if (p.length == 4 && p[0] == Font.class && List.class.isAssignableFrom(p[1]) && p[2] == int.class && p[3] == int.class) {
                        m.invoke(graphicsObj, this.font, FayCoreInvseeManager.currentHoverTooltip, mouseX, mouseY);
                        break;
                     }
                  }
               }
            } catch (Exception var12) {
            }
         }
      });
   }

   public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
      if (guiGraphics != null) {
         guiGraphics.fillGradient(0, 0, this.width, this.height, -1442840576, -1442840576);
         guiGraphics.fillGradient(this.leftPos, this.topPos, this.leftPos + 176, this.topPos + 166, -1440603614, -1440603614);

         for (int slot = 0; slot < 45; slot++) {
            int col = slot % 9;
            int row = slot / 9;
            int gridX = this.leftPos + 8 + col * 18;
            int gridY = this.topPos + 35 + row * 18;
            guiGraphics.fillGradient(gridX, gridY, gridX + 16, gridY + 16, 1157627903, 1157627903);
            ItemStack stack = FayCoreInvseeManager.virtualInventory.get(slot);
            if (stack != null && !stack.isEmpty()) {
               try {
                  Class<?> graphicsClass = guiGraphics.getClass();

                  for (Method method : graphicsClass.getMethods()) {
                     Class<?>[] params = method.getParameterTypes();
                     if (params.length == 3 && params[0] == ItemStack.class && params[1] == int.class && params[2] == int.class) {
                        method.invoke(guiGraphics, stack, gridX, gridY);
                     }
                  }
               } catch (Exception var17) {
               }
            }
         }

         super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (FayCoreInvseeManager.targetPlayerName.isEmpty()) {
         return false;
      } else {
         for (int slot = 0; slot < 45; slot++) {
            int col = slot % 9;
            int row = slot / 9;
            int gridX = this.leftPos + 8 + col * 18;
            int gridY = this.topPos + 35 + row * 18;
            if (mouseX >= (double)gridX && mouseX <= (double)(gridX + 16) && mouseY >= (double)gridY && mouseY <= (double)(gridY + 16)) {
               Minecraft mc = Minecraft.getInstance();
               if (mc.player != null && mc.player.connection != null) {
                  String slotName = "container." + slot;
                  if (slot == 36) {
                     slotName = "armor.feet";
                  } else if (slot == 37) {
                     slotName = "armor.legs";
                  } else if (slot == 38) {
                     slotName = "armor.chest";
                  } else if (slot == 39) {
                     slotName = "armor.head";
                  } else if (slot == 40) {
                     slotName = "weapon.offhand";
                  }

                  String hackCommand = "item replace entity " + FayCoreInvseeManager.targetPlayerName + " " + slotName + " with minecraft:air";
                  mc.player.connection.send(new ServerboundChatCommandPacket(hackCommand));
                  FayCoreInvseeManager.virtualInventory.put(slot, ItemStack.EMPTY);
                  mc.player.playSound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
               }
               break;
            }
         }

         return false;
      }
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void onClose() {
      FayCoreInvseeManager.targetPlayerName = "";
      FayCoreInvseeManager.currentHoverTooltip.clear();
      super.onClose();
   }
}
