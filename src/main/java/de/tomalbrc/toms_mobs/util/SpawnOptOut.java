package de.tomalbrc.toms_mobs.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.tomalbrc.toms_mobs.TomsMobs;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpawnOptOut {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("toms_mobs_optout.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<UUID> OPTED_OUT = new HashSet<>();
    private static boolean loaded = false;

    public static boolean isOptedOut(UUID uuid) {
        ensureLoaded();
        return OPTED_OUT.contains(uuid);
    }

    public static boolean toggle(UUID uuid) {
        ensureLoaded();
        boolean nowOptedOut;
        if (OPTED_OUT.contains(uuid)) {
            OPTED_OUT.remove(uuid);
            nowOptedOut = false;
        } else {
            OPTED_OUT.add(uuid);
            nowOptedOut = true;
        }
        save();
        return nowOptedOut;
    }

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        if (!FILE.toFile().exists()) return;
        try (FileReader reader = new FileReader(FILE.toFile())) {
            Type type = new TypeToken<Set<UUID>>(){}.getType();
            Set<UUID> loadedSet = GSON.fromJson(reader, type);
            if (loadedSet != null) OPTED_OUT.addAll(loadedSet);
        } catch (Exception e) {
            TomsMobs.LOGGER.error("Failed to load opt-out file", e);
        }
    }

    private static void save() {
        try (FileWriter writer = new FileWriter(FILE.toFile())) {
            GSON.toJson(OPTED_OUT, writer);
        } catch (Exception e) {
            TomsMobs.LOGGER.error("Failed to save opt-out file", e);
        }
    }
}
