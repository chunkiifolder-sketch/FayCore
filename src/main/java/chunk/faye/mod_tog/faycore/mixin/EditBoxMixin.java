package chunk.faye.mod_tog.faycore.mixin;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EditBox.class)
public class EditBoxMixin {

    // 👑【總閘口降維打擊】：將時機從 TAIL 換成 HEAD！
    // 只要一準備執行 applyFormat，我們的代碼在最開頭就直接接管，並透過 cancellable = true 物理斬斷官方後續的塗白去色程序！
    @Inject(method = "applyFormat", at = @At("HEAD"), cancellable = true, remap = true)
    private void onApplyFormat(String text, int displayPos, CallbackInfoReturnable<FormattedCharSequence> cir) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();

        // 只有在 FayCore 介面裡才觸發高亮，維持原生的絕對安全相容
        if (mc.screen instanceof chunk.faye.mod_tog.faycore.client.gui.FayCoreMacroScreen) {

            net.minecraft.network.chat.MutableComponent dynamicColored = net.minecraft.network.chat.Component.empty();

            // 📐 【FayCore 萬能雙制導高亮正則過濾器】
            // 第一組：(%[a-zA-Z0-9\\-]+%) ➡️ 負責捕捉開頭結尾為 % 的原生變數
            // 第二組：(\\$[a-zA-Z0-9\\-_]+) ➡️ 🔥【全新加入】：精準捕捉以 $ 開頭、後面接字母或數字的巨集變數！
            // 第三組：(\\b(say|tp|execute|setblock|fill|summon|tag|playsound|particle)\\b) ➡️ 負責捕捉水藍色指令關鍵字
            java.util.regex.Pattern colorPattern = java.util.regex.Pattern.compile("(%[a-zA-Z0-9\\-]+%)|(\\$[a-zA-Z0-9\\-_]+)|(\\b(say|tp|execute|setblock|fill|summon|tag|playsound|particle|var)\\b)");
            java.util.regex.Matcher matcher = colorPattern.matcher(text);

            int lastIdx = 0;
            while (matcher.find()) {
                if (matcher.start() > lastIdx) {
                    dynamicColored.append(net.minecraft.network.chat.Component.literal(text.substring(lastIdx, matcher.start())).withStyle(net.minecraft.ChatFormatting.GRAY));
                }

                String matchedText = matcher.group();

                // 👑 【關鍵修復點】：只要單字是以 % 開頭，或者是以 $ 開頭，通通強制扒皮染成「加粗、粉亮紫色」！
                if ((matchedText.startsWith("%") && matchedText.endsWith("%")) || matchedText.startsWith("$")) {
                    dynamicColored.append(net.minecraft.network.chat.Component.literal(matchedText)
                            .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE)
                            .withStyle(net.minecraft.ChatFormatting.BOLD));
                } else {
                    // 🧱 常用指令關鍵字染成「水藍色」
                    dynamicColored.append(net.minecraft.network.chat.Component.literal(matchedText)
                            .withStyle(net.minecraft.ChatFormatting.AQUA));
                }
                lastIdx = matcher.end();
            }


            if (lastIdx < text.length()) {
                dynamicColored.append(net.minecraft.network.chat.Component.literal(text.substring(lastIdx)).withStyle(net.minecraft.ChatFormatting.GRAY));
            }

            // 🚀 強行改寫！不論官方內部的焦點機制怎麼去色，此處直接強行傳回彩色 VisualOrderText 並就地 return 結束方法！
            cir.setReturnValue(dynamicColored.getVisualOrderText());
        }
    }
}
