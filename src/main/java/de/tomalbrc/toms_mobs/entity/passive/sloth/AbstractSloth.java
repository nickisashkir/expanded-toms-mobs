package de.tomalbrc.toms_mobs.entity.passive.sloth;
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
public abstract class AbstractSloth extends Animal implements AnimatedEntity {
    private final EntityHolder<? extends AbstractSloth> holder;
    private int sleepTicks = 0;
    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.1);
    }
    @Override public EntityHolder<? extends AbstractSloth> getHolder() { return this.holder; }
    protected AbstractSloth(EntityType<? extends Animal> type, Level level, Model model) {
        super(type, level);
        this.holder = new LivingEntityHolder<>(this, model);
        EntityAttachment.ofTicking(this.holder, this);
    }
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.3));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
    }
    @Override
    public void tick() {
        super.tick();
        if (this.tickCount % 2 == 0) {
            if (sleepTicks > 0) { sleepTicks -= 2; this.holder.getAnimator().playAnimation("sleep"); this.holder.getAnimator().pauseAnimation("walk"); this.holder.getAnimator().pauseAnimation("idle"); }
            else { this.holder.getAnimator().pauseAnimation("sleep"); AnimationHelper.updateWalkAnimation(this, this.holder); }
            AnimationHelper.updateHurtColor(this, this.holder);
        }
    }
    @Override public @NotNull InteractionResult mobInteract(@NotNull Player p, @NotNull InteractionHand h) { InteractionResult r = super.mobInteract(p, h); if (r.consumesAction()) return r; sleepTicks = 100; return InteractionResult.SUCCESS; }
    @Override public boolean isFood(ItemStack i) { return i.is(Items.MELON_SLICE); }
    @Nullable @Override public AgeableMob getBreedOffspring(@NotNull ServerLevel l, @NotNull AgeableMob m) { return null; }
    @Override protected void dropCustomDeathLoot(@NotNull ServerLevel l, @NotNull DamageSource s, boolean r) { super.dropCustomDeathLoot(l, s, r); this.spawnAtLocation(l, new ItemStack(Items.VINE, 1 + this.random.nextInt(2))); }
    @Override protected SoundEvent getHurtSound(@NotNull DamageSource d) { return SoundEvents.PANDA_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PANDA_DEATH; }
    @Override protected @NotNull PathNavigation createNavigation(@NotNull Level l) { return new SmoothGroundNavigation(this, l); }
}
