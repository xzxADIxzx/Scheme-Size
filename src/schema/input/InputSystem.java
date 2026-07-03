package schema.input;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.*;
import mindustry.ai.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.game.Teams.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.InputHandler.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.ConstructBlock.*;

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

    /// Latest position of the mouse in the world.
    protected Vec2 mouse = new Vec2();
    /// Latest position of the mouse on the screen.
    protected Vec2 panel = new Vec2();

    /// Last known coordinates of the mouse.
    protected int lastX, lastY;
    /// Last known coordinates of the lines.
    protected int lineX, lineY;

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

    /// Plans to be builded.
    protected Seq<BuildPlan> plans = new Seq<>();
    /// Plans to be flushed.
    protected Seq<BuildPlan> rough = new Seq<>();

    /// Selected block, the one in your hand.
    public Block block;
    /// Whether the building is ongoing or paused.
    public boolean building, dropping;

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
    // region regions

    /// Draws a region using the specified colors and flags.
    public void region(Color c1, Color c2, boolean breaks, boolean repair, boolean plan, boolean team, boolean text)
    {
        var area = grids.normalize(lineX, lineY, lastX, lastY, maxSchematicSize);
        var draw = grids.normalize(area);

        Lines.stroke(2f, c2);
        Lines.rect(draw.x1, draw.y1 - 1f, draw.width(), draw.height());
        Lines.stroke(2f, c1);
        Lines.rect(draw.x1, draw.y1 - 0f, draw.width(), draw.height());

        Tmp.r2.set(draw.x1, draw.y1, draw.width(), draw.height());

        if (breaks) grids.iterate(area, (x, y) ->
        {
            var build = world.build(x, y);
            if (build != null && Build.validBreak(player.team(), x, y)) Drawf.selected(build, c1);
        });

        if (repair) grids.iterate(area, (x, y) ->
        {
            var build = world.build(x, y);
            if (build != null && repairable(build)) Drawf.selected(build, c1);
        });

        if (plan) plans.each(p ->
        {
            if (p.block.bounds(p.x, p.y, Tmp.r1).overlaps(Tmp.r2))
                Drawf.selected(p.x, p.y, p.block, c1);
        });

        if (team) player.team().data().plans.each(p ->
        {
            if (p.block.bounds(p.x, p.y, Tmp.r1).overlaps(Tmp.r2))
                Drawf.selected(p.x, p.y, p.block, c1);
        });

        if (text) Drawf.text
        (
            area.width() + "x" + area.height() + " (" + area.width() * area.height() + ")",
            mouse.x + zoom / 6f,
            mouse.y + zoom * 0f,
            c1,
            zoom / 40f,
            Align.left
        );
    }

    /// Frees a region from any sort of buildings or plans.
    public void free(Cons<BlockPlan> each, boolean breaks, boolean repair)
    {
        var area = grids.normalize(lineX, lineY, lastX, lastY, maxSchematicSize);
        var draw = grids.normalize(area);

        Tmp.r2.set(draw.x1, draw.y1, draw.width(), draw.height());

        if (each == null) plans.removeAll(p -> p.block.bounds(p.x, p.y, Tmp.r1).overlaps(Tmp.r2));

        if (breaks) grids.iterate(area, (x, y) ->
        {
            var build = world.build(x, y);
            if (build != null && Build.validBreak(player.team(), x, y)) plans.add(new BuildPlan(build.tileX(), build.tileY()));
        });

        if (repair) grids.iterate(area, (x, y) ->
        {
            var build = world.build(x, y);
            if (build != null && repairable(build)) plans.add(new BuildPlan(build.tileX(), build.tileY(), build.rotation, build.block, build.config()));
        });

        if (each == null) plans.removeAll(p -> plans.contains(o -> o != p && o.samePos(p)));

        var itr = player.team().data().plans.iterator();
        var seq = Pools.obtain(IntSeq.class, IntSeq::new);

        while (itr.hasNext())
        {
            var p = itr.next();
            if (p.block.bounds(p.x, p.y, Tmp.r1).overlaps(Tmp.r2))
            {
                if (each == null)
                {
                    itr.remove();
                    seq.add(Point2.pack(p.x, p.y));
                }
                else each.get(p);
            }
        }
        if (seq.size > 0 && net.active()) Call.deletePlans(player, seq.toArray());

        seq.clear();
        Pools.free(seq);
    }

    /// Copies a region.
    public void copyRegion() { control.input.useSchematic(schematics.create(lineX, lineY, lastX, lastY)); }

    /// Breaks a region.
    public void breakRegion() { free(null, true, false); }

    /// Clears a region.
    public void clearRegion() { free(null, false, false); }

    /// Rebuilds a region.
    public void rebuildRegion() { free(p -> plans.add(new BuildPlan(p.x, p.y, p.rotation, p.block, p.config)), false, true); }

    // endregion
    // region draw

    @SuppressWarnings("unchecked")
    QueryEachable query = new QueryEachable(null, plans, rough);

    /// Draws all plans of all players.
    protected void drawPlayers()
    {
        final Boolf<BuildPlan> drawable = p -> !p.breaking && !p.initialized;

        plans.each(drawable, p -> p.animScale = Mathf.lerpDelta(p.animScale, 1f, .2f));
        rough.each(drawable, p -> p.animScale = Mathf.lerpDelta(p.animScale, 1f, .2f));

        plans.each(drawable, p -> p.block.drawPlan(p, query, placeable(p, true, false)));
        rough.each(drawable, p -> p.block.drawPlan(p, query, placeable(p, true, false)));

        plans.each(drawable, p -> p.block.drawPlanConfigTop(p, query));
        rough.each(drawable, p -> p.block.drawPlanConfigTop(p, query));

        plans.each
        (
            p -> p.breaking && !p.initialized,
            p -> Drawf.selected(p.x, p.y, p.block, Pal.remove)
        );

        control.input.drawOtherBuildPlans();

        Groups.player.each(p -> p != player && !p.dead(), p ->
        {
            Drawf.limitLine(p, Tmp.v3.set(p.mouseX, p.mouseY), p.unit().hitSize + 2f, 0f, Tmp.c1.set(p.team().color).a(.6f));
        });
    }

    /// Draws the command mode overlay.
    protected void drawCommand()
    {
        if (commandRect != null)
        {
            renderer.effectBuffer.begin(Color.clear);

            Draw.color(Pal.accent, .8f);
            Fill.crect(commandRect.x, commandRect.y, mouse.x - commandRect.x, mouse.y - commandRect.y);

            renderer.effectBuffer.end();
            renderer.effectBuffer.blit(Shaders.buildBeam);

            selectedRegion(u ->
            {
                if (!commandUnits.contains(u)) Drawf.poly(u.x, u.y, 6, u.hitSize + Mathf.absin(Time.time - u.dst(Vec2.ZERO), 4f, 1f), 0f, Pal.accent);
            });
        }
        commandUnits.each(u -> overlay.post(u.isFlying() ? Layer.flyingUnitLow - 1f : Layer.groundUnit - 1f, () ->
        {
            // a unit may become uncommandable between this#update and renderer#draw calls
            if (!u.isCommandable()) return;

            var ai = u.command();
            var dest = ai.attackTarget != null ? ai.attackTarget : ai.targetPos;
            if (dest != null)
            {
                Drawf.limitLine(u, dest, u.hitSize + 2f, 4f, Tmp.c1.set(Pal.accent).a(.6f));

                if (ai.attackTarget == null)
                    Drawf.square(dest.getX(), dest.getY(), 3f);
                else
                    Drawf.target(dest.getX(), dest.getY(), 4f, Pal.remove);
            }
            Drawf.poly(u.x, u.y, 6, u.hitSize, 0f, Pal.accent);

            Position[] last = { dest };
            ai.commandQueue.each(c ->
            {
                Drawf.limitLine(last[0], c, 4f, 4f, Tmp.c1.set(Pal.accent).a(.6f));

                if (c instanceof Vec2)
                    Drawf.square(c.getX(), c.getY(), 3f);
                else
                    Drawf.target(c.getX(), c.getY(), 4f, Pal.remove);

                last[0] = c;
            });

            if (dest != null && ai.currentCommand() == UnitCommand.loopPayloadCommand && u instanceof Payloadc p)
            {
                Draw.color(Pal.accent, .4f + Mathf.absin(4f, .4f));
                Draw.rect
                (
                    p.hasPayload() ? Icon.download.getRegion() : Icon.upload.getRegion(),
                    dest.getX(),
                    dest.getY() + 12f,
                    8f, 8.27f
                );
                if (ai.commandQueue.size >= 1) Draw.rect
                (
                    p.hasPayload() ? Icon.upload.getRegion() : Icon.download.getRegion(),
                    ai.commandQueue.first().getX(),
                    ai.commandQueue.first().getY() + 12f,
                    8f, 8.27f
                );
            }
        }));
        commandBuildings.each(b ->
        {
            var dest = b.getCommandPosition();
            if (dest != null)
            {
                Drawf.limitLine(b, dest, b.hitSize() / 2f + 2f, 4f, Tmp.c1.set(Pal.accent).a(.6f));
                Drawf.square(dest.getX(), dest.getY(), 3f);
            }
            Drawf.square(b.x, b.y, b.hitSize() / 2f);
        });
        if (commandRect == null || commandRect.within(mouse, 8f))
        {
            var unit  = selectedUnit(true);
            var build = selectedBuilding();

            if (unit != null)
                Drawf.poly(unit.x, unit.y, 6, unit.hitSize + Mathf.absin(4f, 1f), 0f, commandUnits.contains(unit) ? Pal.remove : Pal.accent);

            else if (build != null && build.team == player.team() && build.isCommandable())
                Drawf.square(build.x, build.y, build.hitSize() / 2f + Mathf.absin(4f, 1f), commandBuildings.contains(build) ? Pal.remove : Pal.accent);
        }
    }

    /// Draws the control mode overlay.
    protected void drawControl()
    {
        var unit  = selectedUnit(true);
        var build = selectedBuilding();

        if (unit == null && build instanceof ControlBlock c && c.canControl() && !c.isControlled()) unit = c.unit();

        boolean has = (unit != null && unit.team == player.team()) || (build != null && build.team == player.team() && builds.controllable(build));
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

    /// Draws the item that is dropped.
    protected void drawDropped()
    {
        boolean invalid = !clickable();

        var unit = player.unit();
        if (unit == null) return;

        var build = selectedBuilding();
        if (build != null && build.team == player.team() && build.acceptStack(unit.item(), unit.stack.amount, unit) > 0 && player.within(build, itemTransferRange))
        {
            if (invalid = !build.allowDeposit()) build.block.drawPlaceText(bundle.get("bar.onlycoredeposit"), build.tileX(), build.tileY(), false);
        }

        Lines.stroke(1f, invalid ? Pal.remove : Pal.accent);
        overlay.capture(2f);

        Lines.circle(mouse.x, mouse.y, 6f + Mathf.absin(4f, 1f));

        overlay.render();
        Draw.reset();
        Draw.rect(unit.item().fullIcon, mouse, 8f, 8f);
    }

    /// Returns the control mode alpha.
    public float fade(Unit unit) { return controlUnit == unit ? controlFade : 0f; }

    // endregion
    // region tools

    /// Position of the hand under the mouse.
    public int handX() { return block == null ? tileX() : Math.round((mouse.x - block.offset) / tilesize); }

    /// Position of the hand under the mouse.
    public int handY() { return block == null ? tileY() : Math.round((mouse.y - block.offset) / tilesize); }

    /// Position of the tile under the mouse.
    public int tileX() { return Math.round(mouse.x / tilesize); }

    /// Position of the tile under the mouse.
    public int tileY() { return Math.round(mouse.y / tilesize); }

    /// Returns whether hands have any block.
    public boolean any() { return block != null || rough.any(); }

    /// Returns the line position to default.
    public void deline() { lastX = lastY = lineX = lineY = -01; }

    /// Lerps the camera to the given target.
    public void lerpCam(Vec2 target) { camera.position.lerpDelta(target, .064f); }

    /// Poses the camera to the given target.
    public void poseCam(Vec2 target) { camera.position.set(target); }

    /// Moves the camera by the given offset.
    public void moveCam(Vec2 offset) { camera.position.add(offset); }

    /// Inspects the content under the mouse.
    public void inspect(boolean use)
    {
        Cons2<UnlockableContent, Object> ret = (content, config) ->
        {
            if (content instanceof Block b)
            {
                if (use && polyblock.unlocked(b))
                {
                    block = b;
                    block.lastConfig = config;
                }
                else if (polyblock.unlocked(b)) ui.content.show(content);
            }
            else if (content.unlockedNowHost()) ui.content.show(content);
        };
        if (!use)
        {
            if (block != null)
            {
                ret.get(block, null);
                return;
            }

            var unit = selectedUnit(true);
            if (unit == null)
                unit = selectedUnit(false);
            if (unit != null)
            {
                ret.get(unit.type, null);
                return;
            }
        }
        {
            var plan = selectedPlan(true);
            if (plan == null)
                plan = selectedPlan(false);
            if (plan != null)
            {
                ret.get(plan.block, plan.config);
                return;
            }

            var build = selectedBuilding();
            if (build != null && !build.inFogTo(player.team())) ret.get(build instanceof ConstructBuild c ? c.current : build.block, build.config());
        }
    }

    /// Checks the plan's placeability.
    public boolean placeable(BuildPlan plan, boolean ignorePlans, boolean ignoreUnits)
    {
        plan.block.bounds(plan.x, plan.y, Tmp.r2);
        return
        (
            (ignorePlans || !plans.contains(p -> p != plan && p.block.bounds(p.x, p.y, Tmp.r1).overlaps(Tmp.r2) && !plan.block.canReplace(p.block)))
            &&
            Build.validPlaceIgnoreUnits(plan.block, player.team(), plan.x, plan.y, plan.rotation, true, true)
            &&
            Build.checkNoUnitOverlap   (plan.block,                plan.x, plan.y) | ignoreUnits
        );
    }

    /// Checks the bld's repairability.
    public boolean repairable(Building build)
    {
        return !state.rules.editor
            && !player.dead()
            && player.team() != Team.derelict
            && build.team == Team.derelict
            && polyblock.unlocked(build.block)
            && Build.validPlace(build.block, player.team(), build.tileX(), build.tileY(), build.rotation);
    }

    /// Checks the unit's clickability.
    public boolean clickable()
    {
        return !player.dead() && player.unit().hasItem() && player.within(mouse, mobile ? 17f : 11f);
    }

    /// Returns a plan under the mouse.
    public BuildPlan selectedPlan(boolean owns)
    {
        if (owns)
            return plans.find(p -> p.block.bounds(p.x, p.y, Tmp.r1).contains(mouse));
        else
        {
            for (var other : Groups.player) if (other != player)
            {
                var plan = other.getPreviewPlans().find(p -> p.block.bounds(p.x, p.y, Tmp.r1).contains(mouse));
                if (plan != null) return plan;
            }
            return null;
        }
    }

    /// Returns a unit under the mouse.
    public Unit selectedUnit(boolean ally)
    {
        if (ally)
            return Units.closest(player.team(), mouse.x, mouse.y, 8f, u -> u.isCommandable() && u.within(mouse, u.hitSize));
        else
            return Units.closestEnemy(player.team(), mouse.x, mouse.y, 8f, _ -> true);
    }

    /// Returns a building under the mouse.
    public Building selectedBuilding() { return world.buildWorld(mouse.x, mouse.y); }

    /// Iterates ally units in the unit selection rectangle.
    public void selectedRegion(Cons<Unit> cons)
    {
        Tmp.r1.set(commandRect.x, commandRect.y, mouse.x - commandRect.x, mouse.y - commandRect.y).normalize();

        player.team().data().tree().intersect(Tmp.r1, u ->
        {
            if (u.isCommandable()) cons.get(u);
        });
    }

    /// Whether the command mode is on.
    public boolean commanding() { return commandMode; }

    /// Releases all units that match the given predicate.
    public void releaseUnits(Boolf<Unit> pred) { commandUnits.removeAll(pred); }

    /// Commands all units to perform the given command.
    public void commandUnits(UnitCommand command) { units.slice(commandUnits, u -> u.type.allowCommand(u, command), (b, _) -> Call.setUnitCommand(player, b, command)); }

    /// Commands all units to perform the given stance.
    public void commandUnits(UnitStance stance, boolean on) { units.slice(commandUnits, u -> u.type.allowStance(u, stance), (b, _) -> Call.setUnitStance(player, b, stance, on)); }

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
            Events.run(Trigger.preDraw, () ->
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
        public boolean isPlacing() { return any() || (Keybind.break_b.down() || Keybind.clear_b.down() || Keybind.rebuild.down()) & !scene.hasKeyboard(); }

        @Override
        public void useSchematic(Schematic schem, boolean checkHidden) { rough.set(schematics.toPlans(schem, tileX(), tileY(), checkHidden)); }

        @Override
        public void getSyncedPlans(Seq<BuildPlan> out) { plans.each(p -> !p.breaking, out::add); }
    }

    // endregion
}
