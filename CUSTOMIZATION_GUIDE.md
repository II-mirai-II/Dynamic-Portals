# 📝 Dynamic Portals - Complete Customization Guide

## 🎯 Overview

Dynamic Portals allows complete customization of dimensional access requirements through JSON configuration files. This guide will teach you how to modify, create, and fine-tune portal requirements to match your gameplay style or modpack vision.

---

## 📂 Configuration Files Location

After first launch, Dynamic Portals creates configuration files in your game directory:

```
minecraft/
├── config/
│   ├── dynamicportals-common.toml          # General settings (colors, HUD, etc.)
│   └── dynamicportals/
│       └── mod_compat/                      # Mod compatibility configs
│           ├── mowziesmobs.json
│           ├── cataclysm.json
│           └── your_custom_mod.json         # Add your own!
```

---

## 🔧 General Settings (`dynamicportals-common.toml`)

### Gameplay Settings

```toml
[gameplay]
    # Enable/disable portal blocking
    enable_portal_blocking = true
    
    # Auto-grant advancements when requirements are met
    auto_grant_advancements = true
    
    # Time window (seconds) for kill assists in multiplayer
    assist_time_window_seconds = 5
    
    # Global vs Individual progress mode
    enable_global_progress = false
```

**Progress Modes:**
- **Individual Mode** (`false`): Each player tracks their own progress
- **Global Mode** (`true`): All players share the same progress (great for co-op servers!)

⚠️ **Important:** Changing modes mid-game requires careful consideration. Backup your world first!

## 🎮 In-Game Controls

- **Toggle HUD:** Press `K` (configurable in Controls menu)
- **Switch Dimension Phase:** Press `Tab` (while HUD is open)
- **Next Page:** Press `→` (Right Arrow)
- **Previous Page:** Press `←` (Left Arrow)

---

## 🔌 Mod Compatibility System

### How It Works

Dynamic Portals automatically integrates mobs and bosses from compatible mods into the progression system. No coding required!

**Default Behavior:**
1. Mod compatibility configs are bundled in the JAR (internal)
2. On first launch, configs are exported to `config/dynamicportals/mod_compat/`
3. You can edit these files to customize which entities are required
4. External configs **override** internal ones

### Built-in Mod Support

Dynamic Portals includes pre-configured compatibility for:

| Mod | Added to Nether Requirements | Added to End Requirements |
|-----|------------------------------|---------------------------|
| **Mowzie's Mobs** | 5 bosses | 1 Nether boss (Sculptor) |
| **L_Ender's Cataclysm** | 6 Overworld mobs + 9 bosses | 1 Nether mob + 3 Nether bosses |

---

## 📝 Creating Custom Mod Compatibility

### Example: Adding a New Mod

Create a new file: `config/dynamicportals/mod_compat/yourmod.json`

```json
{
  "mod_id": "yourmod",
  "enabled": true,
  "overworld_mobs": [
    "yourmod:basic_enemy",
    "yourmod:advanced_enemy"
  ],
  "nether_mobs": [
    "yourmod:nether_creature"
  ],
  "end_mobs": [],
  "bosses": [
    "yourmod:overworld_boss",
    "yourmod:dungeon_boss"
  ],
  "nether_bosses": [
    "yourmod:nether_boss"
  ]
}
```

**Field Explanations:**

- **`mod_id`**: The mod's ID (must match the mod's actual ID)
- **`enabled`**: Set to `false` to disable this compatibility without deleting the file
- **`overworld_mobs`**: Mobs added to **Nether portal requirements**
- **`nether_mobs`**: Mobs added to **End portal requirements**
- **`end_mobs`**: Reserved for future End-related progression
- **`bosses`**: Boss entities added to **Nether portal requirements**
- **`nether_bosses`**: Boss entities added to **End portal requirements**

### Finding Entity IDs

**Method 1 - In-Game Command:**
```
/summon <tab>
```
Tab-complete to see all available entity IDs.

**Method 2 - Advanced Tooltips:**
1. Press `F3 + H` to enable advanced tooltips
2. Look at entity with crosshair
3. ID shown in debug screen (F3)

**Method 3 - Spawn Egg:**
1. Enable advanced tooltips (`F3 + H`)
2. Hover over spawn egg in creative inventory
3. ID shown in tooltip

---

## 🎨 Example Configurations

### Example 1: Peaceful Mode (Items Only)

Want to remove all mob requirements? Edit existing compatibility files:

**`config/dynamicportals/mod_compat/cataclysm.json`:**
```json
{
  "mod_id": "cataclysm",
  "enabled": false
}
```

Repeat for all mod compat files, then only vanilla items (Diamond, Netherite Ingot) will be required.

---

### Example 2: Hardcore Boss Rush

Edit compatibility to add MORE bosses:

**`config/dynamicportals/mod_compat/extra_bosses.json`:**
```json
{
  "mod_id": "minecraft",
  "enabled": true,
  "overworld_mobs": [],
  "nether_mobs": [],
  "end_mobs": [],
  "bosses": [
    "minecraft:elder_guardian",
    "minecraft:warden"
  ],
  "nether_bosses": [
    "minecraft:wither"
  ]
}
```

Now players must defeat Elder Guardian, Warden, AND the Wither!

---

### Example 3: Modded Dimension Support

Dynamic Portals automatically supports ANY dimension, not just Nether/End!

**Example for Twilight Forest:**

Create: `config/dynamicportals/mod_compat/twilight_custom.json`

```json
{
  "mod_id": "twilightforest",
  "enabled": true,
  "overworld_mobs": [
    "twilightforest:kobold",
    "twilightforest:skeleton_druid"
  ],
  "bosses": [
    "twilightforest:naga",
    "twilightforest:lich"
  ]
}
```

**Note:** For custom dimensions, you may need to use the API (see Advanced section).

---

### Example 4: Disabling Specific Mod Entities

Don't want a specific boss? Edit the file:

**Before:**
```json
{
  "mod_id": "cataclysm",
  "enabled": true,
  "bosses": [
    "cataclysm:the_leviathan",
    "cataclysm:ancient_remnant",
    "cataclysm:netherite_monstrosity"
  ]
}
```

**After (removing Netherite Monstrosity):**
```json
{
  "mod_id": "cataclysm",
  "enabled": true,
  "bosses": [
    "cataclysm:the_leviathan",
    "cataclysm:ancient_remnant"
  ]
}
```

---

## 🎯 Balancing Tips

### For Single Player

**Easy Mode:**
- Reduce mob variety
- Keep only bosses you enjoy fighting
- Set `assist_time_window_seconds = 10` (generous window)

**Normal Mode:**
- Use default configuration
- Mix of common mobs and bosses

**Hard Mode:**
- Enable ALL mod compatibilities
- Add extra bosses via custom configs
- Set `assist_time_window_seconds = 3` (tight window)

### For Multiplayer Servers

**Cooperative Play:**
- Enable `enable_global_progress = true`
- Set `assist_time_window_seconds = 10`
- All players share progress!

**Competitive Play:**
- Keep `enable_global_progress = false`
- Set `assist_time_window_seconds = 5`
- Each player races independently

**Team Play:**
- Individual progress mode
- Long assist window (10-15 seconds)
- Teams work together, each gets credit

---

## 🔍 Troubleshooting

### Problem: Entity Not Tracking

**Symptoms:** Killed a mob, but it's not counted in HUD

**Solutions:**
1. **Verify entity ID is correct:**
   ```
   /summon modid:entity_name
   ```
   If summon fails, the ID is wrong.

2. **Check mod compatibility is enabled:**
   - Open `config/dynamicportals/mod_compat/modname.json`
   - Ensure `"enabled": true`

3. **Enable debug logging:**
   ```toml
   [advanced]
   debug_logging = true
   ```
   Check `logs/latest.log` for errors.

4. **Verify mod is loaded:**
   - Check mods list in-game
   - Ensure mod versions are compatible

---

### Problem: HUD Not Showing

**Solutions:**
1. Press `K` to toggle (default key)
2. Check key binding in Controls menu
3. Ensure you have requirements configured
4. Make sure you're in a world/server

---

### Problem: Config Changes Not Loading

**Solutions:**
1. Completely exit and restart Minecraft
2. Check for JSON syntax errors:
   - Missing commas
   - Unclosed brackets
   - Extra/missing quotes
3. Use a JSON validator: https://jsonlint.com/
4. Check `logs/latest.log` for parsing errors

---

## 🛠️ Advanced Customization (For Modpack Developers)

### Using the API

Dynamic Portals provides a public API for other mods to register custom requirements:

```java
// Get the API instance
IPortalRequirementAPI api = DynamicPortals.getAPI();

// Create a custom requirement
PortalRequirement myRequirement = PortalRequirement.builder(new ResourceLocation("modid", "custom_dimension"))
    .advancement(new ResourceLocation("modid", "custom_advancement"))
    .addMob(EntityType.ZOMBIE)
    .addMob(EntityType.SKELETON)
    .addBoss(EntityType.WITHER)
    .addItem(Items.DIAMOND)
    .displayName("§cCustom Dimension")
    .displayColor(0xFFFF0000)
    .displayIcon(new ResourceLocation("modid", "custom_icon"))
    .sortOrder(10)
    .build();

// Register it
api.registerPortalRequirement(myRequirement);
```

**API Benefits:**
- Programmatic requirement creation
- Integration with your mod's progression
- Custom display names and colors
- Full control over requirements

---

### Config File Priority

Understanding load order:

1. **Internal Configs** (bundled in mod JAR) - Loaded first
2. **External Configs** (`config/dynamicportals/mod_compat/*.json`) - Override internal
3. **API Registration** (via code) - Highest priority

**Example Scenario:**
- Internal config says: Cataclysm has 9 bosses
- You edit external config: Remove 3 bosses
- Final result: Only 6 bosses required

---

## 📚 Reference Tables

### Vanilla Requirements (Default)

#### Nether Portal Requirements

| Category | Entities | Count |
|----------|----------|-------|
| Basic Hostiles | Zombie, Skeleton, Creeper, Spider | 4 |
| Special | Enderman, Witch, Slime | 3 |
| Water | Drowned | 1 |
| Desert | Husk | 1 |
| Cold | Stray | 1 |
| Trial Chambers | Breeze, Bogged | 2 |
| Illagers | Pillager, Vindicator, Evoker | 3 |
| Boss | Elder Guardian | 1 |
| Items | Diamond | 1 |
| **Total** | | **17 requirements** |

#### End Portal Requirements

| Category | Entities | Count |
|----------|----------|-------|
| Nether Hostiles | Ghast, Blaze, Wither Skeleton | 3 |
| Piglins | Piglin, Piglin Brute | 2 |
| Hoglin | Hoglin | 1 |
| Bosses | Warden, Wither | 2 |
| Items | Netherite Ingot | 1 |
| **Total** | | **9 requirements** |

---

### Assist Time Window Guidelines

| Playstyle | Recommended Setting | Description |
|-----------|-------------------|-------------|
| Solo | 3-5 seconds | Tight window for personal kills |
| Co-op | 8-10 seconds | Generous for team coordination |
| Server (Competitive) | 5 seconds | Balanced for multiple players |
| Server (Casual) | 10-15 seconds | Very generous, everyone gets credit |

---

## 💡 Creative Ideas

### Themed Modpacks

**Medieval Theme:**
```json
{
  "mod_id": "medieval_mobs",
  "enabled": true,
  "overworld_mobs": [
    "minecraft:skeleton",
    "minecraft:zombie",
    "minecraft:pillager",
    "minecraft:vindicator"
  ],
  "bosses": [
    "minecraft:elder_guardian"
  ]
}
```

**Magic Theme:**
```json
{
  "mod_id": "magical_progression",
  "enabled": true,
  "overworld_mobs": [
    "minecraft:witch",
    "minecraft:evoker"
  ],
  "bosses": [
    "iceandfire:dragon"
  ]
}
```

---

### Progressive Difficulty

**Stage 1: Nether (Easy)**
- Only basic mobs
- Disable boss requirements

**Stage 2: End (Medium)**  
- Nether mobs
- 1 boss

**Stage 3: Custom Dimensions (Hard)**
- All bosses
- Modded mob requirements

---

### Speedrun Mode

Disable all mod compatibilities:
```json
{
  "mod_id": "any_mod",
  "enabled": false
}
```

Vanilla requirements only = faster progression!

---

## 🤝 Community Configs

Share your configurations with the community!

### How to Share

1. Copy your `config/dynamicportals/` folder
2. Create a `.zip` file
3. Share on:
   - CurseForge mod page
   - Modrinth
   - GitHub Issues
   - Discord servers

### Popular Community Configs

Check the mod's GitHub repository for community-shared configurations:
https://github.com/II-mirai-II/Dynamic-Portals/discussions

---

## 📖 JSON Format Guide

### Basic JSON Structure

```json
{
  "key": "value",
  "array": ["item1", "item2"],
  "nested": {
    "inner_key": "inner_value"
  }
}
```

**Important Rules:**
- Use double quotes `"` (not single quotes `'`)
- Separate items with commas `,`
- NO comma after last item in array/object
- Proper bracket matching: `{}` for objects, `[]` for arrays

### Valid JSON Example

```json
{
  "mod_id": "example",
  "enabled": true,
  "overworld_mobs": [
    "minecraft:zombie",
    "minecraft:skeleton"
  ],
  "bosses": []
}
```

### Common Mistakes

❌ **Wrong:** Missing comma
```json
{
  "mod_id": "example"
  "enabled": true
}
```

✅ **Correct:**
```json
{
  "mod_id": "example",
  "enabled": true
}
```

❌ **Wrong:** Trailing comma
```json
{
  "bosses": [
    "minecraft:wither",
  ]
}
```

✅ **Correct:**
```json
{
  "bosses": [
    "minecraft:wither"
  ]
}
```

---

## 🔗 Resources

### Official Links

- **GitHub Repository:** https://github.com/II-mirai-II/Dynamic-Portals
- **Issue Tracker:** https://github.com/II-mirai-II/Dynamic-Portals/issues

### Useful Tools

- **JSON Validator:** https://jsonlint.com/
- **Minecraft Wiki (Entity IDs):** https://minecraft.wiki/w/Java_Edition_data_values#Entities
- **Minecraft Wiki (Item IDs):** https://minecraft.wiki/w/Java_Edition_data_values#Items

---

## ❓ Frequently Asked Questions

### Q: Can I use this in my modpack?
**A:** Yes! Dynamic Portals is designed for modpack customization. Please credit the mod.

---

### Q: Will this work with other progression mods?
**A:** Generally yes, but conflicts may occur if both mods try to control the same portals. Test thoroughly.

---

### Q: Can I add requirements for modded dimensions?
**A:** Yes! Use the API or create compatibility configs. The mod supports ANY dimension.

---

### Q: Does this work on servers?
**A:** Absolutely! Both Individual and Global progress modes work on servers.

---

### Q: What happens if a player has completed requirements and then I add more?
**A:** They'll need to complete the new requirements. Unlocked achievements are preserved.

---

### Q: Can I reset a player's progress?
**A:** Yes, via NBT editing or by having them die 10 times (in Individual mode).

---

### Q: How do I completely disable the mod without removing it?
**A:** Set `enable_portal_blocking = false` in `dynamicportals-common.toml`.

---

### Q: Can I change the death penalty threshold?
**A:** Currently, the 10-death threshold is hardcoded. Request this as a config option on GitHub!

---

### Q: How do I backup my configs?
**A:** Copy the entire `config/dynamicportals/` folder to a safe location.

---

### Q: Does this work with Forge?
**A:** No, Dynamic Portals requires **NeoForge** (Minecraft 1.21.1+).

---

## 📜 License & Credits

**Dynamic Portals** is licensed under the MIT License.

**Created by:** II-mirai-II

**Special Thanks:**
- NeoForge team for the modding framework
- Community contributors for mod compatibility configs
- Testers and feedback providers

---

## 🎉 Final Tips

1. **Start Simple:** Begin with minimal requirements, then expand
2. **Test Incrementally:** Add one mod compatibility at a time
3. **Backup Regularly:** Keep copies of working configs
4. **Join the Community:** Share your configs and get help
5. **Read the Logs:** When issues occur, check `logs/latest.log`
6. **Have Fun:** This is YOUR game - customize it your way!

---

**Happy Customizing! 🚀**

*Dynamic Portals - Making Progression Your Way*

*Version 1.2.0 | Minecraft 1.21.1 | NeoForge*
