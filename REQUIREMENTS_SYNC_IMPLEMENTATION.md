# Requirements Synchronization Implementation

## Summary
Complete client-server synchronization system for portal requirements, enabling the HUD to display mod-added entities like Mowzie's Mobs.

## Problem Solved
The ProgressHUD was reading from `PortalRequirementRegistry.getAll()` which only exists on the server. The client-side registry was empty, so no requirements (including Mowzie's Mobs) appeared in the HUD.

## Solution Architecture

### 1. Network Layer - SyncRequirementsPacket
**File:** `src/main/java/com/mirai/dynamicportals/network/SyncRequirementsPacket.java`

- Custom packet using NeoForge's `CustomPacketPayload` system
- Serializes requirements using `ResourceLocation` IDs instead of game objects
- Includes nested `RequirementData` record for dimension data
- StreamCodec handles bidirectional conversion (EntityType ↔ ResourceLocation)

**Key Features:**
- Encodes: advancement, mobs list, bosses list, items list
- Uses VAR_INT encoding for efficient size transmission
- Handles nullable advancement field
- Registered in ModPackets as playToClient packet

### 2. Client Cache - ClientRequirementsCache
**File:** `src/main/java/com/mirai/dynamicportals/client/ClientRequirementsCache.java`

- Stores deserialized requirements on client-side
- Converts ResourceLocations back to game objects (EntityType, Item)
- Provides `CachedRequirement` wrapper class
- Mirrors server-side `PortalRequirement` structure

**API Methods:**
- `updateFromPacket(SyncRequirementsPacket)` - Update cache from network
- `getRequirement(ResourceLocation)` - Get single dimension requirement
- `getAllRequirements()` - Get all requirements as Map
- `isCacheValid()` - Check if cache has been populated
- `clear()` - Reset cache state

### 3. Packet Handler - ClientPacketHandler
**File:** `src/main/java/com/mirai/dynamicportals/client/ClientPacketHandler.java`

Added `handleSyncRequirements()` method:
- Receives packet on client-side
- Enqueues work on main thread (thread-safe)
- Calls `ClientRequirementsCache.updateFromPacket()`

### 4. Network Registration - ModPackets
**File:** `src/main/java/com/mirai/dynamicportals/network/ModPackets.java`

Registered new packet:
```java
registrar.playToClient(
    SyncRequirementsPacket.TYPE,
    SyncRequirementsPacket.STREAM_CODEC,
    ClientPacketHandler::handleSyncRequirements
);
```

### 5. Server Integration - DynamicPortals
**File:** `src/main/java/com/mirai/dynamicportals/DynamicPortals.java`

**New Features:**
- Static `cachedRequirementsPacket` field - stores prepared packet after server start
- `createRequirementsPacket()` method - builds packet from PortalRequirementRegistry
- Enhanced `onServerStarting()` - creates and caches requirements packet
- Enhanced `onPlayerLoggedIn()` - sends both progress AND requirements to client

**Packet Creation Process:**
1. Iterate all requirements from `PortalRequirementRegistry.getAllRequirements()`
2. Convert each `EntityType` to `ResourceLocation` using `BuiltInRegistries`
3. Convert each `Item` to `ResourceLocation`
4. Package into `RequirementData` records
5. Create `SyncRequirementsPacket` with all data
6. Cache for reuse on player login

### 6. HUD Rewrite - ProgressHUD
**File:** `src/main/java/com/mirai/dynamicportals/client/ProgressHUD.java`

**Critical Changes:**
- Removed import of `PortalRequirementRegistry` and `PortalRequirement`
- Changed data source: `PortalRequirementRegistry.getAll()` → `ClientRequirementsCache.getAllRequirements()`
- Updated method signatures to use `ClientRequirementsCache.CachedRequirement`
- Added cache validation: checks both progress AND requirements cache validity
- Added mod badges: displays `[MOWZIESMOBS]` etc. for non-vanilla mobs

**UX Improvements:**
- Mod namespace badges in cyan with dark gray brackets
- Dynamic requirement display based on server-synced data
- Proper progress tracking for mod-added entities

### 7. Performance Optimization - ClientProgressCache
**File:** `src/main/java/com/mirai/dynamicportals/client/ClientProgressCache.java`

**Optimization:**
- Added `killedMobIdsCache` static field
- `getKilledMobIds()` now builds cache once, reuses on subsequent calls
- Cache invalidated on `updateFromPacket()` and `clear()`
- Eliminates O(n) conversion on every HUD frame render

## Data Flow

### Server Startup
```
ServerStartingEvent
  → ModCompatibilityRegistry.loadCompatibilityConfigs()
  → PortalRequirementRegistry.registerVanillaRequirements()
  → DynamicPortals.createRequirementsPacket()
  → cachedRequirementsPacket stored
```

### Player Login
```
PlayerLoggedInEvent
  → Send SyncProgressPacket (player progress)
  → Send SyncRequirementsPacket (portal requirements)
  ↓
Client receives packets
  → ClientPacketHandler.handleSyncProgress()
    → ClientProgressCache.updateFromPacket()
  → ClientPacketHandler.handleSyncRequirements()
    → ClientRequirementsCache.updateFromPacket()
  ↓
Both caches now valid
  → ProgressHUD can render
```

### HUD Rendering (every frame when visible)
```
onRenderGui event
  → Check cacheValid flags (progress + requirements)
  → Read from ClientRequirementsCache (not registry!)
  → Read from ClientProgressCache
  → Render requirements with progress overlay
  → Show mod badges for non-vanilla entities
```

## Files Created
1. `network/SyncRequirementsPacket.java` - Network packet (119 lines)
2. `client/ClientRequirementsCache.java` - Client cache (116 lines)

## Files Modified
1. `network/ModPackets.java` - Added requirements packet registration
2. `client/ClientPacketHandler.java` - Added packet handler
3. `DynamicPortals.java` - Added packet creation and transmission
4. `client/ProgressHUD.java` - Switched data source to cache, added mod badges
5. `client/ClientProgressCache.java` - Added performance caching

## Testing Checklist
- [ ] Server starts without errors
- [ ] Compatibility configs load (check logs)
- [ ] Player can join world
- [ ] Progress packet received (check cache valid)
- [ ] Requirements packet received (check cache valid)
- [ ] HUD opens with K key
- [ ] Vanilla mobs display (Zombie, Skeleton, etc.)
- [ ] **Mowzie's Mobs display with [MOWZIESMOBS] badges**
- [ ] Progress tracking works (kill mobs, see checkmarks)
- [ ] Mod badges show correct namespace

## Expected Log Output
```
[DynamicPortals] Server starting - loading mod compatibility...
[DynamicPortals] Loading compatibility config: mowziesmobs.json
[DynamicPortals] Successfully loaded 6 mobs from mowziesmobs.json
[DynamicPortals] Mod compatibility loaded and requirements updated!
[DynamicPortals] Player <name> logged in - syncing progress data...
[DynamicPortals] Requirements and progress data synced for player <name>
```

## Known Limitations
- Packet cached at server start - dynamic requirement changes require server restart
- No scrolling yet for large requirement lists (HUD capped at 500px height)
- No progress bars (only text counters)

## Next Steps (Not Implemented)
- Scrolling system for overflow content
- Visual progress bars
- Click-to-expand sections
- Item requirements display
- Achievement status indicators
