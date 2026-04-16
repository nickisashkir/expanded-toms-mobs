package de.tomalbrc.toms_mobs.entity.passive;
import de.tomalbrc.bil.api.AnimatedEntity; import de.tomalbrc.bil.core.holder.entity.EntityHolder; import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder; import de.tomalbrc.bil.core.model.Model; import de.tomalbrc.toms_mobs.util.AnimationHelper; import de.tomalbrc.toms_mobs.util.Util; import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.resources.Identifier; import net.minecraft.server.level.ServerLevel; import net.minecraft.sounds.SoundEvent; import net.minecraft.sounds.SoundEvents; import net.minecraft.world.InteractionHand; import net.minecraft.world.InteractionResult; import net.minecraft.world.damagesource.DamageSource; import net.minecraft.world.entity.AgeableMob; import net.minecraft.world.entity.EntityType; import net.minecraft.world.entity.ai.attributes.AttributeSupplier; import net.minecraft.world.entity.ai.attributes.Attributes; import net.minecraft.world.entity.ai.goal.*; import net.minecraft.world.entity.ai.navigation.PathNavigation; import net.minecraft.world.entity.animal.Animal; import net.minecraft.world.entity.player.Player; import net.minecraft.world.item.ItemStack; import net.minecraft.world.item.Items; import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.core.navigation.SmoothGroundNavigation; import org.jetbrains.annotations.NotNull; import org.jetbrains.annotations.Nullable;
public class Hedgehog extends Animal implements AnimatedEntity {
    public static final Identifier ID = Util.id("hedgehog"); public static final Model MODEL = Util.loadBbModel(ID);
    private final EntityHolder<Hedgehog> holder; private int sniffTicks = 0; private int curlTicks = 0;
    @NotNull public static AttributeSupplier.Builder createAttributes() { return Animal.createMobAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.2); }
    @Override public EntityHolder<Hedgehog> getHolder() { return this.holder; }
    public Hedgehog(EntityType<? extends @NotNull Animal> type, Level level) { super(type, level); this.holder = new LivingEntityHolder<>(this, MODEL); EntityAttachment.ofTicking(this.holder, this); }
    @Override protected void registerGoals() { this.goalSelector.addGoal(0, new FloatGoal(this)); this.goalSelector.addGoal(1, new PanicGoal(this, 1.2)); this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.6)); this.goalSelector.addGoal(5, new RandomLookAroundGoal(this)); this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F)); }
    @Override public void tick() {
        super.tick();
        if (curlTicks > 0) { curlTicks--; this.getNavigation().stop(); this.setDeltaMovement(0, this.getDeltaMovement().y, 0); }
        if (this.tickCount % 2 == 0) {
            if (curlTicks > 0) { this.holder.getAnimator().playAnimation("sniff"); this.holder.getAnimator().pauseAnimation("walk"); this.holder.getAnimator().pauseAnimation("idle"); }
            else if (sniffTicks > 0) { sniffTicks -= 2; this.holder.getAnimator().playAnimation("sniff"); this.holder.getAnimator().pauseAnimation("walk"); this.holder.getAnimator().pauseAnimation("idle"); }
            else { this.holder.getAnimator().pauseAnimation("sniff"); AnimationHelper.updateWalkAnimation(this, this.holder); }
            AnimationHelper.updateHurtColor(this, this.holder);
        }
    }
    @Override public boolean hurtServer(@NotNull ServerLevel level, @NotNull DamageSource source, float amount) {
        // When curled, take 25% damage. Otherwise, start curling for 2 seconds.
        if (curlTicks > 0) {
            amount *= 0.25F;
        } else {
            curlTicks = 40;
        }
        return super.hurtServer(level, source, amount);
    }
    @Override public @NotNull InteractionResult mobInteract(@NotNull Player p, @NotNull InteractionHand h) { InteractionResult r = super.mobInteract(p, h); if (r.consumesAction()) return r; sniffTicks = 30; return InteractionResult.SUCCESS; }
    @Override public boolean isFood(ItemStack i) { return i.is(Items.APPLE) || i.is(Items.MELON_SLICE); }
    @Nullable @Override public AgeableMob getBreedOffspring(@NotNull ServerLevel l, @NotNull AgeableMob m) { return null; }
    @Override protected void dropCustomDeathLoot(@NotNull ServerLevel l, @NotNull DamageSource s, boolean r) { super.dropCustomDeathLoot(l, s, r); if (this.random.nextBoolean()) this.spawnAtLocation(l, new ItemStack(Items.APPLE, 1)); }
    @Override protected SoundEvent getHurtSound(@NotNull DamageSource d) { return SoundEvents.RABBIT_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.RABBIT_DEATH; }
    @Override protected @NotNull PathNavigation createNavigation(@NotNull Level l) { return new SmoothGroundNavigation(this, l); }
}
