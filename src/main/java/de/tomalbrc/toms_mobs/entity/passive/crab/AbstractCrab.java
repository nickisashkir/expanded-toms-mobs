package de.tomalbrc.toms_mobs.entity.passive.crab;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.util.AnimationHelper;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.tslat.smartbrainlib.api.core.navigation.SmoothGroundNavigation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractCrab extends Animal implements AnimatedEntity {
    private final EntityHolder<? extends AbstractCrab> holder;
    private int danceTicks = 0;
    private int fleeToWaterTicks = 0;
    private BlockPos waterTarget = null;

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.ATTACK_DAMAGE, 7.0);
    }

    @Override
    public EntityHolder<? extends AbstractCrab> getHolder() {
        return this.holder;
    }

    protected AbstractCrab(EntityType<? extends Animal> type, Level level, Model model) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.holder = new LivingEntityHolder<>(this, model);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));

        // Fight back when attacked
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public float getWalkTargetValue(@NotNull BlockPos blockPos, LevelReader levelReader) {
        BlockState below = levelReader.getBlockState(blockPos.below());
        if (below.is(Blocks.SAND) || below.is(Blocks.RED_SAND)) {
            return 10.0F;
        }
        if (below.is(Blocks.GRAVEL) || below.is(Blocks.CLAY)) {
            return 5.0F;
        }
        return levelReader.getPathfindingCostFromLightLevels(blockPos);
    }

    @Override
    public void tick() {
        super.tick();

        // Flee to water after being hit
        if (fleeToWaterTicks > 0) {
            fleeToWaterTicks--;
            if (this.isInWater()) {
                fleeToWaterTicks = 0;
                waterTarget = null;
            } else {
                if (waterTarget == null || this.getNavigation().isDone()) {
                    waterTarget = findNearestWater();
                    if (waterTarget != null) {
                        this.getNavigation().moveTo(waterTarget.getX() + 0.5, waterTarget.getY(), waterTarget.getZ() + 0.5, 1.6);
                    }
                }
            }
        }

        if (this.tickCount % 2 == 0) {
            if (danceTicks > 0) {
                danceTicks -= 2;
                this.holder.getAnimator().playAnimation("dance");
                this.holder.getAnimator().pauseAnimation("walk");
                this.holder.getAnimator().pauseAnimation("idle");
            } else {
                this.holder.getAnimator().pauseAnimation("dance");
                AnimationHelper.updateWalkAnimation(this, this.holder);
            }
            AnimationHelper.updateHurtColor(this, this.holder);
        }
    }

    @Override
    @NotNull
    public InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        InteractionResult result = super.mobInteract(player, hand);
        if (result.consumesAction()) return result;

        // Dance when interacted with!
        if (this.onGround() && !this.isInWater()) {
            danceTicks = 60;
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    // Explosion immune
    @Override
    public boolean isInvulnerableTo(@NotNull ServerLevel level, @NotNull DamageSource source) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) return true;
        return super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean hurtServer(@NotNull ServerLevel level, @NotNull DamageSource source, float amount) {
        // Run toward water when hit, unless already in water
        if (!this.isInWater()) {
            fleeToWaterTicks = 200;
            waterTarget = null;
        }
        return super.hurtServer(level, source, amount);
    }

    private BlockPos findNearestWater() {
        BlockPos origin = this.blockPosition();
        int bestDist = Integer.MAX_VALUE;
        BlockPos best = null;
        for (int dx = -16; dx <= 16; dx++) {
            for (int dz = -16; dz <= 16; dz++) {
                for (int dy = -4; dy <= 2; dy++) {
                    BlockPos check = origin.offset(dx, dy, dz);
                    if (this.level().getFluidState(check).is(net.minecraft.tags.FluidTags.WATER)) {
                        int dist = dx * dx + dy * dy + dz * dz;
                        if (dist < bestDist) {
                            bestDist = dist;
                            best = check;
                        }
                    }
                }
            }
        }
        return best;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(Items.KELP) || itemStack.is(Items.SEAGRASS);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mate) {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.ARMADILLO_HURT_REDUCED;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ARMADILLO_DEATH;
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState state) {
        this.playSound(SoundEvents.ARMADILLO_STEP, 0.15F, 1.4F);
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int count = 1 + this.random.nextInt(2);
        this.spawnAtLocation(level, new ItemStack(Items.KELP, count));
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new SmoothGroundNavigation(this, level);
    }
}
