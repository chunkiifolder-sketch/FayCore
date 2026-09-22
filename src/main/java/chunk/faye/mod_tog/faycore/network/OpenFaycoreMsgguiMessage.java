/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking$Context
 *  net.minecraft.core.SectionPos
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 */
package chunk.faye.mod_tog.faycore.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import chunk.faye.mod_tog.faycore.procedures.OpenFaycoreMsgguiOnKeyPressedProcedure;
import net.minecraft.core.SectionPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public record OpenFaycoreMsgguiMessage(int eventType, int pressedms) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<OpenFaycoreMsgguiMessage> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"faycore", (String)"key_open_faycore_msggui"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenFaycoreMsgguiMessage> STREAM_CODEC = StreamCodec.of((buffer, message) -> {
        buffer.writeInt(message.eventType);
        buffer.writeInt(message.pressedms);
    }, buffer -> new OpenFaycoreMsgguiMessage(buffer.readInt(), buffer.readInt()));

    public CustomPacketPayload.Type<OpenFaycoreMsgguiMessage> type() {
        return TYPE;
    }

    public static void handleData(OpenFaycoreMsgguiMessage message, ServerPlayNetworking.Context context) {
        context.server().execute(() -> OpenFaycoreMsgguiMessage.pressAction((Player)context.player(), message.eventType, message.pressedms));
    }

    public static void pressAction(Player entity, int type, int pressedms) {
        Level world = entity.level();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        if (!world.getChunkSource().hasChunk(SectionPos.blockToSectionCoord((double)x), SectionPos.blockToSectionCoord((double)z))) {
            return;
        }
        if (type == 0) {
            OpenFaycoreMsgguiOnKeyPressedProcedure.execute();
        }
    }
}

