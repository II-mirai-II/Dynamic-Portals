package com.mirai.dynamicportals.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Mod configuration using NeoForge's Config API.
 * Provides runtime-configurable settings for gameplay and UI.
 */
public class ModConfig {
    
    public static final Common COMMON;
    public static final ModConfigSpec COMMON_SPEC;
    
    static {
        Pair<Common, ModConfigSpec> commonPair = new ModConfigSpec.Builder().configure(Common::new);
        COMMON = commonPair.getLeft();
        COMMON_SPEC = commonPair.getRight();
    }
    
    public static class Common {
        
        // Gameplay
        public final ModConfigSpec.BooleanValue enablePortalBlocking;
        public final ModConfigSpec.BooleanValue autoGrantAdvancements;
        public final ModConfigSpec.IntValue assistTimeWindowSeconds;
        public final ModConfigSpec.BooleanValue enableGlobalProgress;
        
        // UI
        public final ModConfigSpec.IntValue maxLinesPerPage;
        public final ModConfigSpec.ConfigValue<String> hudBackgroundColor;
        public final ModConfigSpec.ConfigValue<String> hudHeaderColor;
        public final ModConfigSpec.BooleanValue showModBadges;
        
        // UI Colors
        public final ModConfigSpec.ConfigValue<String> colorCompleted;
        public final ModConfigSpec.ConfigValue<String> colorIncomplete;
        public final ModConfigSpec.ConfigValue<String> colorInProgress;
        
        // Advanced
        public final ModConfigSpec.BooleanValue debugLogging;
        
        public Common(ModConfigSpec.Builder builder) {
            builder.comment("Gameplay Settings")
                    .push("gameplay");
            
            enablePortalBlocking = builder
                    .comment("Enable blocking portal travel until requirements are met.",
                             "If false, players can access any dimension freely (requirements still tracked for progression).",
                             "Default: true")
                    .define("enable_portal_blocking", true);
            
            autoGrantAdvancements = builder
                    .comment("Should advancements be automatically granted when requirements are met?",
                             "Default: true")
                    .define("auto_grant_advancements", true);
            
            assistTimeWindowSeconds = builder
                    .comment("Time window (in seconds) for kill assists.",
                             "Players who damaged a mob within this window get credit for the kill.",
                             "Useful for cooperative multiplayer gameplay.",
                             "Range: 1-60, Default: 5")
                    .defineInRange("assist_time_window_seconds", 5, 1, 60);
            
            enableGlobalProgress = builder
                    .comment("Enable global (shared) progress mode.",
                             "When enabled, all players share the same progress (kills, items, achievements).",
                             "When disabled, each player has individual progress tracking.",
                             "WARNING: Changing this setting mid-game requires migration. Use /dynamicportals migrate command.",
                             "Default: false")
                    .define("enable_global_progress", false);

            builder.pop();            builder.comment("UI Settings")
                    .push("ui");
            
            maxLinesPerPage = builder
                    .comment("Maximum number of lines per page in the HUD.",
                             "Range: 5-50, Default: 20")
                    .defineInRange("max_lines_per_page", 20, 5, 50);
            
            hudBackgroundColor = builder
                    .comment("HUD background color (ARGB Hex format).",
                             "Example: 0xDD000000 (semi-transparent black)",
                             "Default: 0xDD000000")
                    .define("hud_background_color", "0xDD000000");
            
            hudHeaderColor = builder
                    .comment("HUD header bar color (ARGB Hex format).",
                             "Example: 0xFF4A90E2 (blue)",
                             "Default: 0xFF4A90E2")
                    .define("hud_header_color", "0xFF4A90E2");
            
            showModBadges = builder
                    .comment("Should the HUD show mod badges for modded mobs?",
                             "Displays [MODNAME] next to entities from other mods.",
                             "Default: true")
                    .define("show_mod_badges", true);
            
            builder.comment("HUD Text Colors")
                    .push("colors");
            
            colorCompleted = builder
                    .comment("Color for completed requirements (ARGB Hex).",
                             "Default: 0x55FF55 (green)")
                    .define("completed", "0x55FF55");
            
            colorIncomplete = builder
                    .comment("Color for incomplete requirements (ARGB Hex).",
                             "Default: 0xFF5555 (red)")
                    .define("incomplete", "0xFF5555");
            
            colorInProgress = builder
                    .comment("Color for in-progress indicators (ARGB Hex).",
                             "Default: 0xFFFF55 (yellow)")
                    .define("in_progress", "0xFFFF55");
            
            builder.pop(); // colors
            builder.pop(); // ui
            
            builder.comment("Advanced Settings")
                    .push("advanced");
            
            debugLogging = builder
                    .comment("Enable debug logging for portal requirements.",
                             "Useful for troubleshooting configuration issues.",
                             "Default: false")
                    .define("debug_logging", false);
            
            builder.pop();
        }
        
        // Helper methods for color parsing
        public int getHudBackgroundColor() {
            return parseColor(hudBackgroundColor.get(), 0xDD000000);
        }
        
        public int getHudHeaderColor() {
            return parseColor(hudHeaderColor.get(), 0xFF4A90E2);
        }
        
        public int getColorCompleted() {
            return parseColor(colorCompleted.get(), 0x55FF55);
        }
        
        public int getColorIncomplete() {
            return parseColor(colorIncomplete.get(), 0xFF5555);
        }
        
        public int getColorInProgress() {
            return parseColor(colorInProgress.get(), 0xFFFF55);
        }
        
        private int parseColor(String hex, int defaultValue) {
            try {
                if (hex.startsWith("0x") || hex.startsWith("0X")) {
                    return (int) Long.parseLong(hex.substring(2), 16);
                }
                return (int) Long.parseLong(hex, 16);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }
    
    // Static helper methods for easy access
    public static boolean isGlobalProgressEnabled() {
        return COMMON.enableGlobalProgress.get();
    }
    
    public static boolean isPortalBlockingEnabled() {
        return COMMON.enablePortalBlocking.get();
    }
    
    public static boolean shouldAutoGrantAdvancements() {
        return COMMON.autoGrantAdvancements.get();
    }
    
    public static int getAssistTimeWindowSeconds() {
        return COMMON.assistTimeWindowSeconds.get();
    }
}
