package net.mirai.dynamicportals.chat;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.mirai.dynamicportals.config.DynamicPortalsConfig;
import net.mirai.dynamicportals.party.PartyData;
import net.mirai.dynamicportals.party.PartyStore;
import net.mirai.dynamicportals.util.DisplayText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class ProgressNotifier {
    private static final long PARTY_BROADCAST_DEDUPE_MS = 1000L;
    private static final Map<String, Long> RECENT_PARTY_COMPLETIONS = new ConcurrentHashMap<>();

    private ProgressNotifier() {
    }

    public static void progress(ServerPlayer player, String type, String targetId, int current, int required) {
        if (!DynamicPortalsConfig.CONFIG.enableChatProgress.get()) {
            return;
        }

        Component message = Component.translatable(
            "dynamicportals.chat.progress",
            typeLabel(type),
            prettyName(targetId),
            current,
            required
        )
            .withStyle(ChatFormatting.YELLOW);
        player.sendSystemMessage(message);
    }

    public static void requirementCompleted(ServerPlayer player, String dimension, String type, String targetId) {
        if (!DynamicPortalsConfig.CONFIG.enableChatProgress.get()) {
            return;
        }

        UUID partyId = PartyStore.getPlayerParty(player);
        if (partyId == null) {
            player.sendSystemMessage(Component.translatable(
                "dynamicportals.chat.requirement_completed",
                typeLabel(type),
                prettyName(targetId)
            ).withStyle(ChatFormatting.GREEN));
            playSuccess(player);
            return;
        }

        String dedupeKey = player.getUUID() + "|" + dimension + "|" + type + "|" + targetId;
        long now = System.currentTimeMillis();
        Long lastSent = RECENT_PARTY_COMPLETIONS.get(dedupeKey);
        if (lastSent != null && now - lastSent < PARTY_BROADCAST_DEDUPE_MS) {
            return;
        }
        RECENT_PARTY_COMPLETIONS.put(dedupeKey, now);

        PartyData partyData = PartyData.get(player.level());
        Set<UUID> members = partyData.getPartyMembers(partyId);
        Component partyMessage = Component.translatable(
            "dynamicportals.chat.requirement_completed_party",
            player.getName(),
            typeLabel(type),
            prettyName(targetId)
        ).withStyle(ChatFormatting.GREEN);

        for (UUID memberId : members) {
            ServerPlayer member = player.server.getPlayerList().getPlayer(memberId);
            if (member != null) {
                member.sendSystemMessage(partyMessage);
            }
        }

        playSuccess(player);
    }

    public static void portalUnlocked(ServerPlayer player, String dimension) {
        if (!DynamicPortalsConfig.CONFIG.enableChatProgress.get()) {
            return;
        }

        player.sendSystemMessage(Component.translatable("dynamicportals.chat.portal_unlocked", DisplayText.dimension(dimension))
            .withStyle(ChatFormatting.GOLD));

        PlayerList playerList = player.server.getPlayerList();
        playerList.broadcastSystemMessage(
            Component.translatable("dynamicportals.chat.portal_unlocked_broadcast", player.getName(), DisplayText.dimension(dimension))
                .withStyle(ChatFormatting.AQUA),
            false
        );

        playSuccess(player);
    }

    public static void portalBlocked(ServerPlayer player, String dimension) {
        if (!DynamicPortalsConfig.CONFIG.enableChatProgress.get()) {
            return;
        }

        player.sendSystemMessage(Component.translatable("dynamicportals.chat.portal_blocked", DisplayText.dimension(dimension), dimension)
            .withStyle(ChatFormatting.RED));
    }

    public static void bypassUnlocked(ServerPlayer player, String dimension, String itemId) {
        if (!DynamicPortalsConfig.CONFIG.enableChatProgress.get()) {
            return;
        }

        player.sendSystemMessage(Component.translatable("dynamicportals.chat.bypass", prettyName(itemId), DisplayText.dimension(dimension))
            .withStyle(ChatFormatting.AQUA));
        playSuccess(player);
    }

    public static void partyUnlocked(ServerPlayer player, String dimension) {
        if (!DynamicPortalsConfig.CONFIG.enableChatProgress.get()) {
            return;
        }

        player.sendSystemMessage(Component.literal("✓ Your party has unlocked ")
            .append(Component.literal(DisplayText.dimension(dimension)).withStyle(ChatFormatting.YELLOW))
            .withStyle(ChatFormatting.GREEN)
        );
        playSuccess(player);
    }

    public static void partyJoined(ServerPlayer player, java.util.UUID partyId, String shortCode, String alias) {
        if (!DynamicPortalsConfig.CONFIG.enableChatProgress.get()) {
            return;
        }

        MutableComponent idComponent = Component.literal(shortCode != null ? shortCode : partyId.toString())
            .withStyle(ChatFormatting.BLUE);

        MutableComponent message = Component.literal("✓ Joined party ").append(idComponent);
        if (alias != null) {
            message = message.append(Component.literal(" [" + alias + "]").withStyle(ChatFormatting.AQUA));
        }

        player.sendSystemMessage(message
            .withStyle(ChatFormatting.GREEN)
        );
    }

    public static void partyLeft(ServerPlayer player) {
        if (!DynamicPortalsConfig.CONFIG.enableChatProgress.get()) {
            return;
        }

        player.sendSystemMessage(Component.literal("✓ Left your party")
            .withStyle(ChatFormatting.YELLOW)
        );
    }

    private static void playSuccess(ServerPlayer player) {
        if (!DynamicPortalsConfig.CONFIG.enableSuccessSound.get()) {
            return;
        }
        player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.2F);
    }

    private static Component typeLabel(String rawType) {
        return switch (rawType) {
            case "kill" -> Component.translatable("dynamicportals.type.kill");
            case "item" -> Component.translatable("dynamicportals.type.item");
            case "adv" -> Component.translatable("dynamicportals.type.adv");
            default -> Component.literal(rawType);
        };
    }

    private static String prettyName(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) {
            return humanize(id);
        }
        return humanize(rl.getPath());
    }

    private static String humanize(String raw) {
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
        return builder.isEmpty() ? raw : builder.toString();
    }
}
