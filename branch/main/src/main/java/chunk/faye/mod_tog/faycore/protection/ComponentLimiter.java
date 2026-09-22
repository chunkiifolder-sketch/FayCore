/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 */
package chunk.faye.mod_tog.faycore.protection;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class ComponentLimiter {
    private ComponentLimiter() {
    }

    public static boolean safe(ItemStack stack) {
        try {
            if (stack == null || stack.isEmpty()) {
                return true;
            }
            String text = stack.getHoverName().getString();
            if (text.length() > 256) {
                return false;
            }
            String all = stack.getComponents().toString();
            return all.length() <= 4096;
        }
        catch (Throwable e) {
            return false;
        }
    }

    public static boolean safe(Component component) {
        try {
            if (component == null) {
                return true;
            }
            String text = component.getString();
            return text.length() <= 2048;
        }
        catch (Throwable e) {
            return false;
        }
    }
}

