package schema.tools;

import arc.func.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.ItemBridge.*;

import static mindustry.Vars.*;

/// Utility focused on buildings.
public class Builds
{
    /// List of buildings that were iterated.
    private Seq<Building> iterated = new Seq<>();
    /// List of visible tiles with buildings.
    private Seq<Tile> tiles;

    /// Clears the list of iterated buildings.
    public void clearIterated() { iterated.clear(); }

    /// Iterates visible tiles with buildings.
    public void iterateBuilds(Cons<Tile> cons)
    {
        if (tiles == null) tiles = Reflect.get(renderer.blocks, "tileview");
        tiles.each(t -> t.build != null, cons);
    }

    /// Iterates tiles around the given core and connected storages.
    public void iterateCore(Building build, Cons2<Tile, Integer> cons)
    {
        iterated.add(build);
        build.proximity.each(p -> p.items == build.items && !iterated.contains(p), p -> iterateCore(p, cons));

        int bdx = build.tileX(),
            bdy = build.tileY(),
            min = (build.block.size - 1) / 2,
            max = (build.block.size / 2);

        for (int i = -min; i <= max; i++)
        {
            cons.get(world.tile(bdx + i, bdy + max + 1), 1); // top
            cons.get(world.tile(bdx + i, bdy - min - 1), 3); // bottom

            cons.get(world.tile(bdx + max + 1, bdy + i), 0); // right
            cons.get(world.tile(bdx - min - 1, bdy + i), 2); // left
        }
    }

    /// Iterates the given bridge and consequentially connected ones.
    public void iterateBridge(ItemBridgeBuild build, Cons<ItemBridgeBuild> cons)
    {
        if (iterated.add(build).size > 16) return;

        var linked = world.build(build.link);
        if (linked instanceof ItemBridgeBuild b && !iterated.contains(b)) iterateBridge(b, cons);

        cons.get(build);
    }

    /// Calculates a bunch of values required to draw a health bar.
    public void healthBar(Building build, float radius, boolean outer, Floatc4 context)
    {
        // single block builds have smaller health and status bars
        float multiplier = build.block.size > 1 ? 1f : .64f;

        // the radius is taken from the status indicator, but needs to rotated by 45 degrees
        radius *= multiplier * Mathf.sqrt2;

        // padding from the edges of the building
        float padding = 4f * multiplier - radius;

        context.get(outer ? radius : radius + multiplier * (Mathf.sqrt2 - 1), build.hitSize() / 2f - padding, build.x, build.y - build.hitSize() / 2f + padding + radius);
    }

    /// Whether the building can be controlled by the local player.
    public boolean controllable(Building build) { return player.unit() != null && build.canControlSelect(player.unit()); }
}
