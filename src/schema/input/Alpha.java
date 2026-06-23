package schema.input;

import arc.*;
import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.Timer.*;
import arc.util.pooling.*;
import mindustry.entities.units.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.defense.turrets.TractorBeamTurret.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import mindustry.world.blocks.power.*;
import mindustry.world.meta.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/// Logic system controlling the player's unit.
public class Alpha
{
    /// Task that updates pathfinder obstacles.
    private Task update = new Task()
    {
        @Override
        public void run() { updatePathfinder(); };
    };
    /// Building plan sorting interval in ticks.
    private Interval sort = new Interval();

    /// Unit of the player.
    private Unit unit;
    /// Type of the unit.
    private UnitType type;

    /// Target position to move towards.
    public Position lock;
    /// Source building to drop items from.
    public Building drop;
    /// Target item type to mine.
    public Item mine;
    /// Target position to spin around.
    public Position spin;
    /// Time of the last performed action.
    public float time;

    public Alpha()
    {
        Floatc restart = delay ->
        {
            update.cancel();
            Timer.schedule(update, delay);
        };
        Events.on(TilePreChangeEvent.class, e ->
        {
            if (e.tile.block() instanceof BaseTurret) restart.get(8f);
        });
        Events.on(TileChangeEvent.class, e ->
        {
            if (e.tile.block() instanceof BaseTurret) restart.get(8f);
        });
        Events.run(WorldLoadEvent.class, () -> restart.get(1f));
    }

    /// Resets the system's values.
    public void reset()
    {
        lock = null;
        drop = null;
        mine = null;
        spin = null;
        time = Time.time;
    }

    /// Updates the system's logic.
    public void update(Vec2 flw, Seq<BuildPlan> plans)
    {
        unit = player.unit();
        type = player.dead() ? null : unit.type;

        if (player.dead() || state.isPaused()) return;

        updateMovement(flw);
        updateBuilding(plans);
    }

    /// Updates the movement logic.
    private void updateMovement(Vec2 flw)
    {
        if (!Vec2.ZERO.epsilonEquals(flw))
        {
            unit.movePref(flw.scl(type.speed));
            return;
        }

        Position target = null;
        float range = 0f;

        if (target == null && lock != null)
        {
            target = lock;
            range = tilesize;
        }

        if (target == null && drop != null)
        {
            target = drop;
            range = itemTransferRange;
        }

        if (target == null && unit.buildPlan() != null)
        {
            target = unit.buildPlan();
            range = buildingRange;
        }

        // TODO mine

        if (target == null)
        {
            var rect = camera.bounds(Tmp.r1).grow(-64f);
            if (rect.contains(unit.x, unit.y))
            {
                if (spin == null && Time.time - time > 1200f) spin = new Vec2().rnd(1f).add(unit);
            }
            else target = Tmp.v1.set
            (
                unit.x < rect.x ? rect.x : unit.x < rect.x + rect.width  ? unit.x : rect.x + rect.width,
                unit.y < rect.y ? rect.y : unit.y < rect.y + rect.height ? unit.y : rect.y + rect.height
            );
        }

        if (target != null)
        {
            spin = null;
            time = Time.time;
        }
        if (target == null && spin != null)
        {
            target = Tmp.v1.set(unit).sub(spin).setLength(32f + Mathf.absin(32f, 32f)).rotate(30f).add(spin).sub(unit).setLength(999f).add(unit);
            range = tilesize;
        }

        if (target != null)
        {
            path(target, range).sub(unit);

            // braking distance
            var len = unit.vel.len2() / type.drag;
            // target distance
            var dst = Math.max(0f, Tmp.v6.len() - len);

            unit.movePref(Tmp.v6.limit(dst).limit(type.speed));
        }
        else unit.wobble();
    }

    /// Updates the building logic.
    private void updateBuilding(Seq<BuildPlan> plans)
    {
        if (unit.canBuild() && unit.updateBuilding && plans.any()) for (var p : priorities)
        {
            var plan = plans.find(p::pred);
            if (plan == null) continue;

            unit.plans.clear();
            unit.plans.add(plan);

            if (done(plan)) plans.remove(plan);
            break;
        }
        else unit.plans.clear();

        if (sort.get(60f)) plans.sort(p -> p.dst(unit));
    }

    /// Draws pathfinder obstacles.
    public void drawObstacles() { clusters.each(Cluster::draw); }

    // region movement tools

    /// Clusters present in the world.
    private Seq<Cluster> clusters = new Seq<>();
    /// Obstacles present in the world.
    private Seq<Ranged> obstacles = new Seq<>();

    /// Updates pathfinder's obstacles.
    private void updatePathfinder()
    {
        Pools.freeAll(clusters, true);

        obstacles.clear();
        indexer.getEnemy(player.team(), BlockFlag.turret).each(b ->
        {
            if (b instanceof TractorBeamBuild bb && bb.block instanceof TractorBeamTurret bt && bt.targetAir) obstacles.add(bb);
            if (b instanceof      TurretBuild tb && tb.block instanceof            Turret tt && tt.targetAir) obstacles.add(tb);
        });

        clusters.clear();
        obstacles.each(b ->
        {
            var itr = clusters.iterator();
            Cluster cls = null;

            while (itr.hasNext())
            {
                var c = itr.next();
                if (c.outside(b)) continue;

                if (cls != null)
                {
                    itr.remove();
                    cls.merge(c);
                }
                else
                {
                    cls = c;
                    cls.merge(b);
                }
            }
            if (cls == null) clusters.add(Pools.obtain(Cluster.class, Cluster::new).merge(b));
        });

        clusters.each(Cluster::tighten);
    }

    /// Encountered cluster.
    private Cluster last;

    /// Returns the path to the target.
    private Vec2 path(Position target, float range)
    {
        Tmp.v5.set(target).sub(unit).limit(range);
        Tmp.v6.set(target).sub(Tmp.v5);

        if (unit.hittable())
        {
            Tmp.v5.setLength(64f).add(unit);

            var cls = clusters.find(c -> c.inside(Tmp.v5));
            if (cls == null) cls = last;
            else             last = cls;
            if (cls == null) return Tmp.v6;

            cls.find(unit, Tmp.v6, Tmp.v5);

            if (Vec2.ZERO.epsilonEquals(Tmp.v5))
                last = null;
            else
                Tmp.v6.set(Tmp.v5);
        }
        return Tmp.v6;
    }

    // endregion
    // region building tools

    /// Building priority.
    private interface Priority
    {
        boolean pred(BuildPlan plan);
    }

    /// Building priorities.
    private final Priority[] priorities =
    {
        plan -> priority(plan, 0) && (plan.block instanceof PowerNode || plan.block instanceof BeamNode),
        plan -> priority(plan, 1) && (plan.block instanceof PowerNode || plan.block instanceof BeamNode),
        plan -> priority(plan, 0) && (plan.block instanceof MendProjector),
        plan -> priority(plan, 0) && (plan.block instanceof ForceProjector),
        plan -> priority(plan, 0),
        plan -> priority(plan, 1),
        plan -> priority(plan, 2),
        plan -> priority(plan, 3),
        plan -> priority(plan, 7),
    };

    /// Returns the priority of the given plan.
    private boolean priority(BuildPlan plan, int skip)
    {
        return
        (
            ((skip & 1) == 1 || state.rules.infiniteResources || player.within(plan, type.buildRange))
            &&
            ((skip & 2) == 2 || !unit.shouldSkip(plan, unit.core()))
            &&
            ((skip & 4) == 4 || plan.breaking || insys.placeable(plan, true, false))
        );
    }

    /// Returns the finality of the given plan.
    private boolean done(BuildPlan plan)
    {
        return
        (
            plan.initialized
            &&
            (state.rules.infiniteResources || plan.progress == (plan.breaking ? 0f : 1f))
            ||
            plan.isDone()
        );
    }

    // endregion
}
