package net.mirai.dynamicportals.party;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * SavedData class that persists the world-level party registry.
 * Stores metadata for all active parties: creator, password hash, member UUIDs.
 */
public class PartyData extends SavedData {
    private static final String NAME = "dynamicportals_parties";
    private static final String PARTIES_KEY = "parties";
    private static final String ID_KEY = "id";
    private static final String CREATOR_KEY = "creator";
    private static final String PASSWORD_HASH_KEY = "password_hash";
    private static final String MEMBERS_KEY = "members";
    private static final String MEMBER_UUID_KEY = "uuid";
    private static final String SHORT_CODE_KEY = "short_code";
    private static final String ALIAS_KEY = "alias";
    private static final int SHORT_CODE_BASE_LENGTH = 6;
    private static final int SHORT_CODE_MAX_SUFFIX = 9;

    private final Map<UUID, PartyInfo> parties = new HashMap<>();
    private final Map<String, UUID> shortCodeToPartyId = new HashMap<>();

    public PartyData() {
    }

    /**
     * Get the party data from the world. Creates a new instance if none exists.
     */
    public static PartyData get(Level level) {
        var storage = level.getServer().getLevel(Level.OVERWORLD).getDataStorage();
        return storage.computeIfAbsent(
            new SavedData.Factory<>(
                PartyData::new,
                PartyData::load
            ),
            NAME
        );
    }

    /**
     * Load existing party data from NBT.
     */
    public static PartyData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        PartyData data = new PartyData();
        ListTag partiesList = tag.getList(PARTIES_KEY, Tag.TAG_COMPOUND);

        for (int i = 0; i < partiesList.size(); i++) {
            CompoundTag partyTag = partiesList.getCompound(i);
            UUID partyId = partyTag.getUUID(ID_KEY);
            UUID creator = partyTag.getUUID(CREATOR_KEY);
            String passwordHash = partyTag.getString(PASSWORD_HASH_KEY);
            String alias = normalizeAlias(partyTag.getString(ALIAS_KEY));
            String shortCode = normalizeShortCode(partyTag.getString(SHORT_CODE_KEY));
            
            Set<UUID> members = new HashSet<>();
            ListTag membersTag = partyTag.getList(MEMBERS_KEY, Tag.TAG_COMPOUND);
            for (int j = 0; j < membersTag.size(); j++) {
                CompoundTag memberTag = membersTag.getCompound(j);
                members.add(memberTag.getUUID(MEMBER_UUID_KEY));
            }

            PartyInfo info = new PartyInfo(creator, passwordHash, members, shortCode, alias);
            data.parties.put(partyId, info);
            if (shortCode != null && !data.shortCodeToPartyId.containsKey(shortCode)) {
                data.shortCodeToPartyId.put(shortCode, partyId);
            }
        }

        return data;
    }

    /**
     * Save party data to NBT. Called automatically by the game.
     */
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag partiesList = new ListTag();

        for (Map.Entry<UUID, PartyInfo> entry : parties.entrySet()) {
            CompoundTag partyTag = new CompoundTag();
            partyTag.putUUID(ID_KEY, entry.getKey());
            partyTag.putUUID(CREATOR_KEY, entry.getValue().creator);
            partyTag.putString(PASSWORD_HASH_KEY, entry.getValue().passwordHash);
            if (entry.getValue().shortCode != null) {
                partyTag.putString(SHORT_CODE_KEY, entry.getValue().shortCode);
            }
            if (entry.getValue().alias != null) {
                partyTag.putString(ALIAS_KEY, entry.getValue().alias);
            }

            ListTag membersTag = new ListTag();
            for (UUID member : entry.getValue().members) {
                CompoundTag memberTag = new CompoundTag();
                memberTag.putUUID(MEMBER_UUID_KEY, member);
                membersTag.add(memberTag);
            }
            partyTag.put(MEMBERS_KEY, membersTag);
            partiesList.add(partyTag);
        }

        tag.put(PARTIES_KEY, partiesList);
        return tag;
    }

    /**
     * Create a new party with the given password.
     */
    public UUID createParty(UUID creator, String password, String alias) {
        UUID partyId = UUID.randomUUID();
        String passwordHash = hashPassword(password);
        Set<UUID> members = new HashSet<>();
        members.add(creator);

        String shortCode = generateShortCode(partyId);
        String sanitizedAlias = normalizeAlias(alias);
        
        parties.put(partyId, new PartyInfo(creator, passwordHash, members, shortCode, sanitizedAlias));
        shortCodeToPartyId.put(shortCode, partyId);
        setDirty();
        return partyId;
    }

    /**
     * Add a member to an existing party if the password matches.
     * Returns true if successful, false if password incorrect or party doesn't exist.
     */
    public boolean addMember(UUID partyId, UUID player, String password) {
        PartyInfo info = parties.get(partyId);
        if (info == null) {
            return false;
        }

        String passwordHash = hashPassword(password);
        if (!passwordHash.equals(info.passwordHash)) {
            return false;
        }

        info.members.add(player);
        setDirty();
        return true;
    }

    /**
     * Remove a member from a party. If the party becomes empty, it is automatically removed.
     */
    public void removeMember(UUID partyId, UUID player) {
        PartyInfo info = parties.get(partyId);
        if (info != null) {
            info.members.remove(player);
            if (info.members.isEmpty()) {
                parties.remove(partyId);
                if (info.shortCode != null) {
                    shortCodeToPartyId.remove(info.shortCode);
                }
            }
            setDirty();
        }
    }

    /**
     * Get the party ID that a player belongs to, or null if not in a party.
     */
    public UUID getPlayerParty(UUID player) {
        for (Map.Entry<UUID, PartyInfo> entry : parties.entrySet()) {
            if (entry.getValue().members.contains(player)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Get all members of a party (including offline members).
     */
    public Set<UUID> getPartyMembers(UUID partyId) {
        PartyInfo info = parties.get(partyId);
        return info != null ? new HashSet<>(info.members) : new HashSet<>();
    }

    /**
     * Get the creator of a party.
     */
    public UUID getPartyCreator(UUID partyId) {
        PartyInfo info = parties.get(partyId);
        return info != null ? info.creator : null;
    }

    /**
     * Delete a party (only creator can do this).
     */
    public boolean dissolveParty(UUID partyId, UUID requester) {
        PartyInfo info = parties.get(partyId);
        if (info != null && info.creator.equals(requester)) {
            parties.remove(partyId);
            if (info.shortCode != null) {
                shortCodeToPartyId.remove(info.shortCode);
            }
            setDirty();
            return true;
        }
        return false;
    }

    /**
     * Check if a party exists.
     */
    public boolean partyExists(UUID partyId) {
        return parties.containsKey(partyId);
    }

    /**
     * Resolve a user-provided key to a party ID. Accepts UUID or short code.
     */
    public UUID resolvePartyId(String partyKey) {
        if (partyKey == null || partyKey.isBlank()) {
            return null;
        }

        try {
            UUID uuid = UUID.fromString(partyKey);
            return parties.containsKey(uuid) ? uuid : null;
        } catch (IllegalArgumentException ignored) {
            return shortCodeToPartyId.get(normalizeShortCode(partyKey));
        }
    }

    /**
     * Get or lazily generate a short code for a party.
     */
    public String getPartyShortCode(UUID partyId) {
        PartyInfo info = parties.get(partyId);
        if (info == null) {
            return null;
        }

        if (info.shortCode == null) {
            String generated = generateShortCode(partyId);
            info.shortCode = generated;
            shortCodeToPartyId.put(generated, partyId);
            setDirty();
        }

        return info.shortCode;
    }

    /**
     * Get the optional human-friendly alias for a party.
     */
    public String getPartyAlias(UUID partyId) {
        PartyInfo info = parties.get(partyId);
        return info != null ? info.alias : null;
    }

    private String generateShortCode(UUID partyId) {
        String normalized = partyId.toString().replace("-", "").toLowerCase();
        String baseCode = normalized.substring(0, SHORT_CODE_BASE_LENGTH);

        if (!shortCodeToPartyId.containsKey(baseCode)) {
            return baseCode;
        }

        for (int i = 1; i <= SHORT_CODE_MAX_SUFFIX; i++) {
            String candidate = baseCode + i;
            if (!shortCodeToPartyId.containsKey(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Unable to allocate unique short code for party " + partyId);
    }

    private static String normalizeShortCode(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeAlias(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Missing SHA-256 algorithm in current Java runtime.", ex);
        }
    }

    /**
     * PartyInfo record holding metadata for a single party.
     */
    public static class PartyInfo {
        public final UUID creator;
        public final String passwordHash;
        public final Set<UUID> members;
        public String shortCode;
        public String alias;

        public PartyInfo(UUID creator, String passwordHash, Set<UUID> members, String shortCode, String alias) {
            this.creator = creator;
            this.passwordHash = passwordHash;
            this.members = members;
            this.shortCode = shortCode;
            this.alias = alias;
        }
    }
}
