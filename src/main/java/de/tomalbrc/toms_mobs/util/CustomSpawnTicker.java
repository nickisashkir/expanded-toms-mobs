package de.tomalbrc.toms_mobs.util;

import de.tomalbrc.toms_mobs.config.ModConfig;
import de.tomalbrc.toms_mobs.registry.MobRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomSpawnTicker {
    // Min/max distance from a player to attempt a spawn
    private static final int MIN_SPAWN_RADIUS = 24;
    private static final int MAX_SPAWN_RADIUS = 48;
    private static final int ATTEMPTS_PER_PLAYER = 1;
    // Soft cap radius — count nearby mobs of the same type within this many blocks
    private static final int SOFT_CAP_RADIUS = 128;
    // Immediate proximity check — don't spawn same type if any exist within this radius
    private static final int IMMEDIATE_PROXIMITY_RADIUS = 64;
    // Hard cap on total wild Tom's Mobs of any type within this radius
    private static final int TOTAL_NEARBY_LIMIT = 40;
    private static final int TOTAL_NEARBY_RADIUS = 400;

    public static void tick(ServerLevel level) {
        ModConfig config = ModConfig.getInstance();
        if (!config.customSpawnTicker) return;
        if (level.getServer().getTickCount() % config.customSpawnTickerIntervalTicks != 0) return;
        if (MobRegistry.SPAWN_ENTRIES.isEmpty()) return;

        RandomSource random = level.getRandom();

        // Track which chunks we've already attempted spawns in this tick to avoid
        // multiple players in the same area multiplying spawn pressure
        Set<Long> attemptedChunks = new HashSet<>();

        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) continue;

            // Skip players who have opted out of custom spawning
            if (SpawnOptOut.isOptedOut(player.getUUID())) continue;

            // Hard cap: skip this player entirely if they're already surrounded by Tom's Mobs
            if (countNearbyTomsMobs(level, player) >= TOTAL_NEARBY_LIMIT) continue;

            for (int attempt = 0; attempt < ATTEMPTS_PER_PLAYER; attempt++) {
                trySpawnNearPlayer(level, player, random, config.customSpawnTickerSoftCapPerType, attemptedChunks);
            }
        }
    }

    /**
     * Force a single spawn attempt for a specific player, ignoring tick interval and ticker-enabled state.
     * Returns the number of mobs spawned across all attempts.
     */
    public static int forceSpawn(ServerLevel level, ServerPlayer player, int attempts) {
        if (MobRegistry.SPAWN_ENTRIES.isEmpty()) return 0;
        RandomSource random = level.getRandom();
        Set<Long> attemptedChunks = new HashSet<>();
        int totalSpawned = 0;
        for (int i = 0; i < attempts; i++) {
            totalSpawned += trySpawnNearPlayer(level, player, random, ModConfig.getInstance().customSpawnTickerSoftCapPerType, attemptedChunks);
        }
        return totalSpawned;
    }

    /**
     * Force-spawn a fixed pack of ocean mobs (seahorses, jellyfish, one seal) in water below the player.
     * Used by /tomsmobs forceocean command.
     */
    public static int forceSpawnOcean(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();
        int spawned = 0;

        // 3-4 random seahorses
        EntityType<?>[] seahorses = {
                BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("toms_mobs", "seahorse_black")),
                BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("toms_mobs", "seahorse_blue")),
                BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("toms_mobs", "seahorse_green")),
                BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("toms_mobs", "seahorse_purple")),
                BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("toms_mobs", "seahorse_red"))
        };
        int seahorseCount = 3 + random.nextInt(2);
        for (int i = 0; i < seahorseCount; i++) {
            EntityType<?> type = seahorses[random.nextInt(seahorses.length)];
            if (spawnInWaterNearPlayer(level, player, type, random)) spawned++;
        }

        // 2 random jellyfish
        EntityType<?>[] jellyfish = {
                BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("toms_mobs", "jellyfish_blue")),
                BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("toms_mobs", "jellyfish_orange")),
                BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("toms_mobs", "jellyfish_pink")),
                BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("toms_mobs", "jellyfish_white")),
                BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("toms_mobs", "jellyfish_golden")),
                BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("toms_mobs", "jellyfish_fusion"))
        };
        for (int i = 0; i < 2; i++) {
            EntityType<?> type = jellyfish[random.nextInt(jellyfish.length)];
            if (spawnInWaterNearPlayer(level, player, type, random)) spawned++;
        }

        // 1 random seal
        EntityType<?>[] seals = {
                BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("toms_mobs", "seal_arctic")),
                BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("toms_mobs", "seal_harbor")),
                BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("toms_mobs", "seal_monk"))
        };
        EntityType<?> sealType = seals[random.nextInt(seals.length)];
        if (spawnInWaterNearPlayer(level, player, sealType, random)) spawned++;

        return spawned;
    }

    private static boolean spawnInWaterNearPlayer(ServerLevel level, ServerPlayer player, EntityType<?> type, RandomSource random) {
        if (type == null) return false;
        // Try a few random spots within 10-30 blocks for water
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = 10 + random.nextDouble() * 20;
            int x = (int) (player.getX() + Math.cos(angle) * dist);
            int z = (int) (player.getZ() + Math.sin(angle) * dist);
            // Find water column — start at sea level and scan down
            int seaLevel = level.getSeaLevel();
            for (int y = seaLevel; y > seaLevel - 30; y--) {
                BlockPos pos = new BlockPos(x, y, z);
                if (level.getBlockState(pos).getFluidState().is(net.minecraft.tags.FluidTags.WATER)) {
                    @SuppressWarnings("unchecked")
                    EntityType<? extends Entity> castType = (EntityType<? extends Entity>) type;
                    Entity entity = castType.create(level, EntitySpawnReason.NATURAL);
                    if (entity == null) return false;
                    entity.snapTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            random.nextFloat() * 360.0F, 0.0F);
                    if (entity instanceof Mob mob) {
                        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.NATURAL, null);
                    }
                    level.addFreshEntity(entity);
                    return true;
                }
            }
        }
        return false;
    }

    private static int countNearbyTomsMobs(ServerLevel level, ServerPlayer player) {
        AABB area = player.getBoundingBox().inflate(TOTAL_NEARBY_RADIUS);
        int count = 0;
        for (Entity e : level.getEntities((Entity) null, area, en -> en.getCustomName() == null && isTomsMob(en.getType()))) {
            count++;
        }
        return count;
    }

    private static boolean isTomsMob(EntityType<?> type) {
        for (SpawnEntry entry : MobRegistry.SPAWN_ENTRIES) {
            if (entry.type() == type) return true;
        }
        return false;
    }

    private static int trySpawnNearPlayer(ServerLevel level, ServerPlayer player, RandomSource random, int softCap, Set<Long> attemptedChunks) {
        // Pick a random position around the player
        double angle = random.nextDouble() * Math.PI * 2;
        double dist = MIN_SPAWN_RADIUS + random.nextDouble() * (MAX_SPAWN_RADIUS - MIN_SPAWN_RADIUS);
        int x = (int) (player.getX() + Math.cos(angle) * dist);
        int z = (int) (player.getZ() + Math.sin(angle) * dist);

        // Dedupe per-chunk so multiple players in same area don't multiply spawns
        long chunkKey = ChunkPos.pack(x >> 4, z >> 4);
        if (!attemptedChunks.add(chunkKey)) return 0;

        if (!level.hasChunkAt(new BlockPos(x, 0, z))) return 0;

        // Find a surface position
        int surfaceY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (surfaceY < level.getMinY() + 1 || surfaceY > level.getMaxY() - 1) return 0;

        BlockPos spawnPos = new BlockPos(x, surfaceY, z);
        var biomeHolder = level.getBiome(spawnPos);

        // Find spawn entries that match this biome
        List<SpawnEntry> matches = new ArrayList<>();
        int totalWeight = 0;
        for (SpawnEntry entry : MobRegistry.SPAWN_ENTRIES) {
            if (entry.matchesBiome(biomeHolder)) {
                matches.add(entry);
                totalWeight += entry.weight();
            }
        }
        if (matches.isEmpty()) return 0;

        // Pick a weighted random entry
        int roll = random.nextInt(totalWeight);
        SpawnEntry chosen = null;
        for (SpawnEntry entry : matches) {
            roll -= entry.weight();
            if (roll < 0) {
                chosen = entry;
                break;
            }
        }
        if (chosen == null) return 0;

        // Build category set: types that share this entry's category (e.g. all dogs, all cats)
        java.util.Set<EntityType<?>> categoryTypes;
        if (chosen.category() != null && !chosen.category().isEmpty()) {
            categoryTypes = new java.util.HashSet<>();
            for (SpawnEntry entry : MobRegistry.SPAWN_ENTRIES) {
                if (chosen.category().equals(entry.category())) categoryTypes.add(entry.type());
            }
        } else {
            categoryTypes = java.util.Set.of(chosen.type());
        }

        // Immediate proximity check: never spawn same-category mobs right next to existing ones
        AABB immediateArea = new AABB(spawnPos).inflate(IMMEDIATE_PROXIMITY_RADIUS);
        if (!level.getEntities((Entity) null, immediateArea, en -> en.getCustomName() == null && categoryTypes.contains(en.getType())).isEmpty()) return 0;

        // Per-type/category cap: use the entry's own maxNearby if set (>0), else fall back to global softCap
        int effectiveCap = chosen.maxNearby() > 0 ? chosen.maxNearby() : softCap;
        AABB softCapArea = new AABB(spawnPos).inflate(SOFT_CAP_RADIUS);
        int existingSameType = 0;
        for (Entity ignored : level.getEntities((Entity) null, softCapArea, en -> en.getCustomName() == null && categoryTypes.contains(en.getType()))) {
            existingSameType++;
            if (existingSameType >= effectiveCap) return 0;
        }

        // Hard cap check at spawn position (in addition to per-player check) —
        // prevents other players triggering spawns into an already-full area.
        AABB hardCapArea = new AABB(spawnPos).inflate(TOTAL_NEARBY_RADIUS);
        int existingTotal = 0;
        for (Entity ignored : level.getEntities((Entity) null, hardCapArea, en -> en.getCustomName() == null && isTomsMob(en.getType()))) {
            existingTotal++;
            if (existingTotal >= TOTAL_NEARBY_LIMIT) return 0;
        }

        // Validate the spawn position using natural-block rules (no man-made blocks)
        if (!SpawnRules.checkNaturalSpawnRules(null, level, null, spawnPos, random)) return 0;
        if (!level.getBlockState(spawnPos).isAir()) return 0;
        if (!level.getBlockState(spawnPos.above()).isAir()) return 0;

        // Calculate group size but clamp so we don't exceed caps
        int groupSize = chosen.minGroup() + random.nextInt(chosen.maxGroup() - chosen.minGroup() + 1);
        int softRoom = effectiveCap - existingSameType;
        int hardRoom = TOTAL_NEARBY_LIMIT - existingTotal;
        groupSize = Math.min(groupSize, Math.min(softRoom, hardRoom));
        if (groupSize <= 0) return 0;

        int spawned = 0;
        for (int i = 0; i < groupSize; i++) {
            int dx = random.nextInt(5) - 2;
            int dz = random.nextInt(5) - 2;
            BlockPos memberPos = new BlockPos(spawnPos.getX() + dx, spawnPos.getY(), spawnPos.getZ() + dz);
            int memberY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, memberPos.getX(), memberPos.getZ());
            BlockPos finalPos = new BlockPos(memberPos.getX(), memberY, memberPos.getZ());

            EntityType<? extends Entity> type = (EntityType<? extends Entity>) chosen.type();
            Entity entity = type.create(level, EntitySpawnReason.NATURAL);
            if (entity == null) continue;

            entity.snapTo(finalPos.getX() + 0.5, finalPos.getY(), finalPos.getZ() + 0.5,
                    random.nextFloat() * 360.0F, 0.0F);
            if (entity instanceof Mob mob) {
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(finalPos), EntitySpawnReason.NATURAL, null);
            }
            level.addFreshEntity(entity);
            spawned++;
        }

        // Spawn babies alongside the adult group based on babyChance
        if (chosen.babyType() != null && chosen.babyChance() > 0.0) {
            for (int i = 0; i < groupSize; i++) {
                if (random.nextDouble() >= chosen.babyChance()) continue;
                int dx = random.nextInt(5) - 2;
                int dz = random.nextInt(5) - 2;
                BlockPos memberPos = new BlockPos(spawnPos.getX() + dx, spawnPos.getY(), spawnPos.getZ() + dz);
                int memberY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, memberPos.getX(), memberPos.getZ());
                BlockPos finalPos = new BlockPos(memberPos.getX(), memberY, memberPos.getZ());

                @SuppressWarnings("unchecked")
                EntityType<? extends Entity> babyType = (EntityType<? extends Entity>) chosen.babyType();
                Entity baby = babyType.create(level, EntitySpawnReason.NATURAL);
                if (baby == null) continue;

                baby.snapTo(finalPos.getX() + 0.5, finalPos.getY(), finalPos.getZ() + 0.5,
                        random.nextFloat() * 360.0F, 0.0F);
                if (baby instanceof Mob mob) {
                    mob.finalizeSpawn(level, level.getCurrentDifficultyAt(finalPos), EntitySpawnReason.NATURAL, null);
                }
                level.addFreshEntity(baby);
                spawned++;
            }
        }

        return spawned;
    }
}
