package chunk.faye.mod_tog.faycore.protection;

import chunk.faye.mod_tog.faycore.config.CrashProtectionConfig;

public final class PacketLimiter {
   private static long lastPacketTime = 0L;
   private static int packetCount = 0;

   private PacketLimiter() {
   }

   public static boolean allowPacket() {
      if (!CrashProtectionConfig.enablePacketLimit) {
         return true;
      } else {
         long now = System.currentTimeMillis();
         if (now - lastPacketTime > 1000L) {
            lastPacketTime = now;
            packetCount = 0;
         }

         packetCount++;
         return packetCount <= CrashProtectionConfig.maxPacketsPerSecond;
      }
   }

   public static boolean allowText(String text) {
      return text == null ? true : text.length() <= CrashProtectionConfig.maxChatLength;
   }
}
