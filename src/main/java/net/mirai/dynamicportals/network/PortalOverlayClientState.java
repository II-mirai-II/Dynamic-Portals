package net.mirai.dynamicportals.network;

public final class PortalOverlayClientState {
    private static volatile String translationKey = "";
    private static volatile String arg0 = "";
    private static volatile String arg1 = "";
    private static volatile int totalTicks = 0;
    private static volatile int remainingTicks = 0;

    private PortalOverlayClientState() {}

    public static void apply(PortalOverlayPayload payload) {
        translationKey = payload.translationKey();
        arg0 = payload.arg0();
        arg1 = payload.arg1();
        totalTicks = payload.durationTicks();
        remainingTicks = payload.durationTicks();
    }

    public static void tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    public static void clear() {
        remainingTicks = 0;
        totalTicks = 0;
        translationKey = "";
        arg0 = "";
        arg1 = "";
    }

    public static OverlaySnapshot snapshot() {
        if (remainingTicks <= 0 || translationKey.isEmpty()) {
            return null;
        }
        return new OverlaySnapshot(translationKey, arg0, arg1, totalTicks, remainingTicks);
    }

    public record OverlaySnapshot(String translationKey, String arg0, String arg1, int totalTicks, int remainingTicks) {}
}
