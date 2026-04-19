package de.tomalbrc.toms_mobs.entity.passive.seahorse;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.entity.control.SemiAquaticMoveControl;
import de.tomalbrc.toms_mobs.entity.goal.aquatic.AquaticPanicGoal;
import de.tomalbrc.toms_mobs.entity.goal.aquatic.PathfinderMobSwimGoal;
import de.tomalbrc.toms_mobs.util.AnimationHelper;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import net.tslat.smartbrainlib.api.core.navigation.SmoothWaterBoundPathNavigation;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractSeahorse extends AbstractFish implements AnimatedEntity {
    private final EntityHolder<? extends AbstractSeahorse> holder;
    private int twirlTicks = 0;

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return AbstractFish.createAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.8)
                .add(Attributes.MAX_HEALTH, 8.0);
    }

    public static boolean checkSeahorseSpawnRules(EntityType<? extends @NotNull LivingEntity> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        int seaLevel = levelAccessor.getSeaLevel();
        return blockPos.getY() >= seaLevel - 20
                && blockPos.getY() <= seaLevel - 2
                && levelAccessor.getFluidState(blockPos).is(FluidTags.WATER)
                && levelAccessor.getBlockState(blockPos.above()).is(Blocks.WATER);
    }

    @Override
    public EntityHolder<? extends AbstractSeahorse> getHolder() {
        return this.holder;
    }

    protected AbstractSeahorse(EntityType<? extends @NotNull AbstractFish> type, Level level, Model model) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 0.F);
        this.moveControl = new SemiAquaticMoveControl(this);
        this.holder = new LivingEntityHolder<>(this, model);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 3;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new AquaticPanicGoal(this, 1.2));
        this.goalSelector.addGoal(1, new CuriousOfPlayerGoal(this));
        this.goalSelector.addGoal(2, new PathfinderMobSwimGoal(this, 1));
    }

    /**
     * Seahorse curiosity. When a player enters 8 blocks while seahorse is in water,
     * 50% chance to trigger a curious state lasting 30-60 seconds. During curiosity
     * the seahorse swims toward the player but stops 3 blocks short. Then cooldown.
     */
    private static class CuriousOfPlayerGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final AbstractSeahorse seahorse;
        private Player target;
        private int endTick = 0;
        private int cooldownUntil = 0;

        CuriousOfPlayerGoal(AbstractSeahorse seahorse) {
            this.seahorse = seahorse;
            this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (seahorse.tickCount < cooldownUntil) return false;
            if (!seahorse.isInWater()) return false;
            Player near = seahorse.level().getNearestPlayer(seahorse, 8.0);
            if (near == null || !near.isAlive()) return false;
            // 50% chance to take interest
            if (seahorse.random.nextInt(2) != 0) {
                // Avoid rolling again immediately
                cooldownUntil = seahorse.tickCount + 100;
                return false;
            }
            target = near;
            // 30-60 seconds of interest
            endTick = seahorse.tickCount + 600 + seahorse.random.nextInt(601);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (target == null || !target.isAlive()) return false;
            if (seahorse.tickCount > endTick) return false;
            if (!seahorse.isInWater()) return false;
            if (seahorse.distanceToSqr(target) > 400) return false; // player got too far away
            return true;
        }

        @Override
        public void tick() {
            if (target == null) return;
            seahorse.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distSqr = seahorse.distanceToSqr(target);
            if (distSqr > 9.0) { // stop 3 blocks short
                if (seahorse.getNavigation().isDone()) {
                    seahorse.getNavigation().moveTo(target, 1.0);
                }
            } else {
                seahorse.getNavigation().stop();
            }
        }

        @Override
        public void stop() {
            target = null;
            // 30-60 second cooldown before potentially re-engaging
            cooldownUntil = seahorse.tickCount + 600 + seahorse.random.nextInt(601);
            seahorse.getNavigation().stop();
        }
    }

    @Override
    public float getWalkTargetValue(@NotNull BlockPos blockPos, LevelReader levelReader) {
        if (levelReader.getFluidState(blockPos).is(FluidTags.WATER)) {
            return 1;
        }
        return levelReader.getPathfindingCostFromLightLevels(blockPos);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount % 2 == 0) {
            if (twirlTicks > 0) {
                twirlTicks -= 2;
                this.holder.getAnimator().playAnimation("twirl");
                this.holder.getAnimator().pauseAnimation("walk");
                this.holder.getAnimator().pauseAnimation("idle");
            } else {
                this.holder.getAnimator().pauseAnimation("twirl");
                AnimationHelper.updateWalkAnimation(this, this.holder);
            }
            AnimationHelper.updateHurtColor(this, this.holder);
        }
    }

    @Override
    @NotNull
    protected InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand interactionHand) {
        twirlTicks = 40;
        return InteractionResult.SUCCESS;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.TROPICAL_FISH_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SALMON_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.SALMON_HURT;
    }

    @Override
    @NotNull
    protected SoundEvent getFlopSound() {
        return SoundEvents.TROPICAL_FISH_FLOP;
    }

    @Override
    @NotNull
    public ItemStack getBucketItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.random.nextBoolean()) {
            this.spawnAtLocation(level, new ItemStack(Items.TROPICAL_FISH, 1));
        }
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new SmoothWaterBoundPathNavigation(this, level);
    }
}
