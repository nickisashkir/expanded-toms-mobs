package de.tomalbrc.toms_mobs.entity.passive.seahorse;

import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SeahorseBlue extends AbstractSeahorse {
    public static final Identifier ID = Util.id("seahorse_blue");
    public static final Model MODEL = Util.loadBbModel(ID);

    public SeahorseBlue(EntityType<? extends @NotNull AbstractFish> type, Level level) {
        super(type, level, MODEL);
    }
}
