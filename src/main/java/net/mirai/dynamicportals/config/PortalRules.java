package net.mirai.dynamicportals.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.mirai.dynamicportals.DynamicPortals;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.config.ModConfigEvent;

public final class PortalRules {
    private static volatile Map<String, PortalDefinition> definitions = Map.of();

    private PortalRules() {
    }

    public static void onConfigLoading(ModConfigEvent.Loading event) {
        GeneratedTomlFormatter.formatIfNeeded(event.getConfig().getFullPath());
        reload();
    }

    public static void onConfigReloading(ModConfigEvent.Reloading event) {
        GeneratedTomlFormatter.formatIfNeeded(event.getConfig().getFullPath());
        reload();
    }

    public static void reload() {
        Map<String, MutablePortalDefinition> mutable = new LinkedHashMap<>();
        ReloadStats stats = new ReloadStats();
        int killLines = DynamicPortalsConfig.CONFIG.killRequirements.get().size();
        int itemLines = DynamicPortalsConfig.CONFIG.itemRequirements.get().size();
        int advancementLines = DynamicPortalsConfig.CONFIG.advancementRequirements.get().size();
        int bypassLines = DynamicPortalsConfig.CONFIG.consumeBypassItems.get().size();

        for (String line : DynamicPortalsConfig.CONFIG.killRequirements.get()) {
            addCounterRequirement(mutable, line, RequirementType.KILL, stats);
        }

        for (String line : DynamicPortalsConfig.CONFIG.itemRequirements.get()) {
            addCounterRequirement(mutable, line, RequirementType.ITEM, stats);
        }

        for (String line : DynamicPortalsConfig.CONFIG.advancementRequirements.get()) {
            addAdvancementRequirement(mutable, line, stats);
        }

        for (String line : DynamicPortalsConfig.CONFIG.consumeBypassItems.get()) {
            addBypassRequirement(mutable, line, stats);
        }

        Map<String, PortalDefinition> built = new LinkedHashMap<>();
        for (Map.Entry<String, MutablePortalDefinition> entry : mutable.entrySet()) {
            MutablePortalDefinition value = entry.getValue();
            built.put(entry.getKey(), new PortalDefinition(entry.getKey(), value.kills, value.items, value.advancements, value.bypassItems));
        }

        definitions = Collections.unmodifiableMap(new LinkedHashMap<>(built));
        DynamicPortals.LOGGER.info(
            "Loaded {} portal rule set(s) from config (kills={}, items={}, advancements={}, bypass={}).",
            definitions.size(),
            killLines,
            itemLines,
            advancementLines,
            bypassLines
        );

        if (stats.inactiveMissingRegistry > 0) {
            DynamicPortals.LOGGER.info(
                "{} portal requirement(s) are inactive because their entity/item id is not registered in the loaded mod set.",
                stats.inactiveMissingRegistry
            );
        }
    }

    public static Collection<PortalDefinition> all() {
        return definitions.values();
    }

    public static PortalDefinition byDimension(String destinationDimension) {
        return definitions.get(destinationDimension);
    }

    private static void addCounterRequirement(Map<String, MutablePortalDefinition> mutable, String line, RequirementType type, ReloadStats stats) {
        String[] parts = line.split("\\|");
        if (parts.length != 3) {
            stats.warnInvalid(type.logName(), line, "expected format destination_dimension|target_id|count");
            return;
        }

        String dimension = normalize(parts[0]);
        String target = normalize(parts[1]);
        int amount;

        try {
            amount = Integer.parseInt(parts[2].trim());
        } catch (NumberFormatException ex) {
            stats.warnInvalid(type.logName(), line, "count must be a whole number");
            return;
        }

        if (isIllustrative(target)) {
            return;
        }

        if (amount <= 0) {
            stats.warnInvalid(type.logName(), line, "count must be greater than zero");
            return;
        }

        ResourceLocation dimensionId = parseResourceLocation(dimension, type.logName(), line, "destination dimension", stats);
        ResourceLocation targetId = parseResourceLocation(target, type.logName(), line, "target id", stats);
        if (dimensionId == null || targetId == null) {
            return;
        }

        if (type == RequirementType.KILL && !BuiltInRegistries.ENTITY_TYPE.containsKey(targetId)) {
            stats.missingRegistry(type, dimensionId, targetId);
            return;
        }

        if (type == RequirementType.ITEM && !BuiltInRegistries.ITEM.containsKey(targetId)) {
            stats.missingRegistry(type, dimensionId, targetId);
            return;
        }

        MutablePortalDefinition definition = mutable.computeIfAbsent(dimensionId.toString(), ignored -> new MutablePortalDefinition());
        if (type == RequirementType.KILL) {
            definition.kills.put(targetId.toString(), amount);
        } else {
            definition.items.put(targetId.toString(), amount);
        }
    }

    private static void addAdvancementRequirement(Map<String, MutablePortalDefinition> mutable, String line, ReloadStats stats) {
        String[] parts = line.split("\\|");
        if (parts.length != 2) {
            stats.warnInvalid("advancement requirement", line, "expected format destination_dimension|advancement_id");
            return;
        }

        String dimension = normalize(parts[0]);
        String advancement = normalize(parts[1]);

        if (isIllustrative(advancement)) {
            return;
        }

        ResourceLocation dimensionId = parseResourceLocation(dimension, "advancement requirement", line, "destination dimension", stats);
        ResourceLocation advancementId = parseResourceLocation(advancement, "advancement requirement", line, "advancement id", stats);
        if (dimensionId == null || advancementId == null) {
            return;
        }

        mutable.computeIfAbsent(dimensionId.toString(), ignored -> new MutablePortalDefinition()).advancements.add(advancementId.toString());
    }

    private static void addBypassRequirement(Map<String, MutablePortalDefinition> mutable, String line, ReloadStats stats) {
        String[] parts = line.split("\\|");
        if (parts.length != 2) {
            stats.warnInvalid("bypass item", line, "expected format destination_dimension|item_id");
            return;
        }

        String dimension = normalize(parts[0]);
        String item = normalize(parts[1]);

        if (isIllustrative(item)) {
            return;
        }

        ResourceLocation dimensionId = parseResourceLocation(dimension, "bypass item", line, "destination dimension", stats);
        ResourceLocation itemId = parseResourceLocation(item, "bypass item", line, "item id", stats);
        if (dimensionId == null || itemId == null) {
            return;
        }

        if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
            stats.missingRegistry(RequirementType.ITEM, dimensionId, itemId);
            return;
        }

        mutable.computeIfAbsent(dimensionId.toString(), ignored -> new MutablePortalDefinition()).bypassItems.add(itemId.toString());
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isIllustrative(String value) {
        return value.startsWith("example:");
    }

    private static ResourceLocation parseResourceLocation(String value, String kind, String line, String fieldName, ReloadStats stats) {
        ResourceLocation location = ResourceLocation.tryParse(value);
        if (location == null) {
            stats.warnInvalid(kind, line, fieldName + " must be a valid resource location");
        }
        return location;
    }

    private enum RequirementType {
        KILL,
        ITEM;

        private String logName() {
            return switch (this) {
                case KILL -> "kill requirement";
                case ITEM -> "item requirement";
            };
        }

        private String registryName() {
            return switch (this) {
                case KILL -> "entity";
                case ITEM -> "item";
            };
        }
    }

    private static final class MutablePortalDefinition {
        private final Map<String, Integer> kills = new LinkedHashMap<>();
        private final Map<String, Integer> items = new LinkedHashMap<>();
        private final Set<String> advancements = new LinkedHashSet<>();
        private final Set<String> bypassItems = new LinkedHashSet<>();
    }

    private static final class ReloadStats {
        private int inactiveMissingRegistry;

        private void warnInvalid(String kind, String line, String reason) {
            DynamicPortals.LOGGER.warn("Ignoring {} config line '{}': {}.", kind, line, reason);
        }

        private void missingRegistry(RequirementType type, ResourceLocation dimensionId, ResourceLocation targetId) {
            inactiveMissingRegistry++;
            DynamicPortals.LOGGER.debug(
                "Keeping {} inactive for {} because {} id '{}' is not registered.",
                type.logName(),
                dimensionId,
                type.registryName(),
                targetId
            );
        }
    }
}
