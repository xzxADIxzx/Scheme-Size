package schema.ui.fragments;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
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

/// Fragment that is displayed during any sort of loading.
public class LoadingFragment extends Table
{
    /// Horizontal and vertical distance between hexes.
    public static final float spacing = 240f, height = Mathf.sqrt3 * spacing / 6f;
    /// Distance between bars and their size.
    public static final float step = 80f, skew = 32f;

    /// Current progress of the ongoing loading.
    private Floatp progress;
    /// Smoothed value of the {@link #progress}.
    private float display;

    /// Post-processing component.
    private Bloom bloom = new Bloom(true);
    /// Collection of hexes' positions.
    private Seq<Vec2> hexes = new Seq<>();

    /// Cancel callback.
    private Runnable cancel;

    public LoadingFragment() { super(Styles.black8); }

    /// Builds the fragment and overrides the original.
    public void build(Group parent)
    {
        Events.run(ResizeEvent.class, () ->
        {
            int w = graphics.getWidth (),
                h = graphics.getHeight();

            bloom.resize(w, h);
            bloom.blurPasses = 8;
            hexes.clear();

            for (int x = 0; x < w / spacing + 1; x++)
            for (int y = 0; y < h / height  + 0; y++)
                hexes.add(new Vec2
                (
                    (w - Mathf.round(w, spacing)) / 2f + (x - (y % 2) * .5f) * spacing,
                    (h - Mathf.round(h, height )) / 2f + y * height
                ));
        });

        parent.addChild(this);
        parent.removeChild(Reflect.get(ui.loadfrag, "table"));

        setFillParent(true);
        hideImmediately();
        label(() -> (int) (progress.get() * 100) + "%").style(Styles.techLabel).color(Pal.accent);

        keyDown(key ->
        {
            if (key == KeyCode.escape | key == KeyCode.back && cancel != null) cancel.run();
        });
    }

    // region control

    /// Shows the fragment with a simple animation.
    public void show()
    {
        hexes.shuffle();
        visible = true;

        requestKeyboard();
        toFront();
        actions(Actions.alpha(.0f), Actions.alpha(1f, .2f));
    }

    /// Hides the fragment with a simple animation.
    public void hide()
    {
        progress = () -> 1f;
        actions(Actions.delay(.2f), Actions.alpha(0f, .2f), Actions.run(this::hideImmediately));
    }

    /// Immediately hides the fragment.
    public void hideImmediately()
    {
        progress = () -> 0f;
        display = 0f;
        visible = false;
        cancel = null;
    }

    // endregion
    // region display

    @Override
    public void draw()
    {
        super.draw();

        float progress = display += Math.min(this.progress.get() - display, Time.delta / 12f);

        float w = graphics.getWidth(), h = graphics.getHeight();
        float x = w / 2f, y = h / 2f;

        bloom.setBloomIntensity(1.5f + progress);
        bloom.capture();

        // region hexes

        for (int i = 0; i < hexes.size; i++)
        {
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

        for (int i = 2; i < bars; i++)
        {
            float fract = 1f - (i - 2f) / (bars - 1f);
            float alpha = Mathf.clamp(1f - (fract - progress) * bars);

            Draw.color(Pal.accent, color.a * alpha);

            for (int side : Mathf.signs)
            {
                float bx = x + i * step * side - skew / 2f;

                Fill.rects(bx, y, skew,  skew, -skew * side);
                Fill.rects(bx, y, skew, -skew, -skew * side);
            }
        }

        // endregion

        bloom.render();
    }

    // endregion
    // region agent

    /// Creates a new agent.
    public Agent agent() { return new Agent(); }

    /// Agent redirecting method calls from the original component.
    public class Agent extends mindustry.ui.fragments.LoadingFragment
    {
        @Override
        public void setProgress(Floatp p) { progress = p; }

        @Override
        public void setProgress(float p) { progress = () -> p; }

        @Override
        public void setButton(Runnable listener) { cancel = listener; }

        @Override
        public void show() { loadfrag.show(); }

        @Override
        public void show(String text) { loadfrag.show(); }

        @Override
        public void hide() { loadfrag.hide(); }

        @Override
        public void snapProgress() { }

        @Override
        public void toFront() { }
    }

    // endregion
}
