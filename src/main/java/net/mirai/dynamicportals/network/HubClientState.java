package net.mirai.dynamicportals.network;

import net.minecraft.nbt.CompoundTag;

public final class HubClientState {
    private static volatile CompoundTag latestState = new CompoundTag();

    private HubClientState() {
    }

    public static void apply(HubStatePayload payload) {
        CompoundTag incoming = payload.state();
        latestState = incoming == null ? new CompoundTag() : incoming.copy();
    }

    public static CompoundTag snapshot() {
        return latestState.copy();
    }
}
