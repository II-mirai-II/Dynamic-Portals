package net.mirai.dynamicportals.hub;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.mirai.dynamicportals.config.PortalDefinition;
import net.mirai.dynamicportals.config.PortalRules;
import net.mirai.dynamicportals.party.PartyData;
import net.mirai.dynamicportals.party.PartyStore;
import net.mirai.dynamicportals.requirements.RequirementEngine;
import net.mirai.dynamicportals.util.DisplayText;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;

public final class HubSnapshotBuilder {
    private static final int PREVIEW_LIMIT = 3;

    private HubSnapshotBuilder() {
    }

    public static CompoundTag build(ServerPlayer player, boolean success, String message) {
        CompoundTag root = new CompoundTag();
        root.putBoolean("success", success);
        root.putString("message", message == null ? "" : message);
        root.putLong("timestamp", System.currentTimeMillis());

        root.put("progress", buildProgress(player));
        root.put("party", buildParty(player));
        return root;
    }

    private static CompoundTag buildProgress(ServerPlayer player) {
        CompoundTag progress = new CompoundTag();
        boolean inParty = PartyStore.isInParty(player);
        progress.putBoolean("in_party", inParty);

        List<PortalHubState> states = new ArrayList<>();
        for (PortalDefinition definition : PortalRules.all()) {
            RequirementEngine.PortalStatus individualStatus = RequirementEngine.evaluate(player, definition);
            RequirementEngine.PortalStatus partyStatus = inParty
                ? RequirementEngine.evaluateParty(player, definition)
                : individualStatus;
            RequirementEngine.PortalStatus effectiveStatus = inParty ? partyStatus : individualStatus;

            PortalVisualState visualState = classifyVisualState(inParty, individualStatus, partyStatus);

            int total = definition.killRequirements().size()
                + definition.itemRequirements().size()
                + definition.advancementRequirements().size();
            int missing = effectiveStatus.missingEntries().size();
            int completed = Math.max(0, total - missing);
            int percent = total == 0 ? 100 : (int) Math.floor((completed * 100.0D) / total);
            boolean bypassBanner = individualStatus.bypassUnlocked() && !individualStatus.missingEntries().isEmpty();

            states.add(new PortalHubState(
                definition,
                visualState,
                total,
                completed,
                percent,
                missing,
                buildMissingPreview(effectiveStatus.missingEntries()),
                bypassBanner,
                individualStatus,
                partyStatus,
                effectiveStatus
            ));
        }

        states.sort(Comparator
            .comparingInt((PortalHubState state) -> state.effectiveStatus().unlocked() ? 1 : 0)
            .thenComparingInt(state -> state.effectiveStatus().missingEntries().size())
            .thenComparing(state -> state.definition().destinationDimension())
        );

        ListTag portalList = new ListTag();
        for (PortalHubState state : states) {
            CompoundTag portalTag = new CompoundTag();
            portalTag.putString("dimension", state.definition().destinationDimension());
            portalTag.putString("display_name", state.definition().displayName());
            portalTag.putString("status_key", state.visualState().statusTranslationKey());
            portalTag.putInt("status_color", state.visualState().colorArgb());
            portalTag.putInt("total", state.total());
            portalTag.putInt("completed", state.completed());
            portalTag.putInt("percent", state.percent());
            portalTag.putInt("missing_count", state.missingCount());
            portalTag.putString("missing_preview", state.missingPreview());
            portalTag.putBoolean("bypass_banner", state.showBypassBanner());
            portalTag.putBoolean("in_party", inParty);
            portalTag.putBoolean("individual_unlocked", state.individualStatus().unlocked());
            portalTag.putBoolean("party_unlocked", state.partyStatus().unlocked());
            portalTag.putBoolean("effective_unlocked", state.effectiveStatus().unlocked());
            portalTag.putBoolean("bypass_unlocked", state.individualStatus().bypassUnlocked());

            ListTag requirementEntries = buildRequirementEntries(state.definition(), state.effectiveStatus().missingEntries());
            portalTag.put("requirement_entries", requirementEntries);

            ListTag missingEntries = new ListTag();
            for (String raw : state.effectiveStatus().missingEntries()) {
                missingEntries.add(StringTag.valueOf(humanizeMissingEntry(raw)));
            }
            portalTag.put("missing_entries", missingEntries);

            portalList.add(portalTag);
        }

        progress.put("portals", portalList);
        return progress;
    }

    private static ListTag buildRequirementEntries(PortalDefinition definition, List<String> missingEntries) {
        Map<String, Integer> missingCounters = new HashMap<>();
        for (String raw : missingEntries) {
            String[] parts = raw.split("\\|", 4);
            if (parts.length != 4) {
                continue;
            }

            try {
                int current = Integer.parseInt(parts[2]);
                missingCounters.put(parts[0] + "|" + parts[1], current);
            } catch (NumberFormatException ignored) {
            }
        }

        ListTag entries = new ListTag();

        for (Map.Entry<String, Integer> kill : definition.killRequirements().entrySet()) {
            entries.add(buildRequirementTag(
                "dynamicportals.type.kill",
                kill.getKey(),
                kill.getValue(),
                missingCounters
            ));
        }

        for (Map.Entry<String, Integer> item : definition.itemRequirements().entrySet()) {
            entries.add(buildRequirementTag(
                "dynamicportals.type.item",
                item.getKey(),
                item.getValue(),
                missingCounters
            ));
        }

        for (String advancement : definition.advancementRequirements()) {
            entries.add(buildRequirementTag(
                "dynamicportals.type.adv",
                advancement,
                1,
                missingCounters
            ));
        }

        return entries;
    }

    private static CompoundTag buildRequirementTag(
        String typeTranslationKey,
        String targetId,
        int required,
        Map<String, Integer> missingCounters
    ) {
        String typeRaw = switch (typeTranslationKey) {
            case "dynamicportals.type.kill" -> "kill";
            case "dynamicportals.type.item" -> "item";
            case "dynamicportals.type.adv" -> "adv";
            default -> "unknown";
        };

        int current = missingCounters.getOrDefault(typeRaw + "|" + targetId, required);
        current = Math.max(0, Math.min(current, required));
        boolean completed = current >= required;

        CompoundTag tag = new CompoundTag();
        tag.putString("type_key", typeTranslationKey);
        tag.putString("target_id", targetId);
        tag.putString("target_name", DisplayText.resource(targetId));
        tag.putInt("current", current);
        tag.putInt("required", required);
        tag.putBoolean("completed", completed);
        return tag;
    }

    private static CompoundTag buildParty(ServerPlayer player) {
        CompoundTag party = new CompoundTag();
        UUID partyId = PartyStore.getPlayerParty(player);
        boolean inParty = partyId != null;
        party.putBoolean("in_party", inParty);

        if (!inParty) {
            return party;
        }

        PartyData partyData = PartyData.get(player.level());
        party.putUUID("party_id", partyId);

        String shortCode = partyData.getPartyShortCode(partyId);
        if (shortCode != null) {
            party.putString("short_code", shortCode);
        }

        String alias = partyData.getPartyAlias(partyId);
        if (alias != null) {
            party.putString("alias", alias);
        }

        UUID creator = partyData.getPartyCreator(partyId);
        if (creator != null) {
            party.putUUID("creator", creator);
            party.putBoolean("is_creator", creator.equals(player.getUUID()));
        }

        Set<UUID> memberIds = partyData.getPartyMembers(partyId);
        party.putInt("member_count", memberIds.size());

        List<MemberView> memberViews = new ArrayList<>();
        for (UUID memberId : memberIds) {
            ServerPlayer onlinePlayer = player.server.getPlayerList().getPlayer(memberId);
            boolean online = onlinePlayer != null;
            String name = online
                ? onlinePlayer.getGameProfile().getName()
                : player.server.getProfileCache().get(memberId).map(profile -> profile.getName()).orElse("Unknown");

            memberViews.add(new MemberView(
                memberId,
                name,
                online,
                memberId.equals(player.getUUID()),
                creator != null && creator.equals(memberId)
            ));
        }

        memberViews.sort(Comparator
            .comparingInt((MemberView member) -> member.online() ? 0 : 1)
            .thenComparing(member -> member.name().toLowerCase(Locale.ROOT))
        );

        ListTag membersTag = new ListTag();
        for (MemberView member : memberViews) {
            CompoundTag memberTag = new CompoundTag();
            memberTag.putUUID("uuid", member.id());
            memberTag.putString("name", member.name());
            memberTag.putBoolean("online", member.online());
            memberTag.putBoolean("is_self", member.isSelf());
            memberTag.putBoolean("is_creator", member.isCreator());
            membersTag.add(memberTag);
        }
        party.put("members", membersTag);

        return party;
    }

    private static PortalVisualState classifyVisualState(
        boolean inParty,
        RequirementEngine.PortalStatus individualStatus,
        RequirementEngine.PortalStatus partyStatus
    ) {
        boolean individualCompleted = individualStatus.unlocked() && !individualStatus.bypassUnlocked();
        boolean partyCompleted = inParty && partyStatus.unlocked() && !partyStatus.bypassUnlocked() && !individualCompleted;
        boolean bypassUnlocked = individualStatus.bypassUnlocked();

        if (individualCompleted) {
            return new PortalVisualState("dynamicportals.command.check.status.unlocked_individual", 0xFF55FF55);
        }
        if (partyCompleted) {
            return new PortalVisualState("dynamicportals.command.check.status.unlocked_party", 0xFF5555FF);
        }
        if (bypassUnlocked) {
            return new PortalVisualState("dynamicportals.command.check.status.unlocked_bypass", 0xFF55FFFF);
        }
        if (partyStatus.unlocked()) {
            return new PortalVisualState("dynamicportals.command.check.status.unlocked_by_party", 0xFF3F3FFF);
        }
        return new PortalVisualState("dynamicportals.command.check.status.blocked", 0xFFFF5555);
    }

    private static String buildMissingPreview(List<String> missingEntries) {
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (String raw : missingEntries) {
            if (count >= PREVIEW_LIMIT) {
                break;
            }

            String[] parts = raw.split("\\|", 4);
            if (parts.length < 2) {
                continue;
            }

            if (!builder.isEmpty()) {
                builder.append(", ");
            }
            builder.append(DisplayText.resource(parts[1]));
            count++;
        }
        return builder.toString();
    }

    private static String humanizeMissingEntry(String raw) {
        String[] parts = raw.split("\\|", 4);
        if (parts.length != 4) {
            return raw;
        }

        String type = switch (parts[0]) {
            case "kill" -> "Kill";
            case "item" -> "Item";
            case "adv" -> "Advancement";
            default -> parts[0];
        };
        return type + ": " + DisplayText.resource(parts[1]) + " " + parts[2] + "/" + parts[3];
    }

    private record PortalVisualState(String statusTranslationKey, int colorArgb) {
    }

    private record PortalHubState(
        PortalDefinition definition,
        PortalVisualState visualState,
        int total,
        int completed,
        int percent,
        int missingCount,
        String missingPreview,
        boolean showBypassBanner,
        RequirementEngine.PortalStatus individualStatus,
        RequirementEngine.PortalStatus partyStatus,
        RequirementEngine.PortalStatus effectiveStatus
    ) {
    }

    private record MemberView(UUID id, String name, boolean online, boolean isSelf, boolean isCreator) {
    }
}
