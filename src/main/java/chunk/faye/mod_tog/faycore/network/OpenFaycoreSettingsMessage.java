package chunk.faye.mod_tog.faycore.network;

import chunk.faye.mod_tog.faycore.procedures.OpenFaycoresettings2Procedure;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.core.SectionPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public record OpenFaycoreSettingsMessage(int eventType, int pressedms) implements CustomPacketPayload {
   public static final Type<OpenFaycoreSettingsMessage> TYPE = new Type(Identifier.fromNamespaceAndPath("faycore", "key_open_faycore_settings"));
   public static final StreamCodec<RegistryFriendlyByteBuf, OpenFaycoreSettingsMessage> STREAM_CODEC = StreamCodec.of((buffer, message) -> {
      buffer.writeInt(message.eventType);
      buffer.writeInt(message.pressedms);
   }, buffer -> new OpenFaycoreSettingsMessage(buffer.readInt(), buffer.readInt()));

   public Type<OpenFaycoreSettingsMessage> type() {
      return TYPE;
   }

   public static void handleData(OpenFaycoreSettingsMessage message, Context context) {
      context.server().execute(() -> pressAction(context.player(), message.eventType, message.pressedms));
   }

   public static void pressAction(Player entity, int type, int pressedms) {
      Level world = entity.level();
      double x = entity.getX();
      double y = entity.getY();
      double z = entity.getZ();
      if (world.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z))) {
         if (type == 0) {
            OpenFaycoresettings2Procedure.execute(world, x, y, z, entity);
         }
      }
   }
}
