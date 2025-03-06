package schema.ui.polygon;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.ui.*;

import static arc.Core.*;
import static schema.Main.*;

/** Fragment that displays a polygonal selection wheel. */
public class Polygon extends Stack {

    /** List of vertices of the polygon. */
    private Seq<Vertex> vertices = new Seq<>();
    /** Radius the polygon and step between vertices in degrees. */
    private float size, step;
    /** Array containing alpha values of the vertices. */
    private float[] alpha = new float[16];

    /** Whether to draw the polygon or not. */
    public boolean draw;
    /** Index of the selected vertex. */
    public int selected;

    /** Builds the fragment and, if extended, assigns actions to the sides. */
    public void build(Group parent) {
        parent.addChild(this);

        setSize(0f);
        hideImmediately();
        update(() -> {
            selected = Mathf.within(x, y, input.mouseX(), input.mouseY(), 64f)
                ? -1
                : Mathf.round(Angles.angle(x, y, input.mouseX(), input.mouseY()) / step) % vertices.size;

            for (int i = 0; i < vertices.size; i++)
                vertices.get(i).label.translation.trns(i * step, size + 6f * alpha[i]);
        });
        keyDown(key -> {
            if (key == KeyCode.escape || key == KeyCode.back) app.post(this::hide);
        });
    }

    /** Triggers the callback of the selected vertex. */
    public void select() { vertices.get(selected).clicked.run(); }

    /** Shows the fragment with a simple animation. */
    public void show() {
        visible = true;
        x = input.mouseX();
        y = input.mouseY();

        size = vertices.size * 10f;
        step = 360f / vertices.size;

        requestKeyboard();
        toFront();
        actions(Actions.alpha(.0f), Actions.alpha(1f, .08f));
    }

    /** Hides the fragment with a simple animation. */
    public void hide() { actions(Actions.alpha(0f, .08f), Actions.run(this::hideImmediately)); }

    /** Immediately hides the fragment without any animation. */
    public void hideImmediately() { visible = false; draw = true; selected = -1; }

    /** Adds a new vertex to the polygon. */
    public void add(String text, boolean highlight, Cons<Integer> clicked) {
        int index = vertices.size;
        var label = new Label(text, Styles.outlineLabel);

        label.setAlignment(Align.center);
        label.visible(() -> draw);

        if (highlight) label.update(() -> label.color.set(Pal.accentBack).lerp(Pal.accent, alpha[index]));

        vertices.add(new Vertex(label, () -> clicked.get(index)));
        add(label);
    }

    /** Adds an empty vertex to the polygon. */
    public void add() { add("", false, i -> {}); }

    /** Removes all vertices from the polygon. */
    public void clear() { vertices.clear(); }

    @Override
    public void draw() {
        Draw.color(Color.black, color.a * .2f);
        Fill.crect(0f, 0f, graphics.getWidth(), graphics.getHeight());

        if (draw) {
            overlay.capture(.8f);

            Draw.color(Pal.accentBack, color.a);
            Lines.stroke(16f);
            Lines.poly(x, y, vertices.size, size);

            for (int i = 0; i < alpha.length; i++) {

                var a = alpha[i] = Mathf.lerpDelta(alpha[i], i == selected ? 1f : 0f, .4f);
                if (a < .01f) continue;

                Draw.color(Pal.accent, color.a * a);
                Lines.stroke(12f);
                Lines.arc(x + Tmp.v1.trns(i * step, 10f * a).x, y + Tmp.v1.y, size - 2f, 2f / vertices.size, (i - 1) * step, vertices.size);
            }

            overlay.render();
        }

        Draw.reset();
        super.draw();
    }

    /** Structure that represents a vertex of the polygon. */
    public record Vertex(Label label, Runnable clicked) {}
}
