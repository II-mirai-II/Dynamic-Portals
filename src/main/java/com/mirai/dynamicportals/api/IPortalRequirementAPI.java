package com.mirai.dynamicportals.api;

import net.minecraft.resources.ResourceLocation;

public interface IPortalRequirementAPI {
    
    /**
     * Register a portal requirement for a specific dimension
     * @param requirement The portal requirement configuration
     */
    void registerPortalRequirement(PortalRequirement requirement);

    /**
     * Get the portal requirement for a specific dimension
     * @param dimension The dimension resource location
     * @return The portal requirement, or null if none exists
     */
    PortalRequirement getRequirement(ResourceLocation dimension);

    /**
     * Check if a dimension has portal requirements
     * @param dimension The dimension resource location
     * @return true if requirements exist
     */
    boolean hasRequirement(ResourceLocation dimension);

    /**
     * Remove a portal requirement
     * @param dimension The dimension to remove requirements for
     */
    void removeRequirement(ResourceLocation dimension);
}
