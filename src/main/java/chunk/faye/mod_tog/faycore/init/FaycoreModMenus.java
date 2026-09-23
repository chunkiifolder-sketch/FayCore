package chunk.faye.mod_tog.faycore.init;

import chunk.faye.mod_tog.faycore.network.MenuStateUpdateMessage;
import chunk.faye.mod_tog.faycore.world.inventory.FaycoreSettingsMenu;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.MenuType.MenuSupplier;

public class FaycoreModMenus {
   public static MenuType<FaycoreSettingsMenu> FAYCORE_SETTINGS;

   public static void load() {
      FAYCORE_SETTINGS = register("faycore_settings", FaycoreSettingsMenu::new);
      FaycoreSettingsMenu.screenInit();
      PayloadTypeRegistry.serverboundPlay().register(MenuStateUpdateMessage.TYPE, MenuStateUpdateMessage.STREAM_CODEC);
      ServerPlayNetworking.registerGlobalReceiver(MenuStateUpdateMessage.TYPE, MenuStateUpdateMessage::handleMenuState);
   }

   public static void clientLoad() {
      PayloadTypeRegistry.clientboundPlay().register(MenuStateUpdateMessage.TYPE, MenuStateUpdateMessage.STREAM_CODEC);
      ClientPlayNetworking.registerGlobalReceiver(MenuStateUpdateMessage.TYPE, MenuStateUpdateMessage::handleClientMenuState);
   }

   private static <M extends AbstractContainerMenu> MenuType<M> register(String registryname, MenuSupplier<M> element) {
      return (MenuType<M>)Registry.register(
         BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath("faycore", registryname), new MenuType(element, FeatureFlags.DEFAULT_FLAGS)
      );
   }

   public interface MenuAccessor {
      Map<String, Object> getMenuState();

      Map<Integer, Slot> getSlots();

      default void sendMenuStateUpdate(Player player, int elementType, String name, Object elementState, boolean needClientUpdate) {
         this.getMenuState().put(elementType + ":" + name, elementState);
         if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new MenuStateUpdateMessage(elementType, name, elementState));
         } else if (player.level().isClientSide()) {
            if (Minecraft.getInstance().screen instanceof FaycoreModScreens.FabricScreenAccessor accessor && needClientUpdate) {
               accessor.updateMenuState(elementType, name, elementState);
            }

            ClientPlayNetworking.send(new MenuStateUpdateMessage(elementType, name, elementState));
         }
      }

      default <T> T getMenuState(int elementType, String name, T defaultValue) {
         try {
            return (T)this.getMenuState().getOrDefault(elementType + ":" + name, defaultValue);
         } catch (ClassCastException var5) {
            return defaultValue;
         }
      }
   }
}
