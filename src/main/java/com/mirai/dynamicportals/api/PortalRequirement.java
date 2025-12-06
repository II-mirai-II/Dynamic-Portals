package com.mirai.dynamicportals.api;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PortalRequirement {
    private final ResourceLocation dimension;
    private final ResourceLocation requiredAdvancement;
    private final List<EntityType<?>> requiredMobs;
    private final List<EntityType<?>> requiredBosses;
    private final List<Item> requiredItems;
    
    // Display information
    private final String displayName;
    private final String displayDescription;
    private final Integer displayColor;
    private final ResourceLocation displayIcon;
    private final int sortOrder;
    private final String blockedMessage;
    private final String unlockedMessage;

    private PortalRequirement(Builder builder) {
        this.dimension = builder.dimension;
        this.requiredAdvancement = builder.requiredAdvancement;
        this.requiredMobs = Collections.unmodifiableList(builder.requiredMobs);
        this.requiredBosses = Collections.unmodifiableList(builder.requiredBosses);
        this.requiredItems = Collections.unmodifiableList(builder.requiredItems);
        this.displayName = builder.displayName;
        this.displayDescription = builder.displayDescription;
        this.displayColor = builder.displayColor;
        this.displayIcon = builder.displayIcon;
        this.sortOrder = builder.sortOrder;
        this.blockedMessage = builder.blockedMessage;
        this.unlockedMessage = builder.unlockedMessage;
    }

    public ResourceLocation getDimension() {
        return dimension;
    }

    public ResourceLocation getRequiredAdvancement() {
        return requiredAdvancement;
    }

    public List<EntityType<?>> getRequiredMobs() {
        return requiredMobs;
    }

    public List<EntityType<?>> getRequiredBosses() {
        return requiredBosses;
    }

    public List<Item> getRequiredItems() {
        return requiredItems;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDisplayDescription() {
        return displayDescription;
    }
    
    public Integer getDisplayColor() {
        return displayColor;
    }
    
    public ResourceLocation getDisplayIcon() {
        return displayIcon;
    }
    
    public int getSortOrder() {
        return sortOrder;
    }
    
    public String getBlockedMessage() {
        return blockedMessage;
    }
    
    public String getUnlockedMessage() {
        return unlockedMessage;
    }

    public static Builder builder(ResourceLocation dimension) {
        return new Builder(dimension);
    }

    public static class Builder {
        private final ResourceLocation dimension;
        private ResourceLocation requiredAdvancement;
        private final List<EntityType<?>> requiredMobs = new ArrayList<>();
        private final List<EntityType<?>> requiredBosses = new ArrayList<>();
        private final List<Item> requiredItems = new ArrayList<>();
        
        // Display information
        private String displayName;
        private String displayDescription;
        private Integer displayColor;
        private ResourceLocation displayIcon;
        private int sortOrder = 0;
        private String blockedMessage;
        private String unlockedMessage;

        private Builder(ResourceLocation dimension) {
            this.dimension = dimension;
        }

        public Builder advancement(ResourceLocation advancement) {
            this.requiredAdvancement = advancement;
            return this;
        }

        public Builder addMob(EntityType<?> mob) {
            this.requiredMobs.add(mob);
            return this;
        }

        public Builder addMobs(EntityType<?>... mobs) {
            Collections.addAll(this.requiredMobs, mobs);
            return this;
        }

        public Builder addMobsList(List<EntityType<?>> mobs) {
            this.requiredMobs.addAll(mobs);
            return this;
        }

        public Builder addMobsFromTag(TagKey<EntityType<?>> tag) {
            BuiltInRegistries.ENTITY_TYPE.getTag(tag).ifPresent(holders -> {
                holders.forEach(holder -> this.requiredMobs.add(holder.value()));
            });
            return this;
        }

        public Builder addBoss(EntityType<?> boss) {
            this.requiredBosses.add(boss);
            return this;
        }

        public Builder addBosses(EntityType<?>... bosses) {
            Collections.addAll(this.requiredBosses, bosses);
            return this;
        }

        public Builder addBossesList(List<EntityType<?>> bosses) {
            this.requiredBosses.addAll(bosses);
            return this;
        }

        public Builder addBossesFromTag(TagKey<EntityType<?>> tag) {
            BuiltInRegistries.ENTITY_TYPE.getTag(tag).ifPresent(holders -> {
                holders.forEach(holder -> this.requiredBosses.add(holder.value()));
            });
            return this;
        }

        public Builder addItem(Item item) {
            this.requiredItems.add(item);
            return this;
        }

        public Builder addItems(Item... items) {
            Collections.addAll(this.requiredItems, items);
            return this;
        }
        
        public Builder addItemsList(List<Item> items) {
            this.requiredItems.addAll(items);
            return this;
        }
        
        public Builder displayName(String name) {
            this.displayName = name;
            return this;
        }
        
        public Builder displayDescription(String description) {
            this.displayDescription = description;
            return this;
        }
        
        public Builder displayColor(Integer color) {
            this.displayColor = color;
            return this;
        }
        
        public Builder displayIcon(ResourceLocation icon) {
            this.displayIcon = icon;
            return this;
        }
        
        public Builder sortOrder(int order) {
            this.sortOrder = order;
            return this;
        }
        
        public Builder blockedMessage(String message) {
            this.blockedMessage = message;
            return this;
        }
        
        public Builder unlockedMessage(String message) {
            this.unlockedMessage = message;
            return this;
        }

        public PortalRequirement build() {
            // Validate that at least one requirement is specified
            if (requiredMobs.isEmpty() && requiredBosses.isEmpty() && 
                requiredItems.isEmpty() && requiredAdvancement == null) {
                throw new IllegalStateException(
                    "Portal requirement for dimension '" + dimension + "' must have at least one requirement. "
                    + "Add mobs, bosses, items, or an advancement before calling build().");
            }
            return new PortalRequirement(this);
        }
    }
}
