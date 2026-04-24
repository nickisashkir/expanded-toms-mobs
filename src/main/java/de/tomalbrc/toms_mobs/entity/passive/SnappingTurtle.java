package de.tomalbrc.toms_mobs.entity.passive;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.entity.control.SemiAquaticMoveControl;
import de.tomalbrc.toms_mobs.entity.goal.aquatic.AquaticWaterAvoidingRandomStrollGoal;
import de.tomalbrc.toms_mobs.entity.goal.aquatic.PathfinderMobSwimGoal;
import de.tomalbrc.toms_mobs.util.AnimationHelper;
import de.tomalbrc.toms_mobs.util.Util;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.PathType;
import net.tslat.smartbrainlib.api.core.navigation.SmoothWaterBoundPathNavigation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SnappingTurtle extends Animal implements AnimatedEntity, de.tomalbrc.toms_mobs.util.HealthDisplayOverride {
    @Override public double getHealthDisplayYOffset() { return 0.9; }

    public static final Identifier ID = Util.id("snapping_turtle");
    public static final Model MODEL = Util.loadBbModel(ID);
    private final EntityHolder<SnappingTurtle> holder;
    private int attackAnimTicks = 0;

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 25.0)
                .add(Attributes.MOVEMENT_SPEED, 0.15)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override public EntityHolder<SnappingTurtle> getHolder() { return this.holder; }

    public SnappingTurtle(EntityType<? extends @NotNull Animal> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 0.F);
        this.moveControl = new SemiAquaticMoveControl(this);
        this.holder = new LivingEntityHolder<>(this, MODEL);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1, true));
        this.goalSelector.addGoal(2, new PathfinderMobSwimGoal(this, 1));
        this.goalSelector.addGoal(4, new BreedGoal(this, 0.6));
        this.goalSelector.addGoal(5, new AquaticWaterAvoidingRandomStrollGoal(this, 0.4));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));

        // Neutral: only retaliates, but also preys on fish while in water
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, AbstractFish.class, true) {
            @Override public boolean canUse() { return SnappingTurtle.this.isInWater() && super.canUse(); }
        });
    }

    @Override
    public boolean doHurtTarget(@NotNull ServerLevel level, @NotNull Entity target) {
        attackAnimTicks = 14;
        return super.doHurtTarget(level, target);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount % 2 == 0) {
            if (attackAnimTicks > 0) {
                attackAnimTicks -= 2;
                this.holder.getAnimator().playAnimation("attack");
                this.holder.getAnimator().pauseAnimation("walk");
                this.holder.getAnimator().pauseAnimation("idle");
            } else {
                this.holder.getAnimator().pauseAnimation("attack");
                AnimationHelper.updateWalkAnimation(this, this.holder);
            }
            AnimationHelper.updateHurtColor(this, this.holder);
        }
    }

    @Override
    public float getWalkTargetValue(@NotNull BlockPos pos, @NotNull LevelReader level) {
        return level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER) ? 1.0F : level.getPathfindingCostFromLightLevels(pos);
    }

    @Override public boolean isFood(ItemStack i) { return i.is(Items.COD) || i.is(Items.SALMON) || i.is(Items.TROPICAL_FISH); }
    @Nullable @Override public AgeableMob getBreedOffspring(@NotNull ServerLevel l, @NotNull AgeableMob m) { return null; }
    @Override protected SoundEvent getHurtSound(@NotNull DamageSource d) { return SoundEvents.TURTLE_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.TURTLE_DEATH; }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel l, @NotNull DamageSource s, boolean r) {
        super.dropCustomDeathLoot(l, s, r);
        this.spawnAtLocation(l, new ItemStack(Items.SEAGRASS, 1 + this.random.nextInt(2)));
        if (this.random.nextInt(3) == 0) this.spawnAtLocation(l, new ItemStack(Items.TURTLE_SCUTE, 1));
    }

    @Override public boolean isPushedByFluid() { return false; }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) { return new SmoothWaterBoundPathNavigation(this, level); }
}
