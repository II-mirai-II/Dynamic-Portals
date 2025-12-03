package com.mirai.dynamicportals.client;

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
    private static int deathCount = 0;
    private static Set<ResourceLocation> unlockedAchievements = new HashSet<>();
    private static boolean cacheValid = false;

    public static void updateFromPacket(SyncProgressPacket packet) {
        killedMobs = new HashMap<>(packet.killedMobs());
        obtainedItems = new HashSet<>(packet.obtainedItems());
        deathCount = packet.deathCount();
        unlockedAchievements = new HashSet<>(packet.unlockedAchievements());
        cacheValid = true;
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

    public static int getDeathCount() {
        return deathCount;
    }

    public static boolean isAchievementUnlocked(ResourceLocation achievement) {
        return unlockedAchievements.contains(achievement);
    }

    public static Map<EntityType<?>, Boolean> getKilledMobs() {
        return new HashMap<>(killedMobs);
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
        deathCount = 0;
        unlockedAchievements.clear();
        cacheValid = false;
    }
}
