package com.mirai.dynamicportals.event;

import com.mirai.dynamicportals.DynamicPortals;
import com.mirai.dynamicportals.api.PortalRequirementRegistry;
import com.mirai.dynamicportals.config.ModConfig;
import com.mirai.dynamicportals.data.ModAttachments;
import com.mirai.dynamicportals.data.PlayerProgressData;
import com.mirai.dynamicportals.manager.GlobalProgressManager;
import com.mirai.dynamicportals.network.SyncProgressPacket;
import com.mirai.dynamicportals.progress.IProgressData;
import com.mirai.dynamicportals.util.ModConstants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Handles player-related events including item pickup and player cloning on death.
 */
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

        IProgressData progressData = GlobalProgressManager.getProgressData(player);
        net.minecraft.world.item.Item pickedItem = event.getItemEntity().getItem().getItem();

        // Get all tracked items from registry
        java.util.Set<net.minecraft.world.item.Item> trackedItems = PortalRequirementRegistry.getInstance().getAllTrackedItems();

        // Track items dynamically
        if (trackedItems.contains(pickedItem)) {
            if (!progressData.hasItemBeenObtained(pickedItem)) {
                progressData.recordItemObtained(pickedItem);
                
                if (ModConfig.COMMON.debugLogging.get()) {
                    DynamicPortals.LOGGER.debug("Player {} obtained tracked item: {}", 
                            player.getName().getString(), 
                            net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(pickedItem));
                }
                
                // Check if player completed any portal requirements
                if (ModConfig.COMMON.autoGrantAdvancements.get()) {
                    com.mirai.dynamicportals.util.PortalProgressUtils.checkAndUnlockPortals(player, progressData);
                }
                
                // Sync to client (broadcast to all if global mode)
                if (ModConfig.isGlobalProgressEnabled()) {
                    for (ServerPlayer onlinePlayer : player.server.getPlayerList().getPlayers()) {
                        PacketDistributor.sendToPlayer(onlinePlayer, SyncProgressPacket.fromProgressData(progressData));
                    }
                } else {
                    PacketDistributor.sendToPlayer(player, SyncProgressPacket.fromProgressData(progressData));
                }
            }
        }
    }

    // Item pickup is tracked via advancement inventory criteria
    // No additional event handling needed here for diamond/netherite

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Sync progress data when player logs in
            IProgressData progressData = GlobalProgressManager.getProgressData(serverPlayer);
            PacketDistributor.sendToPlayer(serverPlayer, SyncProgressPacket.fromProgressData(progressData));
            
            // Check if player can complete any advancements they missed (only for individual mode)
            if (!ModConfig.isGlobalProgressEnabled()) {
                PlayerProgressData playerData = GlobalProgressManager.getPlayerProgressData(serverPlayer);
                checkAndGrantAdvancements(serverPlayer, playerData);
            }
        }
    }

    private void checkAndGrantAdvancements(ServerPlayer player, PlayerProgressData progressData) {
        // Mark achievements as unlocked in our data when player earns them via vanilla advancement system
        if (player.getAdvancements().getOrStartProgress(
                player.server.getAdvancements().get(ModConstants.NETHER_ACCESS_ADVANCEMENT)
        ).isDone()) {
            progressData.recordAdvancementUnlocked(ModConstants.NETHER_ACCESS_ADVANCEMENT);
        }
        
        if (player.getAdvancements().getOrStartProgress(
                player.server.getAdvancements().get(ModConstants.END_ACCESS_ADVANCEMENT)
        ).isDone()) {
            progressData.recordAdvancementUnlocked(ModConstants.END_ACCESS_ADVANCEMENT);
        }
    }
}
