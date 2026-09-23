package chunk.faye.mod_tog.faycore.network;

import chunk.faye.mod_tog.faycore.procedures.OpenFaycoreMsgguiOnKeyPressedProcedure;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.core.SectionPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public record OpenFaycoreMsgguiMessage(int eventType, int pressedms) implements CustomPacketPayload {
   public static final Type<OpenFaycoreMsgguiMessage> TYPE = new Type(Identifier.fromNamespaceAndPath("faycore", "key_open_faycore_msggui"));
   public static final StreamCodec<RegistryFriendlyByteBuf, OpenFaycoreMsgguiMessage> STREAM_CODEC = StreamCodec.of((buffer, message) -> {
      buffer.writeInt(message.eventType);
      buffer.writeInt(message.pressedms);
   }, buffer -> new OpenFaycoreMsgguiMessage(buffer.readInt(), buffer.readInt()));

   public Type<OpenFaycoreMsgguiMessage> type() {
      return TYPE;
   }

   public static void handleData(OpenFaycoreMsgguiMessage message, Context context) {
      context.server().execute(() -> pressAction(context.player(), message.eventType, message.pressedms));
   }

   public static void pressAction(Player entity, int type, int pressedms) {
      Level world = entity.level();
      double x = entity.getX();
      double y = entity.getY();
      double z = entity.getZ();
      if (world.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z))) {
         if (type == 0) {
            OpenFaycoreMsgguiOnKeyPressedProcedure.execute();
         }
      }
   }
}
