package schema.tools;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.meta.*;
import schema.input.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/// Utility focused on overlay.
public class Overlay
{
    /// Distance from which spawners are visible.
    public static final float spawnerMargin = 16f * tilesize;
    /// Interpolation function applied to spawns.
    public static final Interp i = new Interp.PowIn(9f);

    /// Alpha values of certain overlay elements.
    public float fade;
    /// Visibility of certain overlay elements.
    public boolean ruler, borderless;

    public Overlay()
    {
        renderer.addEnvRenderer(Env.none, () -> Draw.draw(Layer.turret + 1f, () ->
        {
            if (Keybind.display_xray.down()) drawXray();
        }));
        renderer.addEnvRenderer(Env.none, () -> Draw.draw(Layer.power + 1f, () ->
        {
            if (settings.getBool("blockhealth", false)) drawBars();
        }));
    }

    // region draw

    /// Draws the fragments of both vanilla and schema overlays.
    public void draw()
    {
        if (ruler)
        {
            var r = camera.bounds(Tmp.r1);
            var m = input.mouseWorld();
            var x = Mathf.round(m.x + 4f, tilesize);
            var y = Mathf.round(m.y + 4f, tilesize);

            Lines.stroke(tilesize, Pal.accent);
            capture(1f, .2f);

            Lines.line(r.x, y, r.x + r.width, y);
            Lines.line(x, r.y, x, r.y + r.height);

            render();
            Draw.reset();
        }

        if (config.shown()) config.selected().drawConfigure();

        if (insys.block == null && !scene.hasMouse())
        {
            var build = insys.selectedBuilding();
            if (build != null)
            {
                config.draw(build);
                if (build.block.drawDisabled && !build.enabled) build.drawDisabled();
            }
        }

        fade = Mathf.lerpDelta(fade, insys.block != null ? 1f : 0f, .06f);
        if (fade > .004f)
        {
            Lines.stroke(fade * 2f);
            capture(4f);
    
            if (state.rules.polygonCoreProtection)
            {
                // TODO implement
            }
            else state.teams.eachEnemyCore(player.team(), c ->
            {
                var radius = state.rules.buildRadius(c.team);
                if (radius != 0f && camera.bounds(Tmp.r1).overlaps(Tmp.r2.setCentered(c.x, c.y, radius * 2f)))
                {
                    Draw.color(c.team.color, Pal.accent, Mathf.absin(4f, .4f));
                    Lines.circle(c.x, c.y, radius);
                }
            });
    
            render();
            Draw.reset();
        }
        if (state.hasSpawns())
        {
            Lines.stroke(2f);
            capture(4f);

            spawner.getSpawns().each(s -> fade > .004f || player.within(s, state.rules.dropZoneRadius + spawnerMargin), s ->
            {
                var fdst = fade > .004f ? fade : 1f - i.apply((player.dst(s) - state.rules.dropZoneRadius) / spawnerMargin);

                Draw.color(Pal.remove, Pal.lightishGray, Mathf.absin(4f, 1f));
                Draw.alpha(fdst);
                Lines.dashCircle(s.worldx(), s.worldy(), state.rules.dropZoneRadius);
            });

            render();
            Draw.reset();
        }
    }

    /// Draws floors above buildings to display underlying ores.
    public void drawXray()
    {
        builds.iterateBuilds(t -> t.getLinkedTiles(l ->
        {
            Draw.alpha(.8f);
            l.floor().drawBase(l);
        }));
    }

    /// Draws health bars using the style of [status][Building#drawStatus].
    public void drawBars()
    {
        Cons4<Float, Float, Float, Float> draw = (x, y, width, height) -> Fill.quad
        (
            x - width,                    y,
            x - width + Math.abs(height), y + height,
            x + width - Math.abs(height), y + height,
            x + width,                    y
        );
        builds.iterateBuilds(t ->
        {
            builds.healthBar(t.build, 2.5f, true, (radius, width, x, y) ->
            {
                Draw.color(Pal.gray);
                draw.get(x, y, width,  radius);
                draw.get(x, y, width, -radius);
            });
            builds.healthBar(t.build, 1.5f, false, (radius, width, x, y) ->
            {
                Draw.color(Pal.darkerGray);
                draw.get(x, y, width,  radius);
                draw.get(x, y, width, -radius);

                Draw.color(Pal.remove);
                float progress = 2f * width * (1f - t.build.healthf());
                float middle = 2f * (width - radius), l;

                l = Math.max(0, progress / radius);
                if (l < 1) Fill.quad
                (
                    x + width - radius,              y + radius,
                    x + width - radius * l,          y + radius * l,
                    x + width - radius * l,          y - radius * l,
                    x + width - radius,              y - radius
                );
                l = Math.max(0, (progress - radius) / middle);
                if (l < 1) Fill.quad
                (
                    x - width + radius,              y + radius,
                    x + width - radius - middle * l, y + radius,
                    x + width - radius - middle * l, y - radius,
                    x - width + radius,              y - radius
                );
                l = Math.max(0, (progress - radius - middle) / radius);
                if (l < 1) Fill.quad
                (
                    x - width,                       y,
                    x - width + radius * (1f - l),   y + radius * (1f - l),
                    x - width + radius * (1f - l),   y - radius * (1f - l),
                    x - width,                       y
                );
            });
        });
    }

    // endregion
    // region bloom

    /// Captures subsequent draw calls.
    public void capture(float... intensity)
    {
        if (renderer.bloom == null) return;

        if (intensity.length > 0) renderer.bloom.setBloomIntensity(intensity[0]);
        if (intensity.length > 1) renderer.bloom.setOriginalIntensity(intensity[1]);

        renderer.bloom.capture();
    }

    /// Renders the captured draw calls.
    public void render()
    {
        if (renderer.bloom == null) return;

        renderer.bloom.render();
        renderer.bloom.setOriginalIntensity(1f);
    }

    // endregion
    // region agent

    /// Creates a new agent.
    public Agent agent() { return new Agent(); }

    /// Agent redirecting method calls from the original component.
    public class Agent extends mindustry.graphics.OverlayRenderer
    {
        @Override
        public void drawBottom() { insys.drawPlans(); }
    
        @Override
        public void drawTop() { insys.drawOverlay(); draw(); }
    
        @Override
        public void checkApplySelection(Unit unit)
        {
            var fade = insys.fade(unit);
            if (fade > .01f)
            {
                var c = Draw.getMixColor();
                Draw.mixcol(c.a > .01f ? c.lerp(Pal.accent, fade) : Pal.accent, Math.max(c.a, fade));
            }
        }
    }

    // endregion
}
