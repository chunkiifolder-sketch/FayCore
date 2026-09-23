package chunk.faye.mod_tog.faycore.protection;

import chunk.faye.mod_tog.faycore.config.CrashProtectionConfig;
import net.minecraft.network.chat.Component;

public final class SafeComponent {
   private SafeComponent() {
   }

   public static String getString(Component component) {
      if (component == null) {
         return "";
      } else if (!CrashProtectionConfig.enableComponentProtection) {
         try {
            return component.getString();
         } catch (Throwable var2) {
            return "";
         }
      } else {
         try {
            String text = component.getString();
            if (text == null) {
               return "";
            } else if (text.length() > CrashProtectionConfig.maxResolvedComponentLength) {
               if (CrashProtectionConfig.debugLog) {
                  System.out.println("[FayCore] Blocked oversized Component: " + text.length());
               }

               return text.substring(0, CrashProtectionConfig.maxResolvedComponentLength);
            } else {
               return text;
            }
         } catch (OutOfMemoryError var3) {
            if (CrashProtectionConfig.debugLog) {
               System.out.println("[FayCore] Blocked recursive Component");
            }

            return "";
         } catch (Throwable var4) {
            if (CrashProtectionConfig.debugLog) {
               var4.printStackTrace();
            }

            return "";
         }
      }
   }

   public static boolean isSafe(Component component) {
      String text = getString(component);
      return text.length() <= CrashProtectionConfig.maxResolvedComponentLength;
   }

   public static Component limit(Component component) {
      if (component == null) {
         return Component.empty();
      } else {
         String text = getString(component);
         return (Component)(text.length() <= CrashProtectionConfig.maxResolvedComponentLength
            ? component
            : Component.literal(text.substring(0, CrashProtectionConfig.maxResolvedComponentLength)));
      }
   }
}
