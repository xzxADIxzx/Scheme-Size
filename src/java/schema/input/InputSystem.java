package schema.input;

import arc.math.geom.*;
import mindustry.gen.*;
import mindustry.input.*;

import static arc.Core.*;
import static schema.Main.*;

/** Input system that controls the building unit, construction plans and many components of the mod. */
public abstract class InputSystem {

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
    public void lerpCam(Vec2 target) { camera.position.lerpDelta(target, .08f); }

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
        public Unit selectedUnit() { return Keybind.command.down() ? super.selectedUnit() : null; }

        @Override
        public Building selectedControlBuild() { return Keybind.command.down() ? super.selectedControlBuild() : null; }
    }

    // endregion
}
