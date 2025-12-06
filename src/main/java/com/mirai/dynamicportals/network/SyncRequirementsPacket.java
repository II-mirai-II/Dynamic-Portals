package com.mirai.dynamicportals.network;

import com.mirai.dynamicportals.util.ModConstants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public record SyncRequirementsPacket(
        Map<ResourceLocation, RequirementData> requirements
) implements CustomPacketPayload {

    public static final Type<SyncRequirementsPacket> TYPE = new Type<>(ModConstants.id("sync_requirements"));

    public static final StreamCodec<ByteBuf, SyncRequirementsPacket> STREAM_CODEC = new StreamCodec<ByteBuf, SyncRequirementsPacket>() {
        @Override
        public SyncRequirementsPacket decode(ByteBuf buffer) {
            int size = ByteBufCodecs.VAR_INT.decode(buffer);
            Map<ResourceLocation, RequirementData> map = new HashMap<>();
            
            for (int i = 0; i < size; i++) {
                ResourceLocation dimension = ResourceLocation.STREAM_CODEC.decode(buffer);
                
                // Decode advancement (nullable)
                boolean hasAdvancement = ByteBufCodecs.BOOL.decode(buffer);
                ResourceLocation advancement = hasAdvancement ? ResourceLocation.STREAM_CODEC.decode(buffer) : null;
                
                // Decode mobs list
                int mobsSize = ByteBufCodecs.VAR_INT.decode(buffer);
                List<ResourceLocation> mobs = new ArrayList<>();
                for (int j = 0; j < mobsSize; j++) {
                    mobs.add(ResourceLocation.STREAM_CODEC.decode(buffer));
                }
                
                // Decode bosses list
                int bossesSize = ByteBufCodecs.VAR_INT.decode(buffer);
                List<ResourceLocation> bosses = new ArrayList<>();
                for (int j = 0; j < bossesSize; j++) {
                    bosses.add(ResourceLocation.STREAM_CODEC.decode(buffer));
                }
                
                // Decode items list
                int itemsSize = ByteBufCodecs.VAR_INT.decode(buffer);
                List<ResourceLocation> items = new ArrayList<>();
                for (int j = 0; j < itemsSize; j++) {
                    items.add(ResourceLocation.STREAM_CODEC.decode(buffer));
                }
                
                // Decode display fields (nullable)
                boolean hasDisplayName = ByteBufCodecs.BOOL.decode(buffer);
                String displayName = hasDisplayName ? ByteBufCodecs.STRING_UTF8.decode(buffer) : null;
                
                boolean hasDisplayColor = ByteBufCodecs.BOOL.decode(buffer);
                Integer displayColor = hasDisplayColor ? ByteBufCodecs.VAR_INT.decode(buffer) : null;
                
                int sortOrder = ByteBufCodecs.VAR_INT.decode(buffer);
                
                map.put(dimension, new RequirementData(advancement, mobs, bosses, items, displayName, displayColor, sortOrder));
            }
            
            return new SyncRequirementsPacket(map);
        }

        @Override
        public void encode(ByteBuf buffer, SyncRequirementsPacket packet) {
            Map<ResourceLocation, RequirementData> map = packet.requirements();
            ByteBufCodecs.VAR_INT.encode(buffer, map.size());
            
            for (Map.Entry<ResourceLocation, RequirementData> entry : map.entrySet()) {
                ResourceLocation dimension = entry.getKey();
                RequirementData data = entry.getValue();
                
                // Encode dimension
                ResourceLocation.STREAM_CODEC.encode(buffer, dimension);
                
                // Encode advancement (nullable)
                ByteBufCodecs.BOOL.encode(buffer, data.advancement() != null);
                if (data.advancement() != null) {
                    ResourceLocation.STREAM_CODEC.encode(buffer, data.advancement());
                }
                
                // Encode mobs
                ByteBufCodecs.VAR_INT.encode(buffer, data.mobs().size());
                for (ResourceLocation mobId : data.mobs()) {
                    if (mobId == null) {
                        throw new IllegalStateException("Cannot encode null mob ResourceLocation");
                    }
                    ResourceLocation.STREAM_CODEC.encode(buffer, mobId);
                }
                
                // Encode bosses
                ByteBufCodecs.VAR_INT.encode(buffer, data.bosses().size());
                for (ResourceLocation bossId : data.bosses()) {
                    if (bossId == null) {
                        throw new IllegalStateException("Cannot encode null boss ResourceLocation");
                    }
                    ResourceLocation.STREAM_CODEC.encode(buffer, bossId);
                }
                
                // Encode items
                ByteBufCodecs.VAR_INT.encode(buffer, data.items().size());
                for (ResourceLocation itemId : data.items()) {
                    if (itemId == null) {
                        throw new IllegalStateException("Cannot encode null item ResourceLocation");
                    }
                    ResourceLocation.STREAM_CODEC.encode(buffer, itemId);
                }
                
                // Encode display fields (nullable)
                ByteBufCodecs.BOOL.encode(buffer, data.displayName() != null);
                if (data.displayName() != null) {
                    ByteBufCodecs.STRING_UTF8.encode(buffer, data.displayName());
                }
                
                ByteBufCodecs.BOOL.encode(buffer, data.displayColor() != null);
                if (data.displayColor() != null) {
                    ByteBufCodecs.VAR_INT.encode(buffer, data.displayColor());
                }
                
                ByteBufCodecs.VAR_INT.encode(buffer, data.sortOrder());
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Serializable requirement data using ResourceLocations instead of game objects.
     * Includes display customization fields for HUD rendering.
     */
    public record RequirementData(
            ResourceLocation advancement,
            List<ResourceLocation> mobs,
            List<ResourceLocation> bosses,
            List<ResourceLocation> items,
            String displayName,
            Integer displayColor,
            int sortOrder
    ) {}
}
