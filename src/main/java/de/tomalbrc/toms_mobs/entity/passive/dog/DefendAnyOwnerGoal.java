package de.tomalbrc.toms_mobs.entity.passive.dog;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * Defends ALL owners (primary + co-owners) when they take damage.
 * Replaces vanilla OwnerHurtByTargetGoal which only checks the primary.
 */
public class DefendAnyOwnerGoal extends TargetGoal {
    private final AbstractDog dog;
    private LivingEntity attacker;
    private int timestamp;

    public DefendAnyOwnerGoal(AbstractDog dog) {
        super(dog, false);
        this.dog = dog;
    }

    @Override
    public boolean canUse() {
        if (!dog.isTame()) return false;

        LivingEntity found = checkOwner(dog.getOwner());
        if (found != null) { attacker = found; return true; }

        if (dog.level() instanceof ServerLevel sl) {
            for (UUID id : dog.getCoOwners()) {
                Player p = sl.getServer().getPlayerList().getPlayer(id);
                if (p == null || !p.isAlive()) continue;
                if (dog.distanceToSqr(p) > 1024) continue; // 32 block radius
                found = checkOwner(p);
                if (found != null) { attacker = found; return true; }
            }
        }
        return false;
    }

    private LivingEntity checkOwner(LivingEntity owner) {
        if (owner == null) return null;
        LivingEntity hurt = owner.getLastHurtByMob();
        if (hurt == null) return null;
        int ts = owner.getLastHurtByMobTimestamp();
        if (ts == timestamp) return null;
        if (!canAttack(hurt, TargetingConditions.DEFAULT)) return null;
        timestamp = ts;
        return hurt;
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse();
    }

    @Override
    public void start() {
        mob.setTarget(attacker);
        super.start();
    }
}
