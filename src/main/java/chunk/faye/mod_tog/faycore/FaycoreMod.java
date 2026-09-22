/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntObjectImmutablePair
 *  it.unimi.dsi.fastutil.ints.IntObjectPair
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.ModInitializer
 *  net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.server.TickTask
 *  net.minecraft.world.entity.player.Player
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  org.jetbrains.annotations.Nullable
 */
package chunk.faye.mod_tog.faycore;

import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import chunk.faye.mod_tog.faycore.init.FaycoreModKeyMappingsServer;
import chunk.faye.mod_tog.faycore.init.FaycoreModMenus;
import chunk.faye.mod_tog.faycore.network.FaycoreModVariables;
import net.minecraft.server.TickTask;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class FaycoreMod
implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger(FaycoreMod.class);
    public static final String MODID = "faycore";
    private static final Queue<IntObjectPair<Runnable>> workToBeScheduled = new ConcurrentLinkedQueue<IntObjectPair<Runnable>>();
    private static final PriorityQueue<TickTask> workQueue = new PriorityQueue<TickTask>(Comparator.comparingInt(TickTask::getTick));
    private static Object minecraft;
    private static MethodHandle playerHandle;

    public void onInitialize() {
        LOGGER.info("Initializing FaycoreMod");
        FaycoreModVariables.variablesLoad();
        FaycoreModMenus.load();
        FaycoreModKeyMappingsServer.serverLoad();
        this.tick();
    }

    public static void queueServerWork(int delay, Runnable action) {
        workToBeScheduled.add((IntObjectPair<Runnable>)new IntObjectImmutablePair(delay, (Object)action));
    }

    private void tick() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            IntObjectPair<Runnable> work;
            int currentTick = server.getTickCount();
            while ((work = workToBeScheduled.poll()) != null) {
                workQueue.add(new TickTask(currentTick + work.leftInt(), (Runnable)work.right()));
            }
            while (!workQueue.isEmpty() && currentTick >= workQueue.peek().getTick()) {
                workQueue.poll().run();
            }
        });
    }

    @Nullable
    public static Player clientPlayer() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            try {
                if (minecraft == null || playerHandle == null) {
                    Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
                    minecraft = MethodHandles.publicLookup().findStatic(minecraftClass, "getInstance", MethodType.methodType(minecraftClass)).invoke();
                    playerHandle = MethodHandles.publicLookup().findGetter(minecraftClass, "player", Class.forName("net.minecraft.client.player.LocalPlayer"));
                }
                return playerHandle.invoke(minecraft);
            }
            catch (Throwable e) {
                LOGGER.error("Failed to get client player", e);
                return null;
            }
        }
        return null;
    }
}

