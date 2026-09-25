package chunk.faye.mod_tog.faycore.procedures;

public class OpenFaycoreMsgguiOnKeyPressedProcedure {
	public static boolean eventResult = true;

	public static void execute() {
		net.minecraft.client.Minecraft.getInstance().setScreen(new chunk.faye.mod_tog.faycore.client.gui.FayCoreCustomInputScreen());
	}
}