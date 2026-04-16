package de.tomalbrc.toms_mobs.entity.passive.owl;

import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class OwlBarn extends AbstractOwl {
    public static final Identifier ID = Util.id("owl_barn");
    public static final Model MODEL = Util.loadBbModel(ID);

    public OwlBarn(EntityType<? extends @NotNull Animal> type, Level level) {
        super(type, level, MODEL);
    }
}
