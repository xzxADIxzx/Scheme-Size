package scheme.tools;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.blocks.defense.turrets.BaseTurret.BaseTurretBuild;
import mindustry.world.blocks.power.ImpactReactor;
import mindustry.world.blocks.power.NuclearReactor;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class RendererTools {

    public Rect bounds = new Rect();
    public boolean xray, grid, ruler, unitInfo, unitRadius, turretRadius, reactorRadius;

    public void draw() {
        camera.bounds(bounds); // do NOT use Tmp.r1 
        int xwidth = (int) (bounds.x + bounds.width), yheigth = (int) (bounds.y + bounds.height);

        if (xray) builds.each(bounds, tile -> tile.floor().drawBase(tile));

        if (grid) Draw.draw(Layer.blockUnder, () -> {
            Lines.stroke(1f, Pal.darkMetal);

            int sx = Mathf.round(bounds.x, tilesize) + 4;
            int sy = Mathf.round(bounds.y, tilesize) + 4;

            for (int x = sx; x < xwidth; x += tilesize)
                for (int y = sy - 2; y < yheigth; y += tilesize)
                    Lines.line(x, y, x, y + 4);

            for (int y = sy; y < yheigth; y += tilesize)
                for (int x = sx - 2; x < xwidth; x += tilesize)
                    Lines.line(x, y, x + 4, y);
        });

        if (ruler) Draw.draw(Layer.overlayUI, () -> {
            Lines.stroke(1f, Pal.accent);

            int x = Mathf.round(input.mouseWorldX() - 4, tilesize) + 4;
            int y = Mathf.round(input.mouseWorldY() - 4, tilesize) + 4;

            Lines.line(x, bounds.y, x, yheigth);
            Lines.line(x + tilesize, bounds.y, x + tilesize, yheigth);
            Lines.line(bounds.x, y, xwidth, y);
            Lines.line(bounds.x, y + tilesize, xwidth, y + tilesize);
        });

        if (turretRadius) Draw.draw(Layer.overlayUI, () -> builds.turrets.each(BaseTurretBuild::drawSelect));

        if (reactorRadius) Draw.draw(Layer.overlayUI, () -> {
            builds.nuclears.each(nuclear -> drawRadius(nuclear, ((NuclearReactor) nuclear.block).explosionRadius, Pal.thoriumPink));
            builds.impacts.each(impact -> drawRadius(impact, ((ImpactReactor) impact.block).explosionRadius, Pal.meltdownHit));
        });

        Seq<Unit> units = unitInfo || unitRadius ? Groups.unit.intersect(bounds.x, bounds.y, bounds.width, bounds.height) : null;

        if (unitInfo) Draw.draw(Layer.overlayUI, () -> units.each(unit -> unit != player.unit(), unit -> {
            if (unit.isPlayer()) {
                Tmp.v1.set(unit.aimX, unit.aimY).sub(unit).setLength(unit.hitSize);
                Lines.stroke(2f, unit.team.color);
                Lines.line(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, unit.aimX, unit.aimY);
            }

            drawHealthBar(unit, Pal.darkishGray, 1f);
            drawHealthBar(unit, Pal.health, Mathf.clamp(unit.health / unit.maxHealth));
        }));

        if (unitRadius) Draw.draw(Layer.overlayUI, () -> units.each(unit -> Drawf.circles(unit.x, unit.y, unit.range(), unit.team.color)));

        // asynchrony requires sacrifice
        Draw.draw(Layer.blockUnder, Draw::reset);
        Draw.draw(Layer.overlayUI, Draw::reset); 
    }

    public void showUnits(boolean hide) {
        if (hide) units.cache();
        else units.uncache();
    }

    public void toggleHistory() {
        Call.sendChatMessage("/history");
    }

    public void toggleCoreItems() {
        settings.put("coreitems", !settings.getBool("coreitems"));
	}

    public void toggleFodOfWar(){

    }

    private void drawRadius(Building build, int radius, Color color) {
        Drawf.dashCircle(build.x, build.y, radius * tilesize, color);
    }

    private void drawHealthBar(Unit unit, Color color, float fract) {
        Draw.color(color);

        float size = Mathf.sqrt(unit.hitSize) * 3f;
        float x = unit.x - size / 2f, y = unit.y - size;

        float height = size * 2f, stroke = size * -.35f, xs = x - size;
        float f1 = Math.min(fract * 2f, 1f), f2 = (fract - .5f) * 2f;

        float bo = -(1f - f1) * (-size - stroke);
        Fill.quad(
                x, y,
                x + stroke, y,
                xs + bo, y + size * f1,
                xs - stroke + bo, y + size * f1);

        if (f2 < 0) return;

        float bx = (1f - f2) * (-size - stroke) + x;
        Fill.quad(
                xs, y + size,
                xs - stroke, y + size,
                bx, y + height * fract,
                bx + stroke, y + height * fract);
    }
}
