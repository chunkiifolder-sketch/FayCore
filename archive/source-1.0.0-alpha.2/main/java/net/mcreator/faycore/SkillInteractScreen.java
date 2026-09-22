package net.mcreator.faycore;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SkillInteractScreen extends Screen {

    private boolean wasMouseClickedBefore = false;

    public SkillInteractScreen() {
        super(Component.literal("Skill Interact"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractBlurredBackground(net.minecraft.client.gui.GuiGraphicsExtractor guiGraphicsExtractor) {
    }

    @Override
    public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {

        double guiScale = this.minecraft.getWindow().getGuiScale();
        int realMouseX = (int) (this.minecraft.mouseHandler.xpos() / guiScale);
        int realMouseY = (int) (this.minecraft.mouseHandler.ypos() / guiScale);

        boolean isLeftClickDown = com.mojang.blaze3d.platform.InputConstants.isKeyDown(this.minecraft.getWindow(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT);

        int slotWidth = 29;
        int slotHeight = 24;
        int gap = -1;

        int x = this.width - slotWidth - 8;
        int startY = (this.height - (slotHeight * 5 + gap * 4)) / 2;

        boolean hitSkillbarSlot = false;

        if (realMouseX >= x && realMouseX <= x + slotWidth) {
            for (int i = 0; i < 5; i++) {
                int y = startY + i * (slotHeight + gap);
                if (realMouseY >= y && realMouseY <= y + slotHeight) {
                    hitSkillbarSlot = true;
                    final int slotIndex = i;

                    if (isLeftClickDown) {
                        if (!wasMouseClickedBefore) {
                            if (SkillState.getSelected() != slotIndex) {
                                SkillState.setSelected(slotIndex);
                                String skillName = getSkillNameBySlot(slotIndex);
                                this.minecraft.gui.setOverlayMessage(Component.literal("▶ [FayCore] 已裝配特技: " + skillName + " ◀").withStyle(net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.BOLD), false);
                                this.minecraft.level.playSound(null, this.minecraft.player.getX(), this.minecraft.player.getY(), this.minecraft.player.getZ(),
                                        net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), net.minecraft.sounds.SoundSource.PLAYERS, 0.7F, 1.0F);
                            }
                            wasMouseClickedBefore = true;
                        }
                    }

                    var finalTooltipComponent = Component.empty()
                            .append(getSingleComponentTooltip(slotIndex))
                            .append("\n\n")
                            .append(Component.literal("[FayCore - Skillbar]").withStyle(net.minecraft.ChatFormatting.BLUE));

                    try {
                        var splitLines = this.minecraft.font.split(finalTooltipComponent, Integer.MAX_VALUE);

                        guiGraphicsExtractor.setTooltipForNextFrame(this.font, splitLines, realMouseX, realMouseY);
                    } catch (Throwable t) {

                    }
                    break;

                }
            }
        }

        if (!isLeftClickDown) {
            wasMouseClickedBefore = false;
        }

        if (hitSkillbarSlot && isLeftClickDown) {
            this.minecraft.options.keyAttack.setDown(false);
            if (this.minecraft.gameMode != null) {
                this.minecraft.gameMode.stopDestroyBlock();
            }
        }

        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyReleased(net.minecraft.client.input.KeyEvent event) {
        if (event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT) {
            this.onClose();
            return true;
        }
        return super.keyReleased(event);
    }

    // 空槽位 模板
    private static final Component EMPTY_SLOT = Component.empty()
            .append(Component.literal("§4 空槽"))
            .append("\n").append(Component.literal("§e使用時: §7無作用"));

    // 槽位懸停簡介
    private Component getSingleComponentTooltip(int slot) {
        var base = Component.empty();
        switch (slot) {
            case 0 -> {
                base = base.append(Component.literal("§7§l【二段跳】"))
                        .append("\n").append(Component.literal("§e使用時: §7在空中跳躍"));
            }
            case 1 -> {
                base = EMPTY_SLOT.copy();
            }
            case 2 -> {
                base = EMPTY_SLOT.copy();
            }
            case 3 -> {
                base = EMPTY_SLOT.copy();
            }
            case 4 -> {
                base = EMPTY_SLOT.copy();
            }
            default -> base = base.append(Component.literal("§3[FayCore] &4未知技能").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
        }
        return base;
    }

    // 自訂skillbar槽位名稱
    private String getSkillNameBySlot(int slot) {
        return switch (slot) {
            case 0 -> "浮空術";
            case 1 -> "空槽";
            case 2 -> "空槽";
            case 3 -> "空槽";
            case 4 -> "空槽";
            default -> "未知技能";
        };
    }
}
