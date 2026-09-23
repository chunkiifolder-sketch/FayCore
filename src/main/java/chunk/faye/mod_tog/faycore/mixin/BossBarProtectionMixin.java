package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.config.CrashProtectionConfig;
import chunk.faye.mod_tog.faycore.protection.SafeComponent;
import chunk.faye.mod_tog.faycore.tracker.BossBarTracker;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket.Handler;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public class BossBarProtectionMixin {
   @Inject(
      method = {"handleBossUpdate"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void faycore$protectBossBar(ClientboundBossEventPacket packet, final CallbackInfo ci) {
      if (CrashProtectionConfig.enableBossBarLimit) {
         try {
            packet.dispatch(
               new Handler() {
                  {
                     Objects.requireNonNull(BossBarProtectionMixin.this);
                  }

                  public void add(
                     UUID uuid,
                     Component name,
                     float progress,
                     BossBarColor color,
                     BossBarOverlay overlay,
                     boolean darkenScreen,
                     boolean playMusic,
                     boolean createWorldFog
                  ) {
                     String text = SafeComponent.getString(name);
                     if (text.length() > CrashProtectionConfig.maxBossBarNameLength) {
                        ci.cancel();
                     } else {
                        if (!BossBarTracker.add(uuid)) {
                           ci.cancel();
                        }
                     }
                  }

                  public void remove(UUID uuid) {
                     BossBarTracker.remove(uuid);
                  }

                  public void updateProgress(UUID uuid, float progress) {
                  }

                  public void updateName(UUID uuid, Component name) {
                     String text = SafeComponent.getString(name);
                     if (text.length() > CrashProtectionConfig.maxBossBarNameLength) {
                        ci.cancel();
                     }
                  }

                  public void updateStyle(UUID uuid, BossBarColor color, BossBarOverlay overlay) {
                  }

                  public void updateProperties(UUID uuid, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
                  }
               }
            );
         } catch (Throwable var4) {
            ci.cancel();
            if (CrashProtectionConfig.debugLog) {
               var4.printStackTrace();
            }
         }
      }
   }
}
