package com.mirai.dynamicportals.client;

import com.mirai.dynamicportals.DynamicPortals;
import com.mirai.dynamicportals.config.ModConfig;
import com.mirai.dynamicportals.network.SyncProgressPacket;
import com.mirai.dynamicportals.network.SyncRequirementsPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPacketHandler {
    public static void handleSyncProgress(final SyncProgressPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                ClientProgressCache.updateFromPacket(packet);
                
                if (ModConfig.COMMON.debugLogging.get()) {
                    DynamicPortals.LOGGER.debug("Synced player progress: {} entities killed",
                        packet.killedMobs().size());
                }
            } catch (Exception e) {
                DynamicPortals.LOGGER.error("Failed to process SyncProgressPacket", e);
            }
        });
    }

    public static void handleSyncRequirements(final SyncRequirementsPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                ClientRequirementsCache.updateFromPacket(packet);
                
                if (ModConfig.COMMON.debugLogging.get()) {
                    DynamicPortals.LOGGER.debug("Synced portal requirements: {} dimensions",
                        packet.requirements().size());
                }
            } catch (Exception e) {
                DynamicPortals.LOGGER.error("Failed to process SyncRequirementsPacket", e);
            }
        });
    }
}
