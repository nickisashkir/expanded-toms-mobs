package de.tomalbrc.toms_mobs.entity.passive.npc;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
public class NpcLibrarian6 extends AbstractNpc {
    public static final Identifier ID = Util.id("npc_librarian_6");
    public static final Model MODEL = Util.loadBbModel(ID);
    public NpcLibrarian6(EntityType<? extends Animal> type, Level level) { super(type, level, MODEL); }
    @Override protected String getIdleAnimation() { return "clean1"; }
}
