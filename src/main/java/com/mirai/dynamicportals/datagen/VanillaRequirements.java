package com.mirai.dynamicportals.datagen;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Static definitions for vanilla portal requirements.
 * Used by DataGen to generate advancement JSON files at build time.
 * 
 * IMPORTANT DESIGN NOTE:
 * - Advancements generated here only reference VANILLA mobs/bosses
 * - At runtime, PortalRequirementRegistry adds mod compatibility mobs on top of vanilla
 * - The advancement trigger (KillRequirementTrigger) validates against the FULL runtime list
 * - This means advancements track vanilla progress, but runtime enforces vanilla + mod compat
 * - This is intentional: advancement JSONs are static, runtime requirements are dynamic
 */
public class VanillaRequirements {
    
    public static final NetherRequirements NETHER = new NetherRequirements();
    public static final EndRequirements END = new EndRequirements();
    
    public static class NetherRequirements {
        /**
         * Get vanilla overworld mobs for advancement JSON generation.
         * At runtime, PortalRequirementRegistry will add mod compatibility mobs on top.
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
         * Get vanilla overworld bosses for advancement JSON generation.
         * At runtime, PortalRequirementRegistry will add mod compatibility bosses on top.
         */
        public List<EntityType<?>> getBosses() {
            return List.of(EntityType.ELDER_GUARDIAN);
        }
        
        /**
         * Get required items for Nether portal access.
         */
        public List<Item> getItems() {
            return List.of(Items.DIAMOND);
        }
    }
    
    public static class EndRequirements {
        /**
         * Get vanilla nether mobs for advancement JSON generation.
         * At runtime, PortalRequirementRegistry will add mod compatibility mobs on top.
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
         * Get vanilla nether bosses for advancement JSON generation.
         * At runtime, PortalRequirementRegistry will add mod compatibility bosses on top.
         */
        public List<EntityType<?>> getBosses() {
            return List.of(EntityType.WARDEN, EntityType.WITHER);
        }
        
        /**
         * Get required items for End portal access.
         */
        public List<Item> getItems() {
            return List.of(Items.NETHERITE_INGOT);
        }
    }
}
