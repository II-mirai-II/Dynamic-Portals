package com.mirai.dynamicportals.client;

import com.mirai.dynamicportals.util.ModConstants;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {
    public static KeyMapping TOGGLE_HUD_KEY;

    public static void register() {
        TOGGLE_HUD_KEY = new KeyMapping(
                ModConstants.KEY_TOGGLE_HUD,
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                ModConstants.KEY_CATEGORY
        );

        // Registration happens automatically via KeyMapping constructor in NeoForge
    }
}
