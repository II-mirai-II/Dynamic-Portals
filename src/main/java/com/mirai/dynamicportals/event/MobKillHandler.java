package com.mirai.dynamicportals.event;

import com.mirai.dynamicportals.advancement.ModTriggers;
import com.mirai.dynamicportals.data.ModAttachments;
import com.mirai.dynamicportals.data.PlayerProgressData;
import com.mirai.dynamicportals.network.SyncProgressPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles mob kill tracking with assist system.
 * Tracks damage dealt to mobs within a 5-second window to credit kills to multiple players.
 */
public class MobKillHandler {
    
    // Track damage sources for assist system (last 5 seconds)
    private static final Map<LivingEntity, AssistTracker> DAMAGE_TRACKERS = new ConcurrentHashMap<>();
    private static final int CLEANUP_INTERVAL_TICKS = 200; // 10 seconds
    private static final long ASSIST_WINDOW_MS = 5000L; // 5 seconds in milliseconds
    private static int tickCounter = 0;
    
    /**
     * Get assist window in milliseconds (fixed at 5 seconds)
     */
    private static long getAssistWindowMs() {
        return ASSIST_WINDOW_MS;
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        // Periodically cleanup old damage trackers to prevent memory leak
        tickCounter++;
        if (tickCounter >= CLEANUP_INTERVAL_TICKS) {
            cleanupOldTrackers();
            tickCounter = 0;
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        // Clear all trackers on server shutdown
        DAMAGE_TRACKERS.clear();
        tickCounter = 0;
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        
        if (target.level().isClientSide()) {
            return;
        }

        // Track player damage for assist system
        if (source.getEntity() instanceof Player player) {
            AssistTracker tracker = DAMAGE_TRACKERS.computeIfAbsent(target, k -> new AssistTracker());
            tracker.addDamager(player);
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        
        if (entity.level().isClientSide()) {
            return;
        }

        // Get all players who contributed to this kill (killer + assists)
        Set<ServerPlayer> contributors = new HashSet<>();
        
        // Add direct killer if it's a player
        if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            contributors.add(killer);
        }

        // Add assists from damage tracker
        AssistTracker tracker = DAMAGE_TRACKERS.remove(entity);
        if (tracker != null) {
            for (Player player : tracker.getRecentDamagers()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    contributors.add(serverPlayer);
                }
            }
        }

        // Award kill credit to all contributors
        EntityType<?> entityType = entity.getType();
        for (ServerPlayer player : contributors) {
            PlayerProgressData progressData = player.getData(ModAttachments.PLAYER_PROGRESS);
            
            if (!progressData.hasMobBeenKilled(entityType)) {
                progressData.markMobKilled(entityType);
                
                // Trigger advancement check
                ModTriggers.KILL_REQUIREMENT.get().trigger(player);
                
                // Check if player completed any portal requirements
                com.mirai.dynamicportals.util.PortalProgressUtils.checkAndUnlockPortals(player, progressData);
                
                // Sync to client
                PacketDistributor.sendToPlayer(player, SyncProgressPacket.fromProgressData(progressData));
            }
        }
    }

    // Clean up old trackers periodically
    public static void cleanupOldTrackers() {
        long currentTime = System.currentTimeMillis();
        DAMAGE_TRACKERS.entrySet().removeIf(entry -> 
            !entry.getKey().isAlive() || entry.getValue().isExpired(currentTime)
        );
    }

    private static class AssistTracker {
        private final Map<Player, Long> damagers = new HashMap<>();

        public void addDamager(Player player) {
            damagers.put(player, System.currentTimeMillis());
        }

        public List<Player> getRecentDamagers() {
            long currentTime = System.currentTimeMillis();
            long assistWindow = getAssistWindowMs();
            List<Player> recent = new ArrayList<>();
            
            for (Map.Entry<Player, Long> entry : damagers.entrySet()) {
                if (currentTime - entry.getValue() <= assistWindow) {
                    recent.add(entry.getKey());
                }
            }
            
            return recent;
        }

        public boolean isExpired(long currentTime) {
            long assistWindow = getAssistWindowMs();
            return damagers.values().stream()
                    .allMatch(time -> currentTime - time > assistWindow);
        }
    }
}
