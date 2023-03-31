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
import mindustry.world.blocks.defense.OverdriveProjector.OverdriveBuild;
import mindustry.world.blocks.defense.turrets.BaseTurret.BaseTurretBuild;
import mindustry.world.blocks.power.ImpactReactor.ImpactReactorBuild;
import mindustry.world.blocks.power.NuclearReactor.NuclearReactorBuild;
import java.util.*;
import static mindustry.Vars.*;

public class BuildsCache {

    public TilesQuadtree tiles;
    public Building[] builds;

    public Seq<BaseTurretBuild> turrets = new Seq<>();
    public Seq<NuclearReactorBuild> nuclears = new Seq<>();
    public Seq<ImpactReactorBuild> impacts = new Seq<>();
    public Seq<OverdriveBuild> overdrives = new Seq<>();

    public void load() {
        Events.run(WorldLoadEvent.class, this::refresh);
        Events.on(BlockDestroyEvent.class, event -> uncache(event.tile));
        Events.on(BlockBuildBeginEvent.class, event -> {
            if (!event.breaking) put(event.tile.build);
        });
        Events.on(BlockBuildEndEvent.class, event -> {
            if (event.breaking) uncache(event.tile);
            else cache(event.tile.build);
        });
    }

    public void refresh() {
        tiles = new TilesQuadtree(new Rect(0, 0, world.unitWidth(), world.unitHeight()));
        builds = new Building[world.width() * world.height()];
        world.tiles.eachTile(tile -> {
            tiles.insert(tile);
            if (tile.build != null) put(tile.build); // cache all buildings on world load
        });

        turrets.clear();
        nuclears.clear();
        impacts.clear();
        overdrives.clear();
        Groups.build.each(this::cache);
    }

    public interface BuildingCache {
        void cache(Building build);
    }

    public class TurretCache implements BuildingCache {
        private List<BaseTurretBuild> turrets = new ArrayList<>();

        @Override
        public void cache(Building build) {
            if (build instanceof BaseTurretBuild turret) {
                turrets.add(turret);
            }
        }
    }

    public class NuclearCache implements BuildingCache {
        private List<NuclearReactorBuild> nuclears = new ArrayList<>();

        @Override
        public void cache(Building build) {
            if (build instanceof NuclearReactorBuild nuclear) {
                nuclears.add(nuclear);
            }
        }
    }

    public class ImpactCache implements BuildingCache {
        private List<ImpactReactorBuild> impacts = new ArrayList<>();

        @Override
        public void cache(Building build) {
            if (build instanceof ImpactReactorBuild impact) {
                impacts.add(impact);
            }
        }
    }

    public class OverdriveCache implements BuildingCache {
        private List<OverdriveBuild> overdrives = new ArrayList<>();

        @Override
        public void cache(Building build) {
            if (build instanceof OverdriveBuild overdrive) {
                overdrives.add(overdrive);
            }
        }
    }

    public void cache(Building build) {
        BuildingCache turretCache = new TurretCache();
        BuildingCache nuclearCache = new NuclearCache();
        BuildingCache impactCache = new ImpactCache();
        BuildingCache overdriveCache = new OverdriveCache();

        turretCache.cache(build);
        nuclearCache.cache(build);
        impactCache.cache(build);
        overdriveCache.cache(build);
    }


    public void uncache(Tile tile) {
        turrets.filter(turret -> turret.tile != tile);
        nuclears.filter(nuclear -> nuclear.tile != tile);
        impacts.filter(impact -> impact.tile != tile);
        overdrives.filter(overdrive -> overdrive.tile != tile);
    }

    public void each(Rect bounds, Cons<Tile> cons) {
        tiles.intersect(bounds, tile -> {
            if (tile.build != null) cons.get(tile);
        });
    }

    public void put(Building build) {
        if (build == null) return; // idk how is it possible
        builds[build.tileY() * world.width() + build.tileX()] = build;
    }

    public Building get(Tile tile) {
        return builds[tile.y * world.width() + tile.x];
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
