package chunk.faye.mod_tog.faycore.procedures;

import chunk.faye.mod_tog.faycore.client.gui.FayCoreCustomInputScreen;
import net.minecraft.client.Minecraft;

public class OpenFaycoreMsgguiOnKeyPressedProcedure {
   public static boolean eventResult = true;

   public static void execute() {
      Minecraft mc = Minecraft.getInstance();
      mc.execute(() -> {
         if (mc.player != null) {
            mc.setScreen(new FayCoreCustomInputScreen());
         }
      });
   }
}
