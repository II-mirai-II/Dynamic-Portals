package net.mirai.dynamicportals.network;

import io.netty.buffer.ByteBuf;
import net.mirai.dynamicportals.DynamicPortals;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HubStatePayload(CompoundTag state) implements CustomPacketPayload {
    public static final Type<HubStatePayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(DynamicPortals.MOD_ID, "hub_state")
    );

    public static final StreamCodec<ByteBuf, HubStatePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.COMPOUND_TAG,
        HubStatePayload::state,
        HubStatePayload::new
    );

    public HubStatePayload {
        state = state == null ? new CompoundTag() : state;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
