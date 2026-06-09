package schema.input;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.ai.*;
import mindustry.core.*;
import mindustry.core.GameState.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.net.Packets.*;
import mindustry.world.blocks.*;
import schema.ui.hud.*;
import schema.ui.polygons.Polygon;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/// Handles keyboard input.
public class DesktopInput extends InputSystem
{
    /// Amount of scrolls and their last direction.
    private int scrolls, dir;
    /// Amount of scrolls that increase zoom speed.
    private int aspect = 2;

    /// Building being rotated, nullable.
    private Rotatable toRotate;
    /// Whether a block is being rotated.
    private boolean rotating;

    /// Build plan used to draw the selected block.
    private BuildPlan temp = new BuildPlan() {{ animScale = 1f; }};

    @Override
    protected void update()
    {
        mouse.set(input.mouseWorld());
        panel.set(input.mouse());

        if (scene.getKeyboardFocus() instanceof Polygon p)
        {
            if (Keybind.select.tap()) p.select();
            if (Keybind.deselect.tap()) p.hide();
        }

        if (scene.hasKeyboard() || scene.hasDialog())
        {
            updateAI();
            return;
        }

        updateMovement();
        updateZoom();
        updateCommand();
        updateView();

        if (player.isBuilder() && !commandMode && !controlMode && !mapfrag.shown) updateBuilding();
    }

    protected void updateAI()
    {
        if (player.dead() || state.isPaused()) return;

        var unit = player.unit();
        var type = unit.type;

        // TODO implement miner and builder AI

        var rect = camera.bounds(Tmp.r1).grow(-64f);
        if (rect.contains(unit.x, unit.y))
            unit.wobble();
        else
        {
            Tmp.v4.set
            (
                unit.x < rect.x ? rect.x : unit.x < rect.x + rect.width  ? unit.x : rect.x + rect.width,
                unit.y < rect.y ? rect.y : unit.y < rect.y + rect.height ? unit.y : rect.y + rect.height
            )
            .sub(unit);

            // length of the breaking distance
            var len = unit.vel.len2() / 2f / type.accel;
            // distance from the unit to the edge of the screen
            var dst = Math.max(0f, Tmp.v4.len() - len);

            // TODO implement path finder that is not gonna kill the unit while moving across enemy turrets
            unit.movePref(Tmp.v4.limit(dst).limit(type.speed));
        }
    }

    protected void updateMovement()
    {
        var unit = player.unit();
        var type = player.dead() ? null : unit.type;

        Vec2 mov = Tmp.v1.set(Keybind.move_x.axis(), Keybind.move_y.axis()).nor();
        Vec2 pan = Keybind.pan_mv.down()
            ? Tmp.v2.set(panel).sub(graphics.getWidth() / 2f, graphics.getHeight() / 2f).scl(.004f).limit(1f)
            : Tmp.v2.setZero();
        Vec2 flw = Keybind.mouse_mv.down()
            ? Tmp.v3.set(mouse).sub(player).scl(.016f).limit(1f)
            : Tmp.v3.setZero();

        if (units.coreUnit || player.dead())
        {
            // this type of movement is active most of the time
            // the unit simply follows the camera and performs commands

            moveCam(mov.add(pan).limit(1f).scl(settings.getInt("schema-pan-speed", 6) * (Keybind.boost.down() ? 2.4f : 1f) * Time.delta));

            if (unit != null && type != null) unit.movePref(flw.scl(type.speed));

            updateAI(); // TODO move to Gamma.java or smth
        }
        else
        {
            // this type of movement is only active when the player controls a combat unit
            // inherently, this is the classical movement

            lerpCam(pan.scl(512f).add(player));
            unit.movePref(mov.add(flw).limit(1f).scl(unit.speed()));
        }

        if (Keybind.teleport.tap() && unit != null) unit.set(mouse);

        player.mouseX = mouse.x;
        player.mouseY = mouse.y;

        if (player.dead() || state.isPaused()) return;

        player.shooting = Keybind.shoot.down() && block == null && !(commandMode || controlMode || scene.hasMouse() || config.visible);
        player.boosting = Keybind.boost.down();

        if (Keybind.look_at.down()) unit.rotation = Angles.mouseAngle(unit.x, unit.y);
        else
        {
            if (player.shooting && type.omniMovement && type.faceTarget && type.hasWeapons())
                unit.lookAt(mouse);
            else
                unit.lookAt(unit.prefRotation());
        }

        unit.aim(mouse);
        unit.controlWeapons(true, player.shooting);

        if (Keybind.respawn.tap()) Call.unitClear(player);
        if (Keybind.despawn.tap()) ; // TODO admins/hacky functions

        if (Keybind.pick_cargo.tap()) control.input.tryPickupPayload();
        if (Keybind.drop_cargo.tap()) control.input.tryDropPayload();
    }

    protected void updateZoom()
    {
        if (rotating) return;

        int scroll = (int) Keybind.scroll();
        if (scroll == 0 || scene.hasMouse()) return;

        if (dir != scroll)
        {
            dir = scroll;
            scrolls = 0;
        }
        else scrolls++;

        dest -= scroll * (4f + (scrolls / aspect));
        dest = Mathf.clamp(dest, minZoom, maxZoom);
    }

    protected void updateCommand()
    {
        if (commandMode = Keybind.command.down() && !mapfrag.shown)
        {
            if (Keybind.command.tap()) commandRect = null;

            if (Keybind.select.tap    () && !scene.hasMouse()) commandRect = mouse.cpy();
            if (Keybind.select.release() && commandRect != null)
            {
                if (commandRect.within(mouse, 8f))
                {
                    var unit  = selectedUnit(true);
                    var build = selectedBuilding();

                    if (unit != null)
                    {
                        commandBuildings.clear();
                        if (!commandUnits.remove(unit)) commandUnits.add(unit);
                    }
                    else if (build != null && build.team == player.team() && build.isCommandable())
                    {
                        commandUnits.clear();
                        if (!commandBuildings.remove(build)) commandBuildings.add(build);
                    }
                }
                else
                {
                    commandBuildings.clear();
                    selectedRegion(commandUnits::addUnique);
                }
                commandRect = null;
            }
            if (Keybind.deselect.tap() ||
                Keybind.select_all_units.tap() ||
                Keybind.select_all_units_on_screen.tap() ||
                Keybind.select_all_factories.tap() ||
                Keybind.select_all_factories_on_screen.tap())
            {
                commandUnits.clear();
                commandBuildings.clear();
            }

            if (Keybind.select_all_units              .tap()) player.team().data().units       .each     (Unit    ::isCommandable, commandUnits    ::add);
            if (Keybind.select_all_factories          .tap()) player.team().data().buildings   .each     (Building::isCommandable, commandBuildings::add);
            if (Keybind.select_all_units_on_screen    .tap()) player.team().data().unitTree    .intersect(camera.bounds(Tmp.r1),   commandUnits         );
            if (Keybind.select_all_factories_on_screen.tap()) player.team().data().buildingTree.intersect(camera.bounds(Tmp.r1),   commandBuildings     );

            commandUnits    .retainAll(Unit    ::isCommandable).retainAll(Unit    ::isValid);
            commandBuildings.retainAll(Building::isCommandable).retainAll(Building::isValid);

            if (Keybind.order.tap() | Keybind.queue.tap() && !scene.hasMouse())
            {
                if (commandUnits.any())
                {
                    var unit  = selectedUnit(false);
                    var build = selectedBuilding();

                    units.slice(commandUnits, _ -> true, (batch, last) -> Call.commandUnits
                    (
                        player,
                        batch,
                        build == null || build.team == player.team() ? null : build,
                        unit,
                        mouse.cpy(),
                        Keybind.queue.tap(), last
                    ));
                }
                if (commandBuildings.any()) Call.commandBuilding(player, commandBuildings.mapInt(Building::pos).toArray(), mouse.cpy());
            }
            if (Keybind.cancel.tap()) commandUnits(UnitStance.stop, true);
        }
        if (controlMode = Keybind.control.down() && !mapfrag.shown && !scene.hasMouse() && state.rules.possessionAllowed)
        {
            // do not merge it with the condition above, silly
            if (!Keybind.select.tap()) return;

            var unit  = selectedUnit(true);
            var build = selectedBuilding();

            if (build != null && build.team != player.team()) build = null;

            if (unit != null)
                Call.unitControl(player, unit);

            else if (build != null && build instanceof ControlBlock c && c.canControl() && !c.isControlled())
                Call.unitControl(player, c.unit());

            else if (build != null && build.canControlSelect(player.unit()))
                Call.buildingControlSelect(player, build);
        }
    }

    protected void updateView()
    {
        if (Keybind.menu.tap())
        {
            if (ui.chatfrag.shown())
                ui.chatfrag.hide();

            else if (mapfrag.shown)
                mapfrag.shown = false;

            else
            {
                ui.paused.show();
                if (!net.active() && !state.rules.pauseDisabled) state.set(State.paused);
            }
        }
        if (Keybind.skip.tap())
        {
            if (net.client() && player.admin)
                Call.adminRequest(player, AdminAction.wave, null);
            else
                logic.skipWave();
        }
        if (Keybind.pause.tap() && !net.client())
        {
            if (state.rules.pauseDisabled)
                hudfrag.show(Notification.rules, 3f);
            else
                state.set(state.isPaused() ? State.playing : State.paused);
        }

        if (Keybind.sector_map.tap()) mapfrag.toggle();
        if (Keybind.planet_map.tap() && state.isCampaign()) ui.planet.show();
        if (Keybind.research.tap() && state.isCampaign()) ui.research.show();
        if (Keybind.database.tap()) ui.database.show();

        if (Keybind.inspect.tap()) inspect(false);

        if (Keybind.tgl_menus.tap()) hudfrag.shown = !hudfrag.shown;
        if (Keybind.tgl_ruler.tap()) overlay.ruler = !overlay.ruler;
        if (Keybind.tgl_power_lasers.tap())
        {
            if (settings.getInt("lasersopacity") == 0)
                settings.put("lasersopacity", settings.getInt("preferredlaseropacity", 100));
            else
            {
                settings.put("preferredlaseropacity", settings.getInt("lasersopacity", 100));
                settings.put("lasersopacity", 0);
            }
        }
        if (Keybind.tgl_block_status.tap()) settings.put("blockstatus", !settings.getBool("blockstatus"));
        if (Keybind.tgl_block_health.tap()) settings.put("blockhealth", !settings.getBool("blockhealth"));
    }

    protected void updateBuilding()
    {
        if (Keybind.select.tap())
        {
            var build = selectedBuilding();
            if (block == null && build != null && build.team == player.team())
            {
                inv.show(build);
                config.show(build);
            }
        }
        if (Keybind.deselect.tap())
        {
            block = null;
            inv.hide();
            config.hide();
        }
        if (block != null && inv.visible) inv.hide();
        if (block != null && config.visible) config.hide();

        if (Keybind.hexblock.tap()) polyblock.show(panel);
        if (Keybind.srcblock.tap()) ; // TODO block search fragments & calculator

        if (Keybind.pause_bd.tap()) building = !building;
        if (Keybind.clear_bd.tap()) plans.clear();

        player.unit().updateBuilding(building && !Keybind.mouse_mv.down());

        if (Keybind.ping.tap())
        {
            if (Keymask.any())
                ui.showTextInput("", "", maxPingTextLength, "", r -> Call.pingLocation(player, mouse.x, mouse.y, r));
            else
                Call.pingLocation(player, mouse.x, mouse.y, null);
        }

        if (Keybind.drop.tap    ()) ;
        if (Keybind.drop.release()) ;

        if (Keybind.pick.tap()) inspect(true);

        if (Keybind.rotate.tap    ()) toRotate = new Rotatable(selectedBuilding(), block != null ? temp : null);
        if (Keybind.rotate.release()) toRotate = null;

        temp.block = block; // rotatable uses the plan
        rotating = toRotate != null && toRotate.valid();

        if (rotating)
        {
            if (mouse.within(toRotate, toRotate.radius()))
            {
                int scroll = (int) Keybind.scroll();
                if (scroll != 0) toRotate.rotateBy(scroll);
            }
            else toRotate.rotateTo(Mathf.round(Angles.angle(toRotate.getX(), toRotate.getY(), mouse.x, mouse.y) / 90f) % 4);
        }

        if (Keybind.sel_schematic.tap()) ui.schematics.show();
        if (Keybind.hex_schematic.tap()) polyschem.show(panel);
    }

    @Override
    protected void updateState()
    {
        if (state.isMenu())
        {
            block = null;
            building = true;
            toRotate = null;
        }
        if (Keybind.tgl_fullscreen.tap())
        {
            graphics.setFullscreen(!graphics.isFullscreen());
            settings.put("fullscreen", graphics.isFullscreen());
        }
    }

    @Override
    public void drawPlans()
    {
        drawPlayers();

        if (block == null || commandMode || controlMode) return;

        var tx = rotating ? toRotate.x : World.toTile(mouse.x - block.offset);
        var ty = rotating ? toRotate.y : World.toTile(mouse.y - block.offset);
        var rt = temp.rotation;
        var valid = control.input.validPlace(tx, ty, block, rt);

        temp.set(tx, ty, rt, block);
        temp.config = block.lastConfig;

        block.drawPlan(temp, plans, valid);
        block.drawPlace(tx, ty, rt, valid);

        // that is a really nice thing, wonder why is it off by default?
        control.input.drawOverlapCheck(block, tx, ty, valid);
    }

    @Override
    public void drawOverlay()
    {
        if (commandMode) drawCommand();
        if (controlMode) drawControl();
        else controlFade = 0f;

        if (rotating)
        {
            Lines.stroke(1f, Pal.accent);
            overlay.capture(4f);

            for (int i = 0; i < 4; i++)
            {
                Tmp.v1.trns(i * 90f, toRotate.radius() + 1f).add(toRotate);

                Lines.arc(toRotate.getX(), toRotate.getY(), toRotate.radius(), .2f, i * 90f - 36f);
                Fill.poly(Tmp.v1.x,        Tmp.v1.y,                        3,  2f, i * 90f      );
            }

            overlay.render();
            Draw.reset();
        }
    }
}
