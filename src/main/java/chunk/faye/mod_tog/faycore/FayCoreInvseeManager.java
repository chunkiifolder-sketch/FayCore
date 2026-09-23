package chunk.faye.mod_tog.faycore;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FayCoreInvseeManager {
   public static String targetPlayerName = "";
   public static final Map<Integer, ItemStack> virtualInventory = new HashMap<>();
   public static final List<Component> currentHoverTooltip = new ArrayList<>();
   private static long lastFullRefresh = 0L;

   public static void tickRefresh() {
      if (!targetPlayerName.isEmpty()) {
         long now = System.currentTimeMillis();
         if (now - lastFullRefresh > 3000L) {
            lastFullRefresh = now;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.connection != null) {
               mc.player.connection.send(new ServerboundChatCommandPacket("data get entity " + targetPlayerName + " Inventory"));
            }
         }
      }
   }

   public static void requestSingleSlotData(int slot) {
      if (!targetPlayerName.isEmpty()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && mc.player.connection != null) {
            mc.player.connection.send(new ServerboundChatCommandPacket("data get entity " + targetPlayerName + " Inventory[{Slot:" + slot + "b}]"));
         }
      }
   }

   public static void parseServerInventoryNbt(String rawMessage) {
      boolean isSingleSlotData = rawMessage.contains("Slot:") && !rawMessage.contains("], {");
      if (isSingleSlotData) {
         currentHoverTooltip.clear();

         try {
            String lowerMsg = rawMessage.toLowerCase();
            if (lowerMsg.contains("id:\"")) {
               int idS = lowerMsg.indexOf("id:\"") + 4;
               int idE = lowerMsg.indexOf("\"", idS);
               String idStr = rawMessage.substring(idS, idE).replace("\"", "");
               currentHoverTooltip.add(Component.literal("§d" + idStr.substring(idStr.indexOf(":") + 1).toUpperCase()));
               currentHoverTooltip.add(Component.literal("§8" + idStr));
            }

            int textIdx = 0;

            while ((textIdx = lowerMsg.indexOf("\"text\":\"", textIdx)) != -1) {
               int startQuote = textIdx + 8;
               int endQuote = rawMessage.indexOf("\"", startQuote);
               if (endQuote == -1) {
                  break;
               }

               String cleanText = rawMessage.substring(startQuote, endQuote);
               if (!cleanText.contains("%1$s") && !cleanText.isEmpty()) {
                  currentHoverTooltip.add(Component.literal("§a" + cleanText));
               }

               textIdx = endQuote;
            }

            if (lowerMsg.contains("components:")) {
               currentHoverTooltip.add(Component.literal("§512 component(s) (FayCore 物理洗淨)"));
            }
         } catch (Exception var28) {
         }
      }

      int start = rawMessage.indexOf("[");
      int end = rawMessage.lastIndexOf("]");
      if (start == -1 || end == -1) {
         if (!rawMessage.contains("Slot:")) {
            return;
         }

         start = rawMessage.indexOf("{");
         end = rawMessage.lastIndexOf("}") + 1;
         if (start == -1 || end == 0) {
            return;
         }

         rawMessage = "[" + rawMessage.substring(start, end) + "]";
         start = 0;
         end = rawMessage.length() - 1;
      }

      String nbtArrayStr = rawMessage.substring(start + 1, end);
      String[] items = nbtArrayStr.split("(?<=\\}),");

      for (String itemStr : items) {
         try {
            String lowerItemStr = itemStr.toLowerCase();
            if (lowerItemStr.contains("slot:")) {
               int slotStart = lowerItemStr.indexOf("slot:") + 5;
               int slotEnd = lowerItemStr.indexOf("b", slotStart);
               int slot = Integer.parseInt(itemStr.substring(slotStart, slotEnd).trim());
               if (lowerItemStr.contains("id:")) {
                  int idStart = lowerItemStr.indexOf("id:");
                  int quoteStart = itemStr.indexOf("\"", idStart);
                  if (quoteStart == -1) {
                     quoteStart = itemStr.indexOf("'", idStart);
                  }

                  String itemId = "";
                  if (quoteStart != -1) {
                     int quoteEnd = itemStr.indexOf("\"", quoteStart + 1);
                     if (quoteEnd != -1) {
                        itemId = itemStr.substring(quoteStart + 1, quoteEnd).trim();
                     }
                  }

                  itemId = itemId.replace("\"", "").replace("'", "").trim();
                  int count = 1;
                  if (lowerItemStr.contains("count:")) {
                     int countStart = lowerItemStr.indexOf("count:") + 6;
                     int countEnd = itemStr.indexOf(",", countStart);
                     if (countEnd == -1) {
                        countEnd = itemStr.indexOf("}", countStart);
                     }

                     String countStr = itemStr.substring(countStart, countEnd).trim().replaceAll("[^0-9]", "");
                     if (!countStr.isEmpty()) {
                        count = Integer.parseInt(countStr);
                     }
                  }

                  int finalCount = count;
                  int finalSlot = slot;
                  String finalItemId = itemId;

                  try {
                     Class<?> resLocClass = Class.forName("net.minecraft.resources.ResourceLocation");
                     Method parseMethod = resLocClass.getMethod("parse", String.class);
                     Object resourceLocationInstance = parseMethod.invoke(null, finalItemId);
                     Method getOptMethod = BuiltInRegistries.ITEM.getClass().getMethod("getOptional", resLocClass);
                     Optional<?> itemOpt = (Optional<?>)getOptMethod.invoke(BuiltInRegistries.ITEM, resourceLocationInstance);
                     itemOpt.ifPresent(itemObj -> virtualInventory.put(finalSlot, new ItemStack((Item)itemObj, finalCount)));
                  } catch (Throwable var26) {
                     for (Item item : BuiltInRegistries.ITEM) {
                        if (item.toString().equals(finalItemId)) {
                           virtualInventory.put(slot, new ItemStack(item, finalCount));
                           break;
                        }
                     }
                  }
               }
            }
         } catch (Exception var27) {
         }
      }
   }
}
