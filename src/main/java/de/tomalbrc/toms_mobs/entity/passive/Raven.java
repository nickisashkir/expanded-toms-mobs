package de.tomalbrc.toms_mobs.entity.passive;

import com.mojang.math.Axis;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.util.AnimationHelper;
import de.tomalbrc.toms_mobs.util.Util;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Raven extends TamableAnimal implements AnimatedEntity {
    public static final Identifier ID = Util.id("raven");
    public static final Model MODEL = Util.loadBbModel(ID);

    private final EntityHolder<Raven> holder;
    private final ItemDisplayElement carryDisplay;
    private int attackAnimTicks = 0;
    private boolean carryAttached = false;
    private ItemStack carryStack = ItemStack.EMPTY;

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
                .add(Attributes.MAX_HEALTH, 35.0)
                .add(Attributes.FLYING_SPEED, 0.2)
                .add(Attributes.MOVEMENT_SPEED, 0.18)
                .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    public EntityHolder<Raven> getHolder() { return this.holder; }

    public Raven(EntityType<? extends @NotNull TamableAnimal> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.holder = new LivingEntityHolder<>(this, MODEL);
        this.carryDisplay = new ItemDisplayElement();
        this.carryDisplay.setScale(new Vector3f(0.35f, 0.35f, 0.35f));
        this.carryDisplay.setOffset(new Vec3(0, 0.25, 0.35)); // forward/below beak
        this.carryDisplay.setLeftRotation(Axis.XP.rotationDegrees(30));
        this.carryDisplay.setViewRange(0.0f);
        this.carryDisplay.setTeleportDuration(3);
        EntityAttachment.ofTicking(this.holder, this);
    }

    public void setCarryStack(ItemStack stack) {
        this.carryStack = stack == null ? ItemStack.EMPTY : stack;
        if (!this.carryAttached) {
            this.holder.addAdditionalDisplay(this.carryDisplay);
            this.carryAttached = true;
        }
        this.carryDisplay.setItem(this.carryStack);
        this.carryDisplay.setViewRange(this.carryStack.isEmpty() ? 0.0f : 1.0f);
    }

    public ItemStack getCarryStack() { return this.carryStack; }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3, true));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.0, 12.0F, 3.0F) {
            @Override public boolean canUse() { return !Raven.this.isIndependent() && super.canUse(); }
        });
        this.goalSelector.addGoal(3, new EnderPearlFetchGoal(this));
        this.goalSelector.addGoal(4, new de.tomalbrc.toms_mobs.entity.goal.flying.BirdPerchGoal(this, 1.0, 50, 1200, 3600));
        this.goalSelector.addGoal(5, new de.tomalbrc.toms_mobs.entity.goal.flying.BirdWanderGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));

        // Angry enderman defense: only target endermen that are targeting a nearby owner.
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, EnderMan.class, 10, true, true, (e, sl) -> {
            if (!(e instanceof EnderMan em)) return false;
            LivingEntity emTarget = em.getTarget();
            if (emTarget == null) return false;
            return Raven.this.isOwnedBy(emTarget);
        }));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            // Dusk/night activity: faster flying, more active. Daytime = slower, lazier.
            if (this.tickCount % 20 == 0) {
                long time = this.level().getOverworldClockTime() % 24000;
                boolean isActive = time >= 11000 && time <= 23000;
                var speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
                var flyAttr = this.getAttribute(Attributes.FLYING_SPEED);
                if (isActive) {
                    if (speedAttr != null) speedAttr.setBaseValue(0.22);
                    if (flyAttr != null) flyAttr.setBaseValue(0.25);
                } else {
                    if (speedAttr != null) speedAttr.setBaseValue(0.14);
                    if (flyAttr != null) flyAttr.setBaseValue(0.16);
                }
            }

            // Resistance I aura to tamed owners within 8 blocks, refreshed every 3s
            if (this.isTame() && this.tickCount % 60 == 0) {
                AABB aura = this.getBoundingBox().inflate(8.0);
                for (Player p : this.level().getEntitiesOfClass(Player.class, aura)) {
                    if (this.isOwnedBy(p)) {
                        p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 0, true, false, true));
                    }
                }
            }

            // Independent mode: pull back home if too far
            if (independent && !this.isOrderedToSit() && this.tickCount % 40 == 0) {
                double dx = this.getX() - homeX;
                double dz = this.getZ() - homeZ;
                if (dx * dx + dz * dz > 256 && this.getNavigation().isDone()) {
                    this.getNavigation().moveTo(homeX + 0.5, homeY, homeZ + 0.5, 1.0);
                }
            }

            // Elytra speed matching. Only when tamed + an owner nearby is fall-flying.
            if (this.isTame()) {
                LivingEntity owner = this.getOwner();
                if (owner instanceof Player p && p.isFallFlying() && this.distanceToSqr(p) < 1024) {
                    this.setDeltaMovement(p.getDeltaMovement());
                }
            }
        }

        if (this.tickCount % 2 == 0) {
            if (attackAnimTicks > 0) {
                attackAnimTicks -= 2;
                this.holder.getAnimator().playAnimation("peck");
                this.holder.getAnimator().pauseAnimation("walk");
                this.holder.getAnimator().pauseAnimation("idle");
            } else {
                this.holder.getAnimator().pauseAnimation("peck");
                boolean isFlying = !this.onGround() && this.getDeltaMovement().horizontalDistanceSqr() > 0.001;
                if (!isFlying && !this.onGround()) {
                    BlockPos pos = this.blockPosition();
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
    public boolean doHurtTarget(@NotNull ServerLevel level, @NotNull Entity target) {
        attackAnimTicks = 14;
        return super.doHurtTarget(level, target);
    }

    @Override
    @NotNull
    public InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);

        if (!this.isTame()) {
            if (itemInHand.is(Items.RABBIT)) {
                if (!player.getAbilities().instabuild) itemInHand.shrink(1);
                if (this.random.nextInt(4) == 0) {
                    this.tame(player);
                    this.setOrderedToSit(false);
                    this.navigation.stop();
                    this.playSound(SoundEvents.PARROT_AMBIENT, 0.8F, 0.8F);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight() * 0.75, this.getZ(), 7, 0.4, 0.4, 0.4, 0.0);
                    }
                } else {
                    attackAnimTicks = 12;
                    this.playSound(SoundEvents.PARROT_HURT, 0.6F, 1.2F);
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

        // Owner shift+right-click empty hand: toggle independent mode
        if (this.isOwnedBy(player) && player.isSecondaryUseActive() && itemInHand.isEmpty()) {
            lastCommandedBy = player.getUUID();
            independent = !independent;
            if (independent) {
                homeX = this.getBlockX(); homeY = this.getBlockY(); homeZ = this.getBlockZ();
                this.setOrderedToSit(false);
                if (player instanceof net.minecraft.server.level.ServerPlayer sp) sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("Raven is now independent and will circle here."), true);
                if (this.level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + this.getBbHeight() * 0.75, this.getZ(), 6, 0.3, 0.3, 0.3, 0.0);
            } else {
                if (player instanceof net.minecraft.server.level.ServerPlayer sp) sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("Raven will follow you again."), true);
            }
            this.playSound(SoundEvents.PARROT_AMBIENT, 0.6F, 1.0F);
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
    public boolean isFood(ItemStack itemStack) { return itemStack.is(Items.RABBIT); }

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

    /**
     * Allay-style fetch goal but scoped to ender pearls and with a visible carry
     * display element attached near the raven's beak.
     */
    private static class EnderPearlFetchGoal extends Goal {
        private final Raven raven;
        private ItemEntity targetItem;
        private boolean carrying = false;
        private int stateTicks = 0;
        private int cooldown = 0;
        private static final int SCAN_RADIUS = 12;
        private static final double PICKUP_DIST = 1.75;
        private static final double DELIVER_DIST = 2.5;
        private static final int MAX_STATE_TICKS = 600;

        EnderPearlFetchGoal(Raven raven) {
            this.raven = raven;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (cooldown > 0) { cooldown--; return false; }
            if (!raven.isTame() || raven.isOrderedToSit()) return false;
            LivingEntity owner = raven.getOwner();
            if (!(owner instanceof Player) || !owner.isAlive()) return false;
            if (raven.distanceToSqr(owner) > 900) return false;

            AABB area = new AABB(owner.getX() - SCAN_RADIUS, owner.getY() - 4, owner.getZ() - SCAN_RADIUS,
                    owner.getX() + SCAN_RADIUS, owner.getY() + 4, owner.getZ() + SCAN_RADIUS);
            List<ItemEntity> pearls = raven.level().getEntitiesOfClass(ItemEntity.class, area,
                    e -> !e.hasPickUpDelay() && e.isAlive() && e.getItem().is(Items.ENDER_PEARL));
            if (pearls.isEmpty()) return false;

            ItemEntity closest = null;
            double closestDist = Double.MAX_VALUE;
            for (ItemEntity it : pearls) {
                double d = raven.distanceToSqr(it);
                if (d < closestDist) { closestDist = d; closest = it; }
            }
            targetItem = closest;
            return targetItem != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (stateTicks > MAX_STATE_TICKS) return false;
            if (!(raven.getOwner() instanceof Player owner) || !owner.isAlive()) return false;
            if (raven.isOrderedToSit()) return false;
            if (carrying) return true;
            return targetItem != null && targetItem.isAlive() && !targetItem.getItem().isEmpty();
        }

        @Override
        public void start() { stateTicks = 0; carrying = false; }

        @Override
        public void stop() {
            targetItem = null;
            carrying = false;
            stateTicks = 0;
            cooldown = 80;
            raven.setCarryStack(ItemStack.EMPTY);
            raven.getNavigation().stop();
        }

        @Override
        public void tick() {
            stateTicks++;
            LivingEntity owner = raven.getOwner();
            if (owner == null) return;

            if (!carrying) {
                if (targetItem == null || !targetItem.isAlive()) return;
                raven.getLookControl().setLookAt(targetItem, 30.0F, 30.0F);
                double dist = raven.distanceToSqr(targetItem);
                if (dist > PICKUP_DIST * PICKUP_DIST) {
                    if (raven.getNavigation().isDone()) raven.getNavigation().moveTo(targetItem, 1.2);
                } else {
                    // Pick up the ender pearl: show it on the raven, delete the world item
                    ItemStack carried = targetItem.getItem().copyWithCount(1);
                    raven.setCarryStack(carried);
                    // If the item entity stack had more than 1, leave the rest on the ground
                    if (targetItem.getItem().getCount() > 1) {
                        ItemStack remainder = targetItem.getItem().copy();
                        remainder.shrink(1);
                        targetItem.setItem(remainder);
                    } else {
                        targetItem.discard();
                    }
                    carrying = true;
                    raven.getNavigation().stop();
                    raven.playSound(SoundEvents.PARROT_EAT, 0.6F, 1.2F);
                }
            } else {
                raven.getLookControl().setLookAt(owner, 30.0F, 30.0F);
                double dist = raven.distanceToSqr(owner);
                if (dist > DELIVER_DIST * DELIVER_DIST) {
                    if (raven.getNavigation().isDone()) raven.getNavigation().moveTo(owner, 1.3);
                } else {
                    // Drop at owner's feet
                    ItemStack drop = raven.getCarryStack();
                    if (!drop.isEmpty()) {
                        ItemEntity dropped = new ItemEntity(raven.level(),
                                owner.getX(), owner.getY() + 0.5, owner.getZ(), drop.copy());
                        dropped.setNoPickUpDelay();
                        raven.level().addFreshEntity(dropped);
                        raven.playSound(SoundEvents.PARROT_AMBIENT, 0.8F, 1.2F);
                    }
                    raven.setCarryStack(ItemStack.EMPTY);
                    targetItem = null;
                    carrying = false;
                    stateTicks = MAX_STATE_TICKS;
                }
            }
        }
    }
}
