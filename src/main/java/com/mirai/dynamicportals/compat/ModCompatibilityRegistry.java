package com.mirai.dynamicportals.compat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.mirai.dynamicportals.DynamicPortals;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Registry for mod compatibility configurations.
 * Loads JSON files that define which mobs from other mods should be tracked for portal requirements.
 */
public class ModCompatibilityRegistry {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, ModCompatConfig> LOADED_CONFIGS = new HashMap<>();
    
    // Paths to check for compatibility configs
    private static final String COMPAT_PATH = "/data/dynamicportals/mod_compat/";
    
    /**
     * Compatibility configuration for a mod
     */
    public record ModCompatConfig(
        String modId,
        boolean enabled,
        List<EntityType<?>> overworldMobs,
        List<EntityType<?>> netherMobs,
        List<EntityType<?>> endMobs,
        List<EntityType<?>> bosses,
        List<EntityType<?>> netherBosses
    ) {}
    
    /**
     * Detects installed mods and loads their compatibility configurations.
     * Loads from both internal resources (bundled with mod) and external config folder (user customization).
     */
    public static void loadCompatibilityConfigs() {
        DynamicPortals.LOGGER.info("Loading mod compatibility configurations...");
        
        // Get external config folder
        Path configFolder = FMLPaths.CONFIGDIR.get().resolve("dynamicportals/mod_compat");
        
        // Export default configs to external folder for user editing
        exportDefaultConfigs(configFolder);
        
        // 1. Load internal configs (bundled with mod) for known mods
        loadInternalConfigs();
        
        // 2. Load external configs (user customization) - overrides internal
        loadExternalConfigs(configFolder);
        
        DynamicPortals.LOGGER.info("Loaded {} mod compatibility configuration(s)", LOADED_CONFIGS.size());
    }
    
    /**
     * Checks if a mod is loaded
     */
    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
    
    /**
     * Parses a JSON config into a ModCompatConfig
     */
    private static ModCompatConfig parseConfig(JsonObject json) {
        String modId = json.get("mod_id").getAsString();
        boolean enabled = json.has("enabled") ? json.get("enabled").getAsBoolean() : true;
        
        List<EntityType<?>> overworldMobs = parseEntityList(json, "overworld_mobs");
        List<EntityType<?>> netherMobs = parseEntityList(json, "nether_mobs");
        List<EntityType<?>> endMobs = parseEntityList(json, "end_mobs");
        List<EntityType<?>> bosses = parseEntityList(json, "bosses");
        List<EntityType<?>> netherBosses = parseEntityList(json, "nether_bosses");
        
        return new ModCompatConfig(modId, enabled, overworldMobs, netherMobs, endMobs, bosses, netherBosses);
    }
    
    /**
     * Parses a JSON array of entity IDs into EntityType list
     */
    private static List<EntityType<?>> parseEntityList(JsonObject json, String key) {
        if (!json.has(key)) {
            return List.of();
        }
        
        JsonArray array = json.getAsJsonArray(key);
        List<EntityType<?>> entities = new ArrayList<>();
        
        for (int i = 0; i < array.size(); i++) {
            String entityId = array.get(i).getAsString();
            
            try {
                ResourceLocation id = ResourceLocation.parse(entityId);
                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(id);
                
                if (entityType != null && entityType != EntityType.PIG) { // PIG is the default/empty value
                    entities.add(entityType);
                } else {
                    DynamicPortals.LOGGER.warn("Unknown entity type in config: {}", entityId);
                }
            } catch (Exception e) {
                DynamicPortals.LOGGER.error("Failed to parse entity ID: {}", entityId, e);
            }
        }
        
        return entities;
    }
    
    /**
     * Gets all loaded compatibility configurations
     */
    public static Map<String, ModCompatConfig> getAllConfigs() {
        return Collections.unmodifiableMap(LOADED_CONFIGS);
    }
    
    /**
     * Gets a specific mod's config
     */
    public static Optional<ModCompatConfig> getConfig(String modId) {
        return Optional.ofNullable(LOADED_CONFIGS.get(modId));
    }
    
    /**
     * Gets all overworld mobs from all loaded configs
     */
    public static List<EntityType<?>> getAllOverworldMobs() {
        return LOADED_CONFIGS.values().stream()
            .flatMap(config -> config.overworldMobs().stream())
            .toList();
    }
    
    /**
     * Gets all nether mobs from all loaded configs
     */
    public static List<EntityType<?>> getAllNetherMobs() {
        return LOADED_CONFIGS.values().stream()
            .flatMap(config -> config.netherMobs().stream())
            .toList();
    }
    
    /**
     * Gets all end mobs from all loaded configs
     */
    public static List<EntityType<?>> getAllEndMobs() {
        return LOADED_CONFIGS.values().stream()
            .flatMap(config -> config.endMobs().stream())
            .toList();
    }
    
    /**
     * Gets all bosses from all loaded configs
     */
    public static List<EntityType<?>> getAllBosses() {
        return LOADED_CONFIGS.values().stream()
            .flatMap(config -> config.bosses().stream())
            .toList();
    }
    
    /**
     * Gets all nether bosses from all loaded configs
     */
    public static List<EntityType<?>> getAllNetherBosses() {
        return LOADED_CONFIGS.values().stream()
            .flatMap(config -> config.netherBosses().stream())
            .toList();
    }
    
    /**
     * Clears all loaded configs (for reloading)
     */
    public static void clear() {
        LOADED_CONFIGS.clear();
    }
    
    /**
     * Load internal compatibility configs (bundled with mod)
     */
    private static void loadInternalConfigs() {
        String[] knownMods = {
            "mowziesmobs",
            "cataclysm",
            "alexsmobs",
            "twilightforest",
            "iceandfire",
            "born_in_chaos_v1"
        };
        
        for (String modId : knownMods) {
            if (isModLoaded(modId)) {
                loadInternalModConfig(modId);
            }
        }
    }
    
    /**
     * Load external compatibility configs from config folder.
     * Allows users to add custom mod compatibility without recompiling.
     */
    private static void loadExternalConfigs(Path configFolder) {
        if (!Files.exists(configFolder)) {
            return;
        }
        
        try (Stream<Path> files = Files.list(configFolder)) {
            files.filter(p -> p.toString().endsWith(".json"))
                 .forEach(path -> {
                     try {
                         loadExternalConfig(path);
                     } catch (Exception e) {
                         DynamicPortals.LOGGER.error("Failed to load external config: {}", path, e);
                     }
                 });
        } catch (IOException e) {
            DynamicPortals.LOGGER.error("Failed to list external mod compatibility configs", e);
        }
    }
    
    /**
     * Export default configs to external folder for user editing
     */
    private static void exportDefaultConfigs(Path configFolder) {
        try {
            Files.createDirectories(configFolder);
            
            String[] defaultMods = {
                "mowziesmobs",
                "cataclysm",
                "alexsmobs",
                "twilightforest",
                "iceandfire",
                "born_in_chaos_v1"
            };
            
            for (String modId : defaultMods) {
                Path outputPath = configFolder.resolve(modId + ".json");
                
                // Only export if file doesn't exist (don't overwrite user edits)
                if (!Files.exists(outputPath)) {
                    String resourcePath = COMPAT_PATH + modId + ".json";
                    
                    try (InputStream stream = ModCompatibilityRegistry.class.getResourceAsStream(resourcePath)) {
                        if (stream != null) {
                            Files.copy(stream, outputPath);
                            DynamicPortals.LOGGER.debug("Exported default config for mod: {}", modId);
                        }
                    } catch (IOException e) {
                        DynamicPortals.LOGGER.warn("Failed to export config for mod: {}", modId, e);
                    }
                }
            }
        } catch (IOException e) {
            DynamicPortals.LOGGER.error("Failed to create mod compatibility config folder", e);
        }
    }
    
    /**
     * Load a config from internal resources
     */
    private static void loadInternalModConfig(String modId) {
        String configPath = COMPAT_PATH + modId + ".json";
        
        try (InputStream stream = ModCompatibilityRegistry.class.getResourceAsStream(configPath)) {
            if (stream == null) {
                DynamicPortals.LOGGER.debug("No internal compatibility config found for mod: {}", modId);
                return;
            }
            
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            
            ModCompatConfig config = parseConfig(json);
            
            if (config.enabled()) {
                LOADED_CONFIGS.put(modId, config);
                DynamicPortals.LOGGER.info("Loaded internal compatibility config for mod: {} ({} mobs, {} bosses)", 
                    modId, 
                    config.overworldMobs().size() + config.netherMobs().size() + config.endMobs().size(),
                    config.bosses().size() + config.netherBosses().size());
            } else {
                DynamicPortals.LOGGER.info("Compatibility config for mod {} is disabled", modId);
            }
            
        } catch (IOException e) {
            DynamicPortals.LOGGER.error("Failed to load internal compatibility config for mod: {}", modId, e);
        } catch (Exception e) {
            DynamicPortals.LOGGER.error("Failed to parse internal compatibility config for mod: {}", modId, e);
        }
    }
    
    /**
     * Load a config from external file (user customization)
     */
    private static void loadExternalConfig(Path configPath) throws IOException {
        String fileName = configPath.getFileName().toString();
        String modId = fileName.replace(".json", "");
        
        DynamicPortals.LOGGER.debug("Loading external config: {}", configPath);
        
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(configPath), StandardCharsets.UTF_8)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            ModCompatConfig config = parseConfig(json);
            
            if (config.enabled()) {
                // External configs override internal ones
                LOADED_CONFIGS.put(modId, config);
                DynamicPortals.LOGGER.info("Loaded EXTERNAL compatibility config for mod: {} ({} mobs, {} bosses)", 
                    modId, 
                    config.overworldMobs().size() + config.netherMobs().size() + config.endMobs().size(),
                    config.bosses().size() + config.netherBosses().size());
            } else {
                DynamicPortals.LOGGER.info("External compatibility config for mod {} is disabled", modId);
            }
        }
    }
}
