package com.mirai.dynamicportals.network;

import com.mirai.dynamicportals.progress.IProgressData;
import com.mirai.dynamicportals.util.ModConstants;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record SyncProgressPacket(
        Map<EntityType<?>, Boolean> killedMobs,
        Set<Item> obtainedItems,
        Set<ResourceLocation> unlockedAchievements,
        boolean isGlobal
) implements CustomPacketPayload {

    public static final Type<SyncProgressPacket> TYPE = new Type<>(ModConstants.id("sync_progress"));

    public static final StreamCodec<ByteBuf, SyncProgressPacket> STREAM_CODEC = StreamCodec.composite(
            new StreamCodec<>() {
                @Override
                public Map<EntityType<?>, Boolean> decode(ByteBuf buffer) {
                    int size = ByteBufCodecs.VAR_INT.decode(buffer);
                    Map<EntityType<?>, Boolean> map = new HashMap<>();
                    for (int i = 0; i < size; i++) {
                        ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buffer);
                        boolean killed = ByteBufCodecs.BOOL.decode(buffer);
                        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(id);
                        if (entityType != null) {
                            map.put(entityType, killed);
                        }
                    }
                    return map;
                }

                @Override
                public void encode(ByteBuf buffer, Map<EntityType<?>, Boolean> map) {
                    ByteBufCodecs.VAR_INT.encode(buffer, map.size());
                    for (Map.Entry<EntityType<?>, Boolean> entry : map.entrySet()) {
                        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entry.getKey());
                        if (id == null) {
                            throw new IllegalStateException("Cannot encode EntityType with null registry key: " + entry.getKey());
                        }
                        ResourceLocation.STREAM_CODEC.encode(buffer, id);
                        ByteBufCodecs.BOOL.encode(buffer, entry.getValue());
                    }
                }
            },
            SyncProgressPacket::killedMobs,
            new StreamCodec<>() {
                @Override
                public Set<Item> decode(ByteBuf buffer) {
                    int size = ByteBufCodecs.VAR_INT.decode(buffer);
                    Set<Item> set = new HashSet<>();
                    for (int i = 0; i < size; i++) {
                        ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buffer);
                        Item item = BuiltInRegistries.ITEM.get(id);
                        if (item != null) {
                            set.add(item);
                        }
                    }
                    return set;
                }

                @Override
                public void encode(ByteBuf buffer, Set<Item> set) {
                    ByteBufCodecs.VAR_INT.encode(buffer, set.size());
                    for (Item item : set) {
                        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                        if (id == null) {
                            throw new IllegalStateException("Cannot encode Item with null registry key: " + item);
                        }
                        ResourceLocation.STREAM_CODEC.encode(buffer, id);
                    }
                }
            },
            SyncProgressPacket::obtainedItems,
            new StreamCodec<>() {
                @Override
                public Set<ResourceLocation> decode(ByteBuf buffer) {
                    int size = ByteBufCodecs.VAR_INT.decode(buffer);
                    Set<ResourceLocation> set = new HashSet<>();
                    for (int i = 0; i < size; i++) {
                        set.add(ResourceLocation.STREAM_CODEC.decode(buffer));
                    }
                    return set;
                }

                @Override
                public void encode(ByteBuf buffer, Set<ResourceLocation> set) {
                    ByteBufCodecs.VAR_INT.encode(buffer, set.size());
                    for (ResourceLocation id : set) {
                        ResourceLocation.STREAM_CODEC.encode(buffer, id);
                    }
                }
            },
            SyncProgressPacket::unlockedAchievements,
            ByteBufCodecs.BOOL,
            SyncProgressPacket::isGlobal,
            SyncProgressPacket::new
    );

    public static SyncProgressPacket fromProgressData(IProgressData data) {
        return new SyncProgressPacket(
                new HashMap<>(data.getKilledMobs()),
                new HashSet<>(data.getObtainedItems()),
                new HashSet<>(data.getUnlockedAchievements()),
                data.isGlobal()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
