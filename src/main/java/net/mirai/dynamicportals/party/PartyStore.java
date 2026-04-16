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
    public static UUID createParty(ServerPlayer creator, String password, String alias) {
        PartyData data = getPartyData(creator);
        return data.createParty(creator.getUUID(), password, alias);
    }

    /**
     * Get the party ID that a player is currently part of.
     */
    public static UUID getPlayerParty(ServerPlayer player) {
        PartyData data = getPartyData(player);
        return data.getPlayerParty(player.getUUID());
    }

    /**
     * Add a player to an existing party with a password.
     * Returns true if successful.
     */
    public static boolean joinParty(ServerPlayer player, UUID partyId, String password) {
        PartyData data = getPartyData(player);
        return data.addMember(partyId, player.getUUID(), password);
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
     * Get all online members of a party.
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
}
