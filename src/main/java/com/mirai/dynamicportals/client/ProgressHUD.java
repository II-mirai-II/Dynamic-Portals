package com.mirai.dynamicportals.client;

import com.mirai.dynamicportals.util.ModConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

import java.util.*;

@EventBusSubscriber(modid = ModConstants.MOD_ID, value = Dist.CLIENT)
public class ProgressHUD {
    
    private static boolean hudVisible = false;
    private static int currentPhaseIndex = 0;
    private static List<ResourceLocation> orderedDimensions = null;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (ModKeyBindings.TOGGLE_HUD_KEY.consumeClick()) {
            hudVisible = !hudVisible;
            if (hudVisible) {
                // Reset to first phase when opening
                currentPhaseIndex = 0;
            }
            
            // Play UI click sound when toggling HUD
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
            }
        }
        
        // Switch phases when HUD is visible
        if (hudVisible && ModKeyBindings.SWITCH_PHASE_KEY.consumeClick()) {
            if (orderedDimensions != null && !orderedDimensions.isEmpty()) {
                currentPhaseIndex = (currentPhaseIndex + 1) % orderedDimensions.size();
                
                // Play page turn sound
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    mc.player.playSound(net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiLayerEvent.Post event) {
        if (!hudVisible || !ClientProgressCache.isCacheValid() || !ClientRequirementsCache.isCacheValid()) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        
        if (mc.player == null) {
            return;
        }
        
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        
        // Get all requirements from CLIENT CACHE
        Map<ResourceLocation, ClientRequirementsCache.CachedRequirement> allRequirements = ClientRequirementsCache.getAllRequirements();
        
        if (allRequirements.isEmpty()) {
            return;
        }
        
        // Initialize ordered dimensions list (Nether first, then End)
        if (orderedDimensions == null || orderedDimensions.isEmpty()) {
            orderedDimensions = new ArrayList<>(allRequirements.keySet());
            orderedDimensions.sort((a, b) -> {
                if (a.getPath().contains("nether")) return -1;
                if (b.getPath().contains("nether")) return 1;
                if (a.getPath().contains("end")) return 1;
                if (b.getPath().contains("end")) return -1;
                return a.compareTo(b);
            });
        }
        
        // Ensure current index is valid
        if (currentPhaseIndex >= orderedDimensions.size()) {
            currentPhaseIndex = 0;
        }
        
        Set<ResourceLocation> killedMobIds = ClientProgressCache.getKilledMobIds();
        Set<ResourceLocation> unlockedAchievements = ClientProgressCache.getUnlockedAchievements();
        
        // Calculate dimensions
        int hudWidth = 320;
        int hudX = screenWidth - hudWidth - 10;
        int hudY = 10;
        
        // Render current phase
        ResourceLocation currentDim = orderedDimensions.get(currentPhaseIndex);
        ClientRequirementsCache.CachedRequirement req = allRequirements.get(currentDim);
        
        if (req == null) {
            return;
        }
        
        // Calculate height dynamically
        int hudHeight = calculateRequirementHeight(req);
        
        // Background
        guiGraphics.fill(hudX - 5, hudY - 5, hudX + hudWidth + 5, hudY + hudHeight + 5, 0xDD000000);
        guiGraphics.fill(hudX - 5, hudY - 5, hudX + hudWidth + 5, hudY - 3, 0xFF4A90E2);
        
        int yOffset = hudY;
        
        // Title
        Component title = Component.literal("§6§lPortal Requirements");
        int titleWidth = mc.font.width(title);
        guiGraphics.drawString(mc.font, title, hudX + (hudWidth - titleWidth) / 2, yOffset, 0xFFFFFF);
        yOffset += 15;
        
        // Phase navigation hint - show the actual key binding
        String switchKey = ModKeyBindings.SWITCH_PHASE_KEY.getTranslatedKeyMessage().getString();
        Component tabHint = Component.literal("§7[" + switchKey + "] Next Phase §8(" + (currentPhaseIndex + 1) + "/" + orderedDimensions.size() + ")");
        guiGraphics.drawString(mc.font, tabHint, hudX + 5, yOffset, 0xAAAAAA);
        yOffset += 15;
        
        // Render the current requirement
        renderRequirement(guiGraphics, mc, hudX, yOffset, hudWidth, req, killedMobIds, unlockedAchievements);
    }

    private static void renderRequirement(GuiGraphics guiGraphics, Minecraft mc, int hudX, int yOffset, 
                                          int hudWidth, ClientRequirementsCache.CachedRequirement req, 
                                          Set<ResourceLocation> killedMobIds, 
                                          Set<ResourceLocation> unlockedAchievements) {
        
        // Check achievement status
        boolean achievementUnlocked = req.getAdvancement() == null || 
                                      unlockedAchievements.contains(req.getAdvancement());
        
        // Count total and killed
        List<EntityType<?>> allMobs = new ArrayList<>();
        allMobs.addAll(req.getMobs());
        allMobs.addAll(req.getBosses());
        
        int totalMobs = allMobs.size();
        int totalItems = req.getItems().size();
        int totalRequirements = totalMobs + totalItems;
        
        int killedMobs = 0;
        for (EntityType<?> mob : allMobs) {
            ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(mob);
            if (mobId != null && killedMobIds.contains(mobId)) {
                killedMobs++;
            }
        }
        
        int obtainedItems = 0;
        for (Item item : req.getItems()) {
            if (ClientProgressCache.hasItemBeenObtained(item)) {
                obtainedItems++;
            }
        }
        
        int totalCompleted = killedMobs + obtainedItems;
        boolean isCompleted = totalCompleted == totalRequirements && achievementUnlocked;
        
        // Dimension title
        String dimName = getDimensionName(req.getDimension());
        String statusIcon = isCompleted ? "§a✔" : (achievementUnlocked ? "§e⚠" : "§c✘");
        int dimColor = req.getDimension().getPath().contains("nether") ? 0xFFFF55 : 0xFF5555;
        Component dimTitle = Component.literal(statusIcon + " ").append(Component.literal(dimName).withStyle(style -> style.withBold(true)));
        guiGraphics.drawString(mc.font, dimTitle, hudX + 5, yOffset, dimColor);
        yOffset += 15;
        
        // Progress counter
        Component progressText = Component.literal("§7Progress: §f" + totalCompleted + "/" + totalRequirements);
        guiGraphics.drawString(mc.font, progressText, hudX + 5, yOffset, 0xFFFFFF);
        yOffset += 15;
        
        // If completed, show completion message
        if (isCompleted) {
            Component completedMsg = Component.literal("§a§l✔ COMPLETED!");
            guiGraphics.drawString(mc.font, completedMsg, hudX + 5, yOffset, 0x55FF55);
            return;
        }
        
        yOffset += 5; // Spacing
        
        // SECTION 1: Regular Mobs
        if (!req.getMobs().isEmpty()) {
            Component mobsHeader = Component.literal("§b§lRequired Mobs");
            guiGraphics.drawString(mc.font, mobsHeader, hudX + 5, yOffset, 0xAAAAFF);
            yOffset += 12;
            
            for (EntityType<?> mob : req.getMobs()) {
                yOffset = renderMobLine(guiGraphics, mc, hudX + 15, yOffset, mob, killedMobIds);
            }
            yOffset += 8;
        }
        
        // SECTION 2: Bosses
        if (!req.getBosses().isEmpty()) {
            Component bossHeader = Component.literal("§c§lRequired Bosses");
            guiGraphics.drawString(mc.font, bossHeader, hudX + 5, yOffset, 0xFFAAAA);
            yOffset += 12;
            
            for (EntityType<?> boss : req.getBosses()) {
                yOffset = renderMobLine(guiGraphics, mc, hudX + 15, yOffset, boss, killedMobIds);
            }
            yOffset += 8;
        }
        
        // SECTION 3: Items
        if (!req.getItems().isEmpty()) {
            Component itemsHeader = Component.literal("§a§lRequired Items");
            guiGraphics.drawString(mc.font, itemsHeader, hudX + 5, yOffset, 0xAAFFAA);
            yOffset += 12;
            
            for (Item item : req.getItems()) {
                yOffset = renderItemLine(guiGraphics, mc, hudX + 15, yOffset, item);
            }
        }
    }

    private static int renderMobLine(GuiGraphics guiGraphics, Minecraft mc, int x, int y, 
                                     EntityType<?> mob, Set<ResourceLocation> killedMobIds) {
        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(mob);
        boolean mobKilled = mobId != null && killedMobIds.contains(mobId);
        
        // Unicode checkboxes
        String checkbox = mobKilled ? "☑" : "☐";
        int color = mobKilled ? 0x55FF55 : 0xFF5555;
        
        String mobName = mob.getDescription().getString();
        
        // Mod badge if not vanilla
        String modBadge = "";
        if (mobId != null && !mobId.getNamespace().equals("minecraft")) {
            modBadge = " §8[§b" + mobId.getNamespace().toUpperCase() + "§8]";
        }
        
        Component text = Component.literal(checkbox + " §f" + mobName + modBadge);
        guiGraphics.drawString(mc.font, text, x, y, color);
        
        return y + 12;
    }

    private static int renderItemLine(GuiGraphics guiGraphics, Minecraft mc, int x, int y, Item item) {
        boolean hasItem = ClientProgressCache.hasItemBeenObtained(item);
        
        String checkbox = hasItem ? "☑" : "☐";
        int color = hasItem ? 0x55FF55 : 0xFF5555;
        
        String itemName = item.getDescription().getString();
        Component text = Component.literal(checkbox + " §f" + itemName);
        guiGraphics.drawString(mc.font, text, x, y, color);
        
        return y + 12;
    }

    private static String getDimensionName(ResourceLocation dimension) {
        String path = dimension.getPath();
        if (path.contains("nether")) return "Nether Portal";
        if (path.contains("end")) return "End Portal";
        if (path.equals("overworld")) return "Overworld";
        return path;
    }

    private static int calculateRequirementHeight(ClientRequirementsCache.CachedRequirement req) {
        int height = 60; // Title + phase hint + dim title + progress
        
        // Sections
        if (!req.getMobs().isEmpty()) {
            height += 12 + (req.getMobs().size() * 12) + 8; // Header + mobs + spacing
        }
        
        if (!req.getBosses().isEmpty()) {
            height += 12 + (req.getBosses().size() * 12) + 8; // Header + bosses + spacing
        }
        
        if (!req.getItems().isEmpty()) {
            height += 12 + (req.getItems().size() * 12); // Header + items
        }
        
        return Math.min(height, 500);
    }
}
