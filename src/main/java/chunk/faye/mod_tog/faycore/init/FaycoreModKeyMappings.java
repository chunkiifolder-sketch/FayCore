package chunk.faye.mod_tog.faycore.init;

import chunk.faye.mod_tog.faycore.network.OpenFaycoreMsgguiMessage;
import chunk.faye.mod_tog.faycore.network.OpenFaycoreSettingsMessage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping.Category;

@Environment(EnvType.CLIENT)
public class FaycoreModKeyMappings {
   public static final KeyMapping OPEN_FAYCORE_MSGGUI = new KeyMapping("key.faycore.open_faycore_msggui", 66, Category.MOVEMENT) {
      private boolean isDownOld = false;

      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            ClientPlayNetworking.send(new OpenFaycoreMsgguiMessage(0, 0));
            OpenFaycoreMsgguiMessage.pressAction(Minecraft.getInstance().player, 0, 0);
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping OPEN_FAYCORE_SETTINGS = new KeyMapping("key.faycore.open_faycore_settings", 321, Category.MOVEMENT) {
      private boolean isDownOld = false;

      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            ClientPlayNetworking.send(new OpenFaycoreSettingsMessage(0, 0));
            OpenFaycoreSettingsMessage.pressAction(Minecraft.getInstance().player, 0, 0);
         }

         this.isDownOld = isDown;
      }
   };

   public static void clientLoad() {
      KeyMappingHelper.registerKeyMapping(OPEN_FAYCORE_MSGGUI);
      KeyMappingHelper.registerKeyMapping(OPEN_FAYCORE_SETTINGS);
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         if (client.screen == null) {
            OPEN_FAYCORE_MSGGUI.consumeClick();
            OPEN_FAYCORE_SETTINGS.consumeClick();
         }
      });
   }
}
