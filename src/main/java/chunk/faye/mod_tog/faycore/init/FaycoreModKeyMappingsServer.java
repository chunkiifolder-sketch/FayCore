package chunk.faye.mod_tog.faycore.init;

import chunk.faye.mod_tog.faycore.network.OpenFaycoreMsgguiMessage;
import chunk.faye.mod_tog.faycore.network.OpenFaycoreSettingsMessage;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class FaycoreModKeyMappingsServer {
   public static void serverLoad() {
      PayloadTypeRegistry.serverboundPlay().register(OpenFaycoreMsgguiMessage.TYPE, OpenFaycoreMsgguiMessage.STREAM_CODEC);
      ServerPlayNetworking.registerGlobalReceiver(OpenFaycoreMsgguiMessage.TYPE, OpenFaycoreMsgguiMessage::handleData);
      PayloadTypeRegistry.serverboundPlay().register(OpenFaycoreSettingsMessage.TYPE, OpenFaycoreSettingsMessage.STREAM_CODEC);
      ServerPlayNetworking.registerGlobalReceiver(OpenFaycoreSettingsMessage.TYPE, OpenFaycoreSettingsMessage::handleData);
   }
}
