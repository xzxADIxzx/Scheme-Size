package schema.tools;

import arc.func.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.world.*;

import static mindustry.Vars.*;

/** Utility class that helps iterate buildings. */
public class Builds {

    /** List of buildings that was iterated. */
    private Seq<Building> iterated = new Seq<>();
    /** List of visible tiles acquired via reflection. Contains only the tiles with buildings or boulders on them. */
    private Seq<Tile> tiles;

    /** Clears the list of iterated buildings. Call it before iterating anything. */
    public void clearIterated() { iterated.clear(); }

    /** Iterates visible tiles with buildings on them. */
    public void iterateBuilds(Cons<Tile> cons) {
        if (tiles == null) tiles = Reflect.get(renderer.blocks, "tileview");
        tiles.each(t -> t.build != null, cons);
    }

    /** Iterates tiles around the given core and connected storages. */
    public void iterateCore(Building build, Cons2<Tile, Integer> cons) {

        iterated.add(build);
        build.proximity.each(p -> p.items == build.items && !iterated.contains(p), p -> iterateCore(p, cons));

        int bdx = build.tileX(),
            bdy = build.tileY(),
            min = (build.block.size - 1) / 2,
            max = (build.block.size / 2);

        for (int i = -min; i <= max; i++) { // there used to be complex logic here, but it was simplified

            cons.get(world.tile(bdx + i, bdy + max + 1), 1); // top
            cons.get(world.tile(bdx + i, bdy - min - 1), 3); // bottom

            cons.get(world.tile(bdx + max + 1, bdy + i), 0); // right
            cons.get(world.tile(bdx - min - 1, bdy + i), 2); // left
        }
    }
}
