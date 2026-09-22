/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.faycore.init;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;

import net.mcreator.faycore.network.OpenFaycoreSettingsMessage;
import net.mcreator.faycore.network.OpenFaycoreMsgguiMessage;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
public class FaycoreModKeyMappings {
	public static final KeyMapping OPEN_FAYCORE_MSGGUI = new KeyMapping("key.faycore.open_faycore_msggui", GLFW.GLFW_KEY_B, KeyMapping.Category.MOVEMENT) {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				ClientPlayNetworking.send(new OpenFaycoreMsgguiMessage(0, 0));
				OpenFaycoreMsgguiMessage.pressAction(Minecraft.getInstance().player, 0, 0);
			}
			isDownOld = isDown;
		}
	};
	public static final KeyMapping OPEN_FAYCORE_SETTINGS = new KeyMapping("key.faycore.open_faycore_settings", GLFW.GLFW_KEY_KP_1, KeyMapping.Category.MOVEMENT) {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				ClientPlayNetworking.send(new OpenFaycoreSettingsMessage(0, 0));
				OpenFaycoreSettingsMessage.pressAction(Minecraft.getInstance().player, 0, 0);
			}
			isDownOld = isDown;
		}
	};

	public static void clientLoad() {
		KeyMappingHelper.registerKeyMapping(OPEN_FAYCORE_MSGGUI);
		KeyMappingHelper.registerKeyMapping(OPEN_FAYCORE_SETTINGS);
		ClientTickEvents.END_CLIENT_TICK.register((client) -> {
			if (client.screen == null) {
				OPEN_FAYCORE_MSGGUI.consumeClick();
				OPEN_FAYCORE_SETTINGS.consumeClick();
			}
		});
	}
}