/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.CommandSuggestions
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.Tooltip
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 */
package net.mcreator.faycore.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.mcreator.faycore.client.gui.FayCoreMacroEngine;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class FayCoreMacroScreen
extends Screen {
    private CommandSuggestions commandSuggestions;
    private final List<EditBox> cmdBoxes = new ArrayList<EditBox>();
    private final List<EditBox> delayBoxes = new ArrayList<EditBox>();
    private Button groupSelectButton;
    private Button modeToggleButton;
    private Button toggleEnableButton;
    public static int scrollOffsetIndex = 0;

    public FayCoreMacroScreen() {
        super((Component)Component.literal((String)"FayCore Cmd Macro"));
        FayCoreMacroEngine.loadGroupsFromDisk();
    }

    protected void init() {
        int actualDataIndex;
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
        for (int i = 0; i != displayLinesLimit && (actualDataIndex = scrollOffsetIndex + i) < totalLinesCount; ++i) {
            FayCoreMacroEngine.GroupLine lineData = activeList.get(actualDataIndex);
            EditBox cmdBox = new EditBox(this.font, leftX, startY + i * spacingY, dynamicCmdWidth, 16, (Component)Component.literal((String)""));
            cmdBox.setMaxLength(128);
            String rawCommandText = lineData.command;
            MutableComponent coloredComponent = Component.empty();
            Pattern colorPattern = Pattern.compile("(%[a-zA-Z0-9\\-]+%)|(\\b(say|tp|execute|setblock|fill|summon|tag|playsound|particle)\\b)");
            Matcher matcher = colorPattern.matcher(rawCommandText);
            int lastIdx = 0;
            while (matcher.find()) {
                String matchedText;
                if (matcher.start() > lastIdx) {
                    coloredComponent.append((Component)Component.literal((String)rawCommandText.substring(lastIdx, matcher.start())).withStyle(ChatFormatting.GRAY));
                }
                if ((matchedText = matcher.group()).startsWith("%") && matchedText.endsWith("%")) {
                    coloredComponent.append((Component)Component.literal((String)matchedText).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD));
                } else {
                    coloredComponent.append((Component)Component.literal((String)matchedText).withStyle(ChatFormatting.AQUA));
                }
                lastIdx = matcher.end();
            }
            if (lastIdx < rawCommandText.length()) {
                coloredComponent.append((Component)Component.literal((String)rawCommandText.substring(lastIdx)).withStyle(ChatFormatting.GRAY));
            }
            cmdBox.setValue(coloredComponent.getString());
            cmdBox.setResponder(text -> {
                if (actualDataIndex < activeList.size()) {
                    ((FayCoreMacroEngine.GroupLine)activeList.get((int)actualDataIndex)).command = text;
                    FayCoreMacroEngine.saveGroupsToDisk();
                }
            });
            this.addRenderableWidget((GuiEventListener)cmdBox);
            this.cmdBoxes.add(cmdBox);
            int delX = leftX + dynamicCmdWidth + 5;
            EditBox delBox = new EditBox(this.font, delX, startY + i * spacingY, 30, 16, (Component)Component.literal((String)""));
            delBox.setValue(String.valueOf(lineData.tickDelay));
            delBox.setResponder(text -> {
                if (actualDataIndex < activeList.size()) {
                    try {
                        ((FayCoreMacroEngine.GroupLine)activeList.get((int)actualDataIndex)).tickDelay = Integer.parseInt(text);
                        FayCoreMacroEngine.saveGroupsToDisk();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            });
            this.addRenderableWidget((GuiEventListener)delBox);
            this.delayBoxes.add(delBox);
            int reorderBtnX = delX + 35;
            Button upBtn = Button.builder((Component)Component.literal((String)"\u00a76\u25b2"), btn -> {
                if (actualDataIndex > 0) {
                    Collections.swap(activeList, actualDataIndex, actualDataIndex - 1);
                    if (actualDataIndex == scrollOffsetIndex && scrollOffsetIndex > 0) {
                        --scrollOffsetIndex;
                    }
                    FayCoreMacroEngine.saveGroupsToDisk();
                    this.minecraft.setScreen((Screen)this);
                }
            }).bounds(reorderBtnX, startY + i * spacingY, 15, 16).build();
            if (actualDataIndex == 0) {
                upBtn.active = false;
            } else {
                upBtn.setTooltip(Tooltip.create((Component)Component.literal((String)"\u00a76\u5c07\u6307\u4ee4\u5411\u4e0a\u79fb\u52d5\u4e00\u884c")));
            }
            this.addRenderableWidget((GuiEventListener)upBtn);
            Button downBtn = Button.builder((Component)Component.literal((String)"\u00a76\u25bc"), btn -> {
                if (actualDataIndex < activeList.size() - 1) {
                    Collections.swap(activeList, actualDataIndex, actualDataIndex + 1);
                    if (actualDataIndex == scrollOffsetIndex + maxLinesPerPage - 1) {
                        ++scrollOffsetIndex;
                    }
                    FayCoreMacroEngine.saveGroupsToDisk();
                    this.minecraft.setScreen((Screen)this);
                }
            }).bounds(reorderBtnX + 16, startY + i * spacingY, 15, 16).build();
            if (actualDataIndex == activeList.size() - 1) {
                downBtn.active = false;
            } else {
                downBtn.setTooltip(Tooltip.create((Component)Component.literal((String)"\u00a76\u5c07\u6307\u4ee4\u5411\u4e0b\u79fb\u52d5\u4e00\u884c")));
            }
            this.addRenderableWidget((GuiEventListener)downBtn);
            int delBtnX = reorderBtnX + 33;
            Button deleteBtn = Button.builder((Component)Component.literal((String)"\u00a7cX"), btn -> {
                if (activeList.size() > 1) {
                    activeList.remove(actualDataIndex);
                    if (scrollOffsetIndex + maxLinesPerPage > activeList.size()) {
                        scrollOffsetIndex = Math.max(0, activeList.size() - maxLinesPerPage);
                    }
                    FayCoreMacroEngine.saveGroupsToDisk();
                    this.minecraft.setScreen((Screen)this);
                }
            }).bounds(delBtnX, startY + i * spacingY, 18, 16).build();
            if (activeList.size() == 1) {
                deleteBtn.active = false;
            } else {
                deleteBtn.setTooltip(Tooltip.create((Component)Component.literal((String)"\u00a7c\u79fb\u9664\u6b64\u884c\u6307\u4ee4\n\u00a77(\u6ce8\u610f\uff1a\u81f3\u5c11\u9700\u4fdd\u7559 1 \u884c)")));
            }
            this.addRenderableWidget((GuiEventListener)deleteBtn);
        }
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.literal((String)"\u00a7aNew Command +"), btn -> {
            activeList.add(new FayCoreMacroEngine.GroupLine("Command", 5));
            if (activeList.size() > maxLinesPerPage) {
                scrollOffsetIndex = activeList.size() - maxLinesPerPage;
            }
            this.minecraft.setScreen((Screen)this);
        }).bounds(rightPanelX, startY, 110, 16).build());
        int var10000 = FayCoreMacroEngine.selectedGroupIndex + 1;
        String groupBtnTxt = "Group: " + var10000 + " / " + FayCoreMacroEngine.totalGroupsCount;
        this.groupSelectButton = Button.builder((Component)Component.literal((String)groupBtnTxt), btn -> {
            FayCoreMacroEngine.selectedGroupIndex = (FayCoreMacroEngine.selectedGroupIndex + 1) % FayCoreMacroEngine.totalGroupsCount;
            scrollOffsetIndex = 0;
            this.minecraft.setScreen((Screen)this);
        }).bounds(rightPanelX, startY + 22, 110, 16).build();
        this.addRenderableWidget((GuiEventListener)this.groupSelectButton);
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.literal((String)"Group +"), btn -> {
            if (FayCoreMacroEngine.totalGroupsCount != 10) {
                ++FayCoreMacroEngine.totalGroupsCount;
                this.minecraft.setScreen((Screen)this);
            }
        }).bounds(rightPanelX, startY + 42, 53, 16).build());
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.literal((String)"Group -"), btn -> {
            if (FayCoreMacroEngine.totalGroupsCount != 1) {
                --FayCoreMacroEngine.totalGroupsCount;
                if (FayCoreMacroEngine.selectedGroupIndex != 0) {
                    --FayCoreMacroEngine.selectedGroupIndex;
                }
                scrollOffsetIndex = 0;
                this.minecraft.setScreen((Screen)this);
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
        this.modeToggleButton = Button.builder((Component)Component.literal((String)("Mode: " + modeText)), btn -> {
            int nextMode = (FayCoreMacroEngine.groupModes.get(FayCoreMacroEngine.selectedGroupIndex) + 1) % 3;
            FayCoreMacroEngine.groupModes.set(FayCoreMacroEngine.selectedGroupIndex, nextMode);
            FayCoreMacroEngine.saveGroupsToDisk();
            this.minecraft.setScreen((Screen)this);
        }).bounds(rightPanelX, startY + 64, 110, 16).build();
        this.modeToggleButton.setTooltip(Tooltip.create((Component)Component.literal((String)"\u00a7bMode\n\u00a77Once: when you press, it will execute once time.\n\u00a77Repeat: when you hold, it will keep execute.\n\u00a77Auto: whatever it always execute.")));
        this.addRenderableWidget((GuiEventListener)this.modeToggleButton);
        boolean curEnable = FayCoreMacroEngine.groupEnables.get(FayCoreMacroEngine.selectedGroupIndex);
        String enableText = curEnable ? "\u00a76State:\u00a7a Enable" : "\u00a76State:\u00a7c Disable";
        this.toggleEnableButton = Button.builder((Component)Component.literal((String)enableText), btn -> {
            boolean nextState = !curEnable;
            FayCoreMacroEngine.groupEnables.set(FayCoreMacroEngine.selectedGroupIndex, nextState);
            if (nextState) {
                FayCoreMacroEngine.isMacroRunning = false;
                FayCoreMacroEngine.isSingleTriggerLocked = false;
                FayCoreMacroEngine.matrixTickCooldowns.set(FayCoreMacroEngine.selectedGroupIndex, 0);
            }
            FayCoreMacroEngine.saveGroupsToDisk();
            this.minecraft.setScreen((Screen)this);
        }).bounds(rightPanelX, startY + 84, 110, 16).build();
        this.addRenderableWidget((GuiEventListener)this.toggleEnableButton);
        this.toggleEnableButton.setTooltip(Tooltip.create((Component)Component.literal((String)"\u00a7bState\n\u00a77Click to toggle")));
        this.addRenderableWidget((GuiEventListener)this.toggleEnableButton);
        if (!this.cmdBoxes.isEmpty()) {
            this.commandSuggestions = new CommandSuggestions(this.minecraft, (Screen)this, this.cmdBoxes.get(0), this.font, true, true, 0, 7, false, Integer.MIN_VALUE);
            this.commandSuggestions.updateCommandInfo();
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalScroll, double verticalScroll) {
        List<FayCoreMacroEngine.GroupLine> activeList = FayCoreMacroEngine.dynamicGroupStorage.get(FayCoreMacroEngine.selectedGroupIndex);
        int totalLinesCount = activeList.size();
        if (totalLinesCount <= 18) {
            return super.mouseScrolled(mouseX, mouseY, horizontalScroll, verticalScroll);
        }
        if (mouseX < (double)(this.width - 140)) {
            if (verticalScroll > 0.0) {
                --scrollOffsetIndex;
            } else if (verticalScroll < 0.0) {
                ++scrollOffsetIndex;
            }
            if (scrollOffsetIndex + 18 > totalLinesCount) {
                scrollOffsetIndex = totalLinesCount - 18;
            }
            if (scrollOffsetIndex < 0) {
                scrollOffsetIndex = 0;
            }
            this.saveCurrentInputs();
            if (this.minecraft != null) {
                this.minecraft.setScreen((Screen)this);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalScroll, verticalScroll);
    }

    public void saveCurrentInputs() {
        try {
            List<FayCoreMacroEngine.GroupLine> activeList = FayCoreMacroEngine.dynamicGroupStorage.get(FayCoreMacroEngine.selectedGroupIndex);
            int displayCount = this.cmdBoxes.size();
            for (int i = 0; i != displayCount; ++i) {
                int actualDataIndex = scrollOffsetIndex + i;
                if (actualDataIndex >= activeList.size()) continue;
                activeList.get((int)actualDataIndex).command = this.cmdBoxes.get(i).getValue();
                activeList.get((int)actualDataIndex).tickDelay = Integer.parseInt(this.delayBoxes.get(i).getValue().trim());
            }
            FayCoreMacroEngine.saveGroupsToDisk();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void extractRenderState(GuiGraphicsExtractor g, int mX, int mY, float pT) {
        super.extractRenderState(g, mX, mY, pT);
    }

    public void extractBackground(GuiGraphicsExtractor g, int mX, int mY, float pT) {
        super.extractBackground(g, mX, mY, pT);
        g.text(this.font, (Component)Component.literal((String)"\u00a76\u00a7lFayCore \u7121\u9650\u5236\u91cd\u578b\u6ed1\u52d5\u4e3b\u63a7\u53f0"), 20, 10, 0xFFFFFF, false);
        List<FayCoreMacroEngine.GroupLine> activeList = FayCoreMacroEngine.dynamicGroupStorage.get(FayCoreMacroEngine.selectedGroupIndex);
        int var10000 = activeList.size();
        String infoText = "\u00a77[\u6efe\u8f2a\u4e0a\u4e0b\u6ed1\u52d5] \u6307\u4ee4\u7e3d\u6578: \u00a7e" + var10000 + " \u884c \u00a77(\u76ee\u524d\u986f\u793a\u7b2c \u00a7b" + (scrollOffsetIndex + 1) + " \u00a77\u884c\u8d77)";
        g.text(this.font, (Component)Component.literal((String)infoText), 20, this.height - 20, 0xAAAAAA, false);
        int totalLines = activeList.size();
        if (totalLines > 18) {
            int scrollbarX = this.width - 150;
            int trackY = 20;
            int trackHeight = 324;
            g.fill(scrollbarX, trackY, scrollbarX + 4, trackY + trackHeight, 0x33FFFFFF);
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
        }
        if (event.key() == 256) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return this.commandSuggestions != null && this.commandSuggestions.mouseClicked(event) ? true : super.mouseClicked(event, doubleClick);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

