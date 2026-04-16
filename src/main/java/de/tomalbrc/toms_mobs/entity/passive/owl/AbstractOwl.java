package de.tomalbrc.toms_mobs.entity.passive.owl;

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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractOwl extends Animal implements AnimatedEntity {
    private final EntityHolder<? extends AbstractOwl> holder;
    private int attackAnimTicks = 0;

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 32.0)
                .add(Attributes.FLYING_SPEED, 0.2625)
                .add(Attributes.MOVEMENT_SPEED, 0.1875)
                .add(Attributes.ATTACK_DAMAGE, 14.0);
    }

    @Override
    public EntityHolder<? extends AbstractOwl> getHolder() { return this.holder; }

    protected AbstractOwl(EntityType<? extends Animal> type, Level level, Model model) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.holder = new LivingEntityHolder<>(this, model);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.4, true));
        this.goalSelector.addGoal(2, new de.tomalbrc.toms_mobs.entity.goal.flying.BirdPerchGoal(this, 1.0, 40, 16000, 32000));
        this.goalSelector.addGoal(3, new de.tomalbrc.toms_mobs.entity.goal.flying.BirdWanderGoal(this, 1.0));
        this.goalSelector.addGoal(4, new FloatGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));

        // Hunt small mobs
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Chicken.class, true));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Rabbit.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        // Day/night activity. Daytime owls are sleepy and slow, nighttime owls are fast and active.
        if (this.tickCount % 20 == 0) {
            long time = this.level().getOverworldClockTime() % 24000;
            boolean isNight = time >= 13000 && time <= 23000;
            var speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
            var flyAttr = this.getAttribute(Attributes.FLYING_SPEED);
            if (isNight) {
                if (speedAttr != null) speedAttr.setBaseValue(0.25);
                if (flyAttr != null) flyAttr.setBaseValue(0.35);
            } else {
                if (speedAttr != null) speedAttr.setBaseValue(0.10);
                if (flyAttr != null) flyAttr.setBaseValue(0.15);
            }
        }

        if (this.tickCount % 2 == 0) {
            if (attackAnimTicks > 0) {
                attackAnimTicks -= 2;
                this.holder.getAnimator().playAnimation("attack");
                this.holder.getAnimator().pauseAnimation("walk");
                this.holder.getAnimator().pauseAnimation("idle");
            } else {
                this.holder.getAnimator().pauseAnimation("attack");
                boolean isFlying = !this.onGround() && this.getDeltaMovement().horizontalDistanceSqr() > 0.001;
                if (!isFlying && !this.onGround()) {
                    // setPos() (used by BirdPerchGoal) doesn't update onGround() — check for a
                    // block at/below feet to distinguish "perched" from "truly floating mid-air"
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
    public float getWalkTargetValue(@NotNull BlockPos pos, @NotNull LevelReader level) {
        return level.getBlockState(pos).isAir() ? 10.0F : 0.0F;
    }

    @Override
    public boolean isFlapping() { return !this.onGround(); }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, @NotNull DamageSource source) { return false; }

    @Override
    protected void checkFallDamage(double y, boolean onGround, @NotNull net.minecraft.world.level.block.state.BlockState state, @NotNull BlockPos pos) {}

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(false);
        return nav;
    }

    @Override
    public boolean doHurtTarget(@NotNull ServerLevel level, @NotNull net.minecraft.world.entity.Entity target) {
        attackAnimTicks = 16;
        return super.doHurtTarget(level, target);
    }

    @Override
    public boolean isFood(ItemStack itemStack) { return itemStack.is(Items.RABBIT) || itemStack.is(Items.CHICKEN); }

    @Nullable @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mate) { return null; }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) { return SoundEvents.PARROT_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.PARROT_DEATH; }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int count = 1 + this.random.nextInt(3);
        this.spawnAtLocation(level, new ItemStack(Items.FEATHER, count));
    }
}
