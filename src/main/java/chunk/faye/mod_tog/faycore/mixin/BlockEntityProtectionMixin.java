package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.config.CrashProtectionConfig;
import chunk.faye.mod_tog.faycore.protection.NbtLimiter;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public class BlockEntityProtectionMixin {
   @Inject(
      method = {"handleBlockEntityData"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void faycore$protectBlockEntity(ClientboundBlockEntityDataPacket packet, CallbackInfo ci) {
      if (CrashProtectionConfig.enableNbtLimit) {
         try {
            CompoundTag tag = packet.getTag();
            if (tag != null && !NbtLimiter.safe(tag)) {
               ci.cancel();
            }
         } catch (Throwable var4) {
            ci.cancel();
         }
      }
   }
}
