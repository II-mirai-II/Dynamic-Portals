package com.mirai.dynamicportals.datagen;

import com.mirai.dynamicportals.advancement.KillRequirementTrigger;
import com.mirai.dynamicportals.advancement.ModTriggers;
import com.mirai.dynamicportals.util.ModConstants;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModAdvancementProvider extends AdvancementProvider {

    public ModAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper fileHelper) {
        super(output, registries, fileHelper, List.of(new ModAdvancementGenerator()));
    }

    private static class ModAdvancementGenerator implements AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
            
            // Nether Access Advancement
            AdvancementHolder netherAccess = Advancement.Builder.advancement()
                    .display(
                            Items.NETHERRACK,
                            Component.translatable(ModConstants.ADV_NETHER_TITLE),
                            Component.translatable(ModConstants.ADV_NETHER_DESC),
                            ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/nether.png"),
                            AdvancementType.CHALLENGE,
                            true,
                            true,
                            false
                    )
                    // Require killing all overworld mobs
                    .addCriterion("kill_overworld_mobs", ModTriggers.KILL_REQUIREMENT.get().createCriterion(
                            new KillRequirementTrigger.TriggerInstance(
                                    Optional.empty(),
                                    List.of(
                                            EntityType.ZOMBIE,
                                            EntityType.SKELETON,
                                            EntityType.CREEPER,
                                            EntityType.SPIDER,
                                            EntityType.ENDERMAN,
                                            EntityType.WITCH,
                                            EntityType.SLIME,
                                            EntityType.DROWNED,
                                            EntityType.HUSK,
                                            EntityType.STRAY,
                                            EntityType.BREEZE,
                                            EntityType.BOGGED,
                                            EntityType.PILLAGER,
                                            EntityType.VINDICATOR,
                                            EntityType.EVOKER
                                    ),
                                    List.of(EntityType.ELDER_GUARDIAN), // Bosses
                                    List.of(Items.DIAMOND)
                            )
                    ))
                    // Require obtaining diamond
                    .addCriterion("has_diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))
                    .requirements(AdvancementRequirements.Strategy.AND)
                    .save(saver, ModConstants.NETHER_ACCESS_ADVANCEMENT.toString());

            // End Access Advancement
            Advancement.Builder.advancement()
                    .parent(netherAccess)
                    .display(
                            Items.END_STONE,
                            Component.translatable(ModConstants.ADV_END_TITLE),
                            Component.translatable(ModConstants.ADV_END_DESC),
                            null,
                            AdvancementType.CHALLENGE,
                            true,
                            true,
                            false
                    )
                    // Require killing all nether mobs
                    .addCriterion("kill_nether_mobs", ModTriggers.KILL_REQUIREMENT.get().createCriterion(
                            new KillRequirementTrigger.TriggerInstance(
                                    Optional.empty(),
                                    List.of(
                                            EntityType.GHAST,
                                            EntityType.BLAZE,
                                            EntityType.WITHER_SKELETON,
                                            EntityType.PIGLIN,
                                            EntityType.PIGLIN_BRUTE,
                                            EntityType.HOGLIN
                                    ),
                                    List.of(EntityType.WARDEN, EntityType.WITHER), // Bosses
                                    List.of(Items.NETHERITE_INGOT)
                            )
                    ))
                    // Require obtaining netherite
                    .addCriterion("has_netherite", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_INGOT))
                    .requirements(AdvancementRequirements.Strategy.AND)
                    .save(saver, ModConstants.END_ACCESS_ADVANCEMENT.toString());
        }
    }
}
