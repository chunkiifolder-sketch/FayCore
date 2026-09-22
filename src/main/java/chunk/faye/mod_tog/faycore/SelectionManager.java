/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 */
package chunk.faye.mod_tog.faycore;

import net.minecraft.core.BlockPos;

public class SelectionManager {
    public static BlockPos posA = null;
    public static BlockPos posB = null;

    public static void clear() {
        posA = null;
        posB = null;
    }

    public static boolean isValid() {
        return posA != null && posB != null;
    }
}

