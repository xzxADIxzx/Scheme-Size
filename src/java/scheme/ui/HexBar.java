package scheme.ui;

import arc.func.Cons;
import arc.func.Floatp;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.util.Tmp;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;

/** Contains many magic numbers. */
public class HexBar extends Table {

    public final Floatp amount;
    public float last, blink, value;

    /** I don't even remember what it's for. */
    public final float sw = Scl.scl(25.8f);

    public HexBar(Floatp amount, Cons<Table> cons) {
        super(cons);
        this.amount = amount;
    }

    @Override
    public void draw() {
        float next = amount.get();

        if (next == 0f) return;
        if (next < last) blink = 1f;

        if (Float.isNaN(next) || Float.isInfinite(next)) next = 1f;
        if (Float.isNaN(value) || Float.isInfinite(value)) value = 1f;

        blink = Mathf.lerpDelta(blink, 0f, 0.2f);
        value = Mathf.lerpDelta(value, next, 0.15f);
        last = next;

        drawInner(Pal.darkerGray, 1f); // draw a gray background over the standard one
        if (value > 0) drawInner(Tmp.c1.set(Pal.accent).lerp(Color.white, blink), value);

        Drawf.shadow(x + width / 2f, y + height / 2f, height * 1.13f, parentAlpha);
        Draw.reset();

        super.draw();
    }

    public void drawInner(Color color, float fract) {
        Draw.color(color, parentAlpha);

        float f1 = Math.min(fract * 2f, 1f), f2 = (fract - 0.5f) * 2f;
        float bh = height / 2f, mw = width - sw;

        float dx = sw * f1;
        float dy = bh * f1 + y;
        Fill.quad(
                x + sw, y,
                x + mw, y,
                x + dx + mw, dy,
                x - dx + sw, dy);

        if (f2 < 0) return;

        dx = sw * f2;
        dy = height * fract + y;
        Fill.quad(
                x, y + bh,
                x + width, y + bh,
                x + width - dx, dy,
                x + dx, dy);
    }
}
