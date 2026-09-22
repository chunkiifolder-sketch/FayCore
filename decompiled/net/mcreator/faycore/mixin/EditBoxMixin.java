/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.util.FormattedCharSequence
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.mcreator.faycore.mixin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.mcreator.faycore.client.gui.FayCoreMacroScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={EditBox.class})
public class EditBoxMixin {
    @Inject(method={"applyFormat"}, at={@At(value="HEAD")}, cancellable=true, remap=true)
    private void onApplyFormat(String text, int displayPos, CallbackInfoReturnable<FormattedCharSequence> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof FayCoreMacroScreen) {
            MutableComponent dynamicColored = Component.empty();
            Pattern colorPattern = Pattern.compile("(%[a-zA-Z0-9\\-]+%)|(\\$[a-zA-Z0-9\\-_]+)|(\\b(say|tp|execute|setblock|fill|summon|tag|playsound|particle|var)\\b)");
            Matcher matcher = colorPattern.matcher(text);
            int lastIdx = 0;
            while (matcher.find()) {
                String matchedText;
                if (matcher.start() > lastIdx) {
                    dynamicColored.append((Component)Component.literal((String)text.substring(lastIdx, matcher.start())).withStyle(ChatFormatting.GRAY));
                }
                if ((matchedText = matcher.group()).startsWith("%") && matchedText.endsWith("%") || matchedText.startsWith("$")) {
                    dynamicColored.append((Component)Component.literal((String)matchedText).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD));
                } else {
                    dynamicColored.append((Component)Component.literal((String)matchedText).withStyle(ChatFormatting.AQUA));
                }
                lastIdx = matcher.end();
            }
            if (lastIdx < text.length()) {
                dynamicColored.append((Component)Component.literal((String)text.substring(lastIdx)).withStyle(ChatFormatting.GRAY));
            }
            cir.setReturnValue((Object)dynamicColored.getVisualOrderText());
        }
    }
}

