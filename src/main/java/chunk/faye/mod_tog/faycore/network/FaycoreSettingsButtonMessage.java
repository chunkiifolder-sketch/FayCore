package chunk.faye.mod_tog.faycore.network;

import chunk.faye.mod_tog.faycore.procedures.SetCorePositionProcedure;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.core.SectionPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public record FaycoreSettingsButtonMessage(int buttonID, int x, int y, int z) implements CustomPacketPayload {
   public static final Type<FaycoreSettingsButtonMessage> TYPE = new Type(Identifier.fromNamespaceAndPath("faycore", "faycore_settings_buttons"));
   public static final StreamCodec<RegistryFriendlyByteBuf, FaycoreSettingsButtonMessage> STREAM_CODEC = StreamCodec.of((buffer, message) -> {
      buffer.writeInt(message.buttonID);
      buffer.writeInt(message.x);
      buffer.writeInt(message.y);
      buffer.writeInt(message.z);
   }, buffer -> new FaycoreSettingsButtonMessage(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt()));

   public Type<FaycoreSettingsButtonMessage> type() {
      return TYPE;
   }

   public static void handleData(FaycoreSettingsButtonMessage message, Context context) {
      context.server().execute(() -> handleButtonAction(context.player(), message.buttonID, message.x, message.y, message.z));
   }

   public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z) {
      Level world = entity.level();
      if (world.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z))) {
         if (buttonID == 0) {
            SetCorePositionProcedure.execute(world, entity);
         }
      }
   }
}
