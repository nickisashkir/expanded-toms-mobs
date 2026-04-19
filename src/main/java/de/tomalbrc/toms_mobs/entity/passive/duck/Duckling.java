package de.tomalbrc.toms_mobs.entity.passive.duck;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.entity.goal.aquatic.*;
import de.tomalbrc.toms_mobs.registry.MobRegistry;
import de.tomalbrc.toms_mobs.util.BabyGrowthHelper;
import de.tomalbrc.toms_mobs.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public class Duckling extends AbstractDuck {
    public static final Identifier ID = Util.id("duckling");
    public static final Model MODEL = Util.loadBbModel(ID);
    private int babyAge = 0;
    private boolean ageFrozen = false;

    private static final EntityType<?>[] ADULT_DUCKS = { MobRegistry.DUCK_BROWN, MobRegistry.DUCK_MALLARD, MobRegistry.DUCK_WHITE };

    public Duckling(EntityType<? extends @NotNull Animal> type, Level level) {
        super(type, level, MODEL);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new AquaticPanicGoal(this, 1.4));
        this.goalSelector.addGoal(3, new FollowAdultGoal(this, 1.0, a -> a instanceof AbstractDuck && !(a instanceof Duckling)));
        this.goalSelector.addGoal(4, new ImprintOnPlayerGoal(this));
        this.goalSelector.addGoal(5, new AquaticWaterAvoidingRandomStrollGoal(this, 0.4));
        this.goalSelector.addGoal(5, new PathfinderMobSwimGoal(this, 2));
        this.goalSelector.addGoal(8, new AquaticRandomLookAroundGoal(this));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 6.0F));
    }

    private static class ImprintOnPlayerGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final Duckling duckling;
        private Player target;

        ImprintOnPlayerGoal(Duckling duckling) {
            this.duckling = duckling;
            this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            // Only imprint if no adult duck within 12 blocks
            net.minecraft.world.phys.AABB area = duckling.getBoundingBox().inflate(12.0);
            boolean hasAdult = !duckling.level().getEntitiesOfClass(AbstractDuck.class, area,
                    a -> !(a instanceof Duckling) && a.isAlive()).isEmpty();
            if (hasAdult) return false;
            target = duckling.level().getNearestPlayer(duckling, 16.0);
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            if (target == null || !target.isAlive()) return false;
            if (duckling.distanceToSqr(target) > 256) return false;
            net.minecraft.world.phys.AABB area = duckling.getBoundingBox().inflate(12.0);
            return duckling.level().getEntitiesOfClass(AbstractDuck.class, area,
                    a -> !(a instanceof Duckling) && a.isAlive()).isEmpty();
        }

        @Override
        public void tick() {
            if (target == null) return;
            duckling.getLookControl().setLookAt(target, 10.0F, 10.0F);
            if (duckling.distanceToSqr(target) > 9.0 && duckling.getNavigation().isDone()) {
                duckling.getNavigation().moveTo(target, 1.0);
            }
        }

        @Override
        public void stop() {
            target = null;
            duckling.getNavigation().stop();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            babyAge++;
            @SuppressWarnings("unchecked")
            EntityType<? extends Animal> adultType = (EntityType<? extends Animal>) ADULT_DUCKS[this.random.nextInt(ADULT_DUCKS.length)];
            BabyGrowthHelper.tryGrowUp(this, adultType, babyAge, ageFrozen);
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player p, @NotNull InteractionHand h) {
        if (BabyGrowthHelper.tryFreezeGrowth(this, p, h, ageFrozen)) { ageFrozen = true; return InteractionResult.CONSUME; }
        return super.mobInteract(p, h);
    }

    @Override protected void addAdditionalSaveData(@NotNull ValueOutput output) { super.addAdditionalSaveData(output); BabyGrowthHelper.saveAge(output, babyAge, ageFrozen); }
    @Override protected void readAdditionalSaveData(@NotNull ValueInput input) { super.readAdditionalSaveData(input); babyAge = BabyGrowthHelper.loadAge(input); ageFrozen = BabyGrowthHelper.loadFrozen(input); }
}
