<div align="center">
  <img src="images/banner.png" alt="Dynamic Portals Banner" width="100%">
  
  <h1>Dynamic Portals</h1>

  <p>
    A highly configurable progression mod for Minecraft NeoForge that locks dimensional portals behind customizable requirements.
  </p>

  <a href="https://neoforged.net/">
    <img src="https://img.shields.io/badge/NeoForge-1.21.1-orange?style=for-the-badge" alt="NeoForge 1.21.1">
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/github/license/II-mirai-II/Dynamic-Portals?style=for-the-badge&color=blue" alt="License">
  </a>
</div>

<br>

## 📖 Description

**Dynamic Portals** is a **fully data-driven** progression mod that gives you complete control over portal access requirements. Design custom progression systems for modpacks without touching a single line of code!

### ✨ Key Features

- 🎯 **Fully Configurable**: JSON5-based configuration for all portal requirements
- 👾 **Flexible Requirements**: Combine mob kills, boss fights, and item collection
- 🤝 **Assist System**: Configurable time window for team play
- 📊 **Progress HUD**: Beautiful, paginated overlay with customizable colors
- 🔄 **No Hardcoding**: Everything is data-driven - perfect for modpack creators
- 🎨 **Customizable UI**: Configure HUD colors, pagination, and display info
- 🌍 **Multi-Dimension**: Works with vanilla and modded dimensions
- 🌐 **Localization**: Full support for English and Portuguese (Brazil)

---

## ⚙️ Configuration

Dynamic Portals uses **two configuration systems**:

### 1. TOML Config (`config/dynamicportals-common.toml`)

Controls gameplay mechanics and UI appearance:

```toml
[common]
    # Time window for kill assists (seconds)
    assist_time_window_seconds = 10
    
    # Enable/disable portal blocking
    enable_portal_blocking = true
    
    # HUD pagination limit
    max_lines_per_page = 20
    
    # UI Colors (ARGB hex format)
    hud_background_color = "0xDD000000"
    hud_header_color = "0xFF4A90E2"
    
    # Debug logging
    debug_logging = false
```

### 2. Portal Requirements (`config/dynamicportals/portal_requirements.json5`)

Defines what players must do to unlock each portal. **Auto-generated** on first run:

```json5
{
  "override_defaults": false,
  "portals": [
    {
      "dimension": "minecraft:the_nether",
      "advancement": "dynamicportals:nether_access",
      "requirements": {
        "mobs": ["minecraft:zombie", "minecraft:skeleton", ...],
        "bosses": ["minecraft:elder_guardian"],
        "items": ["minecraft:diamond"]
      },
      "display": {
        "name": "Nether Portal",
        "description": "Defeat overworld threats",
        "color": "0xFFFF5555",
        "icon": "minecraft:netherrack",
        "sort_order": 1
      }
    }
  ]
}
```

**Full documentation:** See [Configuration Guide](#-configuration-guide) below

---

## 🎮 Default Requirements

### Overworld → Nether Access
**Required Mobs (15 total):**
- Zombie, Skeleton, Creeper, Spider, Enderman
- Witch, Slime, Drowned, Husk, Stray
- Breeze, Bogged, Pillager, Vindicator, Evoker

**Boss:** Elder Guardian  
**Item:** Diamond

### Nether → End Access
**Required Mobs (6 total):**
- Ghast, Blaze, Wither Skeleton
- Piglin, Piglin Brute, Hoglin

**Bosses:** Warden, Wither  
**Item:** Netherite Ingot

---

## 🎯 Gameplay

### HUD Controls

| Key | Default | Action |
|-----|---------|--------|
| Toggle HUD | `H` | Show/hide progress overlay |
| Switch Phase | `Tab` | Cycle Nether/End requirements |
| Next Page | `→` | Navigate to next page |
| Previous Page | `←` | Navigate to previous page |

**Customizable in:** Minecraft Settings → Controls → Dynamic Portals

### Progress Tracking

The mod automatically tracks:
- ✅ Mob kills (with assist credit)
- ✅ Boss defeats
- ✅ Item collection
- ✅ Advancement unlocks

**Assist System:** Damage a mob within the configured time window (default 10s) and get credit even if someone else lands the final blow!

---

## 📚 Configuration Guide

### TOML Settings

**`assist_time_window_seconds`** (1-300, default: 10)
- How long players have to damage a mob before getting assist credit
- Higher = easier for teams, lower = more challenging

**`enable_portal_blocking`** (true/false, default: true)
- Set to `false` to disable restrictions (testing mode)

**`max_lines_per_page`** (5-50, default: 20)
- How many requirements show per HUD page
- Increase for larger screens, decrease for compact displays

**`hud_background_color`** / **`hud_header_color`** (ARGB hex)
- Format: `"0xAARRGGBB"`
  - AA = Alpha (00=invisible, FF=opaque)
  - RR = Red, GG = Green, BB = Blue
- Examples:
  - `"0xDD000000"` = Semi-transparent black
  - `"0xFF4A90E2"` = Opaque blue
  - `"0x80FF0000"` = Semi-transparent red

**`debug_logging`** (true/false, default: false)
- Enable detailed logging for troubleshooting

### JSON5 Portal Configuration

**`override_defaults`** (boolean)
- `false`: Your portals **merge** with vanilla defaults
- `true`: Your portals **replace** all defaults (total control)

**Portal Structure:**

```json5
{
  "dimension": "minecraft:the_nether",     // Dimension ID
  "advancement": "dynamicportals:nether_access",  // Optional advancement link
  "requirements": {
    "mobs": [...],      // Regular mob IDs
    "bosses": [...],    // Boss mob IDs
    "items": [...]      // Item IDs
  },
  "display": {
    "name": "Nether Portal",               // Display name in HUD
    "description": "Portal description",   // Description text
    "color": "0xFFFF5555",                 // UI color (ARGB hex)
    "icon": "minecraft:netherrack",        // Icon item ID
    "sort_order": 1                        // Display order (lower = first)
  }
}
```

---

## 🛠️ Customization Examples

### Example 1: Easy Mode (Fewer Requirements)

```json5
{
  "override_defaults": true,
  "portals": [
    {
      "dimension": "minecraft:the_nether",
      "requirements": {
        "mobs": ["minecraft:zombie", "minecraft:skeleton"],
        "bosses": [],
        "items": ["minecraft:diamond"]
      },
      "display": {
        "name": "Nether Portal (Easy)",
        "description": "Kill 2 mob types and find a diamond",
        "color": "0xFFFF8800",
        "icon": "minecraft:netherrack",
        "sort_order": 1
      }
    }
  ]
}
```

### Example 2: Modded Content

```json5
{
  "override_defaults": false,
  "portals": [
    {
      "dimension": "twilightforest:twilight_forest",
      "requirements": {
        "mobs": ["twilightforest:skeleton_druid", "twilightforest:kobold"],
        "bosses": ["twilightforest:naga"],
        "items": ["twilightforest:magic_map"]
      },
      "display": {
        "name": "Twilight Forest",
        "description": "Explore the twilight realm",
        "color": "0xFF00AA00",
        "icon": "twilightforest:twilight_oak_sapling",
        "sort_order": 3
      }
    }
  ]
}
```

### Example 3: Item-Only Challenge (Peaceful Mode)

```json5
{
  "override_defaults": true,
  "portals": [
    {
      "dimension": "minecraft:the_nether",
      "requirements": {
        "mobs": [],
        "bosses": [],
        "items": ["minecraft:diamond", "minecraft:gold_ingot", "minecraft:emerald"]
      },
      "display": {
        "name": "Nether (Peaceful)",
        "description": "Collect 3 ore types",
        "color": "0xFF00FFFF",
        "icon": "minecraft:diamond",
        "sort_order": 1
      }
    }
  ]
}
```

---

## 🔧 For Modpack Creators

### Recommended Workflow

1. **Test Defaults:** Launch game, let config auto-generate, test in survival
2. **Design Progression:** Plan tiers, balance difficulty, consider modded content
3. **Configure:** Edit `portal_requirements.json5` with your custom requirements
4. **Tune Gameplay:** Adjust TOML settings (assist window, HUD colors, pagination)
5. **Test & Iterate:** Validate in survival, check HUD display, verify advancements

### Common Issues

| Problem | Solution |
|---------|----------|
| Portal not blocking | Check `enable_portal_blocking = true` in TOML |
| Kills not registering | Verify mob IDs in JSON5, check assist window timing |
| HUD not showing all mobs | Increase `max_lines_per_page` or use pagination |
| Custom dimension not working | Ensure dimension ID matches exact registry name |

### Debugging

Enable `debug_logging = true` in TOML, then check logs for:
- JSON parsing errors
- Registry warnings
- Packet sync issues

---

## 📊 Technical Details

### Architecture
- **Client-Server Sync:** Custom packet system for progress/requirements
- **Attachment System:** Persistent player data via NeoForge attachments
- **Cache System:** Smart HUD caching for minimal performance impact
- **Registry-Based:** Dynamic tracking via PortalRequirementRegistry

### Performance Optimizations
- ✅ Component caching (mob/item names)
- ✅ Registry ID caching (EntityType lookups)
- ✅ Render cache (rebuilt only on changes)
- ✅ Batch text rendering (single draw call)
- ✅ Pagination (configurable line limits)

---

## 🐛 Troubleshooting

**Enable debug logging:**
```toml
debug_logging = true
```

**Check logs for:**
- `[Dynamic Portals]` prefix messages
- JSON parsing errors
- Registry warnings
- Packet sync issues

**Common problems:**
- **Config not loading:** Check JSON5 syntax, look for parse errors in logs
- **Progress not saving:** Verify world save folder has attachment data
- **HUD not appearing:** Check keybinding conflicts

---

## 🤝 Contributing

This mod is designed to be fully data-driven. You can:
- ✅ Create custom configurations
- ✅ Share JSON5 presets with the community
- ✅ Report bugs via GitHub issues
- ✅ Suggest features (must maintain data-driven design)

---

## 📝 License

This project is licensed under the **MIT License** with template components under separate licensing.

See [LICENSE](LICENSE) and `TEMPLATE_LICENSE.txt` for details.

---

## 👨‍💻 Author

**Mirai** - Original developer

---

## 📧 Support

- **Issues:** [GitHub Issues](https://github.com/II-mirai-II/Dynamic-Portals/issues)
- **Wiki:** [GitHub Wiki](https://github.com/II-mirai-II/Dynamic-Portals/wiki)

---

**Enjoy building your custom progression systems!** 🚀
