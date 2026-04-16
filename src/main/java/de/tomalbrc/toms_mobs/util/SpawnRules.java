package de.tomalbrc.toms_mobs.util;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnRules {
    /**
     * Permissive natural-looking spawn check.
     * Spawns on grass, dirt, sand, stone, terracotta, snow — no man-made blocks.
     * Requires light level > 8 (sunlight counts).
     */
    public static boolean checkNaturalSpawnRules(EntityType<? extends LivingEntity> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (level.getRawBrightness(pos, 0) <= 8) return false;

        BlockState below = level.getBlockState(pos.below());
        return below.is(BlockTags.DIRT)
                || below.is(BlockTags.SAND)
                || below.is(BlockTags.TERRACOTTA)
                || below.is(BlockTags.BADLANDS_TERRACOTTA)
                || below.is(BlockTags.BASE_STONE_OVERWORLD)
                || below.is(BlockTags.SNOW)
                || below.is(BlockTags.ANIMALS_SPAWNABLE_ON);
    }
}
