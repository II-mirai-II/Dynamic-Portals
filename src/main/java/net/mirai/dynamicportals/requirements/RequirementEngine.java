package net.mirai.dynamicportals.requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.mirai.dynamicportals.config.PortalDefinition;
import net.mirai.dynamicportals.party.PartyData;
import net.mirai.dynamicportals.party.PartyStore;
import net.mirai.dynamicportals.progress.ProgressStore;
import net.minecraft.server.level.ServerPlayer;

public final class RequirementEngine {
    private RequirementEngine() {
    }

    public static PortalStatus evaluate(ServerPlayer player, PortalDefinition definition) {
        String dimension = definition.destinationDimension();
        List<String> missing = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : definition.killRequirements().entrySet()) {
            int current = ProgressStore.getKillCount(player, dimension, entry.getKey());
            if (current < entry.getValue()) {
                missing.add("kill|" + entry.getKey() + "|" + current + "|" + entry.getValue());
            }
        }

        for (Map.Entry<String, Integer> entry : definition.itemRequirements().entrySet()) {
            int current = ProgressStore.getItemCount(player, dimension, entry.getKey());
            if (current < entry.getValue()) {
                missing.add("item|" + entry.getKey() + "|" + current + "|" + entry.getValue());
            }
        }

        for (String advancement : definition.advancementRequirements()) {
            if (!ProgressStore.hasAdvancement(player, dimension, advancement)) {
                missing.add("adv|" + advancement + "|0|1");
            }
        }

        boolean allCompleted = missing.isEmpty();
        boolean unlockedBefore = ProgressStore.isPortalUnlocked(player, dimension);
        boolean bypassUnlocked = ProgressStore.isPortalBypassUnlocked(player, dimension);
        boolean newlyUnlocked = false;

        if (allCompleted && !unlockedBefore) {
            newlyUnlocked = ProgressStore.markPortalUnlocked(player, dimension);
        }

        return new PortalStatus(dimension, allCompleted || unlockedBefore || newlyUnlocked, newlyUnlocked, bypassUnlocked, missing);
    }

    /**
     * Evaluate progress aggregated from the player's party.
     * Returns a PortalStatus based on combined party member progress.
     * Individual bypass still doesn't unlock the portal for others.
     */
    public static PortalStatus evaluateParty(ServerPlayer player, PortalDefinition definition) {
        UUID partyId = PartyStore.getPlayerParty(player);
        if (partyId == null) {
            // Player not in party, fall back to individual evaluation
            return evaluate(player, definition);
        }

        String dimension = definition.destinationDimension();
        PartyData partyData = PartyData.get(player.level());
        Set<UUID> partyMembers = partyData.getPartyMembers(partyId);

        // Aggregate progress from all party members (online + offline)
        Map<String, Integer> aggregatedKills = new java.util.HashMap<>();
        Map<String, Integer> aggregatedItems = new java.util.HashMap<>();
        Set<String> aggregatedAdvancements = new java.util.HashSet<>();

        for (UUID memberId : partyMembers) {
            ServerPlayer memberPlayer = player.getServer().getPlayerList().getPlayer(memberId);
            
            // For online members, get their actual progress
            if (memberPlayer != null) {
                // Aggregate kills
                for (Map.Entry<String, Integer> entry : definition.killRequirements().entrySet()) {
                    String key = entry.getKey();
                    int current = ProgressStore.getKillCount(memberPlayer, dimension, key);
                    aggregatedKills.put(key, aggregatedKills.getOrDefault(key, 0) + current);
                }

                // Aggregate items
                for (Map.Entry<String, Integer> entry : definition.itemRequirements().entrySet()) {
                    String key = entry.getKey();
                    int current = ProgressStore.getItemCount(memberPlayer, dimension, key);
                    aggregatedItems.put(key, aggregatedItems.getOrDefault(key, 0) + current);
                }

                // Aggregate advancements (union set)
                for (String advancement : definition.advancementRequirements()) {
                    if (ProgressStore.hasAdvancement(memberPlayer, dimension, advancement)) {
                        aggregatedAdvancements.add(advancement);
                    }
                }
            }
            // Note: Offline member progress would require loading their NBT data, 
            // which is not implemented in v1. See Phase 8 for future optimization.
        }

        // Check if aggregated progress meets requirements
        List<String> missing = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : definition.killRequirements().entrySet()) {
            int current = aggregatedKills.getOrDefault(entry.getKey(), 0);
            if (current < entry.getValue()) {
                missing.add("kill|" + entry.getKey() + "|" + current + "|" + entry.getValue());
            }
        }

        for (Map.Entry<String, Integer> entry : definition.itemRequirements().entrySet()) {
            int current = aggregatedItems.getOrDefault(entry.getKey(), 0);
            if (current < entry.getValue()) {
                missing.add("item|" + entry.getKey() + "|" + current + "|" + entry.getValue());
            }
        }

        for (String advancement : definition.advancementRequirements()) {
            if (!aggregatedAdvancements.contains(advancement)) {
                missing.add("adv|" + advancement + "|0|1");
            }
        }

        boolean partyCompleted = missing.isEmpty();
        boolean individualBypass = ProgressStore.isPortalBypassUnlocked(player, dimension);

        // Party unlock is different from individual unlock
        // An individual player with bypass can enter, but the party unlock is separate
        return new PortalStatus(dimension, partyCompleted || individualBypass, false, individualBypass, missing);
    }

    public static boolean isBypassItem(PortalDefinition definition, String itemId) {
        Set<String> bypassItems = definition.bypassItems();
        return bypassItems.contains(itemId);
    }

    public record PortalStatus(String dimension, boolean unlocked, boolean newlyUnlocked, boolean bypassUnlocked, List<String> missingEntries) {
    }
}
