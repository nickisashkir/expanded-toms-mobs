package de.tomalbrc.toms_mobs.entity.passive.npc;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.toms_mobs.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
public class NpcGuard extends AbstractNpc {
    public static final Identifier ID = Util.id("npc_guard");
    public static final Model MODEL = Util.loadBbModel(ID);
    private int actionTimer = 0;
    private String currentAction = "idle";
    public NpcGuard(EntityType<? extends Animal> type, Level level) { super(type, level, MODEL); }
    @Override protected String getWalkAnimation() { return "walk"; }
    @Override public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount % 20 == 0) {
            if (suppressIdleAnimation) {
                actionTimer--;
                if (actionTimer <= 0) {
                    suppressIdleAnimation = false;
                    this.getHolder().getAnimator().pauseAnimation(currentAction);
                    currentAction = "idle";
                }
            } else if (this.random.nextInt(40) == 0) {
                int pick = this.random.nextInt(3);
                if (pick == 0) {
                    currentAction = "salute";
                    actionTimer = 3;
                } else {
                    currentAction = "wave";
                    actionTimer = 2;
                }
                suppressIdleAnimation = true;
                this.getHolder().getAnimator().pauseAnimation("idle");
                this.getHolder().getAnimator().playAnimation(currentAction);
            }
        }
    }
}
