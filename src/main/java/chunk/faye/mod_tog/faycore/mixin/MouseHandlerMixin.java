/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.MouseHandler
 *  net.minecraft.network.chat.Component
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  org.lwjgl.glfw.GLFW
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.SkillInteractScreen;
import chunk.faye.mod_tog.faycore.SkillManager;
import chunk.faye.mod_tog.faycore.SkillState;
import chunk.faye.mod_tog.faycore.SkillTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MouseHandler.class})
public class MouseHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method={"onScroll"}, at={@At(value="HEAD")}, cancellable=true)
    private void onScroll(long handle, double xoffset, double yoffset, CallbackInfo ci) {
        boolean shift;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        boolean alt = GLFW.glfwGetKey((long)client.getWindow().handle(), (int)342) == 1 || GLFW.glfwGetKey((long)client.getWindow().handle(), (int)346) == 1;
        boolean ctrl = GLFW.glfwGetKey((long)client.getWindow().handle(), (int)341) == 1 || GLFW.glfwGetKey((long)client.getWindow().handle(), (int)345) == 1;
        boolean bl = shift = GLFW.glfwGetKey((long)client.getWindow().handle(), (int)340) == 1 || GLFW.glfwGetKey((long)client.getWindow().handle(), (int)344) == 1;
        if (SkillState.getSelected() == 3 && ctrl) {
            if (yoffset > 0.0) {
                ++SkillTracker.controlDistance;
            }
            if (yoffset < 0.0) {
                --SkillTracker.controlDistance;
            }
            SkillTracker.controlDistance = Math.clamp((long)SkillTracker.controlDistance, 1, 64);
            client.gui.setOverlayMessage((Component)Component.literal((String)("\u00a79[FayCore] \u00a7e\u63a7\u5236\u8ddd\u96e2\uff1a\u00a74 " + SkillTracker.controlDistance)), false);
            ci.cancel();
            return;
        }
        if (ctrl && this.minecraft.player != null && this.minecraft.level != null) {
            this.minecraft.level.playLocalSound(this.minecraft.player.getX(), this.minecraft.player.getY(), this.minecraft.player.getZ(), (SoundEvent)SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.PLAYERS, 1.0f, 1.0f, false);
        }
        if (SkillState.getSelected() == 4 && ctrl) {
            if (yoffset > 0.0) {
                ++SkillManager.teleportDistance;
            }
            if (yoffset < 0.0) {
                --SkillManager.teleportDistance;
            }
            SkillManager.teleportDistance = Math.clamp((long)SkillManager.teleportDistance, 1, 64);
            client.gui.setOverlayMessage((Component)Component.literal((String)("\u00a79[FayCore] \u00a7e\u50b3\u9001\u8ddd\u96e2\uff1a\u00a74 " + SkillManager.teleportDistance)), false);
            ci.cancel();
            return;
        }
        if (SkillState.getSelected() == 1 && ctrl) {
            if (SkillInteractScreen.LaserMode == 0) {
                if (yoffset > 0.0) {
                    SkillManager.TNT_POWER += 0.1f;
                }
                if (yoffset < 0.0) {
                    SkillManager.TNT_POWER -= 0.1f;
                }
                SkillManager.TNT_POWER = Math.clamp(SkillManager.TNT_POWER, 1.0f, 10.0f);
                client.gui.setOverlayMessage((Component)Component.literal((String)String.format("\u00a79[FayCore] \u00a7e\u7206\u70b8\u5a01\u529b\uff1a\u00a74 %.1f", Float.valueOf(SkillManager.TNT_POWER))), false);
                ci.cancel();
                return;
            }
            if (SkillInteractScreen.LaserMode == 1) {
                if (yoffset > 0.0) {
                    ++SkillManager.DELETE_POWER;
                }
                if (yoffset < 0.0) {
                    --SkillManager.DELETE_POWER;
                }
                SkillManager.DELETE_POWER = Math.clamp((long)SkillManager.DELETE_POWER, 1, 3);
                client.gui.setOverlayMessage((Component)Component.literal((String)String.format("\u00a79[FayCore] \u00a7e\u7834\u58de\u5a01\u529b\uff1a\u00a74 %d", SkillManager.DELETE_POWER)), false);
                ci.cancel();
                return;
            }
        }
        if (SkillInteractScreen.LaserMode == 3 && ctrl) {
            if (yoffset > 0.0) {
                ++SkillManager.FLING_POWER;
            }
            if (yoffset < 0.0) {
                --SkillManager.FLING_POWER;
            }
            SkillManager.FLING_POWER = Math.clamp((long)SkillManager.FLING_POWER, 1, 3);
            client.gui.setOverlayMessage((Component)Component.literal((String)String.format("\u00a79[FayCore] \u00a7e\u98c4\u584a\u5a01\u529b\uff1a\u00a74 %d", SkillManager.FLING_POWER)), false);
            ci.cancel();
            return;
        }
        if (alt) {
            if (yoffset > 0.0) {
                SkillState.previous();
            }
            if (yoffset < 0.0) {
                SkillState.next();
            }
            ci.cancel();
        }
    }
}

