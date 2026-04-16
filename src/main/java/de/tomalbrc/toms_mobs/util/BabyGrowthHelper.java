package de.tomalbrc.toms_mobs.util;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BabyGrowthHelper {
    public static final int GROW_UP_TICKS = 72000; // 60 minutes

    public static void saveAge(ValueOutput output, int age, boolean frozen) {
        output.putInt("BabyAge", age);
        output.putBoolean("BabyAgeFrozen", frozen);
    }

    public static int loadAge(ValueInput input) {
        return input.getIntOr("BabyAge", 0);
    }

    public static boolean loadFrozen(ValueInput input) {
        return input.getBooleanOr("BabyAgeFrozen", false);
    }

    public static boolean tryFreezeGrowth(Animal baby, Player player, InteractionHand hand, boolean alreadyFrozen) {
        if (alreadyFrozen) return false;
        if (!player.getItemInHand(hand).is(Items.GOLDEN_DANDELION)) return false;

        if (!player.getAbilities().instabuild) player.getItemInHand(hand).shrink(1);
        baby.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 1.5F);
        if (baby.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    baby.getX(), baby.getY() + baby.getBbHeight() * 0.5, baby.getZ(),
                    10, 0.4, 0.4, 0.4, 0.0);
        }
        return true;
    }

    public static boolean tryGrowUp(Animal baby, EntityType<? extends Animal> adultType, int age, boolean frozen) {
        if (frozen || age < GROW_UP_TICKS) return false;
        if (!(baby.level() instanceof ServerLevel serverLevel)) return false;

        Animal adult = adultType.create(serverLevel, null);
        if (adult == null) return false;

        adult.snapTo(baby.getX(), baby.getY(), baby.getZ(), baby.getYRot(), baby.getXRot());
        adult.setYHeadRot(baby.getYHeadRot());
        if (baby.getCustomName() != null) {
            adult.setCustomName(baby.getCustomName());
            adult.setCustomNameVisible(baby.isCustomNameVisible());
        }
        serverLevel.addFreshEntity(adult);
        baby.discard();
        return true;
    }
}
