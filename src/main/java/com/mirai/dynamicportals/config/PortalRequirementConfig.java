package com.mirai.dynamicportals.config;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Configuration model for portal requirements loaded from JSON files.
 * Used by both internal default configs and user-customizable configs.
 */
public class PortalRequirementConfig {
    
    @SerializedName("version")
    private int version = 1;
    
    @SerializedName("comment")
    private String comment;
    
    @SerializedName("config_version")
    private int configVersion = 1;
    
    @SerializedName("override_defaults")
    private boolean overrideDefaults = false;
    
    @SerializedName("portals")
    private Map<String, PortalConfig> portals;
    
    public int getVersion() {
        return version;
    }
    
    public String getComment() {
        return comment;
    }
    
    public int getConfigVersion() {
        return configVersion;
    }
    
    public boolean isOverrideDefaults() {
        return overrideDefaults;
    }
    
    public Map<String, PortalConfig> getPortals() {
        return portals;
    }
    
    /**
     * Configuration for a single portal/dimension
     */
    public static class PortalConfig {
        
        @SerializedName("enabled")
        private boolean enabled = true;
        
        @SerializedName("advancement")
        private String advancement;
        
        @SerializedName("requirements")
        private RequirementsSection requirements;
        
        @SerializedName("display")
        private DisplaySection display;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public String getAdvancement() {
            return advancement;
        }
        
        public RequirementsSection getRequirements() {
            return requirements;
        }
        
        public DisplaySection getDisplay() {
            return display;
        }
    }
    
    /**
     * Requirements section containing mobs, bosses, and items
     */
    public static class RequirementsSection {
        
        @SerializedName("mobs")
        private Map<String, List<String>> mobs;
        
        @SerializedName("bosses")
        private List<String> bosses;
        
        @SerializedName("items")
        private List<String> items;
        
        public Map<String, List<String>> getMobs() {
            return mobs;
        }
        
        public List<String> getBosses() {
            return bosses;
        }
        
        public List<String> getItems() {
            return items;
        }
    }
    
    /**
     * Display settings for HUD rendering
     */
    public static class DisplaySection {
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("description")
        private String description;
        
        @SerializedName("color")
        private Integer color;
        
        @SerializedName("icon")
        private String icon;
        
        @SerializedName("sort_order")
        private int sortOrder = 0;
        
        @SerializedName("blocked_message")
        private String blockedMessage;
        
        @SerializedName("unlocked_message")
        private String unlockedMessage;
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public Integer getColor() {
            return color;
        }
        
        public String getIcon() {
            return icon;
        }
        
        public int getSortOrder() {
            return sortOrder;
        }
        
        public String getBlockedMessage() {
            return blockedMessage;
        }
        
        public String getUnlockedMessage() {
            return unlockedMessage;
        }
    }
}
