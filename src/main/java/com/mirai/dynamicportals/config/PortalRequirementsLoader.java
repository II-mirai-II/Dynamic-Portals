package com.mirai.dynamicportals.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mirai.dynamicportals.DynamicPortals;
import com.mirai.dynamicportals.api.PortalRequirement;
import com.mirai.dynamicportals.api.PortalRequirementRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Loads portal requirements from internal JSON configuration files.
 * These are the default requirements shipped with the mod.
 */
public class PortalRequirementsLoader {
    
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    
    private static final String CONFIG_PATH = "/data/dynamicportals/portal_requirements/vanilla.json";
    
    /**
     * Load and register all portal requirements from internal JSON config
     */
    public static void loadAndRegister() {
        DynamicPortals.LOGGER.info("Loading default portal requirements from configuration...");
        
        try (InputStream stream = PortalRequirementsLoader.class.getResourceAsStream(CONFIG_PATH)) {
            if (stream == null) {
                DynamicPortals.LOGGER.error("Could not find internal configuration file: {}", CONFIG_PATH);
                DynamicPortals.LOGGER.warn("Falling back to hardcoded requirements");
                PortalRequirementRegistry.getInstance().registerVanillaRequirements();
                return;
            }
            
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            PortalRequirementConfig config = GSON.fromJson(reader, PortalRequirementConfig.class);
            
            if (config == null || config.getPortals() == null) {
                DynamicPortals.LOGGER.error("Invalid configuration file format");
                DynamicPortals.LOGGER.warn("Falling back to hardcoded requirements");
                PortalRequirementRegistry.getInstance().registerVanillaRequirements();
                return;
            }
            
            processConfig(config);
            
            DynamicPortals.LOGGER.info("Successfully loaded {} default portal requirement(s)", 
                config.getPortals().size());
            
        } catch (Exception e) {
            DynamicPortals.LOGGER.error("Failed to load portal requirements configuration", e);
            DynamicPortals.LOGGER.warn("Falling back to hardcoded requirements");
            PortalRequirementRegistry.getInstance().registerVanillaRequirements();
        }
    }
    
    /**
     * Process configuration and register requirements
     */
    private static void processConfig(PortalRequirementConfig config) {
        for (Map.Entry<String, PortalRequirementConfig.PortalConfig> entry : config.getPortals().entrySet()) {
            String dimensionId = entry.getKey();
            PortalRequirementConfig.PortalConfig portalConfig = entry.getValue();
            
            if (!portalConfig.isEnabled()) {
                DynamicPortals.LOGGER.info("Portal requirement for {} is disabled, skipping", dimensionId);
                continue;
            }
            
            try {
                registerPortalRequirement(dimensionId, portalConfig);
            } catch (Exception e) {
                DynamicPortals.LOGGER.error("Failed to register portal requirement for {}", dimensionId, e);
            }
        }
    }
    
    /**
     * Register a single portal requirement
     */
    private static void registerPortalRequirement(String dimensionId, PortalRequirementConfig.PortalConfig portalConfig) {
        ResourceLocation dimension = ResourceLocation.parse(dimensionId);
        PortalRequirement.Builder builder = PortalRequirement.builder(dimension);
        
        // Set advancement
        if (portalConfig.getAdvancement() != null) {
            builder.advancement(ResourceLocation.parse(portalConfig.getAdvancement()));
        }
        
        // Process mobs (all categories)
        if (portalConfig.getRequirements() != null && portalConfig.getRequirements().getMobs() != null) {
            for (List<String> mobList : portalConfig.getRequirements().getMobs().values()) {
                for (String mobId : mobList) {
                    EntityType<?> entityType = parseEntityType(mobId);
                    if (entityType != null) {
                        builder.addMob(entityType);
                    }
                }
            }
        }
        
        // Process bosses
        if (portalConfig.getRequirements() != null && portalConfig.getRequirements().getBosses() != null) {
            for (String bossId : portalConfig.getRequirements().getBosses()) {
                EntityType<?> entityType = parseEntityType(bossId);
                if (entityType != null) {
                    builder.addBoss(entityType);
                }
            }
        }
        
        // Process items
        if (portalConfig.getRequirements() != null && portalConfig.getRequirements().getItems() != null) {
            for (String itemId : portalConfig.getRequirements().getItems()) {
                Item item = parseItem(itemId);
                if (item != null) {
                    builder.addItem(item);
                }
            }
        }
        
        // Process display information
        if (portalConfig.getDisplay() != null) {
            PortalRequirementConfig.DisplaySection display = portalConfig.getDisplay();
            
            if (display.getName() != null) {
                builder.displayName(display.getName());
            }
            if (display.getDescription() != null) {
                builder.displayDescription(display.getDescription());
            }
            if (display.getColor() != null) {
                builder.displayColor(display.getColor());
            }
            if (display.getIcon() != null) {
                builder.displayIcon(ResourceLocation.parse(display.getIcon()));
            }
            builder.sortOrder(display.getSortOrder());
        }
        
        // Register
        PortalRequirement requirement = builder.build();
        PortalRequirementRegistry.getInstance().registerPortalRequirement(requirement);
        
        DynamicPortals.LOGGER.debug("Registered portal requirement for {}: {} mobs, {} bosses, {} items",
            dimensionId,
            requirement.getRequiredMobs().size(),
            requirement.getRequiredBosses().size(),
            requirement.getRequiredItems().size());
    }
    
    /**
     * Parse entity type from string ID
     */
    private static EntityType<?> parseEntityType(String id) {
        try {
            ResourceLocation resourceLocation = ResourceLocation.parse(id);
            
            // Check if the entity type exists in the registry
            if (!BuiltInRegistries.ENTITY_TYPE.containsKey(resourceLocation)) {
                DynamicPortals.LOGGER.warn("Unknown entity type: {}", id);
                return null;
            }
            
            return BuiltInRegistries.ENTITY_TYPE.get(resourceLocation);
        } catch (Exception e) {
            DynamicPortals.LOGGER.error("Failed to parse entity type: {}", id, e);
            return null;
        }
    }
    
    /**
     * Parse item from string ID
     */
    private static Item parseItem(String id) {
        try {
            ResourceLocation resourceLocation = ResourceLocation.parse(id);
            Item item = BuiltInRegistries.ITEM.get(resourceLocation);
            
            if (item == null) {
                DynamicPortals.LOGGER.warn("Unknown item: {}", id);
                return null;
            }
            
            return item;
        } catch (Exception e) {
            DynamicPortals.LOGGER.error("Failed to parse item: {}", id, e);
            return null;
        }
    }
}
