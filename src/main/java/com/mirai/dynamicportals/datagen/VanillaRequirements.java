package com.mirai.dynamicportals.datagen;

import com.mirai.dynamicportals.compat.ModCompatibilityRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Static definitions for vanilla portal requirements.
 * Used by DataGen to generate advancements.
 * IMPORTANT: Must match the runtime requirements in PortalRequirementRegistry!
 */
public class VanillaRequirements {
    
    public static final NetherRequirements NETHER = new NetherRequirements();
    public static final EndRequirements END = new EndRequirements();
    
    public static class NetherRequirements {
        /**
         * Get vanilla overworld mobs only (for backward compatibility)
         */
        public List<EntityType<?>> getMobs() {
            return List.of(
                    EntityType.ZOMBIE,
                    EntityType.SKELETON,
                    EntityType.CREEPER,
                    EntityType.SPIDER,
                    EntityType.ENDERMAN,
                    EntityType.WITCH,
                    EntityType.SLIME,
                    EntityType.DROWNED,
                    EntityType.HUSK,
                    EntityType.STRAY,
                    EntityType.BREEZE,
                    EntityType.BOGGED,
                    EntityType.PILLAGER,
                    EntityType.VINDICATOR,
                    EntityType.EVOKER
            );
        }
        
        /**
         * Get ALL mobs including mod compatibility mobs (matches runtime requirements)
         */
        public List<EntityType<?>> getAllMobs() {
            List<EntityType<?>> allMobs = new ArrayList<>(getMobs());
            allMobs.addAll(ModCompatibilityRegistry.getAllOverworldMobs());
            return allMobs;
        }
        
        /**
         * Get vanilla bosses only
         */
        public List<EntityType<?>> getBosses() {
            return List.of(EntityType.ELDER_GUARDIAN);
        }
        
        /**
         * Get ALL bosses including mod compatibility bosses (matches runtime requirements)
         */
        public List<EntityType<?>> getAllBosses() {
            List<EntityType<?>> allBosses = new ArrayList<>(getBosses());
            allBosses.addAll(ModCompatibilityRegistry.getAllBosses());
            return allBosses;
        }
        
        public List<Item> getItems() {
            return List.of(Items.DIAMOND);
        }
    }
    
    public static class EndRequirements {
        /**
         * Get vanilla nether mobs only (for backward compatibility)
         */
        public List<EntityType<?>> getMobs() {
            return List.of(
                    EntityType.GHAST,
                    EntityType.BLAZE,
                    EntityType.WITHER_SKELETON,
                    EntityType.PIGLIN,
                    EntityType.PIGLIN_BRUTE,
                    EntityType.HOGLIN
            );
        }
        
        /**
         * Get ALL mobs including mod compatibility mobs (matches runtime requirements)
         */
        public List<EntityType<?>> getAllMobs() {
            List<EntityType<?>> allMobs = new ArrayList<>(getMobs());
            allMobs.addAll(ModCompatibilityRegistry.getAllNetherMobs());
            return allMobs;
        }
        
        /**
         * Get vanilla bosses only
         */
        public List<EntityType<?>> getBosses() {
            return List.of(EntityType.WARDEN, EntityType.WITHER);
        }
        
        /**
         * Get ALL bosses including mod compatibility bosses (matches runtime requirements)
         */
        public List<EntityType<?>> getAllBosses() {
            List<EntityType<?>> allBosses = new ArrayList<>(getBosses());
            allBosses.addAll(ModCompatibilityRegistry.getAllNetherBosses());
            return allBosses;
        }
        
        public List<Item> getItems() {
            return List.of(Items.NETHERITE_INGOT);
        }
    }
}
