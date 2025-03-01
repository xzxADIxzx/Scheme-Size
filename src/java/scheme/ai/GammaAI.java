package scheme.ai;

import arc.func.Cons;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.style.Drawable;
import arc.struct.Seq;
import mindustry.entities.units.AIController;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Teams.BlockPlan;
import mindustry.gen.Icon;
import mindustry.gen.Player;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock.ConstructBuild;

import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class GammaAI extends AIController {

    public static final String tooltip = null;

    public static Updater move = Updater.none;
    public static Updater build = Updater.none;
    public static float range = 80f;
    public static float speed = 1f;

    public Player target;
    public Position cache;
    public Position aim;

    @Override
    public void updateUnit() {
        if (target == null || !target.isAdded()) return;

        move.update.get(this);
        build.update.get(this);

        player.boosting = target.boosting;
        aim = new Vec2(target.mouseX, target.mouseY);
    }

    public void draw() {
        if (target != null && target != player) render.drawPlans(target.unit(), build != Updater.destroy);
    }

    public void block(Tile tile, boolean breaking) {
        var build = tile.build;
        unit.addBuild(breaking
                ? new BuildPlan(tile.x, tile.y, build.rotation, build instanceof ConstructBuild c ? c.previous : build.block)
                : new BuildPlan(tile.x, tile.y));
    }

    public void cache() {
        target = ai.players.get();
        cache = target == player ? player.tileOn() : target;
    }

    public float speed() {
        return unit.speed() * speed / 100f;
    }

    public enum Updater {
        none(Icon.line, ai -> {}),
        circle(Icon.commandRally, ai -> {
            ai.circle(ai.cache, range, ai.speed());
            ai.faceMovement();
            ai.stopShooting();
        }),
        cursor(Icon.diagonal, ai -> moveTo(ai, ai.aim)),
        follow(Icon.resize, ai -> moveTo(ai, ai.cache)),
        help(Icon.add, ai -> {
            if (ai.target.unit().plans.isEmpty() || !ai.target.unit().updateBuilding) return;
            ai.unit.addBuild(ai.target.unit().buildPlan());
        }),
        destroy(Icon.hammer, ai -> {}), // works through events
        repair(Icon.wrench, ai -> {
            if (ai.target.team().data().plans.isEmpty() || !ai.target.unit().updateBuilding) return;
            BlockPlan plan = Seq.with(ai.target.team().data().plans).min(p -> ai.unit.dst(p.x * tilesize, p.y * tilesize));
            ai.unit.addBuild(new BuildPlan(plan.x, plan.y, plan.rotation, content.block(plan.block), plan.config));
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

        private static void moveTo(GammaAI ai, Position pos) {
            ai.moveTo(pos, range / 3f);
            ai.unit.vel(ai.unit.vel().limit(ai.speed()));
            if (ai.unit.moving()) ai.faceMovement();
            else ai.unit.lookAt(ai.aim);
            ai.unit.aim(ai.aim);
            ai.unit.controlWeapons(true, player.shooting = ai.target.shooting);
        }
    }
}
