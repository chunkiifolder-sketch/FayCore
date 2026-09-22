/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ParseResults
 *  net.fabricmc.fabric.api.event.Event
 *  net.fabricmc.fabric.api.event.EventFactory
 *  net.minecraft.commands.CommandSourceStack
 */
package chunk.faye.mod_tog.faycore.event;

import com.mojang.brigadier.ParseResults;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.commands.CommandSourceStack;

public class MiscEvents {
    public static final Event<CommandExecute> COMMAND_EXECUTE = EventFactory.createArrayBacked(CommandExecute.class, callbacks -> results -> {
        for (CommandExecute event : callbacks) {
            boolean result = event.onCommandExecuted((ParseResults<CommandSourceStack>)results);
            if (result) continue;
            return false;
        }
        return true;
    });

    @FunctionalInterface
    public static interface CommandExecute {
        public boolean onCommandExecuted(ParseResults<CommandSourceStack> var1);
    }
}

