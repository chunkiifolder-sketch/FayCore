/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.Vec3
 */
package net.mcreator.faycore;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class EntityRaycast {
    public static EntityHitResult raycastEntity(Minecraft mc, double range) {
        if (mc.player == null || mc.level == null) {
            return null;
        }
        Vec3 eye = mc.player.getEyePosition();
        Vec3 look = mc.player.getLookAngle();
        Vec3 end = eye.add(look.x * range, look.y * range, look.z * range);
        AABB box = mc.player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        Entity target = null;
        double closest = range * range;
        for (Entity entity : mc.level.getEntities((Entity)mc.player, box, e -> e.isPickable())) {
            double distance;
            AABB hitbox = entity.getBoundingBox().inflate(0.2);
            Optional result = hitbox.clip(eye, end);
            if (!result.isPresent() || !((distance = eye.distanceToSqr((Vec3)result.get())) < closest)) continue;
            closest = distance;
            target = entity;
        }
        if (target != null) {
            return new EntityHitResult(target);
        }
        return null;
    }
}

