package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.config.CrashProtectionConfig;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Connection.class})
public class PacketProtectionMixin {
   private static final int MAX_PACKET_STRING_LENGTH = 1000000;

   @Inject(
      method = {"genericsFtw"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void faycore$packetGuard(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
      if (CrashProtectionConfig.enableNbtLimit) {
         try {
            String data = packet.toString();
            if (data.length() > 1000000) {
               if (CrashProtectionConfig.debugLog) {
                  System.out.println("[FayCore] Blocked huge packet: " + packet.getClass());
               }

               ci.cancel();
            }
         } catch (Throwable var4) {
            ci.cancel();
         }
      }
   }
}
