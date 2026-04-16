package net.mirai.dynamicportals.events;

import net.mirai.dynamicportals.chat.ProgressNotifier;
import net.mirai.dynamicportals.config.PortalDefinition;
import net.mirai.dynamicportals.config.PortalRules;
import net.mirai.dynamicportals.progress.ProgressStore;
import net.mirai.dynamicportals.requirements.RequirementEngine;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class ProgressEvents {
    @SubscribeEvent
    public void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || player.level().isClientSide) {
            return;
        }

        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).toString();

        for (PortalDefinition definition : PortalRules.all()) {
            Integer required = definition.killRequirements().get(entityId);
            if (required == null) {
                continue;
            }

            int current = ProgressStore.addKill(player, definition.destinationDimension(), entityId, 1);
            ProgressNotifier.progress(player, "kill", entityId, current, required);
            completeRequirementIfNeeded(player, definition.destinationDimension(), "kill", entityId, current, required);
            unlockPortalIfReady(player, definition);
        }
    }

    @SubscribeEvent
    public void onItemTrackingTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) {
            return;
        }

        if (player.tickCount % 20 != 0) {
            return;
        }

        for (PortalDefinition definition : PortalRules.all()) {
            for (var entry : definition.itemRequirements().entrySet()) {
                String itemId = entry.getKey();
                Integer required = entry.getValue();

                int inventoryCount = countInInventory(player.getInventory(), itemId);
                int snapshot = ProgressStore.getInventorySnapshot(player, itemId);
                if (inventoryCount > snapshot) {
                    int gained = inventoryCount - snapshot;
                    int current = ProgressStore.addItem(player, definition.destinationDimension(), itemId, gained);
                    ProgressNotifier.progress(player, "item", itemId, current, required);
                    completeRequirementIfNeeded(player, definition.destinationDimension(), "item", itemId, current, required);
                    unlockPortalIfReady(player, definition);
                }
                ProgressStore.setInventorySnapshot(player, itemId, inventoryCount);
            }
        }
    }

    @SubscribeEvent
    public void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) {
            return;
        }

        String advancementId = event.getAdvancement().id().toString();

        for (PortalDefinition definition : PortalRules.all()) {
            if (!definition.advancementRequirements().contains(advancementId)) {
                continue;
            }

            boolean firstTime = ProgressStore.markAdvancement(player, definition.destinationDimension(), advancementId);
            if (!firstTime) {
                continue;
            }

            ProgressNotifier.progress(player, "adv", advancementId, 1, 1);
            completeRequirementIfNeeded(player, definition.destinationDimension(), "adv", advancementId, 1, 1);
            unlockPortalIfReady(player, definition);
        }
    }

    @SubscribeEvent
    public void onBypassItemRightClick(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) {
            return;
        }

        ItemStack stack = event.getItemStack();
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        for (PortalDefinition definition : PortalRules.all()) {
            if (!RequirementEngine.isBypassItem(definition, itemId)) {
                continue;
            }

            // Food/drink items should use the consumption event path; this handler is for instant-use bypass items.
            if (!isInstantBypassItem(stack)) {
                return;
            }

            boolean newlyUnlocked = ProgressStore.markPortalUnlocked(player, definition.destinationDimension());
            if (newlyUnlocked) {
                ProgressStore.markPortalBypassUnlocked(player, definition.destinationDimension());
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                ProgressNotifier.bypassUnlocked(player, definition.destinationDimension(), itemId);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
            return;
        }
    }

    @SubscribeEvent
    public void onItemConsumed(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player) || player.level().isClientSide) {
            return;
        }

        String itemId = BuiltInRegistries.ITEM.getKey(event.getItem().getItem()).toString();
        for (PortalDefinition definition : PortalRules.all()) {
            if (!RequirementEngine.isBypassItem(definition, itemId)) {
                continue;
            }

            boolean newlyUnlocked = ProgressStore.markPortalUnlocked(player, definition.destinationDimension());
            if (newlyUnlocked) {
                ProgressStore.markPortalBypassUnlocked(player, definition.destinationDimension());
                ProgressNotifier.bypassUnlocked(player, definition.destinationDimension(), itemId);
            }
        }
    }

    @SubscribeEvent
    public void onTravelToDimension(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) {
            return;
        }

        String destination = event.getDimension().location().toString();
        PortalDefinition definition = PortalRules.byDimension(destination);
        if (definition == null) {
            return;
        }

        // Use party-aware evaluation if player is in a party, otherwise individual evaluation
        RequirementEngine.PortalStatus status = RequirementEngine.evaluateParty(player, definition);
        if (status.unlocked()) {
            if (status.newlyUnlocked()) {
                ProgressNotifier.portalUnlocked(player, destination);
            }
            return;
        }

        event.setCanceled(true);
        ProgressNotifier.portalBlocked(player, destination);
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getOriginal() instanceof ServerPlayer original) || !(event.getEntity() instanceof ServerPlayer clone)) {
            return;
        }
        ProgressStore.copyForClone(original, clone);
    }

    private static void unlockPortalIfReady(ServerPlayer player, PortalDefinition definition) {
        RequirementEngine.PortalStatus status = RequirementEngine.evaluate(player, definition);
        if (status.newlyUnlocked()) {
            ProgressNotifier.portalUnlocked(player, definition.destinationDimension());
        }
    }

    private static void completeRequirementIfNeeded(
        ServerPlayer player,
        String dimension,
        String type,
        String targetId,
        int current,
        int required
    ) {
        if (current < required) {
            return;
        }

        String key = type + "|" + dimension + "|" + targetId;
        if (ProgressStore.isRequirementCompleted(player, key)) {
            return;
        }

        ProgressStore.markRequirementCompleted(player, key);
        ProgressNotifier.requirementCompleted(player, dimension, type, targetId);
    }

    private static int countInInventory(Inventory inventory, String itemId) {
        int total = 0;
        for (ItemStack stack : inventory.items) {
            if (stack.isEmpty()) {
                continue;
            }
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (key != null && itemId.equals(key.toString())) {
                total += stack.getCount();
            }
        }
        for (ItemStack stack : inventory.offhand) {
            if (stack.isEmpty()) {
                continue;
            }
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (key != null && itemId.equals(key.toString())) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static boolean isInstantBypassItem(ItemStack stack) {
        UseAnim useAnim = stack.getUseAnimation();
        return useAnim != UseAnim.EAT && useAnim != UseAnim.DRINK;
    }
}
