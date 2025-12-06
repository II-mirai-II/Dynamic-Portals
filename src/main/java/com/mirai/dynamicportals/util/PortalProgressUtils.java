package com.mirai.dynamicportals.util;

import com.mirai.dynamicportals.api.PortalRequirement;
import com.mirai.dynamicportals.api.PortalRequirementRegistry;
import com.mirai.dynamicportals.progress.IProgressData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

/**
 * Utility class for validating portal requirements and unlocking portal access.
 * Centralizes the completion-checking logic used by multiple event handlers.
 * 
 * <p><b>Design Rationale:</b>
 * Previously, this logic was duplicated in both {@link MobKillHandler} and {@link PlayerEventHandler},
 * leading to maintenance issues and inconsistencies. This utility class provides a single source of truth.
 * 
 * <p><b>Thread Safety:</b>
 * These methods are designed to be called from server-side event handlers on the server thread.
 * PlayerProgressData modifications should only occur on the server thread.
 * 
 * <p><b>Usage:</b>
 * Call {@link #checkAndUnlockPortals} after any progress update (mob kill, item pickup)
 * to automatically unlock portals when requirements are met.
 * 
 * @see MobKillHandler#onMobKilled for usage in mob kill events
 * @see PlayerEventHandler#onItemPickup for usage in item pickup events
 */
public class PortalProgressUtils {
    
    /**
     * Check if player has completed all requirements for any portal and unlock it.
     * 
     * <p>This method iterates through all registered portal requirements (Nether and End)
     * and checks if the player has met all conditions. If so, it unlocks the achievement
     * and sends a system message to the player.
     * 
     * <p><b>Side Effects:</b>
     * <ul>
     *   <li>Modifies {@code progressData} by marking achievements as unlocked</li>
     *   <li>Sends system messages to the player when achievements are unlocked</li>
     * </ul>
     * 
     * <p><b>Example:</b>
     * <pre>{@code
     * @SubscribeEvent
     * public void onMobKilled(LivingDeathEvent event) {
     *     // ... update progress data ...
     *     PortalProgressUtils.checkAndUnlockPortals(player, progressData);
     *     // Achievement will be unlocked if all requirements are now met
     * }
     * }</pre>
     * 
     * @param player The player to check and potentially unlock achievements for
     * @param progressData The player's progress data (will be modified if achievements are unlocked)
     */
    public static void checkAndUnlockPortals(ServerPlayer player, IProgressData progressData) {
        // Check Nether Portal
        if (!progressData.hasAdvancementBeenUnlocked(ModConstants.NETHER_ACCESS_ADVANCEMENT)) {
            if (isPortalCompleted(player, progressData, ModConstants.NETHER_DIMENSION)) {
                progressData.recordAdvancementUnlocked(ModConstants.NETHER_ACCESS_ADVANCEMENT);
                player.sendSystemMessage(Component.translatable(ModConstants.ADV_NETHER_TITLE)
                        .append(Component.literal(" - "))
                        .append(Component.translatable(ModConstants.ADV_NETHER_DESC)));
            }
        }
        
        // Check End Portal
        if (!progressData.hasAdvancementBeenUnlocked(ModConstants.END_ACCESS_ADVANCEMENT)) {
            if (isPortalCompleted(player, progressData, ModConstants.END_DIMENSION)) {
                progressData.recordAdvancementUnlocked(ModConstants.END_ACCESS_ADVANCEMENT);
                player.sendSystemMessage(Component.translatable(ModConstants.ADV_END_TITLE)
                        .append(Component.literal(" - "))
                        .append(Component.translatable(ModConstants.ADV_END_DESC)));
            }
        }
    }
    
    /**
     * Check if all requirements for a specific portal are completed.
     * 
     * <p><b>Completion Algorithm:</b>
     * A portal is considered completed if and only if:
     * <ol>
     *   <li>The portal requirement is registered in {@link PortalRequirementRegistry}</li>
     *   <li>All required mobs have been killed by the player</li>
     *   <li>All required bosses have been killed by the player</li>
     *   <li>All required items have been obtained by the player</li>
     * </ol>
     * 
     * <p>If any single requirement is not met, the method returns false.
     * Empty requirement lists (no mobs/bosses/items) are considered satisfied.
     * 
     * @param player The player to check (currently unused but reserved for future extensibility,
     *               such as permission checks or custom requirement validators)
     * @param progressData The player's progress data containing kill/obtain records
     * @param dimension The dimension identifier to check requirements for (e.g., ModConstants.NETHER_DIMENSION)
     * @return true if all requirements are met and the portal should be unlocked, false otherwise
     */
    public static boolean isPortalCompleted(ServerPlayer player, IProgressData progressData, ResourceLocation dimension) {
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
        for (Item item : requirement.getRequiredItems()) {
            if (!progressData.hasItemBeenObtained(item)) {
                return false;
            }
        }
        
        return true;
    }
}
