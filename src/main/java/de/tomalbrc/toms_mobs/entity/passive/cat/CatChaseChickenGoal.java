package de.tomalbrc.toms_mobs.entity.passive.cat;

import de.tomalbrc.toms_mobs.entity.passive.winterbird.AbstractWinterBird;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class CatChaseChickenGoal extends Goal {
    private final PathfinderMob cat;
    private LivingEntity target;
    private int cooldown = 0;
    private int chaseTicks = 0;

    public CatChaseChickenGoal(PathfinderMob cat) {
        this.cat = cat;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) { cooldown--; return false; }
        // Tamed cats only chase occasionally, wild cats more often
        boolean isTamed = cat instanceof TamableAnimal ta && ta.isTame();
        int chance = isTamed ? 200 : 80;
        if (cat.getRandom().nextInt(chance) != 0) return false;

        AABB area = cat.getBoundingBox().inflate(10.0);
        List<LivingEntity> prey = new ArrayList<>();
        prey.addAll(cat.level().getEntitiesOfClass(Chicken.class, area));
        // Wild cats also chase winter birds
        if (!isTamed) {
            prey.addAll(cat.level().getEntitiesOfClass(AbstractWinterBird.class, area));
        }
        if (prey.isEmpty()) return false;
        target = prey.get(cat.getRandom().nextInt(prey.size()));
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && chaseTicks < 100 && cat.distanceToSqr(target) < 400;
    }

    @Override
    public void start() {
        chaseTicks = 0;
        // Scare the target so it flees
        if (target instanceof Mob mob) {
            mob.setLastHurtByMob(cat);
        }
    }

    @Override
    public void stop() {
        target = null;
        chaseTicks = 0;
        cooldown = 200 + cat.getRandom().nextInt(200); // 10-20 sec cooldown
        cat.getNavigation().stop();
    }

    @Override
    public void tick() {
        chaseTicks++;
        if (target == null) return;
        cat.getLookControl().setLookAt(target, 30.0F, 30.0F);
        // Stop chasing if too close (just stand and stare to avoid damaging)
        double dist = cat.distanceToSqr(target);
        if (dist < 2.0) {
            cat.getNavigation().stop();
        } else {
            cat.getNavigation().moveTo(target, 1.2);
        }
    }
}
