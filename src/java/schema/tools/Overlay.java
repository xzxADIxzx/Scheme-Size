package schema.tools;

import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.graphics.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/** Component that renders overlay elements: command and control mode, blocks configuration, objectives and so on. */
public class Overlay {

    /** Distance from which spawners are visible. */
    public static final float spawnerMargin = 16f * tilesize;
    /** Interpolation function applied to alpha. */
    public static final Interp i = new Interp.PowIn(8f);

    /** Draws the elements of both vanilla and schema overlay */
    public void draw() {
        state.rules.objectives.eachRunning(o -> {
            for (var marker : o.markers) marker.draw();
        });

        if (config.shown()) config.selected().drawConfigure();
        insys.drawOverlay();

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

        if (insys.block == null && !scene.hasMouse()) {
            var build = insys.selectedBuilding();
            if (build != null && build.team == player.team()) {

                build.drawSelect();

                if (build.block.drawDisabled && !build.enabled) build.drawDisabled();
            }
        }
    }

    /** Captures subsequent draw calls. */
    public void capture(float intensity) {
        if (renderer.bloom != null) {
            renderer.bloom.setBloomIntensity(intensity);
            renderer.bloom.capture();
        }
    }

    /** Renders the {@link #capture(float) captured draw calls} with bloom effect. */
    public void render() {
        if (renderer.bloom != null) renderer.bloom.render();
    }

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
