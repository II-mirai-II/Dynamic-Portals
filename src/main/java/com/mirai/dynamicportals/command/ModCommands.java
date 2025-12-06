package com.mirai.dynamicportals.command;

import com.mirai.dynamicportals.DynamicPortals;
import com.mirai.dynamicportals.api.PortalRequirementRegistry;
import com.mirai.dynamicportals.compat.ModCompatibilityRegistry;
import com.mirai.dynamicportals.config.CustomPortalRequirementsLoader;
import com.mirai.dynamicportals.config.PortalRequirementsLoader;
import com.mirai.dynamicportals.data.ModAttachments;
import com.mirai.dynamicportals.manager.GlobalProgressManager;
import com.mirai.dynamicportals.progress.IProgressData;
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
                .then(Commands.literal("mode")
                    .executes(ModCommands::modeCommand)
                )
                .then(Commands.literal("migrate")
                    .then(Commands.literal("toGlobal")
                        .executes(ModCommands::migrateToGlobalCommand)
                    )
                    .then(Commands.literal("toIndividual")
                        .executes(ModCommands::migrateToIndividualCommand)
                    )
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
                IProgressData progressData = GlobalProgressManager.getProgressData(player);
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
    
    /**
     * Display current progress mode (individual vs global)
     */
    private static int modeCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        boolean isGlobalMode = com.mirai.dynamicportals.config.ModConfig.isGlobalProgressEnabled();
        
        if (isGlobalMode) {
            source.sendSuccess(() -> Component.literal("Current Mode: ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("GLOBAL")
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .append(Component.literal("\nAll players share the same progress.")
                    .withStyle(ChatFormatting.GRAY)), false);
        } else {
            source.sendSuccess(() -> Component.literal("Current Mode: ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("INDIVIDUAL")
                    .withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
                .append(Component.literal("\nEach player has their own progress.")
                    .withStyle(ChatFormatting.GRAY)), false);
        }
        
        source.sendSuccess(() -> Component.literal("To change mode, edit ")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal("config/dynamicportals-common.toml")
                .withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" and use /dynamicportals migrate")
                .withStyle(ChatFormatting.GRAY)), false);
        
        return 1;
    }
    
    /**
     * Migrate all player progress to global progress
     */
    private static int migrateToGlobalCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (com.mirai.dynamicportals.config.ModConfig.isGlobalProgressEnabled()) {
            source.sendFailure(Component.literal("Already in global mode. No migration needed.")
                .withStyle(ChatFormatting.RED));
            return 0;
        }
        
        try {
            com.mirai.dynamicportals.data.GlobalProgressData globalData = 
                com.mirai.dynamicportals.data.GlobalProgressData.get(source.getLevel());
            
            int playerCount = 0;
            for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
                com.mirai.dynamicportals.data.PlayerProgressData playerData = 
                    GlobalProgressManager.getPlayerProgressData(player);
                
                // Merge player data into global
                for (var entry : playerData.getKilledMobs().entrySet()) {
                    if (entry.getValue()) {
                        globalData.recordMobKill(entry.getKey());
                    }
                }
                
                for (var item : playerData.getObtainedItems()) {
                    globalData.recordItemObtained(item);
                }
                
                for (var achievement : playerData.getUnlockedAchievements()) {
                    globalData.recordAdvancementUnlocked(achievement);
                }
                
                playerCount++;
            }
            
            final int finalCount = playerCount;
            source.sendSuccess(() -> Component.literal("Successfully migrated progress from ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(finalCount + " player(s)")
                    .withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" to global mode.")
                    .withStyle(ChatFormatting.GREEN)), true);
            
            source.sendSuccess(() -> Component.literal("⚠ Now enable global mode in config and reload the server!")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), false);
            
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to migrate: " + e.getMessage())
                .withStyle(ChatFormatting.RED));
            DynamicPortals.LOGGER.error("Migration to global failed", e);
            return 0;
        }
    }
    
    /**
     * Migrate global progress to all individual players
     */
    private static int migrateToIndividualCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!com.mirai.dynamicportals.config.ModConfig.isGlobalProgressEnabled()) {
            source.sendFailure(Component.literal("Already in individual mode. No migration needed.")
                .withStyle(ChatFormatting.RED));
            return 0;
        }
        
        try {
            com.mirai.dynamicportals.data.GlobalProgressData globalData = 
                com.mirai.dynamicportals.data.GlobalProgressData.get(source.getLevel());
            
            int playerCount = 0;
            for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
                com.mirai.dynamicportals.data.PlayerProgressData playerData = 
                    GlobalProgressManager.getPlayerProgressData(player);
                
                // Copy global data to each player
                for (var entry : globalData.getKilledMobs().entrySet()) {
                    if (entry.getValue()) {
                        playerData.recordMobKill(entry.getKey());
                    }
                }
                
                for (var item : globalData.getObtainedItems()) {
                    playerData.recordItemObtained(item);
                }
                
                for (var achievement : globalData.getUnlockedAchievements()) {
                    playerData.recordAdvancementUnlocked(achievement);
                }
                
                // Sync to player
                PacketDistributor.sendToPlayer(player, SyncProgressPacket.fromProgressData(playerData));
                playerCount++;
            }
            
            final int finalCount = playerCount;
            source.sendSuccess(() -> Component.literal("Successfully migrated global progress to ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(finalCount + " player(s)")
                    .withStyle(ChatFormatting.GOLD))
                .append(Component.literal(".")
                    .withStyle(ChatFormatting.GREEN)), true);
            
            source.sendSuccess(() -> Component.literal("⚠ Now disable global mode in config and reload the server!")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), false);
            
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to migrate: " + e.getMessage())
                .withStyle(ChatFormatting.RED));
            DynamicPortals.LOGGER.error("Migration to individual failed", e);
            return 0;
        }
    }
}
