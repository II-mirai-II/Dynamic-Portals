package com.mirai.dynamicportals.api;

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
     * Register vanilla dimension requirements (Nether and End)
     * Called during mod initialization
     */
    public void registerVanillaRequirements() {
        // Nether requirement
        PortalRequirement netherRequirement = PortalRequirement.builder(ModConstants.NETHER_DIMENSION)
                .advancement(ModConstants.NETHER_ACCESS_ADVANCEMENT)
                .addMobs(
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
                )
                .addBoss(EntityType.ELDER_GUARDIAN)
                .addItem(Items.DIAMOND)
                .build();

        // End requirement
        PortalRequirement endRequirement = PortalRequirement.builder(ModConstants.END_DIMENSION)
                .advancement(ModConstants.END_ACCESS_ADVANCEMENT)
                .addMobs(
                        EntityType.GHAST,
                        EntityType.BLAZE,
                        EntityType.WITHER_SKELETON,
                        EntityType.PIGLIN,
                        EntityType.PIGLIN_BRUTE,
                        EntityType.HOGLIN
                )
                .addBosses(EntityType.WARDEN, EntityType.WITHER)
                .addItem(Items.NETHERITE_INGOT)
                .build();

        registerPortalRequirement(netherRequirement);
        registerPortalRequirement(endRequirement);
    }

    /**
     * Get all registered requirements
     * @return Unmodifiable map of all requirements
     */
    public Map<ResourceLocation, PortalRequirement> getAllRequirements() {
        return Map.copyOf(requirements);
    }
}
