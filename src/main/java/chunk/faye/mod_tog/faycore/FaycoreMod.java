package chunk.faye.mod_tog.faycore;

import chunk.faye.mod_tog.faycore.init.FaycoreModKeyMappingsServer;
import chunk.faye.mod_tog.faycore.init.FaycoreModMenus;
import chunk.faye.mod_tog.faycore.network.FaycoreModVariables;
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
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.TickTask;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class FaycoreMod implements ModInitializer {
   public static final Logger LOGGER = LogManager.getLogger(FaycoreMod.class);
   public static final String MODID = "faycore";
   private static final Queue<IntObjectPair<Runnable>> workToBeScheduled = new ConcurrentLinkedQueue<>();
   private static final PriorityQueue<TickTask> workQueue = new PriorityQueue<>(Comparator.comparingInt(TickTask::getTick));
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
      workToBeScheduled.add(new IntObjectImmutablePair(delay, action));
   }

   private void tick() {
      ServerTickEvents.END_SERVER_TICK.register((EndTick)server -> {
         int currentTick = server.getTickCount();

         IntObjectPair<Runnable> work;
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
      if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
         return null;
      } else {
         try {
            if (minecraft == null || playerHandle == null) {
               Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
               minecraft = (Object)MethodHandles.publicLookup().findStatic(minecraftClass, "getInstance", MethodType.methodType(minecraftClass)).invoke();
               playerHandle = MethodHandles.publicLookup().findGetter(minecraftClass, "player", Class.forName("net.minecraft.client.player.LocalPlayer"));
            }

            return (Player)playerHandle.invoke((Object)minecraft);
         } catch (Throwable var1) {
            LOGGER.error("Failed to get client player", var1);
            return null;
         }
      }
   }
}
