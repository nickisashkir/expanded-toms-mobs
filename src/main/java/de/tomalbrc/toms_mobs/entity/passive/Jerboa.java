package de.tomalbrc.toms_mobs.entity.passive;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.util.AnimationHelper;
import de.tomalbrc.toms_mobs.util.Util;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import de.tomalbrc.toms_mobs.entity.passive.cat.AbstractCat;
import de.tomalbrc.toms_mobs.entity.passive.dog.AbstractDog;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.core.navigation.SmoothGroundNavigation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Jerboa extends Animal implements AnimatedEntity {
    public static final Identifier ID = Util.id("jerboa");
    public static final Model MODEL = Util.loadBbModel(ID);
    private final EntityHolder<Jerboa> holder;
    private int boogieTicks = 0;

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35);
    }

    @Override public EntityHolder<Jerboa> getHolder() { return this.holder; }

    public Jerboa(EntityType<? extends @NotNull Animal> type, Level level) {
        super(type, level);
        this.holder = new LivingEntityHolder<>(this, MODEL);
        this.getAttribute(Attributes.JUMP_STRENGTH);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.8));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, AbstractDog.class, 10.0F, 1.3, 1.7));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, AbstractCat.class, 10.0F, 1.3, 1.7));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, FennecFox.class, 10.0F, 1.3, 1.7));
        this.goalSelector.addGoal(4, new BreedGoal(this, 0.8));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
    }

    @Override
    public void tick() {
        super.tick();

        // Rare "jerboogie" trigger when idle
        if (!this.level().isClientSide() && boogieTicks <= 0 && this.tickCount % 40 == 0
                && this.getNavigation().isDone() && this.random.nextInt(80) == 0) {
            boogieTicks = 80;
        }

        if (this.tickCount % 2 == 0) {
            if (boogieTicks > 0) {
                boogieTicks -= 2;
                this.holder.getAnimator().playAnimation("jerboogie");
                this.holder.getAnimator().pauseAnimation("walk");
                this.holder.getAnimator().pauseAnimation("idle");
            } else {
                this.holder.getAnimator().pauseAnimation("jerboogie");
                AnimationHelper.updateWalkAnimation(this, this.holder);
            }
            AnimationHelper.updateHurtColor(this, this.holder);
        }
    }

    @Override public boolean isFood(ItemStack i) { return i.is(Items.WHEAT_SEEDS) || i.is(Items.MELON_SEEDS) || i.is(Items.BEETROOT_SEEDS); }
    @Nullable @Override public AgeableMob getBreedOffspring(@NotNull ServerLevel l, @NotNull AgeableMob m) { return null; }
    @Override protected SoundEvent getHurtSound(@NotNull DamageSource d) { return SoundEvents.RABBIT_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.RABBIT_DEATH; }
    @Override protected @NotNull PathNavigation createNavigation(@NotNull Level l) { return new SmoothGroundNavigation(this, l); }
}
