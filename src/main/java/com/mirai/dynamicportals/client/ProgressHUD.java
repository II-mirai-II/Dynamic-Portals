package com.mirai.dynamicportals.client;

import com.mirai.dynamicportals.config.ModConfig;
import com.mirai.dynamicportals.util.ModConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
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
    private static int currentPage = 0;
    private static int totalPages = 1;
    private static List<ResourceLocation> orderedDimensions = null;
    
    // Persist pagination state per dimension
    private static final Map<ResourceLocation, Integer> phasePageMemory = new HashMap<>();
    
    // Performance caches
    private static final Map<EntityType<?>, ResourceLocation> ENTITY_ID_CACHE = new HashMap<>();
    private static final Map<EntityType<?>, Component> MOB_NAME_CACHE = new HashMap<>();
    private static final Map<Item, Component> ITEM_NAME_CACHE = new HashMap<>();
    private static RenderCache currentRenderCache = null;
    private static boolean cacheDirty = true;
    private static ResourceLocation lastDimension = null;
    
    /**
     * Cache for rendered components to avoid recreation every frame
     * Now uses Component objects directly for maximum performance
     */
    private static class RenderCache {
        final Component title;
        final Component tabHint;
        final List<RenderedLine> lines;
        final int hudHeight;
        final int hudWidth;
        
        RenderCache(Component title, Component tabHint, List<RenderedLine> lines, int hudHeight, int hudWidth) {
            this.title = title;
            this.tabHint = tabHint;
            this.lines = lines;
            this.hudHeight = hudHeight;
            this.hudWidth = hudWidth;
        }
    }
    
    private static class RenderedLine {
        final Component component;
        final int x;
        final int yOffset;
        final int color;
        
        RenderedLine(Component component, int x, int yOffset, int color) {
            this.component = component;
            this.x = x;
            this.yOffset = yOffset;
            this.color = color;
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (ModKeyBindings.TOGGLE_HUD_KEY.consumeClick()) {
            hudVisible = !hudVisible;
            if (hudVisible) {
                // Reset to first phase when opening
                currentPhaseIndex = 0;
                cacheDirty = true; // Force cache rebuild
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
                // Save current page for current dimension
                if (lastDimension != null) {
                    phasePageMemory.put(lastDimension, currentPage);
                }
                
                currentPhaseIndex = (currentPhaseIndex + 1) % orderedDimensions.size();
                
                // Restore saved page for new dimension (or start at 0)
                ResourceLocation newDimension = orderedDimensions.get(currentPhaseIndex);
                currentPage = phasePageMemory.getOrDefault(newDimension, 0);
                
                cacheDirty = true; // Force cache rebuild on phase switch
                
                // Play page turn sound
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    mc.player.playSound(net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
                }
            }
        }
        
        // Navigate to next page
        if (hudVisible && ModKeyBindings.NEXT_PAGE_KEY.consumeClick()) {
            if (currentPage < totalPages - 1) {
                currentPage++;
                cacheDirty = true;
                
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    mc.player.playSound(net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
                }
            }
        }
        
        // Navigate to previous page
        if (hudVisible && ModKeyBindings.PREV_PAGE_KEY.consumeClick()) {
            if (currentPage > 0) {
                currentPage--;
                cacheDirty = true;
                
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
        
        // Get all requirements from CLIENT CACHE (now optimized)
        Map<ResourceLocation, ClientRequirementsCache.CachedRequirement> allRequirements = ClientRequirementsCache.getAllRequirements();
        
        if (allRequirements.isEmpty()) {
            return;
        }
        
        // Initialize ordered dimensions list (sorted by custom sortOrder if configured)
        if (orderedDimensions == null || orderedDimensions.isEmpty()) {
            orderedDimensions = new ArrayList<>(allRequirements.keySet());
            orderedDimensions.sort((a, b) -> {
                ClientRequirementsCache.CachedRequirement reqA = allRequirements.get(a);
                ClientRequirementsCache.CachedRequirement reqB = allRequirements.get(b);
                
                // Use custom sort order if both have it configured
                if (reqA.getSortOrder() != 0 || reqB.getSortOrder() != 0) {
                    return Integer.compare(reqA.getSortOrder(), reqB.getSortOrder());
                }
                
                // Fallback to default sorting (Nether first, then End)
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
        
        ResourceLocation currentDim = orderedDimensions.get(currentPhaseIndex);
        ClientRequirementsCache.CachedRequirement req = allRequirements.get(currentDim);
        
        if (req == null) {
            return;
        }
        
        // Check if we need to rebuild the cache (dirty flag system)
        if (currentRenderCache == null || cacheDirty || !currentDim.equals(lastDimension)) {
            currentRenderCache = buildRenderCache(req, mc);
            cacheDirty = false;
            lastDimension = currentDim;
        }
        
        // Calculate dimensions
        int hudX = screenWidth - currentRenderCache.hudWidth - 10;
        int hudY = 10;
        
        // Background (configurable colors)
        int backgroundColor = ModConfig.COMMON.getHudBackgroundColor();
        int headerColor = ModConfig.COMMON.getHudHeaderColor();
        guiGraphics.fill(hudX - 5, hudY - 5, hudX + currentRenderCache.hudWidth + 5, hudY + currentRenderCache.hudHeight + 5, backgroundColor);
        guiGraphics.fill(hudX - 5, hudY - 5, hudX + currentRenderCache.hudWidth + 5, hudY - 3, headerColor);
        
        // BATCH TEXT RENDERING - All text in single batch for maximum performance
        PoseStack poseStack = guiGraphics.pose();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        Font font = mc.font;
        
        int yOffset = hudY;
        
        // Title (centered, cached Component)
        int titleWidth = font.width(currentRenderCache.title);
        font.drawInBatch(currentRenderCache.title, hudX + (currentRenderCache.hudWidth - titleWidth) / 2, yOffset, 0xFFFFFF, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        yOffset += 15;
        
        // Phase navigation hint (cached Component)
        font.drawInBatch(currentRenderCache.tabHint, hudX + 5, yOffset, 0xAAAAAA, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        yOffset += 15;
        
        // Render all cached lines in batch
        for (RenderedLine line : currentRenderCache.lines) {
            font.drawInBatch(line.component, hudX + line.x, yOffset + line.yOffset, line.color, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        }
        
        // Flush the batch - all text rendered in one go!
        bufferSource.endBatch();
    }

    /**
     * Clear all caches - call when requirements or progress update
     */
    public static void invalidateCache() {
        cacheDirty = true;
        currentRenderCache = null;
        lastDimension = null;
        orderedDimensions = null;
        currentPage = 0; // Reset to first page
        totalPages = 1;
        phasePageMemory.clear(); // Clear pagination memory
        // Keep name/id caches as they're static and permanent
    }
    

    
    /**
     * Build complete render cache for current requirement
     * Optimized with permanent Component caching and StringBuilder
     */
    private static RenderCache buildRenderCache(ClientRequirementsCache.CachedRequirement req, Minecraft mc) {
        Set<ResourceLocation> killedMobIds = ClientProgressCache.getKilledMobIds();
        Set<ResourceLocation> unlockedAchievements = ClientProgressCache.getUnlockedAchievements();
        
        // Title (cached Component)
        Component title = Component.literal("§6§lPortal Requirements");
        
        // Phase navigation hint (optimized with StringBuilder)
        String switchKey = ModKeyBindings.SWITCH_PHASE_KEY.getTranslatedKeyMessage().getString();
        String prevKey = ModKeyBindings.PREV_PAGE_KEY.getTranslatedKeyMessage().getString();
        String nextKey = ModKeyBindings.NEXT_PAGE_KEY.getTranslatedKeyMessage().getString();
        StringBuilder tabHintBuilder = new StringBuilder(80);
        tabHintBuilder.append("§7[").append(switchKey).append("] Phase §8(").append(currentPhaseIndex + 1).append("/").append(orderedDimensions.size()).append(") ");
        tabHintBuilder.append("§7[").append(prevKey).append("/").append(nextKey).append("] Page §8(").append(currentPage + 1).append("/").append(totalPages).append(")");
        Component tabHint = Component.literal(tabHintBuilder.toString());
        
        List<RenderedLine> lines = new ArrayList<>();
        int yOffset = 0;
        
        // Check achievement status
        boolean achievementUnlocked = req.getAdvancement() == null || unlockedAchievements.contains(req.getAdvancement());
        
        // Count total and killed
        List<EntityType<?>> allMobs = new ArrayList<>();
        allMobs.addAll(req.getMobs());
        allMobs.addAll(req.getBosses());
        
        int totalMobs = allMobs.size();
        int totalItems = req.getItems().size();
        int totalRequirements = totalMobs + totalItems;
        
        int killedMobs = 0;
        for (EntityType<?> mob : allMobs) {
            ResourceLocation mobId = getCachedEntityId(mob);
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
        
        // Dimension title (optimized with StringBuilder)
        // Use custom display name if configured, otherwise use default dimension name
        String dimName = req.getDisplayName() != null ? req.getDisplayName() : getDimensionName(req.getDimension());
        String statusIcon = isCompleted ? "§a✔" : (achievementUnlocked ? "§e⚠" : "§c✘");
        
        // Use custom display color if configured, otherwise use dimension-based color
        int dimColor = req.getDisplayColor() != null ? req.getDisplayColor() : 
                      (req.getDimension().getPath().contains("nether") ? 0xFFFF55 : 0xFF5555);
        
        StringBuilder dimTitleBuilder = new StringBuilder(50);
        dimTitleBuilder.append(statusIcon).append(" ").append(dimName);
        Component dimTitle = Component.literal(dimTitleBuilder.toString()).withStyle(style -> style.withBold(true));
        lines.add(new RenderedLine(dimTitle, 5, yOffset, dimColor));
        yOffset += 15;
        
        // Progress counter (optimized with StringBuilder)
        StringBuilder progressBuilder = new StringBuilder(30);
        progressBuilder.append("§7Progress: §f").append(totalCompleted).append("/").append(totalRequirements);
        Component progressText = Component.literal(progressBuilder.toString());
        lines.add(new RenderedLine(progressText, 5, yOffset, 0xFFFFFF));
        yOffset += 1;
        
        // If completed, show completion message
        if (isCompleted) {
            Component completedMsg = Component.literal("§a§l✔ COMPLETED!");
            lines.add(new RenderedLine(completedMsg, 5, yOffset, 0x55FF55));
            int hudHeight = 60 + yOffset;
            return new RenderCache(title, tabHint, lines, hudHeight, 320);
        }
        
        yOffset += 20; // Spacing increased for better separation
        
        // Collect all content lines (before pagination)
        List<RenderedLine> allContentLines = new ArrayList<>();
        int contentYOffset = 0;
        
        // SECTION 1: Regular Mobs
        if (!req.getMobs().isEmpty()) {
            Component mobsHeader = Component.literal("§b§lRequired Mobs");
            allContentLines.add(new RenderedLine(mobsHeader, 5, contentYOffset, 0xAAAAFF));
            contentYOffset += 12;
            
            for (EntityType<?> mob : req.getMobs()) {
                RenderedLine line = buildMobLine(mob, killedMobIds, 15, contentYOffset);
                allContentLines.add(line);
                contentYOffset += 12;
            }
            contentYOffset += 8;
        }
        
        // SECTION 2: Bosses
        if (!req.getBosses().isEmpty()) {
            Component bossHeader = Component.literal("§c§lRequired Bosses");
            allContentLines.add(new RenderedLine(bossHeader, 5, contentYOffset, 0xFFAAAA));
            contentYOffset += 12;
            
            for (EntityType<?> boss : req.getBosses()) {
                RenderedLine line = buildMobLine(boss, killedMobIds, 15, contentYOffset);
                allContentLines.add(line);
                contentYOffset += 12;
            }
            contentYOffset += 8;
        }
        
        // SECTION 3: Items
        if (!req.getItems().isEmpty()) {
            Component itemsHeader = Component.literal("§a§lRequired Items");
            allContentLines.add(new RenderedLine(itemsHeader, 5, contentYOffset, 0xAAFFAA));
            contentYOffset += 12;
            
            for (Item item : req.getItems()) {
                RenderedLine line = buildItemLine(item, 15, contentYOffset);
                allContentLines.add(line);
                contentYOffset += 12;
            }
        }
        
        // Calculate total pages using config
        int maxLinesPerPage = ModConfig.COMMON.maxLinesPerPage.get();
        totalPages = Math.max(1, (int) Math.ceil((double) allContentLines.size() / maxLinesPerPage));
        
        // Ensure current page is valid
        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }
        
        // Get lines for current page
        int startIdx = currentPage * maxLinesPerPage;
        int endIdx = Math.min(startIdx + maxLinesPerPage, allContentLines.size());
        
        // Add only the lines for current page, using accumulated yOffset
        for (int i = startIdx; i < endIdx; i++) {
            RenderedLine originalLine = allContentLines.get(i);
            lines.add(new RenderedLine(originalLine.component, originalLine.x, yOffset, originalLine.color));
            yOffset += 12;
        }
        
        int hudHeight = 60 + yOffset;
        return new RenderCache(title, tabHint, lines, hudHeight, 320);
    }
    
    /**
     * Build cached line for mob/boss
     * Optimized with permanent Component cache and StringBuilder
     */
    private static RenderedLine buildMobLine(EntityType<?> mob, Set<ResourceLocation> killedMobIds, int x, int yOffset) {
        ResourceLocation mobId = getCachedEntityId(mob);
        boolean mobKilled = mobId != null && killedMobIds.contains(mobId);
        
        String checkbox = mobKilled ? "☑" : "☐";
        int color = mobKilled ? 0x55FF55 : 0xFF5555;
        
        Component mobNameComponent = getCachedMobName(mob);
        String mobName = mobNameComponent.getString();
        
        // Build text with StringBuilder for performance
        StringBuilder textBuilder = new StringBuilder(100);
        textBuilder.append(checkbox).append(" §f").append(mobName);
        
        // Mod badge if not vanilla
        if (mobId != null && !mobId.getNamespace().equals("minecraft")) {
            textBuilder.append(" §8[§b").append(mobId.getNamespace().toUpperCase()).append("§8]");
        }
        
        Component text = Component.literal(textBuilder.toString());
        return new RenderedLine(text, x, yOffset, color);
    }
    
    /**
     * Build cached line for item
     * Optimized with permanent Component cache and StringBuilder
     */
    private static RenderedLine buildItemLine(Item item, int x, int yOffset) {
        boolean hasItem = ClientProgressCache.hasItemBeenObtained(item);
        
        String checkbox = hasItem ? "☑" : "☐";
        int color = hasItem ? 0x55FF55 : 0xFF5555;
        
        Component itemNameComponent = getCachedItemName(item);
        String itemName = itemNameComponent.getString();
        
        StringBuilder textBuilder = new StringBuilder(50);
        textBuilder.append(checkbox).append(" §f").append(itemName);
        
        Component text = Component.literal(textBuilder.toString());
        return new RenderedLine(text, x, yOffset, color);
    }
    
    /**
     * Get cached ResourceLocation for EntityType (eliminates registry lookups)
     */
    private static ResourceLocation getCachedEntityId(EntityType<?> entity) {
        return ENTITY_ID_CACHE.computeIfAbsent(entity, e -> BuiltInRegistries.ENTITY_TYPE.getKey(e));
    }
    
    /**
     * Get cached Component for mob (permanent cache - zero translation overhead)
     */
    private static Component getCachedMobName(EntityType<?> mob) {
        return MOB_NAME_CACHE.computeIfAbsent(mob, m -> m.getDescription());
    }
    
    /**
     * Get cached Component for item (permanent cache - zero translation overhead)
     */
    private static Component getCachedItemName(Item item) {
        return ITEM_NAME_CACHE.computeIfAbsent(item, i -> i.getDescription());
    }
    
    private static String getDimensionName(ResourceLocation dimension) {
        String path = dimension.getPath();
        if (path.contains("nether")) return "Nether Portal";
        if (path.contains("end")) return "End Portal";
        if (path.equals("overworld")) return "Overworld";
        return path;
    }
}
