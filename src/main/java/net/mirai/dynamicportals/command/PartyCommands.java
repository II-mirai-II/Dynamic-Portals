package net.mirai.dynamicportals.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Set;
import java.util.UUID;
import net.mirai.dynamicportals.chat.ProgressNotifier;
import net.mirai.dynamicportals.party.PartyData;
import net.mirai.dynamicportals.party.PartyStore;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Handler class for all /dp party subcommands.
 */
public class PartyCommands {
    private static final int MIN_PASSWORD_LENGTH = 4;
    private static final int MAX_PASSWORD_LENGTH = 32;
    private static final int MAX_PARTY_SIZE = 8;
    /**
     * Build the /dp party subtree.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildPartyCommand() {
        return Commands.literal("party")
            .requires(source -> source.getEntity() instanceof ServerPlayer)
            .then(Commands.literal("create")
                .then(Commands.argument("password", StringArgumentType.word())
                    .executes(context -> partyCreate(context.getSource(), StringArgumentType.getString(context, "password"), null))
                    .then(Commands.argument("alias", StringArgumentType.greedyString())
                        .executes(context -> partyCreate(
                            context.getSource(),
                            StringArgumentType.getString(context, "password"),
                            StringArgumentType.getString(context, "alias")
                        ))
                    )
                )
            )
            .then(Commands.literal("join")
                .then(Commands.argument("partyKey", StringArgumentType.word())
                    .then(Commands.argument("password", StringArgumentType.word())
                        .executes(context -> partyJoin(
                            context.getSource(),
                            StringArgumentType.getString(context, "partyKey"),
                            StringArgumentType.getString(context, "password")
                        ))
                    )
                )
            )
            .then(Commands.literal("leave")
                .executes(context -> partyLeave(context.getSource()))
            )
            .then(Commands.literal("members")
                .executes(context -> partyMembers(context.getSource()))
            )
            .then(Commands.literal("dissolve")
                .executes(context -> partyDissolve(context.getSource()))
            )
            .then(Commands.literal("info")
                .executes(context -> partyInfo(context.getSource()))
            );
    }

    private static int partyCreate(CommandSourceStack source, String password, String alias) {
        ServerPlayer player = (ServerPlayer) source.getEntity();
        if (player == null) {
            return 0;
        }

        // Validate password
        if (password.length() < MIN_PASSWORD_LENGTH) {
            source.sendFailure(Component.literal("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.")
                .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            source.sendFailure(Component.literal("Password must be at most " + MAX_PASSWORD_LENGTH + " characters.")
                .withStyle(ChatFormatting.RED));
            return 0;
        }

        // Check if player is already in a party
        if (PartyStore.isInParty(player)) {
            source.sendFailure(Component.literal("You are already in a party. Leave your current party first.")
                .withStyle(ChatFormatting.RED));
            return 0;
        }

        String normalizedAlias = alias != null ? alias.trim() : null;
        if (normalizedAlias != null && normalizedAlias.length() > 24) {
            source.sendFailure(Component.literal("Party alias must be at most 24 characters.")
                .withStyle(ChatFormatting.RED));
            return 0;
        }

        // Create the party
        UUID partyId = PartyStore.createParty(player, password, normalizedAlias);
        String shortCode = PartyStore.getPartyShortCode(player, partyId);
        String partyAlias = PartyStore.getPartyAlias(player, partyId);
        
        source.sendSuccess(
            () -> Component.literal("Party created! Code: " + shortCode)
                .withStyle(ChatFormatting.GREEN),
            false
        );

        if (partyAlias != null) {
            source.sendSuccess(
                () -> Component.literal("Alias: " + partyAlias)
                    .withStyle(ChatFormatting.AQUA),
                false
            );
        }

        source.sendSuccess(
            () -> Component.literal("Internal ID: " + partyId)
                .withStyle(ChatFormatting.GRAY),
            false
        );
        
        source.sendSuccess(
            () -> Component.literal("Share this with others: /dp party join " + shortCode + " <password>")
                .withStyle(ChatFormatting.AQUA),
            false
        );

        source.sendSuccess(
            () -> Component.literal("UUID also works: /dp party join " + partyId + " <password>")
                .withStyle(ChatFormatting.GRAY),
            false
        );

        return 1;
    }

    private static int partyJoin(CommandSourceStack source, String partyKey, String password) {
        ServerPlayer player = (ServerPlayer) source.getEntity();
        if (player == null) {
            return 0;
        }

        // Check if player is already in a party
        if (PartyStore.isInParty(player)) {
            source.sendFailure(Component.literal("You are already in a party. Leave your current party first.")
                .withStyle(ChatFormatting.RED));
            return 0;
        }

        PartyData partyData = PartyData.get(player.level());
        UUID partyId = PartyStore.resolvePartyId(player, partyKey);

        // Check if party exists
        if (partyId == null || !partyData.partyExists(partyId)) {
            source.sendFailure(Component.literal("Party not found. Use a valid UUID or party code.")
                .withStyle(ChatFormatting.RED));
            return 0;
        }

        // Check party size limit
        Set<UUID> members = partyData.getPartyMembers(partyId);
        if (members.size() >= MAX_PARTY_SIZE) {
            source.sendFailure(Component.literal("Party is full (max " + MAX_PARTY_SIZE + " members).")
                .withStyle(ChatFormatting.RED));
            return 0;
        }

        // Try to join the party
        if (PartyStore.joinParty(player, partyId, password)) {
            source.sendSuccess(
                () -> Component.literal("Successfully joined the party!")
                    .withStyle(ChatFormatting.GREEN),
                false
            );
            String shortCode = partyData.getPartyShortCode(partyId);
            String partyAlias = partyData.getPartyAlias(partyId);
            ProgressNotifier.partyJoined(player, partyId, shortCode, partyAlias);
            return 1;
        } else {
            source.sendFailure(Component.literal("Failed to join party. Party not found or incorrect password.")
                .withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int partyLeave(CommandSourceStack source) {
        ServerPlayer player = (ServerPlayer) source.getEntity();
        if (player == null) {
            return 0;
        }

        // Check if player is in a party
        if (!PartyStore.isInParty(player)) {
            source.sendFailure(Component.literal("You are not in a party.")
                .withStyle(ChatFormatting.RED));
            return 0;
        }

        PartyStore.leaveParty(player);
        source.sendSuccess(
            () -> Component.literal("Left the party.")
                .withStyle(ChatFormatting.GREEN),
            false
        );
        ProgressNotifier.partyLeft(player);

        return 1;
    }

    private static int partyMembers(CommandSourceStack source) {
        ServerPlayer player = (ServerPlayer) source.getEntity();
        if (player == null) {
            return 0;
        }

        UUID partyId = PartyStore.getPlayerParty(player);
        if (partyId == null) {
            source.sendFailure(Component.literal("You are not in a party.")
                .withStyle(ChatFormatting.RED));
            return 0;
        }

        Level level = player.level();
        Set<UUID> members = PartyData.get(level).getPartyMembers(partyId);
        
        source.sendSuccess(
            () -> Component.literal("Party Members (" + members.size() + "):").withStyle(ChatFormatting.GOLD),
            false
        );

        for (UUID memberId : members) {
            final String memberName;
            var memberPlayer = level.getServer().getPlayerList().getPlayer(memberId);
            if (memberPlayer != null) {
                memberName = memberPlayer.getName().getString() + " (online)";
            } else {
                memberName = "Unknown (UUID: " + memberId + ")";
            }

            final String displayName = memberName;
            source.sendSuccess(() -> Component.literal("  • " + displayName), false);
        }

        return 1;
    }

    private static int partyDissolve(CommandSourceStack source) {
        ServerPlayer player = (ServerPlayer) source.getEntity();
        if (player == null) {
            return 0;
        }

        UUID partyId = PartyStore.getPlayerParty(player);
        if (partyId == null) {
            source.sendFailure(Component.literal("You are not in a party.")
                .withStyle(ChatFormatting.RED));
            return 0;
        }

        // Check if player is the party creator
        PartyData partyData = PartyData.get(player.level());
        UUID creator = partyData.getPartyCreator(partyId);
        if (!creator.equals(player.getUUID())) {
            source.sendFailure(Component.literal("Only the party creator can dissolve the party.")
                .withStyle(ChatFormatting.RED));
            return 0;
        }

        // Dissolve the party
        partyData.dissolveParty(partyId, player.getUUID());
        source.sendSuccess(
            () -> Component.literal("Party dissolved.")
                .withStyle(ChatFormatting.GREEN),
            false
        );

        return 1;
    }

    private static int partyInfo(CommandSourceStack source) {
        ServerPlayer player = (ServerPlayer) source.getEntity();
        if (player == null) {
            return 0;
        }

        UUID partyId = PartyStore.getPlayerParty(player);
        if (partyId == null) {
            source.sendFailure(Component.literal("You are not in a party.")
                .withStyle(ChatFormatting.RED));
            return 0;
        }

        PartyData partyData = PartyData.get(player.level());
        Set<UUID> members = partyData.getPartyMembers(partyId);
        UUID creator = partyData.getPartyCreator(partyId);
        String shortCode = partyData.getPartyShortCode(partyId);
        String partyAlias = partyData.getPartyAlias(partyId);

        final String creatorName;
        var onlineCreator = player.server.getPlayerList().getPlayer(creator);
        if (onlineCreator != null) {
            creatorName = onlineCreator.getName().getString();
        } else {
            creatorName = player.server.getProfileCache().get(creator)
                .map(profile -> profile.getName())
                .orElse("Unknown");
        }

        source.sendSuccess(
            () -> Component.literal("Party Information").withStyle(ChatFormatting.GOLD),
            false
        );

        if (partyAlias != null) {
            source.sendSuccess(
                () -> Component.literal("Alias: " + partyAlias),
                false
            );
        }
        
        source.sendSuccess(
            () -> Component.literal("Party Code: " + shortCode),
            false
        );

        source.sendSuccess(
            () -> Component.literal("Internal ID: " + partyId)
                .withStyle(ChatFormatting.GRAY),
            false
        );
        
        source.sendSuccess(
            () -> Component.literal("Members: " + members.size()),
            false
        );

        String isCreator = creator.equals(player.getUUID()) ? " (you)" : "";
        source.sendSuccess(
            () -> Component.literal("Creator: " + creatorName + " (UUID: " + creator + ")" + isCreator),
            false
        );

        return 1;
    }
}
