package com.mirai.dynamicportals.command;

import com.mirai.dynamicportals.DynamicPortals;
import com.mirai.dynamicportals.api.PortalRequirementRegistry;
import com.mirai.dynamicportals.compat.ModCompatibilityRegistry;
import com.mirai.dynamicportals.config.CustomPortalRequirementsLoader;
import com.mirai.dynamicportals.config.PortalRequirementsLoader;
import com.mirai.dynamicportals.data.ModAttachments;
import com.mirai.dynamicportals.data.PlayerProgressData;
import com.mirai.dynamicportals.network.SyncProgressPacket;
import com.mirai.dynamicportals.network.SyncRequirementsPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Commands for Dynamic Portals mod.
 * /dynamicportals reload - Reload portal requirements from config
 */
public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("dynamicportals")
                .requires(source -> source.hasPermission(2)) // Requires OP level 2
                .then(Commands.literal("reload")
                    .executes(ModCommands::reloadCommand)
                )
        );
    }

    /**
     * Reload all portal requirements and sync to all players
     */
    private static int reloadCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        try {
            source.sendSuccess(() -> Component.literal("Reloading Dynamic Portals requirements...")
                .withStyle(ChatFormatting.YELLOW), true);
            
            // Step 1: Clear existing requirements
            PortalRequirementRegistry.getInstance().clearRequirements();
            ModCompatibilityRegistry.clear();
            
            // Step 2: Reload mod compatibility configs
            ModCompatibilityRegistry.loadCompatibilityConfigs();
            
            // Step 3: Reload portal requirements
            PortalRequirementsLoader.loadAndRegister();
            CustomPortalRequirementsLoader.loadCustomRequirements();
            
            // Step 4: Invalidate cached requirements packet
            DynamicPortals.invalidateRequirementsCache();
            
            // Step 5: Create new requirements packet
            SyncRequirementsPacket newRequirementsPacket = createRequirementsPacket();
            
            // Step 6: Sync to all online players
            int playerCount = 0;
            for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
                // Send updated requirements
                PacketDistributor.sendToPlayer(player, newRequirementsPacket);
                
                // Re-send player progress (in case requirements changed)
                PlayerProgressData progressData = player.getData(ModAttachments.PLAYER_PROGRESS);
                PacketDistributor.sendToPlayer(player, SyncProgressPacket.fromProgressData(progressData));
                
                playerCount++;
            }
            
            final int finalCount = playerCount;
            source.sendSuccess(() -> Component.literal("Successfully reloaded portal requirements!")
                .withStyle(ChatFormatting.GREEN), true);
            source.sendSuccess(() -> Component.literal("Synced to " + finalCount + " online player(s)")
                .withStyle(ChatFormatting.GRAY), true);
            
            DynamicPortals.LOGGER.info("Portal requirements reloaded by {}", source.getTextName());
            
            return 1; // Success
            
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to reload requirements: " + e.getMessage())
                .withStyle(ChatFormatting.RED));
            DynamicPortals.LOGGER.error("Failed to reload portal requirements", e);
            return 0; // Failure
        }
    }
    
    /**
     * Create requirements packet from current registry state
     * (Duplicated from DynamicPortals.java to avoid circular dependency)
     */
    private static SyncRequirementsPacket createRequirementsPacket() {
        var packetData = new java.util.HashMap<net.minecraft.resources.ResourceLocation, SyncRequirementsPacket.RequirementData>();
        var allRequirements = PortalRequirementRegistry.getInstance().getAllRequirements();
        
        for (var entry : allRequirements.entrySet()) {
            var dimension = entry.getKey();
            var requirement = entry.getValue();
            
            // Convert EntityTypes to ResourceLocations
            var mobIds = new java.util.ArrayList<net.minecraft.resources.ResourceLocation>();
            for (var mob : requirement.getRequiredMobs()) {
                var id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(mob);
                if (id != null) {
                    mobIds.add(id);
                }
            }
            
            var bossIds = new java.util.ArrayList<net.minecraft.resources.ResourceLocation>();
            for (var boss : requirement.getRequiredBosses()) {
                var id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(boss);
                if (id != null) {
                    bossIds.add(id);
                }
            }
            
            var itemIds = new java.util.ArrayList<net.minecraft.resources.ResourceLocation>();
            for (var item : requirement.getRequiredItems()) {
                var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
                if (id != null) {
                    itemIds.add(id);
                }
            }
            
            packetData.put(dimension, new SyncRequirementsPacket.RequirementData(
                requirement.getRequiredAdvancement(),
                mobIds,
                bossIds,
                itemIds,
                requirement.getDisplayName(),
                requirement.getDisplayColor(),
                requirement.getSortOrder()
            ));
        }
        
        return new SyncRequirementsPacket(packetData);
    }
}
