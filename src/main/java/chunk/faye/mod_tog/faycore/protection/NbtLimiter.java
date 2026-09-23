package chunk.faye.mod_tog.faycore.protection;

import chunk.faye.mod_tog.faycore.config.CrashProtectionConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public final class NbtLimiter {
   private NbtLimiter() {
   }

   public static boolean safe(Tag tag) {
      if (!CrashProtectionConfig.enableNbtLimit) {
         return true;
      } else {
         try {
            NbtLimiter.Counter counter = new NbtLimiter.Counter();
            scan(tag, counter, 0);
            return true;
         } catch (Throwable var2) {
            return false;
         }
      }
   }

   private static void scan(Tag tag, NbtLimiter.Counter counter, int depth) {
      if (tag != null) {
         if (depth > CrashProtectionConfig.maxNbtDepth) {
            throw new RuntimeException("NBT depth overflow");
         } else {
            counter.nodes++;
            if (counter.nodes > 50000) {
               throw new RuntimeException("NBT node overflow");
            } else {
               if (tag instanceof StringTag stringTag && stringTag.value().length() > CrashProtectionConfig.maxNbtStringLength) {
                  throw new RuntimeException("NBT string overflow");
               }

               if (tag instanceof CompoundTag compound) {
                  for (String key : compound.keySet()) {
                     scan(compound.get(key), counter, depth + 1);
                  }
               } else if (tag instanceof ListTag list) {
                  if (list.size() > 4096) {
                     throw new RuntimeException("NBT list overflow");
                  }

                  for (Tag child : list) {
                     scan(child, counter, depth + 1);
                  }
               }
            }
         }
      }
   }

   private static class Counter {
      int nodes = 0;
   }
}
