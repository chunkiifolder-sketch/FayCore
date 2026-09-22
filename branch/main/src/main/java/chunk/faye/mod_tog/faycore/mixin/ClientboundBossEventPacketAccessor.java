/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.game.ClientboundBossEventPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package chunk.faye.mod_tog.faycore.mixin;

import java.util.UUID;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ClientboundBossEventPacket.class})
public interface ClientboundBossEventPacketAccessor {
    @Accessor(value="id")
    public UUID faycore$getId();
}

