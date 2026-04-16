package de.tomalbrc.toms_mobs.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.tomalbrc.toms_mobs.util.Util;
import net.minecraft.resources.Identifier;

import java.util.List;

public record ConfiguredSpawn(Identifier mob, List<String> biomes, int weight, int minGroup, int maxGroup, int maxNearby, String category, String baby, double babyChance) {
    public static final Identifier ID = Util.id("spawn-data");
    public static final MapCodec<ConfiguredSpawn> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            Identifier.CODEC.fieldOf("mob").forGetter(ConfiguredSpawn::mob),
                            Codec.STRING.listOf().fieldOf("biomes").forGetter(ConfiguredSpawn::biomes),
                            Codec.INT.fieldOf("weight").forGetter(ConfiguredSpawn::weight),
                            Codec.INT.fieldOf("min-group").forGetter(ConfiguredSpawn::minGroup),
                            Codec.INT.fieldOf("max-group").forGetter(ConfiguredSpawn::maxGroup),
                            Codec.INT.optionalFieldOf("max-nearby", -1).forGetter(ConfiguredSpawn::maxNearby),
                            Codec.STRING.optionalFieldOf("category", "").forGetter(ConfiguredSpawn::category),
                            Codec.STRING.optionalFieldOf("baby", "").forGetter(ConfiguredSpawn::baby),
                            Codec.DOUBLE.optionalFieldOf("baby-chance", 0.0).forGetter(ConfiguredSpawn::babyChance))
                    .apply(builder, ConfiguredSpawn::new)
    );
}