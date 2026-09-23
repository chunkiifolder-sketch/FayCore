package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.protection.ComponentLimiter;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public class ItemComponentProtectionMixin {
   @Inject(
      method = {"handleContainerSetSlot"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void faycore$protectSlot(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
      if (!ComponentLimiter.safe(packet.getItem())) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"handleContainerContent"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void faycore$protectContent(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
      for (ItemStack stack : packet.items()) {
         if (!ComponentLimiter.safe(stack)) {
            ci.cancel();
            return;
         }
      }
   }
}
