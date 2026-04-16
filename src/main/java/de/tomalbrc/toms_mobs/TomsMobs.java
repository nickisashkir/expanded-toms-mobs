package de.tomalbrc.toms_mobs;

import com.mojang.logging.LogUtils;
import de.tomalbrc.toms_mobs.command.TomsMobsCommand;
import de.tomalbrc.toms_mobs.registry.MobRegistry;
import de.tomalbrc.toms_mobs.util.CustomSpawnTicker;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;

public class TomsMobs implements ModInitializer {
    public static final String MODID = "toms_mobs";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.addModAssets(MODID);
        PolymerResourcePackUtils.markAsRequired();
        MobRegistry.registerContent();

        ServerTickEvents.END_LEVEL_TICK.register(CustomSpawnTicker::tick);
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> TomsMobsCommand.register(dispatcher));
    }
}
