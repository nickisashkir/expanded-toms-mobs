package de.tomalbrc.toms_mobs.entity.passive;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.registry.MobRegistry;
import de.tomalbrc.toms_mobs.util.AnimationHelper;
import de.tomalbrc.toms_mobs.util.Util;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
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
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.tslat.smartbrainlib.api.core.navigation.SmoothGroundNavigation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Raccoon extends TamableAnimal implements AnimatedEntity {
    public static final Identifier ID = Util.id("raccoon");
    public static final Model MODEL = Util.loadBbModel(ID);
    private final EntityHolder<Raccoon> holder;
    private int begTicks = 0;
    private boolean wasSitting = false;
    private boolean independent = false;
    private int homeX = 0, homeY = 0, homeZ = 0;
    private final Set<UUID> coOwners = new HashSet<>();
    private UUID lastCommandedBy = null;

    public boolean isIndependent() { return independent; }
    public Set<UUID> getCoOwners() { return coOwners; }
    public void addCoOwner(UUID id) { coOwners.add(id); }
    public void removeCoOwner(UUID id) { coOwners.remove(id); }

    public boolean isPrimaryOwner(Player player) {
        LivingEntity primaryOwner = super.getOwner();
        return primaryOwner != null && primaryOwner.getUUID().equals(player.getUUID());
    }

    @Override
    public boolean isOwnedBy(@NotNull LivingEntity entity) {
        if (super.isOwnedBy(entity)) return true;
        if (entity instanceof Player p) return coOwners.contains(p.getUUID());
        return false;
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        if (lastCommandedBy != null && this.level() instanceof ServerLevel sl) {
            Player p = sl.getServer().getPlayerList().getPlayer(lastCommandedBy);
            if (p != null && p.isAlive() && this.distanceToSqr(p) < 4096) return p;
        }
        return super.getOwner();
    }

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28);
    }

    @Override
    public EntityHolder<Raccoon> getHolder() { return this.holder; }

    public Raccoon(EntityType<? extends @NotNull TamableAnimal> type, Level level) {
        super(type, level);
        this.holder = new LivingEntityHolder<>(this, MODEL);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PassivePetSitGoal(this));
        this.goalSelector.addGoal(2, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(3, new BreedGoal(this, 0.8));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F) {
            @Override public boolean canUse() { return !Raccoon.this.isIndependent() && super.canUse(); }
        });
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide() && independent && !this.isOrderedToSit() && this.tickCount % 40 == 0) {
            double dx = this.getX() - homeX;
            double dz = this.getZ() - homeZ;
            if (dx * dx + dz * dz > 256 && this.getNavigation().isDone()) {
                this.getNavigation().moveTo(homeX + 0.5, homeY, homeZ + 0.5, 1.0);
            }
        }

        // Trigger beg pose when a fight is happening nearby (raccoons don't have a scared anim)
        if (!this.level().isClientSide() && this.tickCount % 20 == 0 && begTicks <= 0 && !this.isOrderedToSit()) {
            if (detectNearbyCombat()) begTicks = 60;
        }

        if (this.tickCount % 2 == 0) {
            if (this.isOrderedToSit()) {
                if (!wasSitting) {
                    this.holder.getAnimator().pauseAnimation("walk");
                    this.holder.getAnimator().pauseAnimation("idle");
                    this.holder.getAnimator().pauseAnimation("beg");
                    this.holder.getAnimator().playAnimation("idle");
                    wasSitting = true;
                }
            } else if (begTicks > 0) {
                begTicks -= 2;
                this.holder.getAnimator().playAnimation("beg");
                this.holder.getAnimator().pauseAnimation("walk");
                this.holder.getAnimator().pauseAnimation("idle");
            } else {
                wasSitting = false;
                this.holder.getAnimator().pauseAnimation("beg");
                AnimationHelper.updateWalkAnimation(this, this.holder);
            }
            AnimationHelper.updateHurtColor(this, this.holder);
        }
    }

    private boolean detectNearbyCombat() {
        AABB area = this.getBoundingBox().inflate(8.0);
        for (Player p : this.level().getEntitiesOfClass(Player.class, area)) {
            if (p.getLastHurtMob() != null && this.tickCount - p.getLastHurtMobTimestamp() < 60) return true;
        }
        return false;
    }

    @Override
    @NotNull
    public InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);

        if (!this.isTame()) {
            if (isTameFood(itemInHand)) {
                if (!player.getAbilities().instabuild) itemInHand.shrink(1);
                if (this.random.nextInt(4) == 0) {
                    this.tame(player);
                    this.setOrderedToSit(false);
                    this.setInSittingPose(false);
                    this.navigation.stop();
                    this.playSound(SoundEvents.RABBIT_JUMP, 0.8F, 1.2F);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight() * 0.75, this.getZ(), 7, 0.4, 0.4, 0.4, 0.0);
                    }
                } else {
                    begTicks = 30;
                    this.playSound(SoundEvents.RABBIT_HURT, 0.6F, 1.2F);
                }
                return InteractionResult.SUCCESS;
            }
            // If not tame food, beg for it
            begTicks = 40;
            return InteractionResult.SUCCESS;
        }

        // Feed to heal
        if (this.isFood(itemInHand) && this.getHealth() < this.getMaxHealth()) {
            if (!player.getAbilities().instabuild) itemInHand.shrink(1);
            this.heal(4.0F);
            return InteractionResult.SUCCESS;
        }

        // Any owner shift+right-click with empty hand toggles independent mode
        if (this.isOwnedBy(player) && player.isSecondaryUseActive() && itemInHand.isEmpty()) {
            lastCommandedBy = player.getUUID();
            independent = !independent;
            if (independent) {
                homeX = this.getBlockX(); homeY = this.getBlockY(); homeZ = this.getBlockZ();
                this.setOrderedToSit(false);
                this.setInSittingPose(false);
                if (player instanceof net.minecraft.server.level.ServerPlayer sp) sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("Raccoon is now independent and will wander here."), true);
                if (this.level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + this.getBbHeight() * 0.75, this.getZ(), 6, 0.3, 0.3, 0.3, 0.0);
            } else {
                if (player instanceof net.minecraft.server.level.ServerPlayer sp) sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("Raccoon will follow you again."), true);
            }
            this.playSound(SoundEvents.RABBIT_JUMP, 0.6F, 1.4F);
            return InteractionResult.SUCCESS;
        }

        // Any owner toggles sit
        if (this.isOwnedBy(player) && !player.isSecondaryUseActive()) {
            lastCommandedBy = player.getUUID();
            boolean sit = !this.isOrderedToSit();
            this.setOrderedToSit(sit);
            this.setInSittingPose(sit);
            this.navigation.stop();
            this.playSound(SoundEvents.RABBIT_JUMP, 0.8F, 1.2F);
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    private static boolean isTameFood(ItemStack s) {
        return s.is(Items.BREAD) || s.is(Items.APPLE) || s.is(Items.SWEET_BERRIES)
                || s.is(Items.CARROT) || s.is(Items.BEETROOT) || s.is(Items.POTATO)
                || s.is(Items.WHEAT_SEEDS) || s.is(Items.MELON_SEEDS) || s.is(Items.PUMPKIN_SEEDS)
                || s.is(Items.BEETROOT_SEEDS) || s.is(Items.MELON_SLICE);
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
    public boolean isFood(ItemStack itemStack) { return isTameFood(itemStack); }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mate) {
        return new RaccoonBaby(MobRegistry.RACCOON_BABY, level);
    }

    @Override protected SoundEvent getHurtSound(@NotNull DamageSource d) { return SoundEvents.RABBIT_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.RABBIT_DEATH; }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState state) {
        this.playSound(SoundEvents.RABBIT_JUMP, 0.15F, 1.0F);
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int roll = this.random.nextInt(20);
        if (roll < 6) this.spawnAtLocation(level, new ItemStack(Items.WHEAT_SEEDS, 1));
        else if (roll < 10) this.spawnAtLocation(level, new ItemStack(Items.STICK, 1));
        else if (roll < 14) this.spawnAtLocation(level, new ItemStack(Items.BONE, 1));
        else if (roll < 16) this.spawnAtLocation(level, new ItemStack(Items.APPLE, 1));
        else if (roll < 18) this.spawnAtLocation(level, new ItemStack(Items.GOLD_NUGGET, 1));
        else if (roll < 19) this.spawnAtLocation(level, new ItemStack(Items.EMERALD, 1));
        else this.spawnAtLocation(level, new ItemStack(Items.DIAMOND, 1));
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) { return new SmoothGroundNavigation(this, level); }

    private static class PassivePetSitGoal extends Goal {
        private final TamableAnimal pet;
        PassivePetSitGoal(TamableAnimal pet) { this.pet = pet; this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE)); }
        @Override public boolean canUse() { return this.pet.isTame() && this.pet.isOrderedToSit(); }
        @Override public boolean canContinueToUse() { return this.pet.isOrderedToSit(); }
        @Override public void start() { this.pet.getNavigation().stop(); this.pet.setInSittingPose(true); }
        @Override public void stop() { this.pet.setInSittingPose(false); }
    }
}
