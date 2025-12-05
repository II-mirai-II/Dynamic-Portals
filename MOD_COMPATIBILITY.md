# Mod Compatibility System

Dynamic Portals includes a flexible compatibility system that allows integration with mods that add new creatures to Minecraft.

## How It Works

The mod automatically detects installed mods and loads their compatibility configurations from JSON files. These configurations define which mobs from other mods should be tracked for portal progression requirements.

## Supported Mods

Currently, Dynamic Portals includes built-in compatibility for:

- **Mowzie's Mobs** - Adds unique boss mobs and creatures
- **L_Ender's Cataclysm** - Adds powerful bosses and challenging creatures across all dimensions

## File Structure

Compatibility files are located at:
```
data/dynamicportals/mod_compat/<mod_id>.json
```

## JSON Format

Each compatibility file follows this structure:

```json
{
  "mod_id": "example_mod",
  "enabled": true,
  "overworld_mobs": [
    "example_mod:creature1",
    "example_mod:creature2"
  ],
  "nether_mobs": [
    "example_mod:nether_creature"
  ],
  "end_mobs": [],
  "bosses": [
    "example_mod:boss1"
  ]
}
```

### Fields:

- **mod_id** (required): The mod's identifier
- **enabled** (optional, default: true): Whether this compatibility is active
- **overworld_mobs**: Creatures that must be killed to unlock the Nether
- **nether_mobs**: Creatures that must be killed to unlock the End
- **end_mobs**: Creatures for End progression (future use)
- **bosses**: Boss creatures (weighted more heavily in progression)

## Creating Custom Compatibility

### For Datapack Creators:

1. Create a datapack with the following structure:
```
your_datapack/
  data/
    dynamicportals/
      mod_compat/
        yourmod.json
```

2. Add your JSON configuration following the format above

3. The mod will automatically load your configuration if the target mod is installed

### For Mod Developers:

You can also use the API to register requirements programmatically:

```java
DynamicPortals.getAPI().registerPortalRequirement(
    PortalRequirement.builder(ModConstants.NETHER_DIMENSION)
        .addMob(YourMod.YOUR_CREATURE.get())
        .build()
);
```

## Entity Tags

The mod also uses entity tags for categorization:

- `dynamicportals:overworld_progression` - Overworld mobs
- `dynamicportals:nether_progression` - Nether mobs
- `dynamicportals:end_progression` - End mobs
- `dynamicportals:bosses_overworld` - Overworld bosses
- `dynamicportals:bosses_nether` - Nether bosses
- `dynamicportals:bosses_end` - End bosses

You can add your mod's creatures to these tags via datapack to integrate with the progression system.

## Examples

### Example 1: Simple Mod Compatibility
```json
{
  "mod_id": "simple_mobs",
  "enabled": true,
  "overworld_mobs": [
    "simple_mobs:goblin",
    "simple_mobs:orc"
  ],
  "bosses": [
    "simple_mobs:goblin_king"
  ]
}
```

### Example 2: Disabling a Compatibility
```json
{
  "mod_id": "hardcore_mobs",
  "enabled": false,
  "overworld_mobs": [...]
}
```

## Troubleshooting

- Check the logs for messages like: `Loaded compatibility config for mod: <modid>`
- If a mob isn't being tracked, verify the entity ID is correct
- Entity IDs must use the format: `namespace:path` (e.g., `mowziesmobs:foliaath`)
- Unknown entity IDs will show warnings in the log

## Contributing

To add compatibility for a new mod:

1. Fork the repository
2. Create a new JSON file in `src/main/resources/data/dynamicportals/mod_compat/`
3. Test with the mod installed
4. Submit a pull request

---

For questions or issues, please visit the GitHub repository.
