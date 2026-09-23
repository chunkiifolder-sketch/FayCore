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
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class FayCoreGhostCmdSettingsScreen extends Screen {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private int currentPage = 1;
   private final int maxPages = 20;
   private final List<String> ghostCommandList = new ArrayList<>();
   private FayCoreGhostCmdSettingsScreen.GhostCommandScrollList scrollList;
   private Button buttonAdd;
   private Button buttonConfirm;
   private Button buttonPrevPage;
   private Button buttonNextPage;
   private Button modeToggleButton;
   public static boolean IsLoop = false;

   public FayCoreGhostCmdSettingsScreen() {
      super(Component.literal("FayCore FastRun Cmd List"));
      this.loadCommandsFromLocal();
   }

   private File getPageFile(int page) {
      return new File(Minecraft.getInstance().gameDirectory, "config/faycore_ghost_page_" + page + ".json");
   }

   protected void init() {
      super.init();
      String initialConfirmText = FayCoreFastRun.IsLoop ? "Stop" : "\ud83d\ude80 Run";
      this.buttonConfirm = Button.builder(Component.literal(initialConfirmText), btn -> {
         if (this.minecraft != null) {
            this.minecraft.setScreen((Screen)null);
         }

         this.saveCommandsToLocal();
         if (btn.getMessage().getString().equals("Stop")) {
            FayCoreFastRun.IsLoop = false;
            if (this.minecraft != null && this.minecraft.player != null) {
               this.modeToggleButton.setMessage(Component.literal("§dLoop"));
               IsLoop = true;
               this.minecraft.player.playSound(SoundEvents.IRON_DOOR_CLOSE, 1.0F, 1.5F);
               this.minecraft.player.sendSystemMessage(Component.literal("§9[FayCore] §aStopped FastRun."));
            }

            btn.setMessage(Component.literal("\ud83d\ude80 Run"));
         } else {
            if (FayCoreFastRun.IsLoop) {
               if (this.minecraft != null && this.minecraft.player != null) {
                  this.minecraft.player.playSound(SoundEvents.ENDER_DRAGON_SHOOT, 1.0F, 1.2F);
                  this.minecraft.player.sendSystemMessage(Component.literal("§9[FayCore] §aStarted a loop FastRun"));
               }

               FayCoreFastRun.fireGhostPayloadQueue(new ArrayList<>(this.ghostCommandList));
               btn.setMessage(Component.literal("Stop"));
            } else {
               if (this.minecraft != null && this.minecraft.player != null) {
                  this.minecraft.player.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.2F);
                  this.minecraft.player.sendSystemMessage(Component.literal("§9[FayCore] §aStarted a FastRun."));
               }

               FayCoreFastRun.fireGhostPayloadQueue(new ArrayList<>(this.ghostCommandList));
               if (this.minecraft != null) {
                  this.minecraft.setScreen((Screen)null);
               }
            }
         }
      }).bounds(this.width / 2 - 45, this.height - 45, 90, 20).build();
      this.addRenderableWidget(this.buttonConfirm);
      String currentModeText = FayCoreFastRun.IsLoop ? "§dLoop" : "§6Once";
      this.modeToggleButton = Button.builder(Component.literal(currentModeText), button -> {
         FayCoreFastRun.IsLoop = !FayCoreFastRun.IsLoop;
         if (FayCoreFastRun.IsLoop) {
            button.setMessage(Component.literal("§dLoop"));
         } else {
            button.setMessage(Component.literal("§6Once"));
         }
      }).bounds(this.width / 2 - 360, this.height - 45, 90, 20).build();
      this.addRenderableWidget(this.modeToggleButton);
      this.scrollList = new FayCoreGhostCmdSettingsScreen.GhostCommandScrollList(this.minecraft, this.width, this.height - 100, 40, 50);
      this.addRenderableWidget(this.scrollList);
      this.buttonAdd = Button.builder(Component.literal("＋ Add Cmd"), btn -> {
         this.ghostCommandList.add("");
         this.updateScrollList();
      }).bounds(this.width / 2 - 195, this.height - 45, 80, 20).build();
      this.addRenderableWidget(this.buttonAdd);
      this.buttonPrevPage = Button.builder(Component.literal("◀ Previous"), btn -> {
         if (this.currentPage > 1) {
            this.saveCommandsToLocal();
            this.currentPage--;
            this.loadCommandsFromLocal();
            this.updateScrollList();
            this.updateButtonStates();
         }
      }).bounds(this.width / 2 - 110, this.height - 45, 60, 20).build();
      this.addRenderableWidget(this.buttonPrevPage);
      this.buttonNextPage = Button.builder(Component.literal("Next ▶"), btn -> {
         if (this.currentPage < 20) {
            this.saveCommandsToLocal();
            this.currentPage++;
            this.loadCommandsFromLocal();
            this.updateScrollList();
            this.updateButtonStates();
         }
      }).bounds(this.width / 2 + 50, this.height - 45, 60, 20).build();
      this.addRenderableWidget(this.buttonNextPage);
      this.updateScrollList();
      this.updateButtonStates();
   }

   public void updateButtonStates() {
      if (this.buttonPrevPage != null) {
         this.buttonPrevPage.active = this.currentPage > 1;
      }

      if (this.buttonNextPage != null) {
         this.buttonNextPage.active = this.currentPage < 20;
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
      String pageInfoStr = String.format("§d§lFayCore FastRun §e(Page: %d / %d)", this.currentPage, 20);
      guiGraphics.text(this.font, Component.literal(pageInfoStr), this.width / 2 - 120, 15, 16777215, true);
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

         try (Writer writer = new FileWriter(pageFile)) {
            GSON.toJson(this.ghostCommandList, writer);
         }
      } catch (IOException var7) {
      }
   }

   private void loadCommandsFromLocal() {
      File pageFile = this.getPageFile(this.currentPage);
      this.ghostCommandList.clear();
      if (pageFile.exists()) {
         try (Reader reader = new FileReader(pageFile)) {
            List loaded = (List)GSON.fromJson(reader, (new TypeToken<List>() {
               {
                  Objects.requireNonNull(FayCoreGhostCmdSettingsScreen.this);
               }
            }).getType());
            if (loaded != null) {
               this.ghostCommandList.addAll(loaded);
            }
         } catch (Exception var7) {
         }
      }
   }

   private class GhostCommandEntry extends Entry<FayCoreGhostCmdSettingsScreen.GhostCommandEntry> {
      private final int index;
      private final MultiLineEditBox multiLineBox;
      private final Button btnUp;
      private final Button btnDown;
      private final Button btnDel;
      private final List<GuiEventListener> children;
      private final List<NarratableEntry> narratables;

      public GhostCommandEntry(int index, String currentText) {
         Objects.requireNonNull(FayCoreGhostCmdSettingsScreen.this);
         super();
         this.children = new ArrayList<>();
         this.narratables = new ArrayList<>();
         this.index = index;
         int startX = FayCoreGhostCmdSettingsScreen.this.width / 2 - 170;
         this.multiLineBox = MultiLineEditBox.builder()
            .setX(startX)
            .setY(0)
            .setPlaceholder(Component.literal("Type a cmd..."))
            .build(FayCoreGhostCmdSettingsScreen.this.font, 190, 30, Component.literal("FastRun"));
         this.multiLineBox.setCharacterLimit(32767);
         this.multiLineBox.setValue(currentText);
         this.multiLineBox.setValueListener(text -> FayCoreGhostCmdSettingsScreen.this.ghostCommandList.set(index, text));
         this.children.add(this.multiLineBox);
         this.narratables.add(this.multiLineBox);
         this.btnUp = Button.builder(Component.literal("▲"), btn -> {
            if (index > 0) {
               FayCoreGhostCmdSettingsScreen.this.saveCommandsToLocal();
               Collections.swap(FayCoreGhostCmdSettingsScreen.this.ghostCommandList, index, index - 1);
               FayCoreGhostCmdSettingsScreen.this.updateScrollList();
            }
         }).bounds(startX + 195, 0, 20, 14).build();
         this.btnUp.active = index > 0;
         this.children.add(this.btnUp);
         this.narratables.add(this.btnUp);
         this.btnDown = Button.builder(Component.literal("▼"), btn -> {
            if (index < FayCoreGhostCmdSettingsScreen.this.ghostCommandList.size() - 1) {
               FayCoreGhostCmdSettingsScreen.this.saveCommandsToLocal();
               Collections.swap(FayCoreGhostCmdSettingsScreen.this.ghostCommandList, index, index + 1);
               FayCoreGhostCmdSettingsScreen.this.updateScrollList();
            }
         }).bounds(startX + 217, 0, 20, 14).build();
         this.btnDown.active = index < FayCoreGhostCmdSettingsScreen.this.ghostCommandList.size() - 1;
         this.children.add(this.btnDown);
         this.narratables.add(this.btnDown);
         this.btnDel = Button.builder(Component.literal("刪"), btn -> {
            FayCoreGhostCmdSettingsScreen.this.ghostCommandList.remove(index);
            FayCoreGhostCmdSettingsScreen.this.updateScrollList();
         }).bounds(startX + 240, 0, 20, 14).build();
         this.children.add(this.btnDel);
         this.narratables.add(this.btnDel);
      }

      public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean isHovered, float partialTicks) {
         int currentY = FayCoreGhostCmdSettingsScreen.this.scrollList.getRowTop(this.index);
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

   private class GhostCommandScrollList extends ContainerObjectSelectionList<FayCoreGhostCmdSettingsScreen.GhostCommandEntry> {
      public GhostCommandScrollList(Minecraft minecraft, int width, int height, int top, int itemHeight) {
         Objects.requireNonNull(FayCoreGhostCmdSettingsScreen.this);
         super(minecraft, width, height, top, itemHeight);
      }

      public int getRowWidth() {
         return 360;
      }

      public void refreshEntries() {
         this.clearEntries();

         for (int i = 0; i < FayCoreGhostCmdSettingsScreen.this.ghostCommandList.size(); i++) {
            this.addEntry(FayCoreGhostCmdSettingsScreen.this.new GhostCommandEntry(i, FayCoreGhostCmdSettingsScreen.this.ghostCommandList.get(i)));
         }
      }
   }
}
