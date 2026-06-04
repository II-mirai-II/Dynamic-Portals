package net.mirai.dynamicportals.party;

import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Utility class for easy access to party operations.
 * Provides static helper methods to manage party membership and lookups.
 */
public final class PartyStore {
    private PartyStore() {
    }

    /**
     * Get the party data from the level (creates if doesn't exist).
     */
    public static PartyData getPartyData(ServerPlayer player) {
        return PartyData.get(player.level());
    }

    /**
     * Create a new party. The creator is automatically added as the first member.
     * Returns the party ID.
     */
    public static UUID createParty(ServerPlayer creator) {
        PartyData data = getPartyData(creator);
        return data.createParty(creator.getUUID());
    }

    /**
     * Get the party ID that a player is currently part of.
     */
    public static UUID getPlayerParty(ServerPlayer player) {
        PartyData data = getPartyData(player);
        return data.getPlayerParty(player.getUUID());
    }

    /**
     * Add a player to an existing party.
     * Returns true if successful.
     */
    public static boolean joinParty(ServerPlayer player, UUID partyId) {
        PartyData data = getPartyData(player);
        return data.addMember(partyId, player.getUUID());
    }

    /**
     * Remove a player from their current party.
     * If the party becomes empty, it is automatically dissolved.
     */
    public static void leaveParty(ServerPlayer player) {
        PartyData data = getPartyData(player);
        UUID partyId = data.getPlayerParty(player.getUUID());
        if (partyId != null) {
            data.removeMember(partyId, player.getUUID());
        }
    }

    /**
     * Get all members of a party, including offline members.
     */
    public static Set<UUID> getPartyMembers(Level level, UUID partyId) {
        PartyData data = PartyData.get(level);
        return data.getPartyMembers(partyId);
    }

    /**
     * Dissolve a party (only creator can do this).
     */
    public static boolean dissolveParty(ServerPlayer requester, UUID partyId) {
        PartyData data = getPartyData(requester);
        return data.dissolveParty(partyId, requester.getUUID());
    }

    /**
     * Check if a player is in a party.
     */
    public static boolean isInParty(ServerPlayer player) {
        return getPlayerParty(player) != null;
    }

    /**
     * Check if a party exists.
     */
    public static boolean partyExists(ServerPlayer player, UUID partyId) {
        PartyData data = getPartyData(player);
        return data.partyExists(partyId);
    }

    /**
     * Resolve a party key (UUID or short code) into the canonical UUID.
     */
    public static UUID resolvePartyId(ServerPlayer player, String partyKey) {
        PartyData data = getPartyData(player);
        return data.resolvePartyId(partyKey);
    }

    /**
     * Get the short code for a party.
     */
    public static String getPartyShortCode(ServerPlayer player, UUID partyId) {
        PartyData data = getPartyData(player);
        return data.getPartyShortCode(partyId);
    }

    /**
     * Get the optional alias for a party.
     */
    public static String getPartyAlias(ServerPlayer player, UUID partyId) {
        PartyData data = getPartyData(player);
        return data.getPartyAlias(partyId);
    }

    public static int addPartyKill(ServerPlayer player, UUID partyId, String dimension, String entityId, int amount) {
        PartyData data = getPartyData(player);
        return data.addPartyKill(partyId, dimension, entityId, amount);
    }

    public static int getPartyKillCount(ServerPlayer player, UUID partyId, String dimension, String entityId) {
        PartyData data = getPartyData(player);
        return data.getPartyKillCount(partyId, dimension, entityId);
    }

    public static int addPartyItem(ServerPlayer player, UUID partyId, String dimension, String itemId, int amount) {
        PartyData data = getPartyData(player);
        return data.addPartyItem(partyId, dimension, itemId, amount);
    }

    public static int getPartyItemCount(ServerPlayer player, UUID partyId, String dimension, String itemId) {
        PartyData data = getPartyData(player);
        return data.getPartyItemCount(partyId, dimension, itemId);
    }

    public static boolean markPartyAdvancement(ServerPlayer player, UUID partyId, String dimension, String advancementId) {
        PartyData data = getPartyData(player);
        return data.markPartyAdvancement(partyId, dimension, advancementId);
    }

    public static boolean hasPartyAdvancement(ServerPlayer player, UUID partyId, String dimension, String advancementId) {
        PartyData data = getPartyData(player);
        return data.hasPartyAdvancement(partyId, dimension, advancementId);
    }

    public static boolean isPartyRequirementCompleted(ServerPlayer player, UUID partyId, String requirementKey) {
        PartyData data = getPartyData(player);
        return data.isPartyRequirementCompleted(partyId, requirementKey);
    }

    public static void markPartyRequirementCompleted(ServerPlayer player, UUID partyId, String requirementKey) {
        PartyData data = getPartyData(player);
        data.markPartyRequirementCompleted(partyId, requirementKey);
    }

    public static boolean resetPartyProgress(ServerPlayer player, UUID partyId) {
        PartyData data = getPartyData(player);
        return data.resetPartyProgress(partyId);
    }

    public static boolean resetPartyProgressForDimension(ServerPlayer player, UUID partyId, String dimension) {
        PartyData data = getPartyData(player);
        return data.resetPartyProgressForDimension(partyId, dimension);
    }
}
