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
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 */
package net.mcreator.faycore.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mcreator.faycore.procedures.SetCorePositionProcedure;
import net.minecraft.core.SectionPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public record FaycoreSettingsButtonMessage(int buttonID, int x, int y, int z) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<FaycoreSettingsButtonMessage> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"faycore", (String)"faycore_settings_buttons"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FaycoreSettingsButtonMessage> STREAM_CODEC = StreamCodec.of((buffer, message) -> {
        buffer.writeInt(message.buttonID);
        buffer.writeInt(message.x);
        buffer.writeInt(message.y);
        buffer.writeInt(message.z);
    }, buffer -> new FaycoreSettingsButtonMessage(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt()));

    public CustomPacketPayload.Type<FaycoreSettingsButtonMessage> type() {
        return TYPE;
    }

    public static void handleData(FaycoreSettingsButtonMessage message, ServerPlayNetworking.Context context) {
        context.server().execute(() -> FaycoreSettingsButtonMessage.handleButtonAction((Player)context.player(), message.buttonID, message.x, message.y, message.z));
    }

    public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z) {
        Level world = entity.level();
        if (!world.getChunkSource().hasChunk(SectionPos.blockToSectionCoord((int)x), SectionPos.blockToSectionCoord((int)z))) {
            return;
        }
        if (buttonID == 0) {
            SetCorePositionProcedure.execute((LevelAccessor)world, (Entity)entity);
        }
    }
}

