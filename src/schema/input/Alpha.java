package schema.input;

import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.power.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/// Logic system controlling the player's unit.
public class Alpha
{
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
                // TODO when idle for a while, follow the cursor and rotate around it
            }
            else target = Tmp.v1.set
            (
                unit.x < rect.x ? rect.x : unit.x < rect.x + rect.width  ? unit.x : rect.x + rect.width,
                unit.y < rect.y ? rect.y : unit.y < rect.y + rect.height ? unit.y : rect.y + rect.height
            );
        }

        if (target != null)
        {
            path(target, range).sub(unit);

            // braking distance
            var len = unit.vel.len2() / 2f / type.accel;
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
    }

    /// Draws pathfinder obstacles.
    public void drawObstacles()
    {
        for (int i = 0; i < clusters.size; i++)
        {
        }
    }

    // region movement tools

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
