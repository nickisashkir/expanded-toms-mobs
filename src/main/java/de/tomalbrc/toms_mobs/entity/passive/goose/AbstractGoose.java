package de.tomalbrc.toms_mobs.entity.passive.goose;

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

public abstract class AbstractGoose extends Animal implements AnimatedEntity {
    private final EntityHolder<? extends AbstractGoose> holder;
    private int chargeAnimTicks = 0;
    private int flapAnimTicks = 0;

    // Familiarity: after enough feeds (with a cooldown between each), player joins the familiar set.
    // Familiar players don't trigger territorial aggression and have a 50% chance to avoid retaliation.
    private final java.util.Set<java.util.UUID> familiarPlayers = new java.util.HashSet<>();
    private final java.util.Map<java.util.UUID, Integer> feedCounts = new java.util.HashMap<>();
    private final java.util.Map<java.util.UUID, Long> lastFeedTick = new java.util.HashMap<>();
    private static final int FEEDS_TO_FAMILIAR = 5;
    private static final long FEED_COOLDOWN_TICKS = 6000L; // 5 minutes of game time

    public boolean isFamiliarWith(java.util.UUID id) { return familiarPlayers.contains(id); }

    /** Returns true if the feed actually counted (cooldown elapsed). */
    public boolean tryCountFeed(Player player) {
        long now = this.level().getGameTime();
        java.util.UUID id = player.getUUID();
        if (familiarPlayers.contains(id)) return false; // already familiar
        Long last = lastFeedTick.get(id);
        if (last != null && now - last < FEED_COOLDOWN_TICKS) return false;
        lastFeedTick.put(id, now);
        int next = feedCounts.getOrDefault(id, 0) + 1;
        feedCounts.put(id, next);
        if (next >= FEEDS_TO_FAMILIAR) {
            familiarPlayers.add(id);
            feedCounts.remove(id);
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART,
                        this.getX(), this.getY() + this.getBbHeight() * 0.9, this.getZ(),
                        6, 0.3, 0.3, 0.3, 0.0);
            }
            this.playSound(SoundEvents.PARROT_AMBIENT, 0.8F, 1.2F);
        }
        return true;
    }

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override public EntityHolder<? extends AbstractGoose> getHolder() { return this.holder; }

    protected AbstractGoose(EntityType<? extends Animal> type, Level level, Model model) {
        super(type, level);
        this.holder = new LivingEntityHolder<>(this, model);
        EntityAttachment.ofTicking(this.holder, this);
    }

    /** Override to true to make the goose chase players who get too close. */
    protected boolean isTerritorial() { return false; }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.5, true));
        this.goalSelector.addGoal(2, new PanicGoal(this, 1.3));
        this.goalSelector.addGoal(3, new BreedGoal(this, 0.8));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new TerritorialPlayerGoal(this));
    }

    /**
     * Only canada geese use this. Triggers when a player comes within 4 blocks.
     * The goose then chases that player up to 30 blocks from where it first aggroed.
     * Once the player escapes the territory, the goose drops the target.
     */
    private static class TerritorialPlayerGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final AbstractGoose goose;
        private Player target;
        private double homeX, homeZ;
        private static final double TRIGGER_RADIUS = 4.0;
        private static final double GIVE_UP_RADIUS = 30.0;

        TerritorialPlayerGoal(AbstractGoose goose) {
            this.goose = goose;
            this.setFlags(java.util.EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!goose.isTerritorial()) return false;
            Player near = goose.level().getNearestPlayer(goose, TRIGGER_RADIUS);
            if (near == null || !near.isAlive() || near.isCreative() || near.isSpectator()) return false;
            // Familiar players don't trigger territorial aggression
            if (goose.isFamiliarWith(near.getUUID())) return false;
            target = near;
            homeX = goose.getX();
            homeZ = goose.getZ();
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (target == null || !target.isAlive()) return false;
            double dx = target.getX() - homeX;
            double dz = target.getZ() - homeZ;
            return dx * dx + dz * dz < GIVE_UP_RADIUS * GIVE_UP_RADIUS;
        }

        @Override
        public void start() { goose.setTarget(target); }

        @Override
        public void stop() {
            goose.setTarget(null);
            target = null;
        }
    }

    @Override
    public boolean doHurtTarget(@NotNull ServerLevel level, @NotNull Entity target) {
        chargeAnimTicks = 16;
        this.playSound(SoundEvents.PARROT_HURT, 1.2F, 0.7F);
        return super.doHurtTarget(level, target);
    }

    @Override
    public void tick() {
        super.tick();

        // Occasional wing flap when idle on ground
        if (!this.level().isClientSide() && this.onGround() && flapAnimTicks <= 0 && chargeAnimTicks <= 0
                && this.tickCount % 40 == 0 && this.random.nextInt(60) == 0
                && this.getNavigation().isDone()) {
            flapAnimTicks = 40;
        }

        if (this.tickCount % 2 == 0) {
            if (chargeAnimTicks > 0) {
                chargeAnimTicks -= 2;
                this.holder.getAnimator().playAnimation("charge");
                this.holder.getAnimator().pauseAnimation("walk");
                this.holder.getAnimator().pauseAnimation("idle");
                this.holder.getAnimator().pauseAnimation("flap");
            } else if (flapAnimTicks > 0) {
                flapAnimTicks -= 2;
                this.holder.getAnimator().playAnimation("flap");
                this.holder.getAnimator().pauseAnimation("walk");
                this.holder.getAnimator().pauseAnimation("idle");
                this.holder.getAnimator().pauseAnimation("charge");
            } else {
                this.holder.getAnimator().pauseAnimation("charge");
                this.holder.getAnimator().pauseAnimation("flap");
                AnimationHelper.updateWalkAnimation(this, this.holder);
            }
            AnimationHelper.updateHurtColor(this, this.holder);
        }
    }

    @Override public boolean isFood(ItemStack i) { return i.is(Items.WHEAT) || i.is(Items.WHEAT_SEEDS) || i.is(Items.BEETROOT_SEEDS); }
    @Nullable @Override public AgeableMob getBreedOffspring(@NotNull ServerLevel l, @NotNull AgeableMob m) { return null; }

    @Override
    @NotNull
    public net.minecraft.world.InteractionResult mobInteract(@NotNull Player player, @NotNull net.minecraft.world.InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        if (isFood(item)) {
            boolean counted = tryCountFeed(player);
            if (counted) {
                if (!player.getAbilities().instabuild) item.shrink(1);
                if (this.getHealth() < this.getMaxHealth()) this.heal(2.0F);
                this.playSound(SoundEvents.PARROT_EAT, 0.6F, 1.2F);
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
            // On cooldown: fall through to vanilla (may still trigger breeding if love mode)
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurtServer(@NotNull ServerLevel level, @NotNull DamageSource source, float amount) {
        // Familiar players have a 50% chance not to trigger retaliation
        if (source.getEntity() instanceof Player p && familiarPlayers.contains(p.getUUID()) && this.random.nextBoolean()) {
            boolean wasHurt = super.hurtServer(level, source, amount);
            this.setLastHurtByMob(null);
            this.setTarget(null);
            return wasHurt;
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull net.minecraft.world.level.storage.ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("FamiliarPlayers", familiarPlayers.stream().map(java.util.UUID::toString).collect(java.util.stream.Collectors.joining(",")));
        StringBuilder feeds = new StringBuilder();
        for (var e : feedCounts.entrySet()) {
            if (feeds.length() > 0) feeds.append(";");
            feeds.append(e.getKey()).append(":").append(e.getValue());
        }
        output.putString("FeedCounts", feeds.toString());
        StringBuilder lastFeed = new StringBuilder();
        for (var e : lastFeedTick.entrySet()) {
            if (lastFeed.length() > 0) lastFeed.append(";");
            lastFeed.append(e.getKey()).append(":").append(e.getValue());
        }
        output.putString("LastFeedTick", lastFeed.toString());
    }

    @Override
    protected void readAdditionalSaveData(@NotNull net.minecraft.world.level.storage.ValueInput input) {
        super.readAdditionalSaveData(input);
        familiarPlayers.clear();
        feedCounts.clear();
        lastFeedTick.clear();
        String fam = input.getStringOr("FamiliarPlayers", "");
        if (!fam.isEmpty()) for (String s : fam.split(",")) try { familiarPlayers.add(java.util.UUID.fromString(s)); } catch (IllegalArgumentException ignored) {}
        String feeds = input.getStringOr("FeedCounts", "");
        if (!feeds.isEmpty()) for (String pair : feeds.split(";")) {
            int colon = pair.indexOf(':');
            if (colon <= 0) continue;
            try { feedCounts.put(java.util.UUID.fromString(pair.substring(0, colon)), Integer.parseInt(pair.substring(colon + 1))); } catch (Exception ignored) {}
        }
        String last = input.getStringOr("LastFeedTick", "");
        if (!last.isEmpty()) for (String pair : last.split(";")) {
            int colon = pair.indexOf(':');
            if (colon <= 0) continue;
            try { lastFeedTick.put(java.util.UUID.fromString(pair.substring(0, colon)), Long.parseLong(pair.substring(colon + 1))); } catch (Exception ignored) {}
        }
    }
    @Override protected SoundEvent getHurtSound(@NotNull DamageSource d) { return SoundEvents.PARROT_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PARROT_DEATH; }

    /** Resource id of the filament head item that this goose drops (null = no head). */
    protected net.minecraft.resources.Identifier getHeadItemId() { return null; }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel l, @NotNull DamageSource s, boolean r) {
        super.dropCustomDeathLoot(l, s, r);
        this.spawnAtLocation(l, new ItemStack(Items.FEATHER, 1 + this.random.nextInt(2)));

        // 20% chance to drop the head
        net.minecraft.resources.Identifier headId = getHeadItemId();
        if (headId != null && this.random.nextInt(5) == 0) {
            var head = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(headId);
            if (head != net.minecraft.world.item.Items.AIR) {
                this.spawnAtLocation(l, new ItemStack(head));
            }
        }
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) { return new SmoothGroundNavigation(this, level); }
}
