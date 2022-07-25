package scheme.tools;

import arc.Events;
import arc.func.Cons;
import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.struct.Seq;
import mindustry.game.EventType.*;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.BaseTurret.BaseTurretBuild;
import mindustry.world.blocks.power.ImpactReactor.ImpactReactorBuild;
import mindustry.world.blocks.power.NuclearReactor.NuclearReactorBuild;

import static mindustry.Vars.*;

public class BuildsCache {

    public TilesQuadtree tiles;

    public Seq<BaseTurretBuild> turrets = new Seq<>();
    public Seq<NuclearReactorBuild> nuclears = new Seq<>();
    public Seq<ImpactReactorBuild> impacts = new Seq<>();

    public void load() {
        Events.run(WorldLoadEvent.class, this::refresh);
        Events.on(BlockDestroyEvent.class, event -> uncache(event.tile));
        Events.on(BlockBuildEndEvent.class, event -> {
            if (event.breaking) uncache(event.tile);
            else cache(event.tile.build);
        });
    }

    public void refresh() {
        tiles = new TilesQuadtree(new Rect(0, 0, world.unitWidth(), world.unitHeight()));
        world.tiles.eachTile(tile -> tiles.insert(tile));

        turrets.clear();
        nuclears.clear();
        impacts.clear();
        Groups.build.each(this::cache);
    }

    public void cache(Building build) {
        if (build instanceof BaseTurretBuild turret) turrets.add(turret);
        if (build instanceof NuclearReactorBuild nuclear) nuclears.add(nuclear);
        if (build instanceof ImpactReactorBuild impact) impacts.add(impact);
    }

    public void uncache(Tile tile) {
        turrets.filter(turret -> turret.tile != tile);
        nuclears.filter(nuclear -> nuclear.tile != tile);
        impacts.filter(impact -> impact.tile != tile);
    }

    public void each(Rect bounds, Cons<Tile> cons) {
        tiles.intersect(bounds, tile -> {
            if (tile.build != null) cons.get(tile);
        });
    }

    public static class TilesQuadtree extends QuadTree<Tile> {

        public TilesQuadtree(Rect bounds) {
            super(bounds);
        }

        @Override
        public void hitbox(Tile tile) {
            var floor = tile.floor();
            tmp.setCentered(tile.worldx(), tile.worldy(), floor.clipSize, floor.clipSize);
        }

        @Override
        protected QuadTree<Tile> newChild(Rect rect) {
            return new TilesQuadtree(rect);
        }
    }
}
