package de.tomalbrc.toms_mobs.registry;

import aqario.fowlplay.common.entity.bird.FlyingBirdEntity;
import com.mojang.serialization.JsonOps;
import de.tomalbrc.toms_mobs.TomsMobs;
import de.tomalbrc.toms_mobs.config.ConfiguredSpawn;
import de.tomalbrc.toms_mobs.config.ModConfig;
import de.tomalbrc.toms_mobs.entity.passive.*;
import de.tomalbrc.toms_mobs.entity.passive.jellyfish.*;
import de.tomalbrc.toms_mobs.entity.passive.seal.*;
import de.tomalbrc.toms_mobs.entity.passive.crab.*;
import de.tomalbrc.toms_mobs.entity.passive.owl.*;
import de.tomalbrc.toms_mobs.entity.passive.squirrel.*;
import de.tomalbrc.toms_mobs.entity.passive.seahorse.*;
import de.tomalbrc.toms_mobs.entity.passive.duck.*;
import de.tomalbrc.toms_mobs.entity.passive.dog.*;
import de.tomalbrc.toms_mobs.entity.passive.cat.*;
import de.tomalbrc.toms_mobs.entity.passive.sloth.*;
import de.tomalbrc.toms_mobs.entity.passive.koi.*;
import de.tomalbrc.toms_mobs.entity.passive.npc.*;
import de.tomalbrc.toms_mobs.entity.passive.winterbird.*;
import de.tomalbrc.toms_mobs.item.TexturedPolymerSpawnEggItem;
import de.tomalbrc.toms_mobs.item.VanillaPolymerSpawnEggItem;
import de.tomalbrc.toms_mobs.util.BiomeHelper;
import de.tomalbrc.toms_mobs.util.SpawnEntry;
import de.tomalbrc.toms_mobs.util.SpawnRules;
import de.tomalbrc.toms_mobs.util.Util;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerCreativeModeTabUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MobRegistry {
    public static final java.util.List<SpawnEntry> SPAWN_ENTRIES = new java.util.ArrayList<>();

    //public static final EntityType<@NotNull Penguin> PENGUIN = register(Penguin.ID, FabricEntityType.Builder.createMob(Penguin::new, MobCategory.CREATURE, x -> x.defaultAttributes(Penguin::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 1.05f));

    //public static final EntityType<@NotNull Elephant> ELEPHANT = register(Elephant.ID, FabricEntityType.Builder.createMob(Elephant::new, MobCategory.CREATURE, x -> x.defaultAttributes(Elephant::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(2.99f, 3.65f));

    //public static final EntityType<@NotNull Capybara> CAPYBARA = register(Capybara.ID, FabricEntityType.Builder.createMob(Capybara::new, MobCategory.CREATURE, x -> x.defaultAttributes(Capybara::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.9f, 1.f));

    //public static final EntityType<@NotNull Possum> POSSUM = register(Possum.ID, FabricEntityType.Builder.createMob(Possum::new, MobCategory.CREATURE, x -> x.defaultAttributes(Possum::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.9f, 1.f));

    /*public static final EntityType<Vulture> VULTURE = register(
            Vulture.ID,
            FabricEntityTypeBuilder.createMob()
                    .entityFactory(Vulture::new)
                    .spawnGroup(MobCategory.CREATURE)
                    .dimensions(EntityDimensions.scalable(1.f, 1.f))
                    .defaultAttributes(Vulture::createAttributes)
                    .spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)
    );
*/
    //public static final EntityType<@NotNull Seagull> SEAGULL = register(Seagull.ID, FabricEntityType.Builder.createMob(Seagull::new, MobCategory.CREATURE, x -> x.defaultAttributes(Seagull::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, FlyingBirdEntity::canSpawnShorebirds)).sized(0.6f, 0.8f).eyeHeight(0.7f));

    //public static final EntityType<@NotNull Mantaray> MANTARAY = register(Mantaray.ID, FabricEntityType.Builder.createMob(Mantaray::new, MobCategory.WATER_CREATURE, x -> x.defaultAttributes(Mantaray::createAttributes).spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mantaray::checkRareDeepWaterSpawnRules)).sized(1.4f, 0.4f));

    //public static final EntityType<@NotNull Tuna> TUNA = register(Tuna.ID, FabricEntityType.Builder.createMob(Tuna::new, MobCategory.WATER_AMBIENT, x -> x.defaultAttributes(Tuna::createAttributes).spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.OCEAN_FLOOR, Tuna::checkDeepWaterSpawnRules)).sized(0.55f, 0.55f));

    //public static final EntityType<@NotNull Lobster> LOBSTER = register(Lobster.ID, FabricEntityType.Builder.createMob(Lobster::new, MobCategory.WATER_CREATURE, x -> x.defaultAttributes(Lobster::createAttributes).spawnPlacement(SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.OCEAN_FLOOR, (xx, levelAccessor, z, blockPos, r) -> r.nextInt(4) == 2 && blockPos.getY() < levelAccessor.getSeaLevel() + 3 && (TurtleEggBlock.onSand(levelAccessor, blockPos) || levelAccessor.getBlockState(blockPos).getFluidState().is(FluidTags.WATER)) && levelAccessor.getRawBrightness(blockPos, 0) > 1)).sized(0.65f, 0.35f));

    public static final EntityType<@NotNull JellyfishBlue> JELLYFISH_BLUE = register(JellyfishBlue.ID, FabricEntityType.Builder.createMob(JellyfishBlue::new, MobCategory.WATER_AMBIENT, x -> x.defaultAttributes(AbstractJellyfish::createAttributes).spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractJellyfish::checkJellyfishSpawnRules)).sized(0.6f, 1.0f));

    public static final EntityType<@NotNull JellyfishGolden> JELLYFISH_GOLDEN = register(JellyfishGolden.ID, FabricEntityType.Builder.createMob(JellyfishGolden::new, MobCategory.WATER_AMBIENT, x -> x.defaultAttributes(AbstractJellyfish::createAttributes).spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractJellyfish::checkJellyfishSpawnRules)).sized(0.6f, 1.0f));

    public static final EntityType<@NotNull JellyfishFusion> JELLYFISH_FUSION = register(JellyfishFusion.ID, FabricEntityType.Builder.createMob(JellyfishFusion::new, MobCategory.WATER_AMBIENT, x -> x.defaultAttributes(AbstractJellyfish::createAttributes).spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractJellyfish::checkJellyfishSpawnRules)).sized(0.6f, 1.0f));

    public static final EntityType<@NotNull JellyfishOrange> JELLYFISH_ORANGE = register(JellyfishOrange.ID, FabricEntityType.Builder.createMob(JellyfishOrange::new, MobCategory.WATER_AMBIENT, x -> x.defaultAttributes(AbstractJellyfish::createAttributes).spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractJellyfish::checkJellyfishSpawnRules)).sized(0.6f, 1.0f));

    public static final EntityType<@NotNull JellyfishPink> JELLYFISH_PINK = register(JellyfishPink.ID, FabricEntityType.Builder.createMob(JellyfishPink::new, MobCategory.WATER_AMBIENT, x -> x.defaultAttributes(AbstractJellyfish::createAttributes).spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractJellyfish::checkJellyfishSpawnRules)).sized(0.6f, 1.0f));

    public static final EntityType<@NotNull JellyfishWhite> JELLYFISH_WHITE = register(JellyfishWhite.ID, FabricEntityType.Builder.createMob(JellyfishWhite::new, MobCategory.WATER_AMBIENT, x -> x.defaultAttributes(AbstractJellyfish::createAttributes).spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractJellyfish::checkJellyfishSpawnRules)).sized(0.6f, 1.0f));

    // === Seals ===
    public static final EntityType<@NotNull SealArctic> SEAL_ARCTIC = register(SealArctic.ID, FabricEntityType.Builder.createMob(SealArctic::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractSeal::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(1.0f, 0.6f));

    public static final EntityType<@NotNull SealHarbor> SEAL_HARBOR = register(SealHarbor.ID, FabricEntityType.Builder.createMob(SealHarbor::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractSeal::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(1.0f, 0.6f));

    public static final EntityType<@NotNull SealMonk> SEAL_MONK = register(SealMonk.ID, FabricEntityType.Builder.createMob(SealMonk::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractSeal::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(1.0f, 0.6f));

    // === Crabs ===
    public static final EntityType<@NotNull CrabBlue> CRAB_BLUE = register(CrabBlue.ID, FabricEntityType.Builder.createMob(CrabBlue::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractCrab::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 0.4f));

    public static final EntityType<@NotNull CrabOrange> CRAB_ORANGE = register(CrabOrange.ID, FabricEntityType.Builder.createMob(CrabOrange::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractCrab::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 0.4f));

    public static final EntityType<@NotNull CrabRed> CRAB_RED = register(CrabRed.ID, FabricEntityType.Builder.createMob(CrabRed::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractCrab::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 0.4f));

    // === Owls ===
    public static final EntityType<@NotNull OwlBarn> OWL_BARN = register(OwlBarn.ID, FabricEntityType.Builder.createMob(OwlBarn::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractOwl::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.7f));

    public static final EntityType<@NotNull OwlHorned> OWL_HORNED = register(OwlHorned.ID, FabricEntityType.Builder.createMob(OwlHorned::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractOwl::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.7f));

    public static final EntityType<@NotNull OwlLongEared> OWL_LONG_EARED = register(OwlLongEared.ID, FabricEntityType.Builder.createMob(OwlLongEared::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractOwl::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.7f));

    public static final EntityType<@NotNull OwlSnowy> OWL_SNOWY = register(OwlSnowy.ID, FabricEntityType.Builder.createMob(OwlSnowy::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractOwl::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.7f));

    // === Raccoon ===
    public static final EntityType<@NotNull Raccoon> RACCOON = register(Raccoon.ID, FabricEntityType.Builder.createMob(Raccoon::new, MobCategory.CREATURE, x -> x.defaultAttributes(Raccoon::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 0.5f));
    public static final EntityType<@NotNull RaccoonBaby> RACCOON_BABY = register(RaccoonBaby.ID, FabricEntityType.Builder.createMob(RaccoonBaby::new, MobCategory.CREATURE, x -> x.defaultAttributes(RaccoonBaby::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.35f, 0.3f));

    // === Red Panda ===
    public static final EntityType<@NotNull RedPanda> RED_PANDA = register(RedPanda.ID, FabricEntityType.Builder.createMob(RedPanda::new, MobCategory.CREATURE, x -> x.defaultAttributes(RedPanda::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.7f, 0.6f));
    public static final EntityType<@NotNull RedPandaBaby> RED_PANDA_BABY = register(RedPandaBaby.ID, FabricEntityType.Builder.createMob(RedPandaBaby::new, MobCategory.CREATURE, x -> x.defaultAttributes(RedPandaBaby::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.4f, 0.4f));

    // === Meerkat ===
    public static final EntityType<@NotNull Meerkat> MEERKAT = register(Meerkat.ID, FabricEntityType.Builder.createMob(Meerkat::new, MobCategory.CREATURE, x -> x.defaultAttributes(Meerkat::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.4f, 0.6f));
    public static final EntityType<@NotNull MeerkatBaby> MEERKAT_BABY = register(MeerkatBaby.ID, FabricEntityType.Builder.createMob(MeerkatBaby::new, MobCategory.CREATURE, x -> x.defaultAttributes(MeerkatBaby::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.25f, 0.35f));

    // === Squirrels ===
    public static final EntityType<@NotNull SquirrelBrown> SQUIRREL_BROWN = register(SquirrelBrown.ID, FabricEntityType.Builder.createMob(SquirrelBrown::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractSquirrel::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.4f, 0.4f));

    public static final EntityType<@NotNull SquirrelGray> SQUIRREL_GRAY = register(SquirrelGray.ID, FabricEntityType.Builder.createMob(SquirrelGray::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractSquirrel::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.4f, 0.4f));

    public static final EntityType<@NotNull SquirrelRed> SQUIRREL_RED = register(SquirrelRed.ID, FabricEntityType.Builder.createMob(SquirrelRed::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractSquirrel::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.4f, 0.4f));

    // === Seahorses ===
    public static final EntityType<@NotNull SeahorseBlack> SEAHORSE_BLACK = register(SeahorseBlack.ID, FabricEntityType.Builder.createMob(SeahorseBlack::new, MobCategory.WATER_AMBIENT, x -> x.defaultAttributes(AbstractSeahorse::createAttributes).spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractSeahorse::checkSeahorseSpawnRules)).sized(0.4f, 0.6f));

    public static final EntityType<@NotNull SeahorseBlue> SEAHORSE_BLUE = register(SeahorseBlue.ID, FabricEntityType.Builder.createMob(SeahorseBlue::new, MobCategory.WATER_AMBIENT, x -> x.defaultAttributes(AbstractSeahorse::createAttributes).spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractSeahorse::checkSeahorseSpawnRules)).sized(0.4f, 0.6f));

    public static final EntityType<@NotNull SeahorseGreen> SEAHORSE_GREEN = register(SeahorseGreen.ID, FabricEntityType.Builder.createMob(SeahorseGreen::new, MobCategory.WATER_AMBIENT, x -> x.defaultAttributes(AbstractSeahorse::createAttributes).spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractSeahorse::checkSeahorseSpawnRules)).sized(0.4f, 0.6f));

    public static final EntityType<@NotNull SeahorsePurple> SEAHORSE_PURPLE = register(SeahorsePurple.ID, FabricEntityType.Builder.createMob(SeahorsePurple::new, MobCategory.WATER_AMBIENT, x -> x.defaultAttributes(AbstractSeahorse::createAttributes).spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractSeahorse::checkSeahorseSpawnRules)).sized(0.4f, 0.6f));

    public static final EntityType<@NotNull SeahorseRed> SEAHORSE_RED = register(SeahorseRed.ID, FabricEntityType.Builder.createMob(SeahorseRed::new, MobCategory.WATER_AMBIENT, x -> x.defaultAttributes(AbstractSeahorse::createAttributes).spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractSeahorse::checkSeahorseSpawnRules)).sized(0.4f, 0.6f));

    // === Koi ===
    public static final EntityType<@NotNull KoiWhite> KOI_WHITE = register(KoiWhite.ID, FabricEntityType.Builder.createMob(KoiWhite::new, MobCategory.WATER_AMBIENT, x -> x.defaultAttributes(AbstractKoi::createAttributes).spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.4f, 0.3f));
    public static final EntityType<@NotNull KoiOrange> KOI_ORANGE = register(KoiOrange.ID, FabricEntityType.Builder.createMob(KoiOrange::new, MobCategory.WATER_AMBIENT, x -> x.defaultAttributes(AbstractKoi::createAttributes).spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.4f, 0.3f));
    public static final EntityType<@NotNull KoiRed> KOI_RED = register(KoiRed.ID, FabricEntityType.Builder.createMob(KoiRed::new, MobCategory.WATER_AMBIENT, x -> x.defaultAttributes(AbstractKoi::createAttributes).spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.4f, 0.3f));
    public static final EntityType<@NotNull KoiGold> KOI_GOLD = register(KoiGold.ID, FabricEntityType.Builder.createMob(KoiGold::new, MobCategory.WATER_AMBIENT, x -> x.defaultAttributes(AbstractKoi::createAttributes).spawnPlacement(SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.4f, 0.3f));

    // === NPCs ===
    public static final EntityType<@NotNull NpcBlacksmith> NPC_BLACKSMITH = register(NpcBlacksmith.ID, FabricEntityType.Builder.createMob(NpcBlacksmith::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractNpc::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 1.8f));
    public static final EntityType<@NotNull NpcBlacksmith2> NPC_BLACKSMITH_2 = register(NpcBlacksmith2.ID, FabricEntityType.Builder.createMob(NpcBlacksmith2::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractNpc::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 1.8f));
    public static final EntityType<@NotNull NpcBlacksmith3> NPC_BLACKSMITH_3 = register(NpcBlacksmith3.ID, FabricEntityType.Builder.createMob(NpcBlacksmith3::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractNpc::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 1.8f));
    public static final EntityType<@NotNull NpcBlacksmith4> NPC_BLACKSMITH_4 = register(NpcBlacksmith4.ID, FabricEntityType.Builder.createMob(NpcBlacksmith4::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractNpc::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 1.8f));
    public static final EntityType<@NotNull NpcBlacksmith5> NPC_BLACKSMITH_5 = register(NpcBlacksmith5.ID, FabricEntityType.Builder.createMob(NpcBlacksmith5::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractNpc::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 1.8f));
    public static final EntityType<@NotNull NpcGuard> NPC_GUARD = register(NpcGuard.ID, FabricEntityType.Builder.createMob(NpcGuard::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractNpc::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 1.8f));
    public static final EntityType<@NotNull NpcGuard2> NPC_GUARD_2 = register(NpcGuard2.ID, FabricEntityType.Builder.createMob(NpcGuard2::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractNpc::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 1.8f));
    public static final EntityType<@NotNull NpcLibrarian> NPC_LIBRARIAN = register(NpcLibrarian.ID, FabricEntityType.Builder.createMob(NpcLibrarian::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractNpc::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 1.8f));
    public static final EntityType<@NotNull NpcLibrarian2> NPC_LIBRARIAN_2 = register(NpcLibrarian2.ID, FabricEntityType.Builder.createMob(NpcLibrarian2::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractNpc::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 1.8f));
    public static final EntityType<@NotNull NpcLibrarian3> NPC_LIBRARIAN_3 = register(NpcLibrarian3.ID, FabricEntityType.Builder.createMob(NpcLibrarian3::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractNpc::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 1.8f));
    public static final EntityType<@NotNull NpcLibrarian4> NPC_LIBRARIAN_4 = register(NpcLibrarian4.ID, FabricEntityType.Builder.createMob(NpcLibrarian4::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractNpc::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 1.8f));
    public static final EntityType<@NotNull NpcLibrarian5> NPC_LIBRARIAN_5 = register(NpcLibrarian5.ID, FabricEntityType.Builder.createMob(NpcLibrarian5::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractNpc::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 1.8f));
    public static final EntityType<@NotNull NpcLibrarian6> NPC_LIBRARIAN_6 = register(NpcLibrarian6.ID, FabricEntityType.Builder.createMob(NpcLibrarian6::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractNpc::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 1.8f));
    public static final EntityType<@NotNull NpcLibrarian7> NPC_LIBRARIAN_7 = register(NpcLibrarian7.ID, FabricEntityType.Builder.createMob(NpcLibrarian7::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractNpc::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 1.8f));
    public static final EntityType<@NotNull NpcFarmer> NPC_FARMER = register(NpcFarmer.ID, FabricEntityType.Builder.createMob(NpcFarmer::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractNpc::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 1.8f));
    public static final EntityType<@NotNull NpcFarmer2> NPC_FARMER_2 = register(NpcFarmer2.ID, FabricEntityType.Builder.createMob(NpcFarmer2::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractNpc::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 1.8f));
    public static final EntityType<@NotNull NpcFarmer3> NPC_FARMER_3 = register(NpcFarmer3.ID, FabricEntityType.Builder.createMob(NpcFarmer3::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractNpc::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 1.8f));
    public static final EntityType<@NotNull NpcFarmer4> NPC_FARMER_4 = register(NpcFarmer4.ID, FabricEntityType.Builder.createMob(NpcFarmer4::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractNpc::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 1.8f));
    public static final EntityType<@NotNull NpcFarmer5> NPC_FARMER_5 = register(NpcFarmer5.ID, FabricEntityType.Builder.createMob(NpcFarmer5::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractNpc::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 1.8f));

    public static final EntityType<@NotNull DuckBrown> DUCK_BROWN = register(DuckBrown.ID, FabricEntityType.Builder.createMob(DuckBrown::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDuck::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.5f));
    public static final EntityType<@NotNull DuckMallard> DUCK_MALLARD = register(DuckMallard.ID, FabricEntityType.Builder.createMob(DuckMallard::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDuck::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.5f));
    public static final EntityType<@NotNull DuckWhite> DUCK_WHITE = register(DuckWhite.ID, FabricEntityType.Builder.createMob(DuckWhite::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDuck::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.5f));
    public static final EntityType<@NotNull Duckling> DUCKLING = register(Duckling.ID, FabricEntityType.Builder.createMob(Duckling::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDuck::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.3f, 0.3f));
    public static final EntityType<@NotNull DogBulldogEnglish> DOG_BULLDOG_ENGLISH = register(DogBulldogEnglish.ID, FabricEntityType.Builder.createMob(DogBulldogEnglish::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDog::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 0.6f));
    public static final EntityType<@NotNull DogCorgi> DOG_CORGI = register(DogCorgi.ID, FabricEntityType.Builder.createMob(DogCorgi::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDog::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.5f));
    public static final EntityType<@NotNull DogDalmation> DOG_DALMATION = register(DogDalmation.ID, FabricEntityType.Builder.createMob(DogDalmation::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDog::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 0.7f));
    public static final EntityType<@NotNull DogFinnishLapphundBlack> DOG_FINNISH_LAPPHUND_BLACK = register(DogFinnishLapphundBlack.ID, FabricEntityType.Builder.createMob(DogFinnishLapphundBlack::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDog::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 0.7f));
    public static final EntityType<@NotNull DogFinnishLapphundGinger> DOG_FINNISH_LAPPHUND_GINGER = register(DogFinnishLapphundGinger.ID, FabricEntityType.Builder.createMob(DogFinnishLapphundGinger::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDog::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 0.7f));
    public static final EntityType<@NotNull DogFinnishLapphundWhite> DOG_FINNISH_LAPPHUND_WHITE = register(DogFinnishLapphundWhite.ID, FabricEntityType.Builder.createMob(DogFinnishLapphundWhite::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDog::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 0.7f));
    public static final EntityType<@NotNull DogGermanShepherd> DOG_GERMAN_SHEPHERD = register(DogGermanShepherd.ID, FabricEntityType.Builder.createMob(DogGermanShepherd::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDog::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.7f, 0.8f));
    public static final EntityType<@NotNull DogHuskyBlack> DOG_HUSKY_BLACK = register(DogHuskyBlack.ID, FabricEntityType.Builder.createMob(DogHuskyBlack::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDog::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 0.7f));
    public static final EntityType<@NotNull DogHuskyBrown> DOG_HUSKY_BROWN = register(DogHuskyBrown.ID, FabricEntityType.Builder.createMob(DogHuskyBrown::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDog::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 0.7f));
    public static final EntityType<@NotNull DogLabradorBlack> DOG_LABRADOR_BLACK = register(DogLabradorBlack.ID, FabricEntityType.Builder.createMob(DogLabradorBlack::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDog::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.7f, 0.7f));
    public static final EntityType<@NotNull DogLabradorChocolate> DOG_LABRADOR_CHOCOLATE = register(DogLabradorChocolate.ID, FabricEntityType.Builder.createMob(DogLabradorChocolate::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDog::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.7f, 0.7f));
    public static final EntityType<@NotNull DogLabradorYellow> DOG_LABRADOR_YELLOW = register(DogLabradorYellow.ID, FabricEntityType.Builder.createMob(DogLabradorYellow::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDog::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.7f, 0.7f));
    public static final EntityType<@NotNull DogPoodleBlack> DOG_POODLE_BLACK = register(DogPoodleBlack.ID, FabricEntityType.Builder.createMob(DogPoodleBlack::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDog::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.7f));
    public static final EntityType<@NotNull DogPoodleBrown> DOG_POODLE_BROWN = register(DogPoodleBrown.ID, FabricEntityType.Builder.createMob(DogPoodleBrown::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDog::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.7f));
    public static final EntityType<@NotNull DogPoodleWhite> DOG_POODLE_WHITE = register(DogPoodleWhite.ID, FabricEntityType.Builder.createMob(DogPoodleWhite::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDog::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.7f));
    public static final EntityType<@NotNull DogPugBlack> DOG_PUG_BLACK = register(DogPugBlack.ID, FabricEntityType.Builder.createMob(DogPugBlack::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDog::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.4f, 0.4f));
    public static final EntityType<@NotNull DogPugYellow> DOG_PUG_YELLOW = register(DogPugYellow.ID, FabricEntityType.Builder.createMob(DogPugYellow::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractDog::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.4f, 0.4f));
    public static final EntityType<@NotNull CatBombay> CAT_BOMBAY = register(CatBombay.ID, FabricEntityType.Builder.createMob(CatBombay::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractCat::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.5f));
    public static final EntityType<@NotNull CatChartreux> CAT_CHARTREUX = register(CatChartreux.ID, FabricEntityType.Builder.createMob(CatChartreux::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractCat::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.5f));
    public static final EntityType<@NotNull CatEgyptianMauBrown> CAT_EGYPTIAN_MAU_BROWN = register(CatEgyptianMauBrown.ID, FabricEntityType.Builder.createMob(CatEgyptianMauBrown::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractCat::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.5f));
    public static final EntityType<@NotNull CatEgyptianMauGray> CAT_EGYPTIAN_MAU_GRAY = register(CatEgyptianMauGray.ID, FabricEntityType.Builder.createMob(CatEgyptianMauGray::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractCat::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.5f));
    public static final EntityType<@NotNull CatJapaneseBobtail> CAT_JAPANESE_BOBTAIL = register(CatJapaneseBobtail.ID, FabricEntityType.Builder.createMob(CatJapaneseBobtail::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractCat::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.5f));
    public static final EntityType<@NotNull CatMaineCoonBrown> CAT_MAINE_COON_BROWN = register(CatMaineCoonBrown.ID, FabricEntityType.Builder.createMob(CatMaineCoonBrown::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractCat::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 0.6f));
    public static final EntityType<@NotNull CatMaineCoonGray> CAT_MAINE_COON_GRAY = register(CatMaineCoonGray.ID, FabricEntityType.Builder.createMob(CatMaineCoonGray::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractCat::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 0.6f));
    public static final EntityType<@NotNull CatOrange> CAT_ORANGE = register(CatOrange.ID, FabricEntityType.Builder.createMob(CatOrange::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractCat::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.5f));
    public static final EntityType<@NotNull CatSiamese> CAT_SIAMESE = register(CatSiamese.ID, FabricEntityType.Builder.createMob(CatSiamese::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractCat::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.5f));
    public static final EntityType<@NotNull CatSphynx> CAT_SPHYNX = register(CatSphynx.ID, FabricEntityType.Builder.createMob(CatSphynx::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractCat::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.5f));
    public static final EntityType<@NotNull CatStray> CAT_STRAY = register(CatStray.ID, FabricEntityType.Builder.createMob(CatStray::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractCat::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.5f));
    public static final EntityType<@NotNull CatTabby> CAT_TABBY = register(CatTabby.ID, FabricEntityType.Builder.createMob(CatTabby::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractCat::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.5f));
    public static final EntityType<@NotNull CatTuxedo> CAT_TUXEDO = register(CatTuxedo.ID, FabricEntityType.Builder.createMob(CatTuxedo::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractCat::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.5f));
    public static final EntityType<@NotNull SlothBrown> SLOTH_BROWN = register(SlothBrown.ID, FabricEntityType.Builder.createMob(SlothBrown::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractSloth::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.7f, 0.6f));
    public static final EntityType<@NotNull SlothGray> SLOTH_GRAY = register(SlothGray.ID, FabricEntityType.Builder.createMob(SlothGray::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractSloth::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.7f, 0.6f));
    public static final EntityType<@NotNull Robin> ROBIN = register(Robin.ID, FabricEntityType.Builder.createMob(Robin::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractWinterBird::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.3f, 0.3f));
    public static final EntityType<@NotNull Cardinal> CARDINAL = register(Cardinal.ID, FabricEntityType.Builder.createMob(Cardinal::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractWinterBird::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.3f, 0.3f));
    public static final EntityType<@NotNull BlueTit> BLUE_TIT = register(BlueTit.ID, FabricEntityType.Builder.createMob(BlueTit::new, MobCategory.CREATURE, x -> x.defaultAttributes(AbstractWinterBird::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.3f, 0.3f));
    public static final EntityType<@NotNull Partridge> PARTRIDGE = register(Partridge.ID, FabricEntityType.Builder.createMob(Partridge::new, MobCategory.CREATURE, x -> x.defaultAttributes(Partridge::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.4f, 0.4f));
    public static final EntityType<@NotNull Raven> RAVEN = register(Raven.ID, FabricEntityType.Builder.createMob(Raven::new, MobCategory.CREATURE, x -> x.defaultAttributes(Raven::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 0.7f));
    public static final EntityType<@NotNull Hedgehog> HEDGEHOG = register(Hedgehog.ID, FabricEntityType.Builder.createMob(Hedgehog::new, MobCategory.CREATURE, x -> x.defaultAttributes(Hedgehog::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.4f, 0.3f));
    public static final EntityType<@NotNull HedgehogBaby> HEDGEHOG_BABY = register(HedgehogBaby.ID, FabricEntityType.Builder.createMob(HedgehogBaby::new, MobCategory.CREATURE, x -> x.defaultAttributes(HedgehogBaby::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.25f, 0.2f));
    public static final EntityType<@NotNull Reindeer> REINDEER = register(Reindeer.ID, FabricEntityType.Builder.createMob(Reindeer::new, MobCategory.CREATURE, x -> x.defaultAttributes(Reindeer::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(1.0f, 1.4f));
    public static final EntityType<@NotNull ReindeerBaby> REINDEER_BABY = register(ReindeerBaby.ID, FabricEntityType.Builder.createMob(ReindeerBaby::new, MobCategory.CREATURE, x -> x.defaultAttributes(ReindeerBaby::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 0.85f));
    public static final EntityType<@NotNull FennecFox> FENNEC_FOX = register(FennecFox.ID, FabricEntityType.Builder.createMob(FennecFox::new, MobCategory.CREATURE, x -> x.defaultAttributes(FennecFox::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.4f));
    public static final EntityType<@NotNull FennecFoxBaby> FENNEC_FOX_BABY = register(FennecFoxBaby.ID, FabricEntityType.Builder.createMob(FennecFoxBaby::new, MobCategory.CREATURE, x -> x.defaultAttributes(FennecFoxBaby::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.3f, 0.25f));

    public static final EntityType<@NotNull Giraffe> GIRAFFE = register(Giraffe.ID, FabricEntityType.Builder.createMob(Giraffe::new, MobCategory.CREATURE, x -> x.defaultAttributes(Giraffe::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(1.5f, 3.5f));
    public static final EntityType<@NotNull GiraffeBaby> GIRAFFE_BABY = register(GiraffeBaby.ID, FabricEntityType.Builder.createMob(GiraffeBaby::new, MobCategory.CREATURE, x -> x.defaultAttributes(GiraffeBaby::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.8f, 1.8f));
    public static final EntityType<@NotNull Zebra> ZEBRA = register(Zebra.ID, FabricEntityType.Builder.createMob(Zebra::new, MobCategory.CREATURE, x -> x.defaultAttributes(Zebra::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(1.3f, 1.6f));
    public static final EntityType<@NotNull ZebraBaby> ZEBRA_BABY = register(ZebraBaby.ID, FabricEntityType.Builder.createMob(ZebraBaby::new, MobCategory.CREATURE, x -> x.defaultAttributes(ZebraBaby::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.7f, 0.9f));
    public static final EntityType<@NotNull Antelope> ANTELOPE = register(Antelope.ID, FabricEntityType.Builder.createMob(Antelope::new, MobCategory.CREATURE, x -> x.defaultAttributes(Antelope::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(1.0f, 1.3f));
    public static final EntityType<@NotNull AntelopeBaby> ANTELOPE_BABY = register(AntelopeBaby.ID, FabricEntityType.Builder.createMob(AntelopeBaby::new, MobCategory.CREATURE, x -> x.defaultAttributes(AntelopeBaby::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.7f));
    public static final EntityType<@NotNull Tiger> TIGER = register(Tiger.ID, FabricEntityType.Builder.createMob(Tiger::new, MobCategory.CREATURE, x -> x.defaultAttributes(Tiger::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(1.3f, 0.9f));
    public static final EntityType<@NotNull TigerBaby> TIGER_BABY = register(TigerBaby.ID, FabricEntityType.Builder.createMob(TigerBaby::new, MobCategory.CREATURE, x -> x.defaultAttributes(TigerBaby::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.7f, 0.5f));
    //public static final EntityType<@NotNull Otter> OTTER = register(Otter.ID, FabricEntityType.Builder.createMob(Otter::new, MobCategory.CREATURE, x -> x.defaultAttributes(Otter::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.6f, 0.4f));
    public static final EntityType<@NotNull IcePenguin> ICE_PENGUIN = register(IcePenguin.ID, FabricEntityType.Builder.createMob(IcePenguin::new, MobCategory.CREATURE, x -> x.defaultAttributes(IcePenguin::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.5f, 0.8f));
    public static final EntityType<@NotNull IcePenguinBaby> ICE_PENGUIN_BABY = register(IcePenguinBaby.ID, FabricEntityType.Builder.createMob(IcePenguinBaby::new, MobCategory.CREATURE, x -> x.defaultAttributes(IcePenguinBaby::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.3f, 0.4f));

    //public static final EntityType<@NotNull Firemoth> FIREMOTH = register(Firemoth.ID, FabricEntityType.Builder.createMob(Firemoth::new, MobCategory.AMBIENT, x -> x.defaultAttributes(Firemoth::createAttributes).spawnPlacement(SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING, Firemoth::checkFiremothSpawnRules)).sized(0.5f, 0.5f));

    //public static final EntityType<@NotNull Butterfly> BUTTERFLY = register(Butterfly.ID, FabricEntityType.Builder.createMob(Butterfly::new, MobCategory.AMBIENT, x -> x.defaultAttributes(Butterfly::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Butterfly::checkButterflySpawnRules)).sized(0.25f, 0.25f));

    //public static final EntityType<@NotNull LargeButterfly> EMPEROR_BUTTERFLY = register(Util.id("emperor_butterfly"), FabricEntityType.Builder.createMob(LargeButterfly::new, MobCategory.AMBIENT, x -> x.defaultAttributes(LargeButterfly::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, LargeButterfly::checkLargeButterflySpawnRules)).sized(0.6f, 0.6f));

    //public static final EntityType<@NotNull Snake> SNAKE = register(Snake.ID, FabricEntityType.Builder.createMob(Snake::new, MobCategory.CREATURE, x -> x.defaultAttributes(Snake::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnRules::checkNaturalSpawnRules)).sized(0.9f, 0.4f));

    //public static final EntityType<@NotNull Sculkling> SCULKLING = register(Sculkling.ID, FabricEntityType.Builder.createMob(Sculkling::new, MobCategory.MONSTER, x -> x.defaultAttributes(Sculkling::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Sculkling::checkSculklingSpawnRules)).sized(0.5f, 0.9f));

    //public static final EntityType<@NotNull Showmaster> SHOWMASTER = register(Showmaster.ID, FabricEntityType.Builder.createMob(Showmaster::new, MobCategory.MONSTER, x -> x.defaultAttributes(Showmaster::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Showmaster::checkMobSpawnRules)).sized(0.7f, 1.8f));

    //public static final EntityType<@NotNull Iceologer> ICEOLOGER = register(Iceologer.ID, FabricEntityType.Builder.createMob(Iceologer::new, MobCategory.MONSTER, x -> x.defaultAttributes(Iceologer::createAttributes).spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Iceologer::checkIceologerSpawnRules)).sized(0.7f, 1.8f));

    //public static final EntityType<@NotNull IceSpike> ICE_SPIKE = register(IceSpike.ID, EntityType.Builder.of(IceSpike::new, MobCategory.MISC).sized(1.f, 2.f));

    //public static final EntityType<@NotNull IceSpikeSmall> ICE_SPIKE_SMALL = register(IceSpikeSmall.ID, EntityType.Builder.of(IceSpikeSmall::new, MobCategory.MISC).sized(1.2f, 0.8f));

    //public static final EntityType<@NotNull IceCluster> ICE_CLUSTER = register(IceCluster.ID, EntityType.Builder.of(IceCluster::new, MobCategory.MISC).sized(2, 1));

    private static <T extends Entity> EntityType<@NotNull T> register(Identifier id, EntityType.Builder<@NotNull T> builder) {
        EntityType<@NotNull T> type = builder.build(ResourceKey.create(Registries.ENTITY_TYPE, id));
        PolymerEntityUtils.registerType(type);

        return Registry.register(BuiltInRegistries.ENTITY_TYPE, id, type);
    }

    public static void registerMobs(RegistryAccess.Frozen layeredRegistryAccess) {
        var res = ConfiguredSpawn.CODEC.codec().listOf().decode(RegistryOps.create(JsonOps.INSTANCE, layeredRegistryAccess), ModConfig.getInstance().spawnsJson);
        res.ifError(x -> TomsMobs.LOGGER.info("Could not decode spawn data! {}", x.message()));

        if (res.hasResultOrPartial()) {
            var list = res.getPartialOrThrow().getFirst();
            SPAWN_ENTRIES.clear();
            for (ConfiguredSpawn config : list) {
                // Resolve biome IDs and tag keys lazily inside the predicate so tags are available
                List<ResourceKey<Biome>> directBiomes = new ArrayList<>();
                List<TagKey<Biome>> tagKeys = new ArrayList<>();
                for (String biome : config.biomes()) {
                    if (!biome.startsWith("#")) {
                        directBiomes.add(ResourceKey.create(Registries.BIOME, Identifier.parse(biome)));
                    } else {
                        tagKeys.add(TagKey.create(Registries.BIOME, Identifier.parse(biome.substring(1))));
                    }
                }

                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(config.mob());
                EntityType<?> babyType = null;
                if (config.baby() != null && !config.baby().isEmpty()) {
                    babyType = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(config.baby()));
                }
                SPAWN_ENTRIES.add(new SpawnEntry(entityType, directBiomes, tagKeys, config.weight(), config.minGroup(), config.maxGroup(), config.maxNearby(), config.category(), babyType, config.babyChance()));

                TomsMobs.LOGGER.info("Spawn registered: {} ({} biomes, {} tags, weight {})", config.mob(), directBiomes.size(), tagKeys.size(), config.weight());
                BiomeHelper.addSpawn(entityType, config.weight(), config.minGroup(), config.maxGroup(), context -> {
                    for (ResourceKey<Biome> key : directBiomes) {
                        if (context.getBiomeKey().equals(key)) return true;
                    }
                    for (TagKey<Biome> tag : tagKeys) {
                        if (context.hasTag(tag)) return true;
                    }
                    return false;
                });
            }
            TomsMobs.LOGGER.info("Tom's Mobs: Registered {} spawn entries", list.size());
        }
    }

    public static void registerContent() {
        // === All non-jellyfish mobs disabled for testing ===
        //addSpawnEgg(PENGUIN, Items.POLAR_BEAR_SPAWN_EGG);
        //addSpawnEggModeled(ELEPHANT, Util.id("elephant_spawn_egg"));
        //addSpawnEgg(FIREMOTH, Items.PARROT_SPAWN_EGG);
        //addSpawnEgg(SEAGULL, Items.CAT_SPAWN_EGG);
        //addSpawnEgg(BUTTERFLY, Items.ENDER_DRAGON_SPAWN_EGG);
        //addSpawnEgg(EMPEROR_BUTTERFLY, Items.ENDER_DRAGON_SPAWN_EGG);
        //addSpawnEgg(POSSUM, Items.CAMEL_SPAWN_EGG);
        //addSpawnEggModeled(CAPYBARA, Util.id("capybara_spawn_egg"));
        //addSpawnEgg(MANTARAY, Items.WARDEN_SPAWN_EGG);
        //addSpawnEgg(TUNA, Items.COD_SPAWN_EGG);
        //addSpawnEgg(LOBSTER, Items.PARROT_SPAWN_EGG);
        //addSpawnEgg(SCULKLING, Items.WARDEN_SPAWN_EGG);
        //addSpawnEggModeled(SNAKE, Util.id("snake_spawn_egg"));
        //addSpawnEgg(SHOWMASTER, Items.ENDERMITE_SPAWN_EGG);
        //addSpawnEgg(ICEOLOGER, Items.VEX_SPAWN_EGG);

        // === Jellyfish ===
        addSpawnEgg(JELLYFISH_BLUE, Items.SALMON_SPAWN_EGG);
        addSpawnEgg(JELLYFISH_GOLDEN, Items.SALMON_SPAWN_EGG);
        addSpawnEgg(JELLYFISH_FUSION, Items.SALMON_SPAWN_EGG);
        addSpawnEgg(JELLYFISH_ORANGE, Items.SALMON_SPAWN_EGG);
        addSpawnEgg(JELLYFISH_PINK, Items.SALMON_SPAWN_EGG);
        addSpawnEgg(JELLYFISH_WHITE, Items.SALMON_SPAWN_EGG);

        // === Seals ===
        addSpawnEgg(SEAL_ARCTIC, Items.POLAR_BEAR_SPAWN_EGG);
        addSpawnEgg(SEAL_HARBOR, Items.POLAR_BEAR_SPAWN_EGG);
        addSpawnEgg(SEAL_MONK, Items.POLAR_BEAR_SPAWN_EGG);

        // === Crabs ===
        addSpawnEgg(CRAB_BLUE, Items.PARROT_SPAWN_EGG);
        addSpawnEgg(CRAB_ORANGE, Items.PARROT_SPAWN_EGG);
        addSpawnEgg(CRAB_RED, Items.PARROT_SPAWN_EGG);

        // === Owls ===
        addSpawnEgg(OWL_BARN, Items.CAT_SPAWN_EGG);
        addSpawnEgg(OWL_HORNED, Items.CAT_SPAWN_EGG);
        addSpawnEgg(OWL_LONG_EARED, Items.CAT_SPAWN_EGG);
        addSpawnEgg(OWL_SNOWY, Items.CAT_SPAWN_EGG);

        // === Raccoon ===
        addSpawnEgg(RACCOON, Items.FOX_SPAWN_EGG);
        addSpawnEgg(RACCOON_BABY, Items.FOX_SPAWN_EGG);

        // === Red Panda ===
        addSpawnEgg(RED_PANDA, Items.FOX_SPAWN_EGG);
        addSpawnEgg(RED_PANDA_BABY, Items.FOX_SPAWN_EGG);

        // === Meerkat ===
        addSpawnEgg(MEERKAT, Items.RABBIT_SPAWN_EGG);
        addSpawnEgg(MEERKAT_BABY, Items.RABBIT_SPAWN_EGG);

        // === Squirrels ===
        addSpawnEgg(SQUIRREL_BROWN, Items.RABBIT_SPAWN_EGG);
        addSpawnEgg(SQUIRREL_GRAY, Items.RABBIT_SPAWN_EGG);
        addSpawnEgg(SQUIRREL_RED, Items.RABBIT_SPAWN_EGG);

        // === Seahorses ===
        addSpawnEgg(SEAHORSE_BLACK, Items.TROPICAL_FISH_SPAWN_EGG);
        addSpawnEgg(SEAHORSE_BLUE, Items.TROPICAL_FISH_SPAWN_EGG);
        addSpawnEgg(SEAHORSE_GREEN, Items.TROPICAL_FISH_SPAWN_EGG);
        addSpawnEgg(SEAHORSE_PURPLE, Items.TROPICAL_FISH_SPAWN_EGG);
        addSpawnEgg(SEAHORSE_RED, Items.TROPICAL_FISH_SPAWN_EGG);

        addSpawnEgg(NPC_BLACKSMITH, Items.VILLAGER_SPAWN_EGG);
        addSpawnEgg(NPC_BLACKSMITH_2, Items.VILLAGER_SPAWN_EGG);
        addSpawnEgg(NPC_BLACKSMITH_3, Items.VILLAGER_SPAWN_EGG);
        addSpawnEgg(NPC_BLACKSMITH_4, Items.VILLAGER_SPAWN_EGG);
        addSpawnEgg(NPC_BLACKSMITH_5, Items.VILLAGER_SPAWN_EGG);
        addSpawnEgg(NPC_GUARD, Items.VILLAGER_SPAWN_EGG);
        addSpawnEgg(NPC_GUARD_2, Items.VILLAGER_SPAWN_EGG);
        addSpawnEgg(NPC_LIBRARIAN, Items.VILLAGER_SPAWN_EGG);
        addSpawnEgg(NPC_LIBRARIAN_2, Items.VILLAGER_SPAWN_EGG);
        addSpawnEgg(NPC_LIBRARIAN_3, Items.VILLAGER_SPAWN_EGG);
        addSpawnEgg(NPC_LIBRARIAN_4, Items.VILLAGER_SPAWN_EGG);
        addSpawnEgg(NPC_LIBRARIAN_5, Items.VILLAGER_SPAWN_EGG);
        addSpawnEgg(NPC_LIBRARIAN_6, Items.VILLAGER_SPAWN_EGG);
        addSpawnEgg(NPC_LIBRARIAN_7, Items.VILLAGER_SPAWN_EGG);
        addSpawnEgg(NPC_FARMER, Items.VILLAGER_SPAWN_EGG);
        addSpawnEgg(NPC_FARMER_2, Items.VILLAGER_SPAWN_EGG);
        addSpawnEgg(NPC_FARMER_3, Items.VILLAGER_SPAWN_EGG);
        addSpawnEgg(NPC_FARMER_4, Items.VILLAGER_SPAWN_EGG);
        addSpawnEgg(NPC_FARMER_5, Items.VILLAGER_SPAWN_EGG);

        addSpawnEgg(KOI_WHITE, Items.TROPICAL_FISH_SPAWN_EGG);
        addSpawnEgg(KOI_ORANGE, Items.TROPICAL_FISH_SPAWN_EGG);
        addSpawnEgg(KOI_RED, Items.TROPICAL_FISH_SPAWN_EGG);
        addSpawnEgg(KOI_GOLD, Items.TROPICAL_FISH_SPAWN_EGG);

        addSpawnEgg(DUCK_BROWN, Items.CHICKEN_SPAWN_EGG);
        addSpawnEgg(DUCK_MALLARD, Items.CHICKEN_SPAWN_EGG);
        addSpawnEgg(DUCK_WHITE, Items.CHICKEN_SPAWN_EGG);
        addSpawnEgg(DUCKLING, Items.CHICKEN_SPAWN_EGG);
        addSpawnEgg(DOG_BULLDOG_ENGLISH, Items.WOLF_SPAWN_EGG);
        addSpawnEgg(DOG_CORGI, Items.WOLF_SPAWN_EGG);
        addSpawnEgg(DOG_DALMATION, Items.WOLF_SPAWN_EGG);
        addSpawnEgg(DOG_FINNISH_LAPPHUND_BLACK, Items.WOLF_SPAWN_EGG);
        addSpawnEgg(DOG_FINNISH_LAPPHUND_GINGER, Items.WOLF_SPAWN_EGG);
        addSpawnEgg(DOG_FINNISH_LAPPHUND_WHITE, Items.WOLF_SPAWN_EGG);
        addSpawnEgg(DOG_GERMAN_SHEPHERD, Items.WOLF_SPAWN_EGG);
        addSpawnEgg(DOG_HUSKY_BLACK, Items.WOLF_SPAWN_EGG);
        addSpawnEgg(DOG_HUSKY_BROWN, Items.WOLF_SPAWN_EGG);
        addSpawnEgg(DOG_LABRADOR_BLACK, Items.WOLF_SPAWN_EGG);
        addSpawnEgg(DOG_LABRADOR_CHOCOLATE, Items.WOLF_SPAWN_EGG);
        addSpawnEgg(DOG_LABRADOR_YELLOW, Items.WOLF_SPAWN_EGG);
        addSpawnEgg(DOG_POODLE_BLACK, Items.WOLF_SPAWN_EGG);
        addSpawnEgg(DOG_POODLE_BROWN, Items.WOLF_SPAWN_EGG);
        addSpawnEgg(DOG_POODLE_WHITE, Items.WOLF_SPAWN_EGG);
        addSpawnEgg(DOG_PUG_BLACK, Items.WOLF_SPAWN_EGG);
        addSpawnEgg(DOG_PUG_YELLOW, Items.WOLF_SPAWN_EGG);
        addSpawnEgg(CAT_BOMBAY, Items.CAT_SPAWN_EGG);
        addSpawnEgg(CAT_CHARTREUX, Items.CAT_SPAWN_EGG);
        addSpawnEgg(CAT_EGYPTIAN_MAU_BROWN, Items.CAT_SPAWN_EGG);
        addSpawnEgg(CAT_EGYPTIAN_MAU_GRAY, Items.CAT_SPAWN_EGG);
        addSpawnEgg(CAT_JAPANESE_BOBTAIL, Items.CAT_SPAWN_EGG);
        addSpawnEgg(CAT_MAINE_COON_BROWN, Items.CAT_SPAWN_EGG);
        addSpawnEgg(CAT_MAINE_COON_GRAY, Items.CAT_SPAWN_EGG);
        addSpawnEgg(CAT_ORANGE, Items.CAT_SPAWN_EGG);
        addSpawnEgg(CAT_SIAMESE, Items.CAT_SPAWN_EGG);
        addSpawnEgg(CAT_SPHYNX, Items.CAT_SPAWN_EGG);
        addSpawnEgg(CAT_STRAY, Items.CAT_SPAWN_EGG);
        addSpawnEgg(CAT_TABBY, Items.CAT_SPAWN_EGG);
        addSpawnEgg(CAT_TUXEDO, Items.CAT_SPAWN_EGG);
        addSpawnEgg(SLOTH_BROWN, Items.PANDA_SPAWN_EGG);
        addSpawnEgg(SLOTH_GRAY, Items.PANDA_SPAWN_EGG);
        addSpawnEgg(ROBIN, Items.PARROT_SPAWN_EGG);
        addSpawnEgg(CARDINAL, Items.PARROT_SPAWN_EGG);
        addSpawnEgg(BLUE_TIT, Items.PARROT_SPAWN_EGG);
        addSpawnEgg(PARTRIDGE, Items.CHICKEN_SPAWN_EGG);
        addSpawnEgg(RAVEN, Items.PARROT_SPAWN_EGG);
        addSpawnEgg(HEDGEHOG, Items.RABBIT_SPAWN_EGG);
        addSpawnEgg(HEDGEHOG_BABY, Items.RABBIT_SPAWN_EGG);
        addSpawnEgg(REINDEER, Items.COW_SPAWN_EGG);
        addSpawnEgg(REINDEER_BABY, Items.COW_SPAWN_EGG);
        addSpawnEgg(FENNEC_FOX, Items.FOX_SPAWN_EGG);
        addSpawnEgg(FENNEC_FOX_BABY, Items.FOX_SPAWN_EGG);
        addSpawnEgg(GIRAFFE, Items.COW_SPAWN_EGG);
        addSpawnEgg(GIRAFFE_BABY, Items.COW_SPAWN_EGG);
        addSpawnEgg(ZEBRA, Items.HORSE_SPAWN_EGG);
        addSpawnEgg(ZEBRA_BABY, Items.HORSE_SPAWN_EGG);
        addSpawnEgg(ANTELOPE, Items.COW_SPAWN_EGG);
        addSpawnEgg(ANTELOPE_BABY, Items.COW_SPAWN_EGG);
        addSpawnEgg(TIGER, Items.OCELOT_SPAWN_EGG);
        addSpawnEgg(TIGER_BABY, Items.OCELOT_SPAWN_EGG);
        //addSpawnEgg(OTTER, Items.DOLPHIN_SPAWN_EGG);
        addSpawnEgg(ICE_PENGUIN, Items.POLAR_BEAR_SPAWN_EGG);
        addSpawnEgg(ICE_PENGUIN_BABY, Items.POLAR_BEAR_SPAWN_EGG);

        PolymerCreativeModeTabUtils.registerPolymerCreativeModeTab(Util.id("spawn-eggs"), ITEM_GROUP);
    }

    private static void addSpawnEggModeled(EntityType<? extends @NotNull Mob> type, Identifier model) {
        register(Util.id(EntityType.getKey(type).getPath() + "_spawn_egg"), properties -> new TexturedPolymerSpawnEggItem(type, properties, model));
    }

    private static void addSpawnEgg(EntityType<? extends @NotNull Mob> type, Item vanillaItem) {
        register(Util.id(EntityType.getKey(type).getPath() + "_spawn_egg"), properties -> new VanillaPolymerSpawnEggItem(type, vanillaItem, properties));
    }

    static public <T extends Item> void register(Identifier identifier, Function<Item.Properties, T> function) {
        var x = function.apply(new Item.Properties().stacksTo(64).setId(ResourceKey.create(Registries.ITEM, identifier)));
        Registry.register(BuiltInRegistries.ITEM, identifier, x);
        SPAWN_EGGS.putIfAbsent(identifier, x);
    }

    public static final Object2ObjectOpenHashMap<Identifier, Item> SPAWN_EGGS = new Object2ObjectOpenHashMap<>();
    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, -1).title(Component.literal("Toms Mobs").withStyle(ChatFormatting.DARK_GREEN)).icon(Items.BAT_SPAWN_EGG::getDefaultInstance).displayItems((parameters, output) -> SPAWN_EGGS.values().forEach(output::accept)).build();
}
