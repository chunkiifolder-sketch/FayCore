/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.game.ClientboundBossEventPacket
 *  net.minecraft.network.protocol.game.ClientboundBossEventPacket$Handler
 *  net.minecraft.world.BossEvent$BossBarColor
 *  net.minecraft.world.BossEvent$BossBarOverlay
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.mcreator.faycore.mixin;

import java.util.Objects;
import java.util.UUID;
import net.mcreator.faycore.config.CrashProtectionConfig;
import net.mcreator.faycore.protection.SafeComponent;
import net.mcreator.faycore.tracker.BossBarTracker;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public class BossBarProtectionMixin {
    @Inject(method={"handleBossUpdate"}, at={@At(value="HEAD")}, cancellable=true)
    private void faycore$protectBossBar(ClientboundBossEventPacket packet, final CallbackInfo ci) {
        block3: {
            if (!CrashProtectionConfig.enableBossBarLimit) {
                return;
            }
            try {
                packet.dispatch(new ClientboundBossEventPacket.Handler(){
                    {
                        Objects.requireNonNull(this$0);
                    }

                    public void add(UUID uuid, Component name, float progress, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
                        String text = SafeComponent.getString(name);
                        if (text.length() > CrashProtectionConfig.maxBossBarNameLength) {
                            ci.cancel();
                            return;
                        }
                        if (!BossBarTracker.add(uuid)) {
                            ci.cancel();
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

                    public void updateStyle(UUID uuid, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
                    }

                    public void updateProperties(UUID uuid, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
                    }
                });
            }
            catch (Throwable throwable) {
                ci.cancel();
                if (!CrashProtectionConfig.debugLog) break block3;
                throwable.printStackTrace();
            }
        }
    }
}

