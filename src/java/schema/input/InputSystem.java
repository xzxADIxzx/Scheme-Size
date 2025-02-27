package schema.input;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ai.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/** Input system that controls the building unit, construction plans and many components of the mod. */
public abstract class InputSystem {

    /** Current zoom of the camera that is applied during {@link EventType.Trigger#preDraw predraw} event call. */
    protected float zoom = 32f, dest = 32f;
    /** Extreme zoom values: the maximum value is zoom in and the minimum one is zoom out. */
    protected float minZoom = 16f, maxZoom = 256f;

    /** Whether the input system is in unit command/control mode. */
    protected boolean commandMode, controlMode;
    /** Origin of the unit selection rectangle. */
    protected Vec2 commandRect;
    /** Units that are controlled by the player. */
    protected Seq<Unit> commandUnits = new Seq<>();
    /** Buildings that are controlled by the player. */
    protected Seq<Building> commandBuildings = new Seq<>();

    // region general

    /** Updates the main logic of the input system. */
    protected abstract void update();

    /** Updates the state of the input system. */
    protected abstract void updateState();

    /** Draws the building plans of the player and its teammates. */
    protected abstract void drawPlans();

    /** Draws the remaining elements of the interface. */
    protected abstract void drawOverlay();

    // endregion
    // region draw

    /** Draws the command mode overlay */
    protected void drawCommand() {
        if (commandRect != null) {
            renderer.effectBuffer.begin(Color.clear);

            Draw.color(Pal.accent, .8f);
            Fill.crect(commandRect.x, commandRect.y, input.mouseWorldX() - commandRect.x, input.mouseWorldY() - commandRect.y);

            renderer.effectBuffer.end();
            renderer.effectBuffer.blit(Shaders.buildBeam);

            selectedRegion(u -> { if (!commandUnits.contains(u)) Drawf.square(u.x, u.y, u.hitSize / 1.4f + Mathf.absin(4f, 1f)); });
        }
        commandUnits.each(Unitc::isCommandable, u -> {
            var ai = u.command();
            var dest = ai.attackTarget != null ? ai.attackTarget : ai.targetPos;
            if (dest != null && ai.currentCommand().drawTarget) {

                Drawf.limitLine(u, dest, u.hitSize / 1.4f, 3f);

                if (ai.attackTarget == null)
                    Drawf.square(dest.getX(), dest.getY(), 3f);
                else
                    Drawf.target(dest.getX(), dest.getY(), 5f, Pal.remove);
            }
            Drawf.square(u.x, u.y, u.hitSize / 1.4f);
        });
        commandBuildings.each(b -> {
            var dest = b.getCommandPosition();
            if (dest != null) {

                Drawf.limitLine(b, dest, b.hitSize() / 2f, 3f);
                Drawf.square(dest.getX(), dest.getY(), 3f);
            }
            Drawf.square(b.x, b.y, b.hitSize() / 2f);
        });
        if (commandRect == null || commandRect.within(input.mouseWorld(), 8f)) {
            var unit = selectedUnit();
            var build = selectedBuilding();

            if (unit != null)
                Drawf.square(unit.x, unit.y, unit.hitSize / 1.4f + Mathf.absin(4f, 1f), commandUnits.contains(unit) ? Pal.remove : Pal.accent);

            else if (build != null && build.team == player.team() && build.block.commandable)
                Drawf.square(build.x, build.y, build.hitSize() / 2f + Mathf.absin(4f, 1f), commandBuildings.contains(build) ? Pal.remove : Pal.accent);
        }
    }

    // endregion
    // region tools

    /** Sets the position of the camera to the given one. */
    public void setCam(Vec2 pos) { camera.position.set(pos); }

    /** Moves the camera by the given offset. */
    public void moveCam(Vec2 offset) { camera.position.add(offset); }

    /** Lerps the camera to the given target. */
    public void lerpCam(Vec2 target) { camera.position.lerpDelta(target, .064f); }

    /** Returns the unit under the mouse. */
    public Unit selectedUnit() {
        var mouse = input.mouseWorld();
        return Units.closest(player.team(), mouse.x, mouse.y, u -> u.type.playerControllable && u.isAI() && u.within(mouse, u.hitSize));
    }

    /** Returns the enemy under the mouse. */
    public Unit selectedEnemy() {
        var mouse = input.mouseWorld(); var team = player.team();
        return Groups.unit.intersect(mouse.x - 1f, mouse.y - 1f, 2f, 2f).min(u -> u.team != team && u.targetable(team) && !u.inFogTo(team), u -> u.dst(mouse));
    }

    /** Returns the building under the mouse. */
    public Building selectedBuilding() {
        var mouse = input.mouseWorld();
        return world.buildWorld(mouse.x, mouse.y);
    }

    /** Iterates all units in the selection rectangle. */
    public void selectedRegion(Cons<Unit> cons) {
        Tmp.r1.set(commandRect.x, commandRect.y, input.mouseWorldX() - commandRect.x, input.mouseWorldY() - commandRect.y).normalize();
        player.team().data().tree().intersect(Tmp.r1, cons);
    }

    /** Returns true if the input system is in unit command mode. */
    public boolean controlling() { return commandMode; }

    /** Returns the amount of controlled units. */
    public int controlledUnitsAmount() { return commandUnits.size; }

    /** Returns the amount of controlled units of each type. */
    public int[] controlledUnitsAmountByType() {
        int[] counts = new int[content.units().size];
        commandUnits.each(u -> counts[u.type.id]++);
        return counts;
    }

    /** Iterates all units that are controlled by the player and frees the ones that match the predicate. */
    public void freeUnits(Boolf<Unit> pred) { commandUnits.removeAll(pred); }

    /** Commands all units to perform the given task. */
    public void commandUnits(UnitCommand command) { Call.setUnitCommand(player, commandUnits.mapInt(Unitc::id).toArray(), command); }

    // endregion
    // region agent

    /** Returns the agent of this system. */
    public Agent getAgent() { return new Agent(); }

    /** Agent that redirects method calls from the original handler to the new one. */
    public class Agent extends InputHandler {

        @Override
        public void add() { Events.run(EventType.Trigger.preDraw, () -> {
            zoom = Mathf.lerpDelta(zoom, dest, .1f);
            if (Mathf.equal(zoom, dest, .001f)) zoom = dest;

            camera.height = zoom * tilesize;
            camera.width = camera.height * graphics.getAspect();
        }); }

        @Override
        public void remove() {}

        @Override
        public void update() { insys.update(); }

        @Override
        public void updateState() { insys.updateState(); }

        @Override
        public void drawBottom() { insys.drawPlans(); }

        @Override
        public void drawTop() { insys.drawOverlay(); }

        @Override
        public void panCamera(Vec2 pos) { insys.setCam(pos); }

        @Override
        public boolean isPlacing() { return false; }

        @Override
        public boolean isBreaking() { return false; }

        @Override
        public boolean isRebuildSelecting() { return false; }

        @Override
        public Unit selectedUnit() { return null; }

        @Override
        public Building selectedControlBuild() { return null; }
    }

    // endregion
}
