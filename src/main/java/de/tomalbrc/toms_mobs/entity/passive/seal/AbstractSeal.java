package de.tomalbrc.toms_mobs.entity.passive.seal;

import aqario.fowlplay.common.entity.ai.navigation.AmphibiousNavigation;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.entity.control.SemiAquaticMoveControl;
import de.tomalbrc.toms_mobs.entity.goal.aquatic.*;
import de.tomalbrc.toms_mobs.util.AnimationHelper;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractSeal extends Animal implements AnimatedEntity {
    private final EntityHolder<? extends AbstractSeal> holder;

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    public EntityHolder<? extends AbstractSeal> getHolder() {
        return this.holder;
    }

    protected AbstractSeal(EntityType<? extends Animal> type, Level level, Model model) {
        super(type, level);

        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.setPathfindingMalus(PathType.DOOR_IRON_CLOSED, -1.0F);
        this.setPathfindingMalus(PathType.DOOR_WOOD_CLOSED, -1.0F);
        this.setPathfindingMalus(PathType.DOOR_OPEN, -1.0F);

        this.moveControl = new SemiAquaticMoveControl(this);
        this.jumpControl = new JumpControl(this);

        this.holder = new LivingEntityHolder<>(this, model);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new AquaticPanicGoal(this, 0.9));
        this.goalSelector.addGoal(3, new AquaticBreedGoal(this, 0.6));
        this.goalSelector.addGoal(4, new AquaticFollowParentGoal(this, 0.6));
        this.goalSelector.addGoal(5, new PathfinderMobSwimGoal(this, 3));
        this.goalSelector.addGoal(6, new AnimalGoToWaterGoal(this, 0.6));
        this.goalSelector.addGoal(7, new AquaticWaterAvoidingRandomStrollGoal(this, 0.3));
        this.goalSelector.addGoal(8, new AquaticRandomLookAroundGoal(this));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 6.0F));
    }

    @Override
    public float getWalkTargetValue(@NotNull BlockPos blockPos, LevelReader levelReader) {
        if (levelReader.getFluidState(blockPos).is(FluidTags.WATER)) {
            return 1;
        } else {
            return levelReader.getPathfindingCostFromLightLevels(blockPos);
        }
    }

    @Override
    public void tick() {
        // Force breathing BEFORE tick processes drowning
        if (this.isInWater()) {
            this.setAirSupply(this.getMaxAirSupply());
        }

        // Faster in water, slower on land
        if (this.tickCount % 10 == 0) {
            double targetSpeed = this.isInWater() ? 0.45 : 0.12;
            var speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null && Math.abs(speedAttr.getBaseValue() - targetSpeed) > 0.001) {
                speedAttr.setBaseValue(targetSpeed);
            }
        }

        super.tick();

        if (this.tickCount % 2 == 0) {
            if (this.isInWater()) {
                this.holder.getAnimator().playAnimation("swim");
                this.holder.getAnimator().pauseAnimation("idle");
                this.holder.getAnimator().pauseAnimation("walk");
            } else {
                this.holder.getAnimator().pauseAnimation("swim");
                AnimationHelper.updateWalkAnimation(this, this.holder);
            }
            AnimationHelper.updateHurtColor(this, this.holder);
        }

        // Keep seals buoyant and moving in water
        if (this.isInWater()) {
            // Prevent sinking
            if (this.getDeltaMovement().y < -0.01) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.3, 1.0));
            }
            // Gentle upward float
            if (this.getDeltaMovement().y < 0.02) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.003, 0.0));
            }
            // Nudge them to keep moving when idle
            if (this.getNavigation().isDone() && this.tickCount % 40 == 0) {
                double driftX = (this.random.nextDouble() - 0.5) * 0.05;
                double driftZ = (this.random.nextDouble() - 0.5) * 0.05;
                this.setDeltaMovement(this.getDeltaMovement().add(driftX, 0.0, driftZ));
            }
        }
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(Items.COD) || itemStack.is(Items.SALMON);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mate) {
        return null;
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.spawnAtLocation(level, new ItemStack(Items.COD, 1 + this.random.nextInt(3)));
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.DOLPHIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.DOLPHIN_DEATH;
    }

    @Override
    @NotNull
    protected PathNavigation createNavigation(@NotNull Level level) {
        return new AmphibiousNavigation(this, level);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }
}
