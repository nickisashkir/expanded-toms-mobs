package de.tomalbrc.toms_mobs.entity.passive.winterbird;

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
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractWinterBird extends Animal implements AnimatedEntity {
    private final EntityHolder<? extends AbstractWinterBird> holder;
    private int callTicks = 0;

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.FLYING_SPEED, 0.3)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    public EntityHolder<? extends AbstractWinterBird> getHolder() { return this.holder; }

    protected AbstractWinterBird(EntityType<? extends Animal> type, Level level, Model model) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.holder = new LivingEntityHolder<>(this, model);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(2, new de.tomalbrc.toms_mobs.entity.goal.flying.BirdPerchGoal(this, 1.0, 30, 1600, 4800));
        this.goalSelector.addGoal(3, new de.tomalbrc.toms_mobs.entity.goal.flying.BirdWanderGoal(this, 1.0));
        this.goalSelector.addGoal(4, new FloatGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
    }

    @Override
    public void tick() {
        super.tick();

        // Sing at dawn (~ticks 22500-24000 / 0-500). Short call burst, rare chance.
        if (!this.level().isClientSide() && this.tickCount % 40 == 0 && callTicks <= 0) {
            long t = this.level().getOverworldClockTime() % 24000;
            boolean isDawn = t >= 22500 || t <= 500;
            if (isDawn && this.random.nextInt(30) == 0) {
                callTicks = 30;
                this.playSound(SoundEvents.PARROT_AMBIENT, 0.4F, 1.3F + this.random.nextFloat() * 0.4F);
            }
        }

        if (this.tickCount % 2 == 0) {
            if (callTicks > 0) {
                callTicks -= 2;
                this.holder.getAnimator().playAnimation("call");
                this.holder.getAnimator().pauseAnimation("walk");
                this.holder.getAnimator().pauseAnimation("idle");
            } else {
                this.holder.getAnimator().pauseAnimation("call");
                boolean isFlying = !this.onGround() && this.getDeltaMovement().horizontalDistanceSqr() > 0.001;
                if (!isFlying && !this.onGround()) {
                    net.minecraft.core.BlockPos pos = this.blockPosition();
                    if (this.level().getBlockState(pos).isAir() && this.level().getBlockState(pos.below()).isAir()) {
                        isFlying = true;
                    }
                }
                if (isFlying) {
                    this.holder.getAnimator().playAnimation("walk");
                    this.holder.getAnimator().pauseAnimation("idle");
                } else {
                    AnimationHelper.updateWalkAnimation(this, this.holder);
                }
            }
            AnimationHelper.updateHurtColor(this, this.holder);
        }
    }

    @Override
    public void travel(@NotNull Vec3 movementInput) {
        if (this.isEffectiveAi()) {
            if (this.isInWater()) {
                this.moveRelative(0.02F, movementInput);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
            } else if (this.isInLava()) {
                this.moveRelative(0.02F, movementInput);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
            } else {
                this.moveRelative(this.getSpeed(), movementInput);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.91));
            }
        }
        this.calculateEntityAnimation(false);
    }

    @Override
    public float getWalkTargetValue(@NotNull BlockPos pos, @NotNull net.minecraft.world.level.LevelReader level) {
        return level.getBlockState(pos).isAir() ? 10.0F : 0.0F;
    }

    @Override
    public boolean isFlapping() {
        return !this.onGround();
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, @NotNull DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, @NotNull net.minecraft.world.level.block.state.BlockState state, @NotNull BlockPos pos) {
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(false);
        return nav;
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player p, @NotNull InteractionHand h) {
        InteractionResult r = super.mobInteract(p, h);
        if (r.consumesAction()) return r;
        callTicks = 20;
        this.playSound(SoundEvents.PARROT_AMBIENT, 0.5F, 1.5F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFood(ItemStack i) { return i.is(Items.WHEAT_SEEDS); }

    @Nullable @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel l, @NotNull AgeableMob m) { return null; }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel l, @NotNull DamageSource s, boolean r) {
        super.dropCustomDeathLoot(l, s, r);
        this.spawnAtLocation(l, new ItemStack(Items.FEATHER, 1));
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource d) { return SoundEvents.PARROT_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.PARROT_DEATH; }
}
