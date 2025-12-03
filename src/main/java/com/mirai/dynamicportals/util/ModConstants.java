package com.mirai.dynamicportals.util;

import net.minecraft.resources.ResourceLocation;

public class ModConstants {
    public static final String MOD_ID = "dynamicportals";

    // Death penalty configuration
    public static final int DEATH_THRESHOLD = 10;
    public static final int ASSIST_TIME_WINDOW_SECONDS = 5;

    // Dimension identifiers
    public static final ResourceLocation NETHER_DIMENSION = ResourceLocation.withDefaultNamespace("the_nether");
    public static final ResourceLocation END_DIMENSION = ResourceLocation.withDefaultNamespace("the_end");

    // Achievement identifiers
    public static final ResourceLocation NETHER_ACCESS_ADVANCEMENT = ResourceLocation.fromNamespaceAndPath(MOD_ID, "nether_access");
    public static final ResourceLocation END_ACCESS_ADVANCEMENT = ResourceLocation.fromNamespaceAndPath(MOD_ID, "end_access");

    // Translation keys - Messages
    public static final String MSG_PORTAL_BLOCKED_NETHER = "message.dynamicportals.portal_blocked.nether";
    public static final String MSG_PORTAL_BLOCKED_END = "message.dynamicportals.portal_blocked.end";
    public static final String MSG_PROGRESS_RESET = "message.dynamicportals.progress_reset";
    public static final String MSG_ACHIEVEMENT_UNLOCKED = "message.dynamicportals.achievement_unlocked";

    // Translation keys - HUD
    public static final String HUD_TITLE = "hud.dynamicportals.title";
    public static final String HUD_DEATHS = "hud.dynamicportals.deaths";
    public static final String HUD_PROGRESS_OVERWORLD = "hud.dynamicportals.progress.overworld";
    public static final String HUD_PROGRESS_NETHER = "hud.dynamicportals.progress.nether";
    public static final String HUD_PHASE_OVERWORLD = "hud.dynamicportals.phase.overworld";
    public static final String HUD_PHASE_NETHER = "hud.dynamicportals.phase.nether";
    public static final String HUD_REQUIRED_MOBS = "hud.dynamicportals.required_mobs";
    public static final String HUD_REQUIRED_ITEMS = "hud.dynamicportals.required_items";
    public static final String HUD_REQUIRED_BOSSES = "hud.dynamicportals.required_bosses";
    public static final String HUD_COMPLETED = "hud.dynamicportals.completed";
    public static final String HUD_INCOMPLETE = "hud.dynamicportals.incomplete";

    // Translation keys - Advancements
    public static final String ADV_NETHER_TITLE = "advancement.dynamicportals.nether_access.title";
    public static final String ADV_NETHER_DESC = "advancement.dynamicportals.nether_access.description";
    public static final String ADV_END_TITLE = "advancement.dynamicportals.end_access.title";
    public static final String ADV_END_DESC = "advancement.dynamicportals.end_access.description";

    // Translation keys - Keybindings
    public static final String KEY_TOGGLE_HUD = "key.dynamicportals.toggle_hud";
    public static final String KEY_CATEGORY = "key.categories.dynamicportals";

    // NBT keys
    public static final String NBT_KILLED_MOBS = "KilledMobs";
    public static final String NBT_OBTAINED_ITEMS = "ObtainedItems";
    public static final String NBT_DEATH_COUNT = "DeathCount";
    public static final String NBT_UNLOCKED_ACHIEVEMENTS = "UnlockedAchievements";
    public static final String NBT_DATA_VERSION = "DataVersion";

    // Data version for migrations
    public static final int CURRENT_DATA_VERSION = 1;

    private ModConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
