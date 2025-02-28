package schema.ui.fragments;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.fragments.*;
import schema.ui.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/** Fragment that displays the sector map. */
public class MapFragment extends Table {

    /** Zoom of the camera that is not related to the game camera. */
    private float zoom;
    /** Position of the camera, or rather the map itself. */
    private float panx, pany;
    /** Width and height of the map after applying zoom. */
    private float mw, mh;

    /** Whether the fragment is visible. */
    public boolean shown;

    public MapFragment() { touchable = Touchable.enabled; }

    /** Builds the fragment and override the original one. */
    public void build(Group parent) {
        parent.addChild(this);
        parent.getChildren().remove(parent.getChildren().indexOf(ui.minimapfrag.elem) + 1);
        parent.getChildren().remove(parent.getChildren().indexOf(ui.minimapfrag.elem));

        setFillParent(true);
        update(() -> {
            if (!ui.chatfrag.shown()) requestScroll();
        }).visible(() -> shown);

        bottom().table(atlas.drawable("schema-panel-bottom"), cont -> {
            cont.margin(12f, 12f, 4f, 12f);
            cont.defaults().pad(4f);

            cont.button(Icon.left, Style.ibc, () -> shown = false).size(48f).checked(i -> false).tooltip("@back");
            cont.image().growY().width(4f).color(Pal.accent);
            cont.add("@map");
        });
        addListener(new ElementGestureListener() {

            @Override
            public void touchDown(InputEvent event, float x, float y, int pointer, KeyCode key) {
                if (key == KeyCode.mouseRight) pan2(x, y);
            }

            @Override
            public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
                if (event.keyCode == KeyCode.mouseRight) pan2(x, y);
                else {
                    panx += deltaX;
                    pany += deltaY;
                }
            }
        });
        addListener(new InputListener() {

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                // the math equation below behaves a little strange when the zoom delta is zero
                // therefore, return is used instead of clamp
                if (amountY > 0f ? zoom <= .5f : zoom >= 4f) return true;

                zoom = zoom - amountY / 10f;

                panx += panx / (1f + amountY / 10f / zoom) - panx;
                pany += pany / (1f + amountY / 10f / zoom) - pany;

                return true;
            }
        });
    }

    /** Toggles visibility of the fragment. */
    public void toggle() { if (shown = !shown) { zoom = 1f; panx = pany = 0f; } }

    /** Pans the game camera to the world coordinates that correspond to the given local position. */
    public void pan2(float localX, float localY) { insys.setCam(Tmp.v1.set(
        (localX - (graphics.getWidth()  - mw) / 2f - panx) / mw * world.unitWidth(),
        (localY - (graphics.getHeight() - mh) / 2f - pany) / mh * world.unitHeight())); }

    @Override
    public void draw() {
        int w = graphics.getWidth(),
            h = graphics.getHeight();

        Draw.color(Color.black);
        Fill.crect(0, 0, w, h);

        var tex = renderer.minimap.getTexture();
        if (tex != null) {

            float tw = tex.width, th = tex.height;
            if (tw > th) {
                mw = zoom * w;
                mh = zoom * w * th / tw;
            } else {
                mw = zoom * h * tw / th;
                mh = zoom * h;
            }

            Draw.color();
            Draw.rect(Draw.wrap(tex), w / 2f + panx, h / 2f + pany, mw, mh);

            renderer.minimap.drawEntities((w - mw) / 2f + panx, (h - mh) / 2f + pany, mw, mh, zoom, true);
        }

        Draw.reset();
        super.draw();
    }

    // region agent

    /** Returns the agent of this fragment. */
    public Agent getAgent() { return new Agent(); }

    /** Agent that redirects method calls from the original fragment to the new one. */
    public class Agent extends MinimapFragment {

        @Override
        public boolean shown() { return true; } // there is a check in Control#update that should always fail

        @Override
        public void hide() { shown = false; } // planet dialog calls it
    }

    // endregion
}
