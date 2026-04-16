package de.tomalbrc.toms_mobs.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;

import java.util.List;

public record SpawnEntry(
        EntityType<?> type,
        List<ResourceKey<Biome>> directBiomes,
        List<TagKey<Biome>> tagKeys,
        int weight,
        int minGroup,
        int maxGroup,
        int maxNearby,
        String category,
        EntityType<?> babyType,
        double babyChance
) {
    public boolean matchesBiome(net.minecraft.core.Holder<Biome> biomeHolder) {
        for (ResourceKey<Biome> key : directBiomes) {
            if (biomeHolder.is(key)) return true;
        }
        for (TagKey<Biome> tag : tagKeys) {
            if (biomeHolder.is(tag)) return true;
        }
        return false;
    }
}
