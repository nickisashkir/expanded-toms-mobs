package de.tomalbrc.toms_mobs.entity.passive.koi;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
public class KoiRed extends AbstractKoi {
    public static final Identifier ID = Util.id("koi_red");
    public static final Model MODEL = Util.loadBbModel(ID);
    public KoiRed(EntityType<? extends Animal> type, Level level) { super(type, level, MODEL); }
}
