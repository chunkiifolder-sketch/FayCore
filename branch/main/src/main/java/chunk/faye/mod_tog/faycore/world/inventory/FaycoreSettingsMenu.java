/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
 *  net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.Container
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerLevelAccess
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package chunk.faye.mod_tog.faycore.world.inventory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import chunk.faye.mod_tog.faycore.init.FaycoreModMenus;
import chunk.faye.mod_tog.faycore.network.FaycoreSettingsButtonMessage;
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

public class FaycoreSettingsMenu
extends AbstractContainerMenu
implements FaycoreModMenus.MenuAccessor {
    public final Map<String, Object> menuState = new HashMap<String, Object>(this){
        final /* synthetic */ FaycoreSettingsMenu this$0;
        {
            FaycoreSettingsMenu faycoreSettingsMenu = this$0;
            Objects.requireNonNull(faycoreSettingsMenu);
            this.this$0 = faycoreSettingsMenu;
        }

        @Override
        public Object put(String key, Object value) {
            if (!this.containsKey(key) && this.size() >= 2) {
                return null;
            }
            return super.put(key, value);
        }
    };
    public final Level world;
    public final Player entity;
    public int x;
    public int y;
    public int z;
    private ContainerLevelAccess access = ContainerLevelAccess.NULL;
    private final Container inventory;
    private final Map<Integer, Slot> customSlots = new HashMap<Integer, Slot>();
    private boolean bound = false;
    private Supplier<Boolean> boundItemMatcher = null;
    private ItemStack boundItem = null;

    public FaycoreSettingsMenu(int id, Inventory inv) {
        this(id, inv, (Container)new SimpleContainer(0));
        this.x = (int)inv.player.getX();
        this.y = (int)inv.player.getY();
        this.z = (int)inv.player.getZ();
        this.access = ContainerLevelAccess.create((Level)inv.player.level(), (BlockPos)new BlockPos(this.x, this.y, this.z));
    }

    public FaycoreSettingsMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, (Container)new SimpleContainer(0), extraData);
    }

    public FaycoreSettingsMenu(int id, Inventory inv, Container container, FriendlyByteBuf extraData) {
        this(id, inv, container);
        BlockPos pos = null;
        if (extraData != null) {
            pos = extraData.readBlockPos();
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            this.access = ContainerLevelAccess.create((Level)this.world, (BlockPos)pos);
        }
    }

    public FaycoreSettingsMenu(int id, Inventory inv, Container container) {
        super(FaycoreModMenus.FAYCORE_SETTINGS, id);
        this.entity = inv.player;
        this.world = inv.player.level();
        this.inventory = container;
    }

    public boolean stillValid(Player player) {
        if (this.bound && this.boundItemMatcher != null) {
            return this.boundItemMatcher.get();
        }
        return this.inventory.stillValid(player);
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

