package de.tomalbrc.toms_mobs.entity.passive;
import de.tomalbrc.bil.api.AnimatedEntity; import de.tomalbrc.bil.core.holder.entity.EntityHolder; import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder; import de.tomalbrc.bil.core.model.Model; import de.tomalbrc.toms_mobs.util.AnimationHelper; import de.tomalbrc.toms_mobs.util.Util; import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.resources.Identifier; import net.minecraft.server.level.ServerLevel; import net.minecraft.sounds.SoundEvent; import net.minecraft.sounds.SoundEvents; import net.minecraft.world.damagesource.DamageSource; import net.minecraft.world.entity.AgeableMob; import net.minecraft.world.entity.EntityType; import net.minecraft.world.entity.ai.attributes.AttributeSupplier; import net.minecraft.world.entity.ai.attributes.Attributes; import net.minecraft.world.entity.ai.goal.*; import net.minecraft.world.entity.ai.navigation.PathNavigation; import net.minecraft.world.entity.animal.Animal; import net.minecraft.world.entity.player.Player; import net.minecraft.world.item.ItemStack; import net.minecraft.world.item.Items; import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.core.navigation.SmoothGroundNavigation; import org.jetbrains.annotations.NotNull; import org.jetbrains.annotations.Nullable;
public class Reindeer extends Animal implements AnimatedEntity {
    public static final Identifier ID = Util.id("reindeer"); public static final Model MODEL = Util.loadBbModel(ID);
    private final EntityHolder<Reindeer> holder;
    @NotNull public static AttributeSupplier.Builder createAttributes() { return Animal.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.25); }
    @Override public EntityHolder<Reindeer> getHolder() { return this.holder; }
    public Reindeer(EntityType<? extends @NotNull Animal> type, Level level) { super(type, level); this.holder = new LivingEntityHolder<>(this, MODEL); EntityAttachment.ofTicking(this.holder, this); }
    @Override protected void registerGoals() { this.goalSelector.addGoal(0, new FloatGoal(this)); this.goalSelector.addGoal(1, new PanicGoal(this, 1.3)); this.goalSelector.addGoal(3, new BreedGoal(this, 0.7)); this.goalSelector.addGoal(4, new FollowParentGoal(this, 0.7)); this.goalSelector.addGoal(5, new EatBlockGoal(this)); this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6)); this.goalSelector.addGoal(7, new RandomLookAroundGoal(this)); this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F)); }
    @Override public void tick() { super.tick(); if (this.tickCount % 2 == 0) { AnimationHelper.updateWalkAnimation(this, this.holder); AnimationHelper.updateHurtColor(this, this.holder); } }
    @Override public void ate() { super.ate(); this.heal(2.0F); }
    @Override public boolean isFood(ItemStack i) { return i.is(Items.WHEAT) || i.is(Items.CARROT); }
    @Nullable @Override public AgeableMob getBreedOffspring(@NotNull ServerLevel l, @NotNull AgeableMob m) { return null; }
    @Override protected void dropCustomDeathLoot(@NotNull ServerLevel l, @NotNull DamageSource s, boolean r) { super.dropCustomDeathLoot(l, s, r); this.spawnAtLocation(l, new ItemStack(Items.LEATHER, 1 + this.random.nextInt(2))); }
    @Override protected SoundEvent getHurtSound(@NotNull DamageSource d) { return SoundEvents.GOAT_SCREAMING_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.GOAT_SCREAMING_DEATH; }
    @Override protected @NotNull PathNavigation createNavigation(@NotNull Level l) { return new SmoothGroundNavigation(this, l); }
}
