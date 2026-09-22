package net.mcreator.faycore.network;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.SectionPos;

import net.mcreator.faycore.procedures.OpenFaycoreMsgguiOnKeyPressedProcedure;
import net.mcreator.faycore.FaycoreMod;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public record OpenFaycoreMsgguiMessage(int eventType, int pressedms) implements CustomPacketPayload {
	public static final Type<OpenFaycoreMsgguiMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(FaycoreMod.MODID, "key_open_faycore_msggui"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenFaycoreMsgguiMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, OpenFaycoreMsgguiMessage message) -> {
		buffer.writeInt(message.eventType);
		buffer.writeInt(message.pressedms);
	}, (RegistryFriendlyByteBuf buffer) -> new OpenFaycoreMsgguiMessage(buffer.readInt(), buffer.readInt()));

	@Override
	public Type<OpenFaycoreMsgguiMessage> type() {
		return TYPE;
	}

	public static void handleData(final OpenFaycoreMsgguiMessage message, final ServerPlayNetworking.Context context) {
		context.server().execute(() -> {
			pressAction(context.player(), message.eventType, message.pressedms);
		});
	}

	public static void pressAction(Player entity, int type, int pressedms) {
		Level world = entity.level();
		double x = entity.getX();
		double y = entity.getY();
		double z = entity.getZ();
		// security measure to prevent arbitrary chunk generation
		if (!world.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z)))
			return;
		if (type == 0) {
			OpenFaycoreMsgguiOnKeyPressedProcedure.execute();
		}
	}
}