package com.mirai.dynamicportals.event;

import com.mirai.dynamicportals.config.ModConfig;
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
        // Check if portal blocking is enabled
        if (!ModConfig.COMMON.enablePortalBlocking.get()) {
            return;
        }
        
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ResourceKey<Level> targetDimension = event.getDimension();
        PlayerProgressData progressData = player.getData(ModAttachments.PLAYER_PROGRESS);

        // Check if this dimension has portal requirements (supports custom dimensions)
        com.mirai.dynamicportals.api.PortalRequirement requirement = 
            com.mirai.dynamicportals.api.PortalRequirementRegistry.getInstance()
                .getRequirement(targetDimension.location());
        
        if (requirement == null) {
            return; // No requirements for this dimension
        }
        
        // Check if player has unlocked the required advancement
        if (requirement.getRequiredAdvancement() != null) {
            if (!progressData.isAchievementUnlocked(requirement.getRequiredAdvancement())) {
                event.setCanceled(true);
                
                // Debug logging
                if (ModConfig.COMMON.debugLogging.get()) {
                    com.mirai.dynamicportals.DynamicPortals.LOGGER.info(
                        "Blocked portal travel: {} attempted to enter {} without required advancement {}",
                        player.getName().getString(),
                        targetDimension.location(),
                        requirement.getRequiredAdvancement()
                    );
                }
                
                // Send appropriate message based on dimension
                if (targetDimension.location().equals(ModConstants.NETHER_DIMENSION)) {
                    player.sendSystemMessage(Component.translatable(ModConstants.MSG_PORTAL_BLOCKED_NETHER));
                } else if (targetDimension.location().equals(ModConstants.END_DIMENSION)) {
                    player.sendSystemMessage(Component.translatable(ModConstants.MSG_PORTAL_BLOCKED_END));
                } else {
                    player.sendSystemMessage(Component.translatable("msg.dynamicportals.portal_blocked_generic", 
                        targetDimension.location().toString()));
                }
                return;
            }
        }
    }
}
