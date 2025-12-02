package com.mirai.dynamicportals;

import com.mirai.dynamicportals.advancement.ModTriggers;
import com.mirai.dynamicportals.api.IPortalRequirementAPI;
import com.mirai.dynamicportals.api.PortalRequirementRegistry;
import com.mirai.dynamicportals.client.ModKeyBindings;
import com.mirai.dynamicportals.data.ModAttachments;
import com.mirai.dynamicportals.datagen.DataGenerators;
import com.mirai.dynamicportals.event.AdvancementEventHandler;
import com.mirai.dynamicportals.event.MobKillHandler;
import com.mirai.dynamicportals.event.PlayerEventHandler;
import com.mirai.dynamicportals.event.PortalEventHandler;
import com.mirai.dynamicportals.network.ModPackets;
import com.mirai.dynamicportals.util.ModConstants;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ModConstants.MOD_ID)
public class DynamicPortals {
    public static final Logger LOGGER = LoggerFactory.getLogger(ModConstants.MOD_ID);
    private static IPortalRequirementAPI apiInstance;

    public DynamicPortals(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Dynamic Portals mod...");

        // Register mod components
        ModAttachments.register(modEventBus);
        ModTriggers.register(modEventBus);
        ModPackets.register(modEventBus);

        // Register lifecycle events
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(DataGenerators::gatherData);

        // Register game event handlers
        NeoForge.EVENT_BUS.register(new PortalEventHandler());
        NeoForge.EVENT_BUS.register(new MobKillHandler());
        NeoForge.EVENT_BUS.register(new PlayerEventHandler());
        NeoForge.EVENT_BUS.register(new AdvancementEventHandler());

        // Initialize API
        apiInstance = PortalRequirementRegistry.getInstance();

        LOGGER.info("Dynamic Portals mod initialized successfully!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Common setup phase...");
        event.enqueueWork(() -> {
            // Initialize portal requirements
            PortalRequirementRegistry.getInstance().registerVanillaRequirements();
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Client setup phase...");
        event.enqueueWork(() -> {
            ModKeyBindings.register();
        });
    }

    public static IPortalRequirementAPI getAPI() {
        return apiInstance;
    }
}
