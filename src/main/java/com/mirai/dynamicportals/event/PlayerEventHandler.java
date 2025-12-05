package com.mirai.dynamicportals.event;

import com.mirai.dynamicportals.DynamicPortals;
import com.mirai.dynamicportals.api.PortalRequirement;
import com.mirai.dynamicportals.api.PortalRequirementRegistry;
import com.mirai.dynamicportals.config.ModConfig;
import com.mirai.dynamicportals.data.ModAttachments;
import com.mirai.dynamicportals.data.PlayerProgressData;
import com.mirai.dynamicportals.network.SyncProgressPacket;
import com.mirai.dynamicportals.util.ModConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class PlayerEventHandler {

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            Player original = event.getOriginal();
            Player newPlayer = event.getEntity();

            // Copy data from old player to new player
            PlayerProgressData oldData = original.getData(ModAttachments.PLAYER_PROGRESS);
            PlayerProgressData newData = newPlayer.getData(ModAttachments.PLAYER_PROGRESS);
            
            newData.copyFrom(oldData);

            // Sync to client if on server
            if (newPlayer instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, SyncProgressPacket.fromProgressData(newData));
            }
        }
    }

    @SubscribeEvent
    public void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        PlayerProgressData progressData = player.getData(ModAttachments.PLAYER_PROGRESS);
        net.minecraft.world.item.Item pickedItem = event.getItemEntity().getItem().getItem();

        // Get all tracked items from registry
        java.util.Set<net.minecraft.world.item.Item> trackedItems = PortalRequirementRegistry.getInstance().getAllTrackedItems();

        // Track items dynamically
        if (trackedItems.contains(pickedItem)) {
            if (!progressData.hasItemBeenObtained(pickedItem)) {
                progressData.markItemObtained(pickedItem);
                
                if (ModConfig.COMMON.debugLogging.get()) {
                    DynamicPortals.LOGGER.debug("Player {} obtained tracked item: {}", 
                            player.getName().getString(), 
                            net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(pickedItem));
                }
                
                // Check if player completed any portal requirements
                if (ModConfig.COMMON.autoGrantAdvancements.get()) {
                    checkPortalCompletion(player, progressData);
                }
                
                // Sync to client
                PacketDistributor.sendToPlayer(player, SyncProgressPacket.fromProgressData(progressData));
            }
        }
    }
    
    /**
     * Check if player has completed all requirements for any portal and unlock it
     */
    private void checkPortalCompletion(ServerPlayer player, PlayerProgressData progressData) {
        // Check Nether Portal
        if (!progressData.isAchievementUnlocked(ModConstants.NETHER_ACCESS_ADVANCEMENT)) {
            if (isPortalCompleted(player, progressData, ModConstants.NETHER_DIMENSION)) {
                progressData.unlockAchievement(ModConstants.NETHER_ACCESS_ADVANCEMENT);
                player.sendSystemMessage(Component.translatable(ModConstants.ADV_NETHER_TITLE)
                        .append(Component.literal(" - "))
                        .append(Component.translatable(ModConstants.ADV_NETHER_DESC)));
            }
        }
        
        // Check End Portal
        if (!progressData.isAchievementUnlocked(ModConstants.END_ACCESS_ADVANCEMENT)) {
            if (isPortalCompleted(player, progressData, ModConstants.END_DIMENSION)) {
                progressData.unlockAchievement(ModConstants.END_ACCESS_ADVANCEMENT);
                player.sendSystemMessage(Component.translatable(ModConstants.ADV_END_TITLE)
                        .append(Component.literal(" - "))
                        .append(Component.translatable(ModConstants.ADV_END_DESC)));
            }
        }
    }
    
    /**
     * Check if all requirements for a portal are completed
     */
    private boolean isPortalCompleted(ServerPlayer player, PlayerProgressData progressData, ResourceLocation dimension) {
        PortalRequirement requirement = PortalRequirementRegistry.getInstance().getRequirement(dimension);
        if (requirement == null) {
            return false;
        }
        
        // Check all mobs killed
        for (EntityType<?> mob : requirement.getRequiredMobs()) {
            if (!progressData.hasMobBeenKilled(mob)) {
                return false;
            }
        }
        
        // Check all bosses killed
        for (EntityType<?> boss : requirement.getRequiredBosses()) {
            if (!progressData.hasMobBeenKilled(boss)) {
                return false;
            }
        }
        
        // Check all items obtained
        for (net.minecraft.world.item.Item item : requirement.getRequiredItems()) {
            if (!progressData.hasItemBeenObtained(item)) {
                return false;
            }
        }
        
        return true;
    }

    // Item pickup is tracked via advancement inventory criteria
    // No additional event handling needed here for diamond/netherite

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Sync progress data when player logs in
            PlayerProgressData progressData = serverPlayer.getData(ModAttachments.PLAYER_PROGRESS);
            PacketDistributor.sendToPlayer(serverPlayer, SyncProgressPacket.fromProgressData(progressData));
            
            // Check if player can complete any advancements they missed
            checkAndGrantAdvancements(serverPlayer, progressData);
        }
    }

    private void checkAndGrantAdvancements(ServerPlayer player, PlayerProgressData progressData) {
        // Mark achievements as unlocked in our data when player earns them via vanilla advancement system
        if (player.getAdvancements().getOrStartProgress(
                player.server.getAdvancements().get(ModConstants.NETHER_ACCESS_ADVANCEMENT)
        ).isDone()) {
            progressData.unlockAchievement(ModConstants.NETHER_ACCESS_ADVANCEMENT);
        }
        
        if (player.getAdvancements().getOrStartProgress(
                player.server.getAdvancements().get(ModConstants.END_ACCESS_ADVANCEMENT)
        ).isDone()) {
            progressData.unlockAchievement(ModConstants.END_ACCESS_ADVANCEMENT);
        }
    }
}
