/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking$Context
 *  net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking$Context
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.inventory.AbstractContainerMenu
 */
package net.mcreator.faycore.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mcreator.faycore.init.FaycoreModMenus;
import net.mcreator.faycore.init.FaycoreModScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record MenuStateUpdateMessage(int elementType, String name, Object elementState) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<MenuStateUpdateMessage> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"faycore", (String)"menustate_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MenuStateUpdateMessage> STREAM_CODEC = StreamCodec.of(MenuStateUpdateMessage::write, MenuStateUpdateMessage::read);

    public static void write(FriendlyByteBuf buffer, MenuStateUpdateMessage message) {
        Object object;
        buffer.writeInt(message.elementType);
        buffer.writeUtf(message.name);
        if (message.elementType == 0) {
            buffer.writeUtf((String)message.elementState);
        } else if (message.elementType == 1) {
            buffer.writeBoolean(((Boolean)message.elementState).booleanValue());
        } else if (message.elementType == 2 && (object = message.elementState) instanceof Number) {
            Number n = (Number)object;
            buffer.writeDouble(n.doubleValue());
        }
    }

    public static MenuStateUpdateMessage read(FriendlyByteBuf buffer) {
        int elementType = buffer.readInt();
        String name = buffer.readUtf();
        Object elementState = null;
        if (elementType == 0) {
            elementState = buffer.readUtf();
        } else if (elementType == 1) {
            elementState = buffer.readBoolean();
        } else if (elementType == 2) {
            elementState = buffer.readDouble();
        }
        return new MenuStateUpdateMessage(elementType, name, elementState);
    }

    public CustomPacketPayload.Type<MenuStateUpdateMessage> type() {
        return TYPE;
    }

    public static void handleMenuState(MenuStateUpdateMessage message, ServerPlayNetworking.Context context) {
        String string;
        Object object;
        if (message.name.length() > 256 || (object = message.elementState) instanceof String && (string = (String)object).length() > 8192) {
            return;
        }
        context.server().execute(() -> {
            AbstractContainerMenu patt0$temp = context.player().containerMenu;
            if (patt0$temp instanceof FaycoreModMenus.MenuAccessor) {
                FaycoreModMenus.MenuAccessor menu = (FaycoreModMenus.MenuAccessor)patt0$temp;
                menu.getMenuState().put(message.elementType + ":" + message.name, message.elementState);
                Screen patt1$temp = Minecraft.getInstance().screen;
                if (patt1$temp instanceof FaycoreModScreens.FabricScreenAccessor) {
                    FaycoreModScreens.FabricScreenAccessor accessor = (FaycoreModScreens.FabricScreenAccessor)patt1$temp;
                    accessor.updateMenuState(message.elementType, message.name, message.elementState);
                }
            }
        });
    }

    public static void handleClientMenuState(MenuStateUpdateMessage message, ClientPlayNetworking.Context context) {
        String string;
        Object object;
        if (message.name.length() > 256 || (object = message.elementState) instanceof String && (string = (String)object).length() > 8192) {
            return;
        }
        context.client().execute(() -> {
            AbstractContainerMenu patt0$temp = context.player().containerMenu;
            if (patt0$temp instanceof FaycoreModMenus.MenuAccessor) {
                FaycoreModMenus.MenuAccessor menu = (FaycoreModMenus.MenuAccessor)patt0$temp;
                menu.getMenuState().put(message.elementType + ":" + message.name, message.elementState);
                Screen patt1$temp = Minecraft.getInstance().screen;
                if (patt1$temp instanceof FaycoreModScreens.FabricScreenAccessor) {
                    FaycoreModScreens.FabricScreenAccessor accessor = (FaycoreModScreens.FabricScreenAccessor)patt1$temp;
                    accessor.updateMenuState(message.elementType, message.name, message.elementState);
                }
            }
        });
    }
}

