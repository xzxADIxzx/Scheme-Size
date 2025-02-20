package schema.input;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.gen.*;

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

            var rect = camera.bounds(Tmp.r1).grow(-64f);
            if (rect.contains(unit.x, unit.y) || Keybind.mouse_mv.down())
                unit.wobble();
            else {
                Tmp.v4.set(
                    unit.x < rect.x ? rect.x : unit.x < rect.x + rect.width  ? unit.x : rect.x + rect.width,
                    unit.y < rect.y ? rect.y : unit.y < rect.y + rect.height ? unit.y : rect.y + rect.height
                ).sub(unit);

                // length of the breaking distance
                var len = unit.vel.len2() / 2f / type.accel;
                // distance from the unit to the edge of the screen
                var dst = Math.max(0f, Tmp.v4.len() - len);

                // TODO implement path finder that is not gonna kill the unit while moving across enemy turrets
                unit.movePref(Tmp.v4.limit(dst).limit(type.speed));
            }
        } else {
            // this type of movement is activate only when the player controls a combat unit
            // inherently, this is the classical movement

            lerpCam(pan.scl(64f * tilesize).add(player));
            unit.movePref(mov.add(flw).limit2(1f).scl(unit.speed()));
        }

        if (Keybind.teleport.tap()) unit.set(input.mouseWorld());

        float angle = Angles.mouseAngle(unit.x, unit.y);

        if (Keybind.look_at.down())
            unit.rotation = angle;
        else {
            if (player.shooting && type.omniMovement && type.faceTarget && type.hasWeapons())
                unit.lookAt(angle);
            else
                unit.lookAt(unit.prefRotation());
        }

        if (Keybind.respawn.tap()) Call.unitClear(player);
        if (Keybind.despawn.tap()); // TODO admins/hacky functions

        if (unit instanceof Payloadc pay) {
            if (Keybind.pick_cargo.tap()) {

                var target = Units.closest(unit.team, unit.x, unit.y, u -> u.isGrounded() && u.within(unit, (u.hitSize + type.hitSize) * 2f) && pay.canPickup(u));
                if (target != null) Call.requestUnitPayload(player, target);

                else {
                    var build = world.buildWorld(unit.x, unit.y);
                    if (build != null && state.teams.canInteract(unit.team, build.team)) Call.requestBuildPayload(player, build);
                }
            }
            if (Keybind.drop_cargo.tap()) Call.requestDropPayload(player, player.x, player.y);
        }

        unit.aim(input.mouseWorld());
        unit.controlWeapons(true, player.shooting);

        player.mouseX = unit.aimX;
        player.mouseY = unit.aimY;
        player.boosting = Keybind.boost.down();
    }

    @Override
    protected void updateState() {}

    @Override
    protected void drawPlans() {}

    @Override
    protected void drawOverlay() {}
}
