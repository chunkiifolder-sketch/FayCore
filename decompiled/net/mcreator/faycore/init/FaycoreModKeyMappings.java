/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.entity.player.Player
 */
package net.mcreator.faycore.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mcreator.faycore.network.OpenFaycoreMsgguiMessage;
import net.mcreator.faycore.network.OpenFaycoreSettingsMessage;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

@Environment(value=EnvType.CLIENT)
public class FaycoreModKeyMappings {
    public static final KeyMapping OPEN_FAYCORE_MSGGUI = new KeyMapping("key.faycore.open_faycore_msggui", 66, KeyMapping.Category.MOVEMENT){
        private boolean isDownOld = false;

        public void setDown(boolean isDown) {
            super.setDown(isDown);
            if (this.isDownOld != isDown && isDown) {
                ClientPlayNetworking.send((CustomPacketPayload)new OpenFaycoreMsgguiMessage(0, 0));
                OpenFaycoreMsgguiMessage.pressAction((Player)Minecraft.getInstance().player, 0, 0);
            }
            this.isDownOld = isDown;
        }
    };
    public static final KeyMapping OPEN_FAYCORE_SETTINGS = new KeyMapping("key.faycore.open_faycore_settings", 321, KeyMapping.Category.MOVEMENT){
        private boolean isDownOld = false;

        public void setDown(boolean isDown) {
            super.setDown(isDown);
            if (this.isDownOld != isDown && isDown) {
                ClientPlayNetworking.send((CustomPacketPayload)new OpenFaycoreSettingsMessage(0, 0));
                OpenFaycoreSettingsMessage.pressAction((Player)Minecraft.getInstance().player, 0, 0);
            }
            this.isDownOld = isDown;
        }
    };

    public static void clientLoad() {
        KeyMappingHelper.registerKeyMapping((KeyMapping)OPEN_FAYCORE_MSGGUI);
        KeyMappingHelper.registerKeyMapping((KeyMapping)OPEN_FAYCORE_SETTINGS);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.screen == null) {
                OPEN_FAYCORE_MSGGUI.consumeClick();
                OPEN_FAYCORE_SETTINGS.consumeClick();
            }
        });
    }
}

