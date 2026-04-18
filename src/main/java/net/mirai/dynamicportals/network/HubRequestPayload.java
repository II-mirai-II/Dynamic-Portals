package net.mirai.dynamicportals.network;

import io.netty.buffer.ByteBuf;
import net.mirai.dynamicportals.DynamicPortals;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HubRequestPayload(String action, String arg0, String arg1) implements CustomPacketPayload {
    public static final Type<HubRequestPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(DynamicPortals.MOD_ID, "hub_request")
    );

    public static final StreamCodec<ByteBuf, HubRequestPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        HubRequestPayload::action,
        ByteBufCodecs.STRING_UTF8,
        HubRequestPayload::arg0,
        ByteBufCodecs.STRING_UTF8,
        HubRequestPayload::arg1,
        HubRequestPayload::new
    );

    public HubRequestPayload {
        action = safe(action);
        arg0 = safe(arg0);
        arg1 = safe(arg1);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
