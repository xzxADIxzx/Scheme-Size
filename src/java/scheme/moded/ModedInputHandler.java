package scheme.moded;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.content.Blocks;
import mindustry.core.World;
import mindustry.entities.units.BuildPlan;
import mindustry.graphics.Pal;
import mindustry.input.InputHandler;
import mindustry.input.Placement;
import mindustry.input.Placement.NormalizeDrawResult;
import mindustry.world.Tile;
import scheme.tools.BuildingTools.Mode;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

/** Last update - Jul 19, 2022 */
public interface ModedInputHandler {

    public int sizeX = mobile ? 0 : -16;
    public int sizeY = mobile ? 32 : -16;

    public default void modedInput() {}

    public default void buildInput() {}

    public boolean hasMoved(int x, int y);

    public void changePanSpeed(float value);

    public void lockMovement();

    public void lockShooting();

    public void flush(Seq<BuildPlan> plans);

    public default void flushLastRemoved() {
        flush(build.removed);
        build.removed.clear();
    }

    public default void flushBuildingTools() {
        if (build.mode != Mode.remove) flush(build.plan);
        else build.plan.each(player.unit()::addBuild);
        build.plan.clear();
    }

    public InputHandler asHandler();

    // methods that exist but, who knows why, not available
    public default Tile tileAt() {
        return world.tiles.getc(tileX(), tileY());
    }

    public default int tileX() {
        return World.toTile(input.mouseWorldX());
    }

    public default int tileY() {
        return World.toTile(input.mouseWorldY());
    }

    // some drawing methods
    public default void drawSize(int x1, int y1, int x2, int y2, int maxLength) {
        String x = getSize(Math.abs(x1 - x2), maxLength);
        String y = getSize(Math.abs(y1 - y2), maxLength);
        ui.showLabel(x + ", " + y, 0.02f, x2 * tilesize + sizeX, y2 * tilesize + sizeY);
    }

    public default String getSize(int size, int maxLength) {
        return ++size >= maxLength ? "[accent]" + maxLength + "[]" : String.valueOf(size);
    }

    public default void drawEditSelection(int x1, int y1, int x2, int y2, int maxLength){
        NormalizeDrawResult result = Placement.normalizeDrawArea(Blocks.air, x1, y1, x2, y2, false, maxLength, 1f);

        drawSize(x1, y1, x2, y2, maxLength);
        Lines.stroke(2f);

        Draw.color(Pal.darkerMetal);
        Lines.rect(result.x, result.y - 1, result.x2 - result.x, result.y2 - result.y);
        Draw.color(Pal.darkMetal);
        Lines.rect(result.x, result.y, result.x2 - result.x, result.y2 - result.y);
    }

    public default void drawEditSelection(int x, int y, int radius) {
        Vec2[] polygons = Geometry.pixelCircle(radius);
        Lines.stroke(2f);

        Draw.color(Pal.darkerMetal);
        Lines.poly(polygons, x * tilesize - 4, y * tilesize - 5, tilesize);
        Draw.color(Pal.darkMetal);
        Lines.poly(polygons, x * tilesize - 4, y * tilesize - 4, tilesize);
    }

    public default void drawLocked(float x, float y) {
        Color color = Color.orange.cpy().lerp(Color.scarlet, Mathf.absin(Time.time, 3f, 1f));
        ui.showLabel(bundle.format("movementlocked", color), 0.02f, x, y);
    }
}
