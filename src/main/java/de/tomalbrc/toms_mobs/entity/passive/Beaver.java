package de.tomalbrc.toms_mobs.entity.passive;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.entity.control.SemiAquaticMoveControl;
import de.tomalbrc.toms_mobs.entity.goal.aquatic.AquaticWaterAvoidingRandomStrollGoal;
import de.tomalbrc.toms_mobs.entity.goal.aquatic.PathfinderMobSwimGoal;
import de.tomalbrc.toms_mobs.util.AnimationHelper;
import de.tomalbrc.toms_mobs.util.Util;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import aqario.fowlplay.common.entity.ai.navigation.AmphibiousNavigation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class Beaver extends Animal implements AnimatedEntity, de.tomalbrc.toms_mobs.util.HealthDisplayOverride {
    @Override public double getHealthDisplayYOffset() { return 1.0; }

    public static final Identifier ID = Util.id("beaver");
    public static final Model MODEL = Util.loadBbModel(ID);
    private final EntityHolder<Beaver> holder;
    private int chewAnimTicks = 0;
    // Dam-building state
    private boolean hasLogToPlace = false;
    private int placeDelayTicks = 0;
    // Absolute game-time tick when the next chew is allowed. 0 = can chew now.
    private long nextChewAllowedAt = 0L;

    public boolean hasLogToPlace() { return hasLogToPlace; }
    public void setHasLogToPlace(boolean v) { this.hasLogToPlace = v; }
    public int getPlaceDelayTicks() { return placeDelayTicks; }
    public void decrementPlaceDelay() { if (placeDelayTicks > 0) placeDelayTicks--; }
    public void setPlaceDelayTicks(int v) { this.placeDelayTicks = v; }
    public long getNextChewAllowedAt() { return nextChewAllowedAt; }
    public void setNextChewAllowedAt(long v) { this.nextChewAllowedAt = v; }

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    @Override public EntityHolder<Beaver> getHolder() { return this.holder; }

    public Beaver(EntityType<? extends @NotNull Animal> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 0.F);
        this.moveControl = new SemiAquaticMoveControl(this);
        this.holder = new LivingEntityHolder<>(this, MODEL);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(2, new PathfinderMobSwimGoal(this, 1));
        this.goalSelector.addGoal(3, new ChewBirchGoal(this));
        this.goalSelector.addGoal(4, new BreedGoal(this, 0.7));
        this.goalSelector.addGoal(5, new PlaceLogInWaterGoal(this));
        this.goalSelector.addGoal(6, new AquaticWaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
    }

    public void playChewAnim() { chewAnimTicks = 30; }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) decrementPlaceDelay();
        if (this.tickCount % 2 == 0) {
            if (chewAnimTicks > 0) {
                chewAnimTicks -= 2;
                this.holder.getAnimator().playAnimation("chew");
                this.holder.getAnimator().pauseAnimation("walk");
                this.holder.getAnimator().pauseAnimation("idle");
            } else {
                this.holder.getAnimator().pauseAnimation("chew");
                AnimationHelper.updateWalkAnimation(this, this.holder);
            }
            AnimationHelper.updateHurtColor(this, this.holder);
        }
    }

    @Override
    public float getWalkTargetValue(@NotNull BlockPos pos, @NotNull LevelReader level) {
        return level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER) ? 1.0F : level.getPathfindingCostFromLightLevels(pos);
    }

    @Override public boolean isFood(ItemStack i) { return i.is(Items.APPLE) || i.is(Items.SWEET_BERRIES); }
    @Nullable @Override public AgeableMob getBreedOffspring(@NotNull ServerLevel l, @NotNull AgeableMob m) { return null; }
    @Override protected SoundEvent getHurtSound(@NotNull DamageSource d) { return SoundEvents.FOX_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.FOX_DEATH; }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel l, @NotNull DamageSource s, boolean r) {
        super.dropCustomDeathLoot(l, s, r);
        if (this.random.nextBoolean()) this.spawnAtLocation(l, new ItemStack(Items.STICK, 1 + this.random.nextInt(2)));
    }

    @Override public boolean isPushedByFluid() { return false; }

    @Override
    protected void addAdditionalSaveData(@NotNull net.minecraft.world.level.storage.ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("HasLogToPlace", hasLogToPlace);
        output.putInt("PlaceDelayTicks", placeDelayTicks);
        output.putLong("NextChewAllowedAt", nextChewAllowedAt);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull net.minecraft.world.level.storage.ValueInput input) {
        super.readAdditionalSaveData(input);
        hasLogToPlace = input.getBooleanOr("HasLogToPlace", false);
        placeDelayTicks = input.getIntOr("PlaceDelayTicks", 0);
        nextChewAllowedAt = input.getLongOr("NextChewAllowedAt", 0L);
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) { return new AmphibiousNavigation(this, level); }

    /**
     * Beavers hunt for birch logs (NOT stripped, NOT planks) within a small radius.
     * When they reach one, they chew it over a few seconds and break it,
     * dropping the log as a normal item.
     * Long cooldown between chews so they don't clear forests.
     */
    private static class ChewBirchGoal extends Goal {
        private final Beaver beaver;
        private BlockPos targetPos;
        private int stateTicks = 0;
        private boolean chewing = false;
        private static final int SCAN_RADIUS = 10;
        private static final int CHEW_DURATION_TICKS = 60;
        private static final int COOLDOWN_TICKS = 6000; // 5 minutes

        ChewBirchGoal(Beaver beaver) {
            this.beaver = beaver;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (beaver.level().getGameTime() < beaver.getNextChewAllowedAt()) return false;
            if (beaver.isInWater()) return false;
            BlockPos origin = beaver.blockPosition();
            // Search nearby for a birch log
            for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    for (int dy = -3; dy <= 6; dy++) {
                        BlockPos check = origin.offset(dx, dy, dz);
                        if (beaver.level().getBlockState(check).is(Blocks.BIRCH_LOG)) {
                            targetPos = check;
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (stateTicks > 400) return false;
            if (targetPos == null) return false;
            return beaver.level().getBlockState(targetPos).is(Blocks.BIRCH_LOG);
        }

        @Override
        public void start() { stateTicks = 0; chewing = false; }

        @Override
        public void stop() {
            targetPos = null;
            chewing = false;
            stateTicks = 0;
            beaver.setNextChewAllowedAt(beaver.level().getGameTime() + COOLDOWN_TICKS);
            beaver.getNavigation().stop();
        }

        /**
         * Flood-fill from the chewed log to find the whole birch tree (connected logs +
         * their adjacent leaves, capped). Each block becomes a FallingBlockEntity with a
         * small horizontal kick so it tumbles. A birch sapling is planted at the stump
         * position if the ground below supports one.
         */
        private static void fellTree(ServerLevel sl, BlockPos start, Beaver beaver) {
            java.util.Set<BlockPos> logs = new java.util.HashSet<>();
            java.util.ArrayDeque<BlockPos> queue = new java.util.ArrayDeque<>();
            queue.add(start);
            final int cap = 30;
            while (!queue.isEmpty() && logs.size() < cap) {
                BlockPos p = queue.poll();
                if (logs.contains(p)) continue;
                if (!sl.getBlockState(p).is(Blocks.BIRCH_LOG)) continue;
                logs.add(p);
                for (int ox = -1; ox <= 1; ox++)
                    for (int oy = -1; oy <= 1; oy++)
                        for (int oz = -1; oz <= 1; oz++)
                            if (ox != 0 || oy != 0 || oz != 0) queue.add(p.offset(ox, oy, oz));
            }
            java.util.Set<BlockPos> tree = new java.util.HashSet<>(logs);
            for (BlockPos log : logs) {
                if (tree.size() >= cap) break;
                for (int ox = -1; ox <= 1; ox++)
                    for (int oy = -1; oy <= 1; oy++)
                        for (int oz = -1; oz <= 1; oz++) {
                            if (tree.size() >= cap) break;
                            if (ox == 0 && oy == 0 && oz == 0) continue;
                            BlockPos n = log.offset(ox, oy, oz);
                            if (sl.getBlockState(n).is(Blocks.BIRCH_LEAVES)) tree.add(n);
                        }
            }

            BlockPos stump = null;
            for (BlockPos p : logs) if (stump == null || p.getY() < stump.getY()) stump = p;

            // Capture every block state first, then wipe them all before spawning falling
            // entities. Otherwise each falling block lands on the next log below and settles
            // as a block, making the tree look unchanged.
            java.util.Map<BlockPos, net.minecraft.world.level.block.state.BlockState> states = new java.util.HashMap<>();
            for (BlockPos p : tree) states.put(p, sl.getBlockState(p));
            for (BlockPos p : tree) sl.setBlock(p, Blocks.AIR.defaultBlockState(), 3);

            for (var entry : states.entrySet()) {
                BlockPos p = entry.getKey();
                net.minecraft.world.entity.item.FallingBlockEntity fbe =
                        net.minecraft.world.entity.item.FallingBlockEntity.fall(sl, p, entry.getValue());
                fbe.setDeltaMovement(
                        (beaver.getRandom().nextDouble() - 0.5) * 0.35,
                        0.2,
                        (beaver.getRandom().nextDouble() - 0.5) * 0.35);
            }

            if (stump != null) {
                net.minecraft.world.level.block.state.BlockState sapling =
                        Blocks.BIRCH_SAPLING.defaultBlockState();
                if (sl.getBlockState(stump).isAir() && sapling.canSurvive(sl, stump)) {
                    sl.setBlock(stump, sapling, 3);
                }
            }
        }

        @Override
        public void tick() {
            stateTicks++;
            if (targetPos == null) return;
            double dx = targetPos.getX() + 0.5 - beaver.getX();
            double dy = targetPos.getY() + 0.5 - beaver.getY();
            double dz = targetPos.getZ() + 0.5 - beaver.getZ();
            double distSqr = dx * dx + dy * dy + dz * dz;

            beaver.getLookControl().setLookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, 30, 30);

            if (distSqr > 2.5 * 2.5) {
                if (beaver.getNavigation().isDone()) {
                    beaver.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 1.0);
                }
                chewing = false;
            } else {
                // Within chew range
                beaver.getNavigation().stop();
                if (!chewing) { chewing = true; stateTicks = 0; }
                if (stateTicks % 15 == 0) beaver.playChewAnim();
                if (stateTicks >= CHEW_DURATION_TICKS && beaver.level() instanceof ServerLevel sl) {
                    fellTree(sl, targetPos, beaver);
                    beaver.playSound(SoundEvents.WOOD_BREAK, 1.0F, 0.9F);
                    // Remember a log to place in shallow water later
                    beaver.setHasLogToPlace(true);
                    beaver.setPlaceDelayTicks(2400 + beaver.getRandom().nextInt(3600)); // 2-5 min
                    stateTicks = 500; // force stop
                }
            }
        }
    }

    /**
     * Beavers that recently chewed a birch log will, after a delay, look for a shallow
     * water block nearby and place a birch log in it. Crude dam-building.
     */
    private static class PlaceLogInWaterGoal extends Goal {
        private final Beaver beaver;
        private BlockPos targetWater;
        private int stateTicks = 0;
        private static final int SCAN_RADIUS = 8;
        private static final int GIVE_UP_TICKS = 400;

        PlaceLogInWaterGoal(Beaver beaver) {
            this.beaver = beaver;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!beaver.hasLogToPlace() || beaver.getPlaceDelayTicks() > 0) return false;
            BlockPos origin = beaver.blockPosition();
            for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    for (int dy = -2; dy <= 1; dy++) {
                        BlockPos p = origin.offset(dx, dy, dz);
                        if (isShallowWaterSpot(p)) { targetWater = p; return true; }
                    }
                }
            }
            // Nothing found this cycle; wait a bit before trying again
            beaver.setPlaceDelayTicks(600);
            return false;
        }

        private boolean isShallowWaterSpot(BlockPos p) {
            var level = beaver.level();
            // Must be a water block with air above and solid block below
            if (!level.getFluidState(p).is(net.minecraft.tags.FluidTags.WATER)) return false;
            if (!level.getBlockState(p.above()).isAir()) return false;
            if (!level.getBlockState(p.below()).isSolid()) return false;
            // Skip if a log is already adjacent (no stacking)
            for (int ox = -1; ox <= 1; ox++) for (int oz = -1; oz <= 1; oz++) {
                if (ox == 0 && oz == 0) continue;
                if (level.getBlockState(p.offset(ox, 0, oz)).is(Blocks.BIRCH_LOG)) return false;
            }
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (stateTicks > GIVE_UP_TICKS) return false;
            if (targetWater == null) return false;
            if (!beaver.hasLogToPlace()) return false;
            // Target still shallow water?
            return beaver.level().getFluidState(targetWater).is(net.minecraft.tags.FluidTags.WATER)
                    && beaver.level().getBlockState(targetWater.above()).isAir();
        }

        @Override
        public void start() { stateTicks = 0; }

        @Override
        public void stop() {
            targetWater = null;
            stateTicks = 0;
            beaver.getNavigation().stop();
        }

        @Override
        public void tick() {
            stateTicks++;
            if (targetWater == null) return;
            double dx = targetWater.getX() + 0.5 - beaver.getX();
            double dz = targetWater.getZ() + 0.5 - beaver.getZ();
            double distSqr = dx * dx + dz * dz;

            beaver.getLookControl().setLookAt(targetWater.getX() + 0.5, targetWater.getY() + 0.5, targetWater.getZ() + 0.5, 30, 30);

            if (distSqr > 3.0 * 3.0) {
                if (beaver.getNavigation().isDone()) {
                    beaver.getNavigation().moveTo(targetWater.getX() + 0.5, targetWater.getY(), targetWater.getZ() + 0.5, 1.0);
                }
            } else {
                // Close enough - place the log
                if (beaver.level() instanceof ServerLevel sl) {
                    sl.setBlock(targetWater, Blocks.BIRCH_LOG.defaultBlockState(), 3);
                    beaver.playSound(SoundEvents.WOOD_PLACE, 0.8F, 0.9F);
                    beaver.playChewAnim();
                    beaver.setHasLogToPlace(false);
                    // Short cooldown so it doesn't instantly find the same water column
                    beaver.setPlaceDelayTicks(200);
                }
                stateTicks = GIVE_UP_TICKS + 1;
            }
        }
    }
}
