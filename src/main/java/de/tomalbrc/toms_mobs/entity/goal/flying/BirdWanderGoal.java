package de.tomalbrc.toms_mobs.entity.goal.flying;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Flying bird wander - picks random air positions to fly to.
 */
public class BirdWanderGoal extends Goal {
    private final PathfinderMob bird;
    private final double speedModifier;
    private Vec3 target;

    public BirdWanderGoal(PathfinderMob bird, double speedModifier) {
        this.bird = bird;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.bird.getNavigation().isInProgress()) return false;
        if (this.bird.getRandom().nextInt(30) != 0) return false;
        this.target = findTarget();
        return this.target != null;
    }

    private Vec3 findTarget() {
        Vec3 viewVec = this.bird.getViewVector(0.0F);
        // Try to pick a position in the air up to 8 blocks away, 3-7 blocks up
        Vec3 pos = HoverRandomPos.getPos(this.bird, 8, 7, viewVec.x, viewVec.z, ((float) Math.PI / 2F), 3, 1);
        if (pos != null) return pos;
        return AirAndWaterRandomPos.getPos(this.bird, 8, 4, -2, viewVec.x, viewVec.z, ((float) Math.PI / 2F));
    }

    @Override
    public boolean canContinueToUse() {
        return this.bird.getNavigation().isInProgress();
    }

    @Override
    public void start() {
        this.bird.getNavigation().moveTo(this.target.x, this.target.y, this.target.z, this.speedModifier);
    }
}
