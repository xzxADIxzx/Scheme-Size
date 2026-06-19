package schema.tools;

import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.pooling.*;
import mindustry.entities.units.*;
import mindustry.world.*;

import static mindustry.Vars.*;

/// Utility focused on grids.
public class Grids
{
    /// Normalized tile area.
    private final Area area = new Area();
    /// Normalized draw area.
    private final Draw draw = new Draw();

    /// Normalizes the space.
    public Area normalize(int x1, int y1, int x2, int y2, int max)
    {
        area.x1 = x1 < x2 ? x1 : x1 - Math.min(max - 1, Math.abs(x1 - x2));
        area.x2 = x1 > x2 ? x1 : x1 + Math.min(max - 1, Math.abs(x1 - x2));

        area.y1 = y1 < y2 ? y1 : y1 - Math.min(max - 1, Math.abs(y1 - y2));
        area.y2 = y1 > y2 ? y1 : y1 + Math.min(max - 1, Math.abs(y1 - y2));

        return area;
    }

    /// Normalizes the space.
    public Draw normalize(Area area)
    {
        draw.x1 = area.x1 * tilesize - tilesize / 2f;
        draw.x2 = area.x2 * tilesize + tilesize / 2f;

        draw.y1 = area.y1 * tilesize - tilesize / 2f;
        draw.y2 = area.y2 * tilesize + tilesize / 2f;

        return draw;
    }

    /// Iterates the surface.
    public void iterate(Area area, Intc2 cons)
    {
        for (int x = area.x1; x <= area.x2; x++)
        for (int y = area.y1; y <= area.y2; y++)
            cons.get(x, y);
    }

    /// Draws a line or rect.
    public void update(Seq<BuildPlan> plans, Block block, int rotation, boolean diagonal, int x1, int y1, int x2, int y2)
    {
        if (block == null) return;

        line(block, x1, y1, x2, y2); // TODO implement other types

        block.changePlacementPath(temp, rotation, diagonal);

        Point2 prev = null, plan = null, next = temp.any() ? temp.first() : null;

        for (int i = 0; i < temp.size; i++)
        {
            prev = plan;
            plan = next;
            next = i + 1 < temp.size ? temp.get(i + 1) : null;

            int rt = prev == null && next == null ? rotation : next == null
                ? Tile.relativeTo(prev.x, prev.y, plan.x, plan.y)
                : Tile.relativeTo(plan.x, plan.y, next.x, next.y);

            plans.add(new BuildPlan(plan.x, plan.y, rt != -1 ? rt : rotation, block, block.nextConfig()) {{ animScale = 1f; }});
        }

        block.handlePlacementLine(plans);

        Pools.freeAll(temp, true);
        temp.clear();
    }

    /// Brief transfer point.
    private final Seq<Point2> temp = new Seq<>();

    /// Adds a straight line.
    private void line(Block block, int x1, int y1, int x2, int y2)
    {
        var bd = block.swapDiagonalPlacement
            ? Geometry.d8[Mathf.round(Angles.angle(x1, y1, x2, y2) / 45f) % 8]
            : Geometry.d4[Mathf.round(Angles.angle(x1, y1, x2, y2) / 90f) % 4];

        Point2 dir = Pools.obtain(Point2.class, Point2::new).set(bd.x * block.size, bd.y * block.size);
        Point2 pos = Pools.obtain(Point2.class, Point2::new).set(x1, y1);

        float dst = pos.dst2(x2, y2);
        int limit = 512;
        do
        {
            temp.add(Pools.obtain(Point2.class, Point2::new).set(pos));

            pos.add(dir);

            if (dst < pos.dst2(x2, y2))
                break;
            else
                dst = pos.dst2(x2, y2);
        }
        while (--limit > 0);

        Pools.free(dir);
        Pools.free(pos);
    }

    /// Structure representing a tile area.
    public class Area
    {
        /// Normalized corners of the area.
        public int x1, y1, x2, y2;

        public int width () { return x2 - x1 + 1; }
        public int height() { return y2 - y1 + 1; }

        @Override
        public String toString() { return "(" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ")"; }
    }

    /// Structure representing a draw area.
    public class Draw
    {
        /// Normalized corners of the area.
        public float x1, y1, x2, y2;

        public float width () { return x2 - x1; }
        public float height() { return y2 - y1; }

        @Override
        public String toString() { return "(" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ")"; }
    }
}
