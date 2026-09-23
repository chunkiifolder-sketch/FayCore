package chunk.faye.mod_tog.faycore.protection;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class ComponentLimiter {
   private ComponentLimiter() {
   }

   public static boolean safe(ItemStack stack) {
      try {
         if (stack != null && !stack.isEmpty()) {
            String text = stack.getHoverName().getString();
            if (text.length() > 256) {
               return false;
            } else {
               String all = stack.getComponents().toString();
               return all.length() <= 4096;
            }
         } else {
            return true;
         }
      } catch (Throwable var3) {
         return false;
      }
   }

   public static boolean safe(Component component) {
      try {
         if (component == null) {
            return true;
         } else {
            String text = component.getString();
            return text.length() <= 2048;
         }
      } catch (Throwable var2) {
         return false;
      }
   }
}
