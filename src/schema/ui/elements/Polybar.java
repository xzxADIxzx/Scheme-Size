package schema.ui.elements;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.*;
import mindustry.graphics.*;

/// Elements that displays a polygonal bar.
public class Polybar extends Element
{
    /// Current value.
    public Floatp provider;
    /// Display value.
    public float value;

    public Polybar(Floatp provider, Color color, Boolp visible)
    {
        this.provider = provider;
        setColor(color);
        visible(visible);
    }

    @Override
    public void draw()
    {
        value = Mathf.lerpDelta(value, provider.get(), .2f);
        draw(Pal.accentBack, 1f);
        draw(color, value);
    }

    public void draw(Color color, float value)
    {
        float stroke = 8f;
        float f1 = Math.min(1f, value * 2f), f2 = Math.min(.5f, value - .5f), w = x + width, z = y + height / 2f;

        Draw.color(color, parentAlpha);
        Fill.quad
        (
            x - stroke, y,
            w - stroke, y,
            w - stroke * (1f - f1), y + height * f1 / 2f,
            x - stroke * (1f - f1), y + height * f1 / 2f
        );
        if (f2 > 0f) Fill.quad
        (
            x, z,
            w, z,
            w - stroke * (2f * f2), z + height * f2,
            x - stroke * (2f * f2), z + height * f2
        );
    }
}
