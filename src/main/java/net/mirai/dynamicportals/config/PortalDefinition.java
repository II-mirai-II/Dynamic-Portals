package net.mirai.dynamicportals.config;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import net.mirai.dynamicportals.util.DisplayText;

public record PortalDefinition(
    String destinationDimension,
    Map<String, Integer> killRequirements,
    Map<String, Integer> itemRequirements,
    Set<String> advancementRequirements,
    Set<String> bypassItems
) {
    public PortalDefinition {
        killRequirements = Collections.unmodifiableMap(killRequirements);
        itemRequirements = Collections.unmodifiableMap(itemRequirements);
        advancementRequirements = Collections.unmodifiableSet(advancementRequirements);
        bypassItems = Collections.unmodifiableSet(bypassItems);
    }

    public String displayName() {
        return DisplayText.dimension(destinationDimension);
    }
}
