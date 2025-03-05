package schema.ui.fragments;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.fragments.*;
import mindustry.world.blocks.distribution.BufferedItemBridge.*;
import mindustry.world.blocks.distribution.ItemBridge.*;
import mindustry.world.blocks.liquid.LiquidBridge.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.power.ImpactReactor.*;
import mindustry.world.blocks.power.NuclearReactor.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.production.BurstDrill.*;
import mindustry.world.blocks.production.Drill.*;
import mindustry.world.blocks.storage.CoreBlock.*;
import mindustry.world.blocks.storage.StorageBlock.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/** Fragment that displays the block configuration overlay. */
public class ConfigFragment extends Table {

    /** Building that is being configured at the moment. */
    private Building selected;
    /** Building that is being configured at the moment. */
    private ObjectMap<Object, Cons<?>> overrides = new ObjectMap<>();

    public ConfigFragment() { touchable = Touchable.enabled; }

    /** Builds the fragment and override the original one. */
    public void build(Group parent) {
        parent.addChild(this);

        setTransform(true);
        update(() -> {
            if (selected != null) selected.updateTableAlign(this);
        });
        override(CoreBuild.class, this::drawCoreEdges);
        override(StorageBuild.class, this::drawCoreEdges);
        override(NuclearReactorBuild.class, b -> drawExplosionRadius(b, Pal.thoriumPink));
        override(ImpactReactorBuild.class, b -> drawExplosionRadius(b, Pal.meltdownHit));
        override(DrillBuild.class, this::drawOres);
        override(BurstDrillBuild.class, this::drawOres);
        override(ItemBridgeBuild.class, this::drawBridgeSequence);
        override(BufferedItemBridgeBuild.class, this::drawBridgeSequence);
        override(LiquidBridgeBuild.class, this::drawBridgeSequence);
    }

    // region config

    /** Shows the fragment with a simple animation. */
    public void show(Building build) {
        if (selected != null) selected.onConfigureClosed();
        if (build.configTapped()) {
            selected = build;
            visible = true;

            clear();
            build.buildConfiguration(this);
            pack();
            actions(Actions.scaleTo(0f, 1f), Actions.scaleTo(1f, 1f, .08f));
        }
    }

    /** Hides the fragment with a simple animation. */
    public void hide() {
        if (selected != null) selected.onConfigureClosed();
        actions(Actions.scaleTo(0f, 1f, .08f), Actions.run(this::hideImmediately));
    }

    /** Immediately hides the fragment without any animation. */
    public void hideImmediately() {
        selected = null;
        visible = false;
    }

    /** Whether the fragment is shown. */
    public boolean shown() { return visible && selected != null; }

    /** Returns the building that is being configured. */
    public Building selected() { return selected; }

    // endregion
    // region render

    /** Overrides the {@link Building#drawSelect() draw method} of the given building. */
    public <T extends Building> void override(Class<T> build, Cons<T> draw) { overrides.put(build, draw); }

    /** Draws the selection overlay of the given building. */
    public <T extends Building> void draw(T build) {
        @SuppressWarnings("unchecked")
        var draw = (Cons<T>) overrides.get(build.getClass());

        if (draw != null)
            draw.get(build);
        else
            build.drawSelect();
    }

    /** Draws a line around the edges of the given core and connected storages. */
    public void drawCoreEdges(Building build) {

        // do not highlight storages that are not connected to anything
        if (build instanceof StorageBuild && !build.proximity.contains(p -> p.items == build.items)) return;

        Lines.stroke(2f, build.team.color);
        overlay.capture(2f, 1f);

        builds.clearIterated();
        builds.iterateCore(build, (t, d) -> {
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
    }

    /** Draws the explosion radius of the given building. */
    public void drawExplosionRadius(Building build, Color color) {
        int radius = ((PowerGenerator) build.block).explosionRadius * tilesize;

        indexer.eachBlock(build, radius, b -> true, b -> Drawf.selected(b, Tmp.c1.set(color).a(Mathf.absin(4f, 1f))));
        Drawf.dashCircle(build.x, build.y, radius, color);
    }

    /** Draws ores under the given building. */
    public void drawOres(DrillBuild build) {
        build.tile.getLinkedTiles(t -> {
            if (t.drop() != null) Drawf.square(t.worldx(), t.worldy(), Mathf.absin(Time.time - t.dst(Vec2.ZERO), 4f, .5f) - .8f, t.drop().color);
        });

        Drill block = (Drill) build.block;
        float speed = build.dominantItems * 60f / block.getDrillTime(build.dominantItem);
        float width = block.drawPlaceText(bundle.formatFloat("bar.drillspeed", speed, 2), build.tileX(), build.tileY(), true);

        float dx = build.x - width / 2f - 4f, dy = build.y + block.size * tilesize / 2f + 5f;

        Draw.mixcol(Color.darkGray, 1f);
        Draw.rect(build.dominantItem.fullIcon, dx, dy - 1f, 6f, 6f);
        Draw.reset();
        Draw.rect(build.dominantItem.fullIcon, dx, dy, 6f, 6f);
    }

    /** Draws overlay of the given and consequentially connected bridges. */
    public void drawBridgeSequence(ItemBridgeBuild build) {
        builds.clearIterated();
        builds.iterateBridge(build, ItemBridgeBuild::drawSelect);

        Lines.stroke(2.5f, Pal.gray);
        Lines.square(build.x, build.y, 2f, 45f);
        Lines.stroke(1f, Pal.place);
        Lines.square(build.x, build.y, 2f, 45f);

        if (build.items == null) return;
        Draw.reset();

        var count = build.items.sum((i, a) -> 1f) - 1f;
        var start = build.x - count * 2f - 4f;
        var x = new float[1];

        Draw.mixcol(Pal.gray, 1f);
        x[0] = start;
        build.items.each((i, a) -> Draw.rect(i.fullIcon, x[0] += 4f, build.y + 8f, 6f, 6f));

        Draw.reset();
        x[0] = start;
        build.items.each((i, a) -> Draw.rect(i.fullIcon, x[0] += 4f, build.y + 9f, 6f, 6f));
    }

    // endregion
    // region agent

    /** Returns the agent of this fragment. */
    public Agent getAgent() { return new Agent(); }

    /** Agent that redirects method calls from the original fragment to the new one. */
    public class Agent extends BlockConfigFragment {

        @Override
        public void hideConfig() { hide(); }

        @Override
        public void forceHide() { hideImmediately(); }

        @Override
        public boolean isShown() { return shown(); }

        @Override
        public Building getSelected() { return selected; }
    }

    // endregion
}
