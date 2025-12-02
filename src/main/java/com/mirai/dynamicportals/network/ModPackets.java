package com.mirai.dynamicportals.network;

import com.mirai.dynamicportals.client.ClientPacketHandler;
import com.mirai.dynamicportals.util.ModConstants;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModPackets {
    public static void register(IEventBus eventBus) {
        eventBus.addListener(ModPackets::registerPayloads);
    }

    private static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(ModConstants.MOD_ID)
                .versioned("1.0");

        registrar.playToClient(
                SyncProgressPacket.TYPE,
                SyncProgressPacket.STREAM_CODEC,
                ClientPacketHandler::handleSyncProgress
        );
    }
}
