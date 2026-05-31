package schema.ui.fragments;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.distribution.BufferedItemBridge.*;
import mindustry.world.blocks.distribution.ItemBridge.*;
import mindustry.world.blocks.liquid.LiquidBridge.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.power.ImpactReactor.*;
import mindustry.world.blocks.power.NuclearReactor.*;
import mindustry.world.blocks.production.BurstDrill.*;
import mindustry.world.blocks.production.Drill.*;
import mindustry.world.blocks.storage.CoreBlock.*;
import mindustry.world.blocks.storage.StorageBlock.*;
import schema.ui.*;

import static mindustry.Vars.*;
import static schema.Main.*;

/// Fragment that displays the block configuration overlay.
public class ConfigFragment extends Table
{
    /// Building that is being configured at the moment.
    private Building selected;
    /// Draw overrides implementing the schema overlays.
    private ObjectMap<Object, Cons<?>> overrides = new ObjectMap<>();

    public ConfigFragment() { touchable = Touchable.enabled; }

    /// Builds the fragment and overrides the original.
    public void build(Group parent)
    {
        parent.addChild(this);

        setTransform(true);
        update(() ->
        {
            if (selected != null) selected.updateTableAlign(this);
            setOrigin(Align.center);
        }
        ).visible(() -> selected != null && selected.isValid());

        override(CoreBuild              .class, this::drawCoreEdges);
        override(StorageBuild           .class, this::drawCoreEdges);
        override(NuclearReactorBuild    .class, bd -> drawExplosionRadius(bd, Pal.thoriumPink));
        override(ImpactReactorBuild     .class, bd -> drawExplosionRadius(bd, Pal.meltdownHit));
        override(DrillBuild             .class, this::drawOres);
        override(BurstDrillBuild        .class, this::drawOres);
        override(ItemBridgeBuild        .class, this::drawBridgeSequence);
        override(BufferedItemBridgeBuild.class, this::drawBridgeSequence);
        override(LiquidBridgeBuild      .class, this::drawBridgeSequence);
    }

    // region control

    /// Shows the fragment with a simple animation.
    public void show(Building build)
    {
        if (build.configTapped())
        {
            selected = build;

            clear();
            build.buildConfiguration(this);
            pack();

            getChildren().each(c ->
            {
                if (c instanceof Table t) t.background(Style.find("panel-x-shape"));
                if (c instanceof Button b) b.getStyle().up = Style.find("panel-x-shape");
            });

            actions(Actions.scaleTo(0f, 1f), Actions.scaleTo(1f, 1f, .1f, Interp.pow4Out));
        }
    }

    /// Hides the fragment with a simple animation.
    public void hide()
    {
        actions(Actions.scaleTo(0f, 1f, .1f, Interp.pow4Out), Actions.run(this::hideImmediately));
    }

    /// Immediately hides the fragment.
    public void hideImmediately()
    {
        selected = null;
    }

    // endregion
    // region display

    /// Overrides the [draw method][Building#drawSelect()].
    public <T extends Building> void override(Class<T> build, Cons<T> draw) { overrides.put(build, draw); }

    /// Draws the selection overlays of the given building.
    public <T extends Building> void draw(T build)
    {
        @SuppressWarnings("unchecked")
        var draw = (Cons<T>) overrides.get(build.getClass());

        if (draw != null)
            draw.get(build);
        else
            build.drawSelect();
    }

    /// Draws the configure overlays of the given building.
    public void drawConfigure()
    {
        if (selected != null) selected.drawConfigure();
    }

    private void drawCoreEdges(Building build)
    {
        // do not highlight storages that are not connected to anything
        if (build instanceof StorageBuild && !build.proximity.contains(p -> p.items == build.items)) return;

        Lines.stroke(2f, build.team.color);
        overlay.capture(2f);

        builds.clearIterated();
        builds.iterateCore(build, (t, d) ->
        {
            if (t.build != null && t.build.items == build.items) return;

            var dir = Geometry.d4[d];
            var x = t.worldx() - dir.x * 4f;
            var y = t.worldy() - dir.y * 4f;

            if (dir.x == 0)
                Lines.line(x - 4f, y, x + 4f, y);
            else
                Lines.line(x, y - 4f, x, y + 4f);
        });

        overlay.render();
        Draw.reset();
    }

    private void drawExplosionRadius(Building build, Color color)
    {
        int radius = ((PowerGenerator) build.block).explosionRadius * tilesize;

        indexer.eachBlock(build, radius, b -> true, b -> Drawf.selected(b, Tmp.c1.set(color).a(Mathf.absin(4f, 1f))));

        Drawf.dashCircle(build.x, build.y, radius, color);
    }

    private void drawOres(DrillBuild build)
    {
        build.tile.getLinkedTiles(t ->
        {
            if (t.drop() != null) Drawf.square(t.worldx(), t.worldy(), Mathf.absin(Time.time - t.dst(Vec2.ZERO), 4f, .5f) - .8f, t.drop().color);
        });
        build.block.drawPlace(build.tileX(), build.tileY(), 0, true);
    }

    private void drawBridgeSequence(ItemBridgeBuild build)
    {
        builds.clearIterated();
        builds.iterateBridge(build, ItemBridgeBuild::drawSelect);

        Lines.stroke(2.5f, Pal.gray);
        Lines.square(build.x, build.y, 2f, 45f);
        Lines.stroke(1f, Pal.place);
        Lines.square(build.x, build.y, 2f, 45f);

        if (build.items == null) return;
        Draw.reset();

        var count = build.items.sum((_, _) -> 1f) - 1f;
        var start = build.x - count * 2f - 4f;
        float[] x = { start };

        Draw.mixcol(Pal.gray, 1f);
        x[0] = start;
        build.items.each((i, a) -> Draw.rect(i.fullIcon, x[0] += 4f, build.y + 8f, 6f, 6f));

        Draw.reset();
        x[0] = start;
        build.items.each((i, a) -> Draw.rect(i.fullIcon, x[0] += 4f, build.y + 9f, 6f, 6f));
    }

    // endregion
    // region agent

    /// Creates a new agent.
    public Agent agent() { return new Agent(); }

    /// Agent redirecting method calls from the original component.
    public class Agent extends mindustry.ui.fragments.BlockConfigFragment
    {
        @Override
        public void showConfig(Building tile) { show(tile); }

        @Override
        public void hideConfig() { hide(); }

        @Override
        public void forceHide() { hideImmediately(); }

        @Override
        public boolean isShown() { return visible; }

        @Override
        public Building getSelected() { return selected; }
    }

    // endregion
}
