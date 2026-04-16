package de.tomalbrc.toms_mobs.entity.passive.cat;

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
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
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

public abstract class AbstractCat extends TamableAnimal implements AnimatedEntity {
    private final EntityHolder<? extends AbstractCat> holder;
    private int loafTicks = 0;
    private boolean wasSitting = false;
    private boolean independent = false;
    private int homeX = 0, homeY = 0, homeZ = 0;

    public boolean isIndependent() { return independent; }

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 15.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    public EntityHolder<? extends AbstractCat> getHolder() { return this.holder; }

    protected AbstractCat(EntityType<? extends TamableAnimal> type, Level level, Model model) {
        super(type, level);
        this.holder = new LivingEntityHolder<>(this, model);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CatSitGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F) {
            @Override public boolean canUse() { return !AbstractCat.this.isIndependent() && super.canUse(); }
        });
        this.goalSelector.addGoal(4, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(5, new CatChaseChickenGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
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
                    this.holder.getAnimator().pauseAnimation("loaf");
                    this.holder.getAnimator().playAnimation("sit");
                    wasSitting = true;
                }
            } else if (loafTicks > 0) {
                loafTicks -= 2;
                this.holder.getAnimator().playAnimation("loaf");
                this.holder.getAnimator().pauseAnimation("walk");
                this.holder.getAnimator().pauseAnimation("idle");
                this.holder.getAnimator().pauseAnimation("sit");
            } else {
                wasSitting = false;
                this.holder.getAnimator().pauseAnimation("loaf");
                this.holder.getAnimator().pauseAnimation("sit");
                AnimationHelper.updateWalkAnimation(this, this.holder);
            }
            AnimationHelper.updateHurtColor(this, this.holder);
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);

        if (!this.isTame()) {
            if (itemInHand.is(Items.COD) || itemInHand.is(Items.SALMON)) {
                if (!player.getAbilities().instabuild) itemInHand.shrink(1);
                if (this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.setOrderedToSit(false);
                    this.setInSittingPose(false);
                    this.navigation.stop();
                    this.playSound(SoundEvents.OCELOT_AMBIENT, 0.8F, 1.0F);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HEART,
                                this.getX(), this.getY() + this.getBbHeight() * 0.75, this.getZ(),
                                7, 0.4, 0.4, 0.4, 0.0);
                    }
                } else {
                    loafTicks = 15;
                    this.playSound(SoundEvents.OCELOT_HURT, 0.8F, 1.0F);
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

        // Owner shift+right-click with empty hand toggles independent mode
        if (this.getOwner() instanceof Player owner && owner.equals(player) && player.isSecondaryUseActive() && itemInHand.isEmpty()) {
            independent = !independent;
            if (independent) {
                homeX = this.getBlockX();
                homeY = this.getBlockY();
                homeZ = this.getBlockZ();
                this.setOrderedToSit(false);
                this.setInSittingPose(false);
                if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("Cat is now independent and will wander here."), true);
                }
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            this.getX(), this.getY() + this.getBbHeight() * 0.75, this.getZ(),
                            6, 0.3, 0.3, 0.3, 0.0);
                }
            } else {
                if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("Cat will follow you again."), true);
                }
            }
            this.playSound(SoundEvents.OCELOT_AMBIENT, 0.6F, 1.3F);
            return InteractionResult.SUCCESS;
        }

        // Owner toggles sit
        if (this.getOwner() instanceof Player owner && owner.equals(player) && !player.isSecondaryUseActive()) {
            boolean sit = !this.isOrderedToSit();
            this.setOrderedToSit(sit);
            this.setInSittingPose(sit);
            this.navigation.stop();
            loafTicks = 15;
            this.playSound(SoundEvents.OCELOT_AMBIENT, 0.8F, 1.2F);
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
    }

    @Override
    protected void readAdditionalSaveData(@NotNull net.minecraft.world.level.storage.ValueInput input) {
        super.readAdditionalSaveData(input);
        independent = input.getBooleanOr("Independent", false);
        homeX = input.getIntOr("HomeX", 0);
        homeY = input.getIntOr("HomeY", 0);
        homeZ = input.getIntOr("HomeZ", 0);
    }

    @Override
    public boolean isFood(@NotNull ItemStack itemStack) { return itemStack.is(Items.COD) || itemStack.is(Items.SALMON); }

    @Nullable @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mate) { return null; }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.spawnAtLocation(level, new ItemStack(Items.STRING, 1 + this.random.nextInt(2)));
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource d) { return SoundEvents.OCELOT_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.OCELOT_DEATH; }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) { return new SmoothGroundNavigation(this, level); }

    private static class CatSitGoal extends Goal {
        private final TamableAnimal cat;

        CatSitGoal(TamableAnimal cat) {
            this.cat = cat;
            this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
        }

        @Override
        public boolean canUse() { return this.cat.isTame() && this.cat.isOrderedToSit(); }

        @Override
        public boolean canContinueToUse() { return this.cat.isOrderedToSit(); }

        @Override
        public void start() { this.cat.getNavigation().stop(); this.cat.setInSittingPose(true); }

        @Override
        public void stop() { this.cat.setInSittingPose(false); }
    }
}
