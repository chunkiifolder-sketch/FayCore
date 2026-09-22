/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Tooltip
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 */
package chunk.faye.mod_tog.faycore.client.gui;

import chunk.faye.mod_tog.faycore.SkillManager;
import chunk.faye.mod_tog.faycore.SkillTracker;
import chunk.faye.mod_tog.faycore.client.gui.FayCoreMacroEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class FayCoreControlScreen
extends Screen {
    Minecraft mc = Minecraft.getInstance();
    private Button KillButton;
    private Button FreezeButton;
    private Button CrashButton;
    private Button BlacklistButton;

    public FayCoreControlScreen() {
        super((Component)Component.literal((String)"Test"));
    }

    protected void init() {
        Entity target = SkillTracker.getTargetEntity();
        String TargetName = null;
        String TargetUUID = null;
        if (target != null && SkillManager.IsControl) {
            TargetName = target.getName().getString();
            TargetUUID = target.getUUID().toString();
        }
        String finalTargetName = TargetName;
        String finalTargetUUID = TargetUUID;
        boolean isPlayer = target instanceof Player;
        this.KillButton = Button.builder((Component)Component.literal((String)"\u00a77Kill"), button -> {
            if (finalTargetUUID != null) {
                String KillTarget = String.format("kill %s", finalTargetUUID);
                FayCoreMacroEngine.autoFindAndInjectVCommand(this.mc, KillTarget);
            }
            Minecraft.getInstance().setScreen(null);
        }).bounds(this.width / 2 - 95, this.height / 2 - 50, 50, 20).build();
        this.KillButton.active = target != null && SkillManager.IsControl;
        this.KillButton.setTooltip(Tooltip.create((Component)Component.literal((String)("\u00a7cTarget: \u00a7f" + TargetName + "\n\u00a77UUID: \u00a7f" + TargetUUID))));
        this.addRenderableWidget((GuiEventListener)this.KillButton);
        this.FreezeButton = Button.builder((Component)Component.literal((String)"\u00a77Freeze"), button -> {
            if (finalTargetUUID != null) {
                double TargetX = target.getX();
                double TargetY = target.getY();
                double TargetZ = target.getZ();
                String FreezeTarget = String.format("tp %s %f %f %f", finalTargetUUID, TargetX, TargetY, TargetZ);
                FayCoreMacroEngine.autoFindAndInjectVCommand(this.mc, FreezeTarget);
                SkillTracker.glowTarget = null;
                SkillTracker.controlTarget = null;
            }
            Minecraft.getInstance().setScreen(null);
        }).bounds(this.width / 2 - 25, this.height / 2 - 50, 50, 20).build();
        this.FreezeButton.active = target != null && SkillManager.IsControl && isPlayer;
        this.FreezeButton.setTooltip(Tooltip.create((Component)Component.literal((String)("\u00a7cTarget: \u00a7f" + TargetName + "\n\u00a77UUID: \u00a7f" + TargetUUID))));
        this.addRenderableWidget((GuiEventListener)this.FreezeButton);
        this.CrashButton = Button.builder((Component)Component.literal((String)"\u00a77Crash"), button -> {
            if (finalTargetUUID != null) {
                String DialogPayload = String.format("execute as %s at @s run dialog show @s {type:\"minecraft:multi_action\",body:[],title:{text:\"\"},inputs:[],columns:2147483647,actions:[{label:\"\"}]}", finalTargetName);
                String TranslationPayload = String.format("tellraw %s {translate:\"%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s\",with:[{translate:\"%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s\",with:[{translate:\"%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s\",with:[{translate:\"%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s\",with:[{translate:\"%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s\",with:[{translate:\"%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s\",with:[{translate:\"%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s\",with:[{translate:\"%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s\",with:[{translate:\"%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s\",with:[{translate:\"%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s%1$s\",with:[{text:\"#############################\",obfuscated:true}]}]}]}]}]}]}]}]}]}]}", finalTargetName);
                String ForceGms = String.format("gamemode survival %s", finalTargetName);
                FayCoreMacroEngine.autoFindAndInjectVCommand(this.mc, DialogPayload);
                FayCoreMacroEngine.autoFindAndInjectVCommand(this.mc, TranslationPayload);
                FayCoreMacroEngine.autoFindAndInjectVCommand(this.mc, TranslationPayload);
                SkillTracker.glowTarget = null;
                SkillTracker.controlTarget = null;
            }
            Minecraft.getInstance().setScreen(null);
        }).bounds(this.width / 2 + 50, this.height / 2 - 50, 50, 20).build();
        this.CrashButton.active = target != null && SkillManager.IsControl && isPlayer;
        this.CrashButton.setTooltip(Tooltip.create((Component)Component.literal((String)("\u00a7cTarget: \u00a7f" + TargetName + "\n\u00a77UUID: \u00a7f" + TargetUUID))));
        this.addRenderableWidget((GuiEventListener)this.CrashButton);
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, -1442840576, -1442840576);
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

