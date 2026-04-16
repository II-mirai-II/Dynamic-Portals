package net.mirai.dynamicportals.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.mirai.dynamicportals.DynamicPortals;
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
        int killLines = DynamicPortalsConfig.CONFIG.killRequirements.get().size();
        int itemLines = DynamicPortalsConfig.CONFIG.itemRequirements.get().size();
        int advancementLines = DynamicPortalsConfig.CONFIG.advancementRequirements.get().size();
        int bypassLines = DynamicPortalsConfig.CONFIG.consumeBypassItems.get().size();

        for (String line : DynamicPortalsConfig.CONFIG.killRequirements.get()) {
            addCounterRequirement(mutable, line, RequirementType.KILL);
        }

        for (String line : DynamicPortalsConfig.CONFIG.itemRequirements.get()) {
            addCounterRequirement(mutable, line, RequirementType.ITEM);
        }

        for (String line : DynamicPortalsConfig.CONFIG.advancementRequirements.get()) {
            addAdvancementRequirement(mutable, line);
        }

        for (String line : DynamicPortalsConfig.CONFIG.consumeBypassItems.get()) {
            addBypassRequirement(mutable, line);
        }

        Map<String, PortalDefinition> built = new LinkedHashMap<>();
        for (Map.Entry<String, MutablePortalDefinition> entry : mutable.entrySet()) {
            MutablePortalDefinition value = entry.getValue();
            built.put(entry.getKey(), new PortalDefinition(entry.getKey(), value.kills, value.items, value.advancements, value.bypassItems));
        }

        definitions = Map.copyOf(built);
        DynamicPortals.LOGGER.info(
            "Loaded {} portal rule set(s) from config (kills={}, items={}, advancements={}, bypass={}).",
            definitions.size(),
            killLines,
            itemLines,
            advancementLines,
            bypassLines
        );
    }

    public static Collection<PortalDefinition> all() {
        return definitions.values();
    }

    public static PortalDefinition byDimension(String destinationDimension) {
        return definitions.get(destinationDimension);
    }

    private static void addCounterRequirement(Map<String, MutablePortalDefinition> mutable, String line, RequirementType type) {
        String[] parts = line.split("\\|");
        if (parts.length != 3) {
            return;
        }

        String dimension = normalize(parts[0]);
        String target = normalize(parts[1]);
        int amount;

        try {
            amount = Integer.parseInt(parts[2].trim());
        } catch (NumberFormatException ex) {
            return;
        }

        if (amount <= 0 || isIllustrative(target) || !isValidResourceLocation(dimension) || !isValidResourceLocation(target)) {
            return;
        }

        MutablePortalDefinition definition = mutable.computeIfAbsent(dimension, ignored -> new MutablePortalDefinition());
        if (type == RequirementType.KILL) {
            definition.kills.put(target, amount);
        } else {
            definition.items.put(target, amount);
        }
    }

    private static void addAdvancementRequirement(Map<String, MutablePortalDefinition> mutable, String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 2) {
            return;
        }

        String dimension = normalize(parts[0]);
        String advancement = normalize(parts[1]);

        if (isIllustrative(advancement) || !isValidResourceLocation(dimension) || !isValidResourceLocation(advancement)) {
            return;
        }

        mutable.computeIfAbsent(dimension, ignored -> new MutablePortalDefinition()).advancements.add(advancement);
    }

    private static void addBypassRequirement(Map<String, MutablePortalDefinition> mutable, String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 2) {
            return;
        }

        String dimension = normalize(parts[0]);
        String item = normalize(parts[1]);

        if (isIllustrative(item) || !isValidResourceLocation(dimension) || !isValidResourceLocation(item)) {
            return;
        }

        mutable.computeIfAbsent(dimension, ignored -> new MutablePortalDefinition()).bypassItems.add(item);
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isIllustrative(String value) {
        return value.startsWith("example:");
    }

    private static boolean isValidResourceLocation(String value) {
        return ResourceLocation.tryParse(value) != null;
    }

    private enum RequirementType {
        KILL,
        ITEM
    }

    private static final class MutablePortalDefinition {
        private final Map<String, Integer> kills = new LinkedHashMap<>();
        private final Map<String, Integer> items = new LinkedHashMap<>();
        private final Set<String> advancements = new LinkedHashSet<>();
        private final Set<String> bypassItems = new LinkedHashSet<>();
    }
}
