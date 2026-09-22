/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DynamicOps
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtOps
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 *  net.minecraft.world.item.component.ItemLore
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.CommandBlockEntity$Mode
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 */
package chunk.faye.mod_tog.faycore.client.gui;

import com.mojang.serialization.DynamicOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class FayCoreFastRun {
    public static boolean IsLoop = false;
    public static int Range = 6;
    private static final List<BlockPos> recentPositions = new ArrayList<BlockPos>();

    public static void fireGhostPayloadQueue(ArrayList<String> commandQueue) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.player.connection == null) {
            return;
        }
        BlockPos playerPos = mc.player.blockPosition();
        Random random = new Random();
        if (Minecraft.getInstance().player != null && !Minecraft.getInstance().player.isCreative()) {
            Minecraft.getInstance().player.playSound((SoundEvent)SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 0.0f);
            Minecraft.getInstance().player.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
            Minecraft.getInstance().player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a7cErrror: Request creative!"));
            Minecraft.getInstance().setScreen(null);
            return;
        }
        new Thread(() -> {
            try {
                for (int i = 0; i < commandQueue.size(); ++i) {
                    String currentTargetCommand = (String)commandQueue.get(i);
                    if (currentTargetCommand == null || currentTargetCommand.trim().isEmpty()) continue;
                    ItemStack commandBlockItem = new ItemStack((ItemLike)Blocks.COMMAND_BLOCK, 1);
                    CompoundTag blockEntityNbt = new CompoundTag();
                    blockEntityNbt.putString("id", "minecraft:command_block");
                    blockEntityNbt.putString("Command", currentTargetCommand);
                    blockEntityNbt.putByte("auto", (byte)1);
                    DataComponents.BLOCK_ENTITY_DATA.codec().parse((DynamicOps)NbtOps.INSTANCE, (Object)blockEntityNbt).result().ifPresent(structuralObject -> commandBlockItem.set(DataComponents.BLOCK_ENTITY_DATA, structuralObject));
                    CompoundTag backupTag = new CompoundTag();
                    backupTag.put("BlockEntityTag", (Tag)blockEntityNbt);
                    commandBlockItem.set(DataComponents.CUSTOM_DATA, (Object)CustomData.of((CompoundTag)backupTag));
                    commandBlockItem.set(DataComponents.CUSTOM_NAME, (Object)Component.literal((String)"\u00a79FayCore").withStyle(style -> style.withItalic(Boolean.valueOf(false))));
                    commandBlockItem.set(DataComponents.LORE, (Object)new ItemLore(List.of(Component.literal((String)"\ua730\u1d00\u028f\u1d04\u1d0f\u0280\u1d07").withStyle(style -> style.withColor(11599765).withItalic(Boolean.valueOf(false))), Component.literal((String)"\u00a77Made by \u00a7cFayeCruz").withStyle(style -> style.withItalic(Boolean.valueOf(false))))));
                    int currentSlotIndex = Minecraft.getInstance().player.getInventory().getSelectedSlot();
                    if (Minecraft.getInstance().player.connection != null) {
                        Minecraft.getInstance().player.connection.send((Packet)new ServerboundSetCreativeModeSlotPacket(36 + currentSlotIndex, commandBlockItem));
                    }
                    if (Minecraft.getInstance().player == null) {
                        return;
                    }
                    Minecraft.getInstance().player.getInventory().setItem(currentSlotIndex, commandBlockItem);
                    Minecraft.getInstance().player.getInventory().setChanged();
                    int finalRange = Range;
                    BlockPos basePlayerPos = playerPos;
                    BlockPos chosenPos = CompletableFuture.supplyAsync(() -> {
                        BlockPos sampledPos = null;
                        int maxRadiusSq = finalRange * finalRange;
                        for (int attempt = 0; attempt < 30; ++attempt) {
                            BlockPos checkPos;
                            int randomX = random.nextInt(finalRange * 2 + 1) - finalRange;
                            int randomZ = random.nextInt(finalRange * 2 + 1) - finalRange;
                            int randomY = random.nextInt(finalRange) + 1;
                            if (randomX * randomX + randomZ * randomZ > maxRadiusSq || !mc.level.getBlockState(checkPos = basePlayerPos.offset(randomX, randomY, randomZ)).isAir() || recentPositions.contains(checkPos)) continue;
                            sampledPos = checkPos;
                            break;
                        }
                        return sampledPos;
                    }, (Executor)mc).join();
                    boolean needForceClear = false;
                    if (chosenPos == null) {
                        chosenPos = playerPos.above(2);
                        needForceClear = true;
                    }
                    BlockPos finalGhostPos = chosenPos;
                    recentPositions.add(finalGhostPos);
                    if (recentPositions.size() > 6) {
                        recentPositions.remove(0);
                    }
                    Vec3 hitVec = new Vec3((double)finalGhostPos.getX() + 0.5, (double)finalGhostPos.getY(), (double)finalGhostPos.getZ() + 0.5);
                    BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, finalGhostPos, false);
                    ServerboundUseItemOnPacket placeBlockPacket = new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hitResult, 0);
                    mc.player.connection.send((Packet)placeBlockPacket);
                    Thread.sleep(25L);
                    mc.execute(() -> {
                        if (mc.player != null) {
                            mc.player.playSound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 2.0f);
                        }
                    });
                    Thread.sleep(80L);
                    String destroyCoreCmd = String.format("setblock %d %d %d minecraft:air destroy", finalGhostPos.getX(), finalGhostPos.getY(), finalGhostPos.getZ());
                    ServerboundSetCommandBlockPacket destroyCoreCmdPacket = new ServerboundSetCommandBlockPacket(finalGhostPos, destroyCoreCmd, CommandBlockEntity.Mode.REDSTONE, false, false, false);
                    ServerboundSetCommandBlockPacket destroyCoreCmdPacket2 = new ServerboundSetCommandBlockPacket(finalGhostPos, destroyCoreCmd, CommandBlockEntity.Mode.REDSTONE, false, false, true);
                    mc.player.connection.send((Packet)destroyCoreCmdPacket);
                    Thread.sleep(10L);
                    mc.player.connection.send((Packet)destroyCoreCmdPacket2);
                    Thread.sleep(30L);
                }
                if (IsLoop) {
                    Thread.sleep(60L);
                    FayCoreFastRun.fireGhostPayloadQueue(new ArrayList<String>(commandQueue));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            ItemStack Air = new ItemStack((ItemLike)Blocks.AIR, 1);
            int currentSlotIndex = Minecraft.getInstance().player.getInventory().getSelectedSlot();
            if (Minecraft.getInstance().player.connection != null) {
                Minecraft.getInstance().player.connection.send((Packet)new ServerboundSetCreativeModeSlotPacket(36 + currentSlotIndex, Air));
            }
            if (Minecraft.getInstance().player == null) {
                return;
            }
            Minecraft.getInstance().player.getInventory().setItem(currentSlotIndex, Air);
            Minecraft.getInstance().player.getInventory().setChanged();
        }).start();
    }

    public static void spawnAndDestroyGhostCore(String targetCommand) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.player.connection == null) {
            return;
        }
        BlockPos playerPos = mc.player.blockPosition();
        CompletableFuture futurePos = new CompletableFuture();
        mc.execute(() -> {
            BlockPos ghostPos = playerPos.above(3);
            if (!mc.level.getBlockState(ghostPos).isAir() && !mc.level.getBlockState(ghostPos = playerPos.below(2)).isAir()) {
                futurePos.complete(null);
                return;
            }
            futurePos.complete(ghostPos);
        });
        BlockPos finalGhostPos = (BlockPos)futurePos.join();
        if (finalGhostPos == null) {
            return;
        }
        if (Minecraft.getInstance().player != null && !Minecraft.getInstance().player.isCreative()) {
            Minecraft.getInstance().player.playSound((SoundEvent)SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 0.0f);
            Minecraft.getInstance().player.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
            Minecraft.getInstance().player.sendSystemMessage((Component)Component.literal((String)"\u00a79[FayCore] \u00a7cErrror: Request creative!"));
            Minecraft.getInstance().setScreen(null);
            return;
        }
        new Thread(() -> {
            try {
                Vec3 hitVec = new Vec3((double)finalGhostPos.getX() + 0.5, (double)finalGhostPos.getY(), (double)finalGhostPos.getZ() + 0.5);
                BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, finalGhostPos, false);
                ServerboundUseItemOnPacket placeBlockPacket = new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hitResult, 0);
                mc.player.connection.send((Packet)placeBlockPacket);
                Thread.sleep(25L);
                mc.execute(() -> {
                    if (mc.player != null) {
                        mc.player.playSound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 2.0f);
                    }
                });
                ServerboundSetCommandBlockPacket writePacket = new ServerboundSetCommandBlockPacket(finalGhostPos, targetCommand, CommandBlockEntity.Mode.REDSTONE, false, false, false);
                ServerboundSetCommandBlockPacket activatePacket = new ServerboundSetCommandBlockPacket(finalGhostPos, targetCommand, CommandBlockEntity.Mode.REDSTONE, false, false, true);
                mc.player.connection.send((Packet)writePacket);
                Thread.sleep(20L);
                mc.player.connection.send((Packet)activatePacket);
                Thread.sleep(80L);
                String destroyCoreCmd = String.format("setblock %d %d %d minecraft:air destroy", finalGhostPos.getX(), finalGhostPos.getY(), finalGhostPos.getZ());
                ServerboundSetCommandBlockPacket destroyCoreCmdPacket = new ServerboundSetCommandBlockPacket(finalGhostPos, destroyCoreCmd, CommandBlockEntity.Mode.REDSTONE, false, false, false);
                ServerboundSetCommandBlockPacket destroyCoreCmdPacket2 = new ServerboundSetCommandBlockPacket(finalGhostPos, destroyCoreCmd, CommandBlockEntity.Mode.REDSTONE, false, false, true);
                mc.player.connection.send((Packet)destroyCoreCmdPacket);
                Thread.sleep(10L);
                mc.player.connection.send((Packet)destroyCoreCmdPacket2);
                Thread.sleep(20L);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            ItemStack ghostCoreStack = new ItemStack((ItemLike)Blocks.AIR, 1);
            int slot = 0;
            if (mc.player != null) {
                slot = mc.player.getInventory().getSelectedSlot();
            }
            int finalSlot = slot;
            mc.player.connection.send((Packet)new ServerboundSetCreativeModeSlotPacket(36 + finalSlot, ghostCoreStack));
            mc.execute(() -> {
                if (mc.player != null) {
                    mc.player.getInventory().setItem(finalSlot, ghostCoreStack);
                    mc.player.containerMenu.broadcastChanges();
                }
            });
        }).start();
    }
}

