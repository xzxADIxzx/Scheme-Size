package scheme.ai;

import arc.func.Cons;
import arc.math.geom.Position;
import mindustry.entities.units.AIController;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Player;

import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class GammaAI extends AIController { // TODO: bt change size

    public MovementType move = MovementType.circle;
    public BuildType build = BuildType.none;

    public Player target;
    public Position cache;

    public float speed = 1f; // TODO: ui
    public float range = 80f; // temp values and speed is imposible

    @Override
    public void updateUnit() {
        if (target == null || !target.isAdded()) return;

        move.update.get(this);
        build.update.get(this);
        player.boosting = target.boosting;
    }

    public void cache() {
        target = ai.players.get();
        cache = target == player ? player.tileOn() : target;
    }

    public void tmp1(){
        move = MovementType.circle;
    }public void tmp2(){
        move = MovementType.follow;
    }public void tmp3(){
        build = BuildType.help;
    }public void tmp4(){
        build = BuildType.none;
    }public void tmp5(){
        build = BuildType.block;
    }

    public enum MovementType {
        circle(ai -> {
            ai.circle(ai.cache, ai.range);
            ai.faceMovement();
            ai.stopShooting();
        }),
        follow(ai -> {
            ai.moveTo(ai.cache, ai.range);
            ai.unit.aim(ai.target.mouseX, ai.target.mouseY);
            ai.unit.controlWeapons(true, ai.target.shooting);
        });

        public final Cons<GammaAI> update;

        private MovementType(Cons<GammaAI> update) {
            this.update = update;
        }
    }

    public enum BuildType {
        none(ai -> {}),
        help(ai -> {
            if (ai.target.unit().plans.isEmpty()) return;
            ai.unit.clearBuilding();
            ai.unit.addBuild(ai.target.unit().plans.first());
        }),
        block(ai -> {
            if (ai.target.unit().plans.isEmpty()) return;
            ai.target.unit().plans.each(plan -> {
                if (!plan.breaking) ai.unit.addBuild(new BuildPlan(plan.x, plan.y));
            });
        });

        public final Cons<GammaAI> update;

        private BuildType(Cons<GammaAI> update) {
            this.update = update;
        }
    }
}