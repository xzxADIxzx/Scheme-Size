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
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static schema.Main.*;

/// Input system controlling the units under command, construction plans, and other aspects of the game.
public abstract class InputSystem
{
    /// Current zoom of the camera applied during a [predraw][EventType.Trigger#preDraw] call.
    protected float zoom = 32f, dest = 32f;
    /// Extreme zoom values: the minimum value is zoom in; hence, the maximum one is zoom out.
    protected float minZoom = 16f, maxZoom = 256f;

    /// Whether the command/control mode is on.
    protected boolean commandMode, controlMode;
    /// Origin of the unit selection rectangle.
    protected Vec2 commandRect;
    /// Controlled units.
    protected Seq<Unit> commandUnits = new Seq<>();
    /// Controlled buildings.
    protected Seq<Building> commandBuildings = new Seq<>();
    /// Control mode overlay.
    protected float controlFade;
    /// Last controlled unit.
    protected Sized controlUnit;

    /// Selected block, the one in your hand.
    public Block block;
    /// Whether the building is ongoing or paused.
    public boolean building;

    // region general

    /// Updates the logic of the input system.
    protected abstract void update();

    /// Updates the state of the input system.
    protected abstract void updateState();

    /// Draws the building plans of the player and its teammates.
    public abstract void drawPlans();

    /// Draws the remaining elements of the interface.
    public abstract void drawOverlay();

    // endregion
    // region draw

    /// Draws the command mode overlay.
    protected void drawCommand()
    {
        if (commandRect != null)
        {
            renderer.effectBuffer.begin(Color.clear);

            Draw.color(Pal.accent, .8f);
            Fill.crect(commandRect.x, commandRect.y, input.mouseWorldX() - commandRect.x, input.mouseWorldY() - commandRect.y);

            renderer.effectBuffer.end();
            renderer.effectBuffer.blit(Shaders.buildBeam);

            selectedRegion(u ->
            {
                if (!commandUnits.contains(u)) Drawf.poly(u.x, u.y, 6, u.hitSize + Mathf.absin(Time.time - u.dst(Vec2.ZERO), 4f, 1f), 0f, Pal.accent);
            });
        }
        commandUnits.each(u -> Draw.draw(u.isFlying() ? Layer.flyingUnitLow - 1f : Layer.groundUnit - 1f, () ->
        {
            var ai = u.command();
            var dest = ai.attackTarget != null ? ai.attackTarget : ai.targetPos;
            if (dest != null && ai.currentCommand().drawTarget)
            {
                Drawf.limitLine(u, dest, u.hitSize, 3f);

                if (ai.attackTarget == null)
                    Drawf.square(dest.getX(), dest.getY(), 3f);
                else
                    Drawf.target(dest.getX(), dest.getY(), 5f, Pal.remove);
            }
            Drawf.poly(u.x, u.y, 6, u.hitSize, 0f, Pal.accent);

            // TODO add support for queue and loops
        }));
        commandBuildings.each(b ->
        {
            var dest = b.getCommandPosition();
            if (dest != null)
            {
                Drawf.limitLine(b, dest, b.hitSize() / 2f, 3f);
                Drawf.square(dest.getX(), dest.getY(), 3f);
            }
            Drawf.square(b.x, b.y, b.hitSize() / 2f);
        });
        if (commandRect == null || commandRect.within(input.mouseWorld(), 8f))
        {
            var unit  = selectedUnit(true);
            var build = selectedBuilding();

            if (unit != null)
                Drawf.poly(unit.x, unit.y, 6, unit.hitSize + Mathf.absin(4f, 1f), 0f, commandUnits.contains(unit) ? Pal.remove : Pal.accent);

            else if (build != null && build.team == player.team() && build.block.commandable)
                Drawf.square(build.x, build.y, build.hitSize() / 2f + Mathf.absin(4f, 1f), commandBuildings.contains(build) ? Pal.remove : Pal.accent);
        }
    }

    /// Draws the control mode overlay.
    protected void drawControl()
    {
        var unit  = selectedUnit(true);
        var build = selectedBuilding();

        if (unit == null && build instanceof ControlBlock c && c.canControl() && !c.isControlled()) unit = c.unit();

        boolean has = unit != null || (build != null && build.team == player.team() && build.canControlSelect(player.unit()));
        controlFade = Mathf.lerpDelta(controlFade, Mathf.num(has), .1f);

        if (has)
        {
            Draw.mixcol(Pal.accent, 1f);
            Draw.alpha(controlFade);
            overlay.capture(1f);

            if (build != null) Draw.rect(build.block.fullIcon, build, 0f);

            Sized sized = unit != null ? unit : build;
            float count = 1.4f + sized.hitSize() / 8f;
            float space = 360f / Mathf.floor(count);
            controlUnit = sized;

            for (int i = 1; i < count; i++)
            {
                float len = sized.hitSize() + 16f - controlFade * 8f;
                float rot = i * space - Time.time % 360f;

                Draw.rect("select-arrow", sized.getX() + Angles.trnsx(rot, len), sized.getY() + Angles.trnsy(rot, len), 12f, 12f, rot - 135f);
            }

            overlay.render();
            Draw.reset();
        }
    }

    /// Returns the control mode alpha.
    public float fade(Unit unit) { return controlUnit == unit ? controlFade : 0f; }

    // endregion
    // region tools

    /// Lerps the camera to the given target.
    public void lerpCam(Vec2 target) { camera.position.lerpDelta(target, .064f); }

    /// Poses the camera to the given target.
    public void poseCam(Vec2 target) { camera.position.set(target); }

    /// Moves the camera by the given offset.
    public void moveCam(Vec2 offset) { camera.position.add(offset); }

    /// Returns a unit under the mouse.
    public Unit selectedUnit()
    {
        var mouse = input.mouseWorld();
        return Units.closest(player.team(), mouse.x, mouse.y, u -> u.type.playerControllable && u.isAI() && u.within(mouse, u.hitSize));
    }

    /// Returns an enemy under the mouse. TODO merge with selectedUnit
    public Unit selectedEnemy()
    {
        var mouse = input.mouseWorld(); var team = player.team();
        return Groups.unit.intersect(mouse.x - 1f, mouse.y - 1f, 2f, 2f).min(u -> u.team != team && u.targetable(team) && !u.inFogTo(team), u -> u.dst(mouse));
    }

    /// Returns a building under the mouse.
    public Building selectedBuilding()
    {
        var mouse = input.mouseWorld();
        return world.buildWorld(mouse.x, mouse.y);
    }

    /// Iterates all units in the unit selection rectangle.
    public void selectedRegion(Cons<Unit> cons)
    {
        Tmp.r1.set(commandRect.x, commandRect.y, input.mouseWorldX() - commandRect.x, input.mouseWorldY() - commandRect.y).normalize();
        player.team().data().tree().intersect(Tmp.r1, cons);
    }

    /// Whether the command mode is on.
    public boolean controlling() { return commandMode; }

    /// Returns the amount of controlled units.
    public int controlledUnitsAmount() { return commandUnits.size; }

    /// Returns the amount of controlled units of each type.
    public int[] controlledUnitsAmountByType()
    {
        int[] counts = new int[content.units().size];
        commandUnits.each(u -> counts[u.type.id]++);
        return counts;
    }

    /// Releases all units that match the given predicate.
    public void releaseUnits(Boolf<Unit> pred) { commandUnits.removeAll(pred); }

    /// Commands all units to perform the given command.
    public void commandUnits(UnitCommand command) { Call.setUnitCommand(player, commandUnits.mapInt(Unitc::id).toArray(), command); }

    // endregion
    // region agent

    /// Creates a new agent.
    public Agent agent() { return new Agent(); }

    /// Agent redirecting method calls from the original component.
    public class Agent extends mindustry.input.InputHandler
    {
        @Override
        public void add()
        {
            Events.run(EventType.Trigger.preDraw, () ->
            {
                zoom = Mathf.lerpDelta(zoom, dest, .1f);
                if (Mathf.equal(zoom, dest, .001f)) zoom = dest;

                camera.height = zoom * tilesize;
                camera.width = camera.height * graphics.getAspect();
            });
        }

        @Override
        public void remove() { }

        @Override
        public void update() { insys.update(); }

        @Override
        public void updateState() { insys.updateState(); }

        @Override
        public void updateSelectQuadtree() { }

        @Override
        public boolean isPlacing() { return insys.block != null || Keybind.rebuild.down() & !scene.hasKeyboard(); }

        @Override
        public void useSchematic(Schematic sch, boolean checkHidden) { } // TODO implement

        @Override
        public void getSyncedPlans(Seq<BuildPlan> out) { } // TODO implement
    }

    // endregion
}
