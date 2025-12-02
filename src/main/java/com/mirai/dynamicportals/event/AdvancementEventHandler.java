package com.mirai.dynamicportals.event;

import com.mirai.dynamicportals.data.ModAttachments;
import com.mirai.dynamicportals.data.PlayerProgressData;
import com.mirai.dynamicportals.network.SyncProgressPacket;
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
        PlayerProgressData progressData = player.getData(ModAttachments.PLAYER_PROGRESS);

        // Track when player unlocks our advancements
        if (advancement.id().equals(ModConstants.NETHER_ACCESS_ADVANCEMENT)) {
            progressData.unlockAchievement(ModConstants.NETHER_ACCESS_ADVANCEMENT);
            PacketDistributor.sendToPlayer(player, SyncProgressPacket.fromProgressData(progressData));
        } else if (advancement.id().equals(ModConstants.END_ACCESS_ADVANCEMENT)) {
            progressData.unlockAchievement(ModConstants.END_ACCESS_ADVANCEMENT);
            PacketDistributor.sendToPlayer(player, SyncProgressPacket.fromProgressData(progressData));
        }
    }
}
