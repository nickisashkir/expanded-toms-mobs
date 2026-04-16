package de.tomalbrc.toms_mobs.entity.goal.aquatic;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * Makes a baby mob follow any nearby adult matching the filter.
 * Unlike vanilla FollowParentGoal, this doesn't require the adult to be the same class.
 */
public class FollowAdultGoal extends Goal {
    private final Animal baby;
    private final double speedModifier;
    private final Predicate<Animal> adultFilter;
    private Animal adult;
    private int timeToRecalcPath;

    public FollowAdultGoal(Animal baby, double speedModifier, Predicate<Animal> adultFilter) {
        this.baby = baby;
        this.speedModifier = speedModifier;
        this.adultFilter = adultFilter;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        List<Animal> nearby = this.baby.level().getEntitiesOfClass(
            Animal.class,
            this.baby.getBoundingBox().inflate(12.0, 4.0, 12.0),
            a -> a != this.baby && this.adultFilter.test(a) && !a.isBaby()
        );
        if (nearby.isEmpty()) return false;

        Animal closest = null;
        double closestDist = Double.MAX_VALUE;
        for (Animal a : nearby) {
            double d = this.baby.distanceToSqr(a);
            if (d < closestDist) {
                closestDist = d;
                closest = a;
            }
        }

        if (closestDist < 9.0) return false; // close enough
        this.adult = closest;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.adult == null || !this.adult.isAlive()) return false;
        double d = this.baby.distanceToSqr(this.adult);
        return d >= 6.25 && d <= 256.0;
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        this.adult = null;
    }

    @Override
    public void tick() {
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            this.baby.getNavigation().moveTo(this.adult, this.speedModifier);
        }
    }
}
