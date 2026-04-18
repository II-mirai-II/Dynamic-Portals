package net.mirai.dynamicportals.network;

import io.netty.buffer.ByteBuf;
import net.mirai.dynamicportals.DynamicPortals;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PortalOverlayPayload(String translationKey, String arg0, String arg1, int durationTicks)
    implements CustomPacketPayload {
    public static final Type<PortalOverlayPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(DynamicPortals.MOD_ID, "portal_overlay")
    );

    public static final StreamCodec<ByteBuf, PortalOverlayPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        PortalOverlayPayload::translationKey,
        ByteBufCodecs.STRING_UTF8,
        PortalOverlayPayload::arg0,
        ByteBufCodecs.STRING_UTF8,
        PortalOverlayPayload::arg1,
        ByteBufCodecs.VAR_INT,
        PortalOverlayPayload::durationTicks,
        PortalOverlayPayload::new
    );

    public PortalOverlayPayload {
        translationKey = safe(translationKey);
        arg0 = safe(arg0);
        arg1 = safe(arg1);
        durationTicks = Math.max(20, durationTicks);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
