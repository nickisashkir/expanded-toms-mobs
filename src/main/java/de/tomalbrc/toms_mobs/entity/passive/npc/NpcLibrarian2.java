package de.tomalbrc.toms_mobs.entity.passive.npc;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
public class NpcLibrarian2 extends AbstractNpc {
    public static final Identifier ID = Util.id("npc_librarian_2");
    public static final Model MODEL = Util.loadBbModel(ID);
    private int fallTimer = 0;
    public NpcLibrarian2(EntityType<? extends Animal> type, Level level) { super(type, level, MODEL); }
    @Override public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount % 20 == 0) {
            if (suppressIdleAnimation) {
                fallTimer--;
                if (fallTimer <= 0) {
                    suppressIdleAnimation = false;
                    this.getHolder().getAnimator().pauseAnimation("fall");
                }
            } else if (this.random.nextInt(60) == 0) {
                suppressIdleAnimation = true;
                fallTimer = 4;
                this.getHolder().getAnimator().pauseAnimation("idle");
                this.getHolder().getAnimator().playAnimation("fall");
            }
        }
    }
}
