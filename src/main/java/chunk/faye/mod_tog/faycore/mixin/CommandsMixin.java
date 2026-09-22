/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ParseResults
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package chunk.faye.mod_tog.faycore.mixin;

import com.mojang.brigadier.ParseResults;
import chunk.faye.mod_tog.faycore.event.MiscEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Commands.class})
public abstract class CommandsMixin {
    @Inject(method={"performCommand(Lcom/mojang/brigadier/ParseResults;Ljava/lang/String;)V"}, at={@At(value="HEAD")}, cancellable=true)
    public void performCommand(ParseResults<CommandSourceStack> parseResults, String string, CallbackInfo ci) {
        boolean result = ((MiscEvents.CommandExecute)MiscEvents.COMMAND_EXECUTE.invoker()).onCommandExecuted(parseResults);
        if (!result) {
            ci.cancel();
        }
    }
}

