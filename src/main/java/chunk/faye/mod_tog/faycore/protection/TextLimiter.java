package chunk.faye.mod_tog.faycore.protection;

import net.minecraft.network.chat.Component;

public final class TextLimiter {
   private TextLimiter() {
   }

   public static boolean check(Component component, int maxLength) {
      try {
         String text = SafeComponent.getString(component);
         return text.length() <= maxLength;
      } catch (Throwable var3) {
         return false;
      }
   }

   public static Component sanitize(Component component, int maxLength) {
      try {
         String text = SafeComponent.getString(component);
         return (Component)(text.length() <= maxLength ? component : Component.literal(text.substring(0, maxLength)));
      } catch (Throwable var3) {
         return Component.empty();
      }
   }
}
