package chunk.faye.mod_tog.faycore;

import chunk.faye.mod_tog.faycore.client.gui.FayCoreControlScreen;
import chunk.faye.mod_tog.faycore.client.gui.FayCoreMacroEngine;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

public class SkillInteractScreen extends Screen {
   public static int LaserMode = 0;
   public static String LaserModeLore = null;
   public static int GunMode = 0;
   public static String GunModeLore = null;
   private boolean wasMouseClickedBefore = false;
   public static String GunModeDamage = "0";
   public static int WingMode = 0;
   public static String WingLore = null;
   public static int WingSize = 0;
   public static String WingSizeLore = null;
   private static final Component EMPTY_SLOT = Component.empty().append(Component.literal("§4 空槽")).append("\n").append(Component.literal("§e使用時: §7無作用"));

   public SkillInteractScreen() {
      super(Component.literal("Skill Interact"));
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void extractBlurredBackground(GuiGraphicsExtractor guiGraphicsExtractor) {
   }

   public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
      double guiScale = (double)this.minecraft.getWindow().getGuiScale();
      int realMouseX = (int)(this.minecraft.mouseHandler.xpos() / guiScale);
      int realMouseY = (int)(this.minecraft.mouseHandler.ypos() / guiScale);
      boolean isLeftClickDown = GLFW.glfwGetMouseButton(this.minecraft.getWindow().handle(), 0) == 1;
      int slotWidth = 29;
      int slotHeight = 24;
      int gap = -1;
      int x = this.width - slotWidth - 8;
      int startY = (this.height - (slotHeight * SkillState.MAX + gap * 4)) / 2;
      boolean hitSkillbarSlot = false;
      if (realMouseX >= x && realMouseX <= x + slotWidth) {
         for (int i = 0; i < SkillState.MAX; i++) {
            int y = startY + i * (slotHeight + gap);
            if (realMouseY >= y && realMouseY <= y + slotHeight) {
               hitSkillbarSlot = true;
               if (isLeftClickDown && !this.wasMouseClickedBefore) {
                  if (SkillState.getSelected() != i) {
                     SkillState.setSelected(i);
                     String skillName = SkillState.getSkillName(i);
                     this.minecraft
                        .gui
                        .setOverlayMessage(
                           Component.literal("▶ [FayCore] 已裝配特技: " + skillName + " ◀")
                              .withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}),
                           false
                        );
                     this.minecraft
                        .level
                        .playSound(
                           null,
                           this.minecraft.player.getX(),
                           this.minecraft.player.getY(),
                           this.minecraft.player.getZ(),
                           (SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(),
                           SoundSource.PLAYERS,
                           0.7F,
                           1.0F
                        );
                  }

                  this.wasMouseClickedBefore = true;
               }

               MutableComponent finalTooltipComponent = Component.empty()
                  .append(this.getSingleComponentTooltip(i))
                  .append("\n\n")
                  .append(Component.literal("[FayCore - Skillbar]").withStyle(ChatFormatting.BLUE));

               try {
                  List<FormattedCharSequence> splitLines = this.minecraft.font.split(finalTooltipComponent, Integer.MAX_VALUE);
                  guiGraphicsExtractor.setTooltipForNextFrame(this.font, splitLines, realMouseX, realMouseY);
               } catch (Throwable var21) {
               }
               break;
            }
         }
      }

      if (!isLeftClickDown) {
         this.wasMouseClickedBefore = false;
      }

      if (hitSkillbarSlot && isLeftClickDown) {
         this.minecraft.options.keyAttack.setDown(false);
         if (this.minecraft.gameMode != null) {
            this.minecraft.gameMode.stopDestroyBlock();
         }
      }

      super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
   }

   public boolean keyReleased(KeyEvent event) {
      if (event.key() == 342) {
         this.onClose();
         return true;
      } else {
         return super.keyReleased(event);
      }
   }

   private Component getSingleComponentTooltip(int slot) {
      MutableComponent base = Component.empty();

      return switch (slot) {
         case 0 -> Component.literal("§7§l【多段跳】\n§e使用時: §7在空中跳躍");
         case 1 -> {
            switch (LaserMode) {
               case 0:
                  LaserModeLore = String.format("爆炸模式 §4[%.1f] §7(1/4)", SkillManager.TNT_POWER);
                  break;
               case 1:
                  LaserModeLore = String.format("刪除模式 §4[%d] §7(2/4)", SkillManager.DELETE_POWER);
                  break;
               case 2:
                  LaserModeLore = "擊退模式 (3/4)";
                  break;
               case 3:
                  LaserModeLore = String.format("飄塊模式 §4[%d] §7(4/4)", SkillManager.FLING_POWER);
                  break;
               default:
                  LaserModeLore = "未知模式";
            }

            yield Component.literal("§7§l【雷射】\n§e使用時: §7發射雷射\n\n§3模式: §7" + LaserModeLore);
         }
         case 2 -> {
            switch (GunMode) {
               case 0:
                  GunModeLore = "普通模式 (1/3)";
                  GunModeDamage = "20";
                  break;
               case 1:
                  GunModeLore = "秒殺模式 (2/3)";
                  GunModeDamage = "Infinity";
                  break;
               case 2:
                  GunModeLore = "火箭筒 (3/3)";
                  GunModeDamage = "§c???";
            }

            yield Component.literal("§7§l【手槍】\n§e使用時: §7發射子彈\n\n§3模式: §7" + GunModeLore + "\n§3傷害: §7" + GunModeDamage);
         }
         case 3 -> Component.literal("§7§l【控制】\n§e使用時: §7控制任何生物\n\n§d具有多種功能:\n§3Ctrl + 滾輪§7 | §3控制距離 (目前: " + SkillTracker.controlDistance + ")");
         case 4 -> Component.literal("§7§l【瞬移】\n§e使用時: §7瞬移到特定位置\n\n§d具有多種功能:\n§3Ctrl + 滾輪§7 | §3傳送距離 (目前: " + SkillManager.teleportDistance + ")");
         case 5 -> {
            switch (WingMode) {
               case 0:
                  WingLore = "Nothing (1/5)";
                  break;
               case 1:
                  WingLore = "Sculk Soul (2/5)";
                  break;
               case 2:
                  WingLore = "Cherry (3/5)";
                  break;
               case 3:
                  WingLore = "Cloud (4/5)";
                  break;
               case 4:
                  WingLore = "Crit (5/5)";
                  break;
               default:
                  WingLore = "未知種類";
            }

            switch (WingSize) {
               case 0:
                  WingSizeLore = "1 (1/3)";
                  break;
               case 1:
                  WingSizeLore = "2 (2/3)";
                  break;
               case 2:
                  WingSizeLore = "Cape (3/3)";
                  break;
               default:
                  WingSizeLore = "未知大小";
            }

            yield Component.literal("§7§l【翅膀顯示】\n§e使用時: §7裝飾品\n\n§3種類: §7" + WingLore + "\n§3大小: §7" + WingSizeLore);
         }
         case 6 -> EMPTY_SLOT.copy();
         default -> base.append(Component.literal("§3[FayCore] §4未知技能").withStyle(ChatFormatting.LIGHT_PURPLE));
      };
   }

   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      Minecraft mc = Minecraft.getInstance();
      int slot = this.getHoveredSlot(event.x(), event.y());
      int CurrentSlot = SkillState.getSelected();
      if (slot != -1) {
         if (event.button() == 0 && slot == CurrentSlot) {
            switch (slot) {
               case 5:
                  WingSize = (WingSize + 1) % 3;
                  break;
               default:
                  FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "title %player% actionbar \"&7請切換到 &e槽位\" + slot");
                  FayCoreMacroEngine.autoFindAndInjectVCommand(
                     mc, "execute as %player% at @s run playsound minecraft:item.bundle.insert_fail player @s ~ ~ ~ 5 1"
                  );
                  return false;
            }
         }

         if (event.button() == 1) {
            if (slot != CurrentSlot) {
               FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "title %player% actionbar \"&7請切換到 &e槽位\" + slot");
               FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "execute as %player% at @s run playsound minecraft:item.bundle.insert_fail player @s ~ ~ ~ 5 1");
               return false;
            }

            switch (slot) {
               case 0:
                  FayCoreMacroEngine.autoFindAndInjectVCommand(
                     mc, "execute as %player% at @s run playsound minecraft:item.bundle.insert_fail player @s ~ ~ ~ 5 1"
                  );
                  break;
               case 1:
                  FayCoreMacroEngine.autoFindAndInjectVCommand(
                     mc, "execute as %player% at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1 1"
                  );
                  LaserMode = (LaserMode + 1) % 4;
                  FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "title %player% actionbar {text:\"\"}");
                  break;
               case 2:
                  GunMode = (GunMode + 1) % 3;
                  FayCoreMacroEngine.autoFindAndInjectVCommand(
                     mc, "execute as %player% at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1 1"
                  );
                  break;
               case 3:
                  mc.setScreen(new FayCoreControlScreen());
               case 4:
               case 6:
               default:
                  break;
               case 5:
                  FayCoreMacroEngine.autoFindAndInjectVCommand(
                     mc, "execute as %player% at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1 1"
                  );
                  WingMode = (WingMode + 1) % 5;
                  FayCoreWings.enableWings = true;
            }
         }

         return true;
      } else {
         return super.mouseClicked(event, doubleClick);
      }
   }

   private int getHoveredSlot(double mouseX, double mouseY) {
      int slotWidth = 29;
      int slotHeight = 24;
      int gap = -1;
      int x = this.width - slotWidth - 8;
      int startY = (this.height - (slotHeight * SkillState.MAX + gap * 4)) / 2;
      if (mouseX >= (double)x && mouseX <= (double)(x + slotWidth)) {
         for (int i = 0; i < SkillState.MAX; i++) {
            int y = startY + i * (slotHeight + gap);
            if (mouseY >= (double)y && mouseY <= (double)(y + slotHeight)) {
               return i;
            }
         }
      }

      return -1;
   }
}
