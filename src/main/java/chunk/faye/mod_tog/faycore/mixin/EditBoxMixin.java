package chunk.faye.mod_tog.faycore.mixin;

import chunk.faye.mod_tog.faycore.client.gui.FayCoreMacroScreen;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

@Mixin({EditBox.class})
public class EditBoxMixin {
   @Inject(
      method = {"applyFormat"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = true
   )
   private void onApplyFormat(String text, int displayPos, CallbackInfoReturnable<FormattedCharSequence> cir) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.screen instanceof FayCoreMacroScreen) {
         MutableComponent dynamicColored = Component.empty();
         Pattern colorPattern = Pattern.compile(
            "(%[a-zA-Z0-9\\-]+%)|(\\$[a-zA-Z0-9\\-_]+)|(\\b(say|tp|execute|setblock|fill|summon|tag|playsound|particle|var)\\b)"
         );
         Matcher matcher = colorPattern.matcher(text);

         int lastIdx;
         for (lastIdx = 0; matcher.find(); lastIdx = matcher.end()) {
            if (matcher.start() > lastIdx) {
               dynamicColored.append(Component.literal(text.substring(lastIdx, matcher.start())).withStyle(ChatFormatting.GRAY));
            }

            String matchedText = matcher.group();
            if ((!matchedText.startsWith("%") || !matchedText.endsWith("%")) && !matchedText.startsWith("$")) {
               dynamicColored.append(Component.literal(matchedText).withStyle(ChatFormatting.AQUA));
            } else {
               dynamicColored.append(Component.literal(matchedText).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD));
            }
         }

         if (lastIdx < text.length()) {
            dynamicColored.append(Component.literal(text.substring(lastIdx)).withStyle(ChatFormatting.GRAY));
         }

         cir.setReturnValue(dynamicColored.getVisualOrderText());
      }
   }
}
