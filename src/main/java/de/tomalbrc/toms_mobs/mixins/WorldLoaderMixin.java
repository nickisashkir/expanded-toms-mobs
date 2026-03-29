package de.tomalbrc.toms_mobs.mixins;

import com.mojang.datafixers.util.Pair;
import de.tomalbrc.toms_mobs.registry.MobRegistry;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

@Mixin(WorldLoader.class)
public class WorldLoaderMixin {
    @Inject(method = "lambda$load$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ReloadableServerResources;loadResources(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/LayeredRegistryAccess;Ljava/util/List;Lnet/minecraft/world/flag/FeatureFlagSet;Lnet/minecraft/commands/Commands$CommandSelection;Lnet/minecraft/server/permissions/PermissionSet;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static <D, R> void tm$almostDone(Pair packsAndResourceManager, List dimensionContextRegistries, WorldLoader.WorldDataSupplier worldDataSupplier, CloseableResourceManager resources, LayeredRegistryAccess initialLayers, RegistryAccess.Frozen loadedWorldgenRegistries, List staticLayerTags, WorldLoader.InitConfig config, Executor backgroundExecutor, Executor mainThreadExecutor, WorldLoader.ResultFactory resultFactory, RegistryAccess.Frozen initialWorldgenDimensions, CallbackInfoReturnable<CompletionStage> cir) {
        MobRegistry.registerMobs(loadedWorldgenRegistries);
    }
}