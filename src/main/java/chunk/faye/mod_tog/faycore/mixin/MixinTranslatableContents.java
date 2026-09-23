package chunk.faye.mod_tog.faycore.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({TranslatableContents.class})
public class MixinTranslatableContents {
   private static long lastNotificationTime = 0L;

   @Inject(
      method = {"<init>(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V"},
      at = {@At("HEAD")}
   )
   private static void onInitHead(String key, String fallback, Object[] args, CallbackInfo ci) {
      if (args != null && args.length != 0) {
         boolean isMalicious = false;

         for (Object arg : args) {
            if (arg != null) {
               String argStr = arg.toString();
               if (argStr.contains("%1$s") || argStr.contains("translate") && argStr.contains("with")) {
                  isMalicious = true;
                  break;
               }
            }
         }

         if (isMalicious) {
            for (int i = 0; i < args.length; i++) {
               args[i] = "";
            }

            try {
               long currentTime = System.currentTimeMillis();
               if (currentTime - lastNotificationTime > 1000L) {
                  lastNotificationTime = currentTime;
                  Minecraft mc = Minecraft.getInstance();
                  if (mc != null) {
                     mc.execute(() -> {
                        if (mc.player != null) {
                           mc.player.sendSystemMessage(Component.literal("§9[FayCore] §7Blocked a Crash!"));
                        }
                     });
                  }
               }
            } catch (Exception var10) {
            }
         }
      }
   }
}
