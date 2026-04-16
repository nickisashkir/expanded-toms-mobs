package de.tomalbrc.toms_mobs.entity.passive.cat;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
public class CatMaineCoonGray extends AbstractCat {
    public static final Identifier ID = Util.id("cat_maine_coon_gray");
    public static final Model MODEL = Util.loadBbModel(ID);
    public CatMaineCoonGray(EntityType<? extends TamableAnimal> type, Level level) { super(type, level, MODEL); }
}
