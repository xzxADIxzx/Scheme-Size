package scheme.ui;

import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

import static arc.Core.*;

public class HexSelection extends Stack {

    public static final float size = Scl.scl(60f), stroke = Scl.scl(24f), half = stroke / 2f;
    public static final Color background = Color.valueOf("00000099");

    public TextButton[] buttons = new TextButton[6];
    public Vec2[] vertices = new Vec2[6];

    public int selectedIndex = -1;
    public boolean updatable = true;

    public HexSelection() {
        for (int deg = 0; deg < 360; deg += 60)
            vertices[deg / 60] = new Vec2(Mathf.cosDeg(deg) * size, Mathf.sinDeg(deg) * size);
    }

    public void add(String icon, Cons<TextButton> listener) {
        int i = children.size;
        add(new Table(table -> {
            table.button(icon, Styles.cleart, () -> listener.get(buttons[i])).with(button -> {
                buttons[i] = button;

                button.setTranslation(vertices[i].x - half, vertices[i].y - half);
                button.getLabel().setWrap(false); // someone can use non-single-character tags
            }).minSize(24f); // unscaled stroke;
        }));
    }

    public void updateAlignment() {
        for (int i = 0; i < buttons.length; i++) {
            int align = buttons[i].getText().length() == 1 ? Align.center : (i <= 1 || i == 5 ? Align.left : Align.right);
            buttons[i].getLabel().setAlignment(Align.center, align);
        }
    }

    @Override
    public void draw() {
        Lines.stroke(stroke, background);
        Lines.poly(x, y, 6, size);

        if (Mathf.within(x, y, input.mouseX(), input.mouseY(), stroke)) selectedIndex = -1;
        else {
            float min = Float.POSITIVE_INFINITY;
            if (updatable) for (int i = 0; i < vertices.length; i++) {
                float dst = Mathf.dst(x + vertices[i].x, y + vertices[i].y, input.mouseX(), input.mouseY());
                if (dst < min) {
                    min = dst;
                    selectedIndex = i;
                } // yeah, it is
            }

            if (selectedIndex == -1) return; // please, try to avoid it
            Vec2 offset = vertices[selectedIndex].cpy().limit(half);

            Draw.color(Pal.accent);
            Lines.stroke(half);
            Lines.arc(x + offset.x, y + offset.y, size, 1f / 3f, (selectedIndex - 1) * 60f, 6);
        }

        Draw.reset();
        super.draw();
    }
}
