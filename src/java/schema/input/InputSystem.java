package schema.input;

import arc.func.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.input.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/** Input system that controls the building unit, construction plans and many components of the mod. */
public abstract class InputSystem {

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

    // endregion
    // region agent

    /** Returns the agent of this system. */
    public Agent getAgent() { return new Agent(); }

    /** Agent that redirects method calls from the original handler to the new one. */
    public class Agent extends InputHandler {

        @Override
        public void add() {}

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
