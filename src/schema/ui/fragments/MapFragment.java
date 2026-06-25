package schema.ui.fragments;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import schema.input.*;
import schema.ui.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/// Fragment that displays the sector map.
public class MapFragment extends Table
{
    /// Zoom of the camera, or rather scale of the map.
    private float zoom;
    /// Position of the camera, or rather the map itself.
    private float panX, panY;
    /// Screen space width and height of the map.
    private float mw, mh;

    /// Whether the fragment is visible.
    public boolean shown;

    public MapFragment() { touchable = Touchable.enabled; }

    /// Builds the fragment and overrides the original.
    public void build(Group parent)
    {
        parent.addChild(this);

        setFillParent(true);
        update(() ->
        {
            if (!ui.chatfrag.shown()) requestScroll();
        }
        ).visible(() -> shown);

        bottom().table(Style.find("panel-n-shape"), cont ->
        {
            cont.margin(12f, 12f, 4f, 12f);
            cont.defaults().pad(4f);

            cont.button(Icon.left, Style.ibc, () -> shown = false).size(48f).checked(i -> false).tooltip("@back");
            cont.image().growY().width(4f).color(Pal.accent);
            cont.add("@map.tools");
        });
        addListener(new ElementGestureListener()
        {
            @Override
            public void touchDown(InputEvent event, float x, float y, int pointer, KeyCode key)
            {
                if (key == KeyCode.mouseRight) insys.poseCam(translate(x, y));
                if (Keybind.ping.tap())
                {
                    var p = translate(x, y);
                    Call.pingLocation(player, p.x, p.y, null);
                }
            }

            @Override
            public void pan(InputEvent event, float x, float y, float deltaX, float deltaY)
            {
                if (event.keyCode == KeyCode.mouseRight) insys.poseCam(translate(x, y));
                if (event.keyCode == KeyCode.mouseLeft)
                {
                    panX += deltaX;
                    panY += deltaY;
                }
            }
        });
        addListener(new InputListener()
        {
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY)
            {
                if (amountY > 0f ? zoom <= .5f : zoom >= 4f) return true;

                zoom -= amountY / 10f;
                panX += panX / (1f + amountY / 10f / zoom) - panX;
                panY += panY / (1f + amountY / 10f / zoom) - panY;

                return true;
            }
        });
    }

    // region control

    /// Immediately toggles the fragment.
    public void toggle()
    {
        if (shown = !shown)
        {
            zoom = 1f;
            panX = panY = 0f;
        }
    }

    /// Translates a point in local space to world space.
    public Vec2 translate(float localX, float localY)
    {
        return Tmp.v1.set
        (
            (localX - (graphics.getWidth () - mw) / 2f - panX) / mw * world.unitWidth (),
            (localY - (graphics.getHeight() - mh) / 2f - panY) / mh * world.unitHeight()
        );
    }

    // endregion
    // region display

    @Override
    public void draw()
    {
        int w = graphics.getWidth (),
            h = graphics.getHeight();

        Draw.color(Color.black);
        Fill.crect(0, 0, w, h);

        var tex = renderer.minimap.getTexture();
        if (tex != null)
        {
            float tw = tex.width, th = tex.height;
            if (tw > th)
            {
                mw = zoom * w;
                mh = zoom * w * th / tw;
            }
            else
            {
                mw = zoom * h * tw / th;
                mh = zoom * h;
            }

            Draw.color();
            Draw.rect(Draw.wrap(tex), w / 2f + panX, h / 2f + panY, mw, mh);

            renderer.minimap.drawEntities((w - mw) / 2f + panX, (h - mh) / 2f + panY, mw, mh, true);
        }

        Draw.reset();
        super.draw();
    }

    // endregion
    // region agent

    /// Creates a new agent.
    public Agent agent() { return new Agent(); }

    /// Agent redirecting method calls from the original component.
    public class Agent extends mindustry.ui.fragments.MinimapFragment
    {
        @Override
        public void toggle() { mapfrag.toggle(); }

        @Override
        public void hide() { shown = false; }

        @Override
        public boolean shown() { return shown; }
    }

    // endregion
}
