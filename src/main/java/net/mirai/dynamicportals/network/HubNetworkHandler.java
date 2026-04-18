package net.mirai.dynamicportals.network;

import java.util.Set;
import java.util.UUID;
import net.mirai.dynamicportals.chat.ProgressNotifier;
import net.mirai.dynamicportals.hub.HubSnapshotBuilder;
import net.mirai.dynamicportals.party.PartyData;
import net.mirai.dynamicportals.party.PartyStore;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class HubNetworkHandler {
    private static final int MIN_PASSWORD_LENGTH = 4;
    private static final int MAX_PASSWORD_LENGTH = 32;
    private static final int MAX_PARTY_SIZE = 8;

    private static final String ACTION_REFRESH = "refresh";
    private static final String ACTION_PARTY_CREATE = "party_create";
    private static final String ACTION_PARTY_JOIN = "party_join";
    private static final String ACTION_PARTY_LEAVE = "party_leave";
    private static final String ACTION_PARTY_DISSOLVE = "party_dissolve";

    private HubNetworkHandler() {
    }

    public static void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
            HubRequestPayload.TYPE,
            HubRequestPayload.STREAM_CODEC,
            HubNetworkHandler::handleRequestOnServer
        );

        registrar.playToClient(
            HubStatePayload.TYPE,
            HubStatePayload.STREAM_CODEC,
            HubNetworkHandler::handleStateOnClient
        );

        registrar.playToClient(
            PortalOverlayPayload.TYPE,
            PortalOverlayPayload.STREAM_CODEC,
            HubNetworkHandler::handlePortalOverlayOnClient
        );
    }

    private static void handleRequestOnServer(final HubRequestPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            ActionResult result = switch (payload.action()) {
                case ACTION_REFRESH -> ActionResult.success("");
                case ACTION_PARTY_CREATE -> handlePartyCreate(player, payload.arg0(), payload.arg1());
                case ACTION_PARTY_JOIN -> handlePartyJoin(player, payload.arg0(), payload.arg1());
                case ACTION_PARTY_LEAVE -> handlePartyLeave(player);
                case ACTION_PARTY_DISSOLVE -> handlePartyDissolve(player);
                default -> ActionResult.failure("Unknown hub action: " + payload.action());
            };

            PacketDistributor.sendToPlayer(player, new HubStatePayload(HubSnapshotBuilder.build(player, result.success(), result.message())));
        });
    }

    private static void handleStateOnClient(final HubStatePayload payload, final IPayloadContext context) {
        HubClientState.apply(payload);
    }

    private static void handlePortalOverlayOnClient(final PortalOverlayPayload payload, final IPayloadContext context) {
        PortalOverlayClientState.apply(payload);
    }

    private static ActionResult handlePartyCreate(ServerPlayer player, String password, String alias) {
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return ActionResult.failure("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return ActionResult.failure("Password must be at most " + MAX_PASSWORD_LENGTH + " characters.");
        }
        if (PartyStore.isInParty(player)) {
            return ActionResult.failure("You are already in a party.");
        }

        String normalizedAlias = normalizeAlias(alias);
        if (normalizedAlias != null && normalizedAlias.length() > 24) {
            return ActionResult.failure("Party alias must be at most 24 characters.");
        }

        UUID partyId = PartyStore.createParty(player, password, normalizedAlias);
        String shortCode = PartyStore.getPartyShortCode(player, partyId);
        String code = shortCode == null ? partyId.toString() : shortCode;
        return ActionResult.success("Party created. Code: " + code);
    }

    private static ActionResult handlePartyJoin(ServerPlayer player, String partyKey, String password) {
        if (PartyStore.isInParty(player)) {
            return ActionResult.failure("You are already in a party.");
        }

        PartyData partyData = PartyData.get(player.level());
        UUID partyId = PartyStore.resolvePartyId(player, partyKey);
        if (partyId == null || !partyData.partyExists(partyId)) {
            return ActionResult.failure("Party not found.");
        }

        Set<UUID> members = partyData.getPartyMembers(partyId);
        if (members.size() >= MAX_PARTY_SIZE) {
            return ActionResult.failure("Party is full (max " + MAX_PARTY_SIZE + ").");
        }

        if (!PartyStore.joinParty(player, partyId, password)) {
            return ActionResult.failure("Failed to join party. Incorrect password.");
        }

        ProgressNotifier.partyJoined(player, partyId, partyData.getPartyShortCode(partyId), partyData.getPartyAlias(partyId));
        return ActionResult.success("Joined party successfully.");
    }

    private static ActionResult handlePartyLeave(ServerPlayer player) {
        if (!PartyStore.isInParty(player)) {
            return ActionResult.failure("You are not in a party.");
        }

        PartyStore.leaveParty(player);
        ProgressNotifier.partyLeft(player);
        return ActionResult.success("Left the party.");
    }

    private static ActionResult handlePartyDissolve(ServerPlayer player) {
        UUID partyId = PartyStore.getPlayerParty(player);
        if (partyId == null) {
            return ActionResult.failure("You are not in a party.");
        }

        PartyData partyData = PartyData.get(player.level());
        UUID creator = partyData.getPartyCreator(partyId);
        if (creator == null || !creator.equals(player.getUUID())) {
            return ActionResult.failure("Only the party creator can dissolve the party.");
        }

        boolean dissolved = partyData.dissolveParty(partyId, player.getUUID());
        if (!dissolved) {
            return ActionResult.failure("Failed to dissolve the party.");
        }

        return ActionResult.success("Party dissolved.");
    }

    private static String normalizeAlias(String alias) {
        if (alias == null) {
            return null;
        }
        String normalized = alias.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public static void requestRefreshFromClient() {
        PacketDistributor.sendToServer(new HubRequestPayload(ACTION_REFRESH, "", ""));
    }

    public static void requestPartyCreateFromClient(String password, String alias) {
        PacketDistributor.sendToServer(new HubRequestPayload(ACTION_PARTY_CREATE, safe(password), safe(alias)));
    }

    public static void requestPartyJoinFromClient(String partyKey, String password) {
        PacketDistributor.sendToServer(new HubRequestPayload(ACTION_PARTY_JOIN, safe(partyKey), safe(password)));
    }

    public static void requestPartyLeaveFromClient() {
        PacketDistributor.sendToServer(new HubRequestPayload(ACTION_PARTY_LEAVE, "", ""));
    }

    public static void requestPartyDissolveFromClient() {
        PacketDistributor.sendToServer(new HubRequestPayload(ACTION_PARTY_DISSOLVE, "", ""));
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private record ActionResult(boolean success, String message) {
        private static ActionResult success(String message) {
            return new ActionResult(true, message == null ? "" : message);
        }

        private static ActionResult failure(String message) {
            return new ActionResult(false, message == null ? "" : message);
        }
    }
}
