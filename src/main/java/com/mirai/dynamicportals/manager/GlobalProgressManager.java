package com.mirai.dynamicportals.manager;

import com.mirai.dynamicportals.config.ModConfig;
import com.mirai.dynamicportals.data.GlobalProgressData;
import com.mirai.dynamicportals.data.ModAttachments;
import com.mirai.dynamicportals.data.PlayerProgressData;
import com.mirai.dynamicportals.progress.IProgressData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Manager class that determines whether to use individual or global progress data
 * based on the current configuration.
 */
public class GlobalProgressManager {
    
    /**
     * Get the appropriate progress data for the given player.
     * Returns GlobalProgressData if global progress is enabled, otherwise PlayerProgressData.
     */
    public static IProgressData getProgressData(Player player) {
        if (ModConfig.isGlobalProgressEnabled() && player.level() instanceof ServerLevel serverLevel) {
            return GlobalProgressData.get(serverLevel);
        } else {
            return player.getData(ModAttachments.PLAYER_PROGRESS);
        }
    }
    
    /**
     * Get the appropriate progress data for the given server player.
     * Returns GlobalProgressData if global progress is enabled, otherwise PlayerProgressData.
     */
    public static IProgressData getProgressData(ServerPlayer player) {
        if (ModConfig.isGlobalProgressEnabled()) {
            return GlobalProgressData.get(player.serverLevel());
        } else {
            return player.getData(ModAttachments.PLAYER_PROGRESS);
        }
    }
    
    /**
     * Get the appropriate progress data for the given server level and player.
     * Returns GlobalProgressData if global progress is enabled, otherwise PlayerProgressData.
     */
    public static IProgressData getProgressData(ServerLevel level, Player player) {
        if (ModConfig.isGlobalProgressEnabled()) {
            return GlobalProgressData.get(level);
        } else {
            return player.getData(ModAttachments.PLAYER_PROGRESS);
        }
    }
    
    /**
     * Get player-specific progress data (always returns PlayerProgressData).
     * Use this when you specifically need individual player data regardless of global mode.
     */
    public static PlayerProgressData getPlayerProgressData(Player player) {
        return player.getData(ModAttachments.PLAYER_PROGRESS);
    }
    
    /**
     * Get global progress data for a level (returns null if not on server side).
     * Use this when you specifically need global data regardless of mode.
     */
    public static GlobalProgressData getGlobalProgressData(ServerLevel level) {
        return GlobalProgressData.get(level);
    }
    
    /**
     * Check if global progress mode is currently enabled.
     */
    public static boolean isGlobalMode() {
        return ModConfig.isGlobalProgressEnabled();
    }
}
