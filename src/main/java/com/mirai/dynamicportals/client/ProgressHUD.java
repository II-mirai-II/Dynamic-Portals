package com.mirai.dynamicportals.client;

import com.mirai.dynamicportals.util.ModConstants;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

import java.util.List;

@EventBusSubscriber(modid = ModConstants.MOD_ID, value = Dist.CLIENT)
public class ProgressHUD {
    
    private static boolean hudVisible = false;
    private static boolean overworldPhase = true; // true = overworld->nether, false = nether->end

    // Overworld -> Nether requirements
    private static final List<EntityType<?>> OVERWORLD_MOBS = List.of(
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.SPIDER,
            EntityType.ENDERMAN, EntityType.WITCH, EntityType.SLIME, EntityType.DROWNED,
            EntityType.HUSK, EntityType.STRAY, EntityType.BREEZE, EntityType.BOGGED,
            EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.EVOKER
    );
    private static final List<EntityType<?>> OVERWORLD_BOSSES = List.of(EntityType.ELDER_GUARDIAN);

    // Nether -> End requirements
    private static final List<EntityType<?>> NETHER_MOBS = List.of(
            EntityType.GHAST, EntityType.BLAZE, EntityType.WITHER_SKELETON,
            EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.HOGLIN
    );
    private static final List<EntityType<?>> NETHER_BOSSES = List.of(EntityType.WARDEN, EntityType.WITHER);

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        
        if (ModKeyBindings.TOGGLE_HUD_KEY.consumeClick()) {
            hudVisible = !hudVisible;
        }

        // Switch phase with Tab when HUD is open
        if (hudVisible && event.getKey() == InputConstants.KEY_TAB && event.getAction() == InputConstants.PRESS) {
            overworldPhase = !overworldPhase;
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiLayerEvent.Post event) {
        if (!hudVisible) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        // Fixed dimensions that scale naturally with GUI scale
        int hudWidth = 200;
        int lineHeight = 10;
        int lineSpacing = 2;
        int sectionSpacing = 4;
        int padding = 8;
        int indent = 8;
        
        // Position from top-right with padding
        int hudX = screenWidth - hudWidth - padding;
        int hudY = padding;

        // Dynamic height calculation
        int estimatedHeight = calculateHUDHeight(lineHeight, lineSpacing, sectionSpacing);

        // Background with padding
        int bgPadding = 4;
        int backgroundColor = 0xA0000000; // Semi-transparent black
        guiGraphics.fill(hudX - bgPadding, hudY - bgPadding, hudX + hudWidth + bgPadding, hudY + estimatedHeight + bgPadding, backgroundColor);

        // Title
        Component title = Component.translatable(ModConstants.HUD_TITLE);
        guiGraphics.drawString(mc.font, title, hudX, hudY, 0xFFFFFF, true);
        hudY += lineHeight + lineSpacing;

        // Death counter
        int deathCount = ClientProgressCache.getDeathCount();
        int deathColor = getDeathCountColor(deathCount);
        Component deathText = Component.translatable(ModConstants.HUD_DEATHS, deathCount, ModConstants.DEATH_THRESHOLD);
        guiGraphics.drawString(mc.font, deathText, hudX, hudY, deathColor, true);
        hudY += lineHeight + sectionSpacing;

        // Phase tabs hint
        Component tabHint = Component.literal("[Tab] Switch Phase");
        guiGraphics.drawString(mc.font, tabHint, hudX, hudY, 0xAAAAAA, false);
        hudY += lineHeight + sectionSpacing;

        // Current phase display
        if (overworldPhase) {
            renderOverworldPhase(guiGraphics, mc, hudX, hudY, lineHeight, lineSpacing, sectionSpacing, indent);
        } else {
            renderNetherPhase(guiGraphics, mc, hudX, hudY, lineHeight, lineSpacing, sectionSpacing, indent);
        }
    }

    private static int calculateHUDHeight(int lineHeight, int lineSpacing, int sectionSpacing) {
        // Base: title + death counter + tab hint
        int height = (lineHeight + lineSpacing) + (lineHeight + sectionSpacing) + (lineHeight + sectionSpacing);
        
        if (overworldPhase) {
            if (!ClientProgressCache.isAchievementUnlocked(ModConstants.NETHER_ACCESS_ADVANCEMENT)) {
                // Phase title + progress line
                height += (lineHeight + lineSpacing) + (lineHeight + lineSpacing);
                // Mobs header + mobs
                height += (lineHeight + lineSpacing) + (OVERWORLD_MOBS.size() * (lineHeight + lineSpacing));
                // Boss header + bosses
                height += sectionSpacing + (lineHeight + lineSpacing) + (OVERWORLD_BOSSES.size() * (lineHeight + lineSpacing));
                // Items header + items
                height += sectionSpacing + (lineHeight + lineSpacing) + (lineHeight + lineSpacing);
            } else {
                // Just phase title + completed message
                height += (lineHeight + lineSpacing) + (lineHeight + lineSpacing);
            }
        } else {
            if (!ClientProgressCache.isAchievementUnlocked(ModConstants.END_ACCESS_ADVANCEMENT)) {
                // Phase title + progress line
                height += (lineHeight + lineSpacing) + (lineHeight + lineSpacing);
                // Mobs header + mobs
                height += (lineHeight + lineSpacing) + (NETHER_MOBS.size() * (lineHeight + lineSpacing));
                // Boss header + bosses
                height += sectionSpacing + (lineHeight + lineSpacing) + (NETHER_BOSSES.size() * (lineHeight + lineSpacing));
                // Items header + items
                height += sectionSpacing + (lineHeight + lineSpacing) + (lineHeight + lineSpacing);
            } else {
                // Just phase title + completed message
                height += (lineHeight + lineSpacing) + (lineHeight + lineSpacing);
            }
        }
        
        return height;
    }

    private static void renderOverworldPhase(GuiGraphics guiGraphics, Minecraft mc, int x, int y, int lineHeight, int lineSpacing, int sectionSpacing, int indent) {
        // Phase title
        Component phaseTitle = Component.translatable(ModConstants.HUD_PHASE_OVERWORLD);
        guiGraphics.drawString(mc.font, phaseTitle, x, y, 0xFFFF55, true);
        y += lineHeight + lineSpacing;

        // Check if phase is completed
        boolean phaseCompleted = ClientProgressCache.isAchievementUnlocked(ModConstants.NETHER_ACCESS_ADVANCEMENT);
        if (phaseCompleted) {
            Component completed = Component.translatable(ModConstants.HUD_COMPLETED);
            guiGraphics.drawString(mc.font, completed, x, y, 0x55FF55, true);
            return;
        }

        // Progress bar
        int killed = 0;
        int total = OVERWORLD_MOBS.size() + OVERWORLD_BOSSES.size() + 1; // +1 for diamond
        
        for (EntityType<?> mob : OVERWORLD_MOBS) {
            if (ClientProgressCache.hasMobBeenKilled(mob)) killed++;
        }
        for (EntityType<?> boss : OVERWORLD_BOSSES) {
            if (ClientProgressCache.hasMobBeenKilled(boss)) killed++;
        }
        if (ClientProgressCache.hasItemBeenObtained(Items.DIAMOND)) killed++;
        
        Component progressText = Component.translatable(ModConstants.HUD_PROGRESS_OVERWORLD, killed, total);
        guiGraphics.drawString(mc.font, progressText, x, y, 0xFFFFFF, false);
        y += lineHeight + lineSpacing;

        // Render mob checklist
        Component mobsHeader = Component.translatable(ModConstants.HUD_REQUIRED_MOBS);
        guiGraphics.drawString(mc.font, mobsHeader, x, y, 0xAAAAFF, false);
        y += lineHeight + lineSpacing;

        for (EntityType<?> mob : OVERWORLD_MOBS) {
            y = renderMobEntry(guiGraphics, mc, x + indent, y, mob, lineHeight, lineSpacing);
        }

        // Render boss checklist
        y += sectionSpacing;
        Component bossHeader = Component.translatable(ModConstants.HUD_REQUIRED_BOSSES);
        guiGraphics.drawString(mc.font, bossHeader, x, y, 0xFFAAAA, false);
        y += lineHeight + lineSpacing;

        for (EntityType<?> boss : OVERWORLD_BOSSES) {
            y = renderMobEntry(guiGraphics, mc, x + indent, y, boss, lineHeight, lineSpacing);
        }

        // Required items
        y += sectionSpacing;
        Component itemsHeader = Component.translatable(ModConstants.HUD_REQUIRED_ITEMS);
        guiGraphics.drawString(mc.font, itemsHeader, x, y, 0xAAFFAA, false);
        y += lineHeight + lineSpacing;

        boolean hasDiamond = ClientProgressCache.hasItemBeenObtained(Items.DIAMOND);
        Component diamondText = Component.literal("Diamond");
        int diamondColor = hasDiamond ? 0x55FF55 : 0xFF5555;
        String diamondCheckbox = hasDiamond ? "☑" : "☐";
        guiGraphics.drawString(mc.font, diamondCheckbox + " " + diamondText.getString(), x + indent, y, diamondColor, false);
    }

    private static void renderNetherPhase(GuiGraphics guiGraphics, Minecraft mc, int x, int y, int lineHeight, int lineSpacing, int sectionSpacing, int indent) {
        // Phase title
        Component phaseTitle = Component.translatable(ModConstants.HUD_PHASE_NETHER);
        guiGraphics.drawString(mc.font, phaseTitle, x, y, 0xFF5555, true);
        y += lineHeight + lineSpacing;

        // Check if phase is completed
        boolean phaseCompleted = ClientProgressCache.isAchievementUnlocked(ModConstants.END_ACCESS_ADVANCEMENT);
        if (phaseCompleted) {
            Component completed = Component.translatable(ModConstants.HUD_COMPLETED);
            guiGraphics.drawString(mc.font, completed, x, y, 0x55FF55, true);
            return;
        }

        // Progress bar
        int killed = 0;
        int total = NETHER_MOBS.size() + NETHER_BOSSES.size() + 1; // +1 for netherite
        
        for (EntityType<?> mob : NETHER_MOBS) {
            if (ClientProgressCache.hasMobBeenKilled(mob)) killed++;
        }
        for (EntityType<?> boss : NETHER_BOSSES) {
            if (ClientProgressCache.hasMobBeenKilled(boss)) killed++;
        }
        if (ClientProgressCache.hasItemBeenObtained(Items.NETHERITE_INGOT)) killed++;
        
        Component progressText = Component.translatable(ModConstants.HUD_PROGRESS_NETHER, killed, total);
        guiGraphics.drawString(mc.font, progressText, x, y, 0xFFFFFF, false);
        y += lineHeight + lineSpacing;

        // Render mob checklist
        Component mobsHeader = Component.translatable(ModConstants.HUD_REQUIRED_MOBS);
        guiGraphics.drawString(mc.font, mobsHeader, x, y, 0xAAAAFF, false);
        y += lineHeight + lineSpacing;

        for (EntityType<?> mob : NETHER_MOBS) {
            y = renderMobEntry(guiGraphics, mc, x + indent, y, mob, lineHeight, lineSpacing);
        }

        // Render boss checklist
        y += sectionSpacing;
        Component bossHeader = Component.translatable(ModConstants.HUD_REQUIRED_BOSSES);
        guiGraphics.drawString(mc.font, bossHeader, x, y, 0xFFAAAA, false);
        y += lineHeight + lineSpacing;

        for (EntityType<?> boss : NETHER_BOSSES) {
            y = renderMobEntry(guiGraphics, mc, x + indent, y, boss, lineHeight, lineSpacing);
        }

        // Required items
        y += sectionSpacing;
        Component itemsHeader = Component.translatable(ModConstants.HUD_REQUIRED_ITEMS);
        guiGraphics.drawString(mc.font, itemsHeader, x, y, 0xAAFFAA, false);
        y += lineHeight + lineSpacing;

        boolean hasNetherite = ClientProgressCache.hasItemBeenObtained(Items.NETHERITE_INGOT);
        Component netheriteText = Component.literal("Netherite Ingot");
        int netheriteColor = hasNetherite ? 0x55FF55 : 0xFF5555;
        String netheriteCheckbox = hasNetherite ? "☑" : "☐";
        guiGraphics.drawString(mc.font, netheriteCheckbox + " " + netheriteText.getString(), x + indent, y, netheriteColor, false);
    }

    private static int renderMobEntry(GuiGraphics guiGraphics, Minecraft mc, int x, int y, EntityType<?> mobType, int lineHeight, int lineSpacing) {
        boolean killed = ClientProgressCache.hasMobBeenKilled(mobType);
        String checkbox = killed ? "☑" : "☐";
        int color = killed ? 0x55FF55 : 0xFF5555;
        
        Component mobName = mobType.getDescription();
        guiGraphics.drawString(mc.font, checkbox + " " + mobName.getString(), x, y, color, false);
        
        return y + lineHeight + lineSpacing;
    }

    private static int getDeathCountColor(int deathCount) {
        if (deathCount <= 3) {
            return 0x55FF55; // Green
        } else if (deathCount <= 7) {
            return 0xFFFF55; // Yellow
        } else if (deathCount <= 9) {
            return 0xFF8855; // Orange
        } else {
            return 0xFF5555; // Red
        }
    }
}
