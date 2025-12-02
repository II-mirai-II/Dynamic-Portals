package com.mirai.dynamicportals.data;

import com.mirai.dynamicportals.util.ModConstants;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ModConstants.MOD_ID);

    public static final Supplier<AttachmentType<PlayerProgressData>> PLAYER_PROGRESS =
            ATTACHMENT_TYPES.register("player_progress", () ->
                    AttachmentType.serializable(PlayerProgressData::new).build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
