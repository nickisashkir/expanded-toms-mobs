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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.core.navigation.SmoothGroundNavigation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Antelope extends Animal implements AnimatedEntity {
    public static final Identifier ID = Util.id("antelope");
    public static final Model MODEL = Util.loadBbModel(ID);
    private final EntityHolder<Antelope> holder;
    private int grazeTicks = 0;

    @NotNull public static AttributeSupplier.Builder createAttributes() { return Animal.createMobAttributes().add(Attributes.MAX_HEALTH, 15.0).add(Attributes.MOVEMENT_SPEED, 0.3); }
    @Override public EntityHolder<Antelope> getHolder() { return this.holder; }
    public Antelope(EntityType<? extends @NotNull Animal> type, Level level) { super(type, level); this.holder = new LivingEntityHolder<>(this, MODEL); EntityAttachment.ofTicking(this.holder, this); }
    @Override protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.8));
        this.goalSelector.addGoal(3, new BreedGoal(this, 0.8));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }
    @Override public void tick() {
        super.tick();
        if (this.tickCount % 2 == 0) {
            // Randomly graze when idle on ground
            if (grazeTicks > 0) {
                grazeTicks -= 2;
                this.holder.getAnimator().playAnimation("graze");
                this.holder.getAnimator().pauseAnimation("walk");
                this.holder.getAnimator().pauseAnimation("idle");
            } else {
                this.holder.getAnimator().pauseAnimation("graze");
                AnimationHelper.updateWalkAnimation(this, this.holder);
                // Start grazing occasionally when standing still
                if (this.onGround() && this.getDeltaMovement().lengthSqr() < 0.001 && this.random.nextInt(200) == 0) {
                    grazeTicks = 60 + this.random.nextInt(60);
                }
            }
            AnimationHelper.updateHurtColor(this, this.holder);
        }
    }
    @Override public boolean isFood(ItemStack i) { return i.is(Items.WHEAT); }
    @Nullable @Override public AgeableMob getBreedOffspring(@NotNull ServerLevel l, @NotNull AgeableMob m) { return new AntelopeBaby(MobRegistry.ANTELOPE_BABY, l); }
    @Override protected void dropCustomDeathLoot(@NotNull ServerLevel l, @NotNull DamageSource s, boolean r) { super.dropCustomDeathLoot(l, s, r); this.spawnAtLocation(l, new ItemStack(Items.LEATHER, 1 + this.random.nextInt(2))); }
    @Override protected SoundEvent getHurtSound(@NotNull DamageSource d) { return SoundEvents.GOAT_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.GOAT_DEATH; }
    @Override protected @NotNull PathNavigation createNavigation(@NotNull Level l) { return new SmoothGroundNavigation(this, l); }
}
