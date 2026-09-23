package chunk.faye.mod_tog.faycore.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class FayCoreMacroScreen extends Screen {
   private CommandSuggestions commandSuggestions;
   private final List<EditBox> cmdBoxes = new ArrayList<>();
   private final List<EditBox> delayBoxes = new ArrayList<>();
   private Button groupSelectButton;
   private Button modeToggleButton;
   private Button toggleEnableButton;
   public static int scrollOffsetIndex = 0;

   public FayCoreMacroScreen() {
      super(Component.literal("FayCore Cmd Macro"));
      FayCoreMacroEngine.loadGroupsFromDisk();
   }

   protected void init() {
      super.init();
      this.cmdBoxes.clear();
      this.delayBoxes.clear();
      int leftX = 25;
      int rightPanelX = this.width - 135;
      int startY = 20;
      int spacingY = 18;
      List<FayCoreMacroEngine.GroupLine> activeList = FayCoreMacroEngine.dynamicGroupStorage.get(FayCoreMacroEngine.selectedGroupIndex);
      int totalLinesCount = activeList.size();
      int maxLinesPerPage = 18;
      if (totalLinesCount <= maxLinesPerPage) {
         scrollOffsetIndex = 0;
      } else if (scrollOffsetIndex + maxLinesPerPage > totalLinesCount) {
         scrollOffsetIndex = totalLinesCount - maxLinesPerPage;
      }

      if (scrollOffsetIndex < 0) {
         scrollOffsetIndex = 0;
      }

      int displayLinesLimit = Math.min(maxLinesPerPage, totalLinesCount);
      int dynamicCmdWidth = this.width - leftX - 197 - (this.width - rightPanelX) - 10;
      if (dynamicCmdWidth < 140) {
         dynamicCmdWidth = 140;
      }

      for (int i = 0; i != displayLinesLimit; i++) {
         int actualDataIndex = scrollOffsetIndex + i;
         if (actualDataIndex >= totalLinesCount) {
            break;
         }

         FayCoreMacroEngine.GroupLine lineData = activeList.get(actualDataIndex);
         EditBox cmdBox = new EditBox(this.font, leftX, startY + i * spacingY, dynamicCmdWidth, 16, Component.literal(""));
         cmdBox.setMaxLength(128);
         String rawCommandText = lineData.command;
         MutableComponent coloredComponent = Component.empty();
         Pattern colorPattern = Pattern.compile("(%[a-zA-Z0-9\\-]+%)|(\\b(say|tp|execute|setblock|fill|summon|tag|playsound|particle)\\b)");
         Matcher matcher = colorPattern.matcher(rawCommandText);

         int lastIdx;
         for (lastIdx = 0; matcher.find(); lastIdx = matcher.end()) {
            if (matcher.start() > lastIdx) {
               coloredComponent.append(Component.literal(rawCommandText.substring(lastIdx, matcher.start())).withStyle(ChatFormatting.GRAY));
            }

            String matchedText = matcher.group();
            if (matchedText.startsWith("%") && matchedText.endsWith("%")) {
               coloredComponent.append(Component.literal(matchedText).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD));
            } else {
               coloredComponent.append(Component.literal(matchedText).withStyle(ChatFormatting.AQUA));
            }
         }

         if (lastIdx < rawCommandText.length()) {
            coloredComponent.append(Component.literal(rawCommandText.substring(lastIdx)).withStyle(ChatFormatting.GRAY));
         }

         cmdBox.setValue(coloredComponent.getString());
         cmdBox.setResponder(text -> {
            if (actualDataIndex < activeList.size()) {
               activeList.get(actualDataIndex).command = text;
               FayCoreMacroEngine.saveGroupsToDisk();
            }
         });
         this.addRenderableWidget(cmdBox);
         this.cmdBoxes.add(cmdBox);
         int delX = leftX + dynamicCmdWidth + 5;
         EditBox delBox = new EditBox(this.font, delX, startY + i * spacingY, 30, 16, Component.literal(""));
         delBox.setValue(String.valueOf(lineData.tickDelay));
         delBox.setResponder(text -> {
            if (actualDataIndex < activeList.size()) {
               try {
                  activeList.get(actualDataIndex).tickDelay = Integer.parseInt(text);
                  FayCoreMacroEngine.saveGroupsToDisk();
               } catch (Exception var4x) {
               }
            }
         });
         this.addRenderableWidget(delBox);
         this.delayBoxes.add(delBox);
         int reorderBtnX = delX + 35;
         Button upBtn = Button.builder(Component.literal("§6▲"), btn -> {
            if (actualDataIndex > 0) {
               Collections.swap(activeList, actualDataIndex, actualDataIndex - 1);
               if (actualDataIndex == scrollOffsetIndex && scrollOffsetIndex > 0) {
                  scrollOffsetIndex--;
               }

               FayCoreMacroEngine.saveGroupsToDisk();
               this.minecraft.setScreen(this);
            }
         }).bounds(reorderBtnX, startY + i * spacingY, 15, 16).build();
         if (actualDataIndex == 0) {
            upBtn.active = false;
         } else {
            upBtn.setTooltip(Tooltip.create(Component.literal("§6將指令向上移動一行")));
         }

         this.addRenderableWidget(upBtn);
         Button downBtn = Button.builder(Component.literal("§6▼"), btn -> {
            if (actualDataIndex < activeList.size() - 1) {
               Collections.swap(activeList, actualDataIndex, actualDataIndex + 1);
               if (actualDataIndex == scrollOffsetIndex + maxLinesPerPage - 1) {
                  scrollOffsetIndex++;
               }

               FayCoreMacroEngine.saveGroupsToDisk();
               this.minecraft.setScreen(this);
            }
         }).bounds(reorderBtnX + 16, startY + i * spacingY, 15, 16).build();
         if (actualDataIndex == activeList.size() - 1) {
            downBtn.active = false;
         } else {
            downBtn.setTooltip(Tooltip.create(Component.literal("§6將指令向下移動一行")));
         }

         this.addRenderableWidget(downBtn);
         int delBtnX = reorderBtnX + 33;
         Button deleteBtn = Button.builder(Component.literal("§cX"), btn -> {
            if (activeList.size() > 1) {
               activeList.remove(actualDataIndex);
               if (scrollOffsetIndex + maxLinesPerPage > activeList.size()) {
                  scrollOffsetIndex = Math.max(0, activeList.size() - maxLinesPerPage);
               }

               FayCoreMacroEngine.saveGroupsToDisk();
               this.minecraft.setScreen(this);
            }
         }).bounds(delBtnX, startY + i * spacingY, 18, 16).build();
         if (activeList.size() == 1) {
            deleteBtn.active = false;
         } else {
            deleteBtn.setTooltip(Tooltip.create(Component.literal("§c移除此行指令\n§7(注意：至少需保留 1 行)")));
         }

         this.addRenderableWidget(deleteBtn);
      }

      this.addRenderableWidget(Button.builder(Component.literal("§aNew Command +"), btn -> {
         activeList.add(new FayCoreMacroEngine.GroupLine("Command", 5));
         if (activeList.size() > maxLinesPerPage) {
            scrollOffsetIndex = activeList.size() - maxLinesPerPage;
         }

         this.minecraft.setScreen(this);
      }).bounds(rightPanelX, startY, 110, 16).build());
      int var10000 = FayCoreMacroEngine.selectedGroupIndex + 1;
      String groupBtnTxt = "Group: " + var10000 + " / " + FayCoreMacroEngine.totalGroupsCount;
      this.groupSelectButton = Button.builder(Component.literal(groupBtnTxt), btn -> {
         FayCoreMacroEngine.selectedGroupIndex = (FayCoreMacroEngine.selectedGroupIndex + 1) % FayCoreMacroEngine.totalGroupsCount;
         scrollOffsetIndex = 0;
         this.minecraft.setScreen(this);
      }).bounds(rightPanelX, startY + 22, 110, 16).build();
      this.addRenderableWidget(this.groupSelectButton);
      this.addRenderableWidget(Button.builder(Component.literal("Group +"), btn -> {
         if (FayCoreMacroEngine.totalGroupsCount != 10) {
            FayCoreMacroEngine.totalGroupsCount++;
            this.minecraft.setScreen(this);
         }
      }).bounds(rightPanelX, startY + 42, 53, 16).build());
      this.addRenderableWidget(Button.builder(Component.literal("Group -"), btn -> {
         if (FayCoreMacroEngine.totalGroupsCount != 1) {
            FayCoreMacroEngine.totalGroupsCount--;
            if (FayCoreMacroEngine.selectedGroupIndex != 0) {
               FayCoreMacroEngine.selectedGroupIndex--;
            }

            scrollOffsetIndex = 0;
            this.minecraft.setScreen(this);
         }
      }).bounds(rightPanelX + 57, startY + 42, 53, 16).build());
      int curMode = FayCoreMacroEngine.groupModes.get(FayCoreMacroEngine.selectedGroupIndex);
      String modeText = "Once";
      if (curMode == 1) {
         modeText = "Repeat";
      }

      if (curMode == 2) {
         modeText = "Auto";
      }

      this.modeToggleButton = Button.builder(Component.literal("Mode: " + modeText), btn -> {
         int nextMode = (FayCoreMacroEngine.groupModes.get(FayCoreMacroEngine.selectedGroupIndex) + 1) % 3;
         FayCoreMacroEngine.groupModes.set(FayCoreMacroEngine.selectedGroupIndex, nextMode);
         FayCoreMacroEngine.saveGroupsToDisk();
         this.minecraft.setScreen(this);
      }).bounds(rightPanelX, startY + 64, 110, 16).build();
      this.modeToggleButton
         .setTooltip(
            Tooltip.create(
               Component.literal(
                  "§bMode\n§7Once: when you press, it will execute once time.\n§7Repeat: when you hold, it will keep execute.\n§7Auto: whatever it always execute."
               )
            )
         );
      this.addRenderableWidget(this.modeToggleButton);
      boolean curEnable = FayCoreMacroEngine.groupEnables.get(FayCoreMacroEngine.selectedGroupIndex);
      String enableText = curEnable ? "§6State:§a Enable" : "§6State:§c Disable";
      this.toggleEnableButton = Button.builder(Component.literal(enableText), btn -> {
         boolean nextState = !curEnable;
         FayCoreMacroEngine.groupEnables.set(FayCoreMacroEngine.selectedGroupIndex, nextState);
         if (nextState) {
            FayCoreMacroEngine.isMacroRunning = false;
            FayCoreMacroEngine.isSingleTriggerLocked = false;
            FayCoreMacroEngine.matrixTickCooldowns.set(FayCoreMacroEngine.selectedGroupIndex, 0);
         }

         FayCoreMacroEngine.saveGroupsToDisk();
         this.minecraft.setScreen(this);
      }).bounds(rightPanelX, startY + 84, 110, 16).build();
      this.addRenderableWidget(this.toggleEnableButton);
      this.toggleEnableButton.setTooltip(Tooltip.create(Component.literal("§bState\n§7Click to toggle")));
      this.addRenderableWidget(this.toggleEnableButton);
      if (!this.cmdBoxes.isEmpty()) {
         this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.cmdBoxes.get(0), this.font, true, true, 0, 7, false, Integer.MIN_VALUE);
         this.commandSuggestions.updateCommandInfo();
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalScroll, double verticalScroll) {
      List<FayCoreMacroEngine.GroupLine> activeList = FayCoreMacroEngine.dynamicGroupStorage.get(FayCoreMacroEngine.selectedGroupIndex);
      int totalLinesCount = activeList.size();
      if (totalLinesCount <= 18) {
         return super.mouseScrolled(mouseX, mouseY, horizontalScroll, verticalScroll);
      } else if (mouseX < (double)(this.width - 140)) {
         if (verticalScroll > 0.0) {
            scrollOffsetIndex--;
         } else if (verticalScroll < 0.0) {
            scrollOffsetIndex++;
         }

         if (scrollOffsetIndex + 18 > totalLinesCount) {
            scrollOffsetIndex = totalLinesCount - 18;
         }

         if (scrollOffsetIndex < 0) {
            scrollOffsetIndex = 0;
         }

         this.saveCurrentInputs();
         if (this.minecraft != null) {
            this.minecraft.setScreen(this);
         }

         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, horizontalScroll, verticalScroll);
      }
   }

   public void saveCurrentInputs() {
      try {
         List<FayCoreMacroEngine.GroupLine> activeList = FayCoreMacroEngine.dynamicGroupStorage.get(FayCoreMacroEngine.selectedGroupIndex);
         int displayCount = this.cmdBoxes.size();

         for (int i = 0; i != displayCount; i++) {
            int actualDataIndex = scrollOffsetIndex + i;
            if (actualDataIndex < activeList.size()) {
               activeList.get(actualDataIndex).command = this.cmdBoxes.get(i).getValue();
               activeList.get(actualDataIndex).tickDelay = Integer.parseInt(this.delayBoxes.get(i).getValue().trim());
            }
         }

         FayCoreMacroEngine.saveGroupsToDisk();
      } catch (Exception var5) {
      }
   }

   public void extractRenderState(GuiGraphicsExtractor g, int mX, int mY, float pT) {
      super.extractRenderState(g, mX, mY, pT);
   }

   public void extractBackground(GuiGraphicsExtractor g, int mX, int mY, float pT) {
      super.extractBackground(g, mX, mY, pT);
      g.text(this.font, Component.literal("§6§lFayCore 無限制重型滑動主控台"), 20, 10, 16777215, false);
      List<FayCoreMacroEngine.GroupLine> activeList = FayCoreMacroEngine.dynamicGroupStorage.get(FayCoreMacroEngine.selectedGroupIndex);
      int var10000 = activeList.size();
      String infoText = "§7[滾輪上下滑動] 指令總數: §e" + var10000 + " 行 §7(目前顯示第 §b" + (scrollOffsetIndex + 1) + " §7行起)";
      g.text(this.font, Component.literal(infoText), 20, this.height - 20, 11184810, false);
      int totalLines = activeList.size();
      if (totalLines > 18) {
         int scrollbarX = this.width - 150;
         int trackY = 20;
         int trackHeight = 324;
         g.fill(scrollbarX, trackY, scrollbarX + 4, trackY + trackHeight, 872415231);
         int maxOffset = totalLines - 18;
         int thumbHeight = Math.max(16, 18 * trackHeight / totalLines);
         int availableTrack = trackHeight - thumbHeight;
         int thumbY = trackY + scrollOffsetIndex * availableTrack / maxOffset;
         g.fill(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight, -23296);
      }
   }

   public boolean keyPressed(KeyEvent event) {
      if (this.commandSuggestions != null && this.commandSuggestions.keyPressed(event)) {
         return true;
      } else if (event.key() == 256) {
         this.onClose();
         return true;
      } else {
         return super.keyPressed(event);
      }
   }

   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      return this.commandSuggestions != null && this.commandSuggestions.mouseClicked(event) ? true : super.mouseClicked(event, doubleClick);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
