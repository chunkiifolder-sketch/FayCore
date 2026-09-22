/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 */
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
        }
        try {
            Counter counter = new Counter();
            NbtLimiter.scan(tag, counter, 0);
            return true;
        }
        catch (Throwable e) {
            return false;
        }
    }

    private static void scan(Tag tag, Counter counter, int depth) {
        block8: {
            block7: {
                StringTag stringTag;
                if (tag == null) {
                    return;
                }
                if (depth > CrashProtectionConfig.maxNbtDepth) {
                    throw new RuntimeException("NBT depth overflow");
                }
                ++counter.nodes;
                if (counter.nodes > 50000) {
                    throw new RuntimeException("NBT node overflow");
                }
                if (tag instanceof StringTag && (stringTag = (StringTag)tag).value().length() > CrashProtectionConfig.maxNbtStringLength) {
                    throw new RuntimeException("NBT string overflow");
                }
                if (!(tag instanceof CompoundTag)) break block7;
                CompoundTag compound = (CompoundTag)tag;
                for (String key : compound.keySet()) {
                    NbtLimiter.scan(compound.get(key), counter, depth + 1);
                }
                break block8;
            }
            if (!(tag instanceof ListTag)) break block8;
            ListTag list = (ListTag)tag;
            if (list.size() > 4096) {
                throw new RuntimeException("NBT list overflow");
            }
            for (Tag child : list) {
                NbtLimiter.scan(child, counter, depth + 1);
            }
        }
    }

    private static class Counter {
        int nodes = 0;

        private Counter() {
        }
    }
}

