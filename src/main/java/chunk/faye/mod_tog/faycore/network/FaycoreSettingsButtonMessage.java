package chunk.faye.mod_tog.faycore.network;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.SectionPos;

import chunk.faye.mod_tog.faycore.procedures.SetCorePositionProcedure;
import chunk.faye.mod_tog.faycore.FaycoreMod;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public record FaycoreSettingsButtonMessage(int buttonID, int x, int y, int z) implements CustomPacketPayload {
	public static final Type<FaycoreSettingsButtonMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(FaycoreMod.MODID, "faycore_settings_buttons"));
	public static final StreamCodec<RegistryFriendlyByteBuf, FaycoreSettingsButtonMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, FaycoreSettingsButtonMessage message) -> {
		buffer.writeInt(message.buttonID);
		buffer.writeInt(message.x);
		buffer.writeInt(message.y);
		buffer.writeInt(message.z);
	}, (RegistryFriendlyByteBuf buffer) -> new FaycoreSettingsButtonMessage(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt()));

	@Override
	public Type<FaycoreSettingsButtonMessage> type() {
		return TYPE;
	}

	public static void handleData(final FaycoreSettingsButtonMessage message, final ServerPlayNetworking.Context context) {
		context.server().execute(() -> handleButtonAction(context.player(), message.buttonID, message.x, message.y, message.z));
	}

	public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z) {
		Level world = entity.level();
		// security measure to prevent arbitrary chunk generation
		if (!world.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z)))
			return;
		if (buttonID == 0) {

			SetCorePositionProcedure.execute(world, entity);
		}
	}
}