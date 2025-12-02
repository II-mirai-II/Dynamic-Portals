package com.mirai.dynamicportals.event;

import com.mirai.dynamicportals.advancement.ModTriggers;
import com.mirai.dynamicportals.data.ModAttachments;
import com.mirai.dynamicportals.data.PlayerProgressData;
import com.mirai.dynamicportals.network.SyncProgressPacket;
import com.mirai.dynamicportals.util.ModConstants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MobKillHandler {
    
    // Track damage sources for assist system (last 5 seconds)
    private static final Map<LivingEntity, AssistTracker> DAMAGE_TRACKERS = new ConcurrentHashMap<>();
    private static final long ASSIST_WINDOW_MS = ModConstants.ASSIST_TIME_WINDOW_SECONDS * 1000L;

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
            List<Player> recent = new ArrayList<>();
            
            for (Map.Entry<Player, Long> entry : damagers.entrySet()) {
                if (currentTime - entry.getValue() <= ASSIST_WINDOW_MS) {
                    recent.add(entry.getKey());
                }
            }
            
            return recent;
        }

        public boolean isExpired(long currentTime) {
            return damagers.values().stream()
                    .allMatch(time -> currentTime - time > ASSIST_WINDOW_MS);
        }
    }
}
