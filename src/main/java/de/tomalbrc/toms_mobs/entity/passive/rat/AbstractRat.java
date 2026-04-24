package de.tomalbrc.toms_mobs.entity.passive.rat;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.util.AnimationHelper;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
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
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.core.navigation.SmoothGroundNavigation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractRat extends Animal implements AnimatedEntity {
    private final EntityHolder<? extends AbstractRat> holder;
    private int biteAnimTicks = 0;

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override public EntityHolder<? extends AbstractRat> getHolder() { return this.holder; }

    protected AbstractRat(EntityType<? extends Animal> type, Level level, Model model) {
        super(type, level);
        this.holder = new LivingEntityHolder<>(this, model);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(4, new BreedGoal(this, 0.8));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 5.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public boolean doHurtTarget(@NotNull ServerLevel level, @NotNull Entity target) {
        biteAnimTicks = 12;
        return super.doHurtTarget(level, target);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount % 2 == 0) {
            if (biteAnimTicks > 0) {
                biteAnimTicks -= 2;
                this.holder.getAnimator().playAnimation("bite");
                this.holder.getAnimator().pauseAnimation("walk");
                this.holder.getAnimator().pauseAnimation("idle");
            } else {
                this.holder.getAnimator().pauseAnimation("bite");
                AnimationHelper.updateWalkAnimation(this, this.holder);
            }
            AnimationHelper.updateHurtColor(this, this.holder);
        }
    }

    @Override public boolean isFood(ItemStack i) { return i.is(Items.BREAD) || i.is(Items.WHEAT) || i.is(Items.CHICKEN) || i.is(Items.BEEF); }
    @Nullable @Override public AgeableMob getBreedOffspring(@NotNull ServerLevel l, @NotNull AgeableMob m) { return null; }
    @Override protected SoundEvent getHurtSound(@NotNull DamageSource d) { return SoundEvents.RABBIT_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.RABBIT_DEATH; }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel l, @NotNull DamageSource s, boolean r) {
        super.dropCustomDeathLoot(l, s, r);
        if (this.random.nextBoolean()) this.spawnAtLocation(l, new ItemStack(Items.STRING, 1));
    }

    @Override protected @NotNull PathNavigation createNavigation(@NotNull Level l) { return new SmoothGroundNavigation(this, l); }
}
