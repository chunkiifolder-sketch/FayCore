package chunk.faye.mod_tog.faycore;

import chunk.faye.mod_tog.faycore.client.gui.FayCoreMacroEngine;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

public class FayCoreWings {
   public static boolean enableWings = true;
   private static float wingTimer = 0.0F;
   private static String wingParticleData = "minecraft:sculk_soul";

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK
         .register(
            (EndTick)client -> {
               if (enableWings && client.player != null && client.level != null) {
                  LocalPlayer player = client.player;
                  if (!client.isPaused()) {
                     wingTimer += 0.18F;
                     switch (SkillInteractScreen.WingMode) {
                        case 0:
                           return;
                        case 1:
                           wingParticleData = "sculk_soul";
                           break;
                        case 2:
                           wingParticleData = "cherry_leaves";
                           break;
                        case 3:
                           wingParticleData = "dust{color:[1.0,1.0,1.0],scale:1.2}";
                           break;
                        case 4:
                           wingParticleData = "crit";
                           break;
                        default:
                           wingParticleData = "sculk_soul";
                     }

                     double x = player.getX();
                     double y = player.getY() + (player.isShiftKeyDown() ? 1.0 : 1.35);
                     double z = player.getZ();
                     float bodyYaw = player.yBodyRot;
                     float radianYaw = bodyYaw * (float) (Math.PI / 180.0);
                     double lookX = (double)Mth.sin((double)radianYaw);
                     double lookZ = (double)(-Mth.cos((double)radianYaw));
                     x += lookX * 0.25;
                     z += lookZ * 0.25;
                     if (SkillInteractScreen.WingSize == 0) {
                        for (int i = 0; i < 12; i++) {
                           double wingX = (double)i * 0.12;
                           double wingY = (double)Mth.sin((double)((float)((double)i * 0.3))) * 0.4;
                           double wave = (double)Mth.sin((double)(wingTimer + (float)i * 0.2F)) * 0.08 * (double)i * 0.15;
                           wingY += wave;
                           double leftRotX = -wingX * (double)Mth.cos((double)radianYaw);
                           double leftRotZ = -wingX * (double)Mth.sin((double)radianYaw);
                           double rightRotX = wingX * (double)Mth.cos((double)radianYaw);
                           double rightRotZ = wingX * (double)Mth.sin((double)radianYaw);
                           FayCoreMacroEngine.autoFindAndInjectVCommand(
                              client, String.format("particle %s %f %f %f 0 0.02 0 0.01 1 force @a", wingParticleData, x + leftRotX, y + wingY, z + leftRotZ)
                           );
                           FayCoreMacroEngine.autoFindAndInjectVCommand(
                              client, String.format("particle %s %f %f %f 0 0.02 0 0.01 1 force @a", wingParticleData, x + rightRotX, y + wingY, z + rightRotZ)
                           );
                           FayCoreMacroEngine.autoFindAndInjectVCommand(
                              client,
                              String.format("particle %s %f %f %f 0 0.02 0 0.01 1 force @a", wingParticleData, x + leftRotX, y + wingY - 0.25, z + leftRotZ)
                           );
                           FayCoreMacroEngine.autoFindAndInjectVCommand(
                              client,
                              String.format("particle %s %f %f %f 0 0.02 0 0.01 1 force @a", wingParticleData, x + rightRotX, y + wingY - 0.25, z + rightRotZ)
                           );
                           FayCoreMacroEngine.autoFindAndInjectVCommand(
                              client,
                              String.format("particle %s %f %f %f 0 0.02 0 0.01 1 force @a", wingParticleData, x + leftRotX, y + wingY - 0.5, z + leftRotZ)
                           );
                           FayCoreMacroEngine.autoFindAndInjectVCommand(
                              client,
                              String.format("particle %s %f %f %f 0 0.02 0 0.01 1 force @a", wingParticleData, x + rightRotX, y + wingY - 0.5, z + rightRotZ)
                           );
                        }
                     }

                     if (SkillInteractScreen.WingSize == 1) {
                        for (int i = 0; i < 13; i++) {
                           double wingX = (double)i * 0.26;
                           double upperWingY = (double)Mth.sin((double)((float)((double)i * 0.24))) * 0.9;
                           double upperWave = (double)Mth.sin((double)(wingTimer + (float)i * 0.18F)) * 0.2 * (double)i * 0.25;
                           upperWingY += upperWave;
                           double lowerWingY = (double)(-Mth.sin((double)((float)((double)i * 0.2)))) * 0.5;
                           double lowerWave = (double)Mth.cos((double)(wingTimer - (float)i * 0.15F)) * 0.15 * (double)i * 0.2;
                           lowerWingY += lowerWave;
                           double centerWingY = (upperWingY + lowerWingY) / 2.0;
                           double centerWave = (double)Mth.sin((double)(wingTimer * 1.5F + (float)i * 0.3F)) * 0.05;
                           centerWingY += centerWave;
                           double upperCenterY = (upperWingY + centerWingY) / 2.0;
                           double lowerCenterY = (lowerWingY + centerWingY) / 2.0;
                           double leftRotX = -wingX * (double)Mth.cos((double)radianYaw);
                           double leftRotZ = -wingX * (double)Mth.sin((double)radianYaw);
                           double rightRotX = wingX * (double)Mth.cos((double)radianYaw);
                           double rightRotZ = wingX * (double)Mth.sin((double)radianYaw);
                           FayCoreMacroEngine.autoFindAndInjectVCommand(
                              client,
                              String.format(
                                 "particle minecraft:%s %f %f %f 0 0.015 0 0.005 1 force @a", wingParticleData, x + leftRotX, y + upperWingY, z + leftRotZ
                              )
                           );
                           FayCoreMacroEngine.autoFindAndInjectVCommand(
                              client,
                              String.format(
                                 "particle minecraft:%s %f %f %f 0 0.015 0 0.005 1 force @a", wingParticleData, x + rightRotX, y + upperWingY, z + rightRotZ
                              )
                           );
                           FayCoreMacroEngine.autoFindAndInjectVCommand(
                              client,
                              String.format(
                                 "particle minecraft:%s %f %f %f 0 0.01 0 0.003 1 force @a", wingParticleData, x + leftRotX, y + lowerWingY, z + leftRotZ
                              )
                           );
                           FayCoreMacroEngine.autoFindAndInjectVCommand(
                              client,
                              String.format(
                                 "particle minecraft:%s %f %f %f 0 0.01 0 0.003 1 force @a", wingParticleData, x + rightRotX, y + lowerWingY, z + rightRotZ
                              )
                           );
                           FayCoreMacroEngine.autoFindAndInjectVCommand(
                              client,
                              String.format(
                                 "particle minecraft:%s %f %f %f 0.02 0.02 0.02 0.005 1 force @a",
                                 wingParticleData,
                                 x + leftRotX,
                                 y + centerWingY,
                                 z + leftRotZ
                              )
                           );
                           FayCoreMacroEngine.autoFindAndInjectVCommand(
                              client,
                              String.format(
                                 "particle minecraft:%s %f %f %f 0.02 0.02 0.02 0.005 1 force @a",
                                 wingParticleData,
                                 x + rightRotX,
                                 y + centerWingY,
                                 z + rightRotZ
                              )
                           );
                           FayCoreMacroEngine.autoFindAndInjectVCommand(
                              client,
                              String.format(
                                 "particle minecraft:%s %f %f %f 0.01 0.01 0.01 0.005 1 force @a",
                                 wingParticleData,
                                 x + leftRotX,
                                 y + upperCenterY,
                                 z + leftRotZ
                              )
                           );
                           FayCoreMacroEngine.autoFindAndInjectVCommand(
                              client,
                              String.format(
                                 "particle minecraft:%s %f %f %f 0.01 0.01 0.01 0.005 1 force @a",
                                 wingParticleData,
                                 x + rightRotX,
                                 y + upperCenterY,
                                 z + rightRotZ
                              )
                           );
                           FayCoreMacroEngine.autoFindAndInjectVCommand(
                              client,
                              String.format(
                                 "particle minecraft:%s %f %f %f 0.01 0.01 0.01 0.005 1 force @a",
                                 wingParticleData,
                                 x + leftRotX,
                                 y + lowerCenterY,
                                 z + leftRotZ
                              )
                           );
                           FayCoreMacroEngine.autoFindAndInjectVCommand(
                              client,
                              String.format(
                                 "particle minecraft:%s %f %f %f 0.01 0.01 0.01 0.005 1 force @a",
                                 wingParticleData,
                                 x + rightRotX,
                                 y + lowerCenterY,
                                 z + rightRotZ
                              )
                           );
                        }
                     }

                     if (SkillInteractScreen.WingSize == 2) {
                        double velX = player.getDeltaMovement().x;
                        double velZ = player.getDeltaMovement().z;
                        double speed = Math.sqrt(velX * velX + velZ * velZ);
                        float capePitch = 85.0F;
                        if (speed > 0.05) {
                           capePitch -= (float)(speed * 160.0);
                        }

                        if (capePitch < 15.0F) {
                           capePitch = 15.0F;
                        }

                        if (capePitch > 85.0F) {
                           capePitch = 85.0F;
                        }

                        float radianPitch = capePitch * (float) (Math.PI / 180.0);
                        double capeY = player.getY() + (player.isShiftKeyDown() ? 1.05 : 1.35);

                        for (int row = 0; row < 8; row++) {
                           double length = (double)row * 0.2;
                           double dragX = Math.sin((double)radianYaw) * Math.cos((double)radianPitch) * length;
                           double dragY = -Math.sin((double)radianPitch) * length;
                           double dragZ = -Math.cos((double)radianYaw) * Math.cos((double)radianPitch) * length;

                           for (int col = -2; col <= 2; col++) {
                              double width = (double)col * 0.09;
                              double rotX = -width * Math.cos((double)radianYaw);
                              double rotZ = -width * Math.sin((double)radianYaw);
                              String capeCmd = String.format(
                                 "particle %s %f %f %f 0.01 0.01 0.01 0.005 1 force @a", wingParticleData, x + rotX + dragX, capeY + dragY, z + rotZ + dragZ
                              );
                              FayCoreMacroEngine.autoFindAndInjectVCommand(client, capeCmd);
                           }
                        }
                     }
                  }
               }
            }
         );
   }
}
