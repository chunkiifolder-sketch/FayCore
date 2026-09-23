package chunk.faye.mod_tog.faycore.client.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class FayCoreRepeatingCmdSettingsScreen extends Screen {
   private static final File CONFIG_FILE = new File(Minecraft.getInstance().gameDirectory, "config/faycore_commands.json");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final List<String> commandList = new ArrayList<>();
   private FayCoreRepeatingCmdSettingsScreen.CommandScrollList scrollList;
   private Button buttonAdd;
   private Button buttonConfirm;

   public FayCoreRepeatingCmdSettingsScreen(Component title) {
      super(title);
      this.loadCommandsFromLocal();
   }

   protected void init() {
      super.init();
      this.scrollList = new FayCoreRepeatingCmdSettingsScreen.CommandScrollList(this.minecraft, this.width, this.height - 100, 40, 24);
      this.addRenderableWidget(this.scrollList);
      this.buttonAdd = Button.builder(Component.literal("§a+ Add command"), btn -> {
         this.commandList.add("");
         this.updateScrollList();
      }).bounds(this.width / 2 - 140, this.height - 45, 110, 20).build();
      this.addRenderableWidget(this.buttonAdd);
      this.buttonConfirm = Button.builder(Component.literal("§a✔ Save"), btn -> {
         this.saveCommandsToLocal();
         Minecraft mc = Minecraft.getInstance();
         int Max_RCy = Math.max(FaycoreSettingsScreen.posA.getY(), FaycoreSettingsScreen.posB.getY());
         if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
            this.minecraft.player.sendSystemMessage(Component.literal("§a[FayCore] §7Repeat commands saved."));
         }

         new Thread(() -> {
            try {
               Thread.sleep(50L);
               FaycoreSettingsScreen.autoFillCommandsViaPacket();
            } catch (Exception var1x) {
               var1x.printStackTrace();
            }
         }).start();
         if (this.minecraft != null) {
            this.minecraft.setScreen((Screen)null);
         }
      }).bounds(this.width / 2 - 20, this.height - 45, 120, 20).build();
      this.addRenderableWidget(this.buttonConfirm);
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

         try (Writer writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this.commandList, writer);
         }
      } catch (IOException var6) {
         var6.printStackTrace();
      }
   }

   private void loadCommandsFromLocal() {
      if (CONFIG_FILE.exists()) {
         try (Reader reader = new FileReader(CONFIG_FILE)) {
            List<String> loaded = (List<String>)GSON.fromJson(reader, (new TypeToken<List<String>>() {
               {
                  Objects.requireNonNull(FayCoreRepeatingCmdSettingsScreen.this);
               }
            }).getType());
            if (loaded != null) {
               this.commandList.clear();
               this.commandList.addAll(loaded);
            }
         } catch (Exception var6) {
            var6.printStackTrace();
         }
      }
   }

   private class CommandEntry extends Entry<FayCoreRepeatingCmdSettingsScreen.CommandEntry> {
      private final int index;
      private final EditBox cmdBox;
      private final Button btnUp;
      private final Button btnDown;
      private final Button btnDel;
      private final List<GuiEventListener> children;
      private final List<NarratableEntry> narratables;

      public CommandEntry(int index, String currentText) {
         Objects.requireNonNull(FayCoreRepeatingCmdSettingsScreen.this);
         super();
         this.children = new ArrayList<>();
         this.narratables = new ArrayList<>();
         this.index = index;
         int startX = FayCoreRepeatingCmdSettingsScreen.this.width / 2 - 170;
         this.cmdBox = new EditBox(FayCoreRepeatingCmdSettingsScreen.this.font, startX, 0, 190, 18, Component.literal("Command"));
         this.cmdBox.setMaxLength(32767);
         this.cmdBox.setValue(currentText);
         this.cmdBox.setResponder(text -> FayCoreRepeatingCmdSettingsScreen.this.commandList.set(index, text));
         this.children.add(this.cmdBox);
         this.narratables.add(this.cmdBox);
         this.btnUp = Button.builder(Component.literal("§6▲"), btn -> {
            if (index > 0) {
               Collections.swap(FayCoreRepeatingCmdSettingsScreen.this.commandList, index, index - 1);
               FayCoreRepeatingCmdSettingsScreen.this.updateScrollList();
            }
         }).bounds(startX + 195, 0, 20, 18).build();
         this.btnUp.active = index > 0;
         this.children.add(this.btnUp);
         this.narratables.add(this.btnUp);
         this.btnDown = Button.builder(Component.literal("§6▼"), btn -> {
            if (index < FayCoreRepeatingCmdSettingsScreen.this.commandList.size() - 1) {
               Collections.swap(FayCoreRepeatingCmdSettingsScreen.this.commandList, index, index + 1);
               FayCoreRepeatingCmdSettingsScreen.this.updateScrollList();
            }
         }).bounds(startX + 217, 0, 20, 18).build();
         this.btnDown.active = index < FayCoreRepeatingCmdSettingsScreen.this.commandList.size() - 1;
         this.children.add(this.btnDown);
         this.narratables.add(this.btnDown);
         this.btnDel = Button.builder(Component.literal("§cDelete"), btn -> {
            FayCoreRepeatingCmdSettingsScreen.this.commandList.remove(index);
            FayCoreRepeatingCmdSettingsScreen.this.updateScrollList();
         }).bounds(startX + 240, 0, 36, 18).build();
         this.children.add(this.btnDel);
         this.narratables.add(this.btnDel);
      }

      public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean isHovered, float partialTicks) {
         int currentY = FayCoreRepeatingCmdSettingsScreen.this.scrollList.getRowTop(this.index);
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

   private class CommandScrollList extends ContainerObjectSelectionList<FayCoreRepeatingCmdSettingsScreen.CommandEntry> {
      public CommandScrollList(Minecraft minecraft, int width, int height, int top, int itemHeight) {
         Objects.requireNonNull(FayCoreRepeatingCmdSettingsScreen.this);
         super(minecraft, width, height, top, itemHeight);
      }

      public int getRowWidth() {
         return 360;
      }

      public void refreshEntries() {
         this.clearEntries();

         for (int i = 0; i < FayCoreRepeatingCmdSettingsScreen.this.commandList.size(); i++) {
            this.addEntry(FayCoreRepeatingCmdSettingsScreen.this.new CommandEntry(i, FayCoreRepeatingCmdSettingsScreen.this.commandList.get(i)));
         }
      }
   }
}
