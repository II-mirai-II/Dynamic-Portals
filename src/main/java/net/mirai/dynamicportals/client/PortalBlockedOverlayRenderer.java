package net.mirai.dynamicportals.client;

import java.util.List;
import net.mirai.dynamicportals.network.PortalOverlayClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public final class PortalBlockedOverlayRenderer {
    private static final float MAX_WIDTH_RATIO = 0.65F;
    private static final int MIN_TEXT_WIDTH = 120;
    private static final int BACKGROUND_PAD = 6;
    private static final int LINE_SPACING = 2;
    private static final int BASE_Y_DIVISOR = 4;
    private static final int FADE_IN_TICKS = 6;
    private static final int FADE_OUT_TICKS = 10;

    private PortalBlockedOverlayRenderer() {}

    public static void render(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }
        if (minecraft.screen instanceof DynamicPortalsHubScreen) {
            return;
        }

        PortalOverlayClientState.OverlaySnapshot snapshot = PortalOverlayClientState.snapshot();
        if (snapshot == null) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        Font font = minecraft.font;
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int maxTextWidth = Math.max(MIN_TEXT_WIDTH, (int) (screenWidth * MAX_WIDTH_RATIO));
        Component text = Component.translatable(snapshot.translationKey(), snapshot.arg0(), snapshot.arg1());
        List<FormattedCharSequence> lines = font.split(text, maxTextWidth);
        if (lines.isEmpty()) {
            return;
        }

        int maxLineWidth = 0;
        for (FormattedCharSequence line : lines) {
            maxLineWidth = Math.max(maxLineWidth, font.width(line));
        }

        int lineHeight = font.lineHeight + LINE_SPACING;
        int textHeight = lines.size() * lineHeight - LINE_SPACING;
        int boxX = (screenWidth - maxLineWidth) / 2 - BACKGROUND_PAD;
        int boxY = Math.max(8, screenHeight / BASE_Y_DIVISOR - BACKGROUND_PAD);
        int boxWidth = maxLineWidth + BACKGROUND_PAD * 2;
        int boxHeight = textHeight + BACKGROUND_PAD * 2;

        float alpha = computeAlpha(snapshot.totalTicks(), snapshot.remainingTicks());
        int textAlpha = Math.max(80, Math.min(255, (int) (255 * alpha)));
        int bgAlpha = Math.max(0, Math.min(180, (int) (120 * alpha)));

        int backgroundColor = (bgAlpha << 24);
        guiGraphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, backgroundColor);

        int y = boxY + BACKGROUND_PAD;
        int textColor = (textAlpha << 24) | 0x00FFFFFF;
        for (FormattedCharSequence line : lines) {
            int lineWidth = font.width(line);
            int x = (screenWidth - lineWidth) / 2;
            guiGraphics.drawString(font, line, x, y, textColor);
            y += lineHeight;
        }
    }

    private static float computeAlpha(int totalTicks, int remainingTicks) {
        if (totalTicks <= 0 || remainingTicks <= 0) {
            return 0.0F;
        }

        int elapsed = totalTicks - remainingTicks;
        float fadeIn = Math.min(1.0F, elapsed / (float) FADE_IN_TICKS);
        float fadeOut = Math.min(1.0F, remainingTicks / (float) FADE_OUT_TICKS);
        return Math.max(0.0F, Math.min(fadeIn, fadeOut));
    }
}
