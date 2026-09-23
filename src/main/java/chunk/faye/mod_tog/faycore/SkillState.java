package chunk.faye.mod_tog.faycore;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SkillState {
   public static int MAX = 7;
   private static int selected = 0;
   private static ItemStack[] SKILL_ITEMS;

   public static void init() {
      if (SKILL_ITEMS == null) {
         SKILL_ITEMS = new ItemStack[]{
            new ItemStack(Items.FEATHER),
            new ItemStack(Items.BEACON),
            new ItemStack(Items.CROSSBOW),
            new ItemStack(Items.PISTON),
            new ItemStack(Items.ENDER_PEARL),
            ItemStack.EMPTY,
            ItemStack.EMPTY
         };
      }
   }

   public static void setSelected(int value) {
      if (value >= 0 && value < MAX) {
         selected = value;
      }
   }

   public static int getSelected() {
      return selected;
   }

   public static void next() {
      selected = (selected + 1) % MAX;
   }

   public static void previous() {
      selected = (selected - 1 + MAX) % MAX;
   }

   public static void reset() {
      selected = 0;
   }

   public static ItemStack getItemInSlot(int slot) {
      if (SKILL_ITEMS == null) {
         return ItemStack.EMPTY;
      } else {
         return slot >= 0 && slot < MAX ? SKILL_ITEMS[slot] : ItemStack.EMPTY;
      }
   }

   public static String getSkillName(int slot) {
      return switch (slot) {
         case 0 -> "§7§l[多段跳]";
         case 1 -> "§7§l[雷射]";
         case 2 -> "§7§l[手槍]";
         case 3 -> "§4§l[控制]";
         case 4 -> "§4§l[瞬移]";
         case 5 -> "§4§l[空槽]";
         case 6 -> "§4§l[空槽]";
         default -> "未知技能";
      };
   }
}
