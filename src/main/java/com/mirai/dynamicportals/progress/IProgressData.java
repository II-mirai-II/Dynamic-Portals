package com.mirai.dynamicportals.progress;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.Set;

/**
 * Abstraction layer for progress data that can be either individual (player-bound)
 * or global (world-level shared by all players).
 */
public interface IProgressData {
    
    /**
     * Record that a mob of the given type has been killed.
     */
    void recordMobKill(EntityType<?> mobType);
    
    /**
     * Check if a mob of the given type has been killed.
     */
    boolean hasMobBeenKilled(EntityType<?> mobType);
    
    /**
     * Record that an item has been obtained.
     */
    void recordItemObtained(Item item);
    
    /**
     * Check if an item has been obtained.
     */
    boolean hasItemBeenObtained(Item item);
    
    /**
     * Record that an advancement has been unlocked.
     */
    void recordAdvancementUnlocked(ResourceLocation advancement);
    
    /**
     * Check if an advancement has been unlocked.
     */
    boolean hasAdvancementBeenUnlocked(ResourceLocation advancement);
    
    /**
     * Get all killed mobs and their kill status.
     */
    Map<EntityType<?>, Boolean> getKilledMobs();
    
    /**
     * Get all obtained items.
     */
    Set<Item> getObtainedItems();
    
    /**
     * Get all unlocked achievements.
     */
    Set<ResourceLocation> getUnlockedAchievements();
    
    /**
     * Check if this is global (shared) progress data.
     */
    boolean isGlobal();
}
