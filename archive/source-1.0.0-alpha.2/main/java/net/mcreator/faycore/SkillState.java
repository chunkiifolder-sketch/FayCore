package net.mcreator.faycore;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SkillState {


    public static int MAX = 5; // 此處設定多少格槽位
    public static void setSelected(int value) {
        if (value >= 0 && value < MAX) {
            selected = value;
        }
    }

    private static int selected = 0;

    // 空槽 直接設定 `AIR`
    private static final ItemStack[] SKILL_ITEMS = new ItemStack[]{
            // 模板: `new ItemStack (Items.<這裡加入你想要的物品>)`
            new ItemStack(Items.FEATHER),
            new ItemStack(Items.AIR),
            new ItemStack(Items.AIR),
            new ItemStack(Items.AIR),
            new ItemStack(Items.AIR)
    };

    public static int getSelected() { return selected; }
    public static void next() { selected = (selected + 1) % MAX; }
    public static void previous() { selected = (selected - 1 + MAX) % MAX; }
    public static void reset() { selected = 0; }

    public static ItemStack getItemInSlot(int slot) {
        if (slot >= 0 && slot < MAX) {
            return SKILL_ITEMS[slot];
        }
        return ItemStack.EMPTY;
    }
}
