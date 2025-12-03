package com.mirai.dynamicportals.advancement;

import com.mirai.dynamicportals.data.ModAttachments;
import com.mirai.dynamicportals.data.PlayerProgressData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Optional;

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

            // Check all required mobs have been killed
            for (EntityType<?> mobType : requiredMobs) {
                if (!progressData.hasMobBeenKilled(mobType)) {
                    return false;
                }
            }

            // Check all required bosses have been killed
            for (EntityType<?> bossType : requiredBosses) {
                if (!progressData.hasMobBeenKilled(bossType)) {
                    return false;
                }
            }

            // Check all required items have been obtained (via inventory check)
            for (Item item : requiredItems) {
                if (!hasObtainedItem(player, item)) {
                    return false;
                }
            }

            return true;
        }

        private boolean hasObtainedItem(ServerPlayer player, Item item) {
            PlayerProgressData progressData = player.getData(ModAttachments.PLAYER_PROGRESS);
            // Check both persistent tracking and current inventory
            return progressData.hasItemBeenObtained(item) || 
                   player.getInventory().hasAnyMatching(stack -> stack.is(item));
        }
    }
}
