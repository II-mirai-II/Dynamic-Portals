# Dynamic Portals 3.0.0 Changelog

Version 3.0.0 is a major polish and systems update focused on making portal progression clearer, party play simpler, and testing/debugging much more reliable. ✨

## Highlights

- Reworked the Progress Hub layout with cleaner hierarchy, centered portal cards, improved spacing, better progress bars, and a more polished dark UI.
- Rebuilt the Details view as its own screen instead of a modal overlay, making requirements easier to read without the main Hub showing behind it.
- Added custom mob head icons for kill requirements, rendered from PNG assets in the mod resource folder.
- Simplified the Party system into a short-code flow: create with one click, join with a 5-character code.
- Added persistent shared party progression saved directly to the party, so offline members still benefit from progress made by the group.
- Added powerful debug commands to complete or reset real progression for testing.
- Improved config safety so invalid or missing modded entities/items no longer create impossible requirements.

## Hub & UI Improvements 🎨

- Removed the unnecessary Refresh button from the main Progress tab.
- Moved Progress and Party navigation to a cleaner lower control bar.
- Prev/Next buttons now only appear when there is actually another page to navigate.
- Portal cards now use cleaner titles like `NETHER PORTAL ACCESS` and `END PORTAL ACCESS`.
- Requirement Details now show only the page counter, such as `1/1`, without extra text.
- Improved card spacing, progress summaries, status colors, and visual hierarchy.
- Refined the Party tab into a simpler, cleaner, more focused screen.

## Custom Mob Head Requirement Icons 🧟

- Kill requirements can now show a custom mob face icon, mob name, and counter.
- Asset path:

```txt
assets/dynamicportals/textures/gui/mob_heads/<namespace>/<path>.png
```

- Example:

```txt
minecraft:zombie -> assets/dynamicportals/textures/gui/mob_heads/minecraft/zombie.png
```

- Added fallback rendering for mobs without a custom PNG.
- Fixed PNG rendering so 32x32 icons are drawn fully and scaled down correctly in the HUD.
- Added `MOB_HEAD_ASSETS.md` with full instructions for creating and naming mob head assets.

## Party System Overhaul 👥

- Parties now use short 5-character invite codes made from clear letters and numbers.
- Removed password-based party creation and joining from the main flow.
- Party creation is now one click.
- Joining only requires the party code.
- Removed unnecessary Party refresh controls from the Hub.
- Party leadership now transfers automatically when the leader leaves and members remain.
- Dissolve remains available only to the current leader.
- Old party data remains compatible where possible.

## Persistent Shared Party Progression 💾

- Parties now have their own saved progression state.
- Kills, item progress, advancements, and completed requirements are stored collectively for the party.
- If one member progresses while another is offline, the offline member sees that progress when they return.
- Portal access, Hub cards, Details, and `/dp check` now use saved party progress while the player is in a party.
- Individual progress is still preserved in parallel.
- Leaving a party returns the player to their own individual progress.

## Safer Config Requirements ⚙️

- Kill requirements are now validated against the loaded entity registry.
- Item requirements and bypass items are now validated against the loaded item registry.
- Invalid IDs, typos, or requirements from missing mods are ignored instead of becoming impossible blockers.
- Optional modded requirements can be safely listed in the default config and will activate only when the required mod is installed.
- The generated `.toml` comments were improved to explain optional compatibility entries.

## Debug & Testing Tools 🛠️

- Added `/dp debug complete <dimension> [targets]`.
- This command completes real Dynamic Portals progress instead of using a fake bypass.
- It fills missing kills, items, and advancements through the same progression stores used by gameplay.
- It also supports party progress without duplicating progress for multiple targets in the same party.
- Added `/dp debug reset [dimension|all] [targets]`.
- This resets Dynamic Portals progression organically for a player or selected targets.
- Reset supports all progress or a single dimension.
- Party progress is reset once per affected party while preserving membership, leader, and invite code.
- Added smart autocomplete for debug dimensions and aliases such as `nether`, `end`, and `all`.

## Debug Sword Improvements ⚔️

- Reworked the Debug Sword to behave like a vanilla wooden sword with extremely high base damage.
- Removed manual second-hit damage behavior.
- Direct hits and vanilla sweep attacks now use the sword's normal damage mechanics.
- The sword remains a simple testing tool without granting special unlocks or bypasses.

## Compatibility & Stability

- No network payload schema changes were required for the Hub improvements.
- Progress snapshots remain server-generated and client-rendered.
- Config defaults can now include optional future compatibility entries more safely.
- Several visual edge cases were cleaned up to avoid overlap, hidden widgets, and confusing inactive controls.

Thanks for testing Dynamic Portals 3.0.0. This version is a big step toward a cleaner, more reliable, and more community-friendly progression system. 🚪
