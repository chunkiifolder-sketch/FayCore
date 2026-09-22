/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.faycore.init;

import net.mcreator.faycore.network.OpenFaycoreSettingsMessage;
import net.mcreator.faycore.network.OpenFaycoreMsgguiMessage;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class FaycoreModKeyMappingsServer {
	public static void serverLoad() {
		PayloadTypeRegistry.serverboundPlay().register(OpenFaycoreMsgguiMessage.TYPE, OpenFaycoreMsgguiMessage.STREAM_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(OpenFaycoreMsgguiMessage.TYPE, OpenFaycoreMsgguiMessage::handleData);
		PayloadTypeRegistry.serverboundPlay().register(OpenFaycoreSettingsMessage.TYPE, OpenFaycoreSettingsMessage.STREAM_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(OpenFaycoreSettingsMessage.TYPE, OpenFaycoreSettingsMessage::handleData);
	}
}