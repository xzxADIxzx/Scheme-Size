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

    /// Updates the system's logic.
    public void update(Vec2 flw, Seq<BuildPlan> plans)
    {
        unit = player.unit();
        type = player.dead() ? null : unit.type;

        if (player.dead()) return;

        updateMovement(flw);
        updateBuilding(plans);
    }

    /// Updates the movement logic.
    private void updateMovement(Vec2 flw)
    {
    }

    /// Updates the building logic.
    private void updateBuilding(Seq<BuildPlan> plans)
    {
        if (plans.isEmpty())
        {
            unit.plans.clear();
            return;
        }
        if (unit.canBuild() && unit.updateBuilding) for (var p : priorities)
        {
            var plan = plans.find(p::pred);
            if (plan == null) continue;

            unit.plans.clear();
            unit.plans.add(plan);

            if (plan.initialized && (state.rules.infiniteResources || plan.progress == (plan.breaking ? 0f : 1f)))
                plans.remove(plan);

            break;
        }
    }

    // region tools

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

    // endregion
}
