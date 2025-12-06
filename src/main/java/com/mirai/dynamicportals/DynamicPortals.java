package com.mirai.dynamicportals;

import com.mirai.dynamicportals.advancement.ModTriggers;
import com.mirai.dynamicportals.api.PortalRequirementRegistry;
import com.mirai.dynamicportals.compat.ModCompatibilityRegistry;
import com.mirai.dynamicportals.config.CustomPortalRequirementsLoader;
import com.mirai.dynamicportals.config.ModConfig;
import com.mirai.dynamicportals.config.PortalRequirementsLoader;
import com.mirai.dynamicportals.data.ModAttachments;
import com.mirai.dynamicportals.data.PlayerProgressData;
import com.mirai.dynamicportals.datagen.DataGenerators;
import com.mirai.dynamicportals.event.AdvancementEventHandler;
import com.mirai.dynamicportals.event.MobKillHandler;
import com.mirai.dynamicportals.event.PlayerEventHandler;
import com.mirai.dynamicportals.event.PortalEventHandler;
import com.mirai.dynamicportals.network.ModPackets;
import com.mirai.dynamicportals.network.SyncProgressPacket;
import com.mirai.dynamicportals.network.SyncRequirementsPacket;
import com.mirai.dynamicportals.util.ModConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import com.mirai.dynamicportals.api.PortalRequirement;
import net.neoforged.bus.api.IEventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod class for Dynamic Portals.
 * 
 * This mod adds configurable requirements for accessing Nether and End portals.
 * Requirements can include:
 * - Killing specific mobs and bosses
 * - Obtaining specific items
 * - Custom advancements
 * 
 * Features:
 * - Mod compatibility system for tracking mobs from other mods
 * - Hot-reload via /dynamicportals reload command
 * - Client-side HUD for tracking progress
 * - Customizable display names, colors, and sort order
 * - JSON-based configuration system
 * 
 * @author II-mirai-II
 * @version 1.1.2
 */
@Mod(ModConstants.MOD_ID)
public class DynamicPortals {
    public static final Logger LOGGER = LoggerFactory.getLogger(ModConstants.MOD_ID);
    
    // Cache the requirements packet after server starts
    private static SyncRequirementsPacket cachedRequirementsPacket = null;
    public DynamicPortals(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Dynamic Portals mod...");

        // Register configuration
        modContainer.registerConfig(Type.COMMON, ModConfig.COMMON_SPEC);

        // Register mod components
        ModAttachments.register(modEventBus);
        ModTriggers.register(modEventBus);
        ModPackets.register(modEventBus);

        // Register lifecycle events
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(DataGenerators::gatherData);
        
        // Register client-only events
        modEventBus.addListener(com.mirai.dynamicportals.client.ModKeyBindings::registerKeyMappings);

        // Register game event handlers
        NeoForge.EVENT_BUS.register(new PortalEventHandler());
        NeoForge.EVENT_BUS.register(new MobKillHandler());
        NeoForge.EVENT_BUS.register(new PlayerEventHandler());
        NeoForge.EVENT_BUS.register(new AdvancementEventHandler());
        
        // Register server event listeners
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);

        LOGGER.info("Dynamic Portals mod initialized successfully!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Common setup phase...");
        // Don't register requirements here - wait for server to fully start
    }

    private void onServerStarting(final ServerStartingEvent event) {
        LOGGER.info("Server starting - loading mod compatibility...");
        
        // Register commands
        com.mirai.dynamicportals.command.ModCommands.register(event.getServer().getCommands().getDispatcher());
        
        // Load mod compatibility configurations (entities are now registered)
        ModCompatibilityRegistry.loadCompatibilityConfigs();
    }
    
    private void onServerStarted(final ServerStartedEvent event) {
        LOGGER.info("Server fully started - registering portal requirements with loaded tags...");
        
        // Now that server is fully started and tags are loaded, register requirements
        PortalRequirementRegistry.getInstance().clearRequirements();
        
        // Export vanilla requirements to external config folder for user customization
        CustomPortalRequirementsLoader.exportVanillaRequirements();
        
        // 1. Load vanilla customizations (if they exist) - highest priority
        CustomPortalRequirementsLoader.loadVanillaCustomizations();
        
        // 2. Load portal requirements from JSON configuration (internal defaults)
        PortalRequirementsLoader.loadAndRegister();
        
        // 3. Load custom portal requirements from config folder (user overrides)
        CustomPortalRequirementsLoader.loadCustomRequirements();
        
        // Prepare requirements packet for clients
        cachedRequirementsPacket = createRequirementsPacket();
        
        LOGGER.info("Portal requirements registered successfully!");
    }

    private void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            LOGGER.info("Player {} logged in - syncing data...", serverPlayer.getName().getString());
            
            com.mirai.dynamicportals.progress.IProgressData data = 
                com.mirai.dynamicportals.manager.GlobalProgressManager.getProgressData(serverPlayer);
            
            // Send progress synchronization packet to client
            PacketDistributor.sendToPlayer(
                serverPlayer,
                SyncProgressPacket.fromProgressData(data)
            );
            LOGGER.debug("Sent progress packet to {}", serverPlayer.getName().getString());
            
            // Send requirements synchronization packet to client
            if (cachedRequirementsPacket != null) {
                PacketDistributor.sendToPlayer(
                    serverPlayer,
                    cachedRequirementsPacket
                );
                LOGGER.info("Sent requirements packet to {} (cache valid)", serverPlayer.getName().getString());
            } else {
                LOGGER.warn("Requirements packet not cached - player {} won't see requirements!", serverPlayer.getName().getString());
            }
        }
    }
    
    /**
     * Create a packet containing all portal requirements for client synchronization.
     * Converts PortalRequirement objects to RequirementData records with ResourceLocations
     * instead of game objects for network transmission.
     * 
     * @return SyncRequirementsPacket ready to send to clients
     */
    private SyncRequirementsPacket createRequirementsPacket() {
        Map<ResourceLocation, SyncRequirementsPacket.RequirementData> packetData = new HashMap<>();
        Map<ResourceLocation, PortalRequirement> allRequirements = PortalRequirementRegistry.getInstance().getAllRequirements();
        
        for (Map.Entry<ResourceLocation, PortalRequirement> entry : allRequirements.entrySet()) {
            ResourceLocation dimension = entry.getKey();
            PortalRequirement requirement = entry.getValue();
            
            // Convert EntityTypes to ResourceLocations
            List<ResourceLocation> mobIds = new ArrayList<>();
            for (EntityType<?> mob : requirement.getRequiredMobs()) {
                ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(mob);
                if (id != null) {
                    mobIds.add(id);
                }
            }
            
            List<ResourceLocation> bossIds = new ArrayList<>();
            for (EntityType<?> boss : requirement.getRequiredBosses()) {
                ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(boss);
                if (id != null) {
                    bossIds.add(id);
                }
            }
            
            // Convert Items to ResourceLocations
            List<ResourceLocation> itemIds = new ArrayList<>();
            for (Item item : requirement.getRequiredItems()) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
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
     * Invalidate the cached requirements packet.
     * Called by the reload command to force recreation of the packet with updated requirements.
     * After invalidation, the next player login will trigger packet recreation.
     * 
     * @see ModCommands#reloadCommand for usage in reload command
     */
    public static void invalidateRequirementsCache() {
        cachedRequirementsPacket = null;
        LOGGER.debug("Requirements cache invalidated");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Client setup phase...");
        // KeyBindings are now registered via @EventBusSubscriber in ModKeyBindings
    }
}
