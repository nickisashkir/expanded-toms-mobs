package de.tomalbrc.toms_mobs.entity.passive;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.util.AnimationHelper;
import de.tomalbrc.toms_mobs.registry.MobRegistry;
import de.tomalbrc.toms_mobs.util.Util;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.core.navigation.SmoothGroundNavigation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Meerkat extends Animal implements AnimatedEntity {
    public static final Identifier ID = Util.id("meerkat");
    public static final Model MODEL = Util.loadBbModel(ID);
    private final EntityHolder<Meerkat> holder;
    private int peekTicks = 0;
    private int forageTicks = 0;
    private boolean huddling = false;

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    public EntityHolder<Meerkat> getHolder() {
        return this.holder;
    }

    public Meerkat(EntityType<? extends @NotNull Animal> type, Level level) {
        super(type, level);
        this.holder = new LivingEntityHolder<>(this, MODEL);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(3, new BreedGoal(this, 0.8));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Spider.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            // Freeze navigation while peeking (sentry stands still)
            if (peekTicks > 0) {
                this.getNavigation().stop();
            }

            if (this.tickCount % 20 == 0 && this.getTarget() == null) {
                boolean spiderNear = !this.level().getEntitiesOfClass(Spider.class,
                        this.getBoundingBox().inflate(10)).isEmpty();

                // Alarm caller: spider within 10 blocks? Trigger panic on all kin nearby.
                if (spiderNear) {
                    huddling = false;
                    for (Meerkat m : this.level().getEntitiesOfClass(Meerkat.class,
                            this.getBoundingBox().inflate(12))) {
                        m.peekTicks = 0;
                        m.forageTicks = 0;
                    }
                }

                long timeOfDay = this.level().getOverworldClockTime() % 24000;
                boolean isNight = timeOfDay >= 13000 && timeOfDay <= 23000;

                // Lookout: idle meerkat with kin nearby occasionally stands sentry (longer than before)
                if (!spiderNear && peekTicks <= 0 && forageTicks <= 0 && !huddling
                        && this.getNavigation().isDone()) {
                    int kin = this.level().getEntitiesOfClass(Meerkat.class,
                            this.getBoundingBox().inflate(12), m -> m != this).size();
                    if (kin >= 2 && this.tickCount % 100 == 0 && this.random.nextInt(4) == 0) {
                        peekTicks = 200 + this.random.nextInt(200); // 10-20 seconds
                    }
                }

                // Forager: random dig/search pauses during the day
                if (!spiderNear && !isNight && peekTicks <= 0 && forageTicks <= 0 && !huddling
                        && this.getNavigation().isDone()
                        && this.tickCount % 120 == 0 && this.random.nextInt(8) == 0) {
                    forageTicks = 80 + this.random.nextInt(80); // 4-8 seconds pause
                    this.getNavigation().stop();
                }

                // Night huddle: find nearest kin, path toward them. Broken by spider.
                if (!spiderNear && isNight && peekTicks <= 0 && this.tickCount % 60 == 0) {
                    Meerkat nearest = null;
                    double nearestDist = Double.MAX_VALUE;
                    for (Meerkat m : this.level().getEntitiesOfClass(Meerkat.class,
                            this.getBoundingBox().inflate(16), m -> m != this)) {
                        double d = this.distanceToSqr(m);
                        if (d < nearestDist) { nearestDist = d; nearest = m; }
                    }
                    if (nearest != null && nearestDist > 4.0 && this.getNavigation().isDone()) {
                        this.getNavigation().moveTo(nearest, 0.7);
                        huddling = true;
                    } else if (nearest != null && nearestDist <= 4.0) {
                        this.getNavigation().stop();
                        huddling = true;
                    }
                } else if (!isNight) {
                    huddling = false;
                }
            }

            if (forageTicks > 0) forageTicks--;
        }

        if (this.tickCount % 2 == 0) {
            if (peekTicks > 0) {
                peekTicks -= 2;
                this.holder.getAnimator().playAnimation("peek");
                this.holder.getAnimator().pauseAnimation("walk");
                this.holder.getAnimator().pauseAnimation("idle");
            } else {
                this.holder.getAnimator().pauseAnimation("peek");
                AnimationHelper.updateWalkAnimation(this, this.holder);
            }
            AnimationHelper.updateHurtColor(this, this.holder);
        }
    }

    @Override
    @NotNull
    public InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        InteractionResult result = super.mobInteract(player, hand);
        if (result.consumesAction()) return result;
        peekTicks = 40;
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(Items.SPIDER_EYE) || itemStack.is(Items.ROTTEN_FLESH);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mate) {
        return new MeerkatBaby(MobRegistry.MEERKAT_BABY, level);
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.FOX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.FOX_DEATH;
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.random.nextBoolean()) {
            this.spawnAtLocation(level, new ItemStack(Items.SPIDER_EYE, 1));
        }
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new SmoothGroundNavigation(this, level);
    }
}
