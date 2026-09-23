package chunk.faye.mod_tog.faycore.world.inventory;

import chunk.faye.mod_tog.faycore.init.FaycoreModMenus;
import chunk.faye.mod_tog.faycore.network.FaycoreSettingsButtonMessage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FaycoreSettingsMenu extends AbstractContainerMenu implements FaycoreModMenus.MenuAccessor {
   public final Map<String, Object> menuState = new HashMap<String, Object>() {
      {
         Objects.requireNonNull(FaycoreSettingsMenu.this);
      }

      public Object put(String key, Object value) {
         return !this.containsKey(key) && this.size() >= 2 ? null : super.put(key, value);
      }
   };
   public final Level world;
   public final Player entity;
   public int x;
   public int y;
   public int z;
   private ContainerLevelAccess access = ContainerLevelAccess.NULL;
   private final Container inventory;
   private final Map<Integer, Slot> customSlots = new HashMap<>();
   private boolean bound = false;
   private Supplier<Boolean> boundItemMatcher = null;
   private ItemStack boundItem = null;

   public FaycoreSettingsMenu(int id, Inventory inv) {
      this(id, inv, new SimpleContainer(0));
      this.x = (int)inv.player.getX();
      this.y = (int)inv.player.getY();
      this.z = (int)inv.player.getZ();
      this.access = ContainerLevelAccess.create(inv.player.level(), new BlockPos(this.x, this.y, this.z));
   }

   public FaycoreSettingsMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
      this(id, inv, new SimpleContainer(0), extraData);
   }

   public FaycoreSettingsMenu(int id, Inventory inv, Container container, FriendlyByteBuf extraData) {
      this(id, inv, container);
      BlockPos pos = null;
      if (extraData != null) {
         pos = extraData.readBlockPos();
         this.x = pos.getX();
         this.y = pos.getY();
         this.z = pos.getZ();
         this.access = ContainerLevelAccess.create(this.world, pos);
      }
   }

   public FaycoreSettingsMenu(int id, Inventory inv, Container container) {
      super(FaycoreModMenus.FAYCORE_SETTINGS, id);
      this.entity = inv.player;
      this.world = inv.player.level();
      this.inventory = container;
   }

   public boolean stillValid(Player player) {
      return this.bound && this.boundItemMatcher != null ? this.boundItemMatcher.get() : this.inventory.stillValid(player);
   }

   public ItemStack quickMoveStack(Player playerIn, int index) {
      return ItemStack.EMPTY;
   }

   @Override
   public Map<Integer, Slot> getSlots() {
      return Collections.unmodifiableMap(this.customSlots);
   }

   @Override
   public Map<String, Object> getMenuState() {
      return this.menuState;
   }

   public static void screenInit() {
      PayloadTypeRegistry.serverboundPlay().register(FaycoreSettingsButtonMessage.TYPE, FaycoreSettingsButtonMessage.STREAM_CODEC);
      ServerPlayNetworking.registerGlobalReceiver(FaycoreSettingsButtonMessage.TYPE, FaycoreSettingsButtonMessage::handleData);
   }
}
