package com.mirai.dynamicportals.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mirai.dynamicportals.DynamicPortals;
import com.mirai.dynamicportals.api.PortalRequirement;
import com.mirai.dynamicportals.api.PortalRequirementRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Loads custom portal requirements from external configuration files.
 * Allows modpack creators and users to completely customize portal requirements.
 */
public class CustomPortalRequirementsLoader {
    
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setLenient()  // Support JSON5-like syntax
            .create();
    
    private static final String CONFIG_DIR = "config/dynamicportals";
    private static final String CONFIG_FILE = "portal_requirements.json5";
    
    /**
     * Load custom portal requirements from config folder.
     * This allows modpack creators to customize everything!
     */
    public static void loadCustomRequirements() {
        Path configPath = Paths.get(CONFIG_DIR, CONFIG_FILE);
        
        // Create default config if doesn't exist
        if (!Files.exists(configPath)) {
            createDefaultConfig(configPath);
            DynamicPortals.LOGGER.info("Created default portal requirements config at: {}", configPath);
            // Don't load the default empty config
            return;
        }
        
        try {
            String content = Files.readString(configPath);
            
            if (ModConfig.COMMON.debugLogging.get()) {
                DynamicPortals.LOGGER.debug("Loading custom portal requirements from: {}", configPath);
            }
            
            // Remove JSON5 comments
            content = removeComments(content);
            
            PortalRequirementConfig config = GSON.fromJson(content, PortalRequirementConfig.class);
            
            if (config == null) {
                DynamicPortals.LOGGER.warn("Portal requirements config is empty or invalid");
                return;
            }
            
            processCustomConfig(config);
            
        } catch (IOException e) {
            DynamicPortals.LOGGER.error("Failed to read portal requirements config from {}", configPath, e);
        } catch (JsonSyntaxException e) {
            DynamicPortals.LOGGER.error("Invalid JSON syntax in portal requirements config: {}", e.getMessage());
            DynamicPortals.LOGGER.error("Please check your config file at: {}", configPath);
        }
    }
    
    /**
     * Create default configuration file with documentation
     */
    private static void createDefaultConfig(Path path) {
        try {
            Files.createDirectories(path.getParent());
            
            String template = generateDefaultTemplate();
            Files.writeString(path, template);
            
        } catch (IOException e) {
            DynamicPortals.LOGGER.error("Failed to create default config file", e);
        }
    }
    
    /**
     * Generate default configuration template with extensive documentation
     */
    private static String generateDefaultTemplate() {
        return """
{
  // Portal Requirements Configuration for Dynamic Portals
  // Version: 1.0
  // 
  // This file allows you to customize portal requirements completely.
  // You can add requirements for vanilla dimensions, modded dimensions, or create entirely new ones!
  //
  // IMPORTANT: After editing this file, use /dynamicportals reload command or restart the game.
  //
  // WARNING: JSON5 Comment Limitations
  // - Do NOT use "//" or "/*" inside string values (e.g., "description": "Use // syntax")
  // - Comments are removed via regex and may break strings containing comment syntax
  // - If you encounter parsing errors, remove all comments from string values
  
  "config_version": 1,
  
  // Should this config override the default requirements?
  // Set to true to completely replace vanilla configs with your own
  "override_defaults": false,
  
  "portals": {
    // Add your custom portal requirements here!
    // 
    // Example structure (remove the // comments when actually using):
    // "minecraft:the_nether": {
    //   "enabled": true,
    //   "advancement": "dynamicportals:nether_access",
    //   "requirements": {
    //     "mobs": {
    //       "overworld_hostiles": [
    //         "minecraft:zombie",
    //         "minecraft:skeleton"
    //       ]
    //     },
    //     "bosses": [
    //       "minecraft:elder_guardian"
    //     ],
    //     "items": [
    //       "minecraft:diamond"
    //     ]
    //   },
    //   "display": {
    //     "name": "Nether Portal",
    //     "description": "Custom description",
    //     "color": 16776533,
    //     "icon": "minecraft:netherrack",
    //     "sort_order": 1
    //   }
    // }
    //
    // To disable a vanilla portal, set "enabled": false
    // To modify vanilla portals, set "override_defaults": true and redefine them here
    //
    // Supported mods: Any mod that adds entities or items!
    // Just use the format "modid:entity_name" or "modid:item_name"
  }
}
""";
    }
    
    /**
     * Remove comments from JSON5-like syntax
     */
    private static String removeComments(String json) {
        // Remove single-line comments (//)
        json = json.replaceAll("//.*?\\n", "\n");
        // Remove multi-line comments (/* */)
        json = json.replaceAll("/\\*.*?\\*/", "");
        // Remove trailing commas (JSON5 feature)
        json = json.replaceAll(",\\s*([}\\]])", "$1");
        return json;
    }
    
    /**
     * Process custom configuration
     */
    private static void processCustomConfig(PortalRequirementConfig config) {
        if (config.isOverrideDefaults()) {
            // Validate that custom portals exist before clearing defaults
            if (config.getPortals() == null || config.getPortals().isEmpty()) {
                DynamicPortals.LOGGER.warn("Override mode enabled but no custom portals defined! "
                    + "Server will have NO portal requirements. Keeping defaults instead.");
                DynamicPortals.LOGGER.warn("To use override mode, add at least one portal configuration.");
                return; // Don't clear defaults if no custom configs
            }
            
            DynamicPortals.LOGGER.info("Override mode enabled - clearing default requirements");
            PortalRequirementRegistry.getInstance().clearRequirements();
        }
        
        if (config.getPortals() == null || config.getPortals().isEmpty()) {
            DynamicPortals.LOGGER.info("No custom portal requirements defined in config file");
            return;
        }
        
        int registered = 0;
        for (Map.Entry<String, PortalRequirementConfig.PortalConfig> entry : config.getPortals().entrySet()) {
            if (registerCustomPortal(entry.getKey(), entry.getValue())) {
                registered++;
            }
        }
        
        DynamicPortals.LOGGER.info("Loaded {} custom portal requirement(s) from config file", registered);
    }
    
    /**
     * Register a custom portal requirement
     */
    private static boolean registerCustomPortal(String dimensionId, PortalRequirementConfig.PortalConfig portalConfig) {
        if (!portalConfig.isEnabled()) {
            DynamicPortals.LOGGER.debug("Custom portal {} is disabled, skipping", dimensionId);
            return false;
        }
        
        try {
            ResourceLocation dimension = ResourceLocation.parse(dimensionId);
            PortalRequirement.Builder builder = PortalRequirement.builder(dimension);
            
            // Advancement
            if (portalConfig.getAdvancement() != null) {
                builder.advancement(ResourceLocation.parse(portalConfig.getAdvancement()));
            }
            
            // Mobs (all categories flattened)
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
            
            // Bosses
            if (portalConfig.getRequirements() != null && portalConfig.getRequirements().getBosses() != null) {
                for (String bossId : portalConfig.getRequirements().getBosses()) {
                    EntityType<?> entityType = parseEntityType(bossId);
                    if (entityType != null) {
                        builder.addBoss(entityType);
                    }
                }
            }
            
            // Items
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
            
            PortalRequirement requirement = builder.build();
            PortalRequirementRegistry.getInstance().registerPortalRequirement(requirement);
            
            DynamicPortals.LOGGER.info("Registered custom portal: {} ({} mobs, {} bosses, {} items)",
                    dimensionId,
                    requirement.getRequiredMobs().size(),
                    requirement.getRequiredBosses().size(),
                    requirement.getRequiredItems().size());
            
            return true;
            
        } catch (Exception e) {
            DynamicPortals.LOGGER.error("Failed to register custom portal: {}", dimensionId, e);
            return false;
        }
    }
    
    /**
     * Parse entity type from string ID
     */
    private static EntityType<?> parseEntityType(String id) {
        try {
            ResourceLocation rl = ResourceLocation.parse(id);
            
            // Check if the entity type exists in the registry
            if (!BuiltInRegistries.ENTITY_TYPE.containsKey(rl)) {
                DynamicPortals.LOGGER.warn("Unknown entity type in config: {}", id);
                return null;
            }
            
            return BuiltInRegistries.ENTITY_TYPE.get(rl);
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
            ResourceLocation rl = ResourceLocation.parse(id);
            Item item = BuiltInRegistries.ITEM.get(rl);
            
            if (item == null) {
                DynamicPortals.LOGGER.warn("Unknown item in config: {}", id);
                return null;
            }
            
            return item;
        } catch (Exception e) {
            DynamicPortals.LOGGER.error("Failed to parse item: {}", id, e);
            return null;
        }
    }
}
