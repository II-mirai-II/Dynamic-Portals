package com.mirai.dynamicportals.event;

import com.mirai.dynamicportals.data.ModAttachments;
import com.mirai.dynamicportals.data.PlayerProgressData;
import com.mirai.dynamicportals.network.SyncProgressPacket;
import com.mirai.dynamicportals.util.ModConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class PlayerEventHandler {

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        PlayerProgressData progressData = player.getData(ModAttachments.PLAYER_PROGRESS);

        // Increment death counter
        progressData.incrementDeathCount();

        // Check if progress should be reset
        if (progressData.shouldResetProgress()) {
            progressData.resetProgress();
            player.sendSystemMessage(Component.translatable(ModConstants.MSG_PROGRESS_RESET));
        }

        // Sync to client
        PacketDistributor.sendToPlayer(player, SyncProgressPacket.fromProgressData(progressData));
    }

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
