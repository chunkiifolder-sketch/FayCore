/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
 *  net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
 *  net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.core.Registry
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.flag.FeatureFlags
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.MenuType$MenuSupplier
 *  net.minecraft.world.inventory.Slot
 */
package chunk.faye.mod_tog.faycore.init;

import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import chunk.faye.mod_tog.faycore.init.FaycoreModScreens;
import chunk.faye.mod_tog.faycore.network.MenuStateUpdateMessage;
import chunk.faye.mod_tog.faycore.world.inventory.FaycoreSettingsMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

public class FaycoreModMenus {
    public static MenuType<FaycoreSettingsMenu> FAYCORE_SETTINGS;

    public static void load() {
        FAYCORE_SETTINGS = FaycoreModMenus.register("faycore_settings", FaycoreSettingsMenu::new);
        FaycoreSettingsMenu.screenInit();
        PayloadTypeRegistry.serverboundPlay().register(MenuStateUpdateMessage.TYPE, MenuStateUpdateMessage.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(MenuStateUpdateMessage.TYPE, MenuStateUpdateMessage::handleMenuState);
    }

    public static void clientLoad() {
        PayloadTypeRegistry.clientboundPlay().register(MenuStateUpdateMessage.TYPE, MenuStateUpdateMessage.STREAM_CODEC);
        ClientPlayNetworking.registerGlobalReceiver(MenuStateUpdateMessage.TYPE, MenuStateUpdateMessage::handleClientMenuState);
    }

    private static <M extends AbstractContainerMenu> MenuType<M> register(String registryname, MenuType.MenuSupplier<M> element) {
        return (MenuType)Registry.register((Registry)BuiltInRegistries.MENU, (Identifier)Identifier.fromNamespaceAndPath((String)"faycore", (String)registryname), (Object)new MenuType(element, FeatureFlags.DEFAULT_FLAGS));
    }

    public static interface MenuAccessor {
        public Map<String, Object> getMenuState();

        public Map<Integer, Slot> getSlots();

        default public void sendMenuStateUpdate(Player player, int elementType, String name, Object elementState, boolean needClientUpdate) {
            this.getMenuState().put(elementType + ":" + name, elementState);
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                ServerPlayNetworking.send((ServerPlayer)serverPlayer, (CustomPacketPayload)new MenuStateUpdateMessage(elementType, name, elementState));
            } else if (player.level().isClientSide()) {
                Screen screen = Minecraft.getInstance().screen;
                if (screen instanceof FaycoreModScreens.FabricScreenAccessor) {
                    FaycoreModScreens.FabricScreenAccessor accessor = (FaycoreModScreens.FabricScreenAccessor)screen;
                    if (needClientUpdate) {
                        accessor.updateMenuState(elementType, name, elementState);
                    }
                }
                ClientPlayNetworking.send((CustomPacketPayload)new MenuStateUpdateMessage(elementType, name, elementState));
            }
        }

        default public <T> T getMenuState(int elementType, String name, T defaultValue) {
            try {
                return (T)this.getMenuState().getOrDefault(elementType + ":" + name, defaultValue);
            }
            catch (ClassCastException e) {
                return defaultValue;
            }
        }
    }
}

