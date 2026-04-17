package de.tomalbrc.toms_mobs.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.tomalbrc.toms_mobs.config.ModConfig;
import de.tomalbrc.toms_mobs.entity.passive.npc.AbstractNpc;
import de.tomalbrc.toms_mobs.registry.MobRegistry;
import de.tomalbrc.toms_mobs.util.CustomSpawnTicker;
import de.tomalbrc.toms_mobs.util.PatrolRoute;
import de.tomalbrc.toms_mobs.util.SpawnEntry;
import de.tomalbrc.toms_mobs.util.SpawnOptOut;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;

public class TomsMobsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        Permission.HasCommandLevel opLevel = new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS);
        dispatcher.register(Commands.literal("tomsmobs")
                .requires(source -> source.permissions().hasPermission(opLevel))

                .then(Commands.literal("status")
                        .executes(TomsMobsCommand::showStatus))

                .then(Commands.literal("ticker")
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(ctx -> setTicker(ctx.getSource(), BoolArgumentType.getBool(ctx, "enabled")))))

                .then(Commands.literal("interval")
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(20, 72000))
                                .executes(ctx -> setInterval(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "ticks")))))

                .then(Commands.literal("softcap")
                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 100))
                                .executes(ctx -> setSoftCap(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value")))))

                .then(Commands.literal("reload")
                        .executes(TomsMobsCommand::reloadConfig))

                .then(Commands.literal("count")
                        .executes(ctx -> countNearby(ctx.getSource(), 128))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(8, 2000))
                                .executes(ctx -> countNearby(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "radius")))))

                .then(Commands.literal("total")
                        .executes(TomsMobsCommand::countTotal))

                .then(Commands.literal("forcespawn")
                        .executes(ctx -> forceSpawn(ctx.getSource(), 5))
                        .then(Commands.argument("attempts", IntegerArgumentType.integer(1, 50))
                                .executes(ctx -> forceSpawn(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "attempts")))))

                .then(Commands.literal("forceocean")
                        .executes(ctx -> forceOcean(ctx.getSource())))

                .then(Commands.literal("patrol")
                        .then(Commands.literal("create")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(ctx -> patrolCreate(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(ctx -> patrolAdd(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
                        .then(Commands.literal("assign")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(ctx -> patrolAssign(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
                        .then(Commands.literal("unassign")
                                .executes(ctx -> patrolUnassign(ctx.getSource())))
                        .then(Commands.literal("list")
                                .executes(ctx -> patrolList(ctx.getSource())))
                        .then(Commands.literal("delete")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(ctx -> patrolDelete(ctx.getSource(), StringArgumentType.getString(ctx, "name"))))))
        );

        // Player-facing opt-out command (no OP required)
        dispatcher.register(Commands.literal("toggleanimals")
                .executes(TomsMobsCommand::toggleOptOut));

        // Player-facing shared ownership commands
        dispatcher.register(Commands.literal("petshare")
                .then(Commands.literal("add")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .executes(ctx -> petShareAdd(ctx.getSource(), StringArgumentType.getString(ctx, "player")))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .executes(ctx -> petShareRemove(ctx.getSource(), StringArgumentType.getString(ctx, "player")))))
                .then(Commands.literal("list")
                        .executes(TomsMobsCommand::petShareList)));

        dispatcher.register(Commands.literal("pettransfer")
                .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> petTransfer(ctx.getSource(), StringArgumentType.getString(ctx, "player")))));
    }

    private static void send(CommandSourceStack source, Component message) {
        source.sendSystemMessage(message);
    }

    private static int toggleOptOut(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }
        boolean nowOptedOut = SpawnOptOut.toggle(player.getUUID());
        if (nowOptedOut) {
            send(source, Component.literal("Tom's Mobs custom spawning disabled for you. Vanilla biome spawns still work.").withStyle(ChatFormatting.YELLOW));
        } else {
            send(source, Component.literal("Tom's Mobs custom spawning re-enabled for you.").withStyle(ChatFormatting.GREEN));
        }
        return 1;
    }

    private static int showStatus(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ModConfig config = ModConfig.getInstance();
        send(source, Component.literal("=== Tom's Mobs Spawn Ticker ===").withStyle(ChatFormatting.GOLD));
        send(source, Component.literal("Ticker enabled: " + config.customSpawnTicker));
        send(source, Component.literal("Interval: " + config.customSpawnTickerIntervalTicks + " ticks (" + (config.customSpawnTickerIntervalTicks / 20.0) + "s)"));
        send(source, Component.literal("Default soft cap per type: " + config.customSpawnTickerSoftCapPerType));
        return 1;
    }

    private static int setTicker(CommandSourceStack source, boolean enabled) {
        ModConfig.getInstance().customSpawnTicker = enabled;
        ModConfig.save();
        send(source, Component.literal("Tom's Mobs spawn ticker " + (enabled ? "enabled" : "disabled")).withStyle(ChatFormatting.GREEN));
        return 1;
    }

    private static int setInterval(CommandSourceStack source, int ticks) {
        ModConfig.getInstance().customSpawnTickerIntervalTicks = ticks;
        ModConfig.save();
        send(source, Component.literal("Tom's Mobs spawn interval set to " + ticks + " ticks (" + (ticks / 20.0) + "s)").withStyle(ChatFormatting.GREEN));
        return 1;
    }

    private static int setSoftCap(CommandSourceStack source, int value) {
        ModConfig.getInstance().customSpawnTickerSoftCapPerType = value;
        ModConfig.save();
        send(source, Component.literal("Tom's Mobs default soft cap set to " + value).withStyle(ChatFormatting.GREEN));
        return 1;
    }

    private static int reloadConfig(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        ModConfig.load();
        send(ctx.getSource(), Component.literal("Tom's Mobs config reloaded from disk").withStyle(ChatFormatting.GREEN));
        return 1;
    }

    private static int countNearby(CommandSourceStack source, int radius) {
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("Must be used in a world"));
            return 0;
        }

        AABB area = new AABB(source.getPosition(), source.getPosition()).inflate(radius);
        Map<EntityType<?>, Integer> counts = new HashMap<>();
        int total = 0;
        int named = 0;

        for (Entity e : level.getEntities((Entity) null, area, en -> isTomsMob(en.getType()))) {
            total++;
            if (e.getCustomName() != null && !e.getCustomName().equals(e.getType().getDescription())) {
                named++;
            }
            counts.merge(e.getType(), 1, Integer::sum);
        }

        send(source, Component.literal("=== Tom's Mobs within " + radius + " blocks ===").withStyle(ChatFormatting.GOLD));
        send(source, Component.literal("Total: " + total + " (" + named + " named, " + (total - named) + " wild)"));

        if (counts.isEmpty()) {
            send(source, Component.literal("No Tom's Mobs nearby.").withStyle(ChatFormatting.GRAY));
            return 0;
        }

        counts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .forEach(entry -> send(source, Component.literal("  " + entry.getValue() + "× ").withStyle(ChatFormatting.YELLOW)
                        .append(entry.getKey().getDescription().copy().withStyle(ChatFormatting.WHITE))));
        return total;
    }

    private static int countTotal(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        int total = 0;
        int named = 0;
        Map<EntityType<?>, Integer> counts = new HashMap<>();

        for (ServerLevel level : source.getServer().getAllLevels()) {
            for (Entity e : level.getAllEntities()) {
                if (!isTomsMob(e.getType())) continue;
                total++;
                if (e.getCustomName() != null && !e.getCustomName().equals(e.getType().getDescription())) {
                    named++;
                }
                counts.merge(e.getType(), 1, Integer::sum);
            }
        }

        send(source, Component.literal("=== Tom's Mobs server total ===").withStyle(ChatFormatting.GOLD));
        send(source, Component.literal("Total loaded: " + total + " (" + named + " named, " + (total - named) + " wild)"));

        if (counts.isEmpty()) {
            send(source, Component.literal("No Tom's Mobs loaded.").withStyle(ChatFormatting.GRAY));
            return 0;
        }

        counts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(20)
                .forEach(entry -> send(source, Component.literal("  " + entry.getValue() + "× ").withStyle(ChatFormatting.YELLOW)
                        .append(entry.getKey().getDescription().copy().withStyle(ChatFormatting.WHITE))));

        if (counts.size() > 20) {
            send(source, Component.literal("  ... and " + (counts.size() - 20) + " more types").withStyle(ChatFormatting.GRAY));
        }
        return total;
    }

    private static int forceSpawn(CommandSourceStack source, int attempts) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("Must be used in a world"));
            return 0;
        }

        int spawned = CustomSpawnTicker.forceSpawn(level, player, attempts);
        if (spawned > 0) {
            send(source, Component.literal("Force-spawned " + spawned + " Tom's mob(s) near you").withStyle(ChatFormatting.GREEN));
        } else {
            send(source, Component.literal("No mobs spawned (caps reached, bad terrain, or no matching biome)").withStyle(ChatFormatting.YELLOW));
        }
        return spawned;
    }

    private static int forceOcean(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("Must be used in a world"));
            return 0;
        }

        int spawned = CustomSpawnTicker.forceSpawnOcean(level, player);
        if (spawned > 0) {
            send(source, Component.literal("Force-spawned " + spawned + " ocean mob(s) near you").withStyle(ChatFormatting.GREEN));
        } else {
            send(source, Component.literal("No ocean mobs spawned (no water nearby)").withStyle(ChatFormatting.YELLOW));
        }
        return spawned;
    }

    // === Patrol commands ===

    private static int patrolCreate(CommandSourceStack source, String name) {
        if (PatrolRoute.getRoute(name) != null) {
            send(source, Component.literal("Route '" + name + "' already exists").withStyle(ChatFormatting.RED));
            return 0;
        }
        PatrolRoute.createRoute(name);
        send(source, Component.literal("Created patrol route '" + name + "'. Use /tomsmobs patrol add " + name + " to add waypoints.").withStyle(ChatFormatting.GREEN));
        return 1;
    }

    private static int patrolAdd(CommandSourceStack source, String name) {
        PatrolRoute route = PatrolRoute.getRoute(name);
        if (route == null) {
            send(source, Component.literal("Route '" + name + "' not found. Create it first.").withStyle(ChatFormatting.RED));
            return 0;
        }
        net.minecraft.core.BlockPos pos = net.minecraft.core.BlockPos.containing(source.getPosition());
        route.addWaypoint(pos);
        send(source, Component.literal("Added waypoint #" + route.waypointCount() + " to '" + name + "' at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()).withStyle(ChatFormatting.GREEN));
        return 1;
    }

    private static int patrolAssign(CommandSourceStack source, String name) {
        PatrolRoute route = PatrolRoute.getRoute(name);
        if (route == null) {
            send(source, Component.literal("Route '" + name + "' not found.").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (route.waypointCount() < 2) {
            send(source, Component.literal("Route needs at least 2 waypoints.").withStyle(ChatFormatting.RED));
            return 0;
        }
        // Find nearest NPC
        if (!(source.getLevel() instanceof ServerLevel level)) return 0;
        AABB area = new AABB(source.getPosition(), source.getPosition()).inflate(10);
        Entity nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (Entity e : level.getEntities((Entity) null, area, en -> en instanceof AbstractNpc)) {
            double dist = e.distanceToSqr(source.getPosition());
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = e;
            }
        }
        if (nearest == null) {
            send(source, Component.literal("No NPC found within 10 blocks.").withStyle(ChatFormatting.RED));
            return 0;
        }
        // Unassign from any existing route first
        PatrolRoute existing = PatrolRoute.getRouteForNpc(nearest.getUUID());
        if (existing != null) existing.unassignNpc(nearest.getUUID());

        route.assignNpc(nearest.getUUID());
        String typeName = nearest.getType().getDescription().getString();
        send(source, Component.literal("Assigned " + typeName + " to patrol route '" + name + "'").withStyle(ChatFormatting.GREEN));
        return 1;
    }

    private static int patrolUnassign(CommandSourceStack source) {
        if (!(source.getLevel() instanceof ServerLevel level)) return 0;
        AABB area = new AABB(source.getPosition(), source.getPosition()).inflate(10);
        Entity nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (Entity e : level.getEntities((Entity) null, area, en -> en instanceof AbstractNpc)) {
            double dist = e.distanceToSqr(source.getPosition());
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = e;
            }
        }
        if (nearest == null) {
            send(source, Component.literal("No NPC found within 10 blocks.").withStyle(ChatFormatting.RED));
            return 0;
        }
        PatrolRoute route = PatrolRoute.getRouteForNpc(nearest.getUUID());
        if (route == null) {
            send(source, Component.literal("This NPC is not assigned to any route.").withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        route.unassignNpc(nearest.getUUID());
        send(source, Component.literal("Unassigned NPC from route '" + route.name + "'").withStyle(ChatFormatting.GREEN));
        return 1;
    }

    private static int patrolList(CommandSourceStack source) {
        var routes = PatrolRoute.allRoutes();
        if (routes.isEmpty()) {
            send(source, Component.literal("No patrol routes defined.").withStyle(ChatFormatting.GRAY));
            return 0;
        }
        send(source, Component.literal("=== Patrol Routes ===").withStyle(ChatFormatting.GOLD));
        for (PatrolRoute route : routes) {
            send(source, Component.literal("  " + route.name + ": " + route.waypointCount() + " waypoints, " + route.assignedNpcs.size() + " NPCs").withStyle(ChatFormatting.WHITE));
        }
        return routes.size();
    }

    private static int patrolDelete(CommandSourceStack source, String name) {
        if (PatrolRoute.deleteRoute(name)) {
            send(source, Component.literal("Deleted route '" + name + "'").withStyle(ChatFormatting.GREEN));
            return 1;
        }
        send(source, Component.literal("Route '" + name + "' not found.").withStyle(ChatFormatting.RED));
        return 0;
    }

    private static boolean isTomsMob(EntityType<?> type) {
        for (SpawnEntry entry : MobRegistry.SPAWN_ENTRIES) {
            if (entry.type() == type) return true;
        }
        return false;
    }

    // --- Shared pet ownership ---

    private static net.minecraft.world.entity.LivingEntity findNearestOwnedPet(ServerPlayer player) {
        AABB box = player.getBoundingBox().inflate(5.0);
        net.minecraft.world.entity.LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (Entity e : player.level().getEntities((Entity) null, box, ent -> ent instanceof de.tomalbrc.toms_mobs.entity.passive.dog.AbstractDog || ent instanceof de.tomalbrc.toms_mobs.entity.passive.cat.AbstractCat)) {
            boolean isPrimary;
            if (e instanceof de.tomalbrc.toms_mobs.entity.passive.dog.AbstractDog d) isPrimary = d.isPrimaryOwner(player);
            else if (e instanceof de.tomalbrc.toms_mobs.entity.passive.cat.AbstractCat c) isPrimary = c.isPrimaryOwner(player);
            else continue;
            if (!isPrimary) continue;
            double d2 = e.distanceToSqr(player);
            if (d2 < closestDist) { closestDist = d2; closest = (net.minecraft.world.entity.LivingEntity) e; }
        }
        return closest;
    }

    private static int petShareAdd(CommandSourceStack source, String targetName) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }
        ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            send(source, Component.literal("Player '" + targetName + "' is not online.").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (target.getUUID().equals(player.getUUID())) {
            send(source, Component.literal("You can't add yourself as a co-owner.").withStyle(ChatFormatting.RED));
            return 0;
        }
        net.minecraft.world.entity.LivingEntity pet = findNearestOwnedPet(player);
        if (pet == null) {
            send(source, Component.literal("No pet you own within 5 blocks.").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (pet instanceof de.tomalbrc.toms_mobs.entity.passive.dog.AbstractDog d) d.addCoOwner(target.getUUID());
        else if (pet instanceof de.tomalbrc.toms_mobs.entity.passive.cat.AbstractCat c) c.addCoOwner(target.getUUID());
        send(source, Component.literal("Added " + targetName + " as a co-owner.").withStyle(ChatFormatting.GREEN));
        target.sendSystemMessage(Component.literal(player.getName().getString() + " added you as co-owner of their pet.").withStyle(ChatFormatting.GREEN));
        return 1;
    }

    private static int petShareRemove(CommandSourceStack source, String targetName) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }
        net.minecraft.world.entity.LivingEntity pet = findNearestOwnedPet(player);
        if (pet == null) {
            send(source, Component.literal("No pet you own within 5 blocks.").withStyle(ChatFormatting.RED));
            return 0;
        }
        ServerPlayer online = source.getServer().getPlayerList().getPlayerByName(targetName);
        if (online == null) {
            send(source, Component.literal("Player '" + targetName + "' must be online to remove co-ownership.").withStyle(ChatFormatting.RED));
            return 0;
        }
        java.util.UUID targetId = online.getUUID();
        if (pet instanceof de.tomalbrc.toms_mobs.entity.passive.dog.AbstractDog d) d.removeCoOwner(targetId);
        else if (pet instanceof de.tomalbrc.toms_mobs.entity.passive.cat.AbstractCat c) c.removeCoOwner(targetId);
        send(source, Component.literal("Removed " + targetName + " as a co-owner.").withStyle(ChatFormatting.YELLOW));
        return 1;
    }

    private static int petShareList(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }
        net.minecraft.world.entity.LivingEntity pet = findNearestOwnedPet(player);
        if (pet == null) {
            send(source, Component.literal("No pet you own within 5 blocks.").withStyle(ChatFormatting.RED));
            return 0;
        }
        java.util.Set<java.util.UUID> coOwners;
        if (pet instanceof de.tomalbrc.toms_mobs.entity.passive.dog.AbstractDog d) coOwners = d.getCoOwners();
        else if (pet instanceof de.tomalbrc.toms_mobs.entity.passive.cat.AbstractCat c) coOwners = c.getCoOwners();
        else return 0;
        send(source, Component.literal("=== Pet owners ===").withStyle(ChatFormatting.GOLD));
        send(source, Component.literal("Primary: " + player.getName().getString()).withStyle(ChatFormatting.AQUA));
        if (coOwners.isEmpty()) {
            send(source, Component.literal("No co-owners.").withStyle(ChatFormatting.GRAY));
        } else {
            for (java.util.UUID id : coOwners) {
                ServerPlayer online = source.getServer().getPlayerList().getPlayer(id);
                String name = online != null ? online.getName().getString() : id.toString();
                send(source, Component.literal(" - " + name).withStyle(ChatFormatting.AQUA));
            }
        }
        return 1;
    }

    private static int petTransfer(CommandSourceStack source, String targetName) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }
        ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            send(source, Component.literal("Player '" + targetName + "' must be online to receive a pet transfer.").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (target.getUUID().equals(player.getUUID())) {
            send(source, Component.literal("You can't transfer to yourself.").withStyle(ChatFormatting.RED));
            return 0;
        }
        net.minecraft.world.entity.LivingEntity pet = findNearestOwnedPet(player);
        if (pet == null) {
            send(source, Component.literal("No pet you own within 5 blocks.").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (pet instanceof net.minecraft.world.entity.TamableAnimal tam) {
            tam.tame(target);
            if (pet instanceof de.tomalbrc.toms_mobs.entity.passive.dog.AbstractDog d) {
                d.removeCoOwner(target.getUUID());
                d.addCoOwner(player.getUUID());
            } else if (pet instanceof de.tomalbrc.toms_mobs.entity.passive.cat.AbstractCat c) {
                c.removeCoOwner(target.getUUID());
                c.addCoOwner(player.getUUID());
            }
            send(source, Component.literal("Primary ownership transferred to " + targetName + ". You are now a co-owner.").withStyle(ChatFormatting.GREEN));
            target.sendSystemMessage(Component.literal(player.getName().getString() + " transferred their pet to you. You are now the primary owner.").withStyle(ChatFormatting.GREEN));
        }
        return 1;
    }
}
