package net.mirai.dynamicportals.party;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * SavedData class that persists the world-level party registry.
 * Stores metadata for all active parties: creator, invite code, member UUIDs.
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
    private static final String KILLS_KEY = "kills";
    private static final String ITEMS_KEY = "items";
    private static final String ADVANCEMENTS_KEY = "advancements";
    private static final String COMPLETED_REQUIREMENTS_KEY = "completed_requirements";
    private static final int SHORT_CODE_LENGTH = 5;
    private static final int SHORT_CODE_MAX_ATTEMPTS = 256;
    private static final String SHORT_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

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
            Map<String, Integer> kills = readIntMap(partyTag.getCompound(KILLS_KEY));
            Map<String, Integer> items = readIntMap(partyTag.getCompound(ITEMS_KEY));
            Set<String> advancements = readStringSet(partyTag.getList(ADVANCEMENTS_KEY, Tag.TAG_STRING));
            Set<String> completedRequirements = readStringSet(partyTag.getList(COMPLETED_REQUIREMENTS_KEY, Tag.TAG_STRING));
            
            Set<UUID> members = new HashSet<>();
            ListTag membersTag = partyTag.getList(MEMBERS_KEY, Tag.TAG_COMPOUND);
            for (int j = 0; j < membersTag.size(); j++) {
                CompoundTag memberTag = membersTag.getCompound(j);
                members.add(memberTag.getUUID(MEMBER_UUID_KEY));
            }

            PartyInfo info = new PartyInfo(creator, passwordHash, members, shortCode, alias, kills, items, advancements, completedRequirements);
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
            partyTag.put(KILLS_KEY, writeIntMap(entry.getValue().kills));
            partyTag.put(ITEMS_KEY, writeIntMap(entry.getValue().items));
            partyTag.put(ADVANCEMENTS_KEY, writeStringSet(entry.getValue().advancements));
            partyTag.put(COMPLETED_REQUIREMENTS_KEY, writeStringSet(entry.getValue().completedRequirements));

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
     * Create a new passwordless party. The creator is automatically added as the first member.
     */
    public UUID createParty(UUID creator) {
        UUID partyId = UUID.randomUUID();
        Set<UUID> members = new HashSet<>();
        members.add(creator);

        String shortCode = generateShortCode();
        
        parties.put(partyId, new PartyInfo(
            creator,
            "",
            members,
            shortCode,
            null,
            new HashMap<>(),
            new HashMap<>(),
            new HashSet<>(),
            new HashSet<>()
        ));
        shortCodeToPartyId.put(shortCode, partyId);
        setDirty();
        return partyId;
    }

    /**
     * Add a member to an existing party by invite code. Password hashes are legacy metadata only.
     */
    public boolean addMember(UUID partyId, UUID player) {
        PartyInfo info = parties.get(partyId);
        if (info == null) {
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
            } else if (info.creator.equals(player)) {
                info.creator = info.members.iterator().next();
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
            String generated = generateShortCode();
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

    public int addPartyKill(UUID partyId, String dimension, String entityId, int amount) {
        PartyInfo info = parties.get(partyId);
        if (info == null) {
            return 0;
        }
        int current = increment(info.kills, progressKey(dimension, entityId), amount);
        setDirty();
        return current;
    }

    public int getPartyKillCount(UUID partyId, String dimension, String entityId) {
        PartyInfo info = parties.get(partyId);
        return info == null ? 0 : info.kills.getOrDefault(progressKey(dimension, entityId), 0);
    }

    public int addPartyItem(UUID partyId, String dimension, String itemId, int amount) {
        PartyInfo info = parties.get(partyId);
        if (info == null) {
            return 0;
        }
        int current = increment(info.items, progressKey(dimension, itemId), amount);
        setDirty();
        return current;
    }

    public int getPartyItemCount(UUID partyId, String dimension, String itemId) {
        PartyInfo info = parties.get(partyId);
        return info == null ? 0 : info.items.getOrDefault(progressKey(dimension, itemId), 0);
    }

    public boolean markPartyAdvancement(UUID partyId, String dimension, String advancementId) {
        PartyInfo info = parties.get(partyId);
        if (info == null) {
            return false;
        }
        boolean added = info.advancements.add(progressKey(dimension, advancementId));
        if (added) {
            setDirty();
        }
        return added;
    }

    public boolean hasPartyAdvancement(UUID partyId, String dimension, String advancementId) {
        PartyInfo info = parties.get(partyId);
        return info != null && info.advancements.contains(progressKey(dimension, advancementId));
    }

    public boolean isPartyRequirementCompleted(UUID partyId, String requirementKey) {
        PartyInfo info = parties.get(partyId);
        return info != null && info.completedRequirements.contains(requirementKey);
    }

    public void markPartyRequirementCompleted(UUID partyId, String requirementKey) {
        PartyInfo info = parties.get(partyId);
        if (info != null && info.completedRequirements.add(requirementKey)) {
            setDirty();
        }
    }

    public boolean resetPartyProgress(UUID partyId) {
        PartyInfo info = parties.get(partyId);
        if (info == null) {
            return false;
        }

        boolean changed = false;
        if (!info.kills.isEmpty()) {
            info.kills.clear();
            changed = true;
        }
        if (!info.items.isEmpty()) {
            info.items.clear();
            changed = true;
        }
        if (!info.advancements.isEmpty()) {
            info.advancements.clear();
            changed = true;
        }
        if (!info.completedRequirements.isEmpty()) {
            info.completedRequirements.clear();
            changed = true;
        }
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean resetPartyProgressForDimension(UUID partyId, String dimension) {
        PartyInfo info = parties.get(partyId);
        if (info == null) {
            return false;
        }

        String dimensionPrefix = dimension + "|";
        String completedMarker = "|" + dimension + "|";
        boolean changed = false;
        changed |= info.kills.keySet().removeIf(key -> key.startsWith(dimensionPrefix));
        changed |= info.items.keySet().removeIf(key -> key.startsWith(dimensionPrefix));
        changed |= info.advancements.removeIf(key -> key.startsWith(dimensionPrefix));
        changed |= info.completedRequirements.removeIf(key -> key.contains(completedMarker));
        if (changed) {
            setDirty();
        }
        return changed;
    }

    private String generateShortCode() {
        for (int attempt = 0; attempt < SHORT_CODE_MAX_ATTEMPTS; attempt++) {
            StringBuilder builder = new StringBuilder(SHORT_CODE_LENGTH);
            for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
                builder.append(SHORT_CODE_ALPHABET.charAt(RANDOM.nextInt(SHORT_CODE_ALPHABET.length())));
            }

            String candidate = builder.toString();
            if (!shortCodeToPartyId.containsKey(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Unable to allocate unique party code.");
    }

    private static String normalizeShortCode(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toUpperCase(java.util.Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeAlias(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static int increment(Map<String, Integer> counters, String key, int amount) {
        int value = counters.getOrDefault(key, 0) + amount;
        counters.put(key, value);
        return value;
    }

    private static String progressKey(String dimension, String target) {
        return dimension + "|" + target;
    }

    private static Map<String, Integer> readIntMap(CompoundTag tag) {
        Map<String, Integer> values = new HashMap<>();
        for (String key : tag.getAllKeys()) {
            if (tag.contains(key, Tag.TAG_INT)) {
                values.put(key, tag.getInt(key));
            }
        }
        return values;
    }

    private static CompoundTag writeIntMap(Map<String, Integer> values) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            tag.putInt(entry.getKey(), entry.getValue());
        }
        return tag;
    }

    private static Set<String> readStringSet(ListTag list) {
        Set<String> values = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            values.add(list.getString(i));
        }
        return values;
    }

    private static ListTag writeStringSet(Set<String> values) {
        ListTag list = new ListTag();
        for (String value : values) {
            list.add(net.minecraft.nbt.StringTag.valueOf(value));
        }
        return list;
    }

    /**
     * PartyInfo record holding metadata for a single party.
     */
    public static class PartyInfo {
        public UUID creator;
        public final String passwordHash;
        public final Set<UUID> members;
        public String shortCode;
        public String alias;
        public final Map<String, Integer> kills;
        public final Map<String, Integer> items;
        public final Set<String> advancements;
        public final Set<String> completedRequirements;

        public PartyInfo(
            UUID creator,
            String passwordHash,
            Set<UUID> members,
            String shortCode,
            String alias,
            Map<String, Integer> kills,
            Map<String, Integer> items,
            Set<String> advancements,
            Set<String> completedRequirements
        ) {
            this.creator = creator;
            this.passwordHash = passwordHash;
            this.members = members;
            this.shortCode = shortCode;
            this.alias = alias;
            this.kills = kills;
            this.items = items;
            this.advancements = advancements;
            this.completedRequirements = completedRequirements;
        }
    }
}
