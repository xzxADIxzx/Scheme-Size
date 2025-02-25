package schema.input;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.world.blocks.*;

import static arc.Core.*;
import static mindustry.Vars.*;

import java.util.*;

/** Handles keyboard input via keybinds. */
public class DesktopInput extends InputSystem {

    /** Amount of scrolls in one direction and the direction itself. */
    public int scrolls, dir;
    /** Amount of scrolls after which the zoom speed increases by one tile per scroll. */
    public int aspect = 2;

    @Override
    protected void update() {
        if (scene.hasField() || scene.hasDialog()) {
            updateAI();
            return;
        }
        updateMovement();
        updateZoom();
        updateCommand();
    }

    protected void updateAI() {
        if (state.isPaused()) return;

        var unit = player.unit();
        var type = unit.type;

        // TODO implement miner and builder AI

        var rect = camera.bounds(Tmp.r1).grow(-64f);
        if (rect.contains(unit.x, unit.y))
            unit.wobble();
        else {
            Tmp.v4.set(
                unit.x < rect.x ? rect.x : unit.x < rect.x + rect.width  ? unit.x : rect.x + rect.width,
                unit.y < rect.y ? rect.y : unit.y < rect.y + rect.height ? unit.y : rect.y + rect.height).sub(unit);

            // length of the breaking distance
            var len = unit.vel.len2() / 2f / type.accel;
            // distance from the unit to the edge of the screen
            var dst = Math.max(0f, Tmp.v4.len() - len);

            // TODO implement path finder that is not gonna kill the unit while moving across enemy turrets
            unit.movePref(Tmp.v4.limit(dst).limit(type.speed));
        }
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

            if (!Keybind.mouse_mv.down()) updateAI();
        } else {
            // this type of movement is activate only when the player controls a combat unit
            // inherently, this is the classical movement

            lerpCam(pan.scl(64f * tilesize).add(player));
            unit.movePref(mov.add(flw).limit2(1f).scl(unit.speed()));
        }

        if (Keybind.teleport.tap()) unit.set(input.mouseWorld());

        if (state.isPlaying()) {
            if (Keybind.look_at.down())
                unit.rotation = Angles.mouseAngle(unit.x, unit.y);
            else {
                if (player.shooting && type.omniMovement && type.faceTarget && type.hasWeapons())
                    unit.lookAt(input.mouseWorld());
                else
                    unit.lookAt(unit.prefRotation());
            }

            unit.aim(input.mouseWorld());
            unit.controlWeapons(true, player.shooting);
        }

        if (Keybind.respawn.tap()) Call.unitClear(player);
        if (Keybind.despawn.tap()); // TODO admins/hacky functions

        if (unit instanceof Payloadc pay && state.isPlaying()) {
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

        player.mouseX = unit.aimX;
        player.mouseY = unit.aimY;
        player.boosting = Keybind.boost.down();
    }

    protected void updateZoom() {
        int scroll = (int) Keybind.scroll();
        if (scroll == 0) return;

        if (dir != scroll) {
            dir = scroll;
            scrolls = 0;
        } else
            scrolls++;

        dest -= scroll * (4f + (scrolls / aspect));
        dest = Mathf.clamp(dest, minZoom, maxZoom);
    }

    protected void updateCommand() {
        if (commandMode = Keybind.command.down()) {
            commandUnits.retainAll(Unitc::isCommandable).retainAll(Healthc::isValid);

            if (Keybind.select.tap() && !scene.hasMouse()) commandRect = input.mouseWorld().cpy();
            if (Keybind.select.release() && commandRect != null) {

                if (commandRect.within(input.mouseWorld(), 8f)) {
                    var unit = selectedUnit();
                    var build = selectedBuilding();

                    if (unit != null) {
                        commandBuildings.clear();
                        if (!commandUnits.remove(unit)) commandUnits.add(unit);
                    }
                    else if (build != null && build.team == player.team()) {
                        commandUnits.clear();
                        if (!commandBuildings.remove(build)) commandBuildings.add(build);
                    }
                } else {
                    commandBuildings.clear();
                    selectedRegion(commandUnits::addUnique);
                }

                commandRect = null;
            }

            if (Keybind.select_all_units.tap()) {
                commandUnits.clear();
                commandBuildings.clear();

                player.team().data().units.each(Unitc::isCommandable, commandUnits::add);
            }
            if (Keybind.select_all_factories.tap()) {
                commandUnits.clear();
                commandBuildings.clear();

                player.team().data().buildings.each(b -> b.block.commandable, commandBuildings::add);
            }
            if (Keybind.deselect.tap()) {
                commandUnits.clear();
                commandBuildings.clear();
            }

            if (Keybind.attack.tap() && !scene.hasMouse()) {
                if (commandUnits.any()) {
                    var unit = selectedEnemy();
                    var build = selectedBuilding();

                    if (build != null && build.team == player.team()) build = null;

                    int[] ids = commandUnits.mapInt(Unit::id).toArray();
                    int chunkSize = 128;

                    if (ids.length <= chunkSize)
                        Call.commandUnits(player, ids, build, unit, input.mouseWorld().cpy());

                    else for (int i = 0; i < ids.length; i += chunkSize) {
                        int[] chunk = Arrays.copyOfRange(ids, i, Math.min(i + chunkSize, ids.length));
                        Call.commandUnits(player, chunk, build, unit, input.mouseWorld().cpy());
                    }
                }
                if (commandBuildings.any()) Call.commandBuilding(player, commandBuildings.mapInt(Building::pos).toArray(), input.mouseWorld().cpy());
            }
        }
        if (controlMode = Keybind.control.down() && !scene.hasMouse() && state.rules.possessionAllowed) {
            if (!Keybind.select.tap()) return;

            var unit = selectedUnit();
            var build = selectedBuilding();

            if (build != null && build.team != player.team()) build = null;

            if (unit != null)
                Call.unitControl(player, unit);
            else if (build != null && build instanceof ControlBlock c && c.canControl() && c.unit().isAI())
                Call.unitControl(player, c.unit());
            else if (build != null)
                Call.buildingControlSelect(player, build);
        }
    }

    @Override
    protected void updateState() {}

    @Override
    protected void drawPlans() {}

    @Override
    protected void drawOverlay() {}
}
