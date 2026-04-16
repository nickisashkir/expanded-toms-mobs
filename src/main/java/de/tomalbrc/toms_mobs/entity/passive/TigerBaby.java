package de.tomalbrc.toms_mobs.entity.passive;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.entity.goal.aquatic.FollowAdultGoal;
import de.tomalbrc.toms_mobs.registry.MobRegistry;
import de.tomalbrc.toms_mobs.util.AnimationHelper;
import de.tomalbrc.toms_mobs.util.BabyGrowthHelper;
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
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.tslat.smartbrainlib.api.core.navigation.SmoothGroundNavigation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TigerBaby extends Animal implements AnimatedEntity {
    public static final Identifier ID = Util.id("tiger_baby");
    public static final Model MODEL = Util.loadBbModel(ID);
    private final EntityHolder<TigerBaby> holder;
    private int rollTicks = 0;
    private int babyAge = 0;
    private boolean ageFrozen = false;

    @NotNull public static AttributeSupplier.Builder createAttributes() { return Animal.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.3); }
    @Override public EntityHolder<TigerBaby> getHolder() { return this.holder; }
    public TigerBaby(EntityType<? extends @NotNull Animal> type, Level level) { super(type, level); this.holder = new LivingEntityHolder<>(this, MODEL); EntityAttachment.ofTicking(this.holder, this); }
    @Override protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(3, new FollowAdultGoal(this, 1.0, a -> a instanceof Tiger));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }
    @Override public void tick() {
        super.tick();
        if (!this.level().isClientSide()) { babyAge++; BabyGrowthHelper.tryGrowUp(this, MobRegistry.TIGER, babyAge, ageFrozen); }
        if (this.tickCount % 2 == 0) {
            if (rollTicks > 0) {
                rollTicks -= 2;
                this.holder.getAnimator().playAnimation("roll");
                this.holder.getAnimator().pauseAnimation("walk");
                this.holder.getAnimator().pauseAnimation("idle");
            } else {
                this.holder.getAnimator().pauseAnimation("roll");
                AnimationHelper.updateWalkAnimation(this, this.holder);
            }
            AnimationHelper.updateHurtColor(this, this.holder);
        }
    }
    @Override public @NotNull InteractionResult mobInteract(@NotNull Player p, @NotNull InteractionHand h) {
        if (BabyGrowthHelper.tryFreezeGrowth(this, p, h, ageFrozen)) { ageFrozen = true; return InteractionResult.CONSUME; }
        InteractionResult r = super.mobInteract(p, h); if (r.consumesAction()) return r;
        rollTicks = 40; return InteractionResult.CONSUME;
    }
    @Override protected void addAdditionalSaveData(@NotNull ValueOutput output) { super.addAdditionalSaveData(output); BabyGrowthHelper.saveAge(output, babyAge, ageFrozen); }
    @Override protected void readAdditionalSaveData(@NotNull ValueInput input) { super.readAdditionalSaveData(input); babyAge = BabyGrowthHelper.loadAge(input); ageFrozen = BabyGrowthHelper.loadFrozen(input); }
    @Override public boolean isFood(ItemStack i) { return false; }
    @Nullable @Override public AgeableMob getBreedOffspring(@NotNull ServerLevel l, @NotNull AgeableMob m) { return null; }
    @Override protected SoundEvent getHurtSound(@NotNull DamageSource d) { return SoundEvents.OCELOT_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.OCELOT_DEATH; }
    @Override protected @NotNull PathNavigation createNavigation(@NotNull Level l) { return new SmoothGroundNavigation(this, l); }
}
