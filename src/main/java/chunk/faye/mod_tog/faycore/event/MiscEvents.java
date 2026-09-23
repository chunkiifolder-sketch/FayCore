package chunk.faye.mod_tog.faycore.event;

import com.mojang.brigadier.ParseResults;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.commands.CommandSourceStack;

public class MiscEvents {
   public static final Event<MiscEvents.CommandExecute> COMMAND_EXECUTE = EventFactory.createArrayBacked(
      MiscEvents.CommandExecute.class, callbacks -> results -> {
            for (MiscEvents.CommandExecute event : callbacks) {
               boolean result = event.onCommandExecuted(results);
               if (!result) {
                  return false;
               }
            }

            return true;
         }
   );

   @FunctionalInterface
   public interface CommandExecute {
      boolean onCommandExecuted(ParseResults<CommandSourceStack> var1);
   }
}
