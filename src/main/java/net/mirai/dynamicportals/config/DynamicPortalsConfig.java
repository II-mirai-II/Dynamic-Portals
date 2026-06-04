package net.mirai.dynamicportals.config;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class DynamicPortalsConfig {
    public static final DynamicPortalsConfig CONFIG;
    public static final ModConfigSpec SPEC;

    public final ModConfigSpec.BooleanValue enableChatProgress;
    public final ModConfigSpec.BooleanValue enableSuccessSound;

    public final ModConfigSpec.ConfigValue<List<? extends String>> killRequirements;
    public final ModConfigSpec.ConfigValue<List<? extends String>> itemRequirements;
    public final ModConfigSpec.ConfigValue<List<? extends String>> advancementRequirements;
    public final ModConfigSpec.ConfigValue<List<? extends String>> consumeBypassItems;

    static {
        Pair<DynamicPortalsConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(DynamicPortalsConfig::new);
        CONFIG = pair.getLeft();
        SPEC = pair.getRight();
    }

    private DynamicPortalsConfig(ModConfigSpec.Builder builder) {
        builder.comment(
            "Dynamic Portals configuration.",
            "All gameplay rules are data-driven from this file.",
            "Players can freely edit requirements without recompiling the mod.",
            "Rules that reference missing entity/item ids stay inactive instead of breaking progression.",
            "This allows optional compatibility entries for other mods: install the mod and reload/restart to activate them.",
            "Format references:",
            "- Counter requirement: destination_dimension|target_id|count",
            "- Advancement requirement: destination_dimension|advancement_id",
            "- Bypass item: destination_dimension|item_id"
        );

        builder.comment(
            "General behavior and feedback settings."
        ).push("general");

        enableChatProgress = builder
            .comment(
                "Enable progress and unlock messages in player chat.",
                "Disable if you want a quieter experience."
            )
            .define("enableChatProgress", true);

        enableSuccessSound = builder
            .comment(
                "Play a success sound when a requirement is completed."
            )
            .define("enableSuccessSound", true);

        builder.pop();

        builder.comment(
            "Portal requirement rules.",
            "These keys are the source of truth in runtime and can be fully edited by players.",
            "Default values below are only initial seeds for first file generation.",
            "After file generation, runtime always follows the content present in TOML."
        );
        builder.push("requirements");

        killRequirements = builder
            .comment(
                "Mob/Boss kill requirements.",
                "Format: destination_dimension|entity_id|count",
                "Example: minecraft:the_nether|minecraft:zombie|10",
                "If entity_id is not registered in the loaded mod set, that line is ignored at runtime.",
                "You can keep optional modded mobs here; they activate when the providing mod is installed.",
                "Defaults include Nether and End progression sets from the project specification.",
                "Project default count: 23 lines (14 Nether + 9 End)."
            )
            .defineList("killRequirements", defaultKillRequirements(), DynamicPortalsConfig::isString);

        itemRequirements = builder
            .comment(
                "Optional item requirements.",
                "Format: destination_dimension|item_id|count",
                "Lines with namespace 'example' are illustrative and ignored on purpose.",
                "Replace or remove illustrative lines to activate your own rules.",
                "You may use items from any mod namespace.",
                "If item_id is not registered in the loaded mod set, that line is ignored at runtime."
            )
            .defineList("itemRequirements", defaultItemRequirements(), DynamicPortalsConfig::isString);

        advancementRequirements = builder
            .comment(
                "Optional advancement requirements.",
                "Format: destination_dimension|advancement_id",
                "Lines with namespace 'example' are illustrative and ignored on purpose.",
                "Replace or remove illustrative lines to activate your own rules.",
                "You may use advancements from any mod namespace."
            )
            .defineList("advancementRequirements", defaultAdvancementRequirements(), DynamicPortalsConfig::isString);

        consumeBypassItems = builder
            .comment(
                "Bypass items that unlock a destination portal for the consuming player.",
                "Format: destination_dimension|item_id",
                "Default: magma cream unlocks Nether, chorus fruit unlocks End.",
                "You can add modded items and custom destination dimensions here.",
                "If item_id is not registered in the loaded mod set, that line is ignored at runtime.",
                "Remove an entry here to disable that bypass path."
            )
            .defineList("consumeBypassItems", defaultConsumeBypassItems(), DynamicPortalsConfig::isString);

        builder.pop();
    }

    private static boolean isString(Object value) {
        return value instanceof String;
    }

    private static List<String> defaultKillRequirements() {
        return List.of(
            "minecraft:the_nether|minecraft:zombie|1",
            "minecraft:the_nether|minecraft:skeleton|1",
            "minecraft:the_nether|minecraft:spider|1",
            "minecraft:the_nether|minecraft:creeper|1",
            "minecraft:the_nether|minecraft:slime|1",
            "minecraft:the_nether|minecraft:witch|1",
            "minecraft:the_nether|minecraft:husk|1",
            "minecraft:the_nether|minecraft:pillager|1",
            "minecraft:the_nether|minecraft:vindicator|1",
            "minecraft:the_nether|minecraft:bogged|1",
            "minecraft:the_nether|minecraft:breeze|1",
            "minecraft:the_nether|minecraft:evoker|1",
            "minecraft:the_nether|minecraft:ravager|1",
            "minecraft:the_nether|minecraft:elder_guardian|1",
            "minecraft:the_end|minecraft:magma_cube|1",
            "minecraft:the_end|minecraft:blaze|1",
            "minecraft:the_end|minecraft:wither_skeleton|1",
            "minecraft:the_end|minecraft:piglin|1",
            "minecraft:the_end|minecraft:hoglin|1",
            "minecraft:the_end|minecraft:piglin_brute|1",
            "minecraft:the_end|minecraft:ghast|1",
            "minecraft:the_end|minecraft:wither|1",
            "minecraft:the_end|minecraft:warden|1"
        );
    }

    private static List<String> defaultItemRequirements() {
        return List.of(
            "minecraft:the_nether|example:disabled_item|1",
            "minecraft:the_end|example:disabled_item|1"
        );
    }

    private static List<String> defaultAdvancementRequirements() {
        return List.of(
            "minecraft:the_nether|example:disabled_advancement",
            "minecraft:the_end|example:disabled_advancement"
        );
    }

    private static List<String> defaultConsumeBypassItems() {
        return List.of(
            "minecraft:the_nether|minecraft:magma_cream",
            "minecraft:the_end|minecraft:chorus_fruit"
        );
    }
}
