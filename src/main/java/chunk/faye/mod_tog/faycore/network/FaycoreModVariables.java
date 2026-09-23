package chunk.faye.mod_tog.faycore.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents.AfterPlayerChange;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.Join;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndLevelTick;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class FaycoreModVariables {
   public static void variablesLoad() {
      PayloadTypeRegistry.clientboundPlay().register(FaycoreModVariables.SavedDataSyncMessage.TYPE, FaycoreModVariables.SavedDataSyncMessage.STREAM_CODEC);
      ServerPlayerEvents.JOIN.register((Join)player -> {
         SavedData mapdata = FaycoreModVariables.MapVariables.get(player.level());
         SavedData worlddata = FaycoreModVariables.WorldVariables.get(player.level());
         if (mapdata != null) {
            ServerPlayNetworking.send(player, new FaycoreModVariables.SavedDataSyncMessage(0, mapdata));
         }

         if (worlddata != null) {
            ServerPlayNetworking.send(player, new FaycoreModVariables.SavedDataSyncMessage(1, worlddata));
         }
      });
      ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((AfterPlayerChange)(player, origin, destination) -> {
         if (!destination.isClientSide()) {
            SavedData worlddata = FaycoreModVariables.WorldVariables.get(player.level());
            if (worlddata != null) {
               ServerPlayNetworking.send(player, new FaycoreModVariables.SavedDataSyncMessage(1, worlddata));
            }
         }
      });
      ServerTickEvents.END_LEVEL_TICK.register((EndLevelTick)level -> {
         FaycoreModVariables.WorldVariables worldVariables = FaycoreModVariables.WorldVariables.get(level);
         if (worldVariables._syncDirty) {
            level.players().forEach(player -> ServerPlayNetworking.send(player, new FaycoreModVariables.SavedDataSyncMessage(1, worldVariables)));
            worldVariables._syncDirty = false;
         }

         FaycoreModVariables.MapVariables mapVariables = FaycoreModVariables.MapVariables.get(level);
         if (mapVariables._syncDirty) {
            PlayerLookup.level(level).forEach(player -> ServerPlayNetworking.send(player, new FaycoreModVariables.SavedDataSyncMessage(0, mapVariables)));
            mapVariables._syncDirty = false;
         }
      });
   }

   public static class MapVariables extends SavedData {
      public static final SavedDataType<FaycoreModVariables.MapVariables> TYPE = new SavedDataType(
         Identifier.parse("faycore:mapvars"), FaycoreModVariables.MapVariables::new, CompoundTag.CODEC.xmap(tag -> {
            FaycoreModVariables.MapVariables instance = new FaycoreModVariables.MapVariables();
            instance.read(tag);
            return instance;
         }, instance -> instance.save(new CompoundTag())), null
      );
      boolean _syncDirty = false;
      public boolean CorePreview = false;
      static FaycoreModVariables.MapVariables clientSide = new FaycoreModVariables.MapVariables();

      public void read(CompoundTag nbt) {
         this.CorePreview = nbt.getBooleanOr("CorePreview", false);
      }

      public CompoundTag save(CompoundTag nbt) {
         nbt.putBoolean("CorePreview", this.CorePreview);
         return nbt;
      }

      public void markSyncDirty() {
         this.setDirty();
         this._syncDirty = true;
      }

      public static FaycoreModVariables.MapVariables get(LevelAccessor world) {
         return world instanceof ServerLevelAccessor serverLevelAccessor
            ? (FaycoreModVariables.MapVariables)serverLevelAccessor.getLevel().getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(TYPE)
            : clientSide;
      }
   }

   public static record SavedDataSyncMessage(int dataType, SavedData data) implements CustomPacketPayload {
      public static final Type<FaycoreModVariables.SavedDataSyncMessage> TYPE = new Type(Identifier.fromNamespaceAndPath("faycore", "saved_data_sync"));
      public static final StreamCodec<RegistryFriendlyByteBuf, FaycoreModVariables.SavedDataSyncMessage> STREAM_CODEC = StreamCodec.of((buffer, message) -> {
         buffer.writeInt(message.dataType);
         if (message.data instanceof FaycoreModVariables.MapVariables mapVariables) {
            buffer.writeNbt(mapVariables.save(new CompoundTag()));
         } else if (message.data instanceof FaycoreModVariables.WorldVariables worldVariables) {
            buffer.writeNbt(worldVariables.save(new CompoundTag()));
         }
      }, buffer -> {
         int dataType = buffer.readInt();
         CompoundTag nbt = buffer.readNbt();
         SavedData data = null;
         if (nbt != null) {
            data = (SavedData)(dataType == 0 ? new FaycoreModVariables.MapVariables() : new FaycoreModVariables.WorldVariables());
            if (data instanceof FaycoreModVariables.MapVariables mapVariables) {
               mapVariables.read(nbt);
            } else if (data instanceof FaycoreModVariables.WorldVariables worldVariables) {
               worldVariables.read(nbt);
            }
         }

         return new FaycoreModVariables.SavedDataSyncMessage(dataType, data);
      });

      public Type<FaycoreModVariables.SavedDataSyncMessage> type() {
         return TYPE;
      }

      public static void handleData(FaycoreModVariables.SavedDataSyncMessage message, Context context) {
         if (message.data != null) {
            context.client().execute(() -> {
               if (message.dataType == 0) {
                  FaycoreModVariables.MapVariables.clientSide.read(((FaycoreModVariables.MapVariables)message.data).save(new CompoundTag()));
               } else {
                  FaycoreModVariables.WorldVariables.clientSide.read(((FaycoreModVariables.WorldVariables)message.data).save(new CompoundTag()));
               }
            });
         }
      }
   }

   public static class WorldVariables extends SavedData {
      public static final SavedDataType<FaycoreModVariables.WorldVariables> TYPE = new SavedDataType(
         Identifier.parse("faycore:worldvars"), FaycoreModVariables.WorldVariables::new, CompoundTag.CODEC.xmap(tag -> {
            FaycoreModVariables.WorldVariables instance = new FaycoreModVariables.WorldVariables();
            instance.read(tag);
            return instance;
         }, instance -> instance.save(new CompoundTag())), null
      );
      boolean _syncDirty = false;
      static FaycoreModVariables.WorldVariables clientSide = new FaycoreModVariables.WorldVariables();

      public void read(CompoundTag nbt) {
      }

      public CompoundTag save(CompoundTag nbt) {
         return nbt;
      }

      public void markSyncDirty() {
         this.setDirty();
         this._syncDirty = true;
      }

      public static FaycoreModVariables.WorldVariables get(LevelAccessor world) {
         return world instanceof ServerLevel level ? (FaycoreModVariables.WorldVariables)level.getDataStorage().computeIfAbsent(TYPE) : clientSide;
      }
   }
}
