package schema.input;

import arc.math.geom.*;
import arc.util.*;

import static arc.Core.*;
import static mindustry.Vars.*;

/** Handles keyboard input via keybinds. */
public class DesktopInput extends InputSystem {

    @Override
    protected void update()
    {
        if (!scene.hasField() && !state.isPaused() && !player.dead()) updateMovement();
    }

    protected void updateMovement() {
        var unit = player.unit();
        var type = unit.type;

        Vec2 mov = Tmp.v1.set(Keybind.move_x.axis(), Keybind.move_y.axis()).nor();
        Vec2 pan = Keybind.pan_mv.down()
            ? Tmp.v2.set(input.mouse()).sub(graphics.getWidth() / 2f, graphics.getHeight() / 2f).scl(.004f).limit2(1f)
            : Tmp.v2.setZero();
        Vec2 flw = Keybind.mouse_mv.down()
            ? Tmp.v3.set(input.mouseWorld()).sub(player).scl(.016f).limit2(1f)
            : Tmp.v3.setZero();

        if (/* units.isCoreUnit */ type == mindustry.content.UnitTypes.gamma) {
            // this type of movement is active most of the time
            // the unit simply follows the camera and performs the commands of the player

            moveCam(mov.add(pan).limit2(1f).scl(settings.getInt("schema-pan-speed", 6) * (Keybind.boost.down() ? 2f : 1f) * Time.delta));
            unit.movePref(flw.scl(type.speed));
        } else {
            // this type of movement is activate only when the player controls a combat unit
            // inherently, this is the classical movement

            lerpCam(pan.scl(64f * tilesize).add(player));
            unit.movePref(mov.add(flw).limit2(1f).scl(type.speed));
        }
    }

    @Override
    protected void updateState() {}

    @Override
    protected void drawPlans() {}

    @Override
    protected void drawOverlay() {}
}
