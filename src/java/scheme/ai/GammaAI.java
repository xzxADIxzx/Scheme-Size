package scheme.ai;

import arc.func.Cons;
import arc.math.geom.Position;
import arc.scene.style.Drawable;
import mindustry.entities.units.AIController;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Icon;
import mindustry.gen.Player;
import scheme.moded.ModedBinding;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class GammaAI extends AIController {

    public static final String tooltip = bundle.format("gamma.tooltip", keybind());

    public static Updater move = Updater.none;
    public static Updater build = Updater.none;
    public static float range = 80f;

    public Player target;
    public Position cache;

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

    /** Key to press to disable ai. */
    public static String keybind() {
        return keybinds.get(ModedBinding.alternative).key.toString() + " + " + keybinds.get(ModedBinding.toggle_ai).key.toString();
    }

    public enum Updater {
        none(Icon.line, ai -> {}),
        circle(Icon.commandRally, ai -> {
            ai.circle(ai.cache, range);
            ai.faceMovement();
            ai.stopShooting();
        }),
        follow(Icon.resize, ai -> {
            ai.moveTo(ai.cache, range / 2f);
            ai.unit.aim(ai.target.mouseX, ai.target.mouseY);
            ai.unit.controlWeapons(true, ai.target.shooting);
        }),
        help(Icon.add, ai -> {
            if (ai.target.unit().plans.isEmpty()) return;
            ai.unit.clearBuilding();
            ai.unit.addBuild(ai.target.unit().plans.first());
        }),
        destroy(Icon.hammer, ai -> {
            if (ai.target.unit().plans.isEmpty()) return;
            ai.target.unit().plans.each(plan -> { // TODO: works bad
                if (!plan.breaking) ai.unit.addBuild(new BuildPlan(plan.x, plan.y));
            });
        });

        public final Drawable icon;
        public final Cons<GammaAI> update;

        private Updater(Drawable icon, Cons<GammaAI> update) {
            this.icon = icon;
            this.update = update;
        }

        public String tooltip() {
            return "@gamma." + name();
        }
    }
}