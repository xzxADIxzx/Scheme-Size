package schema.tools;

import arc.func.*;
import arc.struct.*;
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
        draw.x1 = area.x1 - tilesize / 2f;
        draw.x2 = area.x2 + tilesize / 2f;

        draw.y1 = area.y1 - tilesize / 2f;
        draw.y2 = area.y2 + tilesize / 2f;

        return draw;
    }

    /// Iterates the surface.
    public void iterate(Area area, Cons2<Integer, Integer> cons)
    {
        for (int x = area.x1; x <= area.x2; x++)
        for (int y = area.y1; y <= area.y2; y++)
            cons.get(x, y);
    }

    /// Structure representing a tile area.
    public class Area
    {
        /// Normalized corners of the area.
        public int x1, y1, x2, y2;

        @Override
        public String toString() { return "(" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ")"; }
    }

    /// Structure representing a draw area.
    public class Draw
    {
        /// Normalized corners of the area.
        public float x1, y1, x2, y2;

        @Override
        public String toString() { return "(" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ")"; }
    }
}
