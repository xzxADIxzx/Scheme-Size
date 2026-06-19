package schema.input;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.Pool.*;
import mindustry.graphics.*;
import mindustry.logic.*;

/// Structure representing a cluster of obstacles.
public class Cluster implements Poolable
{
    /// Margin between the obstacles and vertices.
    private static final float margin = 20f;

    /// Obstacles themselves.
    private Seq<Ranged> obstacles = new Seq<>();

    /// Precise collision.
    private Vec2[] vertices = new Vec2[90];
    /// Rougher collision.
    private Rect collision = new Rect();

    /// Adds an obstacle.
    public Cluster merge(Ranged obst)
    {
        obstacles.add(obst);
        return this;
    }

    /// Marges a cluster.
    public Cluster merge(Cluster cls)
    {
        obstacles.add(cls.obstacles);
        return this;
    }

    /// Tightens the collision of the cluster.
    public void tighten()
    {
        Tmp.v1.setZero();
        obstacles.each(Tmp.v1::add);
        Tmp.v1.scl(1f / obstacles.size);

        for (int i = 0; i < 90; i++)
        {
            Tmp.v2.trns(i * 4f, 2048f).add(Tmp.v1);

            var closest = obstacles.min(o -> o.dst(Tmp.v2) - o.range());

            Tmp.v2.sub(closest).limit(closest.range() + margin).add(closest);

            if (vertices[i] == null)
                vertices[i] = new Vec2(Tmp.v2);
            else
                vertices[i].set(Tmp.v2);
        }
        for (int i = 0; i < 12; i++) smooth();

        for (int i = 0; i < 90; i++)
        {
            var t0 = vertices[i];
            var t1 = obstacles.min(o -> o.dst(t0) - o.range());

            Tmp.v2.set(t1).sub(t0).limit(t1.range() + margin);
            Tmp.v3.set(t1).sub(t0).sub(Tmp.v2).scl(1f / (1f + Mathf.pow(Mathf.E, 4f - Tmp.v3.len() / 8f)));

            t0.add(Tmp.v3);
        }
        for (int i = 0; i < 12; i++) smooth();

        collision.setPosition(Tmp.v1).setSize(0f);
        collision.merge(vertices);
    }

    /// Smooths the collision of the cluster.
    public void smooth()
    {
        for (int i = 0; i < 90; i++)
        {
            var v0 = vertex(i);
            var v1 = vertex(i - 1);
            var v2 = vertex(i + 1);
            var d1 = v0.dst(v1);
            var d2 = v0.dst(v2);

            if (d1 > 1.05f * d2)
            {
                Tmp.v2.set(v1).sub(v0).limit((d1 - d2) / 2f);
                v0.add(Tmp.v2);
            }
            if (d2 > 1.05f * d1)
            {
                Tmp.v2.set(v2).sub(v0).limit((d2 - d1) / 2f);
                v0.add(Tmp.v2);
            }
        }
    }

    /// Draws the collision of the cluster.
    public void draw()
    {
        Drawf.dashRect(Pal.reactorPurple2, collision);

        Lines.stroke(3f, Pal.gray);
        Lines.poly(vertices, 0f, 0f, 1f);
        Lines.stroke(1f, Pal.reactorPurple2);
        Lines.poly(vertices, 0f, 0f, 1f);

        Draw.reset();
    }

    /// Finds the path to the set position.
    public void find(Position in1, Position in2, Vec2 out)
    {
        var current = Structs.findMin(vertices, v -> v.dst2(in1));
        var target  = Structs.findMin(vertices, v -> v.dst2(in2));

        if (current == target)
        {
            out.setZero();
            return;
        }

        int ci = Structs.indexOf(vertices, current);
        int ti = Structs.indexOf(vertices, target );

        int dn = ci < ti ? ti - ci : vertices.length - ci + ti;
        int dp = ci > ti ? ci - ti : vertices.length - ti + ci;

        var next = vertex(ci + Mathf.sign(dn < dp));
        out.set(next).sub(current).scl(8f).add(next);
    }

    /// Whether the obstacle is outside.
    public boolean outside(Ranged obst)
    {
        return obstacles.allMatch(o -> !obst.within(o, o.range() + obst.range() + margin * 2f));
    }

    /// Whether the vector is inside.
    public boolean inside(Vec2 pos)
    {
        return collision.contains(pos) && obstacles.contains(o -> pos.within(o, o.range()));
    }

    /// Returns a vertex at the specified index.
    private Vec2 vertex(int i) { return vertices[Mathf.mod(i, vertices.length)]; }

    @Override
    public void reset() { obstacles.clear(); }
}
