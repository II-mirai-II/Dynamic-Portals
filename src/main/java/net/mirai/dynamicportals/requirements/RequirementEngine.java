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
     * Evaluate progress saved directly on the player's party.
     * Individual bypass still doesn't unlock the portal for others.
     */
    public static PortalStatus evaluateParty(ServerPlayer player, PortalDefinition definition) {
        UUID partyId = PartyStore.getPlayerParty(player);
        if (partyId == null) {
            return evaluate(player, definition);
        }

        String dimension = definition.destinationDimension();
        PartyData partyData = PartyData.get(player.level());
        List<String> missing = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : definition.killRequirements().entrySet()) {
            int current = partyData.getPartyKillCount(partyId, dimension, entry.getKey());
            if (current < entry.getValue()) {
                missing.add("kill|" + entry.getKey() + "|" + current + "|" + entry.getValue());
            }
        }

        for (Map.Entry<String, Integer> entry : definition.itemRequirements().entrySet()) {
            int current = partyData.getPartyItemCount(partyId, dimension, entry.getKey());
            if (current < entry.getValue()) {
                missing.add("item|" + entry.getKey() + "|" + current + "|" + entry.getValue());
            }
        }

        for (String advancement : definition.advancementRequirements()) {
            if (!partyData.hasPartyAdvancement(partyId, dimension, advancement)) {
                missing.add("adv|" + advancement + "|0|1");
            }
        }

        boolean partyCompleted = missing.isEmpty();
        boolean individualBypass = ProgressStore.isPortalBypassUnlocked(player, dimension);

        return new PortalStatus(dimension, partyCompleted || individualBypass, false, individualBypass, missing);
    }

    public static boolean isBypassItem(PortalDefinition definition, String itemId) {
        Set<String> bypassItems = definition.bypassItems();
        return bypassItems.contains(itemId);
    }

    public record PortalStatus(String dimension, boolean unlocked, boolean newlyUnlocked, boolean bypassUnlocked, List<String> missingEntries) {
    }
}
