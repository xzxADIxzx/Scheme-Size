package schema.input;

import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.Pool.*;
import mindustry.graphics.*;
import mindustry.logic.*;

/// Structure representing a cluster of obstacles.
public class Cluster implements Poolable
{
    /// Obstacles themselves.
    private Seq<Ranged> obstacles = new Seq<>();

    /// Precise collision.
    private Vec2[] vertices = new Vec2[60];
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

        for (int i = 0; i < 60; i++)
        {
            Tmp.v2.trns(i * 6f, 2048f).add(Tmp.v1);

            var closest = obstacles.min(o -> o.dst(Tmp.v2) - o.range());

            Tmp.v2.sub(closest).limit(closest.range() + 8f).add(closest);

            if (vertices[i] == null)
                vertices[i] = new Vec2(Tmp.v2);
            else
                vertices[i].set(Tmp.v2);
        }

        collision.setCenter(Tmp.v1).setSize(0f);
        collision.merge(vertices);
    }

    /// Draws the collision of the cluster.
    public void draw()
    {
        Drawf.dashRect(Pal.reactorPurple2, collision);

        Lines.stroke(3f, Pal.gray);
        Lines.poly(vertices, 0f, 0f, 1f);
        Lines.stroke(1f, Pal.reactorPurple2);
        Lines.poly(vertices, 0f, 0f, 1f);
    }


    /// Whether the obstacle is outside.
    public boolean outside(Ranged obst)
    {
        return obstacles.allMatch(o -> !obst.within(o, o.range() + obst.range()));
    }

    /// Whether the vector is inside.
    public boolean inside(Vec2 pos)
    {
        return collision.contains(pos) && obstacles.contains(o -> pos.within(o, o.range()));
    }

    @Override
    public void reset() { obstacles.clear(); }
}
