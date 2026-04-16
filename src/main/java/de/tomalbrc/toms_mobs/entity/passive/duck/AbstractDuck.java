package de.tomalbrc.toms_mobs.entity.passive.duck;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.entity.control.SemiAquaticMoveControl;
import de.tomalbrc.toms_mobs.entity.goal.aquatic.*;
import de.tomalbrc.toms_mobs.registry.MobRegistry;
import de.tomalbrc.toms_mobs.util.AnimationHelper;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import aqario.fowlplay.common.entity.ai.navigation.AmphibiousNavigation;
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

public abstract class AbstractDuck extends Animal implements AnimatedEntity {
    private final EntityHolder<? extends AbstractDuck> holder;

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    public EntityHolder<? extends AbstractDuck> getHolder() { return this.holder; }

    protected AbstractDuck(EntityType<? extends Animal> type, Level level, Model model) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.moveControl = new SemiAquaticMoveControl(this);
        this.holder = new LivingEntityHolder<>(this, model);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new AquaticPanicGoal(this, 1.2));
        this.goalSelector.addGoal(3, new AquaticBreedGoal(this, 0.7));
        this.goalSelector.addGoal(4, new AquaticFollowParentGoal(this, 0.7));
        this.goalSelector.addGoal(5, new AquaticWaterAvoidingRandomStrollGoal(this, 0.5));
        this.goalSelector.addGoal(5, new PathfinderMobSwimGoal(this, 2));
        this.goalSelector.addGoal(8, new AquaticRandomLookAroundGoal(this));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 6.0F));
    }

    @Override
    public float getWalkTargetValue(@NotNull BlockPos blockPos, LevelReader levelReader) {
        if (levelReader.getFluidState(blockPos).is(FluidTags.WATER)) return 1;
        return levelReader.getPathfindingCostFromLightLevels(blockPos);
    }

    @Override
    public void tick() {
        if (this.isInWater()) { this.setAirSupply(this.getMaxAirSupply()); }
        super.tick();
        if (this.tickCount % 2 == 0) {
            // Ducks use idle_water/walk_water when swimming
            if (this.isInWater()) {
                if (this.getDeltaMovement().length() > 0.05 || this.walkAnimation.speed() > 0.02) {
                    this.holder.getAnimator().playAnimation("walk_water");
                    this.holder.getAnimator().pauseAnimation("idle_water");
                } else {
                    this.holder.getAnimator().playAnimation("idle_water");
                    this.holder.getAnimator().pauseAnimation("walk_water");
                }
                this.holder.getAnimator().pauseAnimation("walk");
                this.holder.getAnimator().pauseAnimation("idle");
            } else {
                this.holder.getAnimator().pauseAnimation("walk_water");
                this.holder.getAnimator().pauseAnimation("idle_water");
                AnimationHelper.updateWalkAnimation(this, this.holder);
            }
            AnimationHelper.updateHurtColor(this, this.holder);
        }

        // Ducks float on water surface - force them up to the top
        if (this.isInWater()) {
            // Look upward to find water surface
            int searchLimit = 5;
            int currentY = this.blockPosition().getY();
            int surfaceY = currentY;
            for (int i = 0; i < searchLimit; i++) {
                if (this.level().getFluidState(this.blockPosition().above(i)).isEmpty()) {
                    surfaceY = currentY + i;
                    break;
                }
                surfaceY = currentY + i;
            }
            double targetY = surfaceY - 0.15; // belly in water, body above
            double deltaY = targetY - this.getY();
            if (deltaY > 0.1) {
                // Below surface - push up hard
                this.setDeltaMovement(this.getDeltaMovement().x, Math.min(0.3, deltaY * 0.5), this.getDeltaMovement().z);
            } else if (this.getDeltaMovement().y < 0) {
                // At/above surface - cancel downward movement
                this.setDeltaMovement(this.getDeltaMovement().x, 0.0, this.getDeltaMovement().z);
            }
        }
    }

    @Override
    public boolean isFood(ItemStack itemStack) { return itemStack.is(Items.WHEAT_SEEDS) || itemStack.is(Items.MELON_SEEDS); }

    @Nullable @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mate) { return new Duckling(MobRegistry.DUCKLING, level); }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.spawnAtLocation(level, new ItemStack(Items.FEATHER, 1 + this.random.nextInt(2)));
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource d) { return SoundEvents.PARROT_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.PARROT_DEATH; }
    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) { return new AmphibiousNavigation(this, level); }
    @Override
    public boolean isPushedByFluid() { return false; }
    @Override
    public boolean canBreatheUnderwater() { return true; }
}
