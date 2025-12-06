package com.mirai.dynamicportals.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mirai.dynamicportals.DynamicPortals;
import com.mirai.dynamicportals.api.PortalRequirement;
import com.mirai.dynamicportals.api.PortalRequirementRegistry;
import com.mirai.dynamicportals.compat.ModCompatibilityRegistry;
import com.mirai.dynamicportals.util.ModConstants;
import com.mirai.dynamicportals.util.RegistryUtils;
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
            
            // Integrate mobs from loaded mod compatibility configs
            integrateModCompatibilityMobs();
            
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
     * Register a single portal requirement from configuration.
     * Made public to allow CustomPortalRequirementsLoader to use it.
     */
    public static void registerPortalRequirement(String dimensionId, PortalRequirementConfig.PortalConfig portalConfig) {
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
                    EntityType<?> entityType = RegistryUtils.parseEntityType(mobId);
                    if (entityType != null) {
                        builder.addMob(entityType);
                    }
                }
            }
        }
        
        // Process bosses
        if (portalConfig.getRequirements() != null && portalConfig.getRequirements().getBosses() != null) {
            for (String bossId : portalConfig.getRequirements().getBosses()) {
                EntityType<?> entityType = RegistryUtils.parseEntityType(bossId);
                if (entityType != null) {
                    builder.addBoss(entityType);
                }
            }
        }
        
        // Process items
        if (portalConfig.getRequirements() != null && portalConfig.getRequirements().getItems() != null) {
            for (String itemId : portalConfig.getRequirements().getItems()) {
                Item item = RegistryUtils.parseItem(itemId);
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
            
            if (display.getBlockedMessage() != null) {
                builder.blockedMessage(display.getBlockedMessage());
            }
            if (display.getUnlockedMessage() != null) {
                builder.unlockedMessage(display.getUnlockedMessage());
            }
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
     * Integrate mobs from ModCompatibilityRegistry into existing portal requirements.
     * This adds mobs from detected mods (Mowzie's Mobs, Cataclysm, etc.) to the vanilla portals.
     */
    private static void integrateModCompatibilityMobs() {
        PortalRequirementRegistry registry = PortalRequirementRegistry.getInstance();
        
        // Get existing requirements
        PortalRequirement netherReq = registry.getRequirement(ModConstants.NETHER_DIMENSION);
        PortalRequirement endReq = registry.getRequirement(ModConstants.END_DIMENSION);
        
        if (netherReq == null || endReq == null) {
            DynamicPortals.LOGGER.warn("Could not integrate mod compatibility - portal requirements not found");
            return;
        }
        
        // Get mobs from loaded mod compatibility configs
        List<EntityType<?>> overworldMobs = ModCompatibilityRegistry.getAllOverworldMobs();
        List<EntityType<?>> netherMobs = ModCompatibilityRegistry.getAllNetherMobs();
        List<EntityType<?>> bosses = ModCompatibilityRegistry.getAllBosses();
        List<EntityType<?>> netherBosses = ModCompatibilityRegistry.getAllNetherBosses();
        
        int addedToNether = 0;
        int addedToEnd = 0;
        
        // Integrate into Nether portal (overworld mobs + bosses)
        if (!overworldMobs.isEmpty() || !bosses.isEmpty()) {
            PortalRequirement.Builder netherBuilder = PortalRequirement.builder(ModConstants.NETHER_DIMENSION)
                .advancement(netherReq.getRequiredAdvancement())
                .displayName(netherReq.getDisplayName())
                .displayDescription(netherReq.getDisplayDescription())
                .displayColor(netherReq.getDisplayColor())
                .displayIcon(netherReq.getDisplayIcon())
                .sortOrder(netherReq.getSortOrder());
            
            // Add existing mobs, bosses, and items
            netherBuilder.addMobsList(netherReq.getRequiredMobs());
            netherBuilder.addBossesList(netherReq.getRequiredBosses());
            netherBuilder.addItemsList(netherReq.getRequiredItems());
            
            // Add mod compatibility mobs
            netherBuilder.addMobsList(overworldMobs);
            netherBuilder.addBossesList(bosses);
            
            addedToNether = overworldMobs.size() + bosses.size();
            
            // Re-register with integrated mobs
            registry.registerPortalRequirement(netherBuilder.build());
        }
        
        // Integrate into End portal (nether mobs + nether bosses)
        if (!netherMobs.isEmpty() || !netherBosses.isEmpty()) {
            PortalRequirement.Builder endBuilder = PortalRequirement.builder(ModConstants.END_DIMENSION)
                .advancement(endReq.getRequiredAdvancement())
                .displayName(endReq.getDisplayName())
                .displayDescription(endReq.getDisplayDescription())
                .displayColor(endReq.getDisplayColor())
                .displayIcon(endReq.getDisplayIcon())
                .sortOrder(endReq.getSortOrder());
            
            // Add existing mobs, bosses, and items
            endBuilder.addMobsList(endReq.getRequiredMobs());
            endBuilder.addBossesList(endReq.getRequiredBosses());
            endBuilder.addItemsList(endReq.getRequiredItems());
            
            // Add mod compatibility mobs
            endBuilder.addMobsList(netherMobs);
            endBuilder.addBossesList(netherBosses);
            
            addedToEnd = netherMobs.size() + netherBosses.size();
            
            // Re-register with integrated mobs
            registry.registerPortalRequirement(endBuilder.build());
        }
        
        if (addedToNether > 0 || addedToEnd > 0) {
            DynamicPortals.LOGGER.info("Integrated mod compatibility: +{} entities to Nether, +{} entities to End",
                addedToNether, addedToEnd);
        }
    }
}
