/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.util.Mth
 */
package chunk.faye.mod_tog.faycore;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import chunk.faye.mod_tog.faycore.SkillInteractScreen;
import chunk.faye.mod_tog.faycore.client.gui.FayCoreMacroEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

public class FayCoreWings {
    public static boolean enableWings = true;
    private static float wingTimer = 0.0f;
    private static String wingParticleData = "minecraft:sculk_soul";

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            double wingX;
            int i;
            if (!enableWings || client.player == null || client.level == null) {
                return;
            }
            LocalPlayer player = client.player;
            if (!client.isPaused()) {
                wingTimer += 0.18f;
            } else {
                return;
            }
            switch (SkillInteractScreen.WingMode) {
                case 0: {
                    return;
                }
                case 1: {
                    wingParticleData = "sculk_soul";
                    break;
                }
                case 2: {
                    wingParticleData = "cherry_leaves";
                    break;
                }
                case 3: {
                    wingParticleData = "dust{color:[1.0,1.0,1.0],scale:1.2}";
                    break;
                }
                case 4: {
                    wingParticleData = "crit";
                    break;
                }
                default: {
                    wingParticleData = "sculk_soul";
                }
            }
            double x = player.getX();
            double y = player.getY() + (player.isShiftKeyDown() ? 1.0 : 1.35);
            double z = player.getZ();
            float bodyYaw = player.yBodyRot;
            float radianYaw = bodyYaw * ((float)Math.PI / 180);
            double lookX = Mth.sin((double)radianYaw);
            double lookZ = -Mth.cos((double)radianYaw);
            x += lookX * 0.25;
            z += lookZ * 0.25;
            if (SkillInteractScreen.WingSize == 0) {
                for (i = 0; i < 12; ++i) {
                    wingX = (double)i * 0.12;
                    double wingY = (double)Mth.sin((double)((float)((double)i * 0.3))) * 0.4;
                    double wave = (double)Mth.sin((double)(wingTimer + (float)i * 0.2f)) * 0.08 * ((double)i * 0.15);
                    double leftRotX = -wingX * (double)Mth.cos((double)radianYaw);
                    double leftRotZ = -wingX * (double)Mth.sin((double)radianYaw);
                    double rightRotX = wingX * (double)Mth.cos((double)radianYaw);
                    double rightRotZ = wingX * (double)Mth.sin((double)radianYaw);
                    FayCoreMacroEngine.autoFindAndInjectVCommand(client, String.format("particle %s %f %f %f 0 0.02 0 0.01 1 force @a", wingParticleData, x + leftRotX, y + (wingY += wave), z + leftRotZ));
                    FayCoreMacroEngine.autoFindAndInjectVCommand(client, String.format("particle %s %f %f %f 0 0.02 0 0.01 1 force @a", wingParticleData, x + rightRotX, y + wingY, z + rightRotZ));
                    FayCoreMacroEngine.autoFindAndInjectVCommand(client, String.format("particle %s %f %f %f 0 0.02 0 0.01 1 force @a", wingParticleData, x + leftRotX, y + wingY - 0.25, z + leftRotZ));
                    FayCoreMacroEngine.autoFindAndInjectVCommand(client, String.format("particle %s %f %f %f 0 0.02 0 0.01 1 force @a", wingParticleData, x + rightRotX, y + wingY - 0.25, z + rightRotZ));
                    FayCoreMacroEngine.autoFindAndInjectVCommand(client, String.format("particle %s %f %f %f 0 0.02 0 0.01 1 force @a", wingParticleData, x + leftRotX, y + wingY - 0.5, z + leftRotZ));
                    FayCoreMacroEngine.autoFindAndInjectVCommand(client, String.format("particle %s %f %f %f 0 0.02 0 0.01 1 force @a", wingParticleData, x + rightRotX, y + wingY - 0.5, z + rightRotZ));
                }
            }
            if (SkillInteractScreen.WingSize == 1) {
                for (i = 0; i < 13; ++i) {
                    wingX = (double)i * 0.26;
                    double upperWingY = (double)Mth.sin((double)((float)((double)i * 0.24))) * 0.9;
                    double upperWave = (double)Mth.sin((double)(wingTimer + (float)i * 0.18f)) * 0.2 * ((double)i * 0.25);
                    double lowerWingY = (double)(-Mth.sin((double)((float)((double)i * 0.2)))) * 0.5;
                    double lowerWave = (double)Mth.cos((double)(wingTimer - (float)i * 0.15f)) * 0.15 * ((double)i * 0.2);
                    double centerWingY = ((upperWingY += upperWave) + (lowerWingY += lowerWave)) / 2.0;
                    double centerWave = (double)Mth.sin((double)(wingTimer * 1.5f + (float)i * 0.3f)) * 0.05;
                    double upperCenterY = (upperWingY + (centerWingY += centerWave)) / 2.0;
                    double lowerCenterY = (lowerWingY + centerWingY) / 2.0;
                    double leftRotX = -wingX * (double)Mth.cos((double)radianYaw);
                    double leftRotZ = -wingX * (double)Mth.sin((double)radianYaw);
                    double rightRotX = wingX * (double)Mth.cos((double)radianYaw);
                    double rightRotZ = wingX * (double)Mth.sin((double)radianYaw);
                    FayCoreMacroEngine.autoFindAndInjectVCommand(client, String.format("particle minecraft:%s %f %f %f 0 0.015 0 0.005 1 force @a", wingParticleData, x + leftRotX, y + upperWingY, z + leftRotZ));
                    FayCoreMacroEngine.autoFindAndInjectVCommand(client, String.format("particle minecraft:%s %f %f %f 0 0.015 0 0.005 1 force @a", wingParticleData, x + rightRotX, y + upperWingY, z + rightRotZ));
                    FayCoreMacroEngine.autoFindAndInjectVCommand(client, String.format("particle minecraft:%s %f %f %f 0 0.01 0 0.003 1 force @a", wingParticleData, x + leftRotX, y + lowerWingY, z + leftRotZ));
                    FayCoreMacroEngine.autoFindAndInjectVCommand(client, String.format("particle minecraft:%s %f %f %f 0 0.01 0 0.003 1 force @a", wingParticleData, x + rightRotX, y + lowerWingY, z + rightRotZ));
                    FayCoreMacroEngine.autoFindAndInjectVCommand(client, String.format("particle minecraft:%s %f %f %f 0.02 0.02 0.02 0.005 1 force @a", wingParticleData, x + leftRotX, y + centerWingY, z + leftRotZ));
                    FayCoreMacroEngine.autoFindAndInjectVCommand(client, String.format("particle minecraft:%s %f %f %f 0.02 0.02 0.02 0.005 1 force @a", wingParticleData, x + rightRotX, y + centerWingY, z + rightRotZ));
                    FayCoreMacroEngine.autoFindAndInjectVCommand(client, String.format("particle minecraft:%s %f %f %f 0.01 0.01 0.01 0.005 1 force @a", wingParticleData, x + leftRotX, y + upperCenterY, z + leftRotZ));
                    FayCoreMacroEngine.autoFindAndInjectVCommand(client, String.format("particle minecraft:%s %f %f %f 0.01 0.01 0.01 0.005 1 force @a", wingParticleData, x + rightRotX, y + upperCenterY, z + rightRotZ));
                    FayCoreMacroEngine.autoFindAndInjectVCommand(client, String.format("particle minecraft:%s %f %f %f 0.01 0.01 0.01 0.005 1 force @a", wingParticleData, x + leftRotX, y + lowerCenterY, z + leftRotZ));
                    FayCoreMacroEngine.autoFindAndInjectVCommand(client, String.format("particle minecraft:%s %f %f %f 0.01 0.01 0.01 0.005 1 force @a", wingParticleData, x + rightRotX, y + lowerCenterY, z + rightRotZ));
                }
            }
            if (SkillInteractScreen.WingSize == 2) {
                double velX = player.getDeltaMovement().x;
                double velZ = player.getDeltaMovement().z;
                double speed = Math.sqrt(velX * velX + velZ * velZ);
                float capePitch = 85.0f;
                if (speed > 0.05) {
                    capePitch -= (float)(speed * 160.0);
                }
                if (capePitch < 15.0f) {
                    capePitch = 15.0f;
                }
                if (capePitch > 85.0f) {
                    capePitch = 85.0f;
                }
                float radianPitch = capePitch * ((float)Math.PI / 180);
                double capeY = player.getY() + (player.isShiftKeyDown() ? 1.05 : 1.35);
                for (int row = 0; row < 8; ++row) {
                    double length = (double)row * 0.2;
                    double dragX = Math.sin(radianYaw) * Math.cos(radianPitch) * length;
                    double dragY = -Math.sin(radianPitch) * length;
                    double dragZ = -Math.cos(radianYaw) * Math.cos(radianPitch) * length;
                    for (int col = -2; col <= 2; ++col) {
                        double width = (double)col * 0.09;
                        double rotX = -width * Math.cos(radianYaw);
                        double rotZ = -width * Math.sin(radianYaw);
                        String capeCmd = String.format("particle %s %f %f %f 0.01 0.01 0.01 0.005 1 force @a", wingParticleData, x + rotX + dragX, capeY + dragY, z + rotZ + dragZ);
                        FayCoreMacroEngine.autoFindAndInjectVCommand(client, capeCmd);
                    }
                }
            }
        });
    }
}

