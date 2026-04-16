package de.tomalbrc.toms_mobs.entity.passive.cat;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
public class CatTuxedo extends AbstractCat {
    public static final Identifier ID = Util.id("cat_tuxedo");
    public static final Model MODEL = Util.loadBbModel(ID);
    public CatTuxedo(EntityType<? extends TamableAnimal> type, Level level) { super(type, level, MODEL); }
}
