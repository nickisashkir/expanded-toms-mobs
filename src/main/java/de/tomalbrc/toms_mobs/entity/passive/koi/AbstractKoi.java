package de.tomalbrc.toms_mobs.entity.passive.koi;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.util.AnimationHelper;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractKoi extends Animal implements AnimatedEntity {
    private final EntityHolder<? extends AbstractKoi> holder;
    private double circleAngle;
    private double circleCenterX;
    private double circleCenterZ;
    private double circleRadius;
    private boolean circleInitialized = false;

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.15);
    }

    @Override
    public EntityHolder<? extends AbstractKoi> getHolder() { return this.holder; }

    protected AbstractKoi(EntityType<? extends Animal> type, Level level, Model model) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.holder = new LivingEntityHolder<>(this, model);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    protected void registerGoals() {
        // No goals — koi use custom tick-based circular movement
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide() && this.isInWater()) {
            if (!circleInitialized) {
                // Initialize circle center near spawn position with slight randomness
                circleRadius = 2.0 + this.random.nextDouble() * 3.0;
                circleAngle = this.random.nextDouble() * Math.PI * 2;
                circleCenterX = this.getX() + this.random.nextDouble() * 2.0 - 1.0;
                circleCenterZ = this.getZ() + this.random.nextDouble() * 2.0 - 1.0;
                circleInitialized = true;
            }

            // Swim in a circle
            double speed = 0.03 + this.random.nextDouble() * 0.01;
            circleAngle += speed;
            if (circleAngle > Math.PI * 2) circleAngle -= Math.PI * 2;

            double targetX = circleCenterX + Math.cos(circleAngle) * circleRadius;
            double targetZ = circleCenterZ + Math.sin(circleAngle) * circleRadius;

            double dx = targetX - this.getX();
            double dz = targetZ - this.getZ();

            // Maintain water level — stay near the surface
            double targetY = this.level().getSeaLevel() - 1.0;
            double dy = (targetY - this.getY()) * 0.1;

            this.setDeltaMovement(dx * 0.15, dy, dz * 0.15);

            // Face movement direction
            float yaw = (float) (Math.atan2(-dx, dz) * (180.0 / Math.PI));
            this.setYRot(yaw);
            this.yHeadRot = yaw;
            this.yBodyRot = yaw;
        }

        // Cancel downward drift in water
        if (this.isInWater() && this.getDeltaMovement().y < -0.01) {
            this.setDeltaMovement(this.getDeltaMovement().x, 0.0, this.getDeltaMovement().z);
        }

        if (this.tickCount % 2 == 0) {
            this.holder.getAnimator().playAnimation("idle");
            AnimationHelper.updateHurtColor(this, this.holder);
        }
    }

    @Override
    public boolean isFood(@NotNull ItemStack itemStack) { return itemStack.is(Items.WHEAT_SEEDS); }

    @Nullable @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mate) { return null; }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource d) { return SoundEvents.TROPICAL_FISH_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.TROPICAL_FISH_DEATH; }

    @Override
    public boolean isPushedByFluid() { return false; }

    @Override
    public boolean canBreatheUnderwater() { return true; }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        return InteractionResult.PASS;
    }
}
