package de.tomalbrc.toms_mobs.entity.passive.npc;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.util.PatrolRoute;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.core.navigation.SmoothGroundNavigation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractNpc extends Animal implements AnimatedEntity {
    private final EntityHolder<? extends AbstractNpc> holder;

    // Subclasses set this to true to suppress idle/walk animation (e.g. during special animations)
    protected boolean suppressIdleAnimation = false;

    // Patrol state
    private PatrolRoute patrolRoute = null;
    private int currentWaypointIndex = 0;
    private int pauseTimer = 0;
    private boolean patrolChecked = false;
    private boolean isPatrolling = false;

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public EntityHolder<? extends AbstractNpc> getHolder() { return this.holder; }

    protected AbstractNpc(EntityType<? extends Animal> type, Level level, Model model) {
        super(type, level);
        this.holder = new LivingEntityHolder<>(this, model);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    protected void registerGoals() {
        // No AI goals — patrol handled in tick()
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            // Check for patrol route assignment (once, then every 100 ticks to pick up changes)
            if (!patrolChecked || this.tickCount % 100 == 0) {
                patrolRoute = PatrolRoute.getRouteForNpc(this.getUUID());
                patrolChecked = true;
            }

            if (patrolRoute != null && patrolRoute.waypointCount() >= 2) {
                tickPatrol();
            } else {
                // No patrol — stand still
                this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
                this.getNavigation().stop();
                isPatrolling = false;
            }
        }

        if (this.tickCount % 2 == 0) {
            // Play walk animation when patrolling and moving, idle when standing
            // Skip if subclass is playing a special animation
            if (!suppressIdleAnimation) {
                if (isPatrolling && this.walkAnimation.isMoving() && this.walkAnimation.speed() > 0.02) {
                    this.holder.getAnimator().playAnimation(getWalkAnimation());
                    this.holder.getAnimator().pauseAnimation(getIdleAnimation());
                } else {
                    this.holder.getAnimator().playAnimation(getIdleAnimation());
                    this.holder.getAnimator().pauseAnimation(getWalkAnimation());
                }
            }

            // Only hurt color, no health hearts display for NPCs
            if (this.hurtTime > 0 || this.deathTime > 0)
                this.holder.setColor(0xff7e7e);
            else
                this.holder.clearColor();
        }
    }

    private void tickPatrol() {
        if (pauseTimer > 0) {
            pauseTimer--;
            isPatrolling = false;
            this.getNavigation().stop();
            return;
        }

        BlockPos target = patrolRoute.getWaypoint(currentWaypointIndex);

        // Check if we've arrived at the waypoint (within 2 blocks)
        if (this.blockPosition().closerThan(target, 2.0)) {
            // Pause at waypoint
            pauseTimer = patrolRoute.pauseTicks;
            isPatrolling = false;

            // Move to next waypoint
            currentWaypointIndex = (currentWaypointIndex + 1) % patrolRoute.waypointCount();
            return;
        }

        // Navigate to current waypoint
        if (this.getNavigation().isDone()) {
            this.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 0.6);
        }
        isPatrolling = true;
    }

    protected String getIdleAnimation() { return "idle"; }

    protected String getWalkAnimation() { return "walk"; }

    @Override
    public boolean isPushable() { return false; }

    @Override
    public boolean isFood(@NotNull ItemStack itemStack) { return false; }

    @Nullable @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mate) { return null; }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource d) { return SoundEvents.VILLAGER_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.VILLAGER_DEATH; }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) { return new SmoothGroundNavigation(this, level); }
}
