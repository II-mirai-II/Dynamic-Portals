# Dynamic Portals Documentation

> Complete gameplay, configuration, command, party, and testing guide.
> Back to [README.md](README.md).

---

## 1. What Dynamic Portals Does

Dynamic Portals lets server owners and modpack makers lock dimension access behind configurable progression. A portal destination can require mob kills, item progress, advancements, or optional bypass items before a player can enter.

The mod is built around three ideas:

- Progress should feel earned.
- Rules should be editable through TOML, without recompiling.
- Multiplayer progression should be easy to understand and reliable.

The default setup includes Nether and End portal access rules, but the same system can be used for vanilla, modded, or custom dimensions.

---

## 2. Quick Player Guide

1. Open the Hub with `H`, or use `/dp check`.
2. Check which portal access requirements are still missing.
3. Complete the listed goals, such as killing mobs or gathering configured items.
4. Once every active requirement for a destination is complete, the portal becomes available.
5. Optional: create a party so friends can share saved progression.

Useful commands:

```txt
/dp check
/dp check nether
/dp check end
/dp check pending
```

When a portal is blocked, the mod shows feedback in chat/overlay and points the player toward the Hub or `/dp check`.

---

## 3. Progress Hub

The Progress Hub is the main visual interface of the mod.

Default keybind:

```txt
H
```

The Hub includes:

- Portal access cards for each active configured destination.
- Status colors, progress bars, and short requirement summaries.
- A Details screen for the full requirement list.
- Custom PNG mob head icons for kill requirements.
- A Party tab for creating, joining, leaving, or dissolving parties.

The server builds the progression snapshot, and the client only renders it. This keeps the UI synchronized with actual server-side progression.

---

## 4. Requirement Types

Dynamic Portals currently supports four requirement paths.

### Kill Requirements

Players must kill a configured entity.

```txt
destination_dimension|entity_id|count
```

Example:

```txt
minecraft:the_nether|minecraft:zombie|10
```

### Item Requirements

Players must gain configured items. The mod tracks inventory increases for those item IDs.

```txt
destination_dimension|item_id|count
```

Example:

```txt
minecraft:the_nether|minecraft:diamond|3
```

### Advancement Requirements

Players must earn a configured advancement.

```txt
destination_dimension|advancement_id
```

Example:

```txt
minecraft:the_end|minecraft:end/kill_dragon
```

### Bypass Items

Bypass items are optional alternate unlock paths. When consumed or used, they unlock that destination for the consuming player.

```txt
destination_dimension|item_id
```

Example:

```txt
minecraft:the_nether|minecraft:magma_cream
```

Bypass unlocks are individual and do not unlock the portal for other party members.

---

## 5. Configuration

The config is generated at:

```txt
config/dynamicportals-common.toml
```

Main sections:

```toml
[general]
enableChatProgress = true
enableSuccessSound = true

[requirements]
killRequirements = [
    "minecraft:the_nether|minecraft:zombie|1",
    "minecraft:the_nether|minecraft:skeleton|1",
    "minecraft:the_end|minecraft:wither_skeleton|1"
]

itemRequirements = [
    "minecraft:the_nether|example:disabled_item|1"
]

advancementRequirements = [
    "minecraft:the_end|example:disabled_advancement"
]

consumeBypassItems = [
    "minecraft:the_nether|minecraft:magma_cream",
    "minecraft:the_end|minecraft:chorus_fruit"
]
```

Lines using the `example:` namespace are illustrative and intentionally ignored.

After changing the config, reload or restart according to your server setup.

---

## 6. Registry Validation and Mod Compatibility

Dynamic Portals validates active requirements against loaded registries.

- Kill requirements require the entity ID to exist.
- Item requirements require the item ID to exist.
- Bypass items require the item ID to exist.
- Invalid IDs or missing modded entities/items are ignored at runtime.
- The TOML is not rewritten when a rule is inactive.

This makes optional mod compatibility simple:

```txt
some_mod:custom_dimension|some_mod:boss_entity|1
```

If `some_mod` is not installed, the rule stays inactive. If the mod is installed and registers that entity, the rule becomes active.

Destination dimensions are checked syntactically, but not forced to exist immediately, which avoids false negatives with datapacks or dynamic dimension systems.

---

## 7. Default Progression

The generated default config currently includes:

- 14 Nether kill requirements.
- 9 End kill requirements.
- Example item and advancement entries that are disabled by design.
- Magma cream as a Nether bypass item.
- Chorus fruit as an End bypass item.

Server owners can replace, remove, or expand all defaults.

---

## 8. Party System

Parties are passwordless and use short invite codes.

Core flow:

```txt
/dp party create
/dp party join <code>
/dp party leave
/dp party info
```

Party behavior:

- Codes are 5 characters long.
- Codes use clear uppercase letters and numbers.
- A party can have up to 8 members.
- The leader can dissolve the party.
- If the leader leaves and members remain, leadership transfers.
- If the last member leaves, the party is deleted.

The Hub Party tab provides the same core flow visually.

---

## 9. Persistent Shared Party Progress

Party progress is saved directly on the party, not calculated only from online players.

When a party member progresses:

- Individual player progress is still updated.
- Party progress is also updated.
- Hub, Details, `/dp check`, and portal access use party progress while the player remains in that party.

Example:

1. Player X creates a party.
2. Player Y joins.
3. X kills 1 Zombie.
4. Y kills 1 Zombie.
5. The party has 2 Zombie kills.
6. Y logs out.
7. X kills another Zombie.
8. The party has 3 Zombie kills.
9. X logs out.
10. Y returns and still sees 3 party Zombie kills.

If a player leaves the party, they stop using that shared party progress and return to their own individual progression.

---

## 10. Commands

### Progress Commands

```txt
/dp check
/dp check help
/dp check summary [page]
/dp check pending [page]
/dp check <dimension>
/dp check <dimension> summary [page]
/dp check <dimension> pending [page]
/dp check <dimension> <page>
```

Common aliases include:

```txt
nether
end
the_nether
the_end
overworld
```

### Party Commands

```txt
/dp party create
/dp party join <code>
/dp party leave
/dp party members
/dp party info
/dp party dissolve
```

### Debug Commands

Debug commands require permission level 2.

```txt
/dp debug complete <dimension> [targets]
/dp debug reset
/dp debug reset <dimension|all>
/dp debug reset <dimension|all> <targets>
/dp debug sword <targets> [count]
```

`/dp debug complete` fills real Dynamic Portals progress for the selected dimension. It does not use a fake bypass.

`/dp debug reset` clears Dynamic Portals progress so players or parties can test progression again.

`/dp debug sword` gives the tester wooden sword, a vanilla-style wooden sword with extremely high base damage for testing kill requirements.

---

## 11. Debug Complete

Command:

```txt
/dp debug complete <dimension> [targets]
```

Behavior:

- Completes the selected active dimension rule.
- Adds only the missing kill/item delta.
- Marks configured advancements in Dynamic Portals progress.
- Marks completed requirements in the same stores used by gameplay.
- If the target is in a party, it also completes the shared party progress.
- Multiple targets in the same party do not duplicate party progress beyond what is needed.

Console or command blocks must provide targets.

---

## 12. Debug Reset

Commands:

```txt
/dp debug reset
/dp debug reset all
/dp debug reset nether
/dp debug reset <dimension|all> <targets>
```

Behavior:

- Clears saved Dynamic Portals progress.
- Can reset all dimensions or a single dimension.
- Resets affected party progress once per party.
- Does not remove party membership, party code, leader, or members.
- Does not remove items, revoke vanilla advancements, or change vanilla stats.
- Item requirements use the current inventory as the new baseline after reset, avoiding instant recount from items already held.

---

## 13. Custom Mob Head Icons

Kill requirements can show custom PNG head icons in the Hub Details screen.

Path format:

```txt
assets/dynamicportals/textures/gui/mob_heads/<namespace>/<path>.png
```

Examples:

```txt
minecraft:zombie
assets/dynamicportals/textures/gui/mob_heads/minecraft/zombie.png

some_mod:bosses/fire_golem
assets/dynamicportals/textures/gui/mob_heads/some_mod/bosses/fire_golem.png
```

Recommended image format:

- PNG.
- 32x32 pixels.
- Transparent background when possible.
- Face centered in the full canvas.

The HUD renders the full 32x32 image scaled down to the UI size. Missing icons use a fallback square with the mob initial.

See [MOB_HEAD_ASSETS.md](MOB_HEAD_ASSETS.md) for the full asset guide.

---

## 14. Data Model Overview

Individual player progress is stored on player persistent data.

Tracked individual data includes:

- Kill counters.
- Item counters.
- Advancement flags.
- Portal unlocked flags.
- Individual bypass unlocked flags.
- Completed requirement flags.
- Inventory snapshots for item tracking.
- Current party ID.

Party data is stored as world `SavedData`.

Tracked party data includes:

- Party ID.
- Current leader/creator.
- 5-character invite code.
- Members.
- Shared kill counters.
- Shared item counters.
- Shared advancement flags.
- Shared completed requirement flags.

Legacy password metadata may still load from old worlds, but new parties are passwordless.

---

## 15. Event Flow

Typical kill requirement flow:

1. A player kills an entity.
2. The mod checks active portal definitions.
3. If that entity is a configured kill target, individual progress increases.
4. If the player is in a party, party progress also increases.
5. Completed requirements are marked.
6. Portal status is evaluated.
7. Chat, sound, Hub, and portal access reflect the updated state.

Portal travel flow:

1. A player attempts to enter a configured destination.
2. The mod evaluates party progress if the player is in a party.
3. Otherwise, it evaluates individual progress.
4. If unlocked, travel proceeds.
5. If blocked, travel is canceled and feedback is shown.

---

## 16. Installation

1. Install NeoForge for Minecraft 1.21.1.
2. Place the Dynamic Portals jar in the `mods` folder.
3. Start the server/client once to generate the config.
4. Edit `config/dynamicportals-common.toml` if desired.
5. Restart or reload as needed.

The mod is allowed in modpacks.

---

## 17. FAQ

**Can I use this on multiplayer servers?**
Yes. The mod is built with multiplayer and parties in mind.

**Do parties require passwords?**
No. Parties now use short 5-character invite codes.

**Does party progress work with offline members?**
Yes. Party progress is saved directly to the party.

**What happens when I leave a party?**
You stop using shared party progress and return to your own individual progress.

**Can I add mobs from other mods?**
Yes. If the mob is registered in the loaded game, the rule becomes active. If not, it stays inactive.

**Can typos in the TOML create impossible requirements?**
No. Invalid or missing entity/item IDs are ignored at runtime.

**Can I reset progress for testing?**
Yes. Use `/dp debug reset` with permission level 2.

**Can I complete requirements for testing?**
Yes. Use `/dp debug complete <dimension> [targets]`.

---

## 18. Localization

Included languages:

- English (US)
- Brazilian Portuguese

Community translations are welcome.

---

## 19. Related Files

- [README.md](README.md): short CurseForge-style overview.
- [MOB_HEAD_ASSETS.md](MOB_HEAD_ASSETS.md): custom mob head asset guide.
- `config/dynamicportals-common.toml`: generated gameplay config.
