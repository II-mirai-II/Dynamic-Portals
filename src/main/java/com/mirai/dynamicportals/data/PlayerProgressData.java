package com.mirai.dynamicportals.data;

import com.mirai.dynamicportals.util.ModConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.*;

public class PlayerProgressData implements INBTSerializable<CompoundTag> {
    private final Map<EntityType<?>, Boolean> killedMobs = new HashMap<>();
    private final Set<net.minecraft.world.item.Item> obtainedItems = new HashSet<>();
    private final Set<ResourceLocation> unlockedAchievements = new HashSet<>();
    private int dataVersion = ModConstants.CURRENT_DATA_VERSION;

    public PlayerProgressData() {
    }

    // Mob kill tracking
    public void markMobKilled(EntityType<?> entityType) {
        killedMobs.put(entityType, true);
    }

    public boolean hasMobBeenKilled(EntityType<?> entityType) {
        return killedMobs.getOrDefault(entityType, false);
    }

    public Map<EntityType<?>, Boolean> getKilledMobs() {
        return Collections.unmodifiableMap(killedMobs);
    }

    // Item tracking
    public void markItemObtained(net.minecraft.world.item.Item item) {
        obtainedItems.add(item);
    }

    public boolean hasItemBeenObtained(net.minecraft.world.item.Item item) {
        return obtainedItems.contains(item);
    }

    public Set<net.minecraft.world.item.Item> getObtainedItems() {
        return Collections.unmodifiableSet(obtainedItems);
    }

    // Achievement tracking
    public void unlockAchievement(ResourceLocation achievement) {
        unlockedAchievements.add(achievement);
    }

    public boolean isAchievementUnlocked(ResourceLocation achievement) {
        return unlockedAchievements.contains(achievement);
    }

    public Set<ResourceLocation> getUnlockedAchievements() {
        return Collections.unmodifiableSet(unlockedAchievements);
    }

    // NBT Serialization
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        
        nbt.putInt(ModConstants.NBT_DATA_VERSION, dataVersion);

        // Save killed mobs
        CompoundTag mobsTag = new CompoundTag();
        for (Map.Entry<EntityType<?>, Boolean> entry : killedMobs.entrySet()) {
            ResourceLocation mobId = EntityType.getKey(entry.getKey());
            if (mobId != null) {
                mobsTag.putBoolean(mobId.toString(), entry.getValue());
            }
        }
        nbt.put(ModConstants.NBT_KILLED_MOBS, mobsTag);

        // Save obtained items
        ListTag itemsTag = new ListTag();
        for (net.minecraft.world.item.Item item : obtainedItems) {
            ResourceLocation itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
            if (itemId != null) {
                itemsTag.add(StringTag.valueOf(itemId.toString()));
            }
        }
        nbt.put(ModConstants.NBT_OBTAINED_ITEMS, itemsTag);

        // Save unlocked achievements
        ListTag achievementsTag = new ListTag();
        for (ResourceLocation achievement : unlockedAchievements) {
            achievementsTag.add(StringTag.valueOf(achievement.toString()));
        }
        nbt.put(ModConstants.NBT_UNLOCKED_ACHIEVEMENTS, achievementsTag);

        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        // Load data version for potential migrations
        dataVersion = nbt.getInt(ModConstants.NBT_DATA_VERSION);
        
        // Migrate data if needed
        if (dataVersion < ModConstants.CURRENT_DATA_VERSION) {
            migrateData(nbt, dataVersion);
        }

        // Load killed mobs
        killedMobs.clear();
        if (nbt.contains(ModConstants.NBT_KILLED_MOBS)) {
            CompoundTag mobsTag = nbt.getCompound(ModConstants.NBT_KILLED_MOBS);
            for (String key : mobsTag.getAllKeys()) {
                ResourceLocation mobId = ResourceLocation.parse(key);
                Optional<EntityType<?>> entityType = EntityType.byString(mobId.toString());
                entityType.ifPresent(type -> killedMobs.put(type, mobsTag.getBoolean(key)));
            }
        }

        // Load obtained items
        obtainedItems.clear();
        if (nbt.contains(ModConstants.NBT_OBTAINED_ITEMS)) {
            ListTag itemsTag = nbt.getList(ModConstants.NBT_OBTAINED_ITEMS, Tag.TAG_STRING);
            for (Tag tag : itemsTag) {
                ResourceLocation itemId = ResourceLocation.parse(tag.getAsString());
                net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemId);
                if (item != null) {
                    obtainedItems.add(item);
                }
            }
        }

        // Load unlocked achievements
        unlockedAchievements.clear();
        if (nbt.contains(ModConstants.NBT_UNLOCKED_ACHIEVEMENTS)) {
            ListTag achievementsTag = nbt.getList(ModConstants.NBT_UNLOCKED_ACHIEVEMENTS, Tag.TAG_STRING);
            for (Tag tag : achievementsTag) {
                unlockedAchievements.add(ResourceLocation.parse(tag.getAsString()));
            }
        }
    }

    private void migrateData(CompoundTag nbt, int oldVersion) {
        // Future data migrations can be handled here
        // For now, version 1 is the initial version
        if (oldVersion < 1) {
            // Migration logic for pre-v1 data
        }
    }

    public void copyFrom(PlayerProgressData other) {
        this.killedMobs.clear();
        this.killedMobs.putAll(other.killedMobs);
        this.obtainedItems.clear();
        this.obtainedItems.addAll(other.obtainedItems);
        this.unlockedAchievements.clear();
        this.unlockedAchievements.addAll(other.unlockedAchievements);
        this.dataVersion = other.dataVersion;
    }
}
