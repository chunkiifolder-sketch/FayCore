/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking$Context
 *  net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents
 *  net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
 *  net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
 *  net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
 *  net.fabricmc.fabric.api.networking.v1.PlayerLookup
 *  net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.ServerLevelAccessor
 *  net.minecraft.world.level.saveddata.SavedData
 *  net.minecraft.world.level.saveddata.SavedDataType
 */
package chunk.faye.mod_tog.faycore.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class FaycoreModVariables {
    public static void variablesLoad() {
        PayloadTypeRegistry.clientboundPlay().register(SavedDataSyncMessage.TYPE, SavedDataSyncMessage.STREAM_CODEC);
        ServerPlayerEvents.JOIN.register(player -> {
            MapVariables mapdata = MapVariables.get((LevelAccessor)player.level());
            WorldVariables worlddata = WorldVariables.get((LevelAccessor)player.level());
            if (mapdata != null) {
                ServerPlayNetworking.send((ServerPlayer)player, (CustomPacketPayload)new SavedDataSyncMessage(0, mapdata));
            }
            if (worlddata != null) {
                ServerPlayNetworking.send((ServerPlayer)player, (CustomPacketPayload)new SavedDataSyncMessage(1, worlddata));
            }
        });
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> {
            WorldVariables worlddata;
            if (!destination.isClientSide() && (worlddata = WorldVariables.get((LevelAccessor)player.level())) != null) {
                ServerPlayNetworking.send((ServerPlayer)player, (CustomPacketPayload)new SavedDataSyncMessage(1, worlddata));
            }
        });
        ServerTickEvents.END_LEVEL_TICK.register(level -> {
            WorldVariables worldVariables = WorldVariables.get((LevelAccessor)level);
            if (worldVariables._syncDirty) {
                level.players().forEach(player -> ServerPlayNetworking.send((ServerPlayer)player, (CustomPacketPayload)new SavedDataSyncMessage(1, worldVariables)));
                worldVariables._syncDirty = false;
            }
            MapVariables mapVariables = MapVariables.get((LevelAccessor)level);
            if (mapVariables._syncDirty) {
                PlayerLookup.level((ServerLevel)level).forEach(player -> ServerPlayNetworking.send((ServerPlayer)player, (CustomPacketPayload)new SavedDataSyncMessage(0, mapVariables)));
                mapVariables._syncDirty = false;
            }
        });
    }

    public record SavedDataSyncMessage(int dataType, SavedData data) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<SavedDataSyncMessage> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"faycore", (String)"saved_data_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SavedDataSyncMessage> STREAM_CODEC = StreamCodec.of((buffer, message) -> {
            buffer.writeInt(message.dataType);
            SavedData patt0$temp = message.data;
            if (patt0$temp instanceof MapVariables) {
                MapVariables mapVariables = (MapVariables)patt0$temp;
                buffer.writeNbt((Tag)mapVariables.save(new CompoundTag()));
            } else {
                SavedData patt1$temp = message.data;
                if (patt1$temp instanceof WorldVariables) {
                    WorldVariables worldVariables = (WorldVariables)patt1$temp;
                    buffer.writeNbt((Tag)worldVariables.save(new CompoundTag()));
                }
            }
        }, buffer -> {
            int dataType = buffer.readInt();
            CompoundTag nbt = buffer.readNbt();
            SavedData data = null;
            if (nbt != null) {
                SavedData savedData = data = dataType == 0 ? new MapVariables() : new WorldVariables();
                if (data instanceof MapVariables) {
                    MapVariables mapVariables = (MapVariables)data;
                    mapVariables.read(nbt);
                } else if (data instanceof WorldVariables) {
                    WorldVariables worldVariables = (WorldVariables)data;
                    worldVariables.read(nbt);
                }
            }
            return new SavedDataSyncMessage(dataType, data);
        });

        public CustomPacketPayload.Type<SavedDataSyncMessage> type() {
            return TYPE;
        }

        public static void handleData(SavedDataSyncMessage message, ClientPlayNetworking.Context context) {
            if (message.data != null) {
                context.client().execute(() -> {
                    if (message.dataType == 0) {
                        MapVariables.clientSide.read(((MapVariables)message.data).save(new CompoundTag()));
                    } else {
                        WorldVariables.clientSide.read(((WorldVariables)message.data).save(new CompoundTag()));
                    }
                });
            }
        }
    }

    public static class WorldVariables
    extends SavedData {
        public static final SavedDataType<WorldVariables> TYPE = new SavedDataType(Identifier.parse((String)"faycore:worldvars"), WorldVariables::new, CompoundTag.CODEC.xmap(tag -> {
            WorldVariables instance = new WorldVariables();
            instance.read((CompoundTag)tag);
            return instance;
        }, instance -> instance.save(new CompoundTag())), null);
        boolean _syncDirty = false;
        static WorldVariables clientSide = new WorldVariables();

        public void read(CompoundTag nbt) {
        }

        public CompoundTag save(CompoundTag nbt) {
            return nbt;
        }

        public void markSyncDirty() {
            this.setDirty();
            this._syncDirty = true;
        }

        public static WorldVariables get(LevelAccessor world) {
            if (world instanceof ServerLevel) {
                ServerLevel level = (ServerLevel)world;
                return (WorldVariables)level.getDataStorage().computeIfAbsent(TYPE);
            }
            return clientSide;
        }
    }

    public static class MapVariables
    extends SavedData {
        public static final SavedDataType<MapVariables> TYPE = new SavedDataType(Identifier.parse((String)"faycore:mapvars"), MapVariables::new, CompoundTag.CODEC.xmap(tag -> {
            MapVariables instance = new MapVariables();
            instance.read((CompoundTag)tag);
            return instance;
        }, instance -> instance.save(new CompoundTag())), null);
        boolean _syncDirty = false;
        public boolean CorePreview = false;
        static MapVariables clientSide = new MapVariables();

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

        public static MapVariables get(LevelAccessor world) {
            if (world instanceof ServerLevelAccessor) {
                ServerLevelAccessor serverLevelAccessor = (ServerLevelAccessor)world;
                return (MapVariables)serverLevelAccessor.getLevel().getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(TYPE);
            }
            return clientSide;
        }
    }
}

