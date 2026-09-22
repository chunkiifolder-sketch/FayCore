/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
 *  net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
 */
package chunk.faye.mod_tog.faycore.init;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import chunk.faye.mod_tog.faycore.network.OpenFaycoreMsgguiMessage;
import chunk.faye.mod_tog.faycore.network.OpenFaycoreSettingsMessage;

public class FaycoreModKeyMappingsServer {
    public static void serverLoad() {
        PayloadTypeRegistry.serverboundPlay().register(OpenFaycoreMsgguiMessage.TYPE, OpenFaycoreMsgguiMessage.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(OpenFaycoreMsgguiMessage.TYPE, OpenFaycoreMsgguiMessage::handleData);
        PayloadTypeRegistry.serverboundPlay().register(OpenFaycoreSettingsMessage.TYPE, OpenFaycoreSettingsMessage.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(OpenFaycoreSettingsMessage.TYPE, OpenFaycoreSettingsMessage::handleData);
    }
}

