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
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.narration.NarratableEntry
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.sounds.SoundEvents
 */
package chunk.faye.mod_tog.faycore.client.gui;

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
import chunk.faye.mod_tog.faycore.client.gui.FaycoreSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class FayCoreRepeatingCmdSettingsScreen
extends Screen {
    private static final File CONFIG_FILE = new File(Minecraft.getInstance().gameDirectory, "config/faycore_commands.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final List<String> commandList = new ArrayList<String>();
    private CommandScrollList scrollList;
    private Button buttonAdd;
    private Button buttonConfirm;

    public FayCoreRepeatingCmdSettingsScreen(Component title) {
        super(title);
        this.loadCommandsFromLocal();
    }

    protected void init() {
        super.init();
        this.scrollList = new CommandScrollList(this, this.minecraft, this.width, this.height - 100, 40, 24);
        this.addRenderableWidget((GuiEventListener)this.scrollList);
        this.buttonAdd = Button.builder((Component)Component.literal((String)"\u00a7a+ Add command"), btn -> {
            this.commandList.add("");
            this.updateScrollList();
        }).bounds(this.width / 2 - 140, this.height - 45, 110, 20).build();
        this.addRenderableWidget((GuiEventListener)this.buttonAdd);
        this.buttonConfirm = Button.builder((Component)Component.literal((String)"\u00a7a\u2714 Save"), btn -> {
            this.saveCommandsToLocal();
            Minecraft mc = Minecraft.getInstance();
            int Max_RCy = Math.max(FaycoreSettingsScreen.posA.getY(), FaycoreSettingsScreen.posB.getY());
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                this.minecraft.player.sendSystemMessage((Component)Component.literal((String)"\u00a7a[FayCore] \u00a77Repeat commands saved."));
            }
            new Thread(() -> {
                try {
                    Thread.sleep(50L);
                    FaycoreSettingsScreen.autoFillCommandsViaPacket();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            if (this.minecraft != null) {
                this.minecraft.setScreen((Screen)null);
            }
        }).bounds(this.width / 2 - 20, this.height - 45, 120, 20).build();
        this.addRenderableWidget((GuiEventListener)this.buttonConfirm);
        this.updateScrollList();
    }

    public void updateScrollList() {
        if (this.scrollList != null) {
            this.scrollList.refreshEntries();
        }
    }

    public boolean isPauseScreen() {
        return false;
    }

    private void saveCommandsToLocal() {
        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE);){
                GSON.toJson(this.commandList, (Appendable)writer);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCommandsFromLocal() {
        if (!CONFIG_FILE.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(CONFIG_FILE);){
            List loaded = (List)GSON.fromJson((Reader)reader, new TypeToken<List<String>>(this){
                {
                    Objects.requireNonNull(this$0);
                }
            }.getType());
            if (loaded != null) {
                this.commandList.clear();
                this.commandList.addAll(loaded);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class CommandScrollList
    extends ContainerObjectSelectionList<CommandEntry> {
        final /* synthetic */ FayCoreRepeatingCmdSettingsScreen this$0;

        public CommandScrollList(FayCoreRepeatingCmdSettingsScreen fayCoreRepeatingCmdSettingsScreen, Minecraft minecraft, int width, int height, int top, int itemHeight) {
            FayCoreRepeatingCmdSettingsScreen fayCoreRepeatingCmdSettingsScreen2 = fayCoreRepeatingCmdSettingsScreen;
            Objects.requireNonNull(fayCoreRepeatingCmdSettingsScreen2);
            this.this$0 = fayCoreRepeatingCmdSettingsScreen2;
            super(minecraft, width, height, top, itemHeight);
        }

        public int getRowWidth() {
            return 360;
        }

        public void refreshEntries() {
            this.clearEntries();
            for (int i = 0; i < this.this$0.commandList.size(); ++i) {
                this.addEntry((AbstractSelectionList.Entry)new CommandEntry(this.this$0, i, this.this$0.commandList.get(i)));
            }
        }
    }

    private class CommandEntry
    extends ContainerObjectSelectionList.Entry<CommandEntry> {
        private final int index;
        private final EditBox cmdBox;
        private final Button btnUp;
        private final Button btnDown;
        private final Button btnDel;
        private final List<GuiEventListener> children;
        private final List<NarratableEntry> narratables;
        final /* synthetic */ FayCoreRepeatingCmdSettingsScreen this$0;

        public CommandEntry(FayCoreRepeatingCmdSettingsScreen fayCoreRepeatingCmdSettingsScreen, int index, String currentText) {
            FayCoreRepeatingCmdSettingsScreen fayCoreRepeatingCmdSettingsScreen2 = fayCoreRepeatingCmdSettingsScreen;
            Objects.requireNonNull(fayCoreRepeatingCmdSettingsScreen2);
            this.this$0 = fayCoreRepeatingCmdSettingsScreen2;
            this.children = new ArrayList<GuiEventListener>();
            this.narratables = new ArrayList<NarratableEntry>();
            this.index = index;
            int startX = fayCoreRepeatingCmdSettingsScreen.width / 2 - 170;
            this.cmdBox = new EditBox(fayCoreRepeatingCmdSettingsScreen.font, startX, 0, 190, 18, (Component)Component.literal((String)"Command"));
            this.cmdBox.setMaxLength(Short.MAX_VALUE);
            this.cmdBox.setValue(currentText);
            this.cmdBox.setResponder(text -> this$0.commandList.set(index, (String)text));
            this.children.add((GuiEventListener)this.cmdBox);
            this.narratables.add((NarratableEntry)this.cmdBox);
            this.btnUp = Button.builder((Component)Component.literal((String)"\u00a76\u25b2"), btn -> {
                if (index > 0) {
                    Collections.swap(this$0.commandList, index, index - 1);
                    fayCoreRepeatingCmdSettingsScreen.updateScrollList();
                }
            }).bounds(startX + 195, 0, 20, 18).build();
            this.btnUp.active = index > 0;
            this.children.add((GuiEventListener)this.btnUp);
            this.narratables.add((NarratableEntry)this.btnUp);
            this.btnDown = Button.builder((Component)Component.literal((String)"\u00a76\u25bc"), btn -> {
                if (index < this$0.commandList.size() - 1) {
                    Collections.swap(this$0.commandList, index, index + 1);
                    fayCoreRepeatingCmdSettingsScreen.updateScrollList();
                }
            }).bounds(startX + 217, 0, 20, 18).build();
            this.btnDown.active = index < fayCoreRepeatingCmdSettingsScreen.commandList.size() - 1;
            this.children.add((GuiEventListener)this.btnDown);
            this.narratables.add((NarratableEntry)this.btnDown);
            this.btnDel = Button.builder((Component)Component.literal((String)"\u00a7cDelete"), btn -> {
                this$0.commandList.remove(index);
                fayCoreRepeatingCmdSettingsScreen.updateScrollList();
            }).bounds(startX + 240, 0, 36, 18).build();
            this.children.add((GuiEventListener)this.btnDel);
            this.narratables.add((NarratableEntry)this.btnDel);
        }

        public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean isHovered, float partialTicks) {
            int currentY = this.this$0.scrollList.getRowTop(this.index);
            this.cmdBox.setY(currentY);
            this.btnUp.setY(currentY);
            this.btnDown.setY(currentY);
            this.btnDel.setY(currentY);
            this.cmdBox.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
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

