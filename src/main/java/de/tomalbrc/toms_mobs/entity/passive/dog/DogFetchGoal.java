package de.tomalbrc.toms_mobs.entity.passive.dog;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * Tamed dogs scan for dropped items near their owner, pick them up (carry internally),
 * and deliver them to the owner's feet. Like an allay.
 */
public class DogFetchGoal extends Goal {
    private final TamableAnimal dog;
    private ItemEntity targetItem;
    private boolean carrying = false;
    private int cooldown = 0;
    private int stateTicks = 0;
    private static final int SCAN_RADIUS = 10;
    private static final double PICKUP_DIST = 1.5;
    private static final double DELIVER_DIST = 2.0;
    private static final int MAX_STATE_TICKS = 400; // give up after 20 sec

    public DogFetchGoal(TamableAnimal dog) {
        this.dog = dog;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) { cooldown--; return false; }
        if (!dog.isTame() || dog.isOrderedToSit()) return false;
        LivingEntity owner = dog.getOwner();
        if (!(owner instanceof Player) || !owner.isAlive()) return false;
        // Only fetch if we're near the owner (they're calling for follow)
        if (dog.distanceToSqr(owner) > 400) return false;

        AABB area = new AABB(owner.getX() - SCAN_RADIUS, owner.getY() - 4, owner.getZ() - SCAN_RADIUS,
                owner.getX() + SCAN_RADIUS, owner.getY() + 4, owner.getZ() + SCAN_RADIUS);
        List<ItemEntity> items = dog.level().getEntitiesOfClass(ItemEntity.class, area,
                e -> !e.hasPickUpDelay() && e.isAlive() && !e.getItem().isEmpty());
        if (items.isEmpty()) return false;

        // Pick the closest item to the dog
        ItemEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (ItemEntity item : items) {
            double dist = dog.distanceToSqr(item);
            if (dist < closestDist) {
                closestDist = dist;
                closest = item;
            }
        }
        targetItem = closest;
        return targetItem != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (stateTicks > MAX_STATE_TICKS) return false;
        if (!(dog.getOwner() instanceof Player owner) || !owner.isAlive()) return false;
        if (dog.isOrderedToSit()) return false;

        if (carrying) {
            return true; // keep delivering
        }
        // Not carrying yet, make sure item is still valid
        return targetItem != null && targetItem.isAlive() && !targetItem.getItem().isEmpty();
    }

    @Override
    public void start() {
        stateTicks = 0;
        carrying = false;
    }

    @Override
    public void stop() {
        targetItem = null;
        carrying = false;
        stateTicks = 0;
        cooldown = 60; // 3 sec between fetches
        dog.getNavigation().stop();
    }

    @Override
    public void tick() {
        stateTicks++;
        LivingEntity owner = dog.getOwner();
        if (owner == null) return;

        if (!carrying) {
            // Going to item
            if (targetItem == null || !targetItem.isAlive()) return;
            dog.getLookControl().setLookAt(targetItem, 30.0F, 30.0F);
            double dist = dog.distanceToSqr(targetItem);
            if (dist > PICKUP_DIST * PICKUP_DIST) {
                if (dog.getNavigation().isDone()) {
                    dog.getNavigation().moveTo(targetItem, 1.2);
                }
            } else {
                // Pick up: just discard the item entity (dog "carries" it invisibly)
                targetItem.discard();
                // Spawn a new item entity at owner's position after delivery — carry state
                carrying = true;
                dog.getNavigation().stop();
                // Store target item's pickup position for reference
                dog.playSound(net.minecraft.sounds.SoundEvents.FOX_EAT, 0.6F, 1.4F);
            }
        } else {
            // Delivering to owner
            dog.getLookControl().setLookAt(owner, 30.0F, 30.0F);
            double dist = dog.distanceToSqr(owner);
            if (dist > DELIVER_DIST * DELIVER_DIST) {
                if (dog.getNavigation().isDone()) {
                    dog.getNavigation().moveTo(owner, 1.3);
                }
            } else {
                // Drop item at owner's feet
                if (targetItem != null && !targetItem.getItem().isEmpty()) {
                    ItemEntity dropped = new ItemEntity(dog.level(),
                            owner.getX(), owner.getY() + 0.5, owner.getZ(),
                            targetItem.getItem().copy());
                    dropped.setNoPickUpDelay();
                    dog.level().addFreshEntity(dropped);
                    dog.playSound(net.minecraft.sounds.SoundEvents.FOX_AMBIENT, 0.8F, 1.4F);
                }
                targetItem = null;
                carrying = false;
                stateTicks = MAX_STATE_TICKS; // force stop
            }
        }
    }
}
