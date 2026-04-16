Title: Built-In Data Maps | NeoForged docs

URL Source: https://docs.neoforged.net/docs/1.21.1/resources/server/datamaps/builtin

Markdown Content:
NeoForge provides various built-in [data maps](https://docs.neoforged.net/docs/1.21.1/resources/server/datamaps/) for common use cases, replacing hardcoded vanilla fields. Vanilla values are shipped by data map files in NeoForge, so there is no functional difference to the player.

## `neoforge:compostables`[​](https://docs.neoforged.net/docs/1.21.1/resources/server/datamaps/builtin#neoforgecompostables "Direct link to neoforgecompostables")

Allows configuring composter values, as a replacement for `ComposterBlock.COMPOSTABLES` (which is now ignored). This data map is located at `neoforge/data_maps/item/compostables.json` and its objects have the following structure:

`{    // A 0 to 1 (inclusive) float representing the chance that the item will update the level of the composter    "chance": 1,    // Optional, defaults to false - whether farmer villagers can compost this item    "can_villager_compost": false}`

Example:

`{    "values": {        // Give acacia logs a 50% chance that they will fill a composter        "minecraft:acacia_log": {            "chance": 0.5        }    }}`

## `neoforge:furnace_fuels`[​](https://docs.neoforged.net/docs/1.21.1/resources/server/datamaps/builtin#neoforgefurnace_fuels "Direct link to neoforgefurnace_fuels")

Allows configuring item burn times. This data map is located at `neoforge/data_maps/item/furnace_fuels.json` and its objects have the following structure:

`{    // A positive integer representing the item's burn time in ticks    "burn_time": 1000}`

Example:

`{    "values": {        // Give anvils a 2 seconds burn time        "minecraft:anvil": {            "burn_time": 40        }    }}`

info

NeoForge additionally adds the `IItemExtension#getBurnTime` method to be overridden in custom items, overruling this data map. `#getBurnTime` should only be used in scenarios where the datamap does not suffice, for example [data component](https://docs.neoforged.net/docs/1.21.1/items/datacomponents)-dependent burn times.

warning

Vanilla adds an implicit burn time of 300 ticks (15 seconds) for `#minecraft:logs` and `#minecraft:planks`, and then hardcodes the removal of crimson and warped items from that. This means that if you add another non-flammable wood, you should add a removal for that wood type's items from this map, like so:

`{    "replace": false,    "values": [        // values here    ],    "remove": [        "examplemod:example_nether_wood_planks",        "#examplemod:example_nether_wood_stems",        "examplemod:example_nether_wood_door",        // etc.        // other removals here    ]}`

## `neoforge:monster_room_mobs`[​](https://docs.neoforged.net/docs/1.21.1/resources/server/datamaps/builtin#neoforgemonster_room_mobs "Direct link to neoforgemonster_room_mobs")

Allows configuring the mobs that may appear in the mob spawner in a monster room, as a replacement for `MonsterRoomFeature#MOBS` (which is now ignored). This data map is located at `neoforge/data_maps/entity_type/monster_room_mobs.json` and its objects have the following structure:

`{    // The weight of this mob, relative to other mobs in the datamap    "weight": 100}`

Example:

`{    "values": {        // Make squids appear in monster room spawners with a weight of 100        "minecraft:squid": {            "weight": 100        }    }}`

## `neoforge:oxidizables`[​](https://docs.neoforged.net/docs/1.21.1/resources/server/datamaps/builtin#neoforgeoxidizables "Direct link to neoforgeoxidizables")

Allows configuring oxidation stages, as a replacement for `WeatheringCopper#NEXT_BY_BLOCK` (which will be ignored in 1.21.2). This data map is also used to build a reverse deoxidation map (for scraping with an axe). It is located at `neoforge/data_maps/block/oxidizables.json` and its objects have the following structure:

`{    // The block this block will turn into once oxidized    "next_oxidation_stage": "examplemod:oxidized_block"}`

note

Custom blocks must implement `WeatheringCopperFullBlock` or `WeatheringCopper` and call `changeOverTime` in `randomTick` to oxidize naturally.

Example:

`{    "values": {        "mymod:custom_copper": {            // Make a custom copper block oxidize into custom oxidized copper            "next_oxidation_stage": "mymod:custom_oxidized_copper"        }    }}`

## `neoforge:parrot_imitations`[​](https://docs.neoforged.net/docs/1.21.1/resources/server/datamaps/builtin#neoforgeparrot_imitations "Direct link to neoforgeparrot_imitations")

Allows configuring the sounds produced by parrots when they want to imitate a mob, as a replacement for `Parrot#MOB_SOUND_MAP` (which is now ignored). This data map is located at `neoforge/data_maps/entity_type/parrot_imitations.json` and its objects have the following structure:

`{    // The ID of the sound that parrots will produce when imitating the mob    "sound": "minecraft:entity.parrot.imitate.creeper"}`

Example:

`{    "values": {        // Make parrots produce the ambient cave sound when imitating allays        "minecraft:allay": {            "sound": "minecraft:ambient.cave"        }    }}`

## `neoforge:raid_hero_gifts`[​](https://docs.neoforged.net/docs/1.21.1/resources/server/datamaps/builtin#neoforgeraid_hero_gifts "Direct link to neoforgeraid_hero_gifts")

Allows configuring the gift that a villager with a certain `VillagerProfession` may gift you if you stop the raid, as a replacement for `GiveGiftToHero#GIFTS` (which is now ignored). This data map is located at `neoforge/data_maps/villager_profession/raid_hero_gifts.json` and its objects have the following structure:

`{    // The ID of the loot table that a villager profession will hand out after a raid    "loot_table": "minecraft:gameplay/hero_of_the_village/armorer_gift"}`

Example:

`{    "values": {        "minecraft:armorer": {            // Make armorers give the raid hero the armorer gift loot table            "loot_table": "minecraft:gameplay/hero_of_the_village/armorer_gift"        }    }}`

## `neoforge:vibration_frequencies`[​](https://docs.neoforged.net/docs/1.21.1/resources/server/datamaps/builtin#neoforgevibration_frequencies "Direct link to neoforgevibration_frequencies")

Allows configuring the sculk vibration frequencies emitted by game events, as a replacement for `VibrationSystem#VIBRATION_FREQUENCY_FOR_EVENT` (which is now ignored). This data map is located at `neoforge/data_maps/game_event/vibration_frequencies.json` and its objects have the following structure:

`{    // An integer between 1 and 15 (inclusive) that indicates the vibration frequency of the event    "frequency": 2}`

Example:

`{    "values": {        // Make the splash in water game event vibrate on the second frequency        "minecraft:splash": {            "frequency": 2        }    }}`

## `neoforge:villager_types`[​](https://docs.neoforged.net/docs/1.21.1/resources/server/datamaps/builtin#neoforgevillager_types "Direct link to neoforgevillager_types")

Allows configuring the villager type that will spawn based on its biome, as a replacement for `VillagerType#BY_BIOME` (which will be ignored in 1.22). It is located at `neoforge/data_maps/worldgen/biome/villager_types.json` and its objects have the following structure:

`{    // The villager type that will spawn in this biome    // If no villager type is specified for a biome, then `minecraft:plains` will be used    "villager_type": "minecraft:desert"    }`

Example:

`{    "values": {        // Make villagers in the jungle biome be of the desert type        "minecraft:jungle": {            "villager_type": "minecraft:desert"        }    }}`

## `neoforge:waxables`[​](https://docs.neoforged.net/docs/1.21.1/resources/server/datamaps/builtin#neoforgewaxables "Direct link to neoforgewaxables")

Allows configuring the block a block will turn into when waxed (right clicked with a honeycomb), as a replacement for `HoneycombItem#WAXABLES` (which will be ignored in 1.21.2). This data map is also used to build a reverse dewaxing map (for scraping with an axe). It is located at `neoforge/data_maps/block/waxables.json` and its objects have the following structure:

`{    // The waxed variant of this block    "waxed": "minecraft:iron_block"}`

Example:

`{    "values": {        // Make gold blocks turn into iron blocks once waxed        "minecraft:gold_block": {            "waxed": "minecraft:iron_block"        }    }}`