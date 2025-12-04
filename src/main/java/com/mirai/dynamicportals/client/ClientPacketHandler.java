package com.mirai.dynamicportals.client;

import com.mirai.dynamicportals.network.SyncProgressPacket;
import com.mirai.dynamicportals.network.SyncRequirementsPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPacketHandler {
    public static void handleSyncProgress(final SyncProgressPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            // Store the synced data in client-side cache for HUD rendering
            ClientProgressCache.updateFromPacket(packet);
        });
    }

    public static void handleSyncRequirements(final SyncRequirementsPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            // Store the synced requirements in client-side cache for HUD rendering
            ClientRequirementsCache.updateFromPacket(packet);
        });
    }
}
