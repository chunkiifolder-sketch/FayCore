/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.reflect.TypeToken
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket
 *  net.minecraft.world.level.block.entity.CommandBlockEntity$Mode
 */
package chunk.faye.mod_tog.faycore.client.gui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import chunk.faye.mod_tog.faycore.client.gui.FaycoreSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.world.level.block.entity.CommandBlockEntity;

public class FayCoreRepeatCommandManager {
    private static final File CONFIG_FILE = new File(Minecraft.getInstance().gameDirectory, "config/faycore_commands.json");
    private static final Gson GSON = new Gson();
    private static final Gson FAYCORE_INTERNAL_GSON = new Gson();

    public static void autoFillCommandsViaPacket() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.player.connection == null) {
            return;
        }
        if (FaycoreSettingsScreen.RepeatingCorePosA == null || FaycoreSettingsScreen.RepeatingCorePosB == null) {
            return;
        }
        List<String> myCommands = FayCoreRepeatCommandManager.getInternalSavedCommands();
        int Max_RCy = Math.max(FaycoreSettingsScreen.RepeatingCorePosA.getY(), FaycoreSettingsScreen.RepeatingCorePosB.getY());
        int minX = Math.min(FaycoreSettingsScreen.RepeatingCorePosA.getX(), FaycoreSettingsScreen.RepeatingCorePosB.getX());
        int maxX = Math.max(FaycoreSettingsScreen.RepeatingCorePosA.getX(), FaycoreSettingsScreen.RepeatingCorePosB.getX());
        int minY = Math.min(FaycoreSettingsScreen.RepeatingCorePosA.getY(), FaycoreSettingsScreen.RepeatingCorePosB.getY());
        int maxY = Math.max(FaycoreSettingsScreen.RepeatingCorePosA.getY(), FaycoreSettingsScreen.RepeatingCorePosB.getY());
        int minZ = Math.min(FaycoreSettingsScreen.RepeatingCorePosA.getZ(), FaycoreSettingsScreen.RepeatingCorePosB.getZ());
        int maxZ = Math.max(FaycoreSettingsScreen.RepeatingCorePosA.getZ(), FaycoreSettingsScreen.RepeatingCorePosB.getZ());
        boolean totalCommandBlocksFound = false;
        int commandIndex = 0;
        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    if (commandIndex >= myCommands.size()) {
                        mc.player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a77Core filled!"));
                        return;
                    }
                    BlockPos targetPos = new BlockPos(x, y, z);
                    String blockName = mc.level.getBlockState(targetPos).getBlock().getDescriptionId();
                    if (!blockName.contains("repeating_command_block")) continue;
                    String finalCmd = myCommands.get(commandIndex);
                    ServerboundSetCommandBlockPacket setPacket = new ServerboundSetCommandBlockPacket(targetPos, finalCmd, CommandBlockEntity.Mode.AUTO, false, false, true);
                    mc.player.connection.send((Packet)setPacket);
                    ++commandIndex;
                    try {
                        Thread.sleep(5L);
                        continue;
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
        }
    }

    private static List<String> getInternalSavedCommands() {
        ArrayList<String> commands = new ArrayList<String>();
        File configFile = new File(Minecraft.getInstance().gameDirectory, "config/faycore_commands.json");
        if (!configFile.exists()) {
            return commands;
        }
        try (FileReader reader = new FileReader(configFile);){
            List loaded = (List)FAYCORE_INTERNAL_GSON.fromJson((Reader)reader, new TypeToken<List<String>>(){}.getType());
            if (loaded != null) {
                commands.addAll(loaded);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return commands;
    }
}

