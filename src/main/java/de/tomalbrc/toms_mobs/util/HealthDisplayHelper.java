package de.tomalbrc.toms_mobs.util;

import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.WeakHashMap;

public class HealthDisplayHelper {
    private static final double LOOK_RANGE = 8.0;
    private static final double NAME_RANGE = 6.0;
    private static final int DISPLAY_DURATION = 40;
    private static final int CHECK_INTERVAL = 4;
    private static final Style FULL_HEART = Style.EMPTY.withColor(0xFF1414);
    private static final Style EMPTY_HEART = Style.EMPTY.withColor(0x555555);

    private static final WeakHashMap<LivingEntity, HealthDisplayHelper> INSTANCES = new WeakHashMap<>();

    private final TextDisplayElement display;
    private int displayTicks = 0;
    private boolean attached = false;
    private boolean visible = false;
    private float lastHealth = -1;
    private float lastMaxHealth = -1;
    private Component defaultName = null;

    private HealthDisplayHelper() {
        this.display = new TextDisplayElement();
        this.display.setBillboardMode(Display.BillboardConstraints.CENTER);
        this.display.setScale(new Vector3f(0.5f, 0.5f, 0.5f));
        this.display.setBackground(0x60000000);
        this.display.setShadow(true);
        this.display.setViewRange(0.0f);
        this.display.setTeleportDuration(3);
    }

    public static void update(LivingEntity entity, EntityHolder<?> holder) {
        if (entity.tickCount % CHECK_INTERVAL != 0) return;

        HealthDisplayHelper instance = INSTANCES.computeIfAbsent(entity, e -> new HealthDisplayHelper());
        instance.tick(entity, holder);
    }

    private void tick(LivingEntity entity, EntityHolder<?> holder) {
        if (!attached) {
            // Set default custom name so Jade and vanilla nametag show the mob type
            defaultName = entity.getType().getDescription();
            if (entity.getCustomName() == null) {
                entity.setCustomName(defaultName);
            }
            double heartHeight = Math.max(1.7, entity.getBbHeight() * 2.0 + 0.3);
            this.display.setOffset(new Vec3(0, heartHeight, 0));
            holder.addAdditionalDisplay(this.display);
            attached = true;
        }

        // Show/hide nametag based on proximity
        boolean anyPlayerNearby = isAnyPlayerWithinRange(entity, NAME_RANGE);
        entity.setCustomNameVisible(anyPlayerNearby);

        boolean beingLookedAt = isAnyPlayerLookingAt(entity);

        if (beingLookedAt) {
            displayTicks = DISPLAY_DURATION;
            if (!visible) {
                this.display.setViewRange(1.0f);
                visible = true;
            }
            updateText(entity);
        } else if (displayTicks > 0) {
            displayTicks -= CHECK_INTERVAL;
            if (displayTicks <= 0 && visible) {
                this.display.setViewRange(0.0f);
                visible = false;
                lastHealth = -1;
                lastMaxHealth = -1;
            }
        }
    }

    private void updateText(LivingEntity entity) {
        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        if (health == lastHealth && maxHealth == lastMaxHealth) return;

        lastHealth = health;
        lastMaxHealth = maxHealth;

        int totalHearts = Math.max(1, (int) Math.ceil(maxHealth / 2.0));
        int fullHearts = (int) Math.ceil(health / 2.0);

        MutableComponent text = Component.literal("");
        for (int i = 0; i < totalHearts; i++) {
            if (i < fullHearts) {
                text.append(Component.literal("\u2764").withStyle(FULL_HEART));
            } else {
                text.append(Component.literal("\u2764").withStyle(EMPTY_HEART));
            }
        }

        this.display.setText(text);
    }

    private static boolean isAnyPlayerWithinRange(LivingEntity entity, double range) {
        return !entity.level().getEntitiesOfClass(Player.class, entity.getBoundingBox().inflate(range)).isEmpty();
    }

    private static boolean isAnyPlayerLookingAt(LivingEntity entity) {
        List<Player> players = entity.level().getEntitiesOfClass(
                Player.class,
                entity.getBoundingBox().inflate(LOOK_RANGE)
        );

        for (Player player : players) {
            if (isPlayerLookingAt(player, entity)) return true;
        }
        return false;
    }

    private static boolean isPlayerLookingAt(Player player, LivingEntity target) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F).normalize();
        Vec3 endPos = eyePos.add(lookVec.scale(LOOK_RANGE));

        AABB box = target.getBoundingBox().inflate(0.2);
        return box.clip(eyePos, endPos).isPresent();
    }
}
