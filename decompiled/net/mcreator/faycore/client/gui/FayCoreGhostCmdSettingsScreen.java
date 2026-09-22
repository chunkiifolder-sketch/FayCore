/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.reflect.TypeToken
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.AbstractSelectionList$Entry
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.ContainerObjectSelectionList
 *  net.minecraft.client.gui.components.ContainerObjectSelectionList$Entry
 *  net.minecraft.client.gui.components.MultiLineEditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.narration.NarratableEntry
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.sounds.SoundEvents
 */
package net.mcreator.faycore.client.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.mcreator.faycore.client.gui.FayCoreFastRun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class FayCoreGhostCmdSettingsScreen
extends Screen {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private int currentPage = 1;
    private final int maxPages = 20;
    private final List<String> ghostCommandList = new ArrayList<String>();
    private GhostCommandScrollList scrollList;
    private Button buttonAdd;
    private Button buttonConfirm;
    private Button buttonPrevPage;
    private Button buttonNextPage;
    private Button modeToggleButton;
    public static boolean IsLoop = false;

    public FayCoreGhostCmdSettingsScreen() {
        super((Component)Component.literal((String)"FayCore FastRun Cmd List"));
        this.loadCommandsFromLocal();
    }

    private File getPageFile(int page) {
        return new File(Minecraft.getInstance().gameDirectory, "config/faycore_ghost_page_" + page + ".json");
    }

    protected void init() {
        super.init();
        String initialConfirmText = FayCoreFastRun.IsLoop ? "Stop" : "\ud83d\ude80 Run";
        this.buttonConfirm = Button.builder((Component)Component.literal((String)initialConfirmText), btn -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen((Screen)null);
            }
            this.saveCommandsToLocal();
            if (btn.getMessage().getString().equals("Stop")) {
                FayCoreFastRun.IsLoop = false;
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.modeToggleButton.setMessage((Component)Component.literal((String)"\u00a7dLoop"));
                    IsLoop = true;
                    this.minecraft.player.playSound(SoundEvents.IRON_DOOR_CLOSE, 1.0f, 1.5f);
                    this.minecraft.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a7aStopped FastRun."));
                }
                btn.setMessage((Component)Component.literal((String)"\ud83d\ude80 Run"));
                return;
            }
            if (FayCoreFastRun.IsLoop) {
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.playSound(SoundEvents.ENDER_DRAGON_SHOOT, 1.0f, 1.2f);
                    this.minecraft.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a7aStarted a loop FastRun"));
                }
                FayCoreFastRun.fireGhostPayloadQueue(new ArrayList<String>(this.ghostCommandList));
                btn.setMessage((Component)Component.literal((String)"Stop"));
            } else {
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.playSound(SoundEvents.ARROW_SHOOT, 1.0f, 1.2f);
                    this.minecraft.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a7aStarted a FastRun."));
                }
                FayCoreFastRun.fireGhostPayloadQueue(new ArrayList<String>(this.ghostCommandList));
                if (this.minecraft != null) {
                    this.minecraft.setScreen((Screen)null);
                }
            }
        }).bounds(this.width / 2 - 45, this.height - 45, 90, 20).build();
        this.addRenderableWidget((GuiEventListener)this.buttonConfirm);
        String currentModeText = FayCoreFastRun.IsLoop ? "\u00a7dLoop" : "\u00a76Once";
        this.modeToggleButton = Button.builder((Component)Component.literal((String)currentModeText), button -> {
            boolean bl = FayCoreFastRun.IsLoop = !FayCoreFastRun.IsLoop;
            if (FayCoreFastRun.IsLoop) {
                button.setMessage((Component)Component.literal((String)"\u00a7dLoop"));
            } else {
                button.setMessage((Component)Component.literal((String)"\u00a76Once"));
            }
        }).bounds(this.width / 2 - 360, this.height - 45, 90, 20).build();
        this.addRenderableWidget((GuiEventListener)this.modeToggleButton);
        this.scrollList = new GhostCommandScrollList(this, this.minecraft, this.width, this.height - 100, 40, 50);
        this.addRenderableWidget((GuiEventListener)this.scrollList);
        this.buttonAdd = Button.builder((Component)Component.literal((String)"\uff0b Add Cmd"), btn -> {
            this.ghostCommandList.add("");
            this.updateScrollList();
        }).bounds(this.width / 2 - 195, this.height - 45, 80, 20).build();
        this.addRenderableWidget((GuiEventListener)this.buttonAdd);
        this.buttonPrevPage = Button.builder((Component)Component.literal((String)"\u25c0 Previous"), btn -> {
            if (this.currentPage > 1) {
                this.saveCommandsToLocal();
                --this.currentPage;
                this.loadCommandsFromLocal();
                this.updateScrollList();
                this.updateButtonStates();
            }
        }).bounds(this.width / 2 - 110, this.height - 45, 60, 20).build();
        this.addRenderableWidget((GuiEventListener)this.buttonPrevPage);
        this.buttonNextPage = Button.builder((Component)Component.literal((String)"Next \u25b6"), btn -> {
            if (this.currentPage < this.maxPages) {
                this.saveCommandsToLocal();
                ++this.currentPage;
                this.loadCommandsFromLocal();
                this.updateScrollList();
                this.updateButtonStates();
            }
        }).bounds(this.width / 2 + 50, this.height - 45, 60, 20).build();
        this.addRenderableWidget((GuiEventListener)this.buttonNextPage);
        this.updateScrollList();
        this.updateButtonStates();
    }

    public void updateButtonStates() {
        if (this.buttonPrevPage != null) {
            boolean bl = this.buttonPrevPage.active = this.currentPage > 1;
        }
        if (this.buttonNextPage != null) {
            this.buttonNextPage.active = this.currentPage < this.maxPages;
        }
    }

    public void updateScrollList() {
        if (this.scrollList != null) {
            this.scrollList.refreshEntries();
        }
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        String pageInfoStr = String.format("\u00a7d\u00a7lFayCore FastRun \u00a7e(Page: %d / %d)", this.currentPage, this.maxPages);
        guiGraphics.text(this.font, (Component)Component.literal((String)pageInfoStr), this.width / 2 - 120, 15, 0xFFFFFF, true);
    }

    public boolean isPauseScreen() {
        return false;
    }

    private void saveCommandsToLocal() {
        File pageFile = this.getPageFile(this.currentPage);
        try {
            if (!pageFile.getParentFile().exists()) {
                pageFile.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(pageFile);){
                GSON.toJson(this.ghostCommandList, (Appendable)writer);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private void loadCommandsFromLocal() {
        File pageFile = this.getPageFile(this.currentPage);
        this.ghostCommandList.clear();
        if (!pageFile.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(pageFile);){
            List loaded = (List)GSON.fromJson((Reader)reader, new TypeToken<List>(this){
                {
                    Objects.requireNonNull(this$0);
                }
            }.getType());
            if (loaded != null) {
                this.ghostCommandList.addAll(loaded);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private class GhostCommandScrollList
    extends ContainerObjectSelectionList<GhostCommandEntry> {
        final /* synthetic */ FayCoreGhostCmdSettingsScreen this$0;

        public GhostCommandScrollList(FayCoreGhostCmdSettingsScreen fayCoreGhostCmdSettingsScreen, Minecraft minecraft, int width, int height, int top, int itemHeight) {
            FayCoreGhostCmdSettingsScreen fayCoreGhostCmdSettingsScreen2 = fayCoreGhostCmdSettingsScreen;
            Objects.requireNonNull(fayCoreGhostCmdSettingsScreen2);
            this.this$0 = fayCoreGhostCmdSettingsScreen2;
            super(minecraft, width, height, top, itemHeight);
        }

        public int getRowWidth() {
            return 360;
        }

        public void refreshEntries() {
            this.clearEntries();
            for (int i = 0; i < this.this$0.ghostCommandList.size(); ++i) {
                this.addEntry((AbstractSelectionList.Entry)new GhostCommandEntry(this.this$0, i, this.this$0.ghostCommandList.get(i)));
            }
        }
    }

    private class GhostCommandEntry
    extends ContainerObjectSelectionList.Entry<GhostCommandEntry> {
        private final int index;
        private final MultiLineEditBox multiLineBox;
        private final Button btnUp;
        private final Button btnDown;
        private final Button btnDel;
        private final List<GuiEventListener> children;
        private final List<NarratableEntry> narratables;
        final /* synthetic */ FayCoreGhostCmdSettingsScreen this$0;

        public GhostCommandEntry(FayCoreGhostCmdSettingsScreen fayCoreGhostCmdSettingsScreen, int index, String currentText) {
            FayCoreGhostCmdSettingsScreen fayCoreGhostCmdSettingsScreen2 = fayCoreGhostCmdSettingsScreen;
            Objects.requireNonNull(fayCoreGhostCmdSettingsScreen2);
            this.this$0 = fayCoreGhostCmdSettingsScreen2;
            this.children = new ArrayList<GuiEventListener>();
            this.narratables = new ArrayList<NarratableEntry>();
            this.index = index;
            int startX = fayCoreGhostCmdSettingsScreen.width / 2 - 170;
            this.multiLineBox = MultiLineEditBox.builder().setX(startX).setY(0).setPlaceholder((Component)Component.literal((String)"Type a cmd...")).build(fayCoreGhostCmdSettingsScreen.font, 190, 30, (Component)Component.literal((String)"FastRun"));
            this.multiLineBox.setCharacterLimit(Short.MAX_VALUE);
            this.multiLineBox.setValue(currentText);
            this.multiLineBox.setValueListener(text -> this$0.ghostCommandList.set(index, (String)text));
            this.children.add((GuiEventListener)this.multiLineBox);
            this.narratables.add((NarratableEntry)this.multiLineBox);
            this.btnUp = Button.builder((Component)Component.literal((String)"\u25b2"), btn -> {
                if (index > 0) {
                    fayCoreGhostCmdSettingsScreen.saveCommandsToLocal();
                    Collections.swap(this$0.ghostCommandList, index, index - 1);
                    fayCoreGhostCmdSettingsScreen.updateScrollList();
                }
            }).bounds(startX + 195, 0, 20, 14).build();
            this.btnUp.active = index > 0;
            this.children.add((GuiEventListener)this.btnUp);
            this.narratables.add((NarratableEntry)this.btnUp);
            this.btnDown = Button.builder((Component)Component.literal((String)"\u25bc"), btn -> {
                if (index < this$0.ghostCommandList.size() - 1) {
                    fayCoreGhostCmdSettingsScreen.saveCommandsToLocal();
                    Collections.swap(this$0.ghostCommandList, index, index + 1);
                    fayCoreGhostCmdSettingsScreen.updateScrollList();
                }
            }).bounds(startX + 217, 0, 20, 14).build();
            this.btnDown.active = index < fayCoreGhostCmdSettingsScreen.ghostCommandList.size() - 1;
            this.children.add((GuiEventListener)this.btnDown);
            this.narratables.add((NarratableEntry)this.btnDown);
            this.btnDel = Button.builder((Component)Component.literal((String)"\u522a"), btn -> {
                this$0.ghostCommandList.remove(index);
                fayCoreGhostCmdSettingsScreen.updateScrollList();
            }).bounds(startX + 240, 0, 20, 14).build();
            this.children.add((GuiEventListener)this.btnDel);
            this.narratables.add((NarratableEntry)this.btnDel);
        }

        public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean isHovered, float partialTicks) {
            int currentY = this.this$0.scrollList.getRowTop(this.index);
            this.multiLineBox.setY(currentY + 4);
            this.btnUp.setY(currentY + 5);
            this.btnDown.setY(currentY + 23);
            this.btnDel.setY(currentY + 14);
            this.multiLineBox.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
            this.btnUp.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
            this.btnDown.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
            this.btnDel.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        }

        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        public List<? extends NarratableEntry> narratables() {
            return this.narratables;
        }
    }
}

