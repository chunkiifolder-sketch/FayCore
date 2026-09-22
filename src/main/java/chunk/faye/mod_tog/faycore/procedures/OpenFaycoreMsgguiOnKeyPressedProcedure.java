/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 */
package chunk.faye.mod_tog.faycore.procedures;

import chunk.faye.mod_tog.faycore.client.gui.FayCoreCustomInputScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class OpenFaycoreMsgguiOnKeyPressedProcedure {
    public static boolean eventResult = true;

    public static void execute() {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.player != null) {
                mc.setScreen((Screen)new FayCoreCustomInputScreen());
            }
        });
    }
}

