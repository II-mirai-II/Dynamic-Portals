package net.mirai.dynamicportals;

import com.mojang.logging.LogUtils;
import net.mirai.dynamicportals.command.ModCommands;
import net.mirai.dynamicportals.config.DynamicPortalsConfig;
import net.mirai.dynamicportals.config.PortalRules;
import net.mirai.dynamicportals.events.ProgressEvents;
import net.mirai.dynamicportals.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(DynamicPortals.MOD_ID)
public class DynamicPortals {
    public static final String MOD_ID = "dynamicportals";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DynamicPortals(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, DynamicPortalsConfig.SPEC, "dynamicportals-common.toml");

        ModItems.register(modEventBus);
        modEventBus.addListener(PortalRules::onConfigLoading);
        modEventBus.addListener(PortalRules::onConfigReloading);

        NeoForge.EVENT_BUS.register(new ProgressEvents());
        NeoForge.EVENT_BUS.register(new ModCommands());
    }
}
