/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.contents.TranslatableContents
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package chunk.faye.mod_tog.faycore.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={TranslatableContents.class})
public class MixinTranslatableContents {
    private static long lastNotificationTime = 0L;

    @Inject(method={"<init>(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V"}, at={@At(value="HEAD")})
    private static void onInitHead(String key, String fallback, Object[] args, CallbackInfo ci) {
        if (args == null || args.length == 0) {
            return;
        }
        boolean isMalicious = false;
        for (Object arg : args) {
            String argStr;
            if (arg == null || !(argStr = arg.toString()).contains("%1$s") && (!argStr.contains("translate") || !argStr.contains("with"))) continue;
            isMalicious = true;
            break;
        }
        if (isMalicious) {
            for (int i = 0; i < args.length; ++i) {
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
                                mc.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a77Blocked a Crash!"));
                            }
                        });
                    }
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }
}

