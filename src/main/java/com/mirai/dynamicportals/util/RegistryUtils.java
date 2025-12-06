package com.mirai.dynamicportals.util;

import com.mirai.dynamicportals.DynamicPortals;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

/**
 * Utility class for parsing game objects from string resource identifiers.
 * Centralizes registry lookups with consistent error handling and logging.
 * 
 * <p>All methods in this class are thread-safe as they only read from immutable registries.
 * 
 * <p><b>Usage Examples:</b>
 * <pre>{@code
 * // Parse an entity type
 * EntityType<?> zombie = RegistryUtils.parseEntityType("minecraft:zombie");
 * 
 * // Parse an item
 * Item diamond = RegistryUtils.parseItem("minecraft:diamond");
 * 
 * // Parse a resource location
 * ResourceLocation custom = RegistryUtils.parseResourceLocation("mymod:custom_entity");
 * }</pre>
 * 
 * @see PortalRequirementsLoader for usage in portal requirement loading
 * @see CustomPortalRequirementsLoader for usage in custom portal loading
 */
public class RegistryUtils {
    
    /**
     * Parse an EntityType from a string resource identifier.
     * 
     * <p>The identifier must be in the format "namespace:path" (e.g., "minecraft:zombie").
     * If the namespace is omitted, "minecraft" is assumed.
     * 
     * <p>This method validates that the entity type exists in the registry before returning it.
     * Invalid or non-existent entity types will log a warning and return null.
     * 
     * @param id The string resource identifier to parse (e.g., "minecraft:zombie", "cataclysm:ignis")
     * @return The corresponding EntityType, or null if not found or invalid format
     * @throws IllegalArgumentException if the resource identifier format is invalid (caught internally)
     */
    public static EntityType<?> parseEntityType(String id) {
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
     * Parse an Item from a string resource identifier.
     * 
     * <p>The identifier must be in the format "namespace:path" (e.g., "minecraft:diamond").
     * If the namespace is omitted, "minecraft" is assumed.
     * 
     * <p>Unlike entity types, item registry lookups may return a non-null default item
     * for invalid identifiers. This method explicitly checks for null and logs warnings.
     * 
     * @param id The string resource identifier to parse (e.g., "minecraft:diamond", "mymod:custom_item")
     * @return The corresponding Item, or null if not found or invalid format
     * @throws IllegalArgumentException if the resource identifier format is invalid (caught internally)
     */
    public static Item parseItem(String id) {
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
    
    /**
     * Parse a ResourceLocation from a string identifier, with comprehensive error handling.
     * 
     * <p>This method is useful when you need a ResourceLocation but don't want to handle
     * parsing exceptions manually. All exceptions are caught, logged, and null is returned.
     * 
     * <p><b>Common use cases:</b>
     * <ul>
     *   <li>Parsing dimension identifiers ("minecraft:the_nether")</li>
     *   <li>Parsing advancement identifiers</li>
     *   <li>Parsing custom registry keys</li>
     * </ul>
     * 
     * @param id The string identifier to parse (e.g., "minecraft:the_nether")
     * @return The parsed ResourceLocation, or null if the format is invalid
     * @throws IllegalArgumentException if the identifier format is invalid (caught internally)
     */
    public static ResourceLocation parseResourceLocation(String id) {
        try {
            return ResourceLocation.parse(id);
        } catch (Exception e) {
            DynamicPortals.LOGGER.error("Failed to parse ResourceLocation: {}", id, e);
            return null;
        }
    }
}
