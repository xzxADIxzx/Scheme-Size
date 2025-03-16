package schema.input;

import arc.math.*;
import arc.math.geom.*;
import mindustry.entities.units.*;
import mindustry.gen.*;

import static mindustry.Vars.*;

/** Represents a block that can be rotated. It can be either a building or plan. */
public class Rotatable implements Position {

    private Building build;
    private BuildPlan plan;

    /** Position of the tile that contains the block or its center. */
    public int x, y;

    public Rotatable(Building build, BuildPlan plan) {
        if (plan != null)
            this.plan = plan;
        else
            this.build = build;

        x = plan != null ? plan.x : build != null ? build.tileX() : -1;
        y = plan != null ? plan.y : build != null ? build.tileY() : -1;
    }

    /** Whether the block is valid to rotate or not. */
    public boolean valid() { return (plan != null && plan.block.rotate) || (build != null && build.block.rotate && build.team == player.team()); }

    /** Returns the radius of the block. */
    public float radius() {
        if (plan != null)
            return plan.block.size * Mathf.sqrt2 * 4f;
        else
            return build.hitSize() * Mathf.sqrt2 / 2f;
    }

    /** Rotates the block by the given scroll. */
    public void rotateBy(int scroll) {
        if (plan != null)
            plan.rotation = Mathf.mod(plan.rotation + scroll, 4);
        else
            Call.rotateBlock(player, build, scroll > 0);
    }

    /** Rotates the block to the given direction. */
    public void rotateTo(int dir) {
        if (plan != null)
            plan.rotation = dir;
        else {
            boolean j = build.rotation < dir;
            for (int i = build.rotation; i != dir; i += Mathf.sign(j)) Call.rotateBlock(player, build, j);
        }
    }

    @Override
    public float getX() { return x * tilesize; }

    @Override
    public float getY() { return y * tilesize; }
}
