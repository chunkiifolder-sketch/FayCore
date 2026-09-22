/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.protocol.game.ClientboundAddEntityPacket
 *  net.minecraft.world.entity.EntityType
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.config.CrashProtectionConfig;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public class ArmorStandProtectionMixin {
    private static int faycore$armorStandCount = 0;

    @Inject(method={"handleAddEntity"}, at={@At(value="HEAD")}, cancellable=true)
    private void faycore$blockArmorStandSpam(ClientboundAddEntityPacket packet, CallbackInfo ci) {
        if (!CrashProtectionConfig.enableEntityLimit) {
            return;
        }
        try {
            if (packet.getType() == EntityType.ARMOR_STAND && ++faycore$armorStandCount > CrashProtectionConfig.maxArmorStands) {
                ci.cancel();
            }
        }
        catch (Throwable e) {
            ci.cancel();
        }
    }
}

