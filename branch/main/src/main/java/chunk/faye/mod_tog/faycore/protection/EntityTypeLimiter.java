/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EntityType
 */
package chunk.faye.mod_tog.faycore.protection;

import net.minecraft.world.entity.EntityType;

public final class EntityTypeLimiter {
    private EntityTypeLimiter() {
    }

    public static boolean allow(EntityType<?> type) {
        if (type == EntityType.ARMOR_STAND) {
            return true;
        }
        if (type == EntityType.ITEM) {
            return true;
        }
        if (type == EntityType.EXPERIENCE_ORB) {
            return true;
        }
        return true;
    }
}

