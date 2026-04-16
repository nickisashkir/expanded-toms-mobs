package de.tomalbrc.toms_mobs.entity.passive.dog;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
public class DogLabradorChocolate extends AbstractDog {
    public static final Identifier ID = Util.id("dog_labrador_chocolate");
    public static final Model MODEL = Util.loadBbModel(ID);
    public DogLabradorChocolate(EntityType<? extends TamableAnimal> type, Level level) { super(type, level, MODEL); }
}
