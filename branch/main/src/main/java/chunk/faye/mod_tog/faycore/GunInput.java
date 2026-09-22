/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package chunk.faye.mod_tog.faycore;

import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import chunk.faye.mod_tog.faycore.EntityRaycast;
import chunk.faye.mod_tog.faycore.SkillInteractScreen;
import chunk.faye.mod_tog.faycore.client.gui.FayCoreMacroEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class GunInput {
    private static boolean lastRight = false;
    private static int INTERPOLATION_DURATION = 4;
    private static int delay = 0;
    private static boolean waiting = false;

    public static void waitAndRun() {
        waiting = true;
        delay = 10;
    }

    public static Vec3 getTargetLocation(Minecraft mc, double maxDistance) {
        if (mc.player == null || mc.level == null) {
            return Vec3.ZERO;
        }
        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 lookVec = mc.player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.x * maxDistance, lookVec.y * maxDistance, lookVec.z * maxDistance);
        BlockHitResult blockHit = mc.level.clip(new ClipContext(eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)mc.player));
        Vec3 finalHitPos = blockHit.getType() != HitResult.Type.MISS ? blockHit.getLocation() : endPos;
        double currentNearestDist = eyePos.distanceTo(finalHitPos);
        EntityHitResult entityHit = EntityRaycast.raycastEntity(mc, (int)currentNearestDist);
        if (entityHit != null && entityHit.getType() == HitResult.Type.ENTITY) {
            return entityHit.getLocation();
        }
        return finalHitPos;
    }

    public static BlockPos getTargetBlockPos(Minecraft mc, double maxDistance) {
        if (mc.player == null || mc.level == null) {
            return BlockPos.ZERO;
        }
        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 lookVec = mc.player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.x * maxDistance, lookVec.y * maxDistance, lookVec.z * maxDistance);
        BlockHitResult blockHit = mc.level.clip(new ClipContext(eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)mc.player));
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            return blockHit.getBlockPos();
        }
        Vec3 hitVec = GunInput.getTargetLocation(mc, maxDistance);
        return BlockPos.containing((double)hitVec.x, (double)hitVec.y, (double)hitVec.z);
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) {
                return;
            }
            boolean right = mc.options.keyUse.isDown();
            if (right && !lastRight && GunInput.isGun(mc)) {
                GunInput.fireGun(mc);
            }
            lastRight = right;
            if (waiting && --delay <= 0) {
                waiting = false;
                FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "kill @e[type=minecraft:block_display,tag=faycore_gun.bullet]");
            }
        });
    }

    private static boolean isGun(Minecraft mc) {
        ItemStack offhand = mc.player.getOffhandItem();
        CustomData data = (CustomData)offhand.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return false;
        }
        return data.copyTag().contains("faycore_fakegun");
    }

    private static void fireGun(Minecraft mc) {
        Vec3 target;
        Vec3 eye = mc.player.getEyePosition();
        Vec3 look = mc.player.getLookAngle();
        Vec3 end = eye.add(look.x * 100.0, look.y * 100.0, look.z * 100.0);
        BlockHitResult hit = mc.level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)mc.player));
        Vec3 targetPos = hit.getType() == HitResult.Type.BLOCK ? hit.getLocation() : end;
        Vec3 playerPos = mc.player.position();
        double dx = targetPos.x - playerPos.x;
        double dy = targetPos.y - playerPos.y;
        double dz = targetPos.z - playerPos.z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        EntityHitResult entityHit = EntityRaycast.raycastEntity(mc, 100.0);
        if (SkillInteractScreen.GunMode == 0) {
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, String.format("execute as %%player%% at @s positioned ^ ^-0.25 ^0.5 run summon minecraft:block_display ~ ~1.75 ~ {interpolation_duration:" + INTERPOLATION_DURATION + ",start_interpolation:0,block_state:{Name:\"minecraft:yellow_stained_glass\"},transformation:{right_rotation:[0f,0f,0f,1f],left_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[0.05f,0.05f,%ff]},Tags:[\"faycore_gun.bullet\"]}", distance));
        } else if (SkillInteractScreen.GunMode == 1) {
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, String.format("execute as %%player%% at @s positioned ^ ^-0.25 ^0.5 run summon minecraft:block_display ~ ~1.75 ~ {interpolation_duration:" + INTERPOLATION_DURATION + ",start_interpolation:0,block_state:{Name:\"minecraft:red_stained_glass\"},transformation:{right_rotation:[0f,0f,0f,1f],left_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[0.05f,0.05f,%ff]},Tags:[\"faycore_gun.bullet\"]}", distance));
        }
        FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "execute as @e[tag=faycore_gun.bullet] at %player% rotated as %player% positioned ^ ^-0.25 ^0.5 run tp @s ~ ~1.75 ~ ~ ~");
        if (SkillInteractScreen.GunMode == 0) {
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "execute as @e[tag=faycore_gun.bullet] at %player% positioned ^ ^-0.25 ^0.5 run playsound minecraft:entity.copper_golem.death master @a ~ ~ ~ 5 1 1");
        } else if (SkillInteractScreen.GunMode == 1) {
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "execute as @e[tag=faycore_gun.bullet] at %player% positioned ^ ^-0.25 ^0.5 run playsound minecraft:block.beacon.deactivate master @a ~ ~ ~ 5 1 1");
        }
        if (SkillInteractScreen.GunMode != 2) {
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "execute as %player% at @s positioned ^ ^-0.25 ^1 run particle minecraft:dust{color:[0.2,0.2,0.2],scale:0.5} ~ ~1.75 ~ 0.05 0.05 0.05 0 10 force @a");
        } else {
            target = GunInput.getTargetLocation(mc, 100.0);
            double ex = target.x;
            double ey = target.y;
            double ez = target.z;
            String coreFlash = String.format("particle minecraft:explosion_emitter %.2f %.2f %.2f 0 0 0 0 1 force @a", ex, ey, ez);
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, coreFlash);
            String capSmoke = String.format("particle minecraft:campfire_cosy_smoke %.2f %.2f %.2f 1.5 0.5 1.5 0.05 35 force @a", ex, ey + 3.0, ez);
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, capSmoke);
            for (double height = 0.5; height <= 2.5; height += 0.5) {
                String stemSmoke = String.format("particle minecraft:smoke %.2f %.2f %.2f 0.2 %.1f 0.2 0.01 8 force @a", ex, ey + height, ez, height);
                String stemLava = String.format("particle minecraft:lava %.2f %.2f %.2f 0.1 0.2 0.1 0.1 3 force @a", ex, ey + height, ez);
                FayCoreMacroEngine.autoFindAndInjectVCommand(mc, stemSmoke);
                FayCoreMacroEngine.autoFindAndInjectVCommand(mc, stemLava);
            }
            String debrisFlame = String.format("particle minecraft:flame %.2f %.2f %.2f 0.5 0.5 0.5 0.3 15 force @a", ex, ey + 0.5, ez);
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, debrisFlame);
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "summon tnt " + target.x + " " + target.y + " " + target.z + " {fuse:0,explosion_power:10}");
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "execute as %player% at @s positioned ^-0.25 ^-0.25 ^1.5 run particle minecraft:dust{color:[0.2,0.2,0.2],scale:0.5} ~ ~1.75 ~ 0.05 0.05 0.05 0 10 force @a");
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "execute as %player% at @s positioned ^-0.25 ^-0.25 ^1.5 run particle minecraft:lava ~ ~1.75 ~ 0.05 0.05 0.05 0 10 force @a");
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "execute as %player% at @s positioned ^-0.25 ^-0.25 ^1.5 run particle minecraft:explosion ~ ~1.75 ~ 0.05 0.05 0.05 1 1 force @a");
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "execute as %player% at @s positioned ^-0.25 ^-0.25 ^1.5 run playsound minecraft:entity.dragon_fireball.explode player @a ~ ~ ~ 1 1 1");
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "execute as %player% at @s positioned ^-0.25 ^-0.25 ^1.5 run playsound minecraft:entity.generic.explode player @a ~ ~ ~ 1 1 1");
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "execute as %player% at @s positioned ^-0.25 ^-0.25 ^1.5 run playsound minecraft:item.trident.thunder player @a ~ ~ ~ 1 1 1");
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "execute as %player% at @s positioned ^-0.25 ^-0.25 ^1.5 run playsound minecraft:entity.wither.shoot player @a ~ ~ ~ 1 2 1");
        }
        if (SkillInteractScreen.GunMode == 0) {
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, String.format("execute positioned %f %f %f run particle minecraft:lava ~ ~ ~ 0 0 0 0.25 10 force @a", targetPos.x, targetPos.y, targetPos.z));
        } else if (SkillInteractScreen.GunMode == 1) {
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, String.format("execute positioned %f %f %f run particle minecraft:raid_omen ~ ~ ~ 0.25 0.25 0.25 1.245415898 25 force @a", targetPos.x, targetPos.y, targetPos.z));
        }
        waiting = true;
        delay = 5;
        if (entityHit != null) {
            target = entityHit.getEntity();
            UUID uuid = target.getUUID();
            if (SkillInteractScreen.GunMode == 0) {
                String gameruleCmd1 = "gamerule show_death_messages false";
                FayCoreMacroEngine.autoFindAndInjectVCommand(mc, gameruleCmd1);
                FayCoreMacroEngine.autoFindAndInjectVCommand(mc, String.format("damage %s 20 minecraft:arrow by %%player%%", uuid));
                String customDeathMessageCmd = String.format("execute as %s if entity @s[type=minecraft:player] if entity @s[nbt={Health:0f}] run tellraw @a [\"\",{selector:\"%s\"},\" was killed by %%player%% using \",\"[\",{text:\"\u00a79%%player%%'s Gun\",hover_event:{action:\"show_text\",value:[\"\u00a79FayeCruz's Gun\\n\",{\"text\":\"\ua730\u1d00\u028f\u1d04\u1d0f\u0280\u1d07\",\"color\":\"#B0FF95\",italic:false},\"\\n\u00a77Made by \u00a7cFayeCruz\"]}},\"]\"]", uuid, uuid);
                FayCoreMacroEngine.autoFindAndInjectVCommand(mc, customDeathMessageCmd);
                String gameruleCmd2 = "gamerule show_death_messages true";
                FayCoreMacroEngine.autoFindAndInjectVCommand(mc, gameruleCmd2);
            } else if (SkillInteractScreen.GunMode == 1) {
                String gameruleCmd1 = "gamerule show_death_messages false";
                FayCoreMacroEngine.autoFindAndInjectVCommand(mc, gameruleCmd1);
                String customDeathMessageCmd = String.format("execute as %s if entity @s[type=minecraft:player] unless entity @s[nbt={Health:0f}] run tellraw @a [\"\",{selector:\"%s\"},\" was killed by %%player%% using \",\"[\",{text:\"\u00a79%%player%%'s Gun\",hover_event:{action:\"show_text\",value:[\"\u00a79FayeCruz's Gun\\n\",{\"text\":\"\ua730\u1d00\u028f\u1d04\u1d0f\u0280\u1d07\",\"color\":\"#B0FF95\",italic:false},\"\\n\u00a77Made by \u00a7cFayeCruz\"]}},\"]\"]", uuid, uuid);
                FayCoreMacroEngine.autoFindAndInjectVCommand(mc, customDeathMessageCmd);
                String killCmd = "kill " + String.valueOf(uuid);
                FayCoreMacroEngine.autoFindAndInjectVCommand(mc, killCmd);
                String gameruleCmd2 = "gamerule show_death_messages true";
                FayCoreMacroEngine.autoFindAndInjectVCommand(mc, gameruleCmd2);
            }
        }
    }
}

