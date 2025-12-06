package com.mirai.dynamicportals.advancement;

import com.mirai.dynamicportals.api.PortalRequirement;
import com.mirai.dynamicportals.api.PortalRequirementRegistry;
import com.mirai.dynamicportals.data.ModAttachments;
import com.mirai.dynamicportals.data.PlayerProgressData;
import com.mirai.dynamicportals.util.ModConstants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Optional;

/**
 * Advancement trigger that validates portal requirements.
 * 
 * IMPORTANT: Validates against RUNTIME PortalRequirement (vanilla + mod compat),
 * NOT the static advancement JSON (vanilla only).
 * 
 * The advancement JSON contains vanilla mobs for display/tracking purposes,
 * but this trigger checks the full runtime requirement list including mod compatibility.
 */
public class KillRequirementTrigger extends SimpleCriterionTrigger<KillRequirementTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(player));
    }

    public record TriggerInstance(
            Optional<ContextAwarePredicate> player,
            List<EntityType<?>> requiredMobs,
            List<EntityType<?>> requiredBosses,
            List<Item> requiredItems
    ) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        BuiltInRegistries.ENTITY_TYPE.byNameCodec().listOf().fieldOf("required_mobs").forGetter(TriggerInstance::requiredMobs),
                        BuiltInRegistries.ENTITY_TYPE.byNameCodec().listOf().optionalFieldOf("required_bosses", List.of()).forGetter(TriggerInstance::requiredBosses),
                        BuiltInRegistries.ITEM.byNameCodec().listOf().optionalFieldOf("required_items", List.of()).forGetter(TriggerInstance::requiredItems)
                ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(ServerPlayer player) {
            PlayerProgressData progressData = player.getData(ModAttachments.PLAYER_PROGRESS);

            // Determine which dimension this advancement is for by checking the lists
            ResourceLocation dimension = determineDimension();
            
            if (dimension != null) {
                // Use RUNTIME PortalRequirement (vanilla + mod compat) for validation
                PortalRequirement runtimeRequirement = PortalRequirementRegistry.getInstance().getRequirement(dimension);
                
                if (runtimeRequirement != null) {
                    // Check all RUNTIME required mobs (includes mod compat)
                    for (EntityType<?> mobType : runtimeRequirement.getRequiredMobs()) {
                        if (!progressData.hasMobBeenKilled(mobType)) {
                            return false;
                        }
                    }

                    // Check all RUNTIME required bosses (includes mod compat)
                    for (EntityType<?> bossType : runtimeRequirement.getRequiredBosses()) {
                        if (!progressData.hasMobBeenKilled(bossType)) {
                            return false;
                        }
                    }

                    // Check all RUNTIME required items
                    for (Item item : runtimeRequirement.getRequiredItems()) {
                        if (!hasObtainedItem(player, item)) {
                            return false;
                        }
                    }

                    return true;
                }
            }
            
            // Fallback: Use static advancement data if runtime requirement not found
            // This maintains backward compatibility with custom advancements
            for (EntityType<?> mobType : requiredMobs) {
                if (!progressData.hasMobBeenKilled(mobType)) {
                    return false;
                }
            }

            for (EntityType<?> bossType : requiredBosses) {
                if (!progressData.hasMobBeenKilled(bossType)) {
                    return false;
                }
            }

            for (Item item : requiredItems) {
                if (!hasObtainedItem(player, item)) {
                    return false;
                }
            }

            return true;
        }
        
        /**
         * Determine which dimension this advancement is for based on the mob lists.
         * This allows us to look up the runtime PortalRequirement.
         */
        private ResourceLocation determineDimension() {
            // Check if this is the Nether advancement (contains overworld mobs)
            if (requiredMobs.contains(EntityType.ZOMBIE) || requiredMobs.contains(EntityType.CREEPER)) {
                return ModConstants.NETHER_DIMENSION;
            }
            // Check if this is the End advancement (contains nether mobs)
            if (requiredMobs.contains(EntityType.GHAST) || requiredMobs.contains(EntityType.BLAZE)) {
                return ModConstants.END_DIMENSION;
            }
            return null;
        }

        private boolean hasObtainedItem(ServerPlayer player, Item item) {
            PlayerProgressData progressData = player.getData(ModAttachments.PLAYER_PROGRESS);
            // Check both persistent tracking and current inventory
            return progressData.hasItemBeenObtained(item) || 
                   player.getInventory().hasAnyMatching(stack -> stack.is(item));
        }
    }
}
