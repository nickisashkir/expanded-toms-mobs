package de.tomalbrc.toms_mobs.entity.passive.goose;

import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class GooseWhite extends AbstractGoose {
    public static final Identifier ID = Util.id("goose_white");
    public static final Model MODEL = Util.loadBbModel(ID);

    public GooseWhite(EntityType<? extends @NotNull Animal> type, Level level) { super(type, level, MODEL); }

    @Override
    protected net.minecraft.resources.Identifier getHeadItemId() {
        return net.minecraft.resources.Identifier.parse("filament:nm_head_goose_white");
    }
}
