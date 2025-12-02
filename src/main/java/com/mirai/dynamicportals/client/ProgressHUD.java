package com.mirai.dynamicportals.client;

import com.mirai.dynamicportals.util.ModConstants;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

import java.util.ArrayList;
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

        // HUD position (top-right corner)
        int hudX = screenWidth - 220;
        int hudY = 10;
        int hudWidth = 210;
        int lineHeight = 12;

        // Background
        int backgroundColor = 0xA0000000; // Semi-transparent black
        guiGraphics.fill(hudX - 5, hudY - 5, hudX + hudWidth, hudY + 400, backgroundColor);

        // Title
        Component title = Component.translatable(ModConstants.HUD_TITLE);
        guiGraphics.drawString(mc.font, title, hudX, hudY, 0xFFFFFF, true);
        hudY += lineHeight + 5;

        // Death counter
        int deathCount = ClientProgressCache.getDeathCount();
        int deathColor = getDeathCountColor(deathCount);
        Component deathText = Component.translatable(ModConstants.HUD_DEATHS, deathCount, ModConstants.DEATH_THRESHOLD);
        guiGraphics.drawString(mc.font, deathText, hudX, hudY, deathColor, true);
        hudY += lineHeight + 5;

        // Phase tabs hint
        Component tabHint = Component.literal("[Tab] Switch Phase");
        guiGraphics.drawString(mc.font, tabHint, hudX, hudY, 0xAAAAAA, false);
        hudY += lineHeight + 3;

        // Current phase display
        if (overworldPhase) {
            renderOverworldPhase(guiGraphics, mc, hudX, hudY, lineHeight);
        } else {
            renderNetherPhase(guiGraphics, mc, hudX, hudY, lineHeight);
        }
    }

    private static void renderOverworldPhase(GuiGraphics guiGraphics, Minecraft mc, int x, int y, int lineHeight) {
        // Phase title
        Component phaseTitle = Component.translatable(ModConstants.HUD_PHASE_OVERWORLD);
        guiGraphics.drawString(mc.font, phaseTitle, x, y, 0xFFFF55, true);
        y += lineHeight + 2;

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
        
        // TODO: Check diamond inventory
        
        Component progressText = Component.translatable(ModConstants.HUD_PROGRESS_OVERWORLD, killed, total);
        guiGraphics.drawString(mc.font, progressText, x, y, 0xFFFFFF, false);
        y += lineHeight;

        // Render mob checklist
        Component mobsHeader = Component.translatable(ModConstants.HUD_REQUIRED_MOBS);
        guiGraphics.drawString(mc.font, mobsHeader, x, y, 0xAAAAFF, false);
        y += lineHeight;

        for (EntityType<?> mob : OVERWORLD_MOBS) {
            y = renderMobEntry(guiGraphics, mc, x + 5, y, mob, lineHeight);
        }

        // Render boss checklist
        y += 3;
        Component bossHeader = Component.translatable(ModConstants.HUD_REQUIRED_BOSSES);
        guiGraphics.drawString(mc.font, bossHeader, x, y, 0xFFAAAA, false);
        y += lineHeight;

        for (EntityType<?> boss : OVERWORLD_BOSSES) {
            y = renderMobEntry(guiGraphics, mc, x + 5, y, boss, lineHeight);
        }

        // Required items
        y += 3;
        Component itemsHeader = Component.translatable(ModConstants.HUD_REQUIRED_ITEMS);
        guiGraphics.drawString(mc.font, itemsHeader, x, y, 0xAAFFAA, false);
        y += lineHeight;

        Component diamondText = Component.literal("Diamond");
        int diamondColor = 0xFF5555; // Red for not obtained
        guiGraphics.drawString(mc.font, "☐ " + diamondText.getString(), x + 5, y, diamondColor, false);
    }

    private static void renderNetherPhase(GuiGraphics guiGraphics, Minecraft mc, int x, int y, int lineHeight) {
        // Phase title
        Component phaseTitle = Component.translatable(ModConstants.HUD_PHASE_NETHER);
        guiGraphics.drawString(mc.font, phaseTitle, x, y, 0xFF5555, true);
        y += lineHeight + 2;

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
        
        Component progressText = Component.translatable(ModConstants.HUD_PROGRESS_NETHER, killed, total);
        guiGraphics.drawString(mc.font, progressText, x, y, 0xFFFFFF, false);
        y += lineHeight;

        // Render mob checklist
        Component mobsHeader = Component.translatable(ModConstants.HUD_REQUIRED_MOBS);
        guiGraphics.drawString(mc.font, mobsHeader, x, y, 0xAAAAFF, false);
        y += lineHeight;

        for (EntityType<?> mob : NETHER_MOBS) {
            y = renderMobEntry(guiGraphics, mc, x + 5, y, mob, lineHeight);
        }

        // Render boss checklist
        y += 3;
        Component bossHeader = Component.translatable(ModConstants.HUD_REQUIRED_BOSSES);
        guiGraphics.drawString(mc.font, bossHeader, x, y, 0xFFAAAA, false);
        y += lineHeight;

        for (EntityType<?> boss : NETHER_BOSSES) {
            y = renderMobEntry(guiGraphics, mc, x + 5, y, boss, lineHeight);
        }

        // Required items
        y += 3;
        Component itemsHeader = Component.translatable(ModConstants.HUD_REQUIRED_ITEMS);
        guiGraphics.drawString(mc.font, itemsHeader, x, y, 0xAAFFAA, false);
        y += lineHeight;

        Component netheriteText = Component.literal("Netherite Ingot");
        int netheriteColor = 0xFF5555; // Red for not obtained
        guiGraphics.drawString(mc.font, "☐ " + netheriteText.getString(), x + 5, y, netheriteColor, false);
    }

    private static int renderMobEntry(GuiGraphics guiGraphics, Minecraft mc, int x, int y, EntityType<?> mobType, int lineHeight) {
        boolean killed = ClientProgressCache.hasMobBeenKilled(mobType);
        String checkbox = killed ? "☑" : "☐";
        int color = killed ? 0x55FF55 : 0xFF5555;
        
        Component mobName = mobType.getDescription();
        guiGraphics.drawString(mc.font, checkbox + " " + mobName.getString(), x, y, color, false);
        
        return y + lineHeight;
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
