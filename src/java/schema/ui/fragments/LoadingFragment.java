package schema.ui.fragments;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;
import mindustry.ui.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/** Fragment that is displayed during loading of any kind. */
public class LoadingFragment extends Table {

    /** Horizontal and vertical distance between hexes. */
    public static final float spacing = 240f, height = Mathf.sqrt3 * spacing / 6f;
    /** Distance between bars and their size. */
    public static final float step = 80f, skew = 32f;

    /** Provides the current loading progress. */
    private Floatp progress;
    /** Interpolated value of the {@link #progress}. */
    private float displayProgress;

    /** Post-processing component. */
    private Bloom bloom = new Bloom(true);
    /** Collection of hexes' positions. */
    private Seq<Vec2> hexes = new Seq<>();

    public LoadingFragment() { super(Styles.black8); }

    /** Builds the fragment and override the original one. */
    public void build(Group parent) {
        Events.run(ResizeEvent.class, () -> {
            int w = graphics.getWidth(),
                h = graphics.getHeight();

            bloom.resize(w, h);
            bloom.blurPasses = 8;

            hexes.clear();

            for (int x = 0; x <= w / spacing; x++)
                for (int y = 0; y <= h / height + 1; y++)
                    hexes.add(new Vec2((x + (y % 2) * .5f) * spacing, y * height - 14f));
        });

        parent.addChild(this);
        parent.removeChild(Reflect.get(ui.loadfrag, "table"));

        setFillParent(true);
        hideImmediately();
        label(() -> (int) (progress.get() * 100) + "%").style(Styles.techLabel).color(Pal.accent);
    }

    /** Shows the fragment with a simple animation. */
    public void show() {
        hexes.shuffle();
        visible = true;

        toFront();
        actions(Actions.alpha(.0f), Actions.alpha(1f, .4f));
    }

    /** Hides the fragment with a simple animation. */
    public void hide() {
        progress = () -> 1f;
        actions(Actions.delay(.4f), Actions.alpha(0f, .4f), Actions.run(this::hideImmediately));
    }

    /** Immediately hides the fragment without any animation. */
    public void hideImmediately() {
        progress = () -> 0f;
        displayProgress = 0f;
        visible = false;
    }

    @Override
    public void draw() {
        super.draw();

        float progress = displayProgress += Math.min(this.progress.get() - displayProgress, Time.delta / 20f);

        float w = graphics.getWidth(), h = graphics.getHeight();
        float x = w / 2f, y = h / 2f;

        bloom.setBloomIntensity(1.5f + progress);
        bloom.capture();

        // region hexes

        for (int i = 0; i < hexes.size; i++) {
            var alpha = Mathf.clamp(progress * hexes.size - i);
            if (alpha == 0f) break; // the rest of the hexes will have the same result

            Vec2 hex = hexes.get(i);

            Draw.color(Pal.accent, color.a * alpha);
            Fill.poly(hex.x, hex.y, 6, 48f);

            Draw.color(Color.black);
            Fill.poly(hex.x, hex.y, 6, 24f);
        }

        // endregion
        // region bars

        Draw.color(Color.black);
        Fill.rect(x, y, w, 138f);

        Draw.color(Color.black, color.a * .2f);
        Fill.rect(x, y, w, h);

        Draw.color(Pal.accent, color.a);
        Fill.rect(x, y + 50f, w, 12f);
        Fill.rect(x, y - 50f, w, 12f);

        int bars = (int) (w / step / 2f) + 1;

        for (int i = 2; i < bars; i++) {
            float fract = 1f - (i - 2f) / (bars - 1f);
            float alpha = Mathf.clamp(1f - (fract - progress) * bars);

            Draw.color(Pal.accent, color.a * alpha);

            for (int side : Mathf.signs) {
                float bx = x + i * step * side - skew / 2f;

                Fill.rects(bx, y, skew, skew, -skew * side);
                Fill.rects(bx, y, skew, -skew, -skew * side);
            }
        }

        // endregion

        bloom.render();
    }

    // region agent

    /** Returns the agent of this fragment. */
    public Agent getAgent() { return new Agent(); }

    /** Agent that redirects method calls from the original fragment to the new one. */
    public class Agent extends mindustry.ui.fragments.LoadingFragment {

        @Override
        public void setProgress(Floatp p) { progress = p; }

        @Override
        public void setProgress(float p) { progress = () -> p; }

        @Override
        public void setButton(Runnable listener) {} // TODO implement

        @Override
        public void show() { loadfrag.show(); }

        @Override
        public void show(String text) { loadfrag.show(); }

        @Override
        public void hide() { loadfrag.hide(); }

        @Override
        public void toFront() {}

        @Override
        public void snapProgress() {}
    }

    // endregion
}
