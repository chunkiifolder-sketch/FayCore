/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.ItemLore
 */
package net.mcreator.faycore;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.mcreator.faycore.SkillState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

public class SkillRenderer {
    private static final Identifier SKILLBAR_TEX = Identifier.fromNamespaceAndPath((String)"faycore", (String)"textures/screens/skillbar.png");
    private static final Identifier SELECTION_TEX = Identifier.fromNamespaceAndPath((String)"faycore", (String)"textures/screens/skillbar_selection.png");

    public static List<Component> getSingleComponentTooltipList(int slot) {
        ArrayList<Component> lines = new ArrayList<Component>();
        return lines;
    }

    public static void register() {
        HudElementRegistry.addLast((Identifier)Identifier.fromNamespaceAndPath((String)"faycore", (String)"skillbar"), (context, tickCounter) -> {
            int slotWidth = 29;
            int slotHeight = 24;
            int gap = -1;
            int x = context.guiWidth() - slotWidth - 8;
            int startY = (context.guiHeight() - (slotHeight * SkillState.MAX + gap * 4)) / 2;
            for (int i = 0; i < SkillState.MAX; ++i) {
                int y = startY + i * (slotHeight + gap);
                context.blit(SKILLBAR_TEX, x, y, x + slotWidth, y + slotHeight, 0.0f, 1.0f, 0.0f, 1.0f);
                ItemStack stack = SkillState.getItemInSlot(i);
                if (!stack.isEmpty()) {
                    int itemX = x + 10;
                    int itemY = y + 4;
                    ItemStack displayStack = stack.copy();
                    List<Component> splitLines = SkillRenderer.getSingleComponentTooltipList(i);
                    if (!splitLines.isEmpty()) {
                        displayStack.set(DataComponents.CUSTOM_NAME, (Object)splitLines.get(0));
                        ArrayList<Component> loreLines = new ArrayList<Component>();
                        for (int j = 1; j < splitLines.size(); ++j) {
                            loreLines.add(splitLines.get(j));
                        }
                        displayStack.set(DataComponents.LORE, (Object)new ItemLore(loreLines));
                    }
                    context.fakeItem(displayStack, itemX, itemY);
                }
                if (i != SkillState.getSelected()) continue;
                context.blit(SELECTION_TEX, x + 6, y, x + 30, y + 23, 0.0f, 1.0f, 0.0f, 1.0f);
            }
        });
    }
}

