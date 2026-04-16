package de.tomalbrc.toms_mobs.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.tomalbrc.toms_mobs.TomsMobs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

public class PatrolRoute {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("toms_mobs_patrols.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Map<String, PatrolRoute> ROUTES = new LinkedHashMap<>();
    private static boolean loaded = false;

    public String name;
    public List<int[]> waypoints = new ArrayList<>();
    public Set<String> assignedNpcs = new HashSet<>();
    public int pauseTicks = 60; // 3 seconds pause at each waypoint

    public PatrolRoute() {}

    public PatrolRoute(String name) {
        this.name = name;
    }

    public void addWaypoint(BlockPos pos) {
        waypoints.add(new int[]{pos.getX(), pos.getY(), pos.getZ()});
        save();
    }

    public BlockPos getWaypoint(int index) {
        int[] p = waypoints.get(index % waypoints.size());
        return new BlockPos(p[0], p[1], p[2]);
    }

    public int waypointCount() {
        return waypoints.size();
    }

    public void assignNpc(UUID uuid) {
        assignedNpcs.add(uuid.toString());
        save();
    }

    public void unassignNpc(UUID uuid) {
        assignedNpcs.remove(uuid.toString());
        save();
    }

    public boolean isAssigned(UUID uuid) {
        return assignedNpcs.contains(uuid.toString());
    }

    // --- Static route management ---

    public static PatrolRoute getRoute(String name) {
        ensureLoaded();
        return ROUTES.get(name.toLowerCase());
    }

    public static PatrolRoute createRoute(String name) {
        ensureLoaded();
        String key = name.toLowerCase();
        PatrolRoute route = new PatrolRoute(key);
        ROUTES.put(key, route);
        save();
        return route;
    }

    public static boolean deleteRoute(String name) {
        ensureLoaded();
        boolean removed = ROUTES.remove(name.toLowerCase()) != null;
        if (removed) save();
        return removed;
    }

    public static Collection<PatrolRoute> allRoutes() {
        ensureLoaded();
        return ROUTES.values();
    }

    public static Set<String> routeNames() {
        ensureLoaded();
        return ROUTES.keySet();
    }

    public static PatrolRoute getRouteForNpc(UUID uuid) {
        ensureLoaded();
        for (PatrolRoute route : ROUTES.values()) {
            if (route.isAssigned(uuid)) return route;
        }
        return null;
    }

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        if (!FILE.toFile().exists()) return;
        try (FileReader reader = new FileReader(FILE.toFile())) {
            Type type = new TypeToken<LinkedHashMap<String, PatrolRoute>>(){}.getType();
            Map<String, PatrolRoute> loadedMap = GSON.fromJson(reader, type);
            if (loadedMap != null) ROUTES = loadedMap;
        } catch (Exception e) {
            TomsMobs.LOGGER.error("Failed to load patrol routes", e);
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(FILE.toFile())) {
            GSON.toJson(ROUTES, writer);
        } catch (Exception e) {
            TomsMobs.LOGGER.error("Failed to save patrol routes", e);
        }
    }
}
