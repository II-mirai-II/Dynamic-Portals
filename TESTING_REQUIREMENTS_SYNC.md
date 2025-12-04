# Quick Testing Guide

## How to Test

### 1. Build and Launch
```powershell
# In terminal
./gradlew build
./gradlew runClient
```

### 2. What to Look For

#### Server Logs (when world starts)
```
[DynamicPortals] Server starting - loading mod compatibility...
[DynamicPortals] Loading compatibility config: mowziesmobs.json
[DynamicPortals] Successfully loaded 6 mobs from mowziesmobs.json
[DynamicPortals] Mod compatibility loaded and requirements updated!
```

#### Player Join Logs
```
[DynamicPortals] Player YourName logged in - syncing progress data...
[DynamicPortals] Requirements and progress data synced for player YourName
```

### 3. In-Game Testing

**Open HUD:** Press `K` key

**Expected Results:**
✅ HUD appears in top-right corner
✅ Shows "Portal Requirements" title
✅ Shows "Nether Portal" section with mob list
✅ Shows "End Portal" section with mob list
✅ Vanilla mobs appear: Zombie, Skeleton, Creeper, etc.
✅ **Mowzie's Mobs appear with badges:**
   - Ferrous Wroughtnaut [MOWZIESMOBS]
   - Foliaath [MOWZIESMOBS]
   - Frostmaw [MOWZIESMOBS]
   - Naga [MOWZIESMOBS]
   - Sculptor [MOWZIESMOBS]
   - Umvuthana [MOWZIESMOBS]

### 4. Progress Testing

**Kill a mob** (e.g., Zombie)
- HUD should show green checkmark ✔ next to Zombie
- Counter should update (e.g., 1/15)

**Kill a Mowzie's Mob** (e.g., Foliaath)
- HUD should show green checkmark ✔ next to Foliaath [MOWZIESMOBS]
- Counter should update

### 5. Visual Reference

```
╔═══════════════════════════════════════════════════╗
║           Portal Requirements                     ║
╠═══════════════════════════════════════════════════╣
║ ⚠ Nether Portal (2/15)                           ║
║   ✔ Zombie                                        ║
║   ✘ Skeleton                                      ║
║   ✔ Foliaath [MOWZIESMOBS]                       ║
║   ✘ Frostmaw [MOWZIESMOBS]                       ║
║   ...                                             ║
║                                                   ║
║ ✘ End Portal (0/20)                              ║
║   ✘ Blaze                                         ║
║   ✘ Wither Skeleton                               ║
║   ✘ Ferrous Wroughtnaut [MOWZIESMOBS]            ║
║   ...                                             ║
╚═══════════════════════════════════════════════════╝
```

## Troubleshooting

### HUD is Empty
**Problem:** No requirements showing
**Check:**
1. Look for `Requirements and progress data synced` in logs
2. If missing, check ServerStartingEvent logs
3. Restart server if needed

### Mowzie's Mobs Not Showing
**Problem:** Only vanilla mobs visible
**Check:**
1. Verify Mowzie's Mobs mod is installed in `run/mods/`
2. Check logs for "Successfully loaded 6 mobs from mowziesmobs.json"
3. Ensure entity IDs match (ferrous_wroughtnaut, foliaath, frostmaw, naga, sculptor, umvuthana)

### Progress Not Updating
**Problem:** Killing mobs doesn't add checkmarks
**Check:**
1. MobKillHandler registered (check initialization logs)
2. Progress packet sent on kill (should auto-sync)
3. ClientProgressCache valid flag

### Cache Not Valid
**Problem:** HUD doesn't render at all
**Check:**
1. Both caches must be valid: progress + requirements
2. Re-login to trigger packet send
3. Check for packet registration errors in logs

## Success Criteria
✅ Server starts with compatibility loaded
✅ Player login syncs both packets
✅ HUD displays with K key
✅ Vanilla mobs listed
✅ Mowzie's Mobs listed with [MOWZIESMOBS] badges
✅ Progress tracking functional
✅ No errors in logs

## Files to Check if Issues
1. `logs/latest.log` - All log output
2. `config/` - Mod configs (if any)
3. `run/mods/` - Verify mowziesmobs.jar present
4. `src/main/resources/data/dynamicportals/mod_compat/mowziesmobs.json` - Entity IDs
