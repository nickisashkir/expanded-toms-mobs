package de.tomalbrc.toms_mobs.entity.passive.squirrel;

import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SquirrelGray extends AbstractSquirrel {
    public static final Identifier ID = Util.id("squirrel_gray");
    public static final Model MODEL = Util.loadBbModel(ID);

    public SquirrelGray(EntityType<? extends @NotNull Animal> type, Level level) {
        super(type, level, MODEL);
    }
}
