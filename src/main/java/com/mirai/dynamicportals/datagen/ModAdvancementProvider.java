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
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Generates advancement JSON files for portal access.
 * Uses VanillaRequirements for mob/item definitions.
 * IMPORTANT: Must stay in sync with vanilla.json config!
 */
public class ModAdvancementProvider extends AdvancementProvider {

    public ModAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper fileHelper) {
        super(output, registries, fileHelper, List.of(new ModAdvancementGenerator()));
    }

    private static class ModAdvancementGenerator implements AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
            
            // Nether Access Advancement
            // IMPORTANT: Using getAllMobs() and getAllBosses() to include mod compatibility mobs
            // This ensures the advancement matches runtime requirements in PortalRequirementRegistry
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
                    .addCriterion("kill_overworld_mobs", ModTriggers.KILL_REQUIREMENT.get().createCriterion(
                            new KillRequirementTrigger.TriggerInstance(
                                    Optional.empty(),
                                    VanillaRequirements.NETHER.getAllMobs(),  // Includes mod compat mobs
                                    VanillaRequirements.NETHER.getAllBosses(),  // Includes mod compat bosses
                                    VanillaRequirements.NETHER.getItems()
                            )
                    ))
                    .addCriterion("has_diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))
                    .requirements(AdvancementRequirements.Strategy.AND)
                    .save(saver, ModConstants.NETHER_ACCESS_ADVANCEMENT.toString());

            // End Access Advancement
            // IMPORTANT: Using getAllMobs() and getAllBosses() to include mod compatibility mobs
            // This ensures the advancement matches runtime requirements in PortalRequirementRegistry
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
                    .addCriterion("kill_nether_mobs", ModTriggers.KILL_REQUIREMENT.get().createCriterion(
                            new KillRequirementTrigger.TriggerInstance(
                                    Optional.empty(),
                                    VanillaRequirements.END.getAllMobs(),  // Includes mod compat mobs
                                    VanillaRequirements.END.getAllBosses(),  // Includes mod compat bosses
                                    VanillaRequirements.END.getItems()
                            )
                    ))
                    .addCriterion("has_netherite", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_INGOT))
                    .requirements(AdvancementRequirements.Strategy.AND)
                    .save(saver, ModConstants.END_ACCESS_ADVANCEMENT.toString());
        }
    }
}
