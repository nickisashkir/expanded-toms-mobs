package de.tomalbrc.toms_mobs.entity.passive.jellyfish;

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

import java.util.List;

public abstract class AbstractJellyfish extends AbstractFish implements AnimatedEntity {
    private final EntityHolder<? extends AbstractJellyfish> holder;
    private int stingCooldown = 0;

    private static final float STING_DAMAGE = 6.0f;
    private static final double STING_RANGE = 1.5;
    private static final int STING_INTERVAL = 20;

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return AbstractFish.createAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.MAX_HEALTH, 30.0);
    }

    public static boolean checkJellyfishSpawnRules(EntityType<? extends @NotNull LivingEntity> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        int seaLevel = levelAccessor.getSeaLevel();
        return blockPos.getY() >= seaLevel - 30
                && blockPos.getY() <= seaLevel - 3
                && levelAccessor.getFluidState(blockPos).is(FluidTags.WATER)
                && levelAccessor.getBlockState(blockPos.above()).is(Blocks.WATER);
    }

    @Override
    public EntityHolder<? extends AbstractJellyfish> getHolder() {
        return this.holder;
    }

    protected AbstractJellyfish(EntityType<? extends @NotNull AbstractFish> type, Level level, Model model) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 0.F);
        this.moveControl = new SemiAquaticMoveControl(this);
        this.holder = new LivingEntityHolder<>(this, model);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    public boolean canBeLeashed() { return false; }

    @Override
    public int getMaxSpawnClusterSize() { return 4; }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new AquaticPanicGoal(this, 1.2));
        this.goalSelector.addGoal(1, new PathfinderMobSwimGoal(this, 1));
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
            if (this.isInWater()) {
                this.holder.getAnimator().playAnimation("walk");
                this.holder.getAnimator().pauseAnimation("idle");

                // Random pulse (death animation) every 2 seconds, 50% chance
                if (this.tickCount % 40 == 0 && this.random.nextInt(2) == 0) {
                    this.holder.getAnimator().playAnimation("death");
                }
            } else {
                this.holder.getAnimator().playAnimation("idle");
                this.holder.getAnimator().pauseAnimation("walk");
            }
            AnimationHelper.updateHurtColor(this, this.holder);
        }

        // Jellyfish buoyancy and drift
        if (this.isInWater()) {
            // Gentle upward drift
            if (this.getDeltaMovement().y < 0.02) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.005, 0.0));
            }
            // Random horizontal drift
            if (this.tickCount % 60 == 0 && this.random.nextInt(3) == 0) {
                double driftX = (this.random.nextDouble() - 0.5) * 0.03;
                double driftZ = (this.random.nextDouble() - 0.5) * 0.03;
                this.setDeltaMovement(this.getDeltaMovement().add(driftX, 0.0, driftZ));
            }
            // Push down if near surface
            if (this.level().getFluidState(this.blockPosition().above()).isEmpty()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.01, 0.0));
            }
        }

        // Sting mechanic
        if (!this.level().isClientSide() && this.isInWater()) {
            if (stingCooldown > 0) {
                stingCooldown--;
            } else {
                List<Player> nearbyPlayers = this.level().getEntitiesOfClass(
                        Player.class,
                        this.getBoundingBox().inflate(STING_RANGE),
                        player -> player.isInWater() && !player.isCreative() && !player.isSpectator()
                );
                for (Player player : nearbyPlayers) {
                    player.hurt(this.damageSources().mobAttack(this), STING_DAMAGE);
                }
                if (!nearbyPlayers.isEmpty()) {
                    stingCooldown = STING_INTERVAL;
                }
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int count = this.random.nextInt(3);
        if (count > 0) {
            this.spawnAtLocation(level, new ItemStack(Items.TROPICAL_FISH, count));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.TROPICAL_FISH_AMBIENT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.SLIME_DEATH; }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) { return SoundEvents.SLIME_HURT; }

    @Override
    @NotNull
    protected SoundEvent getFlopSound() { return SoundEvents.TROPICAL_FISH_FLOP; }

    @Override
    @NotNull
    public ItemStack getBucketItemStack() { return ItemStack.EMPTY; }

    @Override
    public boolean isPushedByFluid() { return false; }

    @Override
    @NotNull
    protected InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand interactionHand) {
        return InteractionResult.PASS;
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new SmoothWaterBoundPathNavigation(this, level);
    }
}
