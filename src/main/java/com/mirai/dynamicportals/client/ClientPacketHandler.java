package com.mirai.dynamicportals.client;

import com.mirai.dynamicportals.network.SyncProgressPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPacketHandler {
    public static void handleSyncProgress(final SyncProgressPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            // Store the synced data in client-side cache for HUD rendering
            ClientProgressCache.updateFromPacket(packet);
        });
    }
}
