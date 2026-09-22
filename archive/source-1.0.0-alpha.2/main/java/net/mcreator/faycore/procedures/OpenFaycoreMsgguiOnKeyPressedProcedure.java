package net.mcreator.faycore.procedures;

public class OpenFaycoreMsgguiOnKeyPressedProcedure {
	public static boolean eventResult = true;

	public static void execute() {
		net.minecraft.client.Minecraft.getInstance().setScreen(new net.mcreator.faycore.client.gui.FayCoreCustomInputScreen());
	}
}