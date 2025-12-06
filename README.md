<div align="center">
  <img src="images/banner.png" alt="Dynamic Portals Banner" width="100%">
  
  <h1>Dynamic Portals</h1>

  <p>
    <strong>Create your own progression system! Lock dimensional portals behind customizable requirements.</strong>
  </p>

  <a href="https://neoforged.net/">
    <img src="https://img.shields.io/badge/NeoForge-1.21.1-orange?style=for-the-badge" alt="NeoForge 1.21.1">
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/github/license/II-mirai-II/Dynamic-Portals?style=for-the-badge&color=blue" alt="License">
  </a>
</div>

<br>

## 📖 Overview

**Dynamic Portals** allows you to lock dimensions behind specific gameplay tasks. Perfect for modpack creators and players looking for a structured progression system. By default, it requires players to defeat specific mobs and collect items to access the Nether and End, but **everything** can be changed without coding!

### ✨ Key Features

*   **🚫 Portal Blocking:** Prevent players from entering dimensions until they prove themselves.
*   **📝 Fully Data-Driven:** Configure requirements via simple JSON5 files.
*   **⚔️ Flexible Goals:** Combine mob kills, boss fights, item collection, and advancements.
*   **📊 In-Game HUD:** Beautiful progress overlay to track your requirements (Toggle with `H`).
*   **🤝 Team Assist:** Configurable time window for shared kill credit in multiplayer.
*   **🌍 Mod Compatibility:** Works with any modded dimension and entity.

---

## 🎮 How it Works

When you install the mod, standard portals (Nether/End) are locked. You must complete the requirements displayed on your HUD to unlock them.

**Default Requirements (Example):**
*   **Overworld → Nether:** Kill basic mobs (Zombies, Skeletons, etc.), defeat the Elder Guardian, and find a Diamond.
*   **Nether → End:** Hunt Nether mobs, defeat the Wither, and obtain a Netherite Ingot.

*Don't like these rules? Change them entirely in the config!*

---

## ⚙️ Configuration

The mod is designed for **Modpacks**. You can override defaults, add support for modded dimensions (like Twilight Forest), or create "Peaceful" item-only modes.

**Two Config Files:**
1.  `config/dynamicportals-common.toml`: Adjust HUD colors, assist timers, and toggle mechanics.
2.  `config/dynamicportals/portal_requirements.json5`: Define the actual requirements.

**Simple JSON Example:**
```json5
{
  "dimension": "minecraft:the_nether",
  "requirements": {
    "mobs": ["minecraft:zombie", "minecraft:skeleton"],
    "items": ["minecraft:diamond"]
  },
  "display": {
    "name": "Nether Access",
    "description": "Prove your worth to enter the Nether"
  }
}

### 🤝 Compatibility / Contributing

**Built-in Support:** I have personally added full compatibility for two mob mods (v1.1.0 Forward):

```
*   _Mowzie's Mobs_

*   _L\_Ender's Cataclysm_
```

*   You can also customize these integrations via the new JSON/Datapack system. Again, feedback on GitHub is appreciated if you encounter any issues.

### Problems? Please open an issue on GitHub (Contributions, issues, and feature requests are welcome!): [https://github.com/II-mirai-II/Dynamic-Portals/issues](https://github.com/II-mirai-II/Dynamic-Portals/issues)

***