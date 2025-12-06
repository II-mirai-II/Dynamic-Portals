package com.mirai.dynamicportals.data;

import com.mirai.dynamicportals.progress.IProgressData;
import com.mirai.dynamicportals.util.ModConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/**
 * World-level progress data shared by all players.
 * Stored using Minecraft's SavedData system for persistence.
 */
public class GlobalProgressData extends SavedData implements IProgressData {
    
    private static final String DATA_NAME = "dynamicportals_global_progress";
    
    private final Map<EntityType<?>, Boolean> killedMobs = new HashMap<>();
    private final Set<Item> obtainedItems = new HashSet<>();
    private final Set<ResourceLocation> unlockedAchievements = new HashSet<>();
    private int dataVersion = ModConstants.CURRENT_DATA_VERSION;
    
    public GlobalProgressData() {
        super();
    }
    
    public GlobalProgressData(CompoundTag nbt, HolderLookup.Provider provider) {
        this();
        load(nbt, provider);
    }
    
    /**
     * Get or create global progress data for the given level.
     */
    public static GlobalProgressData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(
                GlobalProgressData::new,
                GlobalProgressData::new,
                null
            ),
            DATA_NAME
        );
    }
    
    // IProgressData implementation
    @Override
    public void recordMobKill(EntityType<?> mobType) {
        killedMobs.put(mobType, true);
        setDirty();
    }
    
    @Override
    public boolean hasMobBeenKilled(EntityType<?> mobType) {
        return killedMobs.getOrDefault(mobType, false);
    }
    
    @Override
    public void recordItemObtained(Item item) {
        obtainedItems.add(item);
        setDirty();
    }
    
    @Override
    public boolean hasItemBeenObtained(Item item) {
        return obtainedItems.contains(item);
    }
    
    @Override
    public void recordAdvancementUnlocked(ResourceLocation advancement) {
        unlockedAchievements.add(advancement);
        setDirty();
    }
    
    @Override
    public boolean hasAdvancementBeenUnlocked(ResourceLocation advancement) {
        return unlockedAchievements.contains(advancement);
    }
    
    @Override
    public Map<EntityType<?>, Boolean> getKilledMobs() {
        return Collections.unmodifiableMap(killedMobs);
    }
    
    @Override
    public Set<Item> getObtainedItems() {
        return Collections.unmodifiableSet(obtainedItems);
    }
    
    @Override
    public Set<ResourceLocation> getUnlockedAchievements() {
        return Collections.unmodifiableSet(unlockedAchievements);
    }
    
    @Override
    public boolean isGlobal() {
        return true;
    }
    
    // SavedData serialization
    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
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
        for (Item item : obtainedItems) {
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
    
    private void load(CompoundTag nbt, HolderLookup.Provider provider) {
        // Load data version
        dataVersion = nbt.getInt(ModConstants.NBT_DATA_VERSION);
        
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
                Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemId);
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
}
