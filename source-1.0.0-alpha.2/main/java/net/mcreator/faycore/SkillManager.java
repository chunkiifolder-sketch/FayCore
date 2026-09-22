package net.mcreator.faycore;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import java.util.List;

public class SkillManager {

    private static final String EMPTY_SLOT = "execute as %player% at @s run tellraw @s {\"text\":\"此處空槽\",\"color\":\"red\"}";

    public static void castPressSkill(int slot) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null) return;

        switch (slot) {
            case 0 -> {
                String cmd1 = parseDynamicVariables("execute as %player% at @s run particle minecraft:cloud ~ ~ ~ 0 0 0 0.1 25 force @a");
                String cmd2 = parseDynamicVariables("execute as %player% at @s run particle minecraft:trial_spawner_detection ~ ~ ~ 0.5 0.5 0.5 0 25 force @a");
                String cmd3 = parseDynamicVariables("execute as %player% at @s run effect give @s minecraft:levitation 1 25 true");
                net.mcreator.faycore.client.gui.FayCoreMacroEngine.executeVPressCommand(mc, cmd1);
                net.mcreator.faycore.client.gui.FayCoreMacroEngine.executeVPressCommand(mc, cmd2);
                net.mcreator.faycore.client.gui.FayCoreMacroEngine.executeVPressCommand(mc, cmd3);

                SkillInput.isVKeyWaitingForPhysicalRelease = true;

                java.util.concurrent.CompletableFuture.delayedExecutor(150, java.util.concurrent.TimeUnit.MILLISECONDS)
                        .execute(() -> mc.execute(() -> {
                            if (mc.player != null && mc.level != null) {
                                String releaseCmd = parseDynamicVariables("execute as %player% at @s run effect clear @s minecraft:levitation");
                                net.mcreator.faycore.client.gui.FayCoreMacroEngine.executeVReleaseCommand(mc, releaseCmd);

                                mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                                        net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_THUNDER, net.minecraft.sounds.SoundSource.PLAYERS,
                                        0.6F, 1.0F, false);

                                SkillInput.isVKeyCurrentlyHolding = false;
                                SkillInput.vTickTimer = 0;
                            }
                        }));
            }
            case 1, 2, 3, 4 -> executeSingleCommandList(mc, List.of(EMPTY_SLOT), true);
            default -> executeSingleCommandList(mc, List.of("faycore_pressed"), true);
        }
    }

    public static void castHoldingSkill(int slot) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        switch (slot) {
            case 0 -> { /* 此處無技能指令 */ }
            case 1, 2, 3, 4 -> executeSingleCommandList(mc, List.of(EMPTY_SLOT), false);
            default -> executeSingleCommandList(mc, List.of("faycore_holding"), false);
        }
    }

    // 🛑 3. 處理【手動放開】的技能效果
    public static void castReleaseSkill(int slot) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        switch (slot) {
            case 0 -> { /* 此處無技能指令 */ }
            case 1, 2, 3, 4 -> executeSingleCommandList(mc, List.of(EMPTY_SLOT), false);
            default -> executeSingleCommandList(mc, List.of("execute as %player% at @s run tellraw @s {\"text\":\"faycore_released\",\"color\":\"gray\"}"), false);
        }
    }

    // 輔助方法：執行普通的常數指令列表
    private static void executeSingleCommandList(Minecraft mc, List<String> commands, boolean isPress) {
        for (String cmd : commands) {
            String parsed = parseDynamicVariables(cmd);
            if (isPress) {
                net.mcreator.faycore.client.gui.FayCoreMacroEngine.executeVPressCommand(mc, parsed);
            } else {
                net.mcreator.faycore.client.gui.FayCoreMacroEngine.executeVReleaseCommand(mc, parsed);
            }
        }
    }

    public static String parseDynamicVariables(String originalCommand) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            int px = (int)Math.floor(mc.player.getX());
            int py = (int)Math.floor(mc.player.getY());
            int pz = (int)Math.floor(mc.player.getZ());
            String parsed = originalCommand.replace("%pos-x%", String.valueOf(px));
            parsed = parsed.replace("%pos-y%", String.valueOf(py));
            parsed = parsed.replace("%pos-z%", String.valueOf(pz));
            parsed = parsed.replace("%player%", mc.player.getScoreboardName());

            Direction dir = mc.player.getDirection();
            parsed = parsed.replace("%direction%", dir.getName().toLowerCase());

            HitResult manualHit = mc.player.pick(300.0D, 1.0F, false);
            if (manualHit != null && manualHit.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult)manualHit;
                BlockPos lookPos = blockHit.getBlockPos();
                parsed = parsed.replace("%look-x%", String.valueOf(lookPos.getX()));
                parsed = parsed.replace("%look-y%", String.valueOf(lookPos.getY()));
                parsed = parsed.replace("%look-z%", String.valueOf(lookPos.getZ()));
            }
            return parsed;
        }
        return originalCommand;
    }
}
