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

    private PortalRequirement(Builder builder) {
        this.dimension = builder.dimension;
        this.requiredAdvancement = builder.requiredAdvancement;
        this.requiredMobs = Collections.unmodifiableList(builder.requiredMobs);
        this.requiredBosses = Collections.unmodifiableList(builder.requiredBosses);
        this.requiredItems = Collections.unmodifiableList(builder.requiredItems);
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

    public static Builder builder(ResourceLocation dimension) {
        return new Builder(dimension);
    }

    public static class Builder {
        private final ResourceLocation dimension;
        private ResourceLocation requiredAdvancement;
        private final List<EntityType<?>> requiredMobs = new ArrayList<>();
        private final List<EntityType<?>> requiredBosses = new ArrayList<>();
        private final List<Item> requiredItems = new ArrayList<>();

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

        public PortalRequirement build() {
            return new PortalRequirement(this);
        }
    }
}
