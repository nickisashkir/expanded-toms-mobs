package de.tomalbrc.toms_mobs.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import de.tomalbrc.bil.json.SimpleCodecDeserializer;
import de.tomalbrc.toms_mobs.TomsMobs;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public class ModConfig {
    private static final Path CONFIG_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve(TomsMobs.MODID + ".json");
    private static ModConfig instance;

    private static final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(Identifier.class, new SimpleCodecDeserializer<>(Identifier.CODEC))
            .setPrettyPrinting()
            .create();

    // entries

    @SerializedName("spawns")
    public JsonElement spawnsJson;

    public List<Identifier> disabledMobs = new ObjectArrayList<>();
    public boolean noAdditionalRaidMobs = true;

    @SerializedName("custom_spawn_ticker")
    public boolean customSpawnTicker = true;

    @SerializedName("custom_spawn_ticker_interval_ticks")
    public int customSpawnTickerIntervalTicks = 24000;

    @SerializedName("custom_spawn_ticker_soft_cap_per_type")
    public int customSpawnTickerSoftCapPerType = 12;

    public static ModConfig getInstance() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        if (!CONFIG_FILE_PATH.toFile().exists()) {
            instance = new ModConfig();
            var resource = TomsMobs.class.getResourceAsStream("/default-spawns.json");
            if (resource != null)
                ModConfig.instance.spawnsJson = JsonParser.parseReader(new InputStreamReader(resource));

            save();

            return;
        }

        try {
            ModConfig.instance = gson.fromJson(new FileReader(ModConfig.CONFIG_FILE_PATH.toFile()), ModConfig.class);
            if (ModConfig.instance.spawnsJson == null) {
                var resource = TomsMobs.class.getResourceAsStream("/default-spawns.json");
                if (resource != null) {
                    ModConfig.instance.spawnsJson = JsonParser.parseReader(new InputStreamReader(resource));
                    save();
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void save() {
        try (FileOutputStream stream = new FileOutputStream(CONFIG_FILE_PATH.toFile())) {
            stream.write(gson.toJson(instance).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}