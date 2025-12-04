package com.mirai.dynamicportals.client;

import com.mirai.dynamicportals.DynamicPortals;
import com.mirai.dynamicportals.network.SyncRequirementsPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.*;

public class ClientRequirementsCache {
    private static Map<ResourceLocation, CachedRequirement> requirements = new HashMap<>();
    private static boolean cacheValid = false;

    public static void updateFromPacket(SyncRequirementsPacket packet) {
        DynamicPortals.LOGGER.info("Received requirements packet with {} dimensions", packet.requirements().size());
        requirements.clear();
        
        for (Map.Entry<ResourceLocation, SyncRequirementsPacket.RequirementData> entry : packet.requirements().entrySet()) {
            ResourceLocation dimension = entry.getKey();
            SyncRequirementsPacket.RequirementData data = entry.getValue();
            
            // Convert ResourceLocations back to game objects
            List<EntityType<?>> mobs = new ArrayList<>();
            for (ResourceLocation mobId : data.mobs()) {
                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(mobId);
                if (entityType != null) {
                    mobs.add(entityType);
                }
            }
            
            List<EntityType<?>> bosses = new ArrayList<>();
            for (ResourceLocation bossId : data.bosses()) {
                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(bossId);
                if (entityType != null) {
                    bosses.add(entityType);
                }
            }
            
            List<Item> items = new ArrayList<>();
            for (ResourceLocation itemId : data.items()) {
                Item item = BuiltInRegistries.ITEM.get(itemId);
                if (item != null) {
                    items.add(item);
                }
            }
            
            requirements.put(dimension, new CachedRequirement(
                    dimension,
                    data.advancement(),
                    Collections.unmodifiableList(mobs),
                    Collections.unmodifiableList(bosses),
                    Collections.unmodifiableList(items)
            ));
            
            DynamicPortals.LOGGER.debug("  - {}: {} mobs, {} bosses, {} items",
                dimension.toString(),
                mobs.size(),
                bosses.size(),
                items.size());
        }
        
        cacheValid = true;
        DynamicPortals.LOGGER.info("Requirements cache updated and validated");
    }

    public static CachedRequirement getRequirement(ResourceLocation dimension) {
        return requirements.get(dimension);
    }

    public static Map<ResourceLocation, CachedRequirement> getAllRequirements() {
        return new HashMap<>(requirements);
    }

    public static boolean isCacheValid() {
        return cacheValid;
    }

    public static void invalidateCache() {
        cacheValid = false;
    }

    public static void clear() {
        requirements.clear();
        cacheValid = false;
    }

    /**
     * Cached requirement data with resolved game objects
     */
    public static class CachedRequirement {
        private final ResourceLocation dimension;
        private final ResourceLocation advancement;
        private final List<EntityType<?>> mobs;
        private final List<EntityType<?>> bosses;
        private final List<Item> items;

        public CachedRequirement(ResourceLocation dimension, ResourceLocation advancement,
                                  List<EntityType<?>> mobs, List<EntityType<?>> bosses, List<Item> items) {
            this.dimension = dimension;
            this.advancement = advancement;
            this.mobs = mobs;
            this.bosses = bosses;
            this.items = items;
        }

        public ResourceLocation getDimension() {
            return dimension;
        }

        public ResourceLocation getAdvancement() {
            return advancement;
        }

        public List<EntityType<?>> getMobs() {
            return mobs;
        }

        public List<EntityType<?>> getBosses() {
            return bosses;
        }

        public List<Item> getItems() {
            return items;
        }
    }
}
