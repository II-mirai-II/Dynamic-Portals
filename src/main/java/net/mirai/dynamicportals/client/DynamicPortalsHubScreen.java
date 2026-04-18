package net.mirai.dynamicportals.client;

import java.util.ArrayList;
import java.util.List;
import net.mirai.dynamicportals.network.HubClientState;
import net.mirai.dynamicportals.network.HubNetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class DynamicPortalsHubScreen extends Screen {
    private static final int MAX_PROGRESS_PORTALS_PER_PAGE = 3;
    private static final int CARD_HEIGHT = 64;
    private static final int CARD_GAP = 10;
    private static final int BUTTON_HEIGHT = 20;
    private static final int FIELD_HEIGHT = 20;
    private static final int MIN_MODAL_WIDTH = 280;
    private static final int MAX_MODAL_WIDTH = 420;

    private static final int COLOR_PANEL = 0xAA1A1A1A;
    private static final int COLOR_CARD = 0xAA242424;
    private static final int COLOR_BORDER = 0xAA3A3A3A;

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
    private Button globalRefreshButton;

    private final Button[] progressDetailButtons = new Button[MAX_PROGRESS_PORTALS_PER_PAGE];
    private Button detailsCloseButton;
    private Button detailsPrevPageButton;
    private Button detailsNextPageButton;

    private EditBox createPasswordBox;
    private EditBox createAliasBox;
    private EditBox joinCodeBox;
    private EditBox joinPasswordBox;

    private Button createPartyButton;
    private Button joinPartyButton;
    private Button leavePartyButton;
    private Button dissolvePartyButton;
    private Button partyRefreshButton;

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

        final int globalRefreshY;

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
            int globalRefreshY,
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
            this.globalRefreshY = globalRefreshY;
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
        String createPasswordValue = this.createPasswordBox != null ? this.createPasswordBox.getValue() : "";
        String createAliasValue = this.createAliasBox != null ? this.createAliasBox.getValue() : "";
        String joinCodeValue = this.joinCodeBox != null ? this.joinCodeBox.getValue() : "";
        String joinPasswordValue = this.joinPasswordBox != null ? this.joinPasswordBox.getValue() : "";

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

        this.globalRefreshButton = this.addRenderableWidget(Button.builder(
            Component.translatable("dynamicportals.ui.button.refresh"),
            button -> HubNetworkHandler.requestRefreshFromClient()
        ).bounds(0, this.height - 26, 100, BUTTON_HEIGHT).build());

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

        this.createPasswordBox = this.addRenderableWidget(new EditBox(
            this.font,
            0,
            0,
            180,
            FIELD_HEIGHT,
            Component.translatable("dynamicportals.ui.party.create.password")
        ));
        this.createPasswordBox.setMaxLength(32);

        this.createAliasBox = this.addRenderableWidget(new EditBox(
            this.font,
            0,
            0,
            180,
            FIELD_HEIGHT,
            Component.translatable("dynamicportals.ui.party.create.alias")
        ));
        this.createAliasBox.setMaxLength(24);

        this.joinCodeBox = this.addRenderableWidget(new EditBox(
            this.font,
            0,
            0,
            180,
            FIELD_HEIGHT,
            Component.translatable("dynamicportals.ui.party.join.code")
        ));
        this.joinCodeBox.setMaxLength(64);

        this.joinPasswordBox = this.addRenderableWidget(new EditBox(
            this.font,
            0,
            0,
            180,
            FIELD_HEIGHT,
            Component.translatable("dynamicportals.ui.party.join.password")
        ));
        this.joinPasswordBox.setMaxLength(32);

        this.createPartyButton = this.addRenderableWidget(Button.builder(
            Component.translatable("dynamicportals.ui.button.create_party"),
            button -> {
                confirmLeave = false;
                confirmDissolve = false;
                HubNetworkHandler.requestPartyCreateFromClient(
                    createPasswordBox.getValue().trim(),
                    createAliasBox.getValue().trim()
                );
            }
        ).bounds(0, 0, 180, BUTTON_HEIGHT).build());

        this.joinPartyButton = this.addRenderableWidget(Button.builder(
            Component.translatable("dynamicportals.ui.button.join_party"),
            button -> {
                confirmLeave = false;
                confirmDissolve = false;
                HubNetworkHandler.requestPartyJoinFromClient(
                    joinCodeBox.getValue().trim(),
                    joinPasswordBox.getValue().trim()
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

        this.partyRefreshButton = this.addRenderableWidget(Button.builder(
            Component.translatable("dynamicportals.ui.button.refresh_party"),
            button -> HubNetworkHandler.requestRefreshFromClient()
        ).bounds(0, this.height - 52, 120, BUTTON_HEIGHT).build());

        this.createPasswordBox.setValue(createPasswordValue);
        this.createAliasBox.setValue(createAliasValue);
        this.joinCodeBox.setValue(joinCodeValue);
        this.joinPasswordBox.setValue(joinPasswordValue);

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
        graphics.drawCenteredString(this.font, this.title, layout.centerX, layout.titleY, 0xFFFFFF);

        CompoundTag state = HubClientState.snapshot();
        renderFeedback(graphics, state, layout);

        if (this.activeTab == HubTab.PROGRESS) {
            renderProgressTab(graphics, state, layout);
        } else {
            renderPartyTab(graphics, state, layout);
        }

        if (isDetailsOpen()) {
            renderProgressDetailsModal(graphics, state, layout);
        }
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

            Component portalLine = Component.literal(portal.getString("display_name") + " -> ")
                .append(Component.translatable(portal.getString("status_key")));
            int textX = cardLeft + 8;
            int textMaxWidth = layout.progressCardWidth - 16;
            int textY = cardTop + 6;

            drawEllipsizedString(graphics, portalLine, textX, textY, textMaxWidth, portal.getInt("status_color"));
            textY += this.font.lineHeight + 2;

            Component summaryLine = Component.translatable(
                "dynamicportals.command.check.compact_summary",
                portal.getInt("completed"),
                portal.getInt("total"),
                portal.getInt("percent"),
                portal.getInt("missing_count")
            );
            drawEllipsizedString(graphics, summaryLine, textX, textY, textMaxWidth, 0xFFB0B0B0);
            textY += this.font.lineHeight + 2;

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
            int leftPanelLeft = layout.partyLeftPanelLeft;
            int leftPanelRight = layout.partyLeftPanelRight;
            int rightPanelLeft = layout.partyRightPanelLeft;
            int rightPanelRight = layout.partyRightPanelRight;

            drawPanel(graphics, leftPanelLeft, layout.partyPanelTop, leftPanelRight, layout.partyPanelBottom, COLOR_PANEL, COLOR_BORDER);
            drawPanel(graphics, rightPanelLeft, layout.partyPanelTop, rightPanelRight, layout.partyPanelBottom, COLOR_PANEL, COLOR_BORDER);

            graphics.drawCenteredString(
                this.font,
                Component.translatable("dynamicportals.ui.party.no_party"),
                layout.centerX,
                Math.max(layout.feedbackY, layout.partyPanelTop - this.font.lineHeight - 2),
                0xFFFFFF55
            );

            graphics.drawString(
                this.font,
                Component.translatable("dynamicportals.ui.party.create.section"),
                leftPanelLeft + 8,
                layout.partyPanelTop + 8,
                0xFFFFFFFF,
                false
            );

            Component createHelp = Component.translatable("dynamicportals.ui.party.create.help");
            graphics.drawWordWrap(
                this.font,
                createHelp,
                leftPanelLeft + 8,
                layout.partyPanelTop + 22,
                Math.max(40, leftPanelRight - leftPanelLeft - 16),
                0xFFB0B0B0
            );

            graphics.drawString(
                this.font,
                Component.translatable("dynamicportals.ui.party.join.section"),
                rightPanelLeft + 8,
                layout.partyPanelTop + 8,
                0xFFFFFFFF,
                false
            );

            Component joinHelp = Component.translatable("dynamicportals.ui.party.join.help");
            graphics.drawWordWrap(
                this.font,
                joinHelp,
                rightPanelLeft + 8,
                layout.partyPanelTop + 22,
                Math.max(40, rightPanelRight - rightPanelLeft - 16),
                0xFFB0B0B0
            );
            return;
        }

        int dashboardLeft = layout.partyLeftPanelLeft;
        int dashboardRight = layout.partyRightPanelRight;
        int panelWidth = dashboardRight - dashboardLeft;
        int textMaxWidth = panelWidth - 16;
        int lineStep = this.font.lineHeight + 2;

        drawPanel(graphics, dashboardLeft, layout.partyPanelTop, dashboardRight, layout.partyPanelBottom, COLOR_PANEL, COLOR_BORDER);

        String alias = party.getString("alias");
        String code = party.getString("short_code");
        int memberCount = party.getInt("member_count");
        boolean isCreator = party.getBoolean("is_creator");

        int y = layout.partyPanelTop + 8;
        drawEllipsizedString(graphics, Component.translatable("dynamicportals.ui.party.info.header"), dashboardLeft + 8, y, textMaxWidth, 0xFFFFFFFF);
        y += lineStep;
        drawEllipsizedString(graphics, Component.translatable("dynamicportals.ui.party.dashboard.subtitle"), dashboardLeft + 8, y, textMaxWidth, 0xFFB0B0B0);
        y += lineStep;
        drawEllipsizedString(graphics, Component.translatable("dynamicportals.ui.party.info.code", code), dashboardLeft + 8, y, textMaxWidth, 0xFFAAAAFF);
        y += lineStep;
        drawEllipsizedString(graphics, Component.translatable("dynamicportals.ui.party.info.alias", alias.isBlank() ? "-" : alias), dashboardLeft + 8, y, textMaxWidth, 0xFFAAFFFF);
        y += lineStep;
        drawEllipsizedString(graphics, Component.translatable("dynamicportals.ui.party.info.members", memberCount), dashboardLeft + 8, y, textMaxWidth, 0xFFFFFFAA);
        y += lineStep;

        Component creatorText = isCreator
            ? Component.translatable("dynamicportals.ui.party.info.creator.you")
            : Component.translatable("dynamicportals.ui.party.info.creator.other");
        int wrappedLines = Math.max(1, this.font.split(creatorText, textMaxWidth).size());
        graphics.drawWordWrap(this.font, creatorText, dashboardLeft + 8, y, textMaxWidth, 0xFFB0B0B0);
        y += wrappedLines * lineStep;
        y += 2;

        drawEllipsizedString(graphics, Component.translatable("dynamicportals.ui.party.members.header"), dashboardLeft + 8, y, textMaxWidth, 0xFFFFFFFF);
        y += lineStep;

        ListTag members = party.getList("members", Tag.TAG_COMPOUND);
        int maxMemberLines = Math.max(1, (layout.partyPanelBottom - y - 4) / lineStep);
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
            graphics.drawString(this.font, ellipsize(line, textMaxWidth), dashboardLeft + 8, y, color, false);
            y += lineStep;
        }

        int hiddenMembers = members.size() - shownMembers;
        if (hiddenMembers > 0 && y <= layout.partyPanelBottom - this.font.lineHeight) {
            drawEllipsizedString(
                graphics,
                Component.translatable("dynamicportals.ui.party.members.more", hiddenMembers),
                dashboardLeft + 8,
                y,
                textMaxWidth,
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

        graphics.fill(0, 0, this.width, this.height, 0xAA000000);
        drawPanel(graphics, layout.modalLeft, layout.modalTop, layout.modalRight, layout.modalBottom, 0xF0202020, 0xFF5A5A5A);

        graphics.drawCenteredString(
            this.font,
            Component.translatable("dynamicportals.ui.progress.details.title", portal.getString("display_name")),
            layout.centerX,
            layout.modalTop + 8,
            0xFFFFFFFF
        );
        graphics.hLine(layout.modalLeft, layout.modalRight - 1, layout.modalTop + this.font.lineHeight + 12, 0x553A3A3A);

        int textMaxWidth = layout.modalRight - layout.modalLeft - 20;
        int lineStep = this.font.lineHeight + 2;
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
            lineY += lineStep;
        }

        if (requirements.isEmpty()) {
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
        int totalPages = Math.max(1, (int) Math.ceil(requirements.size() / (double) requirementsPerPage));
        this.detailsRequirementsPage = Math.max(0, Math.min(this.detailsRequirementsPage, totalPages - 1));

        int start = this.detailsRequirementsPage * requirementsPerPage;
        int end = Math.min(requirements.size(), start + requirementsPerPage);

        int y = lineY;
        for (int i = start; i < end; i++) {
            CompoundTag requirement = requirements.getCompound(i);

            Component line = Component.translatable(
                "dynamicportals.command.check.requirement_line",
                Component.translatable(requirement.getString("type_key")),
                requirement.getString("target_name"),
                requirement.getInt("current"),
                requirement.getInt("required")
            );

            int color = requirement.getBoolean("completed") ? 0xFF66CC66 : 0xFFFFFF66;
            drawEllipsizedString(graphics, line, layout.modalLeft + 10, y, textMaxWidth, color);
            y += lineStep;
        }

        graphics.drawCenteredString(
            this.font,
            Component.translatable("dynamicportals.ui.progress.details.page", this.detailsRequirementsPage + 1, totalPages),
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

    private void drawEllipsizedString(GuiGraphics graphics, Component text, int x, int y, int maxWidth, int color) {
        graphics.drawString(this.font, ellipsize(text.getString(), maxWidth), x, y, color, false);
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
        this.progressPrevButton.active = !detailsOpen && this.progressPage > 0;
        this.progressNextButton.active = !detailsOpen && this.progressPage < totalPages - 1;

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

        this.createPartyButton.active = this.createPasswordBox.getValue().trim().length() >= 4;
        this.joinPartyButton.active = !this.joinCodeBox.getValue().trim().isEmpty()
            && !this.joinPasswordBox.getValue().trim().isEmpty();

        CompoundTag party = state.getCompound("party");
        boolean inParty = party.getBoolean("in_party");
        this.leavePartyButton.active = inParty;
        this.dissolvePartyButton.active = inParty && party.getBoolean("is_creator");
        this.partyRefreshButton.active = inParty;

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

        CompoundTag progress = state.getCompound("progress");
        ListTag portals = progress.getList("portals", Tag.TAG_COMPOUND);
        int totalPages = getProgressPages(portals, layout.progressCardsPerPage);
        this.progressPage = Math.max(0, Math.min(this.progressPage, totalPages - 1));
        int currentPageStart = this.progressPage * layout.progressCardsPerPage;
        int visibleProgressCards = Math.max(0, Math.min(layout.progressCardsPerPage, portals.size() - currentPageStart));

        this.progressPrevButton.visible = progressTab && !detailsOpen;
        this.progressNextButton.visible = progressTab && !detailsOpen;

        for (int i = 0; i < this.progressDetailButtons.length; i++) {
            this.progressDetailButtons[i].visible = progressTab && !detailsOpen && i < visibleProgressCards;
        }

        this.detailsCloseButton.visible = progressTab && detailsOpen;
        this.detailsPrevPageButton.visible = progressTab && detailsOpen;
        this.detailsNextPageButton.visible = progressTab && detailsOpen;

        this.globalRefreshButton.visible = progressTab && !detailsOpen;

        CompoundTag party = state.getCompound("party");
        boolean inParty = party.getBoolean("in_party");
        boolean partyTab = this.activeTab == HubTab.PARTY;

        boolean showPartyForms = partyTab && !inParty;
        this.createPasswordBox.visible = showPartyForms;
        this.createAliasBox.visible = showPartyForms;
        this.joinCodeBox.visible = showPartyForms;
        this.joinPasswordBox.visible = showPartyForms;
        this.createPartyButton.visible = showPartyForms;
        this.joinPartyButton.visible = showPartyForms;

        boolean showPartyActions = partyTab && inParty;
        this.leavePartyButton.visible = showPartyActions;
        this.dissolvePartyButton.visible = showPartyActions;
        this.partyRefreshButton.visible = showPartyActions;
    }

    private void applyLayoutToWidgets() {
        if (this.progressTabButton == null || this.partyTabButton == null) {
            return;
        }

        HubLayout layout = computeLayout();

        int tabGap = 8;
        int tabWidth = Math.max(96, Math.min(148, (this.width - 24 - tabGap) / 2));
        this.progressTabButton.setWidth(tabWidth);
        this.partyTabButton.setWidth(tabWidth);
        this.progressTabButton.setX(layout.centerX - tabGap / 2 - tabWidth);
        this.progressTabButton.setY(layout.tabsY);
        this.partyTabButton.setX(layout.centerX + tabGap / 2);
        this.partyTabButton.setY(layout.tabsY);

        int refreshWidth = Math.max(78, Math.min(110, this.width / 4));
        this.globalRefreshButton.setWidth(refreshWidth);
        this.globalRefreshButton.setX(this.width - refreshWidth - 12);
        this.globalRefreshButton.setY(layout.globalRefreshY);

        int navWidth = Math.max(70, Math.min(96, (layout.progressCardWidth - 12) / 3));
        this.progressPrevButton.setWidth(navWidth);
        this.progressNextButton.setWidth(navWidth);
        this.progressPrevButton.setX(layout.progressCardLeft);
        this.progressPrevButton.setY(layout.progressNavY);
        this.progressNextButton.setX(layout.progressCardLeft + navWidth + 8);
        this.progressNextButton.setY(layout.progressNavY);

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

        int leftX = layout.partyLeftPanelLeft + 8;
        int rightX = layout.partyRightPanelLeft + 8;
        int formWidth = Math.max(110, Math.min(180, (layout.partyLeftPanelRight - layout.partyLeftPanelLeft) - 16));

        this.createPasswordBox.setWidth(formWidth);
        this.createPasswordBox.setX(leftX);
        this.createPasswordBox.setY(layout.partyFormTop);
        this.createAliasBox.setWidth(formWidth);
        this.createAliasBox.setX(leftX);
        this.createAliasBox.setY(layout.partyFormTop + 28);

        this.joinCodeBox.setWidth(formWidth);
        this.joinCodeBox.setX(rightX);
        this.joinCodeBox.setY(layout.partyFormTop);
        this.joinPasswordBox.setWidth(formWidth);
        this.joinPasswordBox.setX(rightX);
        this.joinPasswordBox.setY(layout.partyFormTop + 28);

        this.createPartyButton.setWidth(formWidth);
        this.createPartyButton.setX(leftX);
        this.createPartyButton.setY(layout.partyFormTop + 56);

        this.joinPartyButton.setWidth(formWidth);
        this.joinPartyButton.setX(rightX);
        this.joinPartyButton.setY(layout.partyFormTop + 56);

        this.leavePartyButton.setWidth(formWidth);
        this.leavePartyButton.setX(leftX);
        this.leavePartyButton.setY(layout.partyActionsY);

        this.dissolvePartyButton.setWidth(formWidth);
        this.dissolvePartyButton.setX(rightX);
        this.dissolvePartyButton.setY(layout.partyActionsY);

        int partyRefreshWidth = Math.max(96, Math.min(120, layout.progressCardWidth / 3));
        this.partyRefreshButton.setWidth(partyRefreshWidth);
        this.partyRefreshButton.setX(layout.centerX - (partyRefreshWidth / 2));
        this.partyRefreshButton.setY(Math.min(this.height - 24, layout.partyActionsY + 26));
    }

    private HubLayout computeLayout() {
        int centerX = this.width / 2;
        int sideMargin = 10;

        int tabsY = 16;
        int titleY = tabsY + BUTTON_HEIGHT + 10;
        int feedbackY = titleY + this.font.lineHeight + 3;
        int contentTop = feedbackY + this.font.lineHeight + 8;

        int progressNavY = Math.min(this.height - 24, Math.max(contentTop + 90, this.height - 52));
        int globalRefreshY = Math.max(8, this.height - 26);

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
        int partyPanelBottom = Math.max(partyPanelTop + 110, this.height - 92);
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

        int partyFormTop = partyPanelTop + 42;
        int partyActionsY = Math.min(this.height - 24, Math.max(partyPanelBottom + 10, this.height - 78));

        int modalWidth = Math.min(MAX_MODAL_WIDTH, this.width - 20);
        if (modalWidth < MIN_MODAL_WIDTH) {
            modalWidth = Math.max(150, this.width - 8);
        }

        int modalLeft = centerX - (modalWidth / 2);
        int modalRight = modalLeft + modalWidth;
        int modalTop = Math.max(contentTop - 4, 42);
        int modalBottom = Math.min(this.height - 12, this.height - 64);
        if (modalBottom - modalTop < 120) {
            modalTop = Math.max(20, modalBottom - 120);
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
            globalRefreshY,
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
        int lineStep = this.font.lineHeight + 2;
        int contentStartY = layout.modalTop + 24 + (hasBypassBanner ? lineStep : 0);
        int contentEndY = layout.modalBottom - 26;
        int usableHeight = contentEndY - contentStartY;
        int perPage = usableHeight / lineStep;
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
