package net.mirai.dynamicportals.item;

import net.mirai.dynamicportals.DynamicPortals;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, DynamicPortals.MOD_ID);

    public static final DeferredHolder<Item, Item> TESTER_WOODEN_SWORD = ITEMS.register(
        "tester_wooden_sword",
        () -> new TesterWoodenSwordItem(new Item.Properties().stacksTo(1).durability(2048))
    );

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
