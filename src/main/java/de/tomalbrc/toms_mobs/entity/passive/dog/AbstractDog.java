package de.tomalbrc.toms_mobs.entity.passive.dog;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.util.AnimationHelper;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import de.tomalbrc.toms_mobs.entity.passive.squirrel.AbstractSquirrel;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.core.navigation.SmoothGroundNavigation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractDog extends TamableAnimal implements AnimatedEntity {
    private final EntityHolder<? extends AbstractDog> holder;
    private int barkTicks = 0;
    private boolean wasSitting = false;
    private boolean independent = false;
    private int homeX = 0, homeY = 0, homeZ = 0;
    private final Set<UUID> coOwners = new HashSet<>();

    public boolean isIndependent() { return independent; }
    public net.minecraft.core.BlockPos getHome() { return new net.minecraft.core.BlockPos(homeX, homeY, homeZ); }

    public Set<UUID> getCoOwners() { return coOwners; }
    public void addCoOwner(UUID id) { coOwners.add(id); }
    public void removeCoOwner(UUID id) { coOwners.remove(id); }

    public boolean isPrimaryOwner(Player player) {
        LivingEntity owner = this.getOwner();
        return owner != null && owner.getUUID().equals(player.getUUID());
    }

    @Override
    public boolean isOwnedBy(@NotNull LivingEntity entity) {
        if (super.isOwnedBy(entity)) return true;
        if (entity instanceof Player p) return coOwners.contains(p.getUUID());
        return false;
    }

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    public EntityHolder<? extends AbstractDog> getHolder() { return this.holder; }

    protected AbstractDog(EntityType<? extends TamableAnimal> type, Level level, Model model) {
        super(type, level);
        this.holder = new LivingEntityHolder<>(this, model);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DogSitGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(3, new DogFetchGoal(this));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F) {
            @Override public boolean canUse() { return !AbstractDog.this.isIndependent() && super.canUse(); }
        });
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        // Wild dogs chase squirrels
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractSquirrel.class, true) {
            @Override public boolean canUse() { return !AbstractDog.this.isTame() && super.canUse(); }
        });
    }

    @Override
    public void tick() {
        super.tick();

        // Independent mode: pull back home if too far
        if (!this.level().isClientSide() && independent && !this.isOrderedToSit() && this.tickCount % 40 == 0) {
            double dx = this.getX() - homeX;
            double dz = this.getZ() - homeZ;
            double distSqr = dx * dx + dz * dz;
            if (distSqr > 256 && this.getNavigation().isDone()) { // > 16 blocks
                this.getNavigation().moveTo(homeX + 0.5, homeY, homeZ + 0.5, 1.0);
            }
        }

        if (this.tickCount % 2 == 0) {
            if (this.isOrderedToSit()) {
                if (!wasSitting) {
                    this.holder.getAnimator().pauseAnimation("walk");
                    this.holder.getAnimator().pauseAnimation("idle");
                    this.holder.getAnimator().pauseAnimation("bark");
                    this.holder.getAnimator().playAnimation("idle_sit");
                    wasSitting = true;
                }
            } else if (barkTicks > 0) {
                barkTicks -= 2;
                this.holder.getAnimator().pauseAnimation("idle_sit");
                this.holder.getAnimator().playAnimation("bark");
                this.holder.getAnimator().pauseAnimation("walk");
                this.holder.getAnimator().pauseAnimation("idle");
            } else {
                wasSitting = false;
                this.holder.getAnimator().pauseAnimation("bark");
                this.holder.getAnimator().pauseAnimation("idle_sit");
                AnimationHelper.updateWalkAnimation(this, this.holder);
            }
            AnimationHelper.updateHurtColor(this, this.holder);
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);

        if (!this.isTame()) {
            if (itemInHand.is(Items.BONE)) {
                if (!player.getAbilities().instabuild) itemInHand.shrink(1);
                if (this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.setOrderedToSit(false);
                    this.setInSittingPose(false);
                    this.navigation.stop();
                    this.playSound(SoundEvents.FOX_AMBIENT, 0.8F, 1.4F);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HEART,
                                this.getX(), this.getY() + this.getBbHeight() * 0.75, this.getZ(),
                                7, 0.4, 0.4, 0.4, 0.0);
                    }
                } else {
                    barkTicks = 15;
                    this.playSound(SoundEvents.FOX_HURT, 0.8F, 1.2F);
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        // Feed to heal
        if (this.isFood(itemInHand) && this.getHealth() < this.getMaxHealth()) {
            if (!player.getAbilities().instabuild) itemInHand.shrink(1);
            this.heal(4.0F);
            return InteractionResult.SUCCESS;
        }

        // Any owner (primary or co-owner) shift+right-click with empty hand toggles independent mode
        if (this.isOwnedBy(player) && player.isSecondaryUseActive() && itemInHand.isEmpty()) {
            independent = !independent;
            if (independent) {
                homeX = this.getBlockX();
                homeY = this.getBlockY();
                homeZ = this.getBlockZ();
                this.setOrderedToSit(false);
                this.setInSittingPose(false);
                if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("Dog is now independent and will wander here."), true);
                }
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            this.getX(), this.getY() + this.getBbHeight() * 0.75, this.getZ(),
                            6, 0.3, 0.3, 0.3, 0.0);
                }
            } else {
                if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("Dog will follow you again."), true);
                }
            }
            this.playSound(SoundEvents.FOX_AMBIENT, 0.6F, 1.3F);
            return InteractionResult.SUCCESS;
        }

        // Any owner toggles sit. Primary owner or co-owners both allowed.
        if (this.isOwnedBy(player) && !player.isSecondaryUseActive()) {
            boolean sit = !this.isOrderedToSit();
            this.setOrderedToSit(sit);
            this.setInSittingPose(sit);
            this.navigation.stop();
            barkTicks = 15;
            this.playSound(SoundEvents.FOX_HURT, 0.8F, 1.2F);
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull net.minecraft.world.level.storage.ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Independent", independent);
        output.putInt("HomeX", homeX);
        output.putInt("HomeY", homeY);
        output.putInt("HomeZ", homeZ);
        output.putString("CoOwners", coOwners.stream().map(UUID::toString).collect(Collectors.joining(",")));
    }

    @Override
    protected void readAdditionalSaveData(@NotNull net.minecraft.world.level.storage.ValueInput input) {
        super.readAdditionalSaveData(input);
        coOwners.clear();
        String coOwnersStr = input.getStringOr("CoOwners", "");
        if (!coOwnersStr.isEmpty()) {
            for (String s : coOwnersStr.split(",")) {
                try { coOwners.add(UUID.fromString(s)); } catch (IllegalArgumentException ignored) {}
            }
        }
        independent = input.getBooleanOr("Independent", false);
        homeX = input.getIntOr("HomeX", 0);
        homeY = input.getIntOr("HomeY", 0);
        homeZ = input.getIntOr("HomeZ", 0);
    }

    @Override
    public boolean isFood(@NotNull ItemStack itemStack) { return itemStack.is(Items.BONE) || itemStack.is(Items.BEEF); }

    @Nullable @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mate) { return null; }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.spawnAtLocation(level, new ItemStack(Items.BONE, 1 + this.random.nextInt(2)));
    }

    @Override
    public boolean hurtServer(@NotNull ServerLevel level, @NotNull DamageSource source, float amount) {
        barkTicks = 20;
        return super.hurtServer(level, source, amount);
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource d) { return SoundEvents.FOX_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.FOX_DEATH; }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) { return new SmoothGroundNavigation(this, level); }

    private static class DogSitGoal extends Goal {
        private final TamableAnimal dog;

        DogSitGoal(TamableAnimal dog) {
            this.dog = dog;
            this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.dog.isTame() && this.dog.isOrderedToSit();
        }

        @Override
        public boolean canContinueToUse() {
            return this.dog.isOrderedToSit();
        }

        @Override
        public void start() {
            this.dog.getNavigation().stop();
            this.dog.setInSittingPose(true);
        }

        @Override
        public void stop() {
            this.dog.setInSittingPose(false);
        }
    }
}
