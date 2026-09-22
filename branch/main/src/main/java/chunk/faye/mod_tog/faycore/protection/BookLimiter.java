/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.WrittenBookContent
 */
package chunk.faye.mod_tog.faycore.protection;

import java.util.List;
import chunk.faye.mod_tog.faycore.config.CrashProtectionConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;

public final class BookLimiter {
    private BookLimiter() {
    }

    public static boolean check(ItemStack stack) {
        if (!CrashProtectionConfig.enableBookLimit) {
            return true;
        }
        try {
            WrittenBookContent book = (WrittenBookContent)stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (book == null) {
                return true;
            }
            List pages = book.pages();
            if (pages.size() > CrashProtectionConfig.maxBookPages) {
                return false;
            }
            for (Object page : pages) {
                String text = page.toString();
                if (text.length() <= CrashProtectionConfig.maxBookPageLength) continue;
                return false;
            }
            return true;
        }
        catch (Throwable e) {
            return false;
        }
    }
}

