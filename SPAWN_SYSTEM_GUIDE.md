# Tom's Mobs — Custom Spawn System Guide

## Overview

The mod has **two spawn systems** working together:

1. **Vanilla Biome Spawning** — Fabric's `BiomeModifications.addSpawn()` adds mobs to biome spawn pools
2. **Custom Spawn Ticker** — Our own ticker that periodically spawns mobs near players, bypassing vanilla's mob cap

---

## 1. Vanilla Biome Spawn Registration

**File:** `MobRegistry.java` → `registerMobs()`

This runs at world load via a Mixin (`WorldLoaderMixin`). It reads `default-spawns.json` and registers each entry with Fabric's biome modification API.

```java
// Each entity type has a spawn placement registered at definition time:
public static final EntityType<Giraffe> GIRAFFE = register(Giraffe.ID,
    FabricEntityType.Builder.createMob(Giraffe::new, MobCategory.CREATURE,
        x -> x.defaultAttributes(Giraffe::createAttributes)
              .spawnPlacement(SpawnPlacementTypes.ON_GROUND,
                  Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                  SpawnRules::checkNaturalSpawnRules))  // ← spawn rule predicate
    .sized(0.6f, 1.8f));

// Then at world load, biome spawns are registered from JSON config:
BiomeHelper.addSpawn(entityType, weight, minGroup, maxGroup, context -> {
    // Check biome tags (e.g. #c:is_savanna)
    for (TagKey<Biome> tag : tagKeys) {
        if (context.hasTag(tag)) return true;
    }
    // Check direct biome IDs (e.g. terralith:blooming_valley)
    for (ResourceKey<Biome> key : directBiomes) {
        if (context.getBiomeKey().equals(key)) return true;
    }
    return false;
});
```

**Spawn Rules** (`SpawnRules.java`):
```java
// Only spawn on natural blocks — prevents spawning on roofs/builds
public static boolean checkNaturalSpawnRules(EntityType<?> type,
        LevelAccessor level, EntitySpawnReason reason,
        BlockPos pos, RandomSource random) {
    if (level.getRawBrightness(pos, 0) <= 8) return false;
    BlockState below = level.getBlockState(pos.below());
    return below.is(BlockTags.DIRT)
            || below.is(BlockTags.SAND)
            || below.is(BlockTags.TERRACOTTA)
            || below.is(BlockTags.BASE_STONE_OVERWORLD)
            || below.is(BlockTags.SNOW)
            || below.is(BlockTags.ANIMALS_SPAWNABLE_ON);
}
```

---

## 2. Spawn Config Format (`default-spawns.json`)

```json
{
  "mob": "toms_mobs:giraffe",
  "biomes": ["#c:is_savanna", "terralith:ashen_savanna"],
  "weight": 2,
  "min-group": 1,
  "max-group": 3,
  "max-nearby": 4,
  "category": "",
  "baby": "toms_mobs:giraffe_baby",
  "baby-chance": 0.4
}
```

| Field | Purpose |
|-------|---------|
| `mob` | Entity type ID |
| `biomes` | List of biome IDs or `#tag` references |
| `weight` | Spawn weight (higher = more common) |
| `min-group` / `max-group` | Group size range |
| `max-nearby` | Per-type cap within 128 blocks (-1 = use global default) |
| `category` | Shared cap group (e.g. "dog" — all dog breeds share one cap) |
| `baby` | Baby entity type to spawn alongside adults |
| `baby-chance` | Chance per adult to also spawn a baby (0.0–1.0) |

---

## 3. Custom Spawn Ticker (`CustomSpawnTicker.java`)

Registered as a server tick event:
```java
// In TomsMobs.onInitialize():
ServerTickEvents.END_LEVEL_TICK.register(CustomSpawnTicker::tick);
```

### Tick Flow:

```
Every N ticks (default 24000 = 20 minutes):
  For each online player:
    ├── Skip if spectator
    ├── Skip if player opted out (/toggleanimals)
    ├── Skip if 40+ Tom's Mobs within 400 blocks (hard cap)
    └── Try 1 spawn attempt:
        ├── Pick random position 24-48 blocks from player
        ├── Dedupe by chunk (prevents multi-player overlap)
        ├── Check biome → find matching spawn entries
        ├── Weighted random pick from matches
        ├── Proximity check: skip if same type within 64 blocks
        ├── Per-type cap: skip if max-nearby reached in 128 blocks
        ├── Category cap: shared across breeds (e.g. all dogs count together)
        ├── Hard cap: skip if 40+ total Tom's Mobs in 400 blocks
        ├── Block validation: must be natural block below + air above
        ├── Clamp group size to remaining cap room
        ├── Spawn adult group
        └── Roll baby-chance per adult → spawn babies alongside
```

### Key Constants:
```java
MIN_SPAWN_RADIUS = 24;       // Min distance from player
MAX_SPAWN_RADIUS = 48;       // Max distance from player
ATTEMPTS_PER_PLAYER = 1;     // Spawn attempts per tick cycle
SOFT_CAP_RADIUS = 128;       // Per-type cap check radius
IMMEDIATE_PROXIMITY_RADIUS = 64;  // No same-type within this range
TOTAL_NEARBY_LIMIT = 40;     // Hard cap on ALL Tom's Mobs
TOTAL_NEARBY_RADIUS = 400;   // Hard cap check radius
```

---

## 4. Force Spawn (`/tomsmobs forcespawn`)

Bypasses the tick interval and enabled state. Runs the same `trySpawnNearPlayer()` logic but on demand:

```java
public static int forceSpawn(ServerLevel level, ServerPlayer player, int attempts) {
    RandomSource random = level.getRandom();
    Set<Long> attemptedChunks = new HashSet<>();
    int totalSpawned = 0;
    for (int i = 0; i < attempts; i++) {
        totalSpawned += trySpawnNearPlayer(level, player, random,
            ModConfig.getInstance().customSpawnTickerSoftCapPerType, attemptedChunks);
    }
    return totalSpawned;
}
```

Still respects all caps (soft cap, hard cap, proximity check). Safe to spam — won't overcrowd.

---

## 5. Force Ocean Spawn (`/tomsmobs forceocean`)

Spawns a fixed pack of ocean mobs in water near the player:
- 3-4 random seahorses
- 2 random jellyfish
- 1 random seal

```java
// Finds water by scanning down from sea level:
for (int y = seaLevel; y > seaLevel - 30; y--) {
    BlockPos pos = new BlockPos(x, y, z);
    if (level.getBlockState(pos).getFluidState().is(FluidTags.WATER)) {
        Entity entity = type.create(level, EntitySpawnReason.NATURAL);
        entity.snapTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ...);
        level.addFreshEntity(entity);
        return true;
    }
}
```

---

## 6. Config (`config/toms_mobs.json`)

```json
{
  "spawns": [...],
  "disabledMobs": [],
  "noAdditionalRaidMobs": true,
  "custom_spawn_ticker": true,
  "custom_spawn_ticker_interval_ticks": 24000,
  "custom_spawn_ticker_soft_cap_per_type": 12
}
```

**Must delete this file** when updating spawn entries in `default-spawns.json` — the config caches the spawns on first run.

---

## 7. Admin Commands

| Command | What it does |
|---------|-------------|
| `/tomsmobs status` | Show ticker state |
| `/tomsmobs ticker true/false` | Toggle ticker |
| `/tomsmobs interval <ticks>` | Set tick interval |
| `/tomsmobs softcap <n>` | Set default per-type cap |
| `/tomsmobs forcespawn [n]` | Force n spawn attempts |
| `/tomsmobs forceocean` | Force ocean mob pack |
| `/tomsmobs count [radius]` | Count nearby Tom's Mobs |
| `/tomsmobs total` | Count server-wide |
| `/tomsmobs reload` | Reload config from disk |
| `/toggleanimals` | Player opt-out (no OP needed) |
