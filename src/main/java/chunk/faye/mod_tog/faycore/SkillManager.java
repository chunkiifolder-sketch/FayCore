package chunk.faye.mod_tog.faycore;

import chunk.faye.mod_tog.faycore.client.gui.FayCoreMacroEngine;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class SkillManager {
   private static final String EMPTY_SLOT = "execute as %player% at @s run tellraw @s {\"text\":\"此處空槽\",\"color\":\"red\"}";
   private static float laserRollAngle = 0.0F;
   private static float DEGREES_PER_SECOND = 360.0F;
   private static int INTERPOLATION_DURATION = 1;
   private static int TELEPORT_DISTANCE = 5;
   private static String LASER_COLOR = "B385FF";
   static float LaserScale = 0.25F;
   public static float TNT_POWER = 3.0F;
   public static int DELETE_POWER = 1;
   public static int FLING_POWER = 1;
   public static boolean Wing = false;
   public static int teleportDistance = TELEPORT_DISTANCE;
   public static boolean IsControl = false;
   private static int laser_color = Integer.parseInt(LASER_COLOR, 16);
   private static int safeTickBuffer = 0;

   public static void castPressSkill(int slot) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         switch (slot) {
            case 0:
               String cmd1 = FayCoreMacroEngine.parseDynamicVariables("execute as %player% at @s run particle minecraft:cloud ~ ~ ~ 0 0 0 0.1 25 force @a");
               String cmd2 = FayCoreMacroEngine.parseDynamicVariables(
                  "execute as %player% at @s run particle minecraft:trial_spawner_detection ~ ~ ~ 0.5 0.5 0.5 0 25 force @a"
               );
               String cmd3 = FayCoreMacroEngine.parseDynamicVariables("execute as %player% at @s run effect give @s minecraft:levitation 1 100 true");
               FayCoreMacroEngine.executeVPressCommand(mc, cmd1);
               FayCoreMacroEngine.executeVPressCommand(mc, cmd2);
               FayCoreMacroEngine.executeVPressCommand(mc, cmd3);
               SkillInput.isVKeyWaitingForPhysicalRelease = true;
               CompletableFuture.delayedExecutor(30L, TimeUnit.MILLISECONDS)
                  .execute(
                     () -> mc.execute(
                           () -> {
                              if (mc.player != null && mc.level != null) {
                                 String releaseCmd = FayCoreMacroEngine.parseDynamicVariables(
                                    "execute as %player% at @s run effect clear @s minecraft:levitation"
                                 );
                                 FayCoreMacroEngine.executeVReleaseCommand(mc, releaseCmd);
                                 mc.level
                                    .playLocalSound(
                                       mc.player.getX(),
                                       mc.player.getY(),
                                       mc.player.getZ(),
                                       SoundEvents.LIGHTNING_BOLT_THUNDER,
                                       SoundSource.PLAYERS,
                                       0.6F,
                                       1.0F,
                                       false
                                    );
                                 SkillInput.isVKeyCurrentlyHolding = false;
                              }
                           }
                        )
                  );
               break;
            case 1:
               laserRollAngle = laserRollAngle + DEGREES_PER_SECOND / 20.0F;
               if (laserRollAngle >= 360.0F) {
                  laserRollAngle -= 360.0F;
               }

               double rad = Math.toRadians((double)laserRollAngle);
               float sinHalf = (float)Math.sin(rad / 2.0);
               float cosHalf = (float)Math.cos(rad / 2.0);
               FayCoreMacroEngine.executeVPressCommand(mc, "execute as @p run kill @e[tag=faycore_laser,distance=..100]");
               FayCoreMacroEngine.executeVPressCommand(mc, "execute as @p run kill @e[tag=favycore_laser_glass,distance=..100]");
               String spawnCmd1 = FayCoreMacroEngine.parseDynamicVariables(
                  "execute as %player% at @s anchored eyes run summon minecraft:block_display ^ ^ ^1 {block_state:{Name:\"minecraft:end_gateway\"},Tags:[\"faycore_laser\"],Glowing:0b,view_range:128.0f,start_interpolation:0,transformation:{translation:[0f,0f,0f],left_rotation:[0f,0f,"
                     + sinHalf
                     + "f,"
                     + cosHalf
                     + "f],right_rotation:[0f,0f,0f,1f],scale:["
                     + LaserScale
                     + "f,"
                     + LaserScale
                     + "f,"
                     + LaserScale
                     + "f]},start_interpolation:0,interpolation_duration:1}"
               );
               FayCoreMacroEngine.executeVPressCommand(mc, spawnCmd1);
               float LaserGlassScale = LaserScale * 1.5F;
               String spawnCmd2 = FayCoreMacroEngine.parseDynamicVariables(
                  "execute as %player% at @s anchored eyes run summon minecraft:item_display ^ ^ ^1 {item:{id:\"minecraft:black_stained_glass\"},Tags:[\"faycore_laser_glass\"],Glowing:1b,brightness:{block:15,sky:15},view_range:128.0f,start_interpolation:0,transformation:{translation:[0f,0f,0f],left_rotation:[0f,0f,"
                     + sinHalf
                     + "f,"
                     + cosHalf
                     + "f],right_rotation:[0f,0f,0f,1f],scale:["
                     + LaserGlassScale
                     + "f,"
                     + LaserGlassScale
                     + "f,"
                     + LaserGlassScale
                     + "f]},interpolation_duration:"
                     + INTERPOLATION_DURATION
                     + ",start_interpolation:0}"
               );
               FayCoreMacroEngine.executeVPressCommand(mc, spawnCmd2);
               FayCoreMacroEngine.executeVPressCommand(mc, "execute as %player% at @s rotated as @s anchored eyes run tp @e[tag=faycore_laser] ^ ^ ^1 ~ ~");
               FayCoreMacroEngine.executeVPressCommand(
                  mc, "execute as %player% at @s rotated as @s anchored eyes run tp @e[tag=faycore_laser_glass] ^ ^ ^0.95 ~ ~"
               );
               if (mc.level != null && mc.player != null) {
                  mc.level
                     .playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8F, 1.8F, false);
               }
            case 2:
            case 5:
            case 6:
               break;
            case 3:
               EntityHitResult entityHit = EntityRaycast.raycastEntity(mc, 3.0);
               if (entityHit != null) {
                  Entity target = entityHit.getEntity();
                  SkillTracker.glowTarget = target.getUUID();
                  String TargetName = target.getName().getString();
                  FayCoreMacroEngine.autoFindAndInjectVCommand(
                     mc, "title %player% actionbar \"§3Locked Name: §7" + TargetName + " §8; §3Control Status: §7" + IsControl + "\""
                  );
               }
               break;
            case 4:
               Vec3 eyePos = mc.player.getEyePosition();
               Vec3 look = mc.player.getLookAngle();
               ClipContext context = new ClipContext(eyePos, eyePos.add(look.scale((double)teleportDistance)), Block.COLLIDER, Fluid.NONE, mc.player);
               Vec3 targetPos = eyePos.add(look.scale((double)teleportDistance)).add(0.0, -0.75, 0.0);
               BlockHitResult hit = mc.level.clip(context);
               if (hit.getType() == Type.BLOCK) {
                  targetPos = hit.getLocation().subtract(look.scale(0.5));
               } else {
                  targetPos = eyePos.add(look.scale((double)teleportDistance));
               }

               targetPos = targetPos.add(0.0, -0.75, 0.0);
               Vec3 direction = targetPos.subtract(eyePos);
               double distance = direction.length();
               Vec3 unit = direction.normalize();
               double step = 0.5;

               for (double d = 0.0; d <= distance; d += step) {
                  Vec3 pos = eyePos.add(unit.scale(d));
                  String particleCmd = String.format("particle minecraft:end_rod %f %f %f 0 0 0 0 1 force @a", pos.x, pos.y, pos.z);
                  FayCoreMacroEngine.autoFindAndInjectVCommand(mc, particleCmd);
               }

               String TeleportParticleCmd1 = String.format(
                  "particle minecraft:poof %f %f %f 0.25 0.5 0.25 0.15845348 25 force @a", mc.player.getX(), mc.player.getY() + 0.75, mc.player.getZ()
               );
               String TeleportCmd = String.format("tp %%player%% %f %f %f", targetPos.x, targetPos.y, targetPos.z);
               String TeleportParticleCmd2 = String.format(
                  "particle minecraft:dust{color:[0.55,0.0,1.0],scale:0.5} %f %f %f 0.5 0.75 0.5 5 25 force @a", targetPos.x, targetPos.y + 0.75, targetPos.z
               );
               FayCoreMacroEngine.autoFindAndInjectVCommand(mc, TeleportParticleCmd1);
               FayCoreMacroEngine.autoFindAndInjectVCommand(mc, TeleportCmd);
               FayCoreMacroEngine.autoFindAndInjectVCommand(mc, TeleportParticleCmd2);
               break;
            default:
               executeSingleCommandList(mc, List.of("faycore_pressed"), true);
         }
      }
   }

   public static void castHoldingSkill(int slot) {
      Minecraft mc = Minecraft.getInstance();
      switch (slot) {
         case 0:
         case 2:
         case 4:
         case 5:
         case 6:
            break;
         case 1:
            final LocalPlayer player = mc.player;
            if (player == null || mc.level == null) {
               return;
            }

            Vec3 startPos = player.getEyePosition(1.0F);
            Vec3 lookAngle = player.getLookAngle();
            float maxRange = 100.0F;
            Vec3 endPos = startPos.add(lookAngle.x * (double)maxRange, lookAngle.y * (double)maxRange, lookAngle.z * (double)maxRange);
            float finalDistance = maxRange;
            HitResult blockHitResult = player.pick((double)maxRange, 1.0F, false);
            if (blockHitResult != null && blockHitResult.getType() == Type.BLOCK) {
               BlockHitResult bHit = (BlockHitResult)blockHitResult;
               finalDistance = (float)startPos.distanceTo(bHit.getLocation());
               endPos = bHit.getLocation();
               switch (SkillInteractScreen.LaserMode) {
                  case 0:
                     String tntCmd = String.format("summon minecraft:tnt %f %f %f {fuse:0,explosion_power:%f}", endPos.x, endPos.y, endPos.z, TNT_POWER);
                     FayCoreMacroEngine.autoFindAndInjectVCommand(mc, tntCmd);
                     String flameParticleCmd = String.format("particle minecraft:flame %f %f %f 0.2 0.2 0.2 0.05 20 force @a", endPos.x, endPos.y, endPos.z);
                     FayCoreMacroEngine.autoFindAndInjectVCommand(mc, flameParticleCmd);
                     String smokeParticleCmd = String.format("particle minecraft:smoke %f %f %f 0.2 0.2 0.2 0.02 10 force @a", endPos.x, endPos.y, endPos.z);
                     FayCoreMacroEngine.autoFindAndInjectVCommand(mc, smokeParticleCmd);
                     break;
                  case 1:
                     int power = DELETE_POWER;

                     for (int x = -power; x <= power; x++) {
                        for (int y = -power; y <= power; y++) {
                           for (int z = -power; z <= power; z++) {
                              BlockPos pos = BlockPos.containing(endPos.x + (double)x, endPos.y + (double)y, endPos.z + (double)z);
                              BlockState state = mc.level.getBlockState(pos);
                              if (!state.isAir()) {
                                 String bedrockCmd = String.format(
                                    "particle minecraft:block{block_state:{Name:\"minecraft:bedrock\"}} %d %d %d 0.25 0.25 0.25 1 2 force @a",
                                    pos.getX(),
                                    pos.getY(),
                                    pos.getZ()
                                 );
                                 FayCoreMacroEngine.autoFindAndInjectVCommand(mc, bedrockCmd);
                                 String setblockCmd = String.format("setblock %d %d %d barrier", pos.getX(), pos.getY(), pos.getZ());
                                 FayCoreMacroEngine.autoFindAndInjectVCommand(mc, setblockCmd);
                                 String destroyCmd = String.format("setblock %d %d %d air destroy", pos.getX(), pos.getY(), pos.getZ());
                                 FayCoreMacroEngine.autoFindAndInjectVCommand(mc, destroyCmd);
                              }
                           }
                        }
                     }

                     FayCoreMacroEngine.autoFindAndInjectVCommand(
                        mc,
                        String.format(
                           "particle minecraft:raid_omen %f %f %f %f %f %f %f 25 force @a",
                           endPos.x,
                           endPos.y,
                           endPos.z,
                           (float)DELETE_POWER * 0.25F,
                           (float)DELETE_POWER * 0.25F,
                           (float)DELETE_POWER * 0.25F,
                           (float)DELETE_POWER * 100.0F
                        )
                     );
                     break;
                  case 2: {
                     Vec3 dir = player.getLookAngle();
                     double speed = 2.0;
                     String windCmd = String.format(
                        "summon minecraft:wind_charge %f %f %f {Motion:[%ff,%ff,%ff]}",
                        endPos.x,
                        endPos.y,
                        endPos.z,
                        dir.x * speed,
                        dir.y * speed,
                        dir.z * speed
                     );
                     FayCoreMacroEngine.autoFindAndInjectVCommand(mc, windCmd);
                     break;
                  }
                  case 3: {
                     Vec3 dir = player.getLookAngle();
                     double speed = 2.0;
                     int flingPower = FLING_POWER;
                     String particleCmd = String.format("particle minecraft:glow %f %f %f 0 0 0 1 5 force @a", endPos.x, endPos.y, endPos.z);
                     FayCoreMacroEngine.autoFindAndInjectVCommand(mc, particleCmd);
                     RandomSource random = RandomSource.create();
                     double mx = (random.nextDouble() - 0.5) * 0.8;
                     double mz = (random.nextDouble() - 0.5) * 0.8;
                     String mobMotionCmd = String.format(
                        "execute positioned %f %f %f as @e[distance=..%d,type=!minecraft:player] run data merge entity @s {Motion:[%f,0.75,%f]}",
                        endPos.x,
                        endPos.y,
                        endPos.z,
                        flingPower,
                        mx * 2.0,
                        mz * 2.0
                     );
                     FayCoreMacroEngine.autoFindAndInjectVCommand(mc, mobMotionCmd);

                     for (int x = -flingPower; x <= flingPower; x++) {
                        for (int y = -flingPower; y <= flingPower; y++) {
                           for (int zx = -flingPower; zx <= flingPower; zx++) {
                              BlockPos pos = BlockPos.containing(endPos.x + (double)x, endPos.y + (double)y, endPos.z + (double)zx);
                              BlockState state = mc.level.getBlockState(pos);
                              if (!state.is(Blocks.BEDROCK)
                                 && !state.is(Blocks.LAVA)
                                 && !state.is(Blocks.WATER)
                                 && !state.is(Blocks.COMMAND_BLOCK)
                                 && !state.is(Blocks.REPEATING_COMMAND_BLOCK)
                                 && !state.is(Blocks.CHAIN_COMMAND_BLOCK)
                                 && !state.isAir()) {
                                 String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
                                 mx = (random.nextDouble() - 0.5) * 0.8;
                                 mz = (random.nextDouble() - 0.5) * 0.8;
                                 String summonCmd = String.format(
                                    "summon minecraft:falling_block %f %f %f {BlockState:{Name:\"%s\"},Time:1,Motion:[%f,0.8,%f]}",
                                    (double)pos.getX() + 0.5,
                                    (float)pos.getY(),
                                    (double)pos.getZ() + 0.5,
                                    blockId,
                                    mx,
                                    mz
                                 );
                                 FayCoreMacroEngine.autoFindAndInjectVCommand(mc, summonCmd);
                                 String removeCmd = String.format(
                                    "fill %d %d %d %d %d %d air", pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ()
                                 );
                                 FayCoreMacroEngine.autoFindAndInjectVCommand(mc, removeCmd);
                              }
                           }
                        }
                     }
                  }
               }
            }

            AABB searchBox = new AABB(startPos, endPos).inflate(1.0);

            for (Object obj : mc.level.getEntities(player, searchBox, new Predicate() {
               @Override
               public boolean test(Object obj) {
                  if (!(obj instanceof Entity entity)) {
                     return false;
                  } else {
                     boolean isDisplayEntity = entity instanceof Display;
                     return !entity.isSpectator() && entity.isAlive() && entity != player && !isDisplayEntity;
                  }
               }
            })) {
               if (obj instanceof Entity) {
                  Entity entity = (Entity)obj;
                  AABB entityBoundingBox = entity.getBoundingBox().inflate(0.3);
                  Optional<Vec3> clipResult = entityBoundingBox.clip(startPos, endPos);
                  if (clipResult.isPresent()) {
                     Vec3 hitPoint = clipResult.get();
                     float entityDist = (float)startPos.distanceTo(hitPoint);
                     if (entityDist < finalDistance) {
                        finalDistance = entityDist;
                        endPos = hitPoint;
                        switch (SkillInteractScreen.LaserMode) {
                           case 0:
                              String tntCmd = String.format(
                                 "summon minecraft:tnt %f %f %f {fuse:0,explosion_power:%f}", hitPoint.x, hitPoint.y, hitPoint.z, TNT_POWER
                              );
                              EntityHitResult entityHitx = EntityRaycast.raycastEntity(mc, 100.0);
                              if (entityHitx != null) {
                                 Entity target = entityHitx.getEntity();
                                 UUID uuid = target.getUUID();
                                 String gameruleCmd1 = "gamerule show_death_messages false";
                                 FayCoreMacroEngine.autoFindAndInjectVCommand(mc, gameruleCmd1);
                                 String customDeathMessageCmd = String.format(
                                    "execute as %s if entity @s[type=minecraft:player] unless entity @s[nbt={Health:0f}] run tellraw @a [\"\",{selector:\"%s\"},\" was killed by %%player%% using \",\"[\",{text:\"§9%%player%%'s Laser\",hover_event:{action:\"show_text\",value:[\"§9%%player%%'s Laser\\n\",{\"text\":\"ꜰᴀʏᴄᴏʀᴇ\",\"color\":\"#B0FF95\",italic:false},\"\\n§7Made by §cFayeCruz\"]}},\"]\"]",
                                    uuid,
                                    uuid
                                 );
                                 FayCoreMacroEngine.autoFindAndInjectVCommand(mc, customDeathMessageCmd);
                                 String killCmd = "kill " + uuid;
                                 FayCoreMacroEngine.autoFindAndInjectVCommand(mc, killCmd);
                                 String gameruleCmd2 = "gamerule show_death_messages true";
                                 FayCoreMacroEngine.autoFindAndInjectVCommand(mc, gameruleCmd2);
                              }

                              FayCoreMacroEngine.autoFindAndInjectVCommand(mc, tntCmd);
                              String flameParticleCmd = String.format(
                                 "particle minecraft:flame %f %f %f 0.2 0.2 0.2 0.05 20 force @a", hitPoint.x, hitPoint.y, hitPoint.z
                              );
                              FayCoreMacroEngine.autoFindAndInjectVCommand(mc, flameParticleCmd);
                              String smokeParticleCmd = String.format(
                                 "particle minecraft:smoke %f %f %f 0.2 0.2 0.2 0.02 10 force @a", hitPoint.x, hitPoint.y, hitPoint.z
                              );
                              FayCoreMacroEngine.autoFindAndInjectVCommand(mc, smokeParticleCmd);
                              break;
                           case 1:
                              String deleteCmd = String.format(
                                 "fill %d %d %d %d %d %d air",
                                 (int)(hitPoint.x - 1.0),
                                 (int)(hitPoint.y - 1.0),
                                 (int)(hitPoint.z - 1.0),
                                 (int)(hitPoint.x + 1.0),
                                 (int)(hitPoint.y + 1.0),
                                 (int)(hitPoint.z + 1.0)
                              );
                              FayCoreMacroEngine.autoFindAndInjectVCommand(mc, deleteCmd);
                              FayCoreMacroEngine.autoFindAndInjectVCommand(
                                 mc,
                                 String.format(
                                    "particle minecraft:raid_omen %f %f %f 0.25 0.25 0.25 1.845415898 25 force @a", hitPoint.x, hitPoint.y, hitPoint.z
                                 )
                              );
                              EntityHitResult entityHit = EntityRaycast.raycastEntity(mc, 100.0);
                              if (entityHit != null) {
                                 Entity target = entityHit.getEntity();
                                 UUID uuid = target.getUUID();
                                 String gameruleCmd1 = "gamerule show_death_messages false";
                                 FayCoreMacroEngine.autoFindAndInjectVCommand(mc, gameruleCmd1);
                                 String customDeathMessageCmd = String.format(
                                    "execute as %s if entity @s[type=minecraft:player] unless entity @s[nbt={Health:0f}] run tellraw @a [\"\",{selector:\"%s\"},\" was killed by %%player%% using \",\"[\",{text:\"§9%%player%%'s Laser\",hover_event:{action:\"show_text\",value:[\"§9%%player%%'s Laser\\n\",{\"text\":\"ꜰᴀʏᴄᴏʀᴇ\",\"color\":\"#B0FF95\",italic:false},\"\\n§7Made by §cFayeCruz\"]}},\"]\"]",
                                    uuid,
                                    uuid
                                 );
                                 FayCoreMacroEngine.autoFindAndInjectVCommand(mc, customDeathMessageCmd);
                                 String killCmd = "kill " + uuid;
                                 FayCoreMacroEngine.autoFindAndInjectVCommand(mc, killCmd);
                                 String gameruleCmd2 = "gamerule show_death_messages true";
                                 FayCoreMacroEngine.autoFindAndInjectVCommand(mc, gameruleCmd2);
                              }
                              break;
                           case 2:
                              Vec3 dir = player.getLookAngle();
                              double speed = 2.0;
                              String windCmd = String.format(
                                 "summon minecraft:wind_charge %f %f %f {Motion:[%ff,%ff,%ff]}",
                                 hitPoint.x,
                                 hitPoint.y,
                                 hitPoint.z,
                                 dir.x * speed,
                                 dir.y * speed,
                                 dir.z * speed
                              );
                              FayCoreMacroEngine.autoFindAndInjectVCommand(mc, windCmd);
                        }
                     }
                  }
               }
            }

            String posStr = endPos.x + " " + endPos.y + " " + endPos.z;
            String baseTpCmdInner = FayCoreMacroEngine.parseDynamicVariables(
               "execute as %player% at @s anchored eyes run tp @e[tag=faycore_laser,distance=..70,limit=1] ^ ^ ^1 facing " + posStr
            );
            String baseTpCmdGlass = FayCoreMacroEngine.parseDynamicVariables(
               "execute as %player% at @s anchored eyes run tp @e[tag=faycore_laser_glass,distance=..70,limit=1] ^ ^ ^1.2 facing " + posStr
            );
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, baseTpCmdInner);
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, baseTpCmdGlass);
            laserRollAngle = laserRollAngle + DEGREES_PER_SECOND / 20.0F;
            if (laserRollAngle >= 360.0F) {
               laserRollAngle -= 360.0F;
            }

            float halfTranslateZ = finalDistance / 2.0F;
            double rad = Math.toRadians((double)laserRollAngle);
            float sinHalf = (float)Math.sin(rad / 2.0);
            float cosHalf = (float)Math.cos(rad / 2.0);
            float pivotInner = 0.5F * LaserScale;
            float compXInner = (float)((double)(-pivotInner) * Math.cos(rad) + (double)pivotInner * Math.sin(rad));
            float compYInner = (float)((double)(-pivotInner) * Math.sin(rad) - (double)pivotInner * Math.cos(rad));
            String transformCmdInner = String.format(
               "execute as @p run data merge entity @e[tag=faycore_laser,distance=..70,limit=1] {transformation:{translation:[%ff,%ff,0.0f],left_rotation:[0.0f,0.0f,%ff,%ff],scale:[%ff,%ff,%ff],right_rotation:[0.0f,0.0f,0.0f,1.0f]},interpolation_duration:1,start_interpolation:0,glow_color_override:%s,teleport_duration:2}",
               compXInner,
               compYInner,
               sinHalf,
               cosHalf,
               LaserScale,
               LaserScale,
               finalDistance,
               laser_color
            );
            String transformCmdGlass = String.format(
               "execute as @p run data merge entity @e[tag=faycore_laser_glass,distance=..70,limit=1] {transformation:{translation:[0.0f,0.0f,%ff],left_rotation:[0.0f,0.0f,%ff,%ff],scale:[%ff,%ff,%ff],right_rotation:[0.0f,0.0f,0.0f,1.0f]},interpolation_duration:1,start_interpolation:0,glow_color_override:%s,teleport_duration:2}",
               halfTranslateZ,
               sinHalf,
               cosHalf,
               (double)LaserScale * 1.5,
               (double)LaserScale * 1.5,
               finalDistance + 0.5F,
               laser_color
            );
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, transformCmdInner);
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, transformCmdGlass);
            SkillTracker.usingLaser = true;
            FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "execute at %player% run playsound minecraft:block.beacon.ambient player @a ~ ~ ~ 2.5 1 1");
            break;
         case 3:
            IsControl = !IsControl;
            if (!IsControl) {
               SkillTracker.controlTarget = null;
               SkillTracker.CtrlIsStarted = false;
            }
            break;
         default:
            executeSingleCommandList(mc, List.of("faycore_holding"), false);
      }
   }

   public static void castReleaseSkill(int slot) {
      Minecraft mc = Minecraft.getInstance();
      switch (slot) {
         case 0:
            FayCoreMacroEngine.matrixOwnedBlocksCache.get(9).clear();
            break;
         case 1:
            FayCoreMacroEngine.matrixOwnedBlocksCache.get(9).clear();
            FayCoreMacroEngine.executeVReleaseCommand(
               mc, "execute as %player% at @s anchored eyes positioned ^ ^ ^1 run particle minecraft:poof ~ ~ ~ 0 0 0 0 10 force @a"
            );
            FayCoreMacroEngine.executeVReleaseCommand(mc, "kill @e[tag=faycore_laser]");
            FayCoreMacroEngine.executeVReleaseCommand(mc, "kill @e[tag=faycore_laser_glass]");
            SkillTracker.usingLaser = false;
            safeTickBuffer = 0;
            if (mc.level != null && mc.player != null) {
               mc.level
                  .playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8F, 1.5F, false);
            }
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
            break;
         default:
            executeSingleCommandList(mc, List.of("execute as %player% at @s run tellraw @s {\"text\":\"faycore_released\",\"color\":\"gray\"}"), false);
      }
   }

   private static void executeSingleCommandList(Minecraft mc, List<String> commands, boolean isPress) {
      for (String cmd : commands) {
         String parsed = parseDynamicVariables(cmd);
         if (isPress) {
            FayCoreMacroEngine.executeVPressCommand(mc, parsed);
         } else {
            FayCoreMacroEngine.executeVReleaseCommand(mc, parsed);
         }
      }
   }

   public static String parseDynamicVariables(String originalCommand) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         int px = (int)Math.floor(mc.player.getX());
         int py = (int)Math.floor(mc.player.getY());
         int pz = (int)Math.floor(mc.player.getZ());
         String parsed = originalCommand.replace("%pos-x%", String.valueOf(px))
            .replace("%pos-y%", String.valueOf(py))
            .replace("%pos-z%", String.valueOf(pz))
            .replace("%player%", mc.player.getScoreboardName());
         Direction dir = mc.player.getDirection();
         parsed = parsed.replace("%direction%", dir.getName().toLowerCase());
         HitResult manualHit = mc.player.pick(300.0, 1.0F, false);
         if (manualHit != null && manualHit.getType() == Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult)manualHit;
            BlockPos lookPos = blockHit.getBlockPos();
            parsed = parsed.replace("%look-x%", String.valueOf(lookPos.getX()))
               .replace("%look-y%", String.valueOf(lookPos.getY()))
               .replace("%look-z%", String.valueOf(lookPos.getZ()));
         }

         return parsed;
      } else {
         return originalCommand;
      }
   }
}
