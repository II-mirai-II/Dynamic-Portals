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
 * Generates advancement JSON files for portal access at build time.
 * 
 * IMPORTANT DESIGN PATTERN:
 * - Generated advancement JSONs reference VANILLA mobs/bosses only (static at build time)
 * - At runtime, PortalRequirementRegistry dynamically adds mod compatibility mobs
 * - The KillRequirementTrigger validates against the FULL runtime requirement list
 * - This allows mod compatibility without regenerating advancement files
 * 
 * Example flow:
 * 1. DataGen creates advancement: "Kill 15 vanilla overworld mobs"
 * 2. Runtime loads: vanilla.json (15 mobs) + cataclysm.json (3 bosses) = 18 total
 * 3. Player kills vanilla mobs + Cataclysm bosses
 * 4. KillRequirementTrigger checks against runtime PortalRequirement (18 entities)
 * 5. Advancement granted when ALL 18 are killed (vanilla + mod compat)
 */
public class ModAdvancementProvider extends AdvancementProvider {

    public ModAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper fileHelper) {
        super(output, registries, fileHelper, List.of(new ModAdvancementGenerator()));
    }

    private static class ModAdvancementGenerator implements AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
            
            // Nether Access Advancement
            // References vanilla mobs only - runtime adds mod compat via PortalRequirementRegistry
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
                                    VanillaRequirements.NETHER.getMobs(),  // Vanilla only - mod compat added at runtime
                                    VanillaRequirements.NETHER.getBosses(),  // Vanilla only - mod compat added at runtime
                                    VanillaRequirements.NETHER.getItems()
                            )
                    ))
                    .addCriterion("has_diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))
                    .requirements(AdvancementRequirements.Strategy.AND)
                    .save(saver, ModConstants.NETHER_ACCESS_ADVANCEMENT.toString());

            // End Access Advancement
            // References vanilla mobs only - runtime adds mod compat via PortalRequirementRegistry
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
                                    VanillaRequirements.END.getMobs(),  // Vanilla only - mod compat added at runtime
                                    VanillaRequirements.END.getBosses(),  // Vanilla only - mod compat added at runtime
                                    VanillaRequirements.END.getItems()
                            )
                    ))
                    .addCriterion("has_netherite", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_INGOT))
                    .requirements(AdvancementRequirements.Strategy.AND)
                    .save(saver, ModConstants.END_ACCESS_ADVANCEMENT.toString());
        }
    }
}
