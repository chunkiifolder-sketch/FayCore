/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package chunk.faye.mod_tog.faycore;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class SkillState {
    public static int MAX = 7;
    private static int selected = 0;
    private static ItemStack[] SKILL_ITEMS;

    public static void init() {
        if (SKILL_ITEMS != null) {
            return;
        }
        SKILL_ITEMS = new ItemStack[]{new ItemStack((ItemLike)Items.FEATHER), new ItemStack((ItemLike)Items.BEACON), new ItemStack((ItemLike)Items.CROSSBOW), new ItemStack((ItemLike)Items.PISTON), new ItemStack((ItemLike)Items.ENDER_PEARL), ItemStack.EMPTY, ItemStack.EMPTY};
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
        }
        if (slot >= 0 && slot < MAX) {
            return SKILL_ITEMS[slot];
        }
        return ItemStack.EMPTY;
    }

    public static String getSkillName(int slot) {
        return switch (slot) {
            case 0 -> "\u00a77\u00a7l[\u591a\u6bb5\u8df3]";
            case 1 -> "\u00a77\u00a7l[\u96f7\u5c04]";
            case 2 -> "\u00a77\u00a7l[\u624b\u69cd]";
            case 3 -> "\u00a74\u00a7l[\u63a7\u5236]";
            case 4 -> "\u00a74\u00a7l[\u77ac\u79fb]";
            case 5 -> "\u00a74\u00a7l[\u7a7a\u69fd]";
            case 6 -> "\u00a74\u00a7l[\u7a7a\u69fd]";
            default -> "\u672a\u77e5\u6280\u80fd";
        };
    }
}

