package de.tomalbrc.toms_mobs.entity.goal.flying;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

/**
 * Bird flies to a nearby block and perches there for a while.
 * Only lands on leaves, logs, dirt/grass, stone, or planks.
 */
public class BirdPerchGoal extends Goal {
    private final PathfinderMob bird;
    private final double speedModifier;
    private final int triggerChance;
    private final int minPerchTicks;
    private final int maxPerchTicks;
    private BlockPos perchTarget;
    private int perchTicks;
    private float perchYRot;
    private int navigationTimeout;
    private boolean hasArrived;

    // Give up navigating after 10 seconds if the bird hasn't arrived
    private static final int MAX_NAVIGATE_TICKS = 200;

    public BirdPerchGoal(PathfinderMob bird, double speedModifier, int triggerChance, int minPerchTicks, int maxPerchTicks) {
        this.bird = bird;
        this.speedModifier = speedModifier;
        this.triggerChance = triggerChance;
        this.minPerchTicks = minPerchTicks;
        this.maxPerchTicks = maxPerchTicks;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.bird.getRandom().nextInt(this.triggerChance) != 0) return false;
        this.perchTarget = findPerchTarget();
        return this.perchTarget != null;
    }

    private BlockPos findPerchTarget() {
        Level level = this.bird.level();
        BlockPos birdPos = this.bird.blockPosition();
        BlockPos treeTarget = null;
        BlockPos groundTarget = null;
        for (int attempt = 0; attempt < 16; attempt++) {
            int dx = this.bird.getRandom().nextInt(16) - 8;
            int dz = this.bird.getRandom().nextInt(16) - 8;
            int dy = this.bird.getRandom().nextInt(8) - 2;
            BlockPos candidate = birdPos.offset(dx, dy, dz);
            BlockState state = level.getBlockState(candidate);
            // Need 2 clear blocks above the surface so the bird isn't wedged inside canopy
            BlockState above1 = level.getBlockState(candidate.above());
            BlockState above2 = level.getBlockState(candidate.above(2));
            if (!above1.isAir() || !above2.isAir()) continue;
            BlockPos perchPos = candidate.above();
            if (state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS)) {
                if (treeTarget == null) treeTarget = perchPos;
            } else if (state.is(BlockTags.DIRT) || state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.PLANKS)) {
                if (groundTarget == null) groundTarget = perchPos;
            }
        }
        return treeTarget != null ? treeTarget : groundTarget;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.perchTarget == null) return false;
        if (!this.bird.getNavigation().isDone()) return this.navigationTimeout > 0;
        // IMPORTANT: canContinueToUse() is evaluated before tick() in Minecraft's goal selector.
        // When navigation finishes, perchTicks is still 0 — we need one tick to run so tick()
        // can do the arrival check and set perchTicks. hasArrived gates that window.
        if (!this.hasArrived) return true;
        return this.perchTicks > 0;
    }

    @Override
    public void start() {
        this.bird.getNavigation().moveTo(
            this.perchTarget.getX() + 0.5,
            this.perchTarget.getY(),
            this.perchTarget.getZ() + 0.5,
            this.speedModifier
        );
        this.perchTicks = 0;
        this.hasArrived = false;
        this.navigationTimeout = MAX_NAVIGATE_TICKS;
    }

    @Override
    public void tick() {
        if (!this.bird.getNavigation().isDone()) {
            this.navigationTimeout--;
            return;
        }
        if (!this.hasArrived) {
            this.hasArrived = true;
            double distSq = this.bird.position().distanceToSqr(
                this.perchTarget.getX() + 0.5,
                this.perchTarget.getY(),
                this.perchTarget.getZ() + 0.5
            );
            if (distSq > 9.0) {
                this.perchTarget = null; // canContinueToUse() returns false next check
                return;
            }
            this.perchTicks = this.minPerchTicks + this.bird.getRandom().nextInt(this.maxPerchTicks - this.minPerchTicks);
            this.bird.setPos(this.perchTarget.getX() + 0.5, this.perchTarget.getY(), this.perchTarget.getZ() + 0.5);
            this.perchYRot = this.bird.getYRot();
        }
        this.bird.setDeltaMovement(0, 0, 0);
        this.bird.getNavigation().stop();
        this.bird.setYRot(this.perchYRot);
        this.bird.setYBodyRot(this.perchYRot);
        this.bird.setYHeadRot(this.perchYRot);
        this.bird.setXRot(0);
        this.perchTicks--;
    }

    @Override
    public void stop() {
        this.perchTarget = null;
        this.perchTicks = 0;
        this.hasArrived = false;
        this.navigationTimeout = 0;
    }
}
