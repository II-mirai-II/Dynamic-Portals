package com.mirai.dynamicportals.advancement;

import com.mirai.dynamicportals.util.ModConstants;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTriggers {
    private static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, ModConstants.MOD_ID);

    public static final DeferredHolder<CriterionTrigger<?>, KillRequirementTrigger> KILL_REQUIREMENT =
            TRIGGERS.register("kill_requirement", KillRequirementTrigger::new);

    public static void register(IEventBus eventBus) {
        TRIGGERS.register(eventBus);
    }
}
