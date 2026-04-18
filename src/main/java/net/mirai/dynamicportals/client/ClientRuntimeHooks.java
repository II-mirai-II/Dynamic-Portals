package net.mirai.dynamicportals.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.mirai.dynamicportals.network.HubNetworkHandler;
import net.mirai.dynamicportals.network.PortalOverlayClientState;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class ClientRuntimeHooks {
    private static final int AUTO_REFRESH_TICKS = 40;
    private static int refreshTicker = 0;

    private static KeyMapping openHubKey;

    private ClientRuntimeHooks() {
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        openHubKey = new KeyMapping(
            "key.dynamicportals.open_hub",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.dynamicportals"
        );
        event.register(openHubKey);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            refreshTicker = 0;
            PortalOverlayClientState.clear();
            return;
        }

        PortalOverlayClientState.tick();

        if (openHubKey != null) {
            while (openHubKey.consumeClick()) {
                minecraft.setScreen(new DynamicPortalsHubScreen());
                HubNetworkHandler.requestRefreshFromClient();
            }
        }

        if (minecraft.screen instanceof DynamicPortalsHubScreen) {
            refreshTicker++;
            if (refreshTicker >= AUTO_REFRESH_TICKS) {
                refreshTicker = 0;
                HubNetworkHandler.requestRefreshFromClient();
            }
        } else {
            refreshTicker = 0;
        }
    }

    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        PortalBlockedOverlayRenderer.render(event);
    }
}
