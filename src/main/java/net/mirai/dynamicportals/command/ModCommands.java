package net.mirai.dynamicportals.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.mirai.dynamicportals.config.PortalDefinition;
import net.mirai.dynamicportals.config.PortalRules;
import net.mirai.dynamicportals.item.ModItems;
import net.mirai.dynamicportals.party.PartyStore;
import net.mirai.dynamicportals.progress.ProgressStore;
import net.mirai.dynamicportals.requirements.RequirementEngine;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class ModCommands {
    private static final int CHECK_PAGE_SIZE = 14;
    private static final int COMPACT_PREVIEW_LIMIT = 3;
    private static final int CHECK_MAX_LINES = 320;
    private static final String ALIAS_NETHER = "minecraft:the_nether";
    private static final String ALIAS_END = "minecraft:the_end";
    private static final String ALIAS_OVERWORLD = "minecraft:overworld";

    private enum CheckOutputMode {
        COMPACT,
        DETAILED
    }

    private record PortalView(PortalDefinition definition, RequirementEngine.PortalStatus status) {
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        // Root /dp command
        var dpRoot = Commands.literal("dp")
            .then(buildCheckCommand())
            .then(buildDebugCommand())
            .then(PartyCommands.buildPartyCommand());

        event.getDispatcher().register(dpRoot);
    }

    private com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildCheckCommand() {
        return Commands.literal("check")
            .requires(source -> source.getEntity() instanceof ServerPlayer)
            .executes(context -> checkSelf(context, null, CheckOutputMode.COMPACT, false, 1))
            .then(Commands.literal("help")
                .executes(this::checkHelp)
            )
            .then(Commands.literal("summary")
                .executes(context -> checkSelf(context, null, CheckOutputMode.COMPACT, false, 1))
                .then(Commands.argument("page", IntegerArgumentType.integer(1, 999))
                    .executes(context -> checkSelf(context, null, CheckOutputMode.COMPACT, false, IntegerArgumentType.getInteger(context, "page")))
                )
            )
            .then(Commands.literal("pending")
                .executes(context -> checkSelf(context, null, CheckOutputMode.DETAILED, true, 1))
                .then(Commands.argument("page", IntegerArgumentType.integer(1, 999))
                    .executes(context -> checkSelf(
                        context,
                        null,
                        CheckOutputMode.DETAILED,
                        true,
                        IntegerArgumentType.getInteger(context, "page")
                    ))
                )
            )
            .then(Commands.argument("dimension", StringArgumentType.word())
                .executes(context -> checkSelf(context, StringArgumentType.getString(context, "dimension"), CheckOutputMode.COMPACT, false, 1))
                .then(Commands.literal("summary")
                    .executes(context -> checkSelf(context, StringArgumentType.getString(context, "dimension"), CheckOutputMode.COMPACT, false, 1))
                    .then(Commands.argument("page", IntegerArgumentType.integer(1, 999))
                        .executes(context -> checkSelf(
                            context,
                            StringArgumentType.getString(context, "dimension"),
                            CheckOutputMode.COMPACT,
                            false,
                            IntegerArgumentType.getInteger(context, "page")
                        ))
                    )
                )
                .then(Commands.literal("pending")
                    .executes(context -> checkSelf(
                        context,
                        StringArgumentType.getString(context, "dimension"),
                        CheckOutputMode.DETAILED,
                        true,
                        1
                    ))
                    .then(Commands.argument("page", IntegerArgumentType.integer(1, 999))
                        .executes(context -> checkSelf(
                            context,
                            StringArgumentType.getString(context, "dimension"),
                            CheckOutputMode.DETAILED,
                            true,
                            IntegerArgumentType.getInteger(context, "page")
                        ))
                    )
                )
                .then(Commands.argument("page", IntegerArgumentType.integer(1, 999))
                    .executes(context -> checkSelf(
                        context,
                        StringArgumentType.getString(context, "dimension"),
                        CheckOutputMode.DETAILED,
                        false,
                        IntegerArgumentType.getInteger(context, "page")
                    ))
                )
            );
    }

    private com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildDebugCommand() {
        return Commands.literal("debug")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("sword")
                .then(Commands.argument("targets", EntityArgument.players())
                    .executes(context -> giveSword(context, 1))
                    .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                        .executes(context -> giveSword(context, IntegerArgumentType.getInteger(context, "count")))
                    )
                )
            );
    }

    private int checkSelf(
        CommandContext<CommandSourceStack> context,
        String dimensionFilter,
        CheckOutputMode mode,
        boolean pendingOnly,
        int requestedPage
    ) {
        ServerPlayer player = (ServerPlayer) context.getSource().getEntity();
        if (player == null) {
            return 0;
        }

        String normalizedFilter = normalizeDimensionFilter(dimensionFilter);
        List<PortalDefinition> selectedDefinitions = selectDefinitions(normalizedFilter);

        if (normalizedFilter == null && selectedDefinitions.isEmpty()) {
            context.getSource().sendSuccess(
                () -> Component.translatable("dynamicportals.command.check.no_portals_configured")
                    .withStyle(ChatFormatting.RED),
                false
            );
            context.getSource().sendSuccess(
                () -> Component.translatable("dynamicportals.command.check.hint.help")
                    .withStyle(ChatFormatting.GRAY),
                false
            );
            return 0;
        }

        if (normalizedFilter != null && selectedDefinitions.isEmpty()) {
            String available = buildAvailableDimensions();
            context.getSource().sendSuccess(
                () -> Component.translatable("dynamicportals.command.check.invalid_dimension", dimensionFilter, available)
                    .withStyle(ChatFormatting.RED),
                false
            );
            context.getSource().sendSuccess(
                () -> Component.translatable("dynamicportals.command.check.hint.help")
                    .withStyle(ChatFormatting.GRAY),
                false
            );
            return 0;
        }

        boolean usePartyProgress = PartyStore.isInParty(player) && mode == CheckOutputMode.COMPACT;
        List<PortalView> portalViews = buildPortalViews(player, selectedDefinitions, mode, usePartyProgress);

        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("dynamicportals.command.check.header").withStyle(ChatFormatting.GOLD));

        boolean truncated = false;
        for (PortalView view : portalViews) {
            if (lines.size() >= CHECK_MAX_LINES) {
                truncated = true;
                break;
            }
            boolean fullyWritten = appendPortalLines(lines, player, view.definition(), view.status(), mode, pendingOnly, CHECK_MAX_LINES);
            if (!fullyWritten) {
                truncated = true;
                break;
            }
        }

        int totalPages = Math.max(1, (int) Math.ceil(lines.size() / (double) CHECK_PAGE_SIZE));
        int page = Math.max(1, Math.min(requestedPage, totalPages));
        int start = (page - 1) * CHECK_PAGE_SIZE;
        int end = Math.min(lines.size(), start + CHECK_PAGE_SIZE);
        int shown = Math.max(0, end - start);
        int hidden = Math.max(0, lines.size() - end);

        for (int index = start; index < end; index++) {
            Component line = lines.get(index);
            context.getSource().sendSuccess(() -> line, false);
        }

        context.getSource().sendSuccess(
            () -> Component.translatable("dynamicportals.command.check.page", page, totalPages, shown, lines.size(), hidden)
                .withStyle(ChatFormatting.DARK_GRAY),
            false
        );

        if (requestedPage != page) {
            context.getSource().sendSuccess(
                () -> Component.translatable("dynamicportals.command.check.page_adjusted", requestedPage, page)
                    .withStyle(ChatFormatting.GRAY),
                false
            );
        }

        if (mode == CheckOutputMode.COMPACT && page == totalPages) {
            String hintDimension = firstDimensionFromFilter(normalizedFilter);
            context.getSource().sendSuccess(
                () -> Component.translatable("dynamicportals.command.check.hint.detail", hintDimension)
                    .withStyle(ChatFormatting.GRAY),
                false
            );
        }

        if (hidden > 0) {
            String nextCommand = buildNextPageCommand(normalizedFilter, mode, pendingOnly, page + 1);
            context.getSource().sendSuccess(
                () -> Component.translatable("dynamicportals.command.check.hint.next_page", nextCommand)
                    .withStyle(ChatFormatting.GRAY),
                false
            );
        }

        if (truncated) {
            context.getSource().sendSuccess(
                () -> Component.translatable("dynamicportals.command.check.truncated", CHECK_MAX_LINES)
                    .withStyle(ChatFormatting.GRAY),
                false
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    private boolean appendPortalLines(
        List<Component> lines,
        ServerPlayer player,
        PortalDefinition definition,
        RequirementEngine.PortalStatus status,
        CheckOutputMode mode,
        boolean pendingOnly,
        int maxLines
    ) {
        // Evaluate both individual and party status
        boolean inParty = PartyStore.isInParty(player);
        RequirementEngine.PortalStatus individualStatus = RequirementEngine.evaluate(player, definition);
        RequirementEngine.PortalStatus partyStatus = inParty
            ? RequirementEngine.evaluateParty(player, definition)
            : individualStatus;
        RequirementEngine.PortalStatus effectiveStatus = inParty ? partyStatus : individualStatus;
        
        // Determine access origin: individual completed, party completed, or bypass
        boolean individualCompleted = individualStatus.unlocked() && !individualStatus.bypassUnlocked();
        boolean partyCompleted = inParty && partyStatus.unlocked() && !partyStatus.bypassUnlocked() && !individualCompleted;
        boolean isBypassed = individualStatus.bypassUnlocked();
        
        // Build status line with color based on access method
        ChatFormatting statusColor = ChatFormatting.RED; // Default: blocked
        Component statusText = Component.translatable("dynamicportals.command.check.status.blocked");
        
        if (individualCompleted) {
            statusColor = ChatFormatting.GREEN;
            statusText = Component.translatable("dynamicportals.command.check.status.unlocked_individual");
        } else if (partyCompleted) {
            statusColor = ChatFormatting.BLUE;
            statusText = Component.translatable("dynamicportals.command.check.status.unlocked_party");
        } else if (isBypassed) {
            statusColor = ChatFormatting.AQUA;
            statusText = Component.translatable("dynamicportals.command.check.status.unlocked_bypass");
        } else if (partyStatus.unlocked()) {
            statusColor = ChatFormatting.DARK_BLUE;
            statusText = Component.translatable("dynamicportals.command.check.status.unlocked_by_party");
        }
        
        if (!addLine(lines, Component.translatable(
            "dynamicportals.command.check.portal_line",
            definition.displayName(),
            statusText
        ).withStyle(statusColor), maxLines)) {
            return false;
        }

        // Show party status if player is in a party
        if (inParty) {
            Component partyStatusLine;
            
            if (partyStatus.unlocked() && !partyStatus.bypassUnlocked()) {
                partyStatusLine = Component.literal("  Party: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable("dynamicportals.command.check.status.party_completed")
                        .withStyle(ChatFormatting.BLUE)
                    );
            } else if (!partyStatus.missingEntries().isEmpty()) {
                int missing = partyStatus.missingEntries().size();
                int total = definition.killRequirements().size() 
                    + definition.itemRequirements().size() 
                    + definition.advancementRequirements().size();
                partyStatusLine = Component.literal("  Party: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("(" + (total - missing) + "/" + total + " completed)")
                        .withStyle(ChatFormatting.YELLOW)
                    );
            } else {
                partyStatusLine = Component.literal("  Party: no requirements for this portal")
                    .withStyle(ChatFormatting.GRAY);
            }
            
            if (!addLine(lines, partyStatusLine, maxLines)) {
                return false;
            }
        }

        // Show bypass banner for clarity
        if (isBypassed && !individualStatus.missingEntries().isEmpty()) {
            if (!addLine(lines, Component.translatable(
                "dynamicportals.command.check.bypass_banner",
                definition.displayName(),
                definition.destinationDimension()
            ).withStyle(ChatFormatting.AQUA), maxLines)) {
                return false;
            }
        }

        if (mode == CheckOutputMode.COMPACT) {
            return appendCompactPortalLines(lines, definition, effectiveStatus, maxLines);
        }

        return appendDetailedPortalLines(lines, player, definition, pendingOnly, maxLines);
    }

    private boolean appendCompactPortalLines(
        List<Component> lines,
        PortalDefinition definition,
        RequirementEngine.PortalStatus status,
        int maxLines
    ) {
        int totalRequirements = definition.killRequirements().size()
            + definition.itemRequirements().size()
            + definition.advancementRequirements().size();
        int missing = status.missingEntries().size();
        int completed = Math.max(0, totalRequirements - missing);
        int percent = totalRequirements == 0
            ? 100
            : (int) Math.floor((completed * 100.0D) / totalRequirements);

        if (!addLine(lines, Component.translatable(
            "dynamicportals.command.check.compact_summary",
            completed,
            totalRequirements,
            percent,
            missing
        ).withStyle(ChatFormatting.GRAY), maxLines)) {
            return false;
        }

        String preview = buildMissingPreview(status.missingEntries(), COMPACT_PREVIEW_LIMIT);
        if (preview.isEmpty()) {
            return addLine(lines, Component.translatable("dynamicportals.command.check.compact_clear")
                .withStyle(ChatFormatting.DARK_GREEN), maxLines);
        } else {
            return addLine(lines, Component.translatable("dynamicportals.command.check.compact_missing", preview)
                .withStyle(ChatFormatting.YELLOW), maxLines);
        }
    }

    private boolean appendDetailedPortalLines(
        List<Component> lines,
        ServerPlayer player,
        PortalDefinition definition,
        boolean pendingOnly,
        int maxLines
    ) {
        int before = lines.size();

        for (var entry : definition.killRequirements().entrySet()) {
            String entityId = entry.getKey();
            int required = entry.getValue();
            int current = ProgressStore.getKillCount(player, definition.destinationDimension(), entityId);
            if (pendingOnly && current >= required) {
                continue;
            }
            if (!addLine(lines, Component.translatable(
                "dynamicportals.command.check.requirement_line",
                Component.translatable("dynamicportals.type.kill"),
                humanizeResourceId(entityId),
                current,
                required
            ).withStyle(ChatFormatting.YELLOW), maxLines)) {
                return false;
            }
        }

        for (var entry : definition.itemRequirements().entrySet()) {
            String itemId = entry.getKey();
            int required = entry.getValue();
            int current = ProgressStore.getItemCount(player, definition.destinationDimension(), itemId);
            if (pendingOnly && current >= required) {
                continue;
            }
            if (!addLine(lines, Component.translatable(
                "dynamicportals.command.check.requirement_line",
                Component.translatable("dynamicportals.type.item"),
                humanizeResourceId(itemId),
                current,
                required
            ).withStyle(ChatFormatting.YELLOW), maxLines)) {
                return false;
            }
        }

        for (String advancementId : definition.advancementRequirements()) {
            boolean done = ProgressStore.hasAdvancement(player, definition.destinationDimension(), advancementId);
            if (pendingOnly && done) {
                continue;
            }
            if (!addLine(lines, Component.translatable(
                "dynamicportals.command.check.requirement_line",
                Component.translatable("dynamicportals.type.adv"),
                humanizeResourceId(advancementId),
                done ? 1 : 0,
                1
            ).withStyle(ChatFormatting.YELLOW), maxLines)) {
                return false;
            }
        }

        if (pendingOnly && lines.size() == before) {
            return addLine(lines, Component.translatable("dynamicportals.command.check.pending_clear")
                .withStyle(ChatFormatting.DARK_GREEN), maxLines);
        }

        return true;
    }

    private static boolean addLine(List<Component> lines, Component line, int maxLines) {
        if (lines.size() >= maxLines) {
            return false;
        }
        lines.add(line);
        return true;
    }

    private int checkHelp(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(
            () -> Component.translatable("dynamicportals.command.check.help.header").withStyle(ChatFormatting.GOLD),
            false
        );
        context.getSource().sendSuccess(
            () -> Component.translatable("dynamicportals.command.check.help.line_1").withStyle(ChatFormatting.GRAY),
            false
        );
        context.getSource().sendSuccess(
            () -> Component.translatable("dynamicportals.command.check.help.line_2").withStyle(ChatFormatting.GRAY),
            false
        );
        context.getSource().sendSuccess(
            () -> Component.translatable("dynamicportals.command.check.help.line_3").withStyle(ChatFormatting.GRAY),
            false
        );
        context.getSource().sendSuccess(
            () -> Component.translatable("dynamicportals.command.check.help.line_4").withStyle(ChatFormatting.GRAY),
            false
        );
        return Command.SINGLE_SUCCESS;
    }

    private static List<PortalView> buildPortalViews(
        ServerPlayer player,
        List<PortalDefinition> selectedDefinitions,
        CheckOutputMode mode,
        boolean usePartyProgress
    ) {
        List<PortalView> views = new ArrayList<>();
        for (PortalDefinition definition : selectedDefinitions) {
            RequirementEngine.PortalStatus status = usePartyProgress
                ? RequirementEngine.evaluateParty(player, definition)
                : RequirementEngine.evaluate(player, definition);
            views.add(new PortalView(definition, status));
        }

        if (mode == CheckOutputMode.COMPACT && views.size() > 1) {
            views.sort(Comparator
                .comparingInt((PortalView view) -> view.status().unlocked() ? 1 : 0)
                .thenComparingInt(view -> view.status().missingEntries().size())
                .thenComparing(view -> view.definition().destinationDimension()));
        }

        return views;
    }

    private static String buildMissingPreview(List<String> missingEntries, int limit) {
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (String entry : missingEntries) {
            if (count >= limit) {
                break;
            }
            String[] parts = entry.split("\\|", 4);
            if (parts.length < 2) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(", ");
            }
            builder.append(humanizeResourceId(parts[1]));
            count++;
        }
        return builder.toString();
    }

    private String firstDimensionFromFilter(String dimensionFilter) {
        if (dimensionFilter != null && !dimensionFilter.isBlank()) {
            return dimensionFilter;
        }
        for (PortalDefinition definition : PortalRules.all()) {
            return definition.destinationDimension();
        }
        return "minecraft:the_nether";
    }

    private static String normalizeDimensionFilter(String dimensionFilter) {
        if (dimensionFilter == null) {
            return null;
        }

        String normalized = dimensionFilter.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return null;
        }

        return switch (normalized) {
            case "nether", "the_nether" -> ALIAS_NETHER;
            case "end", "the_end" -> ALIAS_END;
            case "overworld", "world" -> ALIAS_OVERWORLD;
            default -> normalized;
        };
    }

    private static List<PortalDefinition> selectDefinitions(String normalizedFilter) {
        List<PortalDefinition> selected = new ArrayList<>();
        if (normalizedFilter == null) {
            selected.addAll(PortalRules.all());
            return selected;
        }

        for (PortalDefinition definition : PortalRules.all()) {
            String destination = definition.destinationDimension();
            if (destination.equalsIgnoreCase(normalizedFilter)) {
                selected.add(definition);
            }
        }
        return selected;
    }

    private static String buildAvailableDimensions() {
        Set<String> dimensions = new LinkedHashSet<>();
        for (PortalDefinition definition : PortalRules.all()) {
            dimensions.add(definition.destinationDimension());
        }
        if (dimensions.isEmpty()) {
            return "-";
        }
        return String.join(", ", dimensions);
    }

    private static String buildNextPageCommand(String normalizedFilter, CheckOutputMode mode, boolean pendingOnly, int nextPage) {
        if (pendingOnly) {
            if (normalizedFilter != null) {
                return "/dp check " + normalizedFilter + " pending " + nextPage;
            }
            return "/dp check pending " + nextPage;
        }

        if (mode == CheckOutputMode.DETAILED && normalizedFilter != null) {
            return "/dp check " + normalizedFilter + " " + nextPage;
        }
        if (normalizedFilter != null) {
            return "/dp check " + normalizedFilter + " summary " + nextPage;
        }
        return "/dp check summary " + nextPage;
    }

    private static String humanizeResourceId(String id) {
        int split = id.indexOf(':');
        String raw = split >= 0 ? id.substring(split + 1) : id;
        String[] words = raw.split("_");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                builder.append(word.substring(1));
            }
        }
        return builder.isEmpty() ? id : builder.toString();
    }

    private int giveSword(CommandContext<CommandSourceStack> context, int count) throws CommandSyntaxException {
        int delivered = 0;
        for (ServerPlayer target : EntityArgument.getPlayers(context, "targets")) {
            ItemStack stack = new ItemStack(ModItems.TESTER_WOODEN_SWORD.get(), count);
            boolean added = target.getInventory().add(stack.copy());
            if (!added) {
                target.drop(stack, false);
            }
            delivered++;
        }

        int finalDelivered = delivered;
        context.getSource().sendSuccess(
            () -> Component.translatable("dynamicportals.command.givesword.success", finalDelivered, count)
                .withStyle(ChatFormatting.GREEN),
            true
        );

        return delivered;
    }
}
