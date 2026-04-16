package de.tomalbrc.toms_mobs.entity.passive;
import de.tomalbrc.bil.api.AnimatedEntity; import de.tomalbrc.bil.core.holder.entity.EntityHolder; import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder; import de.tomalbrc.bil.core.model.Model; import de.tomalbrc.toms_mobs.entity.control.SemiAquaticMoveControl; import de.tomalbrc.toms_mobs.entity.goal.aquatic.*; import de.tomalbrc.toms_mobs.util.AnimationHelper; import de.tomalbrc.toms_mobs.util.Util; import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import aqario.fowlplay.common.entity.ai.navigation.AmphibiousNavigation;
import net.minecraft.core.BlockPos; import net.minecraft.resources.Identifier; import net.minecraft.server.level.ServerLevel; import net.minecraft.sounds.SoundEvent; import net.minecraft.sounds.SoundEvents; import net.minecraft.tags.FluidTags; import net.minecraft.world.damagesource.DamageSource; import net.minecraft.world.entity.AgeableMob; import net.minecraft.world.entity.EntityType; import net.minecraft.world.entity.ai.attributes.AttributeSupplier; import net.minecraft.world.entity.ai.attributes.Attributes; import net.minecraft.world.entity.ai.goal.FollowParentGoal; import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal; import net.minecraft.world.entity.ai.navigation.PathNavigation; import net.minecraft.world.entity.animal.Animal; import net.minecraft.world.entity.player.Player; import net.minecraft.world.item.ItemStack; import net.minecraft.world.item.Items; import net.minecraft.world.level.Level; import net.minecraft.world.level.LevelReader; import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.NotNull; import org.jetbrains.annotations.Nullable;
public class IcePenguin extends Animal implements AnimatedEntity {
    public static final Identifier ID = Util.id("ice_penguin"); public static final Model MODEL = Util.loadBbModel(ID);
    private final EntityHolder<IcePenguin> holder;
    @NotNull public static AttributeSupplier.Builder createAttributes() { return Animal.createMobAttributes().add(Attributes.MAX_HEALTH, 12.0).add(Attributes.MOVEMENT_SPEED, 0.2); }
    @Override public EntityHolder<IcePenguin> getHolder() { return this.holder; }
    public IcePenguin(EntityType<? extends @NotNull Animal> type, Level level) { super(type, level); this.setPathfindingMalus(PathType.WATER, 0.0F); this.moveControl = new SemiAquaticMoveControl(this); this.holder = new LivingEntityHolder<>(this, MODEL); EntityAttachment.ofTicking(this.holder, this); }
    @Override protected void registerGoals() { this.goalSelector.addGoal(1, new AquaticPanicGoal(this, 0.9)); this.goalSelector.addGoal(3, new AquaticBreedGoal(this, 0.6)); this.goalSelector.addGoal(4, new AquaticFollowParentGoal(this, 0.6)); this.goalSelector.addGoal(5, new AquaticWaterAvoidingRandomStrollGoal(this, 0.5)); this.goalSelector.addGoal(5, new PathfinderMobSwimGoal(this, 3)); this.goalSelector.addGoal(8, new AquaticRandomLookAroundGoal(this)); this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 6.0F)); }
    @Override public float getWalkTargetValue(@NotNull BlockPos bp, LevelReader lr) { if (lr.getFluidState(bp).is(FluidTags.WATER)) return 1; return lr.getPathfindingCostFromLightLevels(bp); }
    @Override public void tick() { if (this.isInWater()) { this.setAirSupply(this.getMaxAirSupply()); } super.tick(); if (this.tickCount % 2 == 0) { AnimationHelper.updateAquaticWalkAnimation(this, this.holder); AnimationHelper.updateHurtColor(this, this.holder); } }
    @Override public boolean isFood(ItemStack i) { return i.is(Items.COD) || i.is(Items.SALMON); }
    @Nullable @Override public AgeableMob getBreedOffspring(@NotNull ServerLevel l, @NotNull AgeableMob m) { return null; }
    @Override protected void dropCustomDeathLoot(@NotNull ServerLevel l, @NotNull DamageSource s, boolean r) { super.dropCustomDeathLoot(l, s, r); this.spawnAtLocation(l, new ItemStack(Items.FEATHER, 1 + this.random.nextInt(2))); }
    @Override protected SoundEvent getHurtSound(@NotNull DamageSource d) { return SoundEvents.PARROT_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PARROT_DEATH; }
    @Override protected @NotNull PathNavigation createNavigation(@NotNull Level l) { return new AmphibiousNavigation(this, l); }
    @Override public boolean isPushedByFluid() { return false; }
    @Override public boolean canBreatheUnderwater() { return true; }
}
