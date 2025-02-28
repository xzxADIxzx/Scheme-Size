package schema.tools;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.graphics.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/** Component that renders overlay elements: command and control mode, blocks configuration, objectives and so on. */
public class Overlay {

    /** Distance from which spawners are visible. */
    public static final float spawnerMargin = 16f * tilesize;
    /** Interpolation function applied to alpha. */
    public static final Interp i = new Interp.PowIn(9f);

    /** Alpha value of certain overlay elements. */
    public float fade;

    /** Draws the elements of both vanilla and schema overlay */
    public void draw() {
        state.rules.objectives.eachRunning(o -> {
            for (var marker : o.markers) marker.draw();
        });

        insys.drawOverlay();

        if (/* TODO units.isCoreUnit */ player.unit().type == mindustry.content.UnitTypes.gamma) {
            var m = input.mouseWorld();
            var x = Mathf.round(m.x + 4f, tilesize) - 4f;
            var y = Mathf.round(m.y + 4f, tilesize) - 4f;

            capture(.4f, 0f);
            Lines.stroke(6f, Pal.accent);
            drawRuler(x, y);

            render();
            Lines.stroke(1f, Pal.accent);
            drawRuler(x, y);
        }

        if (state.hasSpawns()) {
            Lines.stroke(2f);
            Draw.color(Pal.remove, Pal.lightishGray, Mathf.absin(4f, 1f));

            capture(4f);
            spawner.getSpawns().each(s -> s.within(player, state.rules.dropZoneRadius + spawnerMargin), s -> {

                Draw.alpha(1f - i.apply((player.dst(s) - state.rules.dropZoneRadius) / spawnerMargin));
                Lines.dashCircle(s.worldx(), s.worldy(), state.rules.dropZoneRadius);
            });
            render();
        }

        if (config.shown()) config.selected().drawConfigure();

        if (insys.block == null && !scene.hasMouse()) {
            var build = insys.selectedBuilding();
            if (build != null && build.team == player.team()) {

                build.drawSelect();

                if (build.block.drawDisabled && !build.enabled) build.drawDisabled();
            }
        }

        fade = Mathf.lerpDelta(fade, insys.block != null ? 1f : 0f, .06f);
        if (fade < .004f) return;

        Lines.stroke(fade * 2f);
        capture(4f);

        if (state.rules.polygonCoreProtection) {
            // TODO
        } else {
            state.teams.eachEnemyCore(player.team(), c -> {

                if (!camera.bounds(Tmp.r1).overlaps(Tmp.r2.setCentered(c.x, c.y, state.rules.enemyCoreBuildRadius * 2f))) return;

                Draw.color(Pal.accent, c.team.color, .5f + Mathf.absin(4f, .5f));
                Lines.circle(c.x, c.y, state.rules.enemyCoreBuildRadius);
            });
        }

        render();
    }

    /** Draws cursor ruler at the given position */
    private void drawRuler(float x, float y) {
        var r = camera.bounds(Tmp.r1);

        Lines.line(x, r.y, x, r.y + r.height);
        Lines.line(r.x, y, r.x + r.width, y);
        x += tilesize;
        y += tilesize;
        Lines.line(x, r.y, x, r.y + r.height);
        Lines.line(r.x, y, r.x + r.width, y);
    }

    // region bloom

    /** Captures subsequent draw calls. */
    public void capture(float... intensity) {
        if (renderer.bloom != null) {
            renderer.bloom.capture();

            if (intensity.length > 0) renderer.bloom.setBloomIntensity(intensity[0]);
            if (intensity.length > 1) renderer.bloom.setOriginalIntensity(intensity[1]);
        }
    }

    /** Renders the {@link #capture(float) captured draw calls} with bloom effect. */
    public void render() {
        if (renderer.bloom != null) {
            renderer.bloom.render();
            renderer.bloom.setOriginalIntensity(1f);
        }
    }

    // endregion
    // region agent

    /** Returns the agent of this component. */
    public Agent getAgent() { return new Agent(); }

    /** Agent that redirects method calls from the original component to the new one. */
    public class Agent extends OverlayRenderer {

        @Override
        public void drawBottom() { insys.drawPlans(); }

        @Override
        public void drawTop() { draw(); }
    }

    // endregion
}
