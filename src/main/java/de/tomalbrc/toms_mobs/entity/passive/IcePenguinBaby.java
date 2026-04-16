package de.tomalbrc.toms_mobs.entity.passive;
import de.tomalbrc.bil.api.AnimatedEntity; import de.tomalbrc.bil.core.holder.entity.EntityHolder; import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder; import de.tomalbrc.bil.core.model.Model; import de.tomalbrc.toms_mobs.entity.control.SemiAquaticMoveControl; import de.tomalbrc.toms_mobs.entity.goal.aquatic.*; import de.tomalbrc.toms_mobs.registry.MobRegistry; import de.tomalbrc.toms_mobs.util.AnimationHelper; import de.tomalbrc.toms_mobs.util.BabyGrowthHelper; import de.tomalbrc.toms_mobs.util.Util; import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import aqario.fowlplay.common.entity.ai.navigation.AmphibiousNavigation;
import net.minecraft.core.BlockPos; import net.minecraft.resources.Identifier; import net.minecraft.server.level.ServerLevel; import net.minecraft.sounds.SoundEvent; import net.minecraft.sounds.SoundEvents; import net.minecraft.tags.FluidTags; import net.minecraft.world.InteractionHand; import net.minecraft.world.InteractionResult; import net.minecraft.world.damagesource.DamageSource; import net.minecraft.world.entity.AgeableMob; import net.minecraft.world.entity.EntityType; import net.minecraft.world.entity.ai.attributes.AttributeSupplier; import net.minecraft.world.entity.ai.attributes.Attributes; import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal; import net.minecraft.world.entity.ai.navigation.PathNavigation; import net.minecraft.world.entity.animal.Animal; import net.minecraft.world.entity.player.Player; import net.minecraft.world.item.ItemStack; import net.minecraft.world.level.Level; import net.minecraft.world.level.LevelReader; import net.minecraft.world.level.pathfinder.PathType; import net.minecraft.world.level.storage.ValueInput; import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull; import org.jetbrains.annotations.Nullable;
public class IcePenguinBaby extends Animal implements AnimatedEntity {
    public static final Identifier ID = Util.id("ice_penguin_baby"); public static final Model MODEL = Util.loadBbModel(ID);
    private final EntityHolder<IcePenguinBaby> holder;
    private int babyAge = 0;
    private boolean ageFrozen = false;
    @NotNull public static AttributeSupplier.Builder createAttributes() { return Animal.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0).add(Attributes.MOVEMENT_SPEED, 0.2); }
    @Override public EntityHolder<IcePenguinBaby> getHolder() { return this.holder; }
    public IcePenguinBaby(EntityType<? extends @NotNull Animal> type, Level level) { super(type, level); this.setPathfindingMalus(PathType.WATER, 0.0F); this.moveControl = new SemiAquaticMoveControl(this); this.holder = new LivingEntityHolder<>(this, MODEL); EntityAttachment.ofTicking(this.holder, this); }
    @Override protected void registerGoals() { this.goalSelector.addGoal(1, new AquaticPanicGoal(this, 1.0)); this.goalSelector.addGoal(3, new FollowAdultGoal(this, 0.8, a -> a instanceof IcePenguin)); this.goalSelector.addGoal(5, new AquaticWaterAvoidingRandomStrollGoal(this, 0.4)); this.goalSelector.addGoal(5, new PathfinderMobSwimGoal(this, 2)); this.goalSelector.addGoal(8, new AquaticRandomLookAroundGoal(this)); this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 6.0F)); }
    @Override public float getWalkTargetValue(@NotNull BlockPos bp, LevelReader lr) { if (lr.getFluidState(bp).is(FluidTags.WATER)) return 1; return lr.getPathfindingCostFromLightLevels(bp); }
    @Override public void tick() {
        if (this.isInWater()) { this.setAirSupply(this.getMaxAirSupply()); }
        super.tick();
        if (!this.level().isClientSide()) { babyAge++; BabyGrowthHelper.tryGrowUp(this, MobRegistry.ICE_PENGUIN, babyAge, ageFrozen); }
        if (this.tickCount % 2 == 0) { AnimationHelper.updateAquaticWalkAnimation(this, this.holder); AnimationHelper.updateHurtColor(this, this.holder); }
    }
    @Override public @NotNull InteractionResult mobInteract(@NotNull Player p, @NotNull InteractionHand h) {
        if (BabyGrowthHelper.tryFreezeGrowth(this, p, h, ageFrozen)) { ageFrozen = true; return InteractionResult.CONSUME; }
        return super.mobInteract(p, h);
    }
    @Override protected void addAdditionalSaveData(@NotNull ValueOutput output) { super.addAdditionalSaveData(output); BabyGrowthHelper.saveAge(output, babyAge, ageFrozen); }
    @Override protected void readAdditionalSaveData(@NotNull ValueInput input) { super.readAdditionalSaveData(input); babyAge = BabyGrowthHelper.loadAge(input); ageFrozen = BabyGrowthHelper.loadFrozen(input); }
    @Override public boolean isFood(ItemStack i) { return false; }
    @Nullable @Override public AgeableMob getBreedOffspring(@NotNull ServerLevel l, @NotNull AgeableMob m) { return null; }
    @Override protected SoundEvent getHurtSound(@NotNull DamageSource d) { return SoundEvents.PARROT_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PARROT_DEATH; }
    @Override protected @NotNull PathNavigation createNavigation(@NotNull Level l) { return new AmphibiousNavigation(this, l); }
    @Override public boolean isPushedByFluid() { return false; }
    @Override public boolean canBreatheUnderwater() { return true; }
}
