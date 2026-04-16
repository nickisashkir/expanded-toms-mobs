package de.tomalbrc.toms_mobs.entity.passive.npc;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
public class NpcFarmer extends AbstractNpc {
    public static final Identifier ID = Util.id("npc_farmer");
    public static final Model MODEL = Util.loadBbModel(ID);
    public NpcFarmer(EntityType<? extends Animal> type, Level level) { super(type, level, MODEL); }
    @Override protected String getIdleAnimation() { return "farm1"; }
}
