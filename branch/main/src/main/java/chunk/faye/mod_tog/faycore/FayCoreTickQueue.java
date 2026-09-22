/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 */
package chunk.faye.mod_tog.faycore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class FayCoreTickQueue {
    private static final List<Task> TASKS = new ArrayList<Task>();

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Iterator<Task> iterator = TASKS.iterator();
            while (iterator.hasNext()) {
                Task task = iterator.next();
                --task.ticks;
                if (task.ticks > 0) continue;
                task.run.run();
                iterator.remove();
            }
        });
    }

    public static void runLater(int ticks, Runnable runnable) {
        TASKS.add(new Task(ticks, runnable));
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

