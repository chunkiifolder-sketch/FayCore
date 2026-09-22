/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  org.lwjgl.glfw.GLFW
 */
package chunk.faye.mod_tog.faycore;

import java.util.List;
import chunk.faye.mod_tog.faycore.FayCoreWings;
import chunk.faye.mod_tog.faycore.SkillManager;
import chunk.faye.mod_tog.faycore.SkillState;
import chunk.faye.mod_tog.faycore.SkillTracker;
import chunk.faye.mod_tog.faycore.client.gui.FayCoreControlScreen;
import chunk.faye.mod_tog.faycore.client.gui.FayCoreMacroEngine;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.lwjgl.glfw.GLFW;

public class SkillInteractScreen
extends Screen {
    public static int LaserMode = 0;
    public static String LaserModeLore = null;
    public static int GunMode = 0;
    public static String GunModeLore = null;
    private boolean wasMouseClickedBefore = false;
    public static String GunModeDamage = "0";
    public static int WingMode = 0;
    public static String WingLore = null;
    public static int WingSize = 0;
    public static String WingSizeLore = null;
    private static final Component EMPTY_SLOT = Component.empty().append((Component)Component.literal((String)"\u00a74 \u7a7a\u69fd")).append("\n").append((Component)Component.literal((String)"\u00a7e\u4f7f\u7528\u6642: \u00a77\u7121\u4f5c\u7528"));

    public SkillInteractScreen() {
        super((Component)Component.literal((String)"Skill Interact"));
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void extractBlurredBackground(GuiGraphicsExtractor guiGraphicsExtractor) {
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        double guiScale = this.minecraft.getWindow().getGuiScale();
        int realMouseX = (int)(this.minecraft.mouseHandler.xpos() / guiScale);
        int realMouseY = (int)(this.minecraft.mouseHandler.ypos() / guiScale);
        boolean isLeftClickDown = GLFW.glfwGetMouseButton((long)this.minecraft.getWindow().handle(), (int)0) == 1;
        int slotWidth = 29;
        int slotHeight = 24;
        int gap = -1;
        int x = this.width - slotWidth - 8;
        int startY = (this.height - (slotHeight * SkillState.MAX + gap * 4)) / 2;
        boolean hitSkillbarSlot = false;
        if (realMouseX >= x && realMouseX <= x + slotWidth) {
            for (int i = 0; i < SkillState.MAX; ++i) {
                int y = startY + i * (slotHeight + gap);
                if (realMouseY < y || realMouseY > y + slotHeight) continue;
                hitSkillbarSlot = true;
                int slotIndex = i;
                if (isLeftClickDown && !this.wasMouseClickedBefore) {
                    if (SkillState.getSelected() != slotIndex) {
                        SkillState.setSelected(slotIndex);
                        String skillName = SkillState.getSkillName(slotIndex);
                        this.minecraft.gui.setOverlayMessage((Component)Component.literal((String)("\u25b6 [FayCore] \u5df2\u88dd\u914d\u7279\u6280: " + skillName + " \u25c0")).withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}), false);
                        this.minecraft.level.playSound(null, this.minecraft.player.getX(), this.minecraft.player.getY(), this.minecraft.player.getZ(), (SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.7f, 1.0f);
                    }
                    this.wasMouseClickedBefore = true;
                }
                MutableComponent finalTooltipComponent = Component.empty().append(this.getSingleComponentTooltip(slotIndex)).append("\n\n").append((Component)Component.literal((String)"[FayCore - Skillbar]").withStyle(ChatFormatting.BLUE));
                try {
                    List splitLines = this.minecraft.font.split((FormattedText)finalTooltipComponent, Integer.MAX_VALUE);
                    guiGraphicsExtractor.setTooltipForNextFrame(this.font, splitLines, realMouseX, realMouseY);
                }
                catch (Throwable throwable) {}
                break;
            }
        }
        if (!isLeftClickDown) {
            this.wasMouseClickedBefore = false;
        }
        if (hitSkillbarSlot && isLeftClickDown) {
            this.minecraft.options.keyAttack.setDown(false);
            if (this.minecraft.gameMode != null) {
                this.minecraft.gameMode.stopDestroyBlock();
            }
        }
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
    }

    public boolean keyReleased(KeyEvent event) {
        if (event.key() == 342) {
            this.onClose();
            return true;
        }
        return super.keyReleased(event);
    }

    private Component getSingleComponentTooltip(int slot) {
        MutableComponent base = Component.empty();
        switch (slot) {
            case 0: {
                base = Component.literal((String)"\u00a77\u00a7l\u3010\u591a\u6bb5\u8df3\u3011\n\u00a7e\u4f7f\u7528\u6642: \u00a77\u5728\u7a7a\u4e2d\u8df3\u8e8d");
                break;
            }
            case 1: {
                switch (LaserMode) {
                    case 0: {
                        LaserModeLore = String.format("\u7206\u70b8\u6a21\u5f0f \u00a74[%.1f] \u00a77(1/4)", Float.valueOf(SkillManager.TNT_POWER));
                        break;
                    }
                    case 1: {
                        LaserModeLore = String.format("\u522a\u9664\u6a21\u5f0f \u00a74[%d] \u00a77(2/4)", SkillManager.DELETE_POWER);
                        break;
                    }
                    case 2: {
                        LaserModeLore = "\u64ca\u9000\u6a21\u5f0f (3/4)";
                        break;
                    }
                    case 3: {
                        LaserModeLore = String.format("\u98c4\u584a\u6a21\u5f0f \u00a74[%d] \u00a77(4/4)", SkillManager.FLING_POWER);
                        break;
                    }
                    default: {
                        LaserModeLore = "\u672a\u77e5\u6a21\u5f0f";
                    }
                }
                base = Component.literal((String)("\u00a77\u00a7l\u3010\u96f7\u5c04\u3011\n\u00a7e\u4f7f\u7528\u6642: \u00a77\u767c\u5c04\u96f7\u5c04\n\n\u00a73\u6a21\u5f0f: \u00a77" + LaserModeLore));
                break;
            }
            case 2: {
                switch (GunMode) {
                    case 0: {
                        GunModeLore = "\u666e\u901a\u6a21\u5f0f (1/3)";
                        GunModeDamage = "20";
                        break;
                    }
                    case 1: {
                        GunModeLore = "\u79d2\u6bba\u6a21\u5f0f (2/3)";
                        GunModeDamage = "Infinity";
                        break;
                    }
                    case 2: {
                        GunModeLore = "\u706b\u7bad\u7b52 (3/3)";
                        GunModeDamage = "\u00a7c???";
                    }
                }
                base = Component.literal((String)("\u00a77\u00a7l\u3010\u624b\u69cd\u3011\n\u00a7e\u4f7f\u7528\u6642: \u00a77\u767c\u5c04\u5b50\u5f48\n\n\u00a73\u6a21\u5f0f: \u00a77" + GunModeLore + "\n\u00a73\u50b7\u5bb3: \u00a77" + GunModeDamage));
                break;
            }
            case 3: {
                base = Component.literal((String)("\u00a77\u00a7l\u3010\u63a7\u5236\u3011\n\u00a7e\u4f7f\u7528\u6642: \u00a77\u63a7\u5236\u4efb\u4f55\u751f\u7269\n\n\u00a7d\u5177\u6709\u591a\u7a2e\u529f\u80fd:\n\u00a73Ctrl + \u6efe\u8f2a\u00a77 | \u00a73\u63a7\u5236\u8ddd\u96e2 (\u76ee\u524d: " + SkillTracker.controlDistance + ")"));
                break;
            }
            case 4: {
                base = Component.literal((String)("\u00a77\u00a7l\u3010\u77ac\u79fb\u3011\n\u00a7e\u4f7f\u7528\u6642: \u00a77\u77ac\u79fb\u5230\u7279\u5b9a\u4f4d\u7f6e\n\n\u00a7d\u5177\u6709\u591a\u7a2e\u529f\u80fd:\n\u00a73Ctrl + \u6efe\u8f2a\u00a77 | \u00a73\u50b3\u9001\u8ddd\u96e2 (\u76ee\u524d: " + SkillManager.teleportDistance + ")"));
                break;
            }
            case 5: {
                switch (WingMode) {
                    case 0: {
                        WingLore = "Nothing (1/5)";
                        break;
                    }
                    case 1: {
                        WingLore = "Sculk Soul (2/5)";
                        break;
                    }
                    case 2: {
                        WingLore = "Cherry (3/5)";
                        break;
                    }
                    case 3: {
                        WingLore = "Cloud (4/5)";
                        break;
                    }
                    case 4: {
                        WingLore = "Crit (5/5)";
                        break;
                    }
                    default: {
                        WingLore = "\u672a\u77e5\u7a2e\u985e";
                    }
                }
                switch (WingSize) {
                    case 0: {
                        WingSizeLore = "1 (1/3)";
                        break;
                    }
                    case 1: {
                        WingSizeLore = "2 (2/3)";
                        break;
                    }
                    case 2: {
                        WingSizeLore = "Cape (3/3)";
                        break;
                    }
                    default: {
                        WingSizeLore = "\u672a\u77e5\u5927\u5c0f";
                    }
                }
                base = Component.literal((String)("\u00a77\u00a7l\u3010\u7fc5\u8180\u986f\u793a\u3011\n\u00a7e\u4f7f\u7528\u6642: \u00a77\u88dd\u98fe\u54c1\n\n\u00a73\u7a2e\u985e: \u00a77" + WingLore + "\n\u00a73\u5927\u5c0f: \u00a77" + WingSizeLore));
                break;
            }
            case 6: {
                base = EMPTY_SLOT.copy();
                break;
            }
            default: {
                base = base.append((Component)Component.literal((String)"\u00a73[FayCore] \u00a74\u672a\u77e5\u6280\u80fd").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
        return base;
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        Minecraft mc = Minecraft.getInstance();
        int slot = this.getHoveredSlot(event.x(), event.y());
        int CurrentSlot = SkillState.getSelected();
        if (slot != -1) {
            if (event.button() == 0 && slot == CurrentSlot) {
                switch (slot) {
                    case 5: {
                        WingSize = (WingSize + 1) % 3;
                        break;
                    }
                    default: {
                        FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "title %player% actionbar \"&7\u8acb\u5207\u63db\u5230 &e\u69fd\u4f4d\" + slot");
                        FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "execute as %player% at @s run playsound minecraft:item.bundle.insert_fail player @s ~ ~ ~ 5 1");
                        return false;
                    }
                }
            }
            if (event.button() == 1) {
                if (slot != CurrentSlot) {
                    FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "title %player% actionbar \"&7\u8acb\u5207\u63db\u5230 &e\u69fd\u4f4d\" + slot");
                    FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "execute as %player% at @s run playsound minecraft:item.bundle.insert_fail player @s ~ ~ ~ 5 1");
                    return false;
                }
                switch (slot) {
                    case 0: {
                        FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "execute as %player% at @s run playsound minecraft:item.bundle.insert_fail player @s ~ ~ ~ 5 1");
                        break;
                    }
                    case 1: {
                        FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "execute as %player% at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1 1");
                        LaserMode = (LaserMode + 1) % 4;
                        FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "title %player% actionbar {text:\"\"}");
                        break;
                    }
                    case 2: {
                        GunMode = (GunMode + 1) % 3;
                        FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "execute as %player% at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1 1");
                        break;
                    }
                    case 3: {
                        mc.setScreen((Screen)new FayCoreControlScreen());
                        break;
                    }
                    case 4: {
                        break;
                    }
                    case 5: {
                        FayCoreMacroEngine.autoFindAndInjectVCommand(mc, "execute as %player% at @s run playsound minecraft:block.note_block.pling player @s ~ ~ ~ 1 1 1");
                        WingMode = (WingMode + 1) % 5;
                        FayCoreWings.enableWings = true;
                        break;
                    }
                }
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private int getHoveredSlot(double mouseX, double mouseY) {
        int slotWidth = 29;
        int slotHeight = 24;
        int gap = -1;
        int x = this.width - slotWidth - 8;
        int startY = (this.height - (slotHeight * SkillState.MAX + gap * 4)) / 2;
        if (mouseX >= (double)x && mouseX <= (double)(x + slotWidth)) {
            for (int i = 0; i < SkillState.MAX; ++i) {
                int y = startY + i * (slotHeight + gap);
                if (!(mouseY >= (double)y) || !(mouseY <= (double)(y + slotHeight))) continue;
                return i;
            }
        }
        return -1;
    }
}

