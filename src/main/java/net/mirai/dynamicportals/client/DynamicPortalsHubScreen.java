package net.mirai.dynamicportals.client;

import java.util.ArrayList;
import java.util.List;
import net.mirai.dynamicportals.DynamicPortals;
import net.mirai.dynamicportals.network.HubClientState;
import net.mirai.dynamicportals.network.HubNetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class DynamicPortalsHubScreen extends Screen {
    private static final int MAX_PROGRESS_PORTALS_PER_PAGE = 3;
    private static final int CARD_HEIGHT = 64;
    private static final int CARD_GAP = 10;
    private static final int BUTTON_HEIGHT = 20;
    private static final int FIELD_HEIGHT = 20;
    private static final int MIN_MODAL_WIDTH = 280;
    private static final int MAX_MODAL_WIDTH = 420;
    private static final int KILL_HEAD_RENDER_SIZE = 18;
    private static final int KILL_HEAD_SOURCE_SIZE = 32;
    private static final int DETAILS_ROW_HEIGHT = 22;
    private static final float TITLE_SCALE = 1.75F;

    private static final int COLOR_PANEL = 0xAA1A1A1A;
    private static final int COLOR_CARD = 0xC0242424;
    private static final int COLOR_CARD_INNER = 0x662F2F2F;
    private static final int COLOR_BORDER = 0xAA3A3A3A;
    private static final int COLOR_PROGRESS_TRACK = 0xAA111111;
    private static final int COLOR_PROGRESS_EMPTY = 0x553A3A3A;

    private enum HubTab {
        PROGRESS,
        PARTY
    }

    private HubTab activeTab = HubTab.PROGRESS;
    private int progressPage = 0;

    private int detailsPortalIndex = -1;
    private int detailsRequirementsPage = 0;

    private Button progressTabButton;
    private Button partyTabButton;

    private Button progressPrevButton;
    private Button progressNextButton;

    private final Button[] progressDetailButtons = new Button[MAX_PROGRESS_PORTALS_PER_PAGE];
    private Button detailsCloseButton;
    private Button detailsPrevPageButton;
    private Button detailsNextPageButton;

    private EditBox joinCodeBox;

    private Button createPartyButton;
    private Button joinPartyButton;
    private Button leavePartyButton;
    private Button dissolvePartyButton;

    private boolean confirmLeave;
    private boolean confirmDissolve;
    private boolean initialized;

    private int cachedWidth = -1;
    private int cachedHeight = -1;
    private double cachedGuiScale = -1.0D;

    private static final class HubLayout {
        final int centerX;
        final int tabsY;
        final int titleY;
        final int feedbackY;
        final int contentTop;

        final int progressPageY;
        final int progressCardsStartY;
        final int progressCardsBottomY;
        final int progressCardLeft;
        final int progressCardRight;
        final int progressCardWidth;
        final int progressCardsPerPage;
        final int progressNavY;

        final int partyPanelTop;
        final int partyPanelBottom;
        final int partyLeftPanelLeft;
        final int partyLeftPanelRight;
        final int partyRightPanelLeft;
        final int partyRightPanelRight;
        final int partyFormTop;
        final int partyActionsY;

        final int modalLeft;
        final int modalRight;
        final int modalTop;
        final int modalBottom;
        final int modalControlY;

        HubLayout(
            int centerX,
            int tabsY,
            int titleY,
            int feedbackY,
            int contentTop,
            int progressPageY,
            int progressCardsStartY,
            int progressCardsBottomY,
            int progressCardLeft,
            int progressCardRight,
            int progressCardWidth,
            int progressCardsPerPage,
            int progressNavY,
            int partyPanelTop,
            int partyPanelBottom,
            int partyLeftPanelLeft,
            int partyLeftPanelRight,
            int partyRightPanelLeft,
            int partyRightPanelRight,
            int partyFormTop,
            int partyActionsY,
            int modalLeft,
            int modalRight,
            int modalTop,
            int modalBottom,
            int modalControlY
        ) {
            this.centerX = centerX;
            this.tabsY = tabsY;
            this.titleY = titleY;
            this.feedbackY = feedbackY;
            this.contentTop = contentTop;
            this.progressPageY = progressPageY;
            this.progressCardsStartY = progressCardsStartY;
            this.progressCardsBottomY = progressCardsBottomY;
            this.progressCardLeft = progressCardLeft;
            this.progressCardRight = progressCardRight;
            this.progressCardWidth = progressCardWidth;
            this.progressCardsPerPage = progressCardsPerPage;
            this.progressNavY = progressNavY;
            this.partyPanelTop = partyPanelTop;
            this.partyPanelBottom = partyPanelBottom;
            this.partyLeftPanelLeft = partyLeftPanelLeft;
            this.partyLeftPanelRight = partyLeftPanelRight;
            this.partyRightPanelLeft = partyRightPanelLeft;
            this.partyRightPanelRight = partyRightPanelRight;
            this.partyFormTop = partyFormTop;
            this.partyActionsY = partyActionsY;
            this.modalLeft = modalLeft;
            this.modalRight = modalRight;
            this.modalTop = modalTop;
            this.modalBottom = modalBottom;
            this.modalControlY = modalControlY;
        }
    }

    public DynamicPortalsHubScreen() {
        super(Component.translatable("dynamicportals.ui.hub.title"));
    }

    @Override
    protected void init() {
        String joinCodeValue = this.joinCodeBox != null ? this.joinCodeBox.getValue() : "";

        super.init();

        int top = 16;

        this.progressTabButton = this.addRenderableWidget(Button.builder(
            Component.translatable("dynamicportals.ui.tab.progress"),
            button -> switchTab(HubTab.PROGRESS)
        ).bounds(0, top, 148, BUTTON_HEIGHT).build());

        this.partyTabButton = this.addRenderableWidget(Button.builder(
            Component.translatable("dynamicportals.ui.tab.party"),
            button -> switchTab(HubTab.PARTY)
        ).bounds(0, top, 148, BUTTON_HEIGHT).build());

        this.progressPrevButton = this.addRenderableWidget(Button.builder(
            Component.translatable("dynamicportals.ui.button.prev"),
            button -> progressPage = Math.max(0, progressPage - 1)
        ).bounds(0, this.height - 52, 90, BUTTON_HEIGHT).build());

        this.progressNextButton = this.addRenderableWidget(Button.builder(
            Component.translatable("dynamicportals.ui.button.next"),
            button -> progressPage = progressPage + 1
        ).bounds(0, this.height - 52, 90, BUTTON_HEIGHT).build());

        for (int i = 0; i < MAX_PROGRESS_PORTALS_PER_PAGE; i++) {
            final int slot = i;
            this.progressDetailButtons[i] = this.addRenderableWidget(Button.builder(
                Component.translatable("dynamicportals.ui.button.details"),
                button -> openDetailsForSlot(slot)
            ).bounds(0, 0, 80, BUTTON_HEIGHT).build());
        }

        this.detailsCloseButton = this.addRenderableWidget(Button.builder(
            Component.translatable("dynamicportals.ui.button.close"),
            button -> closeDetails()
        ).bounds(0, this.height - 94, 80, BUTTON_HEIGHT).build());

        this.detailsPrevPageButton = this.addRenderableWidget(Button.builder(
            Component.translatable("dynamicportals.ui.button.prev"),
            button -> detailsRequirementsPage = Math.max(0, detailsRequirementsPage - 1)
        ).bounds(0, this.height - 94, 80, BUTTON_HEIGHT).build());

        this.detailsNextPageButton = this.addRenderableWidget(Button.builder(
            Component.translatable("dynamicportals.ui.button.next"),
            button -> detailsRequirementsPage = detailsRequirementsPage + 1
        ).bounds(0, this.height - 94, 80, BUTTON_HEIGHT).build());

        this.joinCodeBox = this.addRenderableWidget(new EditBox(
            this.font,
            0,
            0,
            180,
            FIELD_HEIGHT,
            Component.translatable("dynamicportals.ui.party.join.code")
        ));
        this.joinCodeBox.setMaxLength(64);

        this.createPartyButton = this.addRenderableWidget(Button.builder(
            Component.translatable("dynamicportals.ui.button.create_party"),
            button -> {
                confirmLeave = false;
                confirmDissolve = false;
                HubNetworkHandler.requestPartyCreateFromClient();
            }
        ).bounds(0, 0, 180, BUTTON_HEIGHT).build());

        this.joinPartyButton = this.addRenderableWidget(Button.builder(
            Component.translatable("dynamicportals.ui.button.join_party"),
            button -> {
                confirmLeave = false;
                confirmDissolve = false;
                HubNetworkHandler.requestPartyJoinFromClient(
                    joinCodeBox.getValue().trim()
                );
            }
        ).bounds(0, 0, 180, BUTTON_HEIGHT).build());

        this.leavePartyButton = this.addRenderableWidget(Button.builder(
            Component.translatable("dynamicportals.ui.button.leave_party"),
            button -> {
                if (!confirmLeave) {
                    confirmLeave = true;
                    confirmDissolve = false;
                } else {
                    confirmLeave = false;
                    HubNetworkHandler.requestPartyLeaveFromClient();
                }
            }
        ).bounds(0, this.height - 78, 180, BUTTON_HEIGHT).build());

        this.dissolvePartyButton = this.addRenderableWidget(Button.builder(
            Component.translatable("dynamicportals.ui.button.dissolve_party"),
            button -> {
                if (!confirmDissolve) {
                    confirmDissolve = true;
                    confirmLeave = false;
                } else {
                    confirmDissolve = false;
                    HubNetworkHandler.requestPartyDissolveFromClient();
                }
            }
        ).bounds(0, this.height - 78, 180, BUTTON_HEIGHT).build());

        this.joinCodeBox.setValue(joinCodeValue);

        applyLayoutToWidgets();
        cacheViewportState();

        CompoundTag state = HubClientState.snapshot();
        updateWidgetVisibility(state);
        updateButtons(state);

        if (!this.initialized) {
            this.initialized = true;
            HubNetworkHandler.requestRefreshFromClient();
        }
    }

    @Override
    public void onClose() {
        closeDetails();
        super.onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && isDetailsOpen()) {
            closeDetails();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void switchTab(HubTab tab) {
        this.activeTab = tab;
        this.confirmLeave = false;
        this.confirmDissolve = false;
        closeDetails();
        updateWidgetVisibility(HubClientState.snapshot());
        HubNetworkHandler.requestRefreshFromClient();
    }

    @Override
    public void tick() {
        super.tick();

        if (viewportChanged()) {
            applyLayoutToWidgets();
            cacheViewportState();
        }

        CompoundTag state = HubClientState.snapshot();
        updateWidgetVisibility(state);
        updateButtons(state);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(0, 0, this.width, this.height, 0xCC0E0E0E);

        super.render(graphics, mouseX, mouseY, partialTick);

        HubLayout layout = computeLayout();
        CompoundTag state = HubClientState.snapshot();

        if (isDetailsOpen()) {
            renderProgressDetailsModal(graphics, state, layout);
            renderDetailsWidgets(graphics, mouseX, mouseY, partialTick);
            return;
        }

        renderMainTitle(graphics, layout);
        renderFeedback(graphics, state, layout);

        if (this.activeTab == HubTab.PROGRESS) {
            renderProgressTab(graphics, state, layout);
        } else {
            renderPartyTab(graphics, state, layout);
        }

        renderMainWidgets(graphics, mouseX, mouseY, partialTick);
    }

    private void renderMainTitle(GuiGraphics graphics, HubLayout layout) {
        drawScaledCenteredString(graphics, this.title, layout.centerX, layout.titleY, TITLE_SCALE, 0xFFFFFFFF);
        int lineY = layout.titleY + Math.round(this.font.lineHeight * TITLE_SCALE) + 6;
        int lineWidth = Math.min(190, Math.max(96, this.width - 48));
        graphics.hLine(layout.centerX - lineWidth / 2, layout.centerX + lineWidth / 2, lineY, 0x9955FFFF);
        graphics.hLine(layout.centerX - lineWidth / 4, layout.centerX + lineWidth / 4, lineY + 2, 0x6655FF55);
    }

    private void renderFeedback(GuiGraphics graphics, CompoundTag state, HubLayout layout) {
        String message = state.getString("message");
        if (message == null || message.isBlank()) {
            return;
        }

        int color = state.getBoolean("success") ? 0xFF55FF55 : 0xFFFF5555;
        graphics.drawCenteredString(this.font, message, layout.centerX, layout.feedbackY, color);
    }

    private void renderProgressTab(GuiGraphics graphics, CompoundTag state, HubLayout layout) {
        CompoundTag progress = state.getCompound("progress");
        ListTag portals = progress.getList("portals", Tag.TAG_COMPOUND);

        if (portals.isEmpty()) {
            graphics.drawCenteredString(
                this.font,
                Component.translatable("dynamicportals.ui.progress.empty"),
                layout.centerX,
                layout.progressCardsStartY,
                0xFFAAAAAA
            );
            return;
        }

        int totalPages = getProgressPages(portals, layout.progressCardsPerPage);
        this.progressPage = Math.max(0, Math.min(this.progressPage, totalPages - 1));

        int start = this.progressPage * layout.progressCardsPerPage;
        int end = Math.min(portals.size(), start + layout.progressCardsPerPage);

        boolean showPageLabel = !isDetailsOpen() && totalPages > 1;
        if (showPageLabel) {
            graphics.drawCenteredString(
                this.font,
                Component.translatable("dynamicportals.ui.progress.page", this.progressPage + 1, totalPages),
                layout.centerX,
                layout.progressPageY,
                0xFFAAAAAA
            );
            graphics.hLine(layout.progressCardLeft, layout.progressCardRight - 1, layout.progressPageY + this.font.lineHeight + 2, 0x553A3A3A);
        }

        int slot = 0;
        for (int i = start; i < end; i++) {
            CompoundTag portal = portals.getCompound(i);
            int cardTop = layout.progressCardsStartY + (slot * (CARD_HEIGHT + CARD_GAP));
            int cardLeft = layout.progressCardLeft;
            int cardRight = layout.progressCardRight;
            int cardBottom = cardTop + CARD_HEIGHT;

            drawPanel(graphics, cardLeft, cardTop, cardRight, cardBottom, COLOR_CARD, COLOR_BORDER);
            graphics.fill(cardLeft + 1, cardTop + 1, cardLeft + 4, cardBottom - 1, portal.getInt("status_color"));
            graphics.fill(cardLeft + 6, cardTop + 5, cardRight - 6, cardBottom - 5, COLOR_CARD_INNER);

            int detailsReserve = Math.max(82, layout.progressCardWidth / 4) + 10;
            int textX = cardLeft + 12;
            int textMaxWidth = Math.max(40, layout.progressCardWidth - detailsReserve - 20);
            int titleCenterX = textX + (textMaxWidth / 2);
            int textY = cardTop + 11;

            Component portalLine = Component.literal(portalCardTitle(portal));
            drawCenteredEllipsizedString(graphics, portalLine, titleCenterX, textY, textMaxWidth, portal.getInt("status_color"));
            textY += this.font.lineHeight + 4;

            Component summaryLine = Component.translatable(
                "dynamicportals.command.check.compact_summary",
                portal.getInt("completed"),
                portal.getInt("total"),
                portal.getInt("percent"),
                portal.getInt("missing_count")
            );
            drawEllipsizedString(graphics, summaryLine, textX, textY, textMaxWidth, 0xFFB0B0B0);
            textY += this.font.lineHeight + 3;

            drawProgressBar(
                graphics,
                textX,
                textY,
                textX + textMaxWidth,
                textY + 5,
                portal.getInt("percent"),
                portal.getInt("status_color")
            );
            textY += 7;

            String preview = portal.getString("missing_preview");
            Component previewLine = preview.isBlank()
                ? Component.translatable("dynamicportals.command.check.compact_clear")
                : Component.translatable("dynamicportals.command.check.compact_missing", preview);
            drawEllipsizedString(graphics, previewLine, textX, textY, textMaxWidth, preview.isBlank() ? 0xFF66CC66 : 0xFFFFFF55);

            slot++;
        }
    }

    private void renderPartyTab(GuiGraphics graphics, CompoundTag state, HubLayout layout) {
        CompoundTag party = state.getCompound("party");
        boolean inParty = party.getBoolean("in_party");

        if (!inParty) {
            int panelLeft = layout.partyLeftPanelLeft;
            int panelRight = layout.partyRightPanelRight;
            int textMaxWidth = panelRight - panelLeft - 24;

            drawPanel(graphics, panelLeft, layout.partyPanelTop, panelRight, layout.partyPanelBottom, COLOR_PANEL, COLOR_BORDER);
            graphics.fill(panelLeft + 1, layout.partyPanelTop + 1, panelRight - 1, layout.partyPanelTop + 4, 0xFF55FFFF);
            int subtitleY = Math.max(layout.partyPanelTop + 42, layout.partyFormTop - 36);
            int hintY = subtitleY + this.font.lineHeight + 5;
            int dividerY = hintY + this.font.lineHeight + 8;
            graphics.fill(panelLeft + 8, dividerY, panelRight - 8, dividerY + 1, 0x553A3A3A);

            drawScaledCenteredString(
                graphics,
                Component.translatable("dynamicportals.ui.party.home.title"),
                layout.centerX,
                layout.partyPanelTop + 10,
                1.25F,
                0xFFFFFFFF
            );
            drawCenteredEllipsizedString(
                graphics,
                Component.translatable("dynamicportals.ui.party.home.subtitle"),
                layout.centerX,
                subtitleY,
                textMaxWidth,
                0xFFB0B0B0
            );

            graphics.drawCenteredString(
                this.font,
                Component.translatable("dynamicportals.ui.party.online_hint"),
                layout.centerX,
                hintY,
                0xFFAAFFFF
            );
            return;
        }

        int dashboardLeft = layout.partyLeftPanelLeft;
        int dashboardRight = layout.partyRightPanelRight;
        int panelWidth = dashboardRight - dashboardLeft;
        int textMaxWidth = panelWidth - 20;
        int lineStep = this.font.lineHeight + 2;

        drawPanel(graphics, dashboardLeft, layout.partyPanelTop, dashboardRight, layout.partyPanelBottom, COLOR_PANEL, COLOR_BORDER);
        graphics.fill(dashboardLeft + 1, layout.partyPanelTop + 1, dashboardRight - 1, layout.partyPanelTop + 26, 0xAA262626);
        graphics.fill(dashboardLeft + 1, layout.partyPanelTop + 1, dashboardRight - 1, layout.partyPanelTop + 4, 0xFF5555FF);

        String code = party.getString("short_code");
        int memberCount = party.getInt("member_count");
        boolean isCreator = party.getBoolean("is_creator");

        int y = layout.partyPanelTop + 8;
        graphics.drawCenteredString(this.font, Component.translatable("dynamicportals.ui.party.info.header"), layout.centerX, y, 0xFFFFFFFF);
        y += lineStep + 6;
        drawCenteredEllipsizedString(graphics, Component.translatable("dynamicportals.ui.party.dashboard.subtitle"), layout.centerX, y, textMaxWidth, 0xFFB0B0B0);
        y += lineStep + 6;

        int codeBoxTop = y;
        int codeBoxBottom = codeBoxTop + 34;
        drawPanel(graphics, dashboardLeft + 36, codeBoxTop, dashboardRight - 36, codeBoxBottom, 0x66303030, 0x8855FFFF);
        graphics.drawCenteredString(this.font, Component.translatable("dynamicportals.ui.party.invite_code"), layout.centerX, codeBoxTop + 5, 0xFFAAFFFF);
        drawScaledCenteredString(graphics, Component.literal(code.isBlank() ? "-----" : code), layout.centerX, codeBoxTop + 16, 1.35F, 0xFFFFFFFF);
        y = codeBoxBottom + 7;

        drawEllipsizedString(graphics, Component.translatable("dynamicportals.ui.party.info.members", memberCount), dashboardLeft + 10, y, textMaxWidth, 0xFFFFFFAA);
        y += lineStep;

        Component creatorText = isCreator
            ? Component.translatable("dynamicportals.ui.party.info.creator.you")
            : Component.translatable("dynamicportals.ui.party.info.creator.other");
        int wrappedLines = Math.max(1, this.font.split(creatorText, textMaxWidth).size());
        graphics.drawWordWrap(this.font, creatorText, dashboardLeft + 10, y, textMaxWidth, 0xFFB0B0B0);
        y += wrappedLines * lineStep;
        y += 5;

        int memberPanelTop = y - 2;
        int memberPanelBottom = layout.partyPanelBottom - 6;
        drawPanel(graphics, dashboardLeft + 7, memberPanelTop, dashboardRight - 7, memberPanelBottom, 0x55262626, 0x553A3A3A);
        drawEllipsizedString(graphics, Component.translatable("dynamicportals.ui.party.members.header"), dashboardLeft + 14, y, textMaxWidth - 14, 0xFFFFFFFF);
        y += lineStep;

        ListTag members = party.getList("members", Tag.TAG_COMPOUND);
        int maxMemberLines = Math.max(1, (memberPanelBottom - y - 4) / lineStep);
        int shownMembers = Math.min(members.size(), maxMemberLines);

        for (int i = 0; i < shownMembers; i++) {
            CompoundTag member = members.getCompound(i);
            List<Component> badges = new ArrayList<>();
            if (member.getBoolean("is_self")) {
                badges.add(Component.translatable("dynamicportals.ui.party.badge.self"));
            }
            if (member.getBoolean("is_creator")) {
                badges.add(Component.translatable("dynamicportals.ui.party.badge.creator"));
            }

            String suffix = badges.isEmpty() ? "" : " [" + badgesToString(badges) + "]";
            String online = member.getBoolean("online")
                ? Component.translatable("dynamicportals.ui.party.member.online").getString()
                : Component.translatable("dynamicportals.ui.party.member.offline").getString();

            int color = member.getBoolean("online") ? 0xFF80FF80 : 0xFFAAAAAA;
            String line = member.getString("name") + " - " + online + suffix;
            graphics.drawString(this.font, ellipsize(line, textMaxWidth - 16), dashboardLeft + 14, y, color, false);
            y += lineStep;
        }

        int hiddenMembers = members.size() - shownMembers;
        if (hiddenMembers > 0 && y <= memberPanelBottom - this.font.lineHeight) {
            drawEllipsizedString(
                graphics,
                Component.translatable("dynamicportals.ui.party.members.more", hiddenMembers),
                dashboardLeft + 14,
                y,
                textMaxWidth - 16,
                0xFFB0B0B0
            );
        }

        int confirmY = Math.max(layout.partyActionsY - this.font.lineHeight - 4, layout.partyPanelBottom + 2);
        if (confirmLeave) {
            graphics.drawCenteredString(this.font, Component.translatable("dynamicportals.ui.party.confirm.leave"), layout.centerX, confirmY, 0xFFFFDD66);
        }

        if (confirmDissolve) {
            graphics.drawCenteredString(this.font, Component.translatable("dynamicportals.ui.party.confirm.dissolve"), layout.centerX, confirmY, 0xFFFF8888);
        }
    }

    private void renderProgressDetailsModal(GuiGraphics graphics, CompoundTag state, HubLayout layout) {
        CompoundTag progress = state.getCompound("progress");
        ListTag portals = progress.getList("portals", Tag.TAG_COMPOUND);
        if (detailsPortalIndex < 0 || detailsPortalIndex >= portals.size()) {
            closeDetails();
            return;
        }

        CompoundTag portal = portals.getCompound(detailsPortalIndex);
        ListTag requirements = portal.getList("requirement_entries", Tag.TAG_COMPOUND);
        List<CompoundTag> sortedRequirements = sortedRequirements(requirements);

        graphics.fill(0, 0, this.width, this.height, 0xEE101010);
        drawPanel(graphics, layout.modalLeft, layout.modalTop, layout.modalRight, layout.modalBottom, 0xDD202020, 0xFF5A5A5A);
        graphics.fill(layout.modalLeft + 1, layout.modalTop + 1, layout.modalRight - 1, layout.modalTop + 4, portal.getInt("status_color"));

        graphics.drawCenteredString(
            this.font,
            Component.translatable("dynamicportals.ui.progress.details.title", portal.getString("display_name")),
            layout.centerX,
            layout.modalTop + 8,
            0xFFFFFFFF
        );
        graphics.hLine(layout.modalLeft, layout.modalRight - 1, layout.modalTop + this.font.lineHeight + 12, 0x553A3A3A);

        int textMaxWidth = layout.modalRight - layout.modalLeft - 20;
        int lineY = layout.modalTop + 24;

        boolean hasBypassBanner = portal.getBoolean("bypass_banner");
        if (hasBypassBanner) {
            drawEllipsizedString(
                graphics,
                Component.translatable(
                    "dynamicportals.command.check.bypass_banner",
                    portal.getString("display_name"),
                    portal.getString("dimension")
                ),
                layout.modalLeft + 10,
                lineY,
                textMaxWidth,
                0xFF55FFFF
            );
            lineY += this.font.lineHeight + 5;
        }

        if (sortedRequirements.isEmpty()) {
            graphics.drawCenteredString(
                this.font,
                Component.translatable("dynamicportals.ui.progress.details.empty"),
                layout.centerX,
                lineY + 6,
                0xFFAAAAAA
            );
            return;
        }

        int requirementsPerPage = getDetailsRequirementsPerPage(layout, hasBypassBanner);
        int totalPages = Math.max(1, (int) Math.ceil(sortedRequirements.size() / (double) requirementsPerPage));
        this.detailsRequirementsPage = Math.max(0, Math.min(this.detailsRequirementsPage, totalPages - 1));

        int start = this.detailsRequirementsPage * requirementsPerPage;
        int end = Math.min(sortedRequirements.size(), start + requirementsPerPage);

        int y = lineY;
        for (int i = start; i < end; i++) {
            CompoundTag requirement = sortedRequirements.get(i);
            if (isKillRequirement(requirement)) {
                renderKillRequirementLine(graphics, requirement, layout.modalLeft + 10, layout.modalRight - 10, y);
            } else {
                renderTextRequirementLine(graphics, requirement, layout.modalLeft + 10, layout.modalRight - 10, y);
            }
            y += DETAILS_ROW_HEIGHT;
        }

        graphics.drawCenteredString(
            this.font,
            Component.literal((this.detailsRequirementsPage + 1) + "/" + totalPages),
            layout.centerX,
            layout.modalBottom - 18,
            0xFFAAAAAA
        );
    }

    private static String badgesToString(List<Component> badges) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < badges.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(badges.get(i).getString());
        }
        return builder.toString();
    }

    private void drawPanel(GuiGraphics graphics, int left, int top, int right, int bottom, int fillColor, int borderColor) {
        graphics.fill(left, top, right, bottom, fillColor);
        graphics.hLine(left, right - 1, top, borderColor);
        graphics.hLine(left, right - 1, bottom - 1, borderColor);
        graphics.vLine(left, top, bottom - 1, borderColor);
        graphics.vLine(right - 1, top, bottom - 1, borderColor);
    }

    private void drawScaledCenteredString(GuiGraphics graphics, Component text, int centerX, int y, float scale, int color) {
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0F);
        int scaledCenterX = Math.round(centerX / scale);
        int scaledY = Math.round(y / scale);
        graphics.drawCenteredString(this.font, text, scaledCenterX, scaledY + 1, 0xAA000000);
        graphics.drawCenteredString(this.font, text, scaledCenterX, scaledY, color);
        graphics.pose().popPose();
    }

    private void renderMainWidgets(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderWidget(graphics, this.progressTabButton, mouseX, mouseY, partialTick);
        renderWidget(graphics, this.partyTabButton, mouseX, mouseY, partialTick);
        renderWidget(graphics, this.progressPrevButton, mouseX, mouseY, partialTick);
        renderWidget(graphics, this.progressNextButton, mouseX, mouseY, partialTick);

        for (Button detailsButton : this.progressDetailButtons) {
            renderWidget(graphics, detailsButton, mouseX, mouseY, partialTick);
        }

        renderWidget(graphics, this.joinCodeBox, mouseX, mouseY, partialTick);
        renderWidget(graphics, this.createPartyButton, mouseX, mouseY, partialTick);
        renderWidget(graphics, this.joinPartyButton, mouseX, mouseY, partialTick);
        renderWidget(graphics, this.leavePartyButton, mouseX, mouseY, partialTick);
        renderWidget(graphics, this.dissolvePartyButton, mouseX, mouseY, partialTick);
    }

    private void renderDetailsWidgets(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderWidget(graphics, this.detailsCloseButton, mouseX, mouseY, partialTick);
        renderWidget(graphics, this.detailsPrevPageButton, mouseX, mouseY, partialTick);
        renderWidget(graphics, this.detailsNextPageButton, mouseX, mouseY, partialTick);
    }

    private void renderWidget(GuiGraphics graphics, AbstractWidget widget, int mouseX, int mouseY, float partialTick) {
        if (widget != null && widget.visible) {
            widget.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    private void drawProgressBar(GuiGraphics graphics, int left, int top, int right, int bottom, int percent, int fillColor) {
        int clampedPercent = clampInt(percent, 0, 100);
        int width = Math.max(1, right - left);
        int filledWidth = Math.max(0, Math.min(width, (int) Math.floor(width * (clampedPercent / 100.0D))));

        graphics.fill(left, top, right, bottom, COLOR_PROGRESS_TRACK);
        if (filledWidth > 0) {
            graphics.fill(left, top, left + filledWidth, bottom, fillColor);
        }
        if (filledWidth < width) {
            graphics.fill(left + filledWidth, top, right, bottom, COLOR_PROGRESS_EMPTY);
        }
    }

    private void renderKillRequirementLine(GuiGraphics graphics, CompoundTag requirement, int left, int right, int y) {
        boolean completed = requirement.getBoolean("completed");
        int rowColor = completed ? 0x332F4F2F : 0x333F3518;
        int borderColor = completed ? 0x664FAF4F : 0x66C6A63C;
        graphics.fill(left, y - 2, right, y + DETAILS_ROW_HEIGHT - 3, rowColor);
        graphics.hLine(left, right - 1, y - 2, borderColor);

        int headX = left + 3;
        int headY = y;
        ResourceLocation texture = getMobHeadTexture(requirement.getString("target_id"));
        if (texture != null) {
            renderMobHeadTexture(graphics, texture, headX, headY);
        } else {
            drawMobHeadFallback(graphics, headX, headY, requirement.getString("target_name"));
        }

        String counter = requirement.getInt("current") + "/" + requirement.getInt("required");
        int counterWidth = this.font.width(counter);
        int counterX = right - counterWidth - 6;
        int nameX = headX + KILL_HEAD_RENDER_SIZE + 6;
        int nameMaxWidth = Math.max(20, counterX - nameX - 8);
        int textY = y + ((KILL_HEAD_RENDER_SIZE - this.font.lineHeight) / 2);
        int nameColor = completed ? 0xFF88CC88 : 0xFFFFE08A;
        int counterColor = completed ? 0xFF66CC66 : 0xFFFFFF66;

        graphics.drawString(this.font, ellipsize(requirement.getString("target_name"), nameMaxWidth), nameX, textY, nameColor, false);
        graphics.drawString(this.font, counter, counterX, textY, counterColor, false);
    }

    private void renderMobHeadTexture(GuiGraphics graphics, ResourceLocation texture, int x, int y) {
        graphics.blit(
            texture,
            x,
            y,
            KILL_HEAD_RENDER_SIZE,
            KILL_HEAD_RENDER_SIZE,
            0.0F,
            0.0F,
            KILL_HEAD_SOURCE_SIZE,
            KILL_HEAD_SOURCE_SIZE,
            KILL_HEAD_SOURCE_SIZE,
            KILL_HEAD_SOURCE_SIZE
        );
    }

    private void renderTextRequirementLine(GuiGraphics graphics, CompoundTag requirement, int left, int right, int y) {
        boolean completed = requirement.getBoolean("completed");
        int rowColor = completed ? 0x222F4F2F : 0x223F3518;
        graphics.fill(left, y - 2, right, y + DETAILS_ROW_HEIGHT - 3, rowColor);

        Component line = Component.translatable(
            "dynamicportals.command.check.requirement_line",
            Component.translatable(requirement.getString("type_key")),
            requirement.getString("target_name"),
            requirement.getInt("current"),
            requirement.getInt("required")
        );
        int color = completed ? 0xFF88CC88 : 0xFFFFFF66;
        drawEllipsizedString(graphics, line, left + 4, y + 4, right - left - 8, color);
    }

    private void drawMobHeadFallback(GuiGraphics graphics, int x, int y, String targetName) {
        graphics.fill(x, y, x + KILL_HEAD_RENDER_SIZE, y + KILL_HEAD_RENDER_SIZE, 0xFF181818);
        graphics.hLine(x, x + KILL_HEAD_RENDER_SIZE - 1, y, 0xFF555555);
        graphics.hLine(x, x + KILL_HEAD_RENDER_SIZE - 1, y + KILL_HEAD_RENDER_SIZE - 1, 0xFF555555);
        graphics.vLine(x, y, y + KILL_HEAD_RENDER_SIZE - 1, 0xFF555555);
        graphics.vLine(x + KILL_HEAD_RENDER_SIZE - 1, y, y + KILL_HEAD_RENDER_SIZE - 1, 0xFF555555);

        String initial = "?";
        if (targetName != null && !targetName.isBlank()) {
            initial = targetName.substring(0, 1).toUpperCase(java.util.Locale.ROOT);
        }
        int initialX = x + (KILL_HEAD_RENDER_SIZE - this.font.width(initial)) / 2;
        int initialY = y + (KILL_HEAD_RENDER_SIZE - this.font.lineHeight) / 2;
        graphics.drawString(this.font, initial, initialX, initialY, 0xFFAAAAAA, false);
    }

    private ResourceLocation getMobHeadTexture(String targetId) {
        ResourceLocation target = ResourceLocation.tryParse(targetId);
        if (target == null || this.minecraft == null) {
            return null;
        }

        String texturePath = "textures/gui/mob_heads/" + target.getNamespace() + "/" + target.getPath() + ".png";
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(DynamicPortals.MOD_ID, texturePath);
        return this.minecraft.getResourceManager().getResource(texture).isPresent() ? texture : null;
    }

    private static boolean isKillRequirement(CompoundTag requirement) {
        return "dynamicportals.type.kill".equals(requirement.getString("type_key"));
    }

    private static String portalCardTitle(CompoundTag portal) {
        String displayName = portal.getString("display_name");
        if (displayName == null || displayName.isBlank()) {
            displayName = portal.getString("dimension");
        }
        return displayName.toUpperCase(java.util.Locale.ROOT) + " PORTAL ACCESS";
    }

    private static List<CompoundTag> sortedRequirements(ListTag requirements) {
        List<CompoundTag> sorted = new ArrayList<>();
        for (int i = 0; i < requirements.size(); i++) {
            CompoundTag requirement = requirements.getCompound(i);
            if (!requirement.getBoolean("completed")) {
                sorted.add(requirement);
            }
        }
        for (int i = 0; i < requirements.size(); i++) {
            CompoundTag requirement = requirements.getCompound(i);
            if (requirement.getBoolean("completed")) {
                sorted.add(requirement);
            }
        }
        return sorted;
    }

    private void drawEllipsizedString(GuiGraphics graphics, Component text, int x, int y, int maxWidth, int color) {
        graphics.drawString(this.font, ellipsize(text.getString(), maxWidth), x, y, color, false);
    }

    private void drawCenteredEllipsizedString(GuiGraphics graphics, Component text, int centerX, int y, int maxWidth, int color) {
        String line = ellipsize(text.getString(), maxWidth);
        graphics.drawString(this.font, line, centerX - (this.font.width(line) / 2), y, color, false);
    }

    private String ellipsize(String text, int maxWidth) {
        if (maxWidth <= 0) {
            return "";
        }
        if (this.font.width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        int ellipsisWidth = this.font.width(ellipsis);
        if (ellipsisWidth >= maxWidth) {
            return ellipsis;
        }

        int end = text.length();
        while (end > 0) {
            String candidate = text.substring(0, end) + ellipsis;
            if (this.font.width(candidate) <= maxWidth) {
                return candidate;
            }
            end--;
        }
        return ellipsis;
    }

    private void updateButtons(CompoundTag state) {
        this.progressTabButton.active = this.activeTab != HubTab.PROGRESS;
        this.partyTabButton.active = this.activeTab != HubTab.PARTY;

        HubLayout layout = computeLayout();

        CompoundTag progress = state.getCompound("progress");
        ListTag portals = progress.getList("portals", Tag.TAG_COMPOUND);

        int totalPages = getProgressPages(portals, layout.progressCardsPerPage);
        this.progressPage = Math.max(0, Math.min(this.progressPage, totalPages - 1));

        boolean detailsOpen = isDetailsOpen();
        boolean showProgressPrev = this.activeTab == HubTab.PROGRESS && !detailsOpen && this.progressPage > 0;
        boolean showProgressNext = this.activeTab == HubTab.PROGRESS && !detailsOpen && this.progressPage < totalPages - 1;
        this.progressPrevButton.active = showProgressPrev;
        this.progressNextButton.active = showProgressNext;

        for (int slot = 0; slot < MAX_PROGRESS_PORTALS_PER_PAGE; slot++) {
            int globalIndex = getPortalGlobalIndexForSlot(slot, layout.progressCardsPerPage);
            Button detailsButton = this.progressDetailButtons[slot];
            detailsButton.active = !detailsOpen
                && slot < layout.progressCardsPerPage
                && globalIndex >= 0
                && globalIndex < portals.size();
        }

        if (detailsOpen && detailsPortalIndex >= 0 && detailsPortalIndex < portals.size()) {
            CompoundTag portal = portals.getCompound(detailsPortalIndex);
            ListTag requirements = portal.getList("requirement_entries", Tag.TAG_COMPOUND);
            int requirementsPerPage = getDetailsRequirementsPerPage(layout, portal.getBoolean("bypass_banner"));
            int requirementPages = Math.max(1, (int) Math.ceil(requirements.size() / (double) requirementsPerPage));
            this.detailsRequirementsPage = Math.max(0, Math.min(this.detailsRequirementsPage, requirementPages - 1));
            this.detailsPrevPageButton.active = this.detailsRequirementsPage > 0;
            this.detailsNextPageButton.active = this.detailsRequirementsPage < requirementPages - 1;
        } else {
            this.detailsPrevPageButton.active = false;
            this.detailsNextPageButton.active = false;
        }

        this.createPartyButton.active = true;
        this.joinPartyButton.active = !this.joinCodeBox.getValue().trim().isEmpty();

        CompoundTag party = state.getCompound("party");
        boolean inParty = party.getBoolean("in_party");
        this.leavePartyButton.active = inParty;
        this.dissolvePartyButton.active = inParty && party.getBoolean("is_creator");

        this.leavePartyButton.setMessage(confirmLeave
            ? Component.translatable("dynamicportals.ui.button.leave_party_confirm")
            : Component.translatable("dynamicportals.ui.button.leave_party"));

        this.dissolvePartyButton.setMessage(confirmDissolve
            ? Component.translatable("dynamicportals.ui.button.dissolve_party_confirm")
            : Component.translatable("dynamicportals.ui.button.dissolve_party"));
    }

    private void updateWidgetVisibility(CompoundTag state) {
        HubLayout layout = computeLayout();
        boolean progressTab = this.activeTab == HubTab.PROGRESS;
        boolean detailsOpen = isDetailsOpen();

        this.progressTabButton.visible = !detailsOpen;
        this.partyTabButton.visible = !detailsOpen;

        CompoundTag progress = state.getCompound("progress");
        ListTag portals = progress.getList("portals", Tag.TAG_COMPOUND);
        int totalPages = getProgressPages(portals, layout.progressCardsPerPage);
        this.progressPage = Math.max(0, Math.min(this.progressPage, totalPages - 1));
        int currentPageStart = this.progressPage * layout.progressCardsPerPage;
        int visibleProgressCards = Math.max(0, Math.min(layout.progressCardsPerPage, portals.size() - currentPageStart));

        this.progressPrevButton.visible = progressTab && !detailsOpen && this.progressPage > 0;
        this.progressNextButton.visible = progressTab && !detailsOpen && this.progressPage < totalPages - 1;

        for (int i = 0; i < this.progressDetailButtons.length; i++) {
            this.progressDetailButtons[i].visible = progressTab && !detailsOpen && i < visibleProgressCards;
        }

        this.detailsCloseButton.visible = progressTab && detailsOpen;
        boolean showDetailsPrev = false;
        boolean showDetailsNext = false;
        if (progressTab && detailsOpen && detailsPortalIndex >= 0 && detailsPortalIndex < portals.size()) {
            CompoundTag portal = portals.getCompound(detailsPortalIndex);
            ListTag requirements = portal.getList("requirement_entries", Tag.TAG_COMPOUND);
            int requirementsPerPage = getDetailsRequirementsPerPage(layout, portal.getBoolean("bypass_banner"));
            int requirementPages = Math.max(1, (int) Math.ceil(requirements.size() / (double) requirementsPerPage));
            showDetailsPrev = this.detailsRequirementsPage > 0;
            showDetailsNext = this.detailsRequirementsPage < requirementPages - 1;
        }
        this.detailsPrevPageButton.visible = showDetailsPrev;
        this.detailsNextPageButton.visible = showDetailsNext;
        this.detailsPrevPageButton.active = showDetailsPrev;
        this.detailsNextPageButton.active = showDetailsNext;

        CompoundTag party = state.getCompound("party");
        boolean inParty = party.getBoolean("in_party");
        boolean partyTab = this.activeTab == HubTab.PARTY;

        boolean showPartyForms = partyTab && !inParty;
        this.joinCodeBox.visible = showPartyForms;
        this.createPartyButton.visible = showPartyForms;
        this.joinPartyButton.visible = showPartyForms;

        boolean showPartyActions = partyTab && inParty;
        boolean isCreator = party.getBoolean("is_creator");
        this.leavePartyButton.visible = showPartyActions;
        this.dissolvePartyButton.visible = showPartyActions && isCreator;
        if (showPartyActions) {
            int panelWidth = layout.partyRightPanelRight - layout.partyLeftPanelLeft;
            if (isCreator) {
                int partyActionGap = 12;
                int actionWidth = Math.max(110, Math.min(170, (panelWidth - partyActionGap) / 2));
                int actionsLeft = layout.centerX - ((actionWidth * 2 + partyActionGap) / 2);
                this.leavePartyButton.setWidth(actionWidth);
                this.leavePartyButton.setX(actionsLeft);
                this.dissolvePartyButton.setWidth(actionWidth);
                this.dissolvePartyButton.setX(actionsLeft + actionWidth + partyActionGap);
            } else {
                int centeredWidth = Math.max(140, Math.min(190, panelWidth - 48));
                this.leavePartyButton.setWidth(centeredWidth);
                this.leavePartyButton.setX(layout.centerX - (centeredWidth / 2));
            }
        }
    }

    private void applyLayoutToWidgets() {
        if (this.progressTabButton == null || this.partyTabButton == null) {
            return;
        }

        HubLayout layout = computeLayout();

        int navWidth = Math.max(70, Math.min(92, (layout.progressCardWidth - 12) / 4));
        int tabGap = 8;
        int actionGap = Math.max(18, Math.min(34, this.width / 18));
        int tabWidth = Math.max(88, Math.min(126, (layout.progressCardWidth - (navWidth * 2) - actionGap - 24) / 2));
        int controlsWidth = (navWidth * 2) + tabGap + actionGap + (tabWidth * 2) + tabGap;
        int controlsLeft = layout.centerX - controlsWidth / 2;

        this.progressPrevButton.setWidth(navWidth);
        this.progressNextButton.setWidth(navWidth);
        this.progressPrevButton.setX(controlsLeft);
        this.progressPrevButton.setY(layout.progressNavY);
        this.progressNextButton.setX(controlsLeft + navWidth + tabGap);
        this.progressNextButton.setY(layout.progressNavY);

        this.progressTabButton.setWidth(tabWidth);
        this.partyTabButton.setWidth(tabWidth);
        this.progressTabButton.setX(controlsLeft + (navWidth * 2) + tabGap + actionGap);
        this.progressTabButton.setY(layout.tabsY);
        this.partyTabButton.setX(this.progressTabButton.getX() + tabWidth + tabGap);
        this.partyTabButton.setY(layout.tabsY);

        int detailsWidth = Math.max(66, Math.min(86, layout.progressCardWidth / 4));
        for (int i = 0; i < MAX_PROGRESS_PORTALS_PER_PAGE; i++) {
            int cardTop = layout.progressCardsStartY + (i * (CARD_HEIGHT + CARD_GAP));
            this.progressDetailButtons[i].setWidth(detailsWidth);
            this.progressDetailButtons[i].setX(layout.progressCardRight - detailsWidth - 8);
            this.progressDetailButtons[i].setY(cardTop + ((CARD_HEIGHT - BUTTON_HEIGHT) / 2));
        }

        int detailsControlWidth = 80;
        this.detailsCloseButton.setWidth(detailsControlWidth);
        this.detailsPrevPageButton.setWidth(detailsControlWidth);
        this.detailsNextPageButton.setWidth(detailsControlWidth);
        this.detailsCloseButton.setX(layout.modalRight - detailsControlWidth);
        this.detailsCloseButton.setY(layout.modalControlY);
        this.detailsPrevPageButton.setX(layout.modalLeft);
        this.detailsPrevPageButton.setY(layout.modalControlY);
        this.detailsNextPageButton.setX(layout.modalLeft + detailsControlWidth + 8);
        this.detailsNextPageButton.setY(layout.modalControlY);

        int panelWidth = layout.partyRightPanelRight - layout.partyLeftPanelLeft;
        int formWidth = Math.max(140, Math.min(190, panelWidth - 48));
        int formX = layout.centerX - (formWidth / 2);
        this.joinCodeBox.setWidth(formWidth);
        this.joinCodeBox.setX(formX);
        this.joinCodeBox.setY(layout.partyFormTop + 34);

        this.createPartyButton.setWidth(formWidth);
        this.createPartyButton.setX(formX);
        this.createPartyButton.setY(layout.partyFormTop);

        this.joinPartyButton.setWidth(formWidth);
        this.joinPartyButton.setX(formX);
        this.joinPartyButton.setY(layout.partyFormTop + 62);

        int partyActionGap = 12;
        int actionWidth = Math.max(110, Math.min(170, (panelWidth - partyActionGap) / 2));
        int actionsLeft = layout.centerX - ((actionWidth * 2 + partyActionGap) / 2);
        this.leavePartyButton.setWidth(actionWidth);
        this.leavePartyButton.setX(actionsLeft);
        this.leavePartyButton.setY(layout.partyActionsY);

        this.dissolvePartyButton.setWidth(actionWidth);
        this.dissolvePartyButton.setX(actionsLeft + actionWidth + partyActionGap);
        this.dissolvePartyButton.setY(layout.partyActionsY);
    }

    private HubLayout computeLayout() {
        int centerX = this.width / 2;
        int sideMargin = 10;

        int tabsY = Math.max(8, this.height - 28);
        int titleY = Math.max(14, Math.min(28, this.height / 12));
        int titleHeight = Math.round(this.font.lineHeight * TITLE_SCALE) + 10;
        int feedbackY = titleY + titleHeight + 8;
        int contentTop = feedbackY + this.font.lineHeight + 12;

        int progressNavY = Math.min(this.height - 24, Math.max(contentTop + 90, this.height - 52));

        int progressCardsBottomY = progressNavY - 10;
        int progressPageY = contentTop;
        int progressCardsStartY = progressPageY + this.font.lineHeight + 6;

        int progressCardWidth = Math.min(428, this.width - (sideMargin * 2));
        progressCardWidth = Math.max(236, progressCardWidth);
        progressCardWidth = Math.min(progressCardWidth, this.width - 4);
        if (progressCardWidth < 120) {
            progressCardWidth = Math.max(120, this.width - 2);
        }

        int progressCardLeft = centerX - (progressCardWidth / 2);
        int progressCardRight = progressCardLeft + progressCardWidth;

        int slotsFit = (progressCardsBottomY - progressCardsStartY + CARD_GAP) / (CARD_HEIGHT + CARD_GAP);
        int progressCardsPerPage = clampInt(slotsFit, 1, MAX_PROGRESS_PORTALS_PER_PAGE);

        int partyPanelTop = contentTop;
        int partyPanelBottom = Math.max(partyPanelTop + 182, this.height - 92);
        partyPanelBottom = Math.min(this.height - 40, partyPanelBottom);

        int partyGap = 12;
        int partyTotalWidth = Math.min(428, this.width - (sideMargin * 2));
        partyTotalWidth = Math.max(280, partyTotalWidth);
        partyTotalWidth = Math.min(partyTotalWidth, this.width - 4);

        int partyLeft = centerX - (partyTotalWidth / 2);
        int halfWidth = (partyTotalWidth - partyGap) / 2;

        int partyLeftPanelLeft = partyLeft;
        int partyLeftPanelRight = partyLeftPanelLeft + halfWidth;
        int partyRightPanelLeft = partyLeftPanelRight + partyGap;
        int partyRightPanelRight = partyLeft + partyTotalWidth;

        int partyFormTop = Math.min(partyPanelTop + 88, Math.max(partyPanelTop + 74, partyPanelBottom - 90));
        int partyActionsY = Math.min(this.height - 24, Math.max(partyPanelBottom + 10, this.height - 78));

        int modalWidth = Math.min(MAX_MODAL_WIDTH, this.width - 20);
        if (modalWidth < MIN_MODAL_WIDTH) {
            modalWidth = Math.max(150, this.width - 8);
        }

        int modalLeft = centerX - (modalWidth / 2);
        int modalRight = modalLeft + modalWidth;
        int modalTop = Math.max(26, Math.min(48, this.height / 10));
        int modalBottom = Math.max(40, this.height - 54);
        if (modalBottom <= modalTop + 80) {
            modalBottom = Math.min(this.height - 8, modalTop + 120);
        }
        if (modalBottom - modalTop < 120) {
            modalTop = Math.max(8, modalBottom - 120);
        }
        int modalControlY = Math.min(this.height - 24, modalBottom + 8);

        return new HubLayout(
            centerX,
            tabsY,
            titleY,
            feedbackY,
            contentTop,
            progressPageY,
            progressCardsStartY,
            progressCardsBottomY,
            progressCardLeft,
            progressCardRight,
            progressCardWidth,
            progressCardsPerPage,
            progressNavY,
            partyPanelTop,
            partyPanelBottom,
            partyLeftPanelLeft,
            partyLeftPanelRight,
            partyRightPanelLeft,
            partyRightPanelRight,
            partyFormTop,
            partyActionsY,
            modalLeft,
            modalRight,
            modalTop,
            modalBottom,
            modalControlY
        );
    }

    private int getDetailsRequirementsPerPage(HubLayout layout, boolean hasBypassBanner) {
        int bannerHeight = hasBypassBanner ? this.font.lineHeight + 5 : 0;
        int contentStartY = layout.modalTop + 24 + bannerHeight;
        int contentEndY = layout.modalBottom - 26;
        int usableHeight = contentEndY - contentStartY;
        int perPage = usableHeight / DETAILS_ROW_HEIGHT;
        return Math.max(1, perPage);
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private boolean viewportChanged() {
        return this.width != this.cachedWidth
            || this.height != this.cachedHeight
            || Double.compare(getCurrentGuiScale(), this.cachedGuiScale) != 0;
    }

    private void cacheViewportState() {
        this.cachedWidth = this.width;
        this.cachedHeight = this.height;
        this.cachedGuiScale = getCurrentGuiScale();
    }

    private double getCurrentGuiScale() {
        return this.minecraft != null ? this.minecraft.getWindow().getGuiScale() : 0.0D;
    }

    private boolean isDetailsOpen() {
        return detailsPortalIndex >= 0;
    }

    private void closeDetails() {
        this.detailsPortalIndex = -1;
        this.detailsRequirementsPage = 0;
    }

    private void openDetailsForSlot(int slot) {
        if (this.activeTab != HubTab.PROGRESS) {
            return;
        }

        HubLayout layout = computeLayout();
        CompoundTag state = HubClientState.snapshot();
        ListTag portals = state.getCompound("progress").getList("portals", Tag.TAG_COMPOUND);
        int globalIndex = getPortalGlobalIndexForSlot(slot, layout.progressCardsPerPage);
        if (globalIndex < 0 || globalIndex >= portals.size()) {
            return;
        }

        this.detailsPortalIndex = globalIndex;
        this.detailsRequirementsPage = 0;
    }

    private int getPortalGlobalIndexForSlot(int slot, int portalsPerPage) {
        if (slot < 0 || slot >= portalsPerPage) {
            return -1;
        }
        return (this.progressPage * portalsPerPage) + slot;
    }

    private static int getProgressPages(ListTag portals, int portalsPerPage) {
        int safePerPage = Math.max(1, portalsPerPage);
        return Math.max(1, (int) Math.ceil(portals.size() / (double) safePerPage));
    }
 }
