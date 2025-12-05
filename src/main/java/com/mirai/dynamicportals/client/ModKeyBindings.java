package com.mirai.dynamicportals.client;

import com.mirai.dynamicportals.util.ModConstants;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {
    public static KeyMapping TOGGLE_HUD_KEY;
    public static KeyMapping SWITCH_PHASE_KEY;
    public static KeyMapping NEXT_PAGE_KEY;
    public static KeyMapping PREV_PAGE_KEY;

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        TOGGLE_HUD_KEY = new KeyMapping(
                ModConstants.KEY_TOGGLE_HUD,
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                ModConstants.KEY_CATEGORY
        );
        
        SWITCH_PHASE_KEY = new KeyMapping(
                ModConstants.KEY_SWITCH_PHASE,
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_TAB,
                ModConstants.KEY_CATEGORY
        );
        
        NEXT_PAGE_KEY = new KeyMapping(
                ModConstants.KEY_NEXT_PAGE,
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT,
                ModConstants.KEY_CATEGORY
        );
        
        PREV_PAGE_KEY = new KeyMapping(
                ModConstants.KEY_PREV_PAGE,
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT,
                ModConstants.KEY_CATEGORY
        );
        
        event.register(TOGGLE_HUD_KEY);
        event.register(SWITCH_PHASE_KEY);
        event.register(NEXT_PAGE_KEY);
        event.register(PREV_PAGE_KEY);
    }
}
