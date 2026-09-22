/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.mcreator.faycore.mixin;

import net.mcreator.faycore.config.CrashProtectionConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ItemStack.class})
public class ItemTextProtectionMixin {
    @Inject(method={"getHoverName"}, at={@At(value="RETURN")}, cancellable=true)
    private void faycore$limitItemName(CallbackInfoReturnable<Component> cir) {
        if (!CrashProtectionConfig.enableItemTextLimit) {
            return;
        }
        Component text = (Component)cir.getReturnValue();
        if (text != null && text.getString().length() > CrashProtectionConfig.maxItemTextLength) {
            cir.setReturnValue((Object)Component.literal((String)"[Blocked Item Name]"));
        }
    }
}

