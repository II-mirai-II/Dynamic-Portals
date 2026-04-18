package net.mirai.dynamicportals;

import net.mirai.dynamicportals.client.ClientRuntimeHooks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = DynamicPortals.MOD_ID, dist = Dist.CLIENT)
public class DynamicPortalsClient {
    public DynamicPortalsClient(IEventBus modEventBus) {
        modEventBus.addListener(ClientRuntimeHooks::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(ClientRuntimeHooks::onClientTick);
        NeoForge.EVENT_BUS.addListener(ClientRuntimeHooks::onRenderGuiPost);
    }
}
