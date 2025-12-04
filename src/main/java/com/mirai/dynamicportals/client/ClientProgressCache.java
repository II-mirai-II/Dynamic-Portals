package com.mirai.dynamicportals.client;

import com.mirai.dynamicportals.DynamicPortals;
import com.mirai.dynamicportals.network.SyncProgressPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientProgressCache {
    private static Map<EntityType<?>, Boolean> killedMobs = new HashMap<>();
    private static Set<Item> obtainedItems = new HashSet<>();
    private static Set<ResourceLocation> unlockedAchievements = new HashSet<>();
    private static boolean cacheValid = false;
    
    // Cached ResourceLocation sets for performance
    private static Set<ResourceLocation> killedMobIdsCache = null;

    public static void updateFromPacket(SyncProgressPacket packet) {
        killedMobs = new HashMap<>(packet.killedMobs());
        obtainedItems = new HashSet<>(packet.obtainedItems());
        unlockedAchievements = new HashSet<>(packet.unlockedAchievements());
        cacheValid = true;
        
        // Invalidate cached ResourceLocation set
        killedMobIdsCache = null;
        
        DynamicPortals.LOGGER.info("Progress cache updated: {} mobs tracked, {} items obtained, {} achievements unlocked",
            killedMobs.size(),
            obtainedItems.size(),
            unlockedAchievements.size());
    }

    public static boolean hasMobBeenKilled(EntityType<?> entityType) {
        return killedMobs.getOrDefault(entityType, false);
    }

    public static boolean hasItemBeenObtained(Item item) {
        return obtainedItems.contains(item);
    }

    public static Set<Item> getObtainedItems() {
        return new HashSet<>(obtainedItems);
    }

    public static boolean isAchievementUnlocked(ResourceLocation achievement) {
        return unlockedAchievements.contains(achievement);
    }

    public static Map<EntityType<?>, Boolean> getKilledMobs() {
        return new HashMap<>(killedMobs);
    }

    public static Set<ResourceLocation> getKilledMobIds() {
        if (killedMobIdsCache == null) {
            killedMobIdsCache = new HashSet<>();
            for (EntityType<?> entityType : killedMobs.keySet()) {
                if (killedMobs.get(entityType)) {
                    ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
                    if (id != null) {
                        killedMobIdsCache.add(id);
                    }
                }
            }
        }
        return killedMobIdsCache;
    }

    public static Set<ResourceLocation> getUnlockedAchievements() {
        return new HashSet<>(unlockedAchievements);
    }

    public static boolean isCacheValid() {
        return cacheValid;
    }

    public static void invalidateCache() {
        cacheValid = false;
    }

    public static void clear() {
        killedMobs.clear();
        obtainedItems.clear();
        unlockedAchievements.clear();
        cacheValid = false;
        killedMobIdsCache = null;
    }
}
