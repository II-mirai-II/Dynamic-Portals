package com.mirai.dynamicportals.api;

import com.mirai.dynamicportals.DynamicPortals;
import com.mirai.dynamicportals.compat.ModCompatibilityRegistry;
import com.mirai.dynamicportals.util.ModConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class PortalRequirementRegistry implements IPortalRequirementAPI {
    
    private static final PortalRequirementRegistry INSTANCE = new PortalRequirementRegistry();
    private final Map<ResourceLocation, PortalRequirement> requirements = new HashMap<>();

    private PortalRequirementRegistry() {
    }

    public static PortalRequirementRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerPortalRequirement(PortalRequirement requirement) {
        requirements.put(requirement.getDimension(), requirement);
    }

    @Override
    public PortalRequirement getRequirement(ResourceLocation dimension) {
        return requirements.get(dimension);
    }

    @Override
    public boolean hasRequirement(ResourceLocation dimension) {
        return requirements.containsKey(dimension);
    }

    @Override
    public void removeRequirement(ResourceLocation dimension) {
        requirements.remove(dimension);
    }

    /**
     * Clear all registered requirements
     * Used when reloading mod compatibility
     */
    public void clearRequirements() {
        requirements.clear();
    }

    /**
     * Register vanilla dimension requirements (Nether and End)
     * Called during mod initialization
     */
    public void registerVanillaRequirements() {
        // NETHER PORTAL - Overworld mobs + bosses requirement
        PortalRequirement.Builder netherBuilder = PortalRequirement.builder(ModConstants.NETHER_DIMENSION)
                .advancement(ModConstants.NETHER_ACCESS_ADVANCEMENT);
        
        // Add vanilla overworld mobs directly
        netherBuilder
                .addMob(EntityType.ZOMBIE)
                .addMob(EntityType.SKELETON)
                .addMob(EntityType.CREEPER)
                .addMob(EntityType.SPIDER)
                .addMob(EntityType.ENDERMAN)
                .addMob(EntityType.WITCH)
                .addMob(EntityType.SLIME)
                .addMob(EntityType.DROWNED)
                .addMob(EntityType.HUSK)
                .addMob(EntityType.STRAY)
                .addMob(EntityType.BREEZE)
                .addMob(EntityType.BOGGED)
                .addMob(EntityType.PILLAGER)
                .addMob(EntityType.VINDICATOR)
                .addMob(EntityType.EVOKER);
        
        // Add vanilla overworld bosses
        netherBuilder
                .addBoss(EntityType.ELDER_GUARDIAN)
                .addBoss(EntityType.WARDEN);
        
        // Add mobs and bosses from loaded compatibility configs
        netherBuilder.addMobsList(ModCompatibilityRegistry.getAllOverworldMobs());
        netherBuilder.addBossesList(ModCompatibilityRegistry.getAllBosses());
        
        netherBuilder.addItem(Items.DIAMOND);
        
        PortalRequirement netherRequirement = netherBuilder.build();

        // END PORTAL - Nether mobs + bosses requirement
        PortalRequirement.Builder endBuilder = PortalRequirement.builder(ModConstants.END_DIMENSION)
                .advancement(ModConstants.END_ACCESS_ADVANCEMENT);
        
        // Add vanilla nether mobs directly
        endBuilder
                .addMob(EntityType.GHAST)
                .addMob(EntityType.BLAZE)
                .addMob(EntityType.WITHER_SKELETON)
                .addMob(EntityType.PIGLIN)
                .addMob(EntityType.PIGLIN_BRUTE)
                .addMob(EntityType.HOGLIN);
        
        // Add vanilla nether bosses
        endBuilder.addBoss(EntityType.WITHER);
        
        // Add mobs and bosses from loaded compatibility configs
        endBuilder.addMobsList(ModCompatibilityRegistry.getAllNetherMobs());
        endBuilder.addBossesList(ModCompatibilityRegistry.getAllNetherBosses());
        
        endBuilder.addItem(Items.NETHERITE_INGOT);
        
        PortalRequirement endRequirement = endBuilder.build();

        registerPortalRequirement(netherRequirement);
        registerPortalRequirement(endRequirement);
        
        // Log registration details
        DynamicPortals.LOGGER.info("=== Portal Requirements Registration ===");
        DynamicPortals.LOGGER.info("Nether Portal: {} mobs, {} bosses, {} items", 
            netherRequirement.getRequiredMobs().size(),
            netherRequirement.getRequiredBosses().size(),
            netherRequirement.getRequiredItems().size());
        DynamicPortals.LOGGER.info("End Portal: {} mobs, {} bosses, {} items",
            endRequirement.getRequiredMobs().size(),
            endRequirement.getRequiredBosses().size(),
            endRequirement.getRequiredItems().size());
    }

    /**
     * Get all registered requirements
     * @return Unmodifiable map of all requirements
     */
    public Map<ResourceLocation, PortalRequirement> getAllRequirements() {
        return Map.copyOf(requirements);
    }

    /**
     * Get all registered requirements (static access)
     * @return Unmodifiable map of all requirements
     */
    public static Map<ResourceLocation, PortalRequirement> getAll() {
        return getInstance().getAllRequirements();
    }
}
