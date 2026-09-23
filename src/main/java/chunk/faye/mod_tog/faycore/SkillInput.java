package chunk.faye.mod_tog.faycore;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;

public class SkillInput {
   public static boolean wasAltDown = false;
   public static int currentHoveredSlot = -1;
   public static boolean isVKeyCurrentlyHolding = false;
   private static boolean isScreenOpening = false;
   public static boolean isVKeyWaitingForPhysicalRelease = false;

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         if (client.player != null) {
            Window currentWindow = client.getWindow();
            boolean isAltDown = InputConstants.isKeyDown(currentWindow, 342);
            if (isAltDown) {
               if (client.screen == null && !isScreenOpening) {
                  isScreenOpening = true;
                  client.execute(() -> {
                     client.setScreen(new SkillInteractScreen());
                     SkillTracker.skillMenuOpen = true;
                     wasAltDown = true;
                     currentHoveredSlot = -1;
                  });
               }
            } else if (isScreenOpening || wasAltDown) {
               if (client.screen instanceof SkillInteractScreen) {
                  client.execute(() -> client.setScreen(null));
               }

               SkillTracker.skillMenuOpen = false;
               isScreenOpening = false;
               wasAltDown = false;
               currentHoveredSlot = -1;
            }

            if (client.screen != null) {
               isVKeyCurrentlyHolding = false;
               isVKeyWaitingForPhysicalRelease = false;
            } else {
               boolean isVDown = InputConstants.isKeyDown(currentWindow, 86);
               if (isVDown) {
                  if (isVKeyWaitingForPhysicalRelease) {
                     return;
                  }

                  if (!isVKeyCurrentlyHolding) {
                     int currentSelected = SkillState.getSelected();
                     SkillManager.castPressSkill(currentSelected);
                     isVKeyCurrentlyHolding = true;
                  } else {
                     int currentSelected = SkillState.getSelected();
                     SkillManager.castHoldingSkill(currentSelected);
                  }
               } else {
                  isVKeyWaitingForPhysicalRelease = false;
                  if (isVKeyCurrentlyHolding) {
                     int currentSelected = SkillState.getSelected();
                     SkillManager.castReleaseSkill(currentSelected);
                     isVKeyCurrentlyHolding = false;
                  }
               }
            }
         }
      });
   }
}
