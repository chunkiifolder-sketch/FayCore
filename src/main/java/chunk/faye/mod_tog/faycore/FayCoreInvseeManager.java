/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundChatCommandPacket
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
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
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class FayCoreInvseeManager {
    public static String targetPlayerName = "";
    public static final Map<Integer, ItemStack> virtualInventory = new HashMap<Integer, ItemStack>();
    public static final List<Component> currentHoverTooltip = new ArrayList<Component>();
    private static long lastFullRefresh = 0L;

    public static void tickRefresh() {
        if (targetPlayerName.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastFullRefresh > 3000L) {
            lastFullRefresh = now;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.connection != null) {
                mc.player.connection.send((Packet)new ServerboundChatCommandPacket("data get entity " + targetPlayerName + " Inventory"));
            }
        }
    }

    public static void requestSingleSlotData(int slot) {
        if (targetPlayerName.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.connection != null) {
            mc.player.connection.send((Packet)new ServerboundChatCommandPacket("data get entity " + targetPlayerName + " Inventory[{Slot:" + slot + "b}]"));
        }
    }

    public static void parseServerInventoryNbt(String rawMessage) {
        String[] items;
        boolean isSingleSlotData;
        boolean bl = isSingleSlotData = ((String)rawMessage).contains("Slot:") && !((String)rawMessage).contains("], {");
        if (isSingleSlotData) {
            currentHoverTooltip.clear();
            try {
                int startQuote;
                int endQuote;
                String lowerMsg = ((String)rawMessage).toLowerCase();
                if (lowerMsg.contains("id:\"")) {
                    int idS = lowerMsg.indexOf("id:\"") + 4;
                    int idE = lowerMsg.indexOf("\"", idS);
                    String idStr = ((String)rawMessage).substring(idS, idE).replace("\"", "");
                    currentHoverTooltip.add((Component)Component.literal((String)("\u00a7d" + idStr.substring(idStr.indexOf(":") + 1).toUpperCase())));
                    currentHoverTooltip.add((Component)Component.literal((String)("\u00a78" + idStr)));
                }
                int textIdx = 0;
                while ((textIdx = lowerMsg.indexOf("\"text\":\"", textIdx)) != -1 && (endQuote = ((String)rawMessage).indexOf("\"", startQuote = textIdx + 8)) != -1) {
                    String cleanText = ((String)rawMessage).substring(startQuote, endQuote);
                    if (!cleanText.contains("%1$s") && !cleanText.isEmpty()) {
                        currentHoverTooltip.add((Component)Component.literal((String)("\u00a7a" + cleanText)));
                    }
                    textIdx = endQuote;
                }
                if (lowerMsg.contains("components:")) {
                    currentHoverTooltip.add((Component)Component.literal((String)"\u00a7512 component(s) (FayCore \u7269\u7406\u6d17\u6de8)"));
                }
            }
            catch (Exception lowerMsg) {
                // empty catch block
            }
        }
        int start = ((String)rawMessage).indexOf("[");
        int end = ((String)rawMessage).lastIndexOf("]");
        if (start == -1 || end == -1) {
            if (((String)rawMessage).contains("Slot:")) {
                start = ((String)rawMessage).indexOf("{");
                end = ((String)rawMessage).lastIndexOf("}") + 1;
                if (start == -1 || end == 0) {
                    return;
                }
                rawMessage = "[" + ((String)rawMessage).substring(start, end) + "]";
                start = 0;
                end = ((String)rawMessage).length() - 1;
            } else {
                return;
            }
        }
        String nbtArrayStr = ((String)rawMessage).substring(start + 1, end);
        for (String itemStr : items = nbtArrayStr.split("(?<=\\}),")) {
            try {
                int quoteEnd;
                String lowerItemStr = itemStr.toLowerCase();
                if (!lowerItemStr.contains("slot:")) continue;
                int slotStart = lowerItemStr.indexOf("slot:") + 5;
                int slotEnd = lowerItemStr.indexOf("b", slotStart);
                int slot = Integer.parseInt(itemStr.substring(slotStart, slotEnd).trim());
                if (!lowerItemStr.contains("id:")) continue;
                int idStart = lowerItemStr.indexOf("id:");
                int quoteStart = itemStr.indexOf("\"", idStart);
                if (quoteStart == -1) {
                    quoteStart = itemStr.indexOf("'", idStart);
                }
                String itemId = "";
                if (quoteStart != -1 && (quoteEnd = itemStr.indexOf("\"", quoteStart + 1)) != -1) {
                    itemId = itemStr.substring(quoteStart + 1, quoteEnd).trim();
                }
                itemId = itemId.replace("\"", "").replace("'", "").trim();
                int count = 1;
                if (lowerItemStr.contains("count:")) {
                    String countStr;
                    int countStart = lowerItemStr.indexOf("count:") + 6;
                    int countEnd = itemStr.indexOf(",", countStart);
                    if (countEnd == -1) {
                        countEnd = itemStr.indexOf("}", countStart);
                    }
                    if (!(countStr = itemStr.substring(countStart, countEnd).trim().replaceAll("[^0-9]", "")).isEmpty()) {
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
                    Optional itemOpt = (Optional)getOptMethod.invoke((Object)BuiltInRegistries.ITEM, resourceLocationInstance);
                    itemOpt.ifPresent(itemObj -> virtualInventory.put(finalSlot, new ItemStack((ItemLike)((Item)itemObj), finalCount)));
                }
                catch (Throwable t) {
                    for (Item item : BuiltInRegistries.ITEM) {
                        if (!item.toString().equals(finalItemId)) continue;
                        virtualInventory.put(finalSlot, new ItemStack((ItemLike)item, finalCount));
                    }
                    continue;
                }
                {
                    break;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }
}

