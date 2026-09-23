package chunk.faye.mod_tog.faycore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;

public class FayCoreTickQueue {
   private static final List<FayCoreTickQueue.Task> TASKS = new ArrayList<>();

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         Iterator<FayCoreTickQueue.Task> iterator = TASKS.iterator();

         while (iterator.hasNext()) {
            FayCoreTickQueue.Task task = iterator.next();
            task.ticks--;
            if (task.ticks <= 0) {
               task.run.run();
               iterator.remove();
            }
         }
      });
   }

   public static void runLater(int ticks, Runnable runnable) {
      TASKS.add(new FayCoreTickQueue.Task(ticks, runnable));
   }

   private static class Task {
      int ticks;
      Runnable run;

      Task(int ticks, Runnable run) {
         this.ticks = ticks;
         this.run = run;
      }
   }
}
