package chunk.faye.mod_tog.faycore.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.CommandBlockEntity.Mode;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class FayCoreFastRun {
   public static boolean IsLoop = false;
   public static int Range = 6;
   private static final List<BlockPos> recentPositions = new ArrayList<>();

   public static void fireGhostPayloadQueue(ArrayList<String> commandQueue) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null && mc.player.connection != null) {
         BlockPos playerPos = mc.player.blockPosition();
         Random random = new Random();
         if (Minecraft.getInstance().player != null && !Minecraft.getInstance().player.isCreative()) {
            Minecraft.getInstance().player.playSound((SoundEvent)SoundEvents.NOTE_BLOCK_PLING.value(), 1.0F, 0.0F);
            Minecraft.getInstance().player.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("§9[FayCore] §cErrror: Request creative!"));
            Minecraft.getInstance().setScreen(null);
         } else {
            new Thread(
                  () -> {
                     try {
                        for (int i = 0; i < commandQueue.size(); i++) {
                           String currentTargetCommand = commandQueue.get(i);
                           if (currentTargetCommand != null && !currentTargetCommand.trim().isEmpty()) {
                              ItemStack commandBlockItem = new ItemStack(Blocks.COMMAND_BLOCK, 1);
                              CompoundTag blockEntityNbt = new CompoundTag();
                              blockEntityNbt.putString("id", "minecraft:command_block");
                              blockEntityNbt.putString("Command", currentTargetCommand);
                              blockEntityNbt.putByte("auto", (byte)1);
                              DataComponents.BLOCK_ENTITY_DATA
                                 .codec()
                                 .parse(NbtOps.INSTANCE, blockEntityNbt)
                                 .result()
                                 .ifPresent(structuralObject -> commandBlockItem.set(DataComponents.BLOCK_ENTITY_DATA, structuralObject));
                              CompoundTag backupTag = new CompoundTag();
                              backupTag.put("BlockEntityTag", blockEntityNbt);
                              commandBlockItem.set(DataComponents.CUSTOM_DATA, CustomData.of(backupTag));
                              commandBlockItem.set(DataComponents.CUSTOM_NAME, Component.literal("§9FayCore").withStyle(style -> style.withItalic(false)));
                              commandBlockItem.set(
                                 DataComponents.LORE,
                                 new ItemLore(
                                    List.of(
                                       Component.literal("ꜰᴀʏᴄᴏʀᴇ").withStyle(style -> style.withColor(11599765).withItalic(false)),
                                       Component.literal("§7Made by §cFayeCruz").withStyle(style -> style.withItalic(false))
                                    )
                                 )
                              );
                              int currentSlotIndex = Minecraft.getInstance().player.getInventory().getSelectedSlot();
                              if (Minecraft.getInstance().player.connection != null) {
                                 Minecraft.getInstance()
                                    .player
                                    .connection
                                    .send(new ServerboundSetCreativeModeSlotPacket(36 + currentSlotIndex, commandBlockItem));
                              }

                              if (Minecraft.getInstance().player == null) {
                                 return;
                              }

                              Minecraft.getInstance().player.getInventory().setItem(currentSlotIndex, commandBlockItem);
                              Minecraft.getInstance().player.getInventory().setChanged();
                              int finalRange = Range;
                              BlockPos chosenPos = CompletableFuture.<BlockPos>supplyAsync(() -> {
                                 BlockPos sampledPos = null;
                                 int maxRadiusSq = finalRange * finalRange;

                                 for (int attempt = 0; attempt < 30; attempt++) {
                                    int randomX = random.nextInt(finalRange * 2 + 1) - finalRange;
                                    int randomZ = random.nextInt(finalRange * 2 + 1) - finalRange;
                                    int randomY = random.nextInt(finalRange) + 1;
                                    if (randomX * randomX + randomZ * randomZ <= maxRadiusSq) {
                                       BlockPos checkPos = playerPos.offset(randomX, randomY, randomZ);
                                       if (mc.level.getBlockState(checkPos).isAir() && !recentPositions.contains(checkPos)) {
                                          sampledPos = checkPos;
                                          break;
                                       }
                                    }
                                 }

                                 return sampledPos;
                              }, mc).join();
                              boolean needForceClear = false;
                              if (chosenPos == null) {
                                 chosenPos = playerPos.above(2);
                                 needForceClear = true;
                              }

                              recentPositions.add(chosenPos);
                              if (recentPositions.size() > 6) {
                                 recentPositions.remove(0);
                              }

                              Vec3 hitVec = new Vec3((double)chosenPos.getX() + 0.5, (double)chosenPos.getY(), (double)chosenPos.getZ() + 0.5);
                              BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, chosenPos, false);
                              ServerboundUseItemOnPacket placeBlockPacket = new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hitResult, 0);
                              mc.player.connection.send(placeBlockPacket);
                              Thread.sleep(25L);
                              mc.execute(() -> {
                                 if (mc.player != null) {
                                    mc.player.playSound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 2.0F);
                                 }
                              });
                              Thread.sleep(80L);
                              String destroyCoreCmd = String.format(
                                 "setblock %d %d %d minecraft:air destroy", chosenPos.getX(), chosenPos.getY(), chosenPos.getZ()
                              );
                              ServerboundSetCommandBlockPacket destroyCoreCmdPacket = new ServerboundSetCommandBlockPacket(
                                 chosenPos, destroyCoreCmd, Mode.REDSTONE, false, false, false
                              );
                              ServerboundSetCommandBlockPacket destroyCoreCmdPacket2 = new ServerboundSetCommandBlockPacket(
                                 chosenPos, destroyCoreCmd, Mode.REDSTONE, false, false, true
                              );
                              mc.player.connection.send(destroyCoreCmdPacket);
                              Thread.sleep(10L);
                              mc.player.connection.send(destroyCoreCmdPacket2);
                              Thread.sleep(30L);
                           }
                        }

                        if (IsLoop) {
                           Thread.sleep(60L);
                           fireGhostPayloadQueue(new ArrayList<>(commandQueue));
                        }
                     } catch (Exception var21) {
                        var21.printStackTrace();
                     }

                     ItemStack Air = new ItemStack(Blocks.AIR, 1);
                     int currentSlotIndexx = Minecraft.getInstance().player.getInventory().getSelectedSlot();
                     if (Minecraft.getInstance().player.connection != null) {
                        Minecraft.getInstance().player.connection.send(new ServerboundSetCreativeModeSlotPacket(36 + currentSlotIndexx, Air));
                     }

                     if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.getInventory().setItem(currentSlotIndexx, Air);
                        Minecraft.getInstance().player.getInventory().setChanged();
                     }
                  }
               )
               .start();
         }
      }
   }

   public static void spawnAndDestroyGhostCore(String targetCommand) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null && mc.player.connection != null) {
         BlockPos playerPos = mc.player.blockPosition();
         CompletableFuture<BlockPos> futurePos = new CompletableFuture<>();
         mc.execute(() -> {
            BlockPos ghostPos = playerPos.above(3);
            if (!mc.level.getBlockState(ghostPos).isAir()) {
               ghostPos = playerPos.below(2);
               if (!mc.level.getBlockState(ghostPos).isAir()) {
                  futurePos.complete(null);
                  return;
               }
            }

            futurePos.complete(ghostPos);
         });
         BlockPos finalGhostPos = futurePos.join();
         if (finalGhostPos != null) {
            if (Minecraft.getInstance().player != null && !Minecraft.getInstance().player.isCreative()) {
               Minecraft.getInstance().player.playSound((SoundEvent)SoundEvents.NOTE_BLOCK_PLING.value(), 1.0F, 0.0F);
               Minecraft.getInstance().player.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
               Minecraft.getInstance().player.sendSystemMessage(Component.literal("§9[FayCore] §cErrror: Request creative!"));
               Minecraft.getInstance().setScreen(null);
            } else {
               new Thread(
                     () -> {
                        try {
                           Vec3 hitVec = new Vec3((double)finalGhostPos.getX() + 0.5, (double)finalGhostPos.getY(), (double)finalGhostPos.getZ() + 0.5);
                           BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, finalGhostPos, false);
                           ServerboundUseItemOnPacket placeBlockPacket = new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hitResult, 0);
                           mc.player.connection.send(placeBlockPacket);
                           Thread.sleep(25L);
                           mc.execute(() -> {
                              if (mc.player != null) {
                                 mc.player.playSound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 2.0F);
                              }
                           });
                           ServerboundSetCommandBlockPacket writePacket = new ServerboundSetCommandBlockPacket(
                              finalGhostPos, targetCommand, Mode.REDSTONE, false, false, false
                           );
                           ServerboundSetCommandBlockPacket activatePacket = new ServerboundSetCommandBlockPacket(
                              finalGhostPos, targetCommand, Mode.REDSTONE, false, false, true
                           );
                           mc.player.connection.send(writePacket);
                           Thread.sleep(20L);
                           mc.player.connection.send(activatePacket);
                           Thread.sleep(80L);
                           String destroyCoreCmd = String.format(
                              "setblock %d %d %d minecraft:air destroy", finalGhostPos.getX(), finalGhostPos.getY(), finalGhostPos.getZ()
                           );
                           ServerboundSetCommandBlockPacket destroyCoreCmdPacket = new ServerboundSetCommandBlockPacket(
                              finalGhostPos, destroyCoreCmd, Mode.REDSTONE, false, false, false
                           );
                           ServerboundSetCommandBlockPacket destroyCoreCmdPacket2 = new ServerboundSetCommandBlockPacket(
                              finalGhostPos, destroyCoreCmd, Mode.REDSTONE, false, false, true
                           );
                           mc.player.connection.send(destroyCoreCmdPacket);
                           Thread.sleep(10L);
                           mc.player.connection.send(destroyCoreCmdPacket2);
                           Thread.sleep(20L);
                        } catch (Exception var11) {
                           var11.printStackTrace();
                        }

                        ItemStack ghostCoreStack = new ItemStack(Blocks.AIR, 1);
                        int slot = 0;
                        if (mc.player != null) {
                           slot = mc.player.getInventory().getSelectedSlot();
                        }

                        int finalSlot = slot;
                        mc.player.connection.send(new ServerboundSetCreativeModeSlotPacket(36 + finalSlot, ghostCoreStack));
                        mc.execute(() -> {
                           if (mc.player != null) {
                              mc.player.getInventory().setItem(finalSlot, ghostCoreStack);
                              mc.player.containerMenu.broadcastChanges();
                           }
                        });
                     }
                  )
                  .start();
            }
         }
      }
   }
}
