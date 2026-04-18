# 📚 Dynamic Portals - Complete Documentation

> **Full guide for gameplay, configuration, commands, and technical details.**  
> [← Back to README](README.md)

---

## 🎮 How to Play (Step-by-Step Tutorial)

### For New Players

**1. Check Your Requirements** 
Use `/dp check` in chat to see what needs to be done:
```
/dp check
```
You'll see a list of "Next Targets" - the next mob or item to conquer.

**2. Complete a Requirement**
Default example: to unlock the **Nether**, you need to kill:
- 1x Zombie
- 1x Skeleton  
- 1x Spider
- *(and 11 more different mobs)*

Go out and kill a Zombie. You'll receive in chat:
```
✓ You killed 1 Zombie!
```

**3. Track Your Progress**
Keep executing `/dp check` and watch your progress grow. Each completed requirement gets a notification:
```
✓ Requirement complete! [Kill Skeleton]  
✓ Requirement complete! [Kill Spider]
```

**4. Unlock Portals**
When all requirements are completed, trying to enter the portal:
```
[Portal Unlocked!] Nether is now accessible!
```

**5. (Optional) Create a Party**
Call your friends! Create a party to share progress:
```
/dp party create password123 MyAwesomeTeam
```
Share the party code with friends and they can `/dp party join` to enter. Now progress is shared!

### Visualization Modes

```bash
# Compact Mode (default - summarized)
/dp check

# Detailed Mode (shows progress %)
/dp check detailed

# Pending Mode (only missing requirements)
/dp check pending

# Specific Dimension
/dp check nether
/dp check the_end

# With Pagination
/dp check pending 2        # Page 2 of requirements
```

---

## ⚙️ Complete Configuration

The mod is **100% configurable** via TOML file. No recompilation needed!

### File Location
```
config/dynamicportals-common.toml
```

### Basic Structure

```toml
[general]
# Show progress notifications in chat
enableChatProgress = true

# Play sound when completing requirement
enableSuccessSound = true

[requirements.killRequirements]
# Format: dimension|entity|quantity
# Nether (default)
minecraft:the_nether|minecraft:zombie|1
minecraft:the_nether|minecraft:skeleton|1
minecraft:the_nether|minecraft:spider|1
minecraft:the_nether|minecraft:creeper|1
minecraft:the_nether|minecraft:slime|1
# ... 9 more mobs

# The End (default)
minecraft:the_end|minecraft:magma_cube|1
minecraft:the_end|minecraft:blaze|1
minecraft:the_end|minecraft:wither_skeleton|1
# ... 6 more mobs

[requirements.consumeBypassItems]
# Use special item to unlock directly (consumed)
minecraft:the_nether|minecraft:magma_cream
minecraft:the_end|minecraft:chorus_fruit
```

### 3 Configuration Scenarios

#### Scenario 1: Pure Vanilla (Default)
Use the default config without modifications. Works with any vanilla server!

#### Scenario 2: With Additional Mods
Add mobs from mods in the config:

```toml
[requirements.killRequirements]
# Vanilla mobs default
minecraft:the_nether|minecraft:zombie|1

# Now add mobs from mods!
# Twilight Forest
twilightforest:the_twilight|twilightforest:swamp_troll|1
twilightforest:the_twilight|twilightforest:naga|1

# Deep Abyss (example)
deepabyss:deep_abyss|deepabyss:abyss_creature|2
```

#### Scenario 3: Completely Custom
Create your own requirements from scratch:

```toml
[requirements.killRequirements]
# Your server with unique requirements!
minecraft:the_nether|minecraft:piglin|5
minecraft:the_nether|minecraft:hoglin|3
minecraft:the_nether|minecraft:ghast|2

[requirements.itemRequirements]
minecraft:the_nether|minecraft:netherite_ingot|3
minecraft:the_nether|minecraft:golden_apple|2
```

### Advanced Configuration

```toml
[requirements.itemRequirements]
# Item Requirements
minecraft:the_nether|minecraft:iron_ingot|32
minecraft:the_nether|minecraft:gold_ingot|16

[requirements.advancementRequirements]
# Achievement Requirements
minecraft:nether|minecraft:nether/root
minecraft:nether|minecraft/nether/find_bastion
```

---

## 📟 Complete Command List

### Command Structure

```
/dp
├── check [dimension] [mode] [page]       → View progress
├── debug sword [players] [count]         → Give test sword (OP level 2+)
└── party
    ├── create <password> [alias]         → Create new party
    ├── join <code> <password>            → Join existing party
    ├── leave                             → Leave current party
    ├── members                           → List members
    ├── dissolve                          → Dissolve party (creator only)
    └── info                              → View party info
```

### Detailed Commands

#### `/dp check [dimension] [mode] [page]`

View your requirements progress.

**Parameters:**
- `[dimension]` (optional): `nether`, `the_end`, or custom name
- `[mode]` (optional): `compact` (default), `detailed`, or `pending`
- `[page]` (optional): page number (default: 1)

**Examples:**
```bash
/dp check                              # Compact mode, all dimensions, page 1
/dp check nether                       # Compact mode Nether
/dp check the_end detailed             # Detailed mode with %
/dp check pending                      # Only missing requirements
/dp check nether detailed 2            # Nether, detailed mode, page 2
```

**Output Example (Compact):**
```
═══ Dynamic Portals - Progress ═══
📍 Nether (0/1 requirements missing)
✓ Kill Zombie
✓ Kill Skeleton
⏳ Kill Spider
  → Next: Spider (0/1)

📍 The End (2/9 requirements missing)
✓ Kill Magma Cube
⏳ Kill Blaze
  → Next: Blaze (0/1)
  
Page 1/1
```

#### `/dp debug sword [players] [count]`

**OP Level 2+ required.** Give test sword (kills any mob with 1 hit) for debug/testing.

**Examples:**
```bash
/dp debug sword @s 1                   # Give 1 sword to yourself
/dp debug sword @a 2                   # Give 2 swords to all players
```

#### `/dp party create <password> [alias]`

Create a new party and become the creator/leader.

**Parameters:**
- `<password>`: 4-32 characters (required)
- `[alias]`: Party name (optional, up to 24 characters)

**Examples:**
```bash
/dp party create password123           # Create with password
/dp party create MySecret MyTeam        # With custom alias
/dp party create 12345 "My Squad"      # With spaces in alias
```

**Output:**
```
✓ Party created!
Code: AB12CD
Alias: MyTeam
Members: 1/8
```

#### `/dp party join <code> <password>`

Join an existing party using the code and password.

**Examples:**
```bash
/dp party join AB12CD password123      # Join the party
```

**Output:**
```
✓ Joined party: MyTeam
Members: 2/8
Your progress is now shared with party members!
```

#### `/dp party leave`

Leave your current party. Your individual progress is preserved.

```bash
/dp party leave
```

**Output:**
```
✓ Left party. Your individual progress is preserved.
```

#### `/dp party members`

List all members of your party (online/offline).

```bash
/dp party members
```

**Output:**
```
═══ Party Members ═══
👑 PlayerOne (creator, online)
👤 PlayerTwo (online)
👤 PlayerThree (offline)
```

#### `/dp party dissolve`

Dissolve the party (only creator can do this).

```bash
/dp party dissolve
```

**Output:**
```
✓ Party dissolved. All members have been notified.
```

#### `/dp party info`

View party information (name, members, creation date).

```bash
/dp party info
```

**Output:**
```
═══ Party Info ═══
Alias: MyTeam
Members: 3/8 (2 online)
Created: 2026-04-16
```

---

## 🏗️ Technical Architecture (for Server Admins)

### Component Overview

```
┌─────────────────────────────────────────────────┐
│        DynamicPortals (Main Class)              │
└─────────────────────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
   ┌────▼─────┐     ┌─────▼──────┐   ┌────▼────────┐
   │ ModItems │     │   Config   │   │   Events    │
   │ (Items)  │     │(TOML Load) │   │(Listeners)  │
   └────┬─────┘     └─────┬──────┘   └────┬────────┘
        │                 │                │
        │                 ▼                │
        │          ┌──────────────┐        │
        │          │ PortalRules  │        │
        │          │(Definitions) │        │
        │          └──────┬───────┘        │
        │                 │                │
   ┌────▼─────────────────▼────────────────▼──────┐
   │    RequirementEngine (Evaluation)            │
   │  (Player vs Portal Rules)                    │
   └─────────────────┬──────────────────┬────────┘
                     │                  │
         ┌───────────▼──────┐   ┌──────▼──────────┐
         │ ProgressStore    │   │  PartyData      │
         │(Player NBT Data) │   │ (World SavedData)
         └───────────┬──────┘   └──────┬──────────┘
                     │                 │
         ┌───────────▼─────────────────▼────────┐
         │  ProgressNotifier                    │
         │  (Chat UI + Sounds + Broadcast)      │
         └─────────────────┬────────────────────┘
                           │
         ┌─────────────────▼────────────────┐
         │   Command System (/dp, /dp party)│
         └────────────────────────────────┘
```

### Data Flow (From Event to Notification)

1. **Game Event** → Player kills Zombie
2. **Event Listener** (`ProgressEvents.java`) → Captures `LivingDeathEvent`
3. **Progress Tracking** → Increments counter in `ProgressStore` (player NBT)
4. **Requirement Evaluation** → `RequirementEngine` validates if requirement was completed
5. **Broadcast** → If in party, `ProgressNotifier` sends message to all members
6. **Chat Feedback** → Player receives: `✓ Requirement complete! [Kill Zombie]`
7. **Hub Snapshot Sync** → Server builds a live snapshot (`HubSnapshotBuilder`) and sends it to clients.
8. **Progress + Party Hub Render** → Client screen reads synced state and shows Progress/Party data in a dedicated GUI.
9. **Sound Effect** → (If `enableSuccessSound = true`) Plays "level up" sound

### Data Persistence

#### Player Data (NBT - PersistentDataContainer)
```
dynamicportals
├── kills              (nested tags: "dimension|entity" → count)
├── items             (nested tags: "dimension|item" → count)
├── advancements      (nested tags: "dimension|advancement" → boolean)
├── unlocked          (dimension → unlocked flag)
├── bypass_unlocked   (dimension → bypass unlocked)
├── completed         (requirement_key → completion flag)
└── party_id          (UUID of party if member)
```

#### World Data (SavedData - "dynamicportals_parties")
```
parties: [
  {
    id: UUID
    creator: UUID (player who created)
    password_hash: SHA-256 (secure)
    short_code: "AB12CD" (6 digits)
    alias: "MyTeam"
    members: [UUID, UUID, ...]
  }
]
```

### Progress + Party Hub (Client-Side)

Version 2.1.0 introduces a dedicated in-game Hub that upgrades the experience from command-only interaction to a focused visual interface.

- **Primary goal**: Make progression and party management faster and easier for players.
- **Main tabs**: Progress and Party in a single screen.
- **Network flow**: `HubRequestPayload` (client requests) + `HubStatePayload` (server snapshot response).
- **Server builder**: `HubSnapshotBuilder` composes portal status, requirement summaries, and party data.
- **Client state**: `HubClientState` stores synchronized snapshot data for rendering.
- **Runtime integration**: `ClientRuntimeHooks` handles keybind open, periodic refresh, and UI synchronization.
- **UI behavior**: Pagination, detail modal, party actions, and responsive layout for different viewport sizes.

Current scope: the Hub is the central UX layer for Progress + Party workflows, while commands remain available as a fallback and power-user path.

### Party Aggregation (How sharing works)

**Scenario:**
- Party: Alice + Bob
- Alice killed 3 Zombies
- Bob killed 2 Zombies

**Requirement: Kill 4 Zombies**

Function `/dp check` in **compact mode**:
1. Checks: Is Alice in a party? ✓ Yes
2. Calls: `RequirementEngine.evaluateParty()` instead of `evaluate()`
3. `evaluateParty()` sums: Alice (3) + Bob (2) = **5 Zombies**
4. Result: ✓ Requirement **COMPLETE** for both!

**Without party:**
- `/dp check` uses `evaluate()` (individual progress)
- Alice sees: 3/4 Zombies
- Bob sees: 2/4 Zombies

### Notification Deduplication

To prevent spam when multiple events fire rapidly:

```java
private static Map<String, Long> RECENT_PARTY_COMPLETIONS = new ConcurrentHashMap<>();
private static final long PARTY_BROADCAST_DEDUPE_MS = 1000L; // 1 second

// Key: "player-uuid|dimension|type|target"
// If same requirement fires 2x in <1s → message sent only 1x
```

---

## 📦 Installation

### Simple Steps

**1. Download the Mod**
- Download from CurseForge: [Dynamic Portals](https://www.curseforge.com/minecraft/mods/dynamic-portals)

**2. Install on Server/Client**
```bash
# Copy the .jar file to the mods/ folder
cp Dynamic Portals-2.0.0.jar ./mods/
```

**3. Choose: Use Defaults or Customize**

**Option A - Use Default Configuration:**
```bash
# Just start the server/client!
# The mod will create config/dynamicportals-common.toml automatically
```

**Option B - Customize First:**
```bash
# 1. Start once to generate the config file
# 2. Edit config/dynamicportals-common.toml with your requirements
# 3. Restart your server
```

### Config File

After starting, you'll have:
```
config/dynamicportals-common.toml
```

Edit this file to:
- Enable/disable chat notifications
- Enable/disable sounds
- Customize requirements (kills, items, advancements)
- Add custom portals

**No recompilation needed!** Just edit the TOML, save, and run `/reload` or restart.

---

## 🌐 Localization

### Supported Languages

| Language | Status |
|----------|--------|
| English (EN-US) | ✅ Complete |
| Português BR (PT-BR) | ✅ Complete |
| Others | Contributions welcome! |

### How to Contribute a Translation

1. Fork the GitHub repository
2. Edit `src/main/resources/assets/dynamicportals/lang/xx_yy.json`
3. Submit a Pull Request (PR)
4. Will be merged quickly!

---

## ❓ FAQ

**Q: Can I use this mod on my multiplayer server?**  
A: Yes! The mod was developed specifically for multiplayer servers with integrated Party System.

**Q: Is it compatible with other mods?**  
A: Yes! You can add requirements for any mob or item from any mod. Just add it to the TOML config.

**Q: Can I disable success sounds?**  
A: Yes! Edit `dynamicportals-common.toml` and set `enableSuccessSound = false`

**Q: How do I reset my progress?**  
A: Delete the `dynamicportals` key from your player NBT file or use NBT editor commands. Admin can use `/data remove entity @s` if needed.

**Q: What's the member limit in a party?**  
A: Maximum 8 members per party. You can create multiple parties!

**Q: Can I edit the config while the server is running?**  
A: Yes! Edit the TOML and execute `/reload` in-game (requires permission).

**Q: How do I contribute translations?**  
A: See the "Localization" section above.

**Q: Found a bug. How do I report it?**  
A: GitHub Issues on the official repository.

**Q: Can I use requirements for specific items (e.g., Enchanted Sword)?**  
A: Currently only basic resource location (minecraft:diamond_sword). Future versions plan support for specific enchantments.

---

## 🎯 Future Roadmap

- 🔄 Automatic progress sync for offline party members
- 📊 Advanced party statistics (total kills, items collected, etc)
- 🎁 Reward system for unlocking portals
- 🔐 Granular permissions for party members
- 🌍 Support for more languages (español, français, deutsch, etc)

---

## 📝 Credits

- **Framework**: NeoForge
- **Build System**: Gradle
- **Mappings**: Parchment

---

**Need help?** Check [GitHub Issues](https://github.com/seu-usuario/dynamic-portals) or join our community!
