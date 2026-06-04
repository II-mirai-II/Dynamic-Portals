package net.mirai.dynamicportals.progress;

import java.util.ArrayList;
import java.util.List;
import net.mirai.dynamicportals.DynamicPortals;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

public final class ProgressStore {
    private static final String ROOT = DynamicPortals.MOD_ID;
    private static final String KILLS = "kills";
    private static final String ITEMS = "items";
    private static final String ADVANCEMENTS = "advancements";
    private static final String UNLOCKED = "unlocked";
    private static final String BYPASS_UNLOCKED = "bypass_unlocked";
    private static final String COMPLETED = "completed";
    private static final String INVENTORY_SNAPSHOT = "inventory_snapshot";
    private static final String PARTY_ID = "party_id";

    private ProgressStore() {
    }

    public static int addKill(ServerPlayer player, String dimension, String entityId, int amount) {
        return increment(counterTag(player, KILLS), key(dimension, entityId), amount);
    }

    public static int addItem(ServerPlayer player, String dimension, String itemId, int amount) {
        return increment(counterTag(player, ITEMS), key(dimension, itemId), amount);
    }

    public static int getKillCount(ServerPlayer player, String dimension, String entityId) {
        return counterTag(player, KILLS).getInt(key(dimension, entityId));
    }

    public static int getItemCount(ServerPlayer player, String dimension, String itemId) {
        return counterTag(player, ITEMS).getInt(key(dimension, itemId));
    }

    public static boolean markAdvancement(ServerPlayer player, String dimension, String advancementId) {
        CompoundTag advancements = namedTag(player, ADVANCEMENTS);
        String key = key(dimension, advancementId);
        boolean already = advancements.getBoolean(key);
        if (!already) {
            advancements.putBoolean(key, true);
        }
        return !already;
    }

    public static boolean hasAdvancement(ServerPlayer player, String dimension, String advancementId) {
        return namedTag(player, ADVANCEMENTS).getBoolean(key(dimension, advancementId));
    }

    public static boolean isPortalUnlocked(ServerPlayer player, String dimension) {
        return namedTag(player, UNLOCKED).getBoolean(dimension);
    }

    public static boolean markPortalUnlocked(ServerPlayer player, String dimension) {
        CompoundTag unlocked = namedTag(player, UNLOCKED);
        boolean already = unlocked.getBoolean(dimension);
        if (!already) {
            unlocked.putBoolean(dimension, true);
        }
        return !already;
    }

    public static boolean isPortalBypassUnlocked(ServerPlayer player, String dimension) {
        return namedTag(player, BYPASS_UNLOCKED).getBoolean(dimension);
    }

    public static void markPortalBypassUnlocked(ServerPlayer player, String dimension) {
        namedTag(player, BYPASS_UNLOCKED).putBoolean(dimension, true);
    }

    public static boolean isRequirementCompleted(ServerPlayer player, String requirementKey) {
        return namedTag(player, COMPLETED).getBoolean(requirementKey);
    }

    public static void markRequirementCompleted(ServerPlayer player, String requirementKey) {
        namedTag(player, COMPLETED).putBoolean(requirementKey, true);
    }

    public static int getInventorySnapshot(ServerPlayer player, String itemId) {
        return namedTag(player, INVENTORY_SNAPSHOT).getInt(itemId);
    }

    public static void setInventorySnapshot(ServerPlayer player, String itemId, int count) {
        namedTag(player, INVENTORY_SNAPSHOT).putInt(itemId, count);
    }

    public static void resetAll(ServerPlayer player) {
        CompoundTag root = rootTag(player);
        root.remove(KILLS);
        root.remove(ITEMS);
        root.remove(ADVANCEMENTS);
        root.remove(UNLOCKED);
        root.remove(BYPASS_UNLOCKED);
        root.remove(COMPLETED);
        root.remove(INVENTORY_SNAPSHOT);
    }

    public static void resetDimension(ServerPlayer player, String dimension) {
        String dimensionPrefix = dimension + "|";
        removeKeysStartingWith(namedTag(player, KILLS), dimensionPrefix);
        removeKeysStartingWith(namedTag(player, ITEMS), dimensionPrefix);
        removeKeysStartingWith(namedTag(player, ADVANCEMENTS), dimensionPrefix);
        namedTag(player, UNLOCKED).remove(dimension);
        namedTag(player, BYPASS_UNLOCKED).remove(dimension);
        removeCompletedKeysForDimension(namedTag(player, COMPLETED), dimension);
    }

    public static java.util.UUID getPartyId(ServerPlayer player) {
        CompoundTag root = rootTag(player);
        if (root.hasUUID(PARTY_ID)) {
            return root.getUUID(PARTY_ID);
        }
        return null;
    }

    public static void setPartyId(ServerPlayer player, java.util.UUID partyId) {
        CompoundTag root = rootTag(player);
        if (partyId == null) {
            root.remove(PARTY_ID);
        } else {
            root.putUUID(PARTY_ID, partyId);
        }
    }

    public static void copyForClone(ServerPlayer original, ServerPlayer clone) {
        CompoundTag originalRoot = original.getPersistentData().getCompound(ROOT);
        clone.getPersistentData().put(ROOT, originalRoot.copy());
    }

    private static int increment(CompoundTag tag, String key, int amount) {
        int value = tag.getInt(key) + amount;
        tag.putInt(key, value);
        return value;
    }

    private static CompoundTag counterTag(ServerPlayer player, String key) {
        return namedTag(player, key);
    }

    private static CompoundTag namedTag(ServerPlayer player, String key) {
        CompoundTag root = rootTag(player);
        if (!root.contains(key, Tag.TAG_COMPOUND)) {
            root.put(key, new CompoundTag());
        }
        return root.getCompound(key);
    }

    private static CompoundTag rootTag(ServerPlayer player) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(ROOT, Tag.TAG_COMPOUND)) {
            persistentData.put(ROOT, new CompoundTag());
        }
        return persistentData.getCompound(ROOT);
    }

    private static String key(String dimension, String target) {
        return dimension + "|" + target;
    }

    private static void removeKeysStartingWith(CompoundTag tag, String prefix) {
        List<String> toRemove = new ArrayList<>();
        for (String key : tag.getAllKeys()) {
            if (key.startsWith(prefix)) {
                toRemove.add(key);
            }
        }
        for (String key : toRemove) {
            tag.remove(key);
        }
    }

    private static void removeCompletedKeysForDimension(CompoundTag tag, String dimension) {
        List<String> toRemove = new ArrayList<>();
        String marker = "|" + dimension + "|";
        for (String key : tag.getAllKeys()) {
            if (key.contains(marker)) {
                toRemove.add(key);
            }
        }
        for (String key : toRemove) {
            tag.remove(key);
        }
    }
}
