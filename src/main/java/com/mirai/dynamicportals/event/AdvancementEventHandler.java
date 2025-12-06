package com.mirai.dynamicportals.event;

import com.mirai.dynamicportals.config.ModConfig;
import com.mirai.dynamicportals.manager.GlobalProgressManager;
import com.mirai.dynamicportals.network.SyncProgressPacket;
import com.mirai.dynamicportals.progress.IProgressData;
import com.mirai.dynamicportals.util.ModConstants;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class AdvancementEventHandler {

    @SubscribeEvent
    public void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        AdvancementHolder advancement = event.getAdvancement();
        IProgressData progressData = GlobalProgressManager.getProgressData(player);

        // Track when player unlocks our advancements
        if (advancement.id().equals(ModConstants.NETHER_ACCESS_ADVANCEMENT)) {
            progressData.recordAdvancementUnlocked(ModConstants.NETHER_ACCESS_ADVANCEMENT);
            
            // Broadcast to all if global mode
            if (ModConfig.isGlobalProgressEnabled()) {
                for (ServerPlayer onlinePlayer : player.server.getPlayerList().getPlayers()) {
                    PacketDistributor.sendToPlayer(onlinePlayer, SyncProgressPacket.fromProgressData(progressData));
                }
            } else {
                PacketDistributor.sendToPlayer(player, SyncProgressPacket.fromProgressData(progressData));
            }
        } else if (advancement.id().equals(ModConstants.END_ACCESS_ADVANCEMENT)) {
            progressData.recordAdvancementUnlocked(ModConstants.END_ACCESS_ADVANCEMENT);
            
            // Broadcast to all if global mode
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
