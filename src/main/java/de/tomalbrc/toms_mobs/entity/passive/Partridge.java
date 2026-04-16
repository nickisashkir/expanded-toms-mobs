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
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.core.navigation.SmoothGroundNavigation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Partridge extends Animal implements AnimatedEntity {
    public static final Identifier ID = Util.id("partridge");
    public static final Model MODEL = Util.loadBbModel(ID);
    private final EntityHolder<Partridge> holder;
    private int burstFlyTicks = 0;
    private int burstCooldown = 0;

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    public EntityHolder<Partridge> getHolder() { return this.holder; }

    public Partridge(EntityType<? extends @NotNull Animal> type, Level level) {
        super(type, level);
        this.holder = new LivingEntityHolder<>(this, MODEL);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(3, new BreedGoal(this, 0.8));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 0.8));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
    }

    @Override
    public void tick() {
        super.tick();

        if (burstCooldown > 0) burstCooldown--;
        if (burstFlyTicks > 0) {
            burstFlyTicks--;
            // Apply upward velocity for the burst
            if (burstFlyTicks > 10) {
                this.setDeltaMovement(this.getDeltaMovement().x * 1.4, 0.35, this.getDeltaMovement().z * 1.4);
            }
            this.fallDistance = 0;
        }

        // Check for nearby players to trigger burst-fly
        if (!this.level().isClientSide() && burstFlyTicks <= 0 && burstCooldown <= 0 && this.tickCount % 10 == 0) {
            Player nearest = this.level().getNearestPlayer(this, 4.0);
            if (nearest != null && !nearest.isCreative() && !nearest.isSpectator() && this.onGround()) {
                burstFlyTicks = 30;
                burstCooldown = 200;
                // Initial burst velocity away from player
                double dx = this.getX() - nearest.getX();
                double dz = this.getZ() - nearest.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 0.001) {
                    this.setDeltaMovement(dx / dist * 0.5, 0.5, dz / dist * 0.5);
                    }
                this.playSound(SoundEvents.PARROT_FLY, 1.0F, 1.0F);
            }
        }

        if (this.tickCount % 2 == 0) {
            if (burstFlyTicks > 0) {
                this.holder.getAnimator().playAnimation("fly");
                this.holder.getAnimator().pauseAnimation("walk");
                this.holder.getAnimator().pauseAnimation("idle");
            } else {
                this.holder.getAnimator().pauseAnimation("fly");
                AnimationHelper.updateWalkAnimation(this, this.holder);
            }
            AnimationHelper.updateHurtColor(this, this.holder);
        }
    }

    @Override
    public boolean isFood(ItemStack itemStack) { return itemStack.is(Items.WHEAT_SEEDS) || itemStack.is(Items.BEETROOT_SEEDS); }

    @Nullable @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mate) { return null; }

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
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) { return new SmoothGroundNavigation(this, level); }
}
