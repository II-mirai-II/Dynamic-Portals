package com.mirai.dynamicportals.event;

import com.mirai.dynamicportals.data.ModAttachments;
import com.mirai.dynamicportals.data.PlayerProgressData;
import com.mirai.dynamicportals.util.ModConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;

public class PortalEventHandler {

    @SubscribeEvent
    public void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ResourceKey<Level> targetDimension = event.getDimension();
        PlayerProgressData progressData = player.getData(ModAttachments.PLAYER_PROGRESS);

        // Check Nether portal access
        if (targetDimension.location().equals(ModConstants.NETHER_DIMENSION)) {
            if (!progressData.isAchievementUnlocked(ModConstants.NETHER_ACCESS_ADVANCEMENT)) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.translatable(ModConstants.MSG_PORTAL_BLOCKED_NETHER));
                return;
            }
        }

        // Check End portal access
        if (targetDimension.location().equals(ModConstants.END_DIMENSION)) {
            if (!progressData.isAchievementUnlocked(ModConstants.END_ACCESS_ADVANCEMENT)) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.translatable(ModConstants.MSG_PORTAL_BLOCKED_END));
                return;
            }
        }
    }
}
