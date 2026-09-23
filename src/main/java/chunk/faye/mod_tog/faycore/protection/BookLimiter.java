package chunk.faye.mod_tog.faycore.protection;

import chunk.faye.mod_tog.faycore.config.CrashProtectionConfig;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;

public final class BookLimiter {
   private BookLimiter() {
   }

   public static boolean check(ItemStack stack) {
      if (!CrashProtectionConfig.enableBookLimit) {
         return true;
      } else {
         try {
            WrittenBookContent book = (WrittenBookContent)stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (book == null) {
               return true;
            } else {
               List<?> pages = book.pages();
               if (pages.size() > CrashProtectionConfig.maxBookPages) {
                  return false;
               } else {
                  for (Object page : pages) {
                     String text = page.toString();
                     if (text.length() > CrashProtectionConfig.maxBookPageLength) {
                        return false;
                     }
                  }

                  return true;
               }
            }
         } catch (Throwable var6) {
            return false;
         }
      }
   }
}
