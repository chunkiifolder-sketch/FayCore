package net.mcreator.faycore.network;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.SectionPos;

import net.mcreator.faycore.procedures.OpenFaycoresettings2Procedure;
import net.mcreator.faycore.FaycoreMod;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public record OpenFaycoreSettingsMessage(int eventType, int pressedms) implements CustomPacketPayload {
	public static final Type<OpenFaycoreSettingsMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(FaycoreMod.MODID, "key_open_faycore_settings"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenFaycoreSettingsMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, OpenFaycoreSettingsMessage message) -> {
		buffer.writeInt(message.eventType);
		buffer.writeInt(message.pressedms);
	}, (RegistryFriendlyByteBuf buffer) -> new OpenFaycoreSettingsMessage(buffer.readInt(), buffer.readInt()));

	@Override
	public Type<OpenFaycoreSettingsMessage> type() {
		return TYPE;
	}

	public static void handleData(final OpenFaycoreSettingsMessage message, final ServerPlayNetworking.Context context) {
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
			OpenFaycoresettings2Procedure.execute(world, x, y, z, entity);
		}
	}
}