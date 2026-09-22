/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundChatCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket
 *  net.minecraft.world.level.block.entity.CommandBlockEntity$Mode
 */
package net.mcreator.faycore;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.world.level.block.entity.CommandBlockEntity;

public class FayCoreRepeatCommandManager {
    public static void spawnAndDestroyGhostCore(String targetCommand) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.player.connection == null) {
            return;
        }
        BlockPos playerPos = mc.player.blockPosition();
        BlockPos ghostPos = playerPos.above(3);
        String currentBlock = mc.level.getBlockState(ghostPos).getBlock().getDescriptionId();
        if (!currentBlock.contains("air") && !(currentBlock = mc.level.getBlockState(ghostPos = playerPos.below(2)).getBlock().getDescriptionId()).contains("air")) {
            return;
        }
        BlockPos finalGhostPos = ghostPos;
        new Thread(() -> {
            try {
                String placeCoreCmd = String.format("setblock %d %d %d minecraft:repeating_command_block replace", finalGhostPos.getX(), finalGhostPos.getY(), finalGhostPos.getZ());
                mc.player.connection.send((Packet)new ServerboundChatCommandPacket(placeCoreCmd));
                Thread.sleep(20L);
                ServerboundSetCommandBlockPacket writePacket = new ServerboundSetCommandBlockPacket(finalGhostPos, targetCommand, CommandBlockEntity.Mode.REDSTONE, false, false, false);
                mc.player.connection.send((Packet)writePacket);
                Thread.sleep(15L);
                ServerboundSetCommandBlockPacket activatePacket = new ServerboundSetCommandBlockPacket(finalGhostPos, targetCommand, CommandBlockEntity.Mode.AUTO, false, false, true);
                mc.player.connection.send((Packet)activatePacket);
                Thread.sleep(40L);
                String destroyCoreCmd = String.format("setblock %d %d %d minecraft:air replace", finalGhostPos.getX(), finalGhostPos.getY(), finalGhostPos.getZ());
                mc.player.connection.send((Packet)new ServerboundChatCommandPacket(destroyCoreCmd));
                mc.player.sendSystemMessage((Component)Component.literal((String)"\u00a7a[FayCore \u5b9a\u9ede\u5e7d\u9748] \u5169\u968e\u6bb5\u901a\u96fb\u767c\u5c04\u6210\u529f\u4e26\u62b9\u9664\uff01"));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void spawnAndDestroyRandomGhostCore(String targetCommand) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.player.connection == null) {
            return;
        }
        BlockPos playerPos = mc.player.blockPosition();
        Random random = new Random();
        BlockPos chosenPos = null;
        for (int i = 0; i < 10; ++i) {
            int randomX = random.nextInt(11) - 5;
            int randomZ = random.nextInt(11) - 5;
            int randomY = random.nextInt(4) + 1;
            BlockPos checkPos = playerPos.offset(randomX, randomY, randomZ);
            String blockName = mc.level.getBlockState(checkPos).getBlock().getDescriptionId();
            if (!blockName.contains("air")) continue;
            chosenPos = checkPos;
            break;
        }
        if (chosenPos == null) {
            chosenPos = playerPos.above(3);
        }
        BlockPos finalGhostPos = chosenPos;
        new Thread(() -> {
            try {
                String placeCoreCmd = String.format("setblock %d %d %d minecraft:repeating_command_block replace", finalGhostPos.getX(), finalGhostPos.getY(), finalGhostPos.getZ());
                mc.player.connection.send((Packet)new ServerboundChatCommandPacket(placeCoreCmd));
                Thread.sleep(20L);
                ServerboundSetCommandBlockPacket writePacket = new ServerboundSetCommandBlockPacket(finalGhostPos, targetCommand, CommandBlockEntity.Mode.REDSTONE, false, false, false);
                mc.player.connection.send((Packet)writePacket);
                Thread.sleep(15L);
                ServerboundSetCommandBlockPacket activatePacket = new ServerboundSetCommandBlockPacket(finalGhostPos, targetCommand, CommandBlockEntity.Mode.AUTO, false, false, true);
                mc.player.connection.send((Packet)activatePacket);
                Thread.sleep(40L);
                String destroyCoreCmd = String.format("setblock %d %d %d minecraft:air destroy", finalGhostPos.getX(), finalGhostPos.getY(), finalGhostPos.getZ());
                mc.player.connection.send((Packet)new ServerboundChatCommandPacket(destroyCoreCmd));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

