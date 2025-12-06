package com.mirai.dynamicportals.client;

import com.mirai.dynamicportals.util.ModConstants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/**
 * Handles client-side events for cleanup and state management
 */
@EventBusSubscriber(modid = ModConstants.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {

    /**
     * Clear cached data when player disconnects from server
     */
    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientProgressCache.clear();
        ClientRequirementsCache.clear();
    }
}
